--
--	iocore.vhd
--
--	io devices for core print only
--		means all 56 pins are configurable IOs
--
--
--	io address mapping:
--
--		0	in, out port not used
--		1	status
--		2	uart data (rd/wr)
--
--		7	watch dog bit
--		8	l_io dir
--		9	r_io dir
--		10	system clock counter
--		11	tb_io dir
--		12	l_io
--		13	r_io
--		14	tb_io
--
--	status word:
--		0	uart transmit data register empty
--		1	uart read data register full
--
--
--	todo:
--		ff against meta stability on all inputs
--		reorganize io addr
--
--
--	2002-12-01	extracted from memiocore.vhd
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

-- interrupt

	irq			: out std_logic;
	irq_ena		: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts			: in std_logic;
	rts			: out std_logic;

-- watch dog

	wd			: out std_logic;

--	I/O pins of board

	io_b	: inout std_logic_vector(10 downto 1);
	io_l	: inout std_logic_vector(20 downto 1);
	io_r	: inout std_logic_vector(20 downto 1);
	io_t	: inout std_logic_vector(6 downto 1)
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
-- signal for out port, wd
--
	signal l_dir_wr, l_wr		: std_logic;
	signal r_dir_wr, r_wr		: std_logic;
	signal tb_dir_wr, tb_wr		: std_logic;
	signal wd_wr				: std_logic;

	signal l_dir, l_io		: std_logic_vector(19 downto 0);
	signal r_dir, r_io		: std_logic_vector(19 downto 0);
	signal tb_dir, tb_io		: std_logic_vector(15 downto 0);

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

	ua_wr <= '0';
	wd_wr <= '0';

	l_dir_wr <= '0';
	l_wr <= '0';
	r_dir_wr <= '0';
	r_wr <= '0';
	tb_dir_wr <= '0';
	tb_wr <= '0';

	if (wr='1') then
		case addr(3 downto 0) is
			when "0010" =>
				ua_wr <= '1';
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
process(clk, reset, din, addr_wr, wd_wr, l_wr, l_dir_wr, r_wr, r_dir_wr, tb_wr, tb_dir_wr)

begin
	if (reset='1') then

		addr <= (others => '0');
		wd <= '0';

		l_io <= (others => '0');
		r_io <= (others => '0');
		tb_io <= (others => '0');
		l_dir <= (others => '0');
		r_dir <= (others => '0');
		tb_dir <= (others => '0');

	elsif rising_edge(clk) then

		if (addr_wr='1') then addr <= din(3 downto 0); end if;

		if (wd_wr='1') then wd <= din(0); end if;

		if l_wr then l_io <= din(19 downto 0); end if;
		if l_dir_wr then l_dir <= din(19 downto 0); end if;
		if r_wr then r_io <= din(19 downto 0); end if;
		if r_dir_wr then r_dir <= din(19 downto 0); end if;
		if tb_wr then tb_io <= din(15 downto 0); end if;
		if tb_dir_wr then tb_dir <= din(15 downto 0); end if;

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
