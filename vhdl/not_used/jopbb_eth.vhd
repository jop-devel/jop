--
--	jopbb_eth.vhd
--
--	top level of JOP3 with ethernet card on board for Balfour Beatty
--	connected to exp and old key_*
--
--	resources on ACEX1K50-3
--
--		1697 LCs, 21.9 MHz		jopbb
--		1862 LCs, 23.8 MHz		with isa bus,...
--		2374 LCs, 21.9 MHz		mul, barrel shifter, jpc incr. on bc write
--
--	2001-10-27	adapted from jop.vhd
--	2001-11-27	again adapted for pinout
--	2001-02-01	some changes in io port definitions
--	2002-02-25	temp measure with sima delta ADC (without comperator!)
--	2002-03-16	ethernet card (isa bus)
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;

entity jopbb is

generic (
	clk_freq	: integer := 64*115200;	-- 7.3728 MHz clock frequency for uart
	width		: integer := 32;	-- one data word
	ioa_width	: integer := 3		-- address bits of internal io
);

port (
	clk, reset	: in std_logic;

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nrd			: out std_logic;
	nwr			: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	ncts		: in std_logic;
	nrts		: out std_logic;

--
--	pins for bb project:
--

	s1_txd		: out std_logic;
	s1_rxd		: in std_logic;
	s1_ncts		: in std_logic;
	s1_nrts		: out std_logic;

--	extern connector

	ab_t		: in std_logic;
	auf_t		: in std_logic;
	not_stop	: in std_logic;
	tuer		: in std_logic;
	led			: out std_logic_vector(4 downto 1);

--	WD
	wd			: out std_logic;

--	motor sensor

	sens_i		: in std_logic;
	sens_o		: in std_logic;
	sens_u		: in std_logic;

-- display

	disp_rs		: out std_logic;
	disp_nwr	: out std_logic;
	disp_e		: out std_logic;	-- was ndrd
	disp_d		: out std_logic_vector(7 downto 4);

-- triac

	p1_u		: in std_logic;
	p2_u		: in std_logic;
	p3_u		: in std_logic;
	p1_i		: in std_logic;
	p2_i		: in std_logic;
	p3_i		: in std_logic;
	null_i		: in std_logic;

	p1_auf_ab	: out std_logic;
	p2_auf		: out std_logic;
	p3_auf		: out std_logic;
	p2_ab		: out std_logic;
	p3_ab		: out std_logic;

-- relais

	u15kv_res	: out std_logic;
	u15kv_set	: out std_logic;
	loben_res	: out std_logic;
	loben_set	: out std_logic;

	schuetz		: out std_logic;	-- not used (free pin)

	exp9		: in std_logic;		-- temp sd in
	exp10		: out std_logic;	-- temp sd out
	exp11		: out std_logic;	-- GND for sd

-- ethernet card

	isa_d		: inout std_logic_vector(7 downto 0);
	isa_a		: out std_logic_vector(4 downto 0);
	isa_reset	: out std_logic;
	isa_nior	: out std_logic;
	isa_niow	: out std_logic;
	isa_nc		: out std_logic
);
end jopbb;

architecture rtl of jopbb is

--
--	components:
--

component core is
port (
	clk, reset	: in std_logic;

-- memio connection

	din			: in std_logic_vector(width-1 downto 0);
	addr		: out std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: out std_logic;

	dout		: out std_logic_vector(width-1 downto 0)
);
end component;

component memio is
generic (clk_freq : integer; width : integer; ioa_width : integer);
port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	addr		: in std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0);

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nrd			: out std_logic;
	nwr			: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts			: in std_logic;
	rts			: out std_logic;

	s1_txd		: out std_logic;
	s1_rxd		: in std_logic;
	s1_cts		: in std_logic;
	s1_rts		: out std_logic;

-- display

	disp		: out std_logic_vector(5 downto 0);

-- triacs

	tr_on		: out std_logic;
	tr_dir		: out std_logic;
	sense_u		: in std_logic_vector(2 downto 0);
	sense_i		: in std_logic_vector(3 downto 0);

-- io ports
	wd			: out std_logic;
	led			: out std_logic_vector(4 downto 1);
	tast		: in std_logic_vector(3 downto 0);
	relais		: out std_logic_vector(3 downto 0);
	sensor		: in std_logic_vector(3 downto 0);

-- analog ports
	sdi			: in std_logic;
	sdo			: out std_logic;

-- ethernet card
	isa_d		: inout std_logic_vector(7 downto 0);
	isa_a		: out std_logic_vector(4 downto 0);
	isa_reset	: out std_logic;
	isa_nior	: out std_logic;
	isa_niow	: out std_logic;
	isa_nc		: out std_logic
);
end component;

--
--	Signals
--

	signal memio_din		: std_logic_vector(width-1 downto 0);
	signal memio_addr		: std_logic_vector(ioa_width-1 downto 0);
	signal memio_rd			: std_logic;
	signal memio_wr			: std_logic;

	signal memio_dout		: std_logic_vector(width-1 downto 0);

	signal aint				: std_logic_vector(18 downto 0);

	signal rts, cts			: std_logic;
	signal s1_nrxd, s1_rts, s1_cts	: std_logic;

	signal disp				: std_logic_vector(5 downto 0);

	signal tr_on			: std_logic;
	signal tr_dir			: std_logic;
	signal sense_u			: std_logic_vector(2 downto 0);
	signal sense_i			: std_logic_vector(3 downto 0);

	signal tast				: std_logic_vector(3 downto 0);
	signal relais			: std_logic_vector(3 downto 0);
	signal sensor			: std_logic_vector(3 downto 0);

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0);

	signal sr				: std_logic_vector(31 downto 0);

begin

	int_res <= reset or not res_cnt(0) or not res_cnt(1) or not res_cnt(2);

process(clk)
begin
	if rising_edge(clk) then
		if (res_cnt/="111") then
			res_cnt <= res_cnt+1;
		end if;
	end if;
end process;

	cmp_core: core 
		port map (clk, int_res,
			memio_dout, memio_addr,
			memio_rd, memio_wr, memio_din
		);

	cmp_memio: memio generic map (clk_freq, width, ioa_width)
		port map (clk, int_res, memio_din, memio_addr, memio_rd, memio_wr, memio_dout,
			aint, d, nram_cs, nrom_cs, nrd, nwr,
			txd, rxd, cts, rts,
			s1_txd, s1_nrxd, s1_cts, s1_rts,
			disp, 
			tr_on, tr_dir, sense_u, sense_i,
			wd, led, tast, relais, sensor,
			exp9, exp10,
			isa_d, isa_a, isa_reset, isa_nior, isa_niow, isa_nc
		);

	a(18 downto 17) <= "11";			-- CS1 for 128kB ram
	a(16 downto 0) <= aint(16 downto 0);

	nrts <= not rts;
	cts <= not ncts;
	s1_nrxd <= not s1_rxd;				-- MAX1480 inverts rxd
	s1_nrts <= '0' when s1_rts='1' else 'Z';
	s1_cts <= not s1_ncts;

	exp11 <= '0';						-- should be GND for SD ADC
--
--	display
--

	disp_d(7 downto 4) <= disp(3 downto 0);
	disp_rs <= disp(4);
	disp_e <= disp(5);
	disp_nwr <= '0';

--
--	triac
--

	sense_u(0) <= not p1_u;
	sense_u(1) <= not p2_u;
	sense_u(2) <= not p3_u;
	sense_i(0) <= not p1_i;
	sense_i(1) <= not p2_i;
	sense_i(2) <= not p3_i;
	sense_i(3) <= not null_i;

--
--	low activ OC
--
	p1_auf_ab <= '0' when tr_on='1' else 'Z';
	p2_auf <= '0' when tr_dir='0' and tr_on='1' else 'Z';
	p3_auf <= '0' when tr_dir='0' and tr_on='1' else 'Z';
	p2_ab <= '0' when tr_dir='1' and tr_on='1' else 'Z';
	p3_ab <= '0' when tr_dir='1' and tr_on='1' else 'Z';

	tast(0) <= not ab_t;
	tast(1) <= not auf_t;
	tast(2) <= not not_stop;
	tast(3) <= not tuer;

	sensor(0) <= sens_i;
	sensor(1) <= sens_o;
	sensor(2) <= sens_u;

-- OC
	u15kv_res <= '0' when relais(0)='1' else 'Z';
	u15kv_set <= '0' when relais(1)='1' else 'Z';
	loben_res <= '0' when relais(2)='1' else 'Z';
	loben_set <= '0' when relais(3)='1' else 'Z';

	schuetz <= 'Z';				-- not used

end rtl;
