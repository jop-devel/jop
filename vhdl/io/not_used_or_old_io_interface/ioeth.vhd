--
--	ioeth.vhd
--
--	io devices
--
--
--	io address mapping:
--
--		0	in, out port (8/4 Bit)	not used in bb
--		1	status
--		2	uart data (rd/wr)
--		3	reserved for ecp
--		4	ADC rd, exp/lo write	BB
--		5	isa control and addr, I ADC on BB
--		6	isa data
--
--		7	watch dog bit
--		8	leds, taster			BB
--		9	relais, sensor			BB
--		10	system clock counter
--		11	ms clock counter		obsolete
--		12	display out				BB
--		13	keybord (i/o)			BB
--		14	triac out, sense u/i in	BB
--		15	rs485 data (rd/wr)		BB
--
--	status word:
--		0	uart transmit data register empty
--		1	uart read data register full
--		2	second uart transmit data register empty !not used
--		3	second uart read data register full !not used
--		4	rs485 transmit data register empty	BB
--		5	rs485 read data register full		BB
--
--
--	todo:
--		ff against meta stability on all inputs
--
--
--	2002-12-01	extracted from memioeth.vhd
--	2002-12-07	add led output, incr. in, invert in
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity io is
generic (clk_freq : integer; width : integer);

port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);

-- interface to mem

	rd, wr		: in std_logic;
	addr_wr		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0);

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts			: in std_logic;
	rts			: out std_logic;

-- watch dog

	wd			: out std_logic;

-- ethernet chip

	isa_d		: inout std_logic_vector(7 downto 0);
	isa_a		: out std_logic_vector(4 downto 0);
	isa_reset	: out std_logic;
	isa_nior	: out std_logic;
	isa_niow	: out std_logic;

-- io ports
	i			: in std_logic_vector(10 downto 1);
	o			: out std_logic_vector(4 downto 1);
	lo			: out std_logic_vector(10 downto 1);

-- analog ports
	sdi			: in std_logic_vector(2 downto 1);
	sdo			: out std_logic_vector(2 downto 1)
);
end io;

architecture rtl of io is

component uart is
generic (clk_freq : integer; baud_rate : integer;
	txf_depth : integer; txf_thres : integer;
	rxf_depth : integer; rxf_thres : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	txd		: out std_logic;
	rxd		: in std_logic;

	din		: in std_logic_vector(7 downto 0);		-- send data
	dout	: out std_logic_vector(7 downto 0);		-- rvc data

	wr		: in std_logic;		-- send data
	tdre	: out std_logic;	-- transmit data register empty

	rd		: in std_logic;		-- read data
	rdrf	: out std_logic;	-- receive data register full

	cts		: in std_logic;
	rts		: out std_logic
);
end component uart;

component sigdel is

port (
	clk		: in std_logic;
	reset	: in std_logic;

	dout	: out std_logic_vector(15 downto 0);

	sdi		: in std_logic;
	sdo		: out std_logic
);
end component sigdel;

--
--	signals for indirect addressing
--
	signal addr				: std_logic_vector(3 downto 0);

--
--	signals for uart connection
--
	signal ua_dout			: std_logic_vector(7 downto 0);
	signal ua_wr, ua_tdre	: std_logic;
	signal ua_rd, ua_rdrf	: std_logic;

--
--	signal for isa data bus
--
	signal isa_dout			: std_logic_vector(7 downto 0);
	signal isa_d_wr			: std_logic;
	signal isa_ctrl_wr		: std_logic;
	signal isa_dir			: std_logic;		-- direction of isa_d ('1' means driving out)

--
--	signals for ADC
--
	signal sd_dout			: std_logic_vector(31 downto 0);

--
-- signal for out port, wd
--
	signal o_wr				: std_logic;
	signal lo_wr			: std_logic;
	signal wd_wr			: std_logic;
	signal led				: std_logic_vector(10 downto 1);

--
--	signal for timers
--
	signal clock_cnt		: unsigned (31 downto 0);


begin

	cmp_uart : uart generic map (clk_freq, 115200, 16, 8, 20, 4)
			port map (clk, reset, txd, rxd,
				din(7 downto 0), ua_dout,
				ua_wr, ua_tdre,
				ua_rd, ua_rdrf,
				cts, rts
		);

	cmp_sigdel1 : sigdel
			port map (clk, reset,
				sd_dout(15 downto 0),
				sdi(1), sdo(1)
		);

	cmp_sigdel2 : sigdel
			port map (clk, reset,
				sd_dout(31 downto 16),
				sdi(2), sdo(2)
		);

--
--	isa data bus
--
	isa_d <= isa_dout when isa_dir='1' else "ZZZZZZZZ";

--
--	low activ OC for LEDs
--
	lo(1) <= '0' when led(1)='1' else 'Z';
	lo(2) <= '0' when led(2)='1' else 'Z';
	lo(3) <= '0' when led(3)='1' else 'Z';
	lo(4) <= '0' when led(4)='1' else 'Z';
	lo(5) <= '0' when led(5)='1' else 'Z';
	lo(6) <= '0' when led(6)='1' else 'Z';
	lo(7) <= '0' when led(7)='1' else 'Z';
	lo(8) <= '0' when led(8)='1' else 'Z';
	lo(9) <= '0' when led(9)='1' else 'Z';
	lo(10) <= '0' when led(10)='1' else 'Z';

--
--	read mux
--
process(addr, rd, ua_rdrf, ua_tdre, ua_dout, clock_cnt, isa_d, i, sd_dout)
begin

	dout <= std_logic_vector(to_unsigned(0, width));
	ua_rd <= '0';

	case addr(3 downto 0) is			-- use indirect addressing
		when "0000" =>
			dout <= std_logic_vector(to_unsigned(0, width-10)) & not i;		-- input is low active
		when "0001" =>
			dout <= std_logic_vector(to_unsigned(0, width-2)) & ua_rdrf & ua_tdre;
		when "0010" =>
			dout <= std_logic_vector(to_unsigned(0, width-8)) & ua_dout;
			ua_rd <= rd;
		when "0100" =>
			dout <= sd_dout;
		when "0110" =>
			dout <= std_logic_vector(to_unsigned(0, width-8)) & isa_d;
		when "1010" =>
			dout <= std_logic_vector(clock_cnt);
		when others =>
			null;
	end case;

end process;


--
--	write mux
--
process(wr, addr)
begin

	o_wr <= '0';
	lo_wr <= '0';
	ua_wr <= '0';
	isa_d_wr <= '0';
	isa_ctrl_wr <= '0';
	wd_wr <= '0';

	if (wr='1') then
		case addr(3 downto 0) is
			when "0000" =>
				o_wr <= '1';
			when "0010" =>
				ua_wr <= '1';
			when "0100" =>
				lo_wr <= '1';
			when "0101" =>
				isa_ctrl_wr <= '1';
			when "0110" =>
				isa_d_wr <= '1';
			when "0111" =>
				wd_wr <= '1';
			when others =>
				null;
		end case;
	end if;
end process;

--
--	addr, port buffer, wr_addr write
--		one cycle after io write (address is avaliable one cycle before ex stage)
--
process(clk, reset, din, addr_wr, wd_wr, isa_d_wr, isa_ctrl_wr, o_wr, lo_wr)

begin
	if (reset='1') then

		addr <= (others => '0');
		wd <= '0';

		isa_dout <= (others => '0');
		isa_a <= (others => '0');
		isa_reset <= '0';
		isa_nior <= '1';
		isa_niow <= '1';
		isa_dir <= '0';
		o <= (others => '0');
		led <= (others => '0');

	elsif rising_edge(clk) then

		if (addr_wr='1') then
			addr <= din(3 downto 0);
		end if;
		if (wd_wr='1') then
			wd <= din(0);
		end if;
		if (isa_d_wr='1') then
			isa_dout <= din(7 downto 0);
		end if;
		if (isa_ctrl_wr='1') then
			isa_a <= din(4 downto 0);
			isa_reset <= din(5);
			isa_nior <= not din(6);
			isa_niow <= not din(7);
			isa_dir <= din(8);
		end if;
		if (o_wr='1') then
			o <= din(3 downto 0);
		end if;
		if (lo_wr='1') then
			led <= din(9 downto 0);
		end if;


	end if;
end process;

--
--	xx MHz clock
--

process(clk, reset)

begin
	if (reset='1') then
		clock_cnt <= to_unsigned(0, clock_cnt'length);

	elsif rising_edge(clk) then
		clock_cnt <= clock_cnt+1;
	end if;

end process;

end rtl;
