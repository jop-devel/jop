--
--	jop.vhd
--
--	top level of JOP2
--
--	resources on ACEX1K30-3
--		832 LCs, 38.8 MHz		core, io ports, uart and memory interface
--		947 LCs, 45.5 MHz		plus bytecode fetch/decode
--		1100 LCs, 46.5 MHz		plus clk and ms counter
--		1095 LCs, 46.0 MHz		with jopd in memio (accu on outp for debug)
--		1134 LCs, 47.8 MHz		with logic for jpc branches
--		1293 LCs, 45.9 MHz		plus ecp
--		1327 LCs, 46.5 MHz		with outp again
--
--		1463 LCs, 44.1 MHz		jop3
--		1508 LCs, 40.0 MHz
--		1516 LCs, 38.3 MHz		plus ldjpc, stjpc
--		1625 LCs, 28.2 MHz		plus jtbl (unregisterd, in logic), no ecp,.. (bb project)
--		1598 LCs, 30.9 MHz		cp removed
--		1574 LCs, 28.3 MHz		2001-12-05 (???)
--		1570 LCs, 29.1 MHz		instruction set change
--		1629 LCs, 31.1 MHz		imul,... no ecp, ext. opt. area
--
--	2001-05-27	new (split core and memio)
--	2001-06-02	byte code fetch logic
--	2001-07-14	jop3
--	2001-12-04	cp removed
--	2001-12-08	instruction set change (16->8 bit)
--	2001-12-22	nrom_cs, nrd '1'->'Z' from memio moved
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;

entity jop is

generic (
	clk_freq	: integer := 24000000;	-- clock frequency for uart
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

	max_oe		: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts			: in std_logic;
	rts			: out std_logic;

-- lpt interface

	lpt_d		: inout std_logic_vector(7 downto 0);
	lpt_s		: out std_logic_vector(7 downto 3);
	lpt_c		: in std_logic_vector(3 downto 0);

--
--	pins for bb project:
--

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

-- keyboard

	key_in		: in std_logic_vector(3 downto 0);
	key_out		: out std_logic_vector(3 downto 0);

-- triac

	p1_u		: in std_logic;
	p2_u		: in std_logic;
	p3_u		: in std_logic;
	p1_i		: in std_logic;
	p2_i		: in std_logic;
	p3_i		: in std_logic;

	p1_auf_ab	: out std_logic;
	p2_auf		: out std_logic;
	p3_auf		: out std_logic;
	p2_ab		: out std_logic;
	p3_ab		: out std_logic

-- io ports

--	inp			: in std_logic_vector(11 downto 0);	not used for BB project
--	outp		: out std_logic_vector(11 downto 0)

);
end jop;

architecture rtl of jop is

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

	max_oe		: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts			: in std_logic;
	rts			: out std_logic;

-- lpt interface

	lpt_d		: inout std_logic_vector(7 downto 0);
	lpt_s		: out std_logic_vector(7 downto 3);
	lpt_c		: in std_logic_vector(3 downto 0);

-- display

	disp		: out std_logic_vector(5 downto 0);

-- keyboard

	key_in		: in std_logic_vector(3 downto 0);
	key_out		: out std_logic_vector(3 downto 0);

-- triacs

	tr_p		: out std_logic_vector(2 downto 0);
	tr_dir		: out std_logic;
	sense_u		: in std_logic_vector(2 downto 0);
	sense_i		: in std_logic_vector(3 downto 0);

-- io ports

	inp			: in std_logic_vector(11 downto 0);
	outp		: out std_logic_vector(11 downto 0)

);
end component;


--
--	Signals
--

	signal memio_din		: std_logic_vector(width-1 downto 0);
	signal memio_addr		: std_logic_vector(ioa_width-1 downto 0);
	signal memio_rd			: std_logic;
	signal memio_wr			: std_logic;

	signal memio_nrom_cs	: std_logic;
	signal memio_nrd		: std_logic;

	signal memio_dout		: std_logic_vector(width-1 downto 0);

	signal inp				: std_logic_vector(11 downto 0);
	signal outp				: std_logic_vector(11 downto 0);
	signal disp				: std_logic_vector(5 downto 0);
	signal key_oc			: std_logic_vector(3 downto 0);
	signal key_inv			: std_logic_vector(3 downto 0);

	signal tr_p				: std_logic_vector(2 downto 0);
	signal tr_dir			: std_logic;
	signal sense_u			: std_logic_vector(2 downto 0);
	signal sense_i			: std_logic_vector(3 downto 0);

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0);

begin

	int_res <= not res_cnt(0) or not res_cnt(1) or not res_cnt(2);
--	int_res <= reset or not res_cnt(0) or not res_cnt(1) or not res_cnt(2);

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
			a, d, nram_cs, memio_nrom_cs, memio_nrd, nwr, max_oe,
			txd, rxd, cts, rts, lpt_d, lpt_s, lpt_c,
			disp, key_inv, key_oc,
			tr_p, tr_dir, sense_u, sense_i,
			inp, outp
		);

--
--	use tri state for rom_cs and oe (pull up)
--
	nrd <= '0' when memio_nrd='0' else 'Z';
	nrom_cs <= '0' when memio_nrom_cs='0' else 'Z';
--
--	display
--

	disp_d(7 downto 4) <= disp(3 downto 0);
	disp_rs <= disp(4);
	disp_e <= disp(5);
	disp_nwr <= '0';

--
--	keyboard
--

	key_inv <= not key_in;				-- invert keyboard (log 1 activ)
	key_out(0) <= 'Z' when key_oc(0)='0' else '0';
	key_out(1) <= 'Z' when key_oc(1)='0' else '0';
	key_out(2) <= 'Z' when key_oc(2)='0' else '0';
	key_out(3) <= 'Z' when key_oc(3)='0' else '0';
	
--
--	triac
--

	sense_u(0) <= p1_u;
	sense_u(1) <= p2_u;
	sense_u(2) <= p3_u;
	sense_i(0) <= p1_i;
	sense_i(1) <= p2_i;
	sense_i(2) <= p3_i;

--
--	low activ OC
--
	p1_auf_ab <= '0' when tr_p(0)='1' else 'Z';
	p2_auf <= '0' when tr_dir='0' and tr_p(0)='1' else 'Z';
	p3_auf <= '0' when tr_dir='0' and tr_p(0)='1' else 'Z';
	p2_ab <= '0' when tr_dir='1' and tr_p(0)='1' else 'Z';
	p3_ab <= '0' when tr_dir='1' and tr_p(0)='1' else 'Z';
--
--	keine Phasenanschnittsteuerung
--
--	p2_auf <= '0' when tr_dir='0' and tr_p(1)='1' else 'Z';
--	p3_auf <= '0' when tr_dir='0' and tr_p(2)='1' else 'Z';
--	p2_ab <= '0' when tr_dir='1' and tr_p(1)='1' else 'Z';
--	p3_ab <= '0' when tr_dir='1' and tr_p(2)='1' else 'Z';

	wd <= outp(0);
	led <= outp(4 downto 1);

	inp(0) <= ab_t;
	inp(1) <= auf_t;
	inp(2) <= not_stop;
	inp(3) <= tuer;

	inp(4) <= sens_i;
	inp(5) <= sens_o;
	inp(6) <= sens_u;
	inp(11 downto 7) <= (others => '0');		-- not used

end rtl;
