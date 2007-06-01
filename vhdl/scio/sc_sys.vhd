--
--	sc_sys.vhd
--
--	counter, interrrupt handling and watchdog bit
--
--	Author: Martin Schoeberl	martin@good-ear.com
--
--		address map:
--
--			0	read clk counter, write irq ena
--			1	read 1 MHz counter, write timer val (us) + irq ack
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
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;

entity sc_sys is

generic (addr_bits : integer;
	clk_freq : integer;
	cpu_id	 : integer);
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
	irq_in		: out irq_in_type;
	exc_req		: in exception_type;
	
	sync_out : in sync_out_type;
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
	signal yield_int		: std_logic;
	signal int_ack			: std_logic;

	signal timer			: std_logic;
	signal yield			: std_logic;

	signal irq_cnt			: std_logic_vector(31 downto 0);
	signal timer_equ		: std_logic;
	signal timer_dly		: std_logic;

	signal exc_type			: std_logic_vector(7 downto 0);
		
	signal cpu_identity	: std_logic_vector(31 downto 0);
	
	
begin

	cpu_identity <= std_logic_vector(to_unsigned(cpu_id,32));
	rdy_cnt <= "00";	-- no wait states

--
--	read cnt values
--
process(clk, reset)
begin

	if (reset='1') then
		rd_data <= (others => '0');
	elsif rising_edge(clk) then

		if rd='1' then
			case address(2 downto 0) is
				when "000" =>
					rd_data <= clock_cnt;
				when "001" =>
					rd_data <= us_cnt;
				when "100" =>
					rd_data(7 downto 0) <= exc_type;
					rd_data(31 downto 8) <= (others => '0');
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
process(us_cnt, irq_cnt) begin
	timer_equ <= '0';
	if us_cnt = irq_cnt then
		timer_equ <= '1';
	end if;
end process;

process(clk, reset, timer_equ) begin
	if (reset='1') then
		timer_dly <= '0';
	elsif rising_edge(clk) then
		timer_dly <= timer_equ;
	end if;
end process;

	timer_int <= timer_equ and not timer_dly;

--
--	int processing from timer and yield request
--
process(clk, reset, timer_int, yield_int) begin

	if (reset='1') then
		timer <= '0';
		yield <= '0';
	elsif rising_edge(clk) then
		if int_ack='1' then
			timer <= '0';
			yield <= '0';
		else
			if timer_int='1' then
				timer <= '1';
			end if;
			if yield_int='1' then
				yield <= '1';
			end if;
		end if;
	end if;

end process;

	irq_in.irq <= timer or yield;


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

		irq_in.irq_ena <= '0';
		irq_cnt <= (others => '0');
		int_ack <= '0';
		wd <= '0';
		sync_in.s_in <= '0';

		exc_type <= (others => '0');
		irq_in.exc_int <= '0';

	elsif rising_edge(clk) then

		int_ack <= '0';
		yield_int <= '0';

		irq_in.exc_int <= '0';

		if exc_req.spov='1' then
			exc_type(2 downto 0) <= EXC_SPOV;
			irq_in.exc_int <= '1';
		end if;
		if exc_req.np='1' then
			exc_type(2 downto 0) <= EXC_NP;
			irq_in.exc_int <= '1';
		end if;
		if exc_req.ab='1' then
			exc_type(2 downto 0) <= EXC_AB;
			irq_in.exc_int <= '1';
		end if;

		if wr='1' then
			case address(2 downto 0) is
				when "000" =>
					irq_in.irq_ena <= wr_data(0);
				when "001" =>
					irq_cnt <= wr_data;
					int_ack <= '1';
				when "010" =>
					yield_int <= '1';
				when "011" =>
					wd <= wr_data(0);
				when "100" =>
					exc_type <= wr_data(7 downto 0);
					irq_in.exc_int <= '1';
				when "110" =>
					-- nothing, processor id is read only
				when others =>
--				when "111" =>
					sync_in.s_in <= wr_data(0);
			end case;
		end if;
		
	end if;
end process;

end rtl;
