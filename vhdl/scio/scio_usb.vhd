--
--	scio_usb.vhd
--
--	io devices for dspio board (minimal version)
--
--
--	io address mapping:
--
--	IO Base is 0xffffff80 for 'fast' constants (bipush)
--
--		0x00 0-3		system clock counter, us counter, timer int, wd bit
--		0x10 0-1		uart (download)
--		0x20 0-1		USB connection (download)
--		0x30 0-f		MAC
--
--	status word in uart and usb:
--		0	uart transmit data register empty
--		1	uart read data register full
--
--
--	todo:
--
--
--	2003-07-09	created
--	2005-08-27	ignore ncts on uart
--	2005-11-30	changed to SimpCon
--	2005-12-20	dspio board
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.jop_config.all;

entity scio is
generic (addr_bits : integer);

port (
	clk		: in std_logic;
	reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

-- interrupt

	irq			: out std_logic;
	irq_ena		: out std_logic;

-- exception

	exc_req		: in exception_type;
	exc_int		: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	ncts		: in std_logic;
	nrts		: out std_logic;

-- watch dog

	wd			: out std_logic;

-- core i/o pins
	l			: inout std_logic_vector(20 downto 1);
	r			: inout std_logic_vector(20 downto 1);
	t			: inout std_logic_vector(6 downto 1);
	b			: inout std_logic_vector(10 downto 1)
);
end scio;


architecture rtl of scio is

	constant SLAVE_CNT : integer := 4;
	-- SLAVE_CNT <= 2**DECODE_BITS
	constant DECODE_BITS : integer := 2;
	-- number of bits that can be used inside the slave
	constant SLAVE_ADDR_BITS : integer := 4;

	type slave_bit is array(0 to SLAVE_CNT-1) of std_logic;
	signal sc_rd, sc_wr		: slave_bit;

	type slave_dout is array(0 to SLAVE_CNT-1) of std_logic_vector(31 downto 0);
	signal sc_dout			: slave_dout;

	type slave_rdy_cnt is array(0 to SLAVE_CNT-1) of unsigned(1 downto 0);
	signal sc_rdy_cnt		: slave_rdy_cnt;

	signal sel, sel_reg		: integer range 0 to 2**DECODE_BITS-1;


begin

--	unused and input pins tri state
--
	l(20 downto 6) <= (others => 'Z');
	-- tri state AC97 signals
	l(5 downto 1) <= (others => 'Z');
	r(20 downto 14) <= (others => 'Z');
	t <= (others => 'Z');
	b <= (others => 'Z');

	assert SLAVE_CNT <= 2**DECODE_BITS report "Wrong constant in scio";

	sel <= to_integer(unsigned(address(SLAVE_ADDR_BITS+DECODE_BITS-1 downto SLAVE_ADDR_BITS)));

	-- What happens when sel_reg > SLAVE_CNT-1??
	rd_data <= sc_dout(sel_reg);
	rdy_cnt <= sc_rdy_cnt(sel_reg);

	--
	-- Connect SLAVE_CNT slaves
	--
	gsl: for i in 0 to SLAVE_CNT-1 generate

		sc_rd(i) <= rd when i=sel else '0';
		sc_wr(i) <= wr when i=sel else '0';

	end generate;

	--
	--	Register read mux selector
	--
	process(clk, reset)
	begin
		if (reset='1') then
			sel_reg <= 0;
		elsif rising_edge(clk) then
			if rd='1' then
				sel_reg <= sel;
			end if;
		end if;
	end process;
			
	cmp_cnt: entity work.sc_cnt generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq
		)
		port map(
			clk => clk,
			reset => reset,

			address => address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => wr_data,
			rd => sc_rd(0),
			wr => sc_wr(0),
			rd_data => sc_dout(0),
			rdy_cnt => sc_rdy_cnt(0),

			irq => irq,
			irq_ena => irq_ena,

			exc_req => exc_req,
			exc_int => exc_int,
			
			wd => wd
		);

	cmp_ua: entity work.sc_uart generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
			baud_rate => 115200,
			txf_depth => 2,
			txf_thres => 1,
			rxf_depth => 2,
			rxf_thres => 1
		)
		port map(
			clk => clk,
			reset => reset,

			address => address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => wr_data,
			rd => sc_rd(1),
			wr => sc_wr(1),
			rd_data => sc_dout(1),
			rdy_cnt => sc_rdy_cnt(1),

			txd	 => txd,
			rxd	 => rxd,
			ncts => '0',
			nrts => nrts
	);

	cmp_usb: entity work.sc_usb generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq
		)
		port map(
			clk => clk,
			reset => reset,

			address => address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => wr_data,
			rd => sc_rd(2),
			wr => sc_wr(2),
			rd_data => sc_dout(2),
			rdy_cnt => sc_rdy_cnt(2),

			data => r(8 downto 1),
			nrxf => r(9),
			ntxe => r(10),
			nrd => r(11),
			ft_wr => r(12),
			nsi => r(13)
	);

	cmp_mac: entity work.sc_mac16 generic map (
			addr_bits => SLAVE_ADDR_BITS
		)
		port map(
			clk => clk,
			reset => reset,

			address => address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => wr_data,
			rd => sc_rd(3),
			wr => sc_wr(3),
			rd_data => sc_dout(3),
			rdy_cnt => sc_rdy_cnt(3)

	);

end rtl;
