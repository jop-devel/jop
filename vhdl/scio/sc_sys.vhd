--
--	sc_sys.vhd
--
--	counter, interrrupt handling and watchdog bit
--
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--		address map:
--
--			0	read clk counter, write irq ena
--			1	read 1 MHz counter, write timer val (us)
--			2	write generates sw-int (for yield())
--			3	write wd port
--			4	write generates SW exception, read exception reason
--
--	todo:
--
--
--	2003-07-05	new IO standard
--	2003-08-15	us counter, irq added
--	2005-11-30	change interface to SimpCon
--	2006-01-11	added exception
--	2007-03-17	changed interrupts to records
--  2007-06-01  changed name from sc_cnt to sc_sys
--  2007-11-22  added global lock and bootup of CMP
--	2007-12-02	interrupt processing redesign
--


--
--	state for a single interrupt
--
library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity intstate is

port (
	clk		: in std_logic;
	reset	: in std_logic;

	irq		: in std_logic;		-- external request
	ena		: in std_logic;		-- local enable
	ack		: in std_logic;		-- is served
	clear	: in std_logic;		-- reset pending interrupt
	pending	: out std_logic		-- the output request
);
end intstate;

architecture rtl of intstate is

	signal flag		: std_logic;

begin

--	TODO: add minimum interarrival time

process(clk, reset) begin

	if reset='1' then
		flag <= '0';
	elsif rising_edge(clk) then
		if ack='1' or clear='1' then
			flag <= '0';
		elsif irq='1' then
			flag <= '1';
		end if;
	end if;

end process;

	pending <= flag and ena;

end rtl;

--
--	the sc_sys component
--
library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;

entity sc_sys is

generic (addr_bits : integer;
	clk_freq : integer;
	cpu_id	 : integer;
	num_io_int : integer := 2);
port (
	clk		: in std_logic;
	reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

--
--	Interrupts from IO devices
--
	irq_in		: out irq_bcf_type;
	irq_out		: in irq_ack_type;
	exc_req		: in exception_type;

	io_int		: in std_logic_vector(num_io_int-1 downto 0) := "00";
	
	sync_out : in sync_out_type := NO_SYNC;
	sync_in	 : out sync_in_type;
	
	wd				: out std_logic

);
end sc_sys ;

architecture rtl of sc_sys is

	signal clock_cnt		: std_logic_vector(31 downto 0);
	signal pre_scale		: std_logic_vector(7 downto 0);
	signal us_cnt			: std_logic_vector(31 downto 0);

	constant div_val	: integer := clk_freq/1000000-1;

	signal timer_int		: std_logic;

	signal timer_cnt		: std_logic_vector(31 downto 0);
	signal timer_equ		: std_logic;
	signal timer_dly		: std_logic;

	signal exc_type			: std_logic_vector(7 downto 0);
		
	signal cpu_identity	: std_logic_vector(31 downto 0);
	signal rdy_cnt_help : std_logic;

	
--
--	signals for interrupt handling
--
	signal int_pend		: std_logic;
	signal int_ena		: std_logic;
	signal exc_pend		: std_logic;
	signal irq_gate		: std_logic;
	signal irq_dly		: std_logic;
	signal exc_dly		: std_logic;

--
--	signals for interrupt source state machines
--
	constant NUM_INT	: integer := num_io_int+1;		-- plus timer interrupt
	signal hwreq		: std_logic_vector(NUM_INT-1 downto 0);
	signal swreq		: std_logic_vector(NUM_INT-1 downto 0);
	signal intreq		: std_logic_vector(NUM_INT-1 downto 0);
	signal mask			: std_logic_vector(NUM_INT-1 downto 0);
	signal ack			: std_logic_vector(NUM_INT-1 downto 0);
	signal pending		: std_logic_vector(NUM_INT-1 downto 0);
	signal prioint		: std_logic_vector(4 downto 0);
	signal intnr		: std_logic_vector(4 downto 0);		-- processing int number
	signal clearall		: std_logic;

begin

	cpu_identity <= std_logic_vector(to_unsigned(cpu_id,32));
	rdy_cnt <= "11" when sync_out.release='1' else "00";
	
--
--	read cnt values
--
process(clk, reset)
begin

	if reset='1' then
		rd_data <= (others => '0');
	elsif rising_edge(clk) then

		if rd='1' then
			case address(2 downto 0) is
				when "000" =>
					rd_data <= clock_cnt;
				when "001" =>
					rd_data <= us_cnt;
				when "010" =>
					rd_data(4 downto 0) <= intnr;
					rd_data(31 downto 5) <= (others => '0');
				when "100" =>
					rd_data(7 downto 0) <= exc_type;
					rd_data(31 downto 8) <= (others => '0');
				when "101" =>
					rd_data(0) <= rdy_cnt_help;
					rd_data(31 downto 1) <= (others => '0');				
				when "110" =>
					rd_data <= cpu_identity;
--				when "111" =>
				when others =>
					rd_data(0) <= sync_out.s_out;
					rd_data(31 downto 1) <= (others => '0');
			end case;
		end if;
	end if;

end process;

--
--	compare timer value and us counter
--	and generate single shot
--
process(us_cnt, timer_cnt) begin
	timer_equ <= '0';
	if us_cnt = timer_cnt then
		timer_equ <= '1';
	end if;
end process;

process(clk, reset) begin
	if reset='1' then
		timer_dly <= '0';
	elsif rising_edge(clk) then
		timer_dly <= timer_equ;
	end if;
end process;

	timer_int <= timer_equ and not timer_dly;

--
--	int processing from timer and yield request
--

	hwreq(0) <= timer_int;
	hwreq(NUM_INT-1 downto 1) <= io_int;

process(prioint, irq_out.ack_irq) begin
	ack <= (others => '0');
	ack(to_integer(unsigned(prioint))) <= irq_out.ack_irq;
end process;

	gen_int: for i in 0 to NUM_INT-1 generate
		intreq(i) <= hwreq(i) or swreq(i);
		cis: entity work.intstate
			port map(clk, reset,
				irq => intreq(i),
				ena => mask(i),
				ack => ack(i),
				clear => clearall,
				pending => pending(i)
			);

		
	end generate;

-- find highest priority pending interrupt
process(pending) begin

	int_pend <= '0';
	prioint <= (others => '0');
	for i in NUM_INT-1 downto 0 loop
		if pending(i)='1' then
			int_pend <= '1';
			prioint <= std_logic_vector(to_unsigned(i, 5));
			exit;
		end if;
	end loop;
end process;

--
--	interrupt processing
--
process(clk, reset) begin

	if reset='1' then
		irq_dly <= '0';
		exc_dly <= '0';
		intnr <= (others => '0');

	elsif rising_edge(clk) then

		irq_dly <= irq_gate;
		exc_dly <= exc_pend;
		-- save processing interrupt number
		if irq_out.ack_irq='1' then
			intnr <= prioint;
		end if;

	end if;

end process;

	irq_gate <= int_pend and int_ena;
	irq_in.irq <= irq_gate and not irq_dly;
	irq_in.exc <= exc_pend and not exc_dly;



--
--	counters
--		pre_scale is 8 bit => fmax = 255 MHz
--
process(clk, reset) begin

	if (reset='1') then

		clock_cnt <= (others => '0');
		us_cnt <= (others => '0');
		pre_scale <= std_logic_vector(to_unsigned(div_val, pre_scale'length));

	elsif rising_edge(clk) then

		clock_cnt <= std_logic_vector(unsigned(clock_cnt) + 1);
		pre_scale <= std_logic_vector(unsigned(pre_scale) - 1);
		if pre_scale = "00000000" then
			pre_scale <= std_logic_vector(to_unsigned(div_val, pre_scale'length));
			us_cnt <= std_logic_vector(unsigned(us_cnt) + 1);
		end if;

	end if;
end process;

--
--	io write processing and exception processing
--
process(clk, reset)

begin
	if (reset='1') then

		int_ena <= '0';
		timer_cnt <= (others => '0');
		wd <= '0';
		sync_in.s_in <= '0';
		sync_in.lock <= '0';
		rdy_cnt_help <= '0';

		exc_type <= (others => '0');
		exc_pend <= '0';

		swreq <= (others => '0');
		mask <= (others => '0');
		clearall <= '0';

	elsif rising_edge(clk) then

		exc_pend <= '0';
		swreq <= (others => '0');
		clearall <= '0';

		-- disable interrupts on a taken interrupt or excption
		if irq_out.ack_irq='1' or irq_out.ack_exc='1' then
			int_ena <= '0';
		end if;

		-- exceptions from core or memory
		if exc_req.spov='1' then
			exc_type(2 downto 0) <= EXC_SPOV;
			exc_pend <= '1';
		end if;
		if exc_req.np='1' then
			exc_type(2 downto 0) <= EXC_NP;
			exc_pend <= '1';
		end if;
		if exc_req.ab='1' then
			exc_type(2 downto 0) <= EXC_AB;
			exc_pend <= '1';
		end if;

		if wr='1' then
			case address(3 downto 0) is
				when "0000" =>
					int_ena <= wr_data(0);
				when "0001" =>
					timer_cnt <= wr_data;
				when "0010" =>
					swreq(to_integer(unsigned(wr_data))) <= '1';
				when "0011" =>
					wd <= wr_data(0);
				when "0100" =>
					exc_type <= wr_data(7 downto 0);
					exc_pend <= '1';
				when "0101" =>
					sync_in.lock <= wr_data(0);	
					rdy_cnt_help <= wr_data(0);			
				when "0110" =>
					-- nothing, processor id is read only
				when "0111" =>
					sync_in.s_in <= wr_data(0);
				when "1000" =>
					mask <= wr_data(NUM_INT-1 downto 0);
--				when "1001" =>
				when others =>
					clearall <= '1';
			end case;
		end if;
		
	end if;
end process;

end rtl;
