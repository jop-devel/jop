--
--	fetch.vhd
--
--	jbc and instr fetch
--
--	resources on ACEX1K30-3
--
--		132 LCs, max ca. 50 MHz
--
--	todo:
--		5 stage pipeline (jtbl/rom)
--		relativ address for jp, br
--		load pc instead of addres mux befor rom!
--
--	2001-07-04	first version
--	2001-07-18	component pc_inc in own file for Xilinx
--	2001-10-24	added 2 delays for br address (address is now in br opcode!)
--	2001-10-28	ldjpc, stjpc
--	2001-10-31	stbc (write content of jbc)
--	2001-11-13	added jtbl (jtbl and rom in one pipline stage!)
--	2001-11-14	change jbc to 1024 bytes
--	2001-11-16	split to fetch and bcfetch
--	2001-12-06	ir from decode to rom, (one brdly removed)
--				mux befor rom removed, unregistered jfetch conrols imput to
--				pc, jpaddr unregistered!
--	2001-12-07	branch relativ
--	2001-12-08	use table for branch offsets
--	2001-12-08	instruction set changed to 8 bit, pc to 10 bits
--	2002-12-02	wait instruction for memory
--	2003-08-15	move bcfetch to core
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity fetch is

generic (
	jpc_width	: integer := 10;	-- address bits of java byte code pc
	pc_width	: integer := 10;	-- address bits of internal instruction rom
	i_width		: integer := 8		-- instruction width
);
port (
	clk, reset	: in std_logic;

	nxt, opd	: out std_logic;	-- jfetch and jopdfetch from table

	br			: in std_logic;
	pcwait		: in std_logic;
	jpaddr		: in std_logic_vector(pc_width-1 downto 0);

	dout		: out std_logic_vector(i_width-1 downto 0)		-- internal instruction (rom)
);
end fetch;

architecture rtl of fetch is

--
--	rom component (use technology specific vhdl-file (arom/xrom))
--
--	rom unregistered address (or registered on negativ clock edge), registerd out (=ir)
--
component rom is
generic (width : integer; addr_width : integer);
port (
	clk			: in std_logic;

	address		: in std_logic_vector(pc_width-1 downto 0);

	q			: out std_logic_vector(i_width-1 downto 0)
);
end component;
--
--	offsets for relativ branches.
--
component offtbl is
port (
	idx		: in std_logic_vector(4 downto 0);
	q		: out std_logic_vector(pc_width-1 downto 0)
);
end component;

--
--	table to generate jfetch and jopdfetch
--		generated from Jopa.java
--
component bcfetbl is
port (
	addr		: in std_logic_vector(pc_width-1 downto 0);
	nxt, opd	: out std_logic
);
end component;

	signal pc			: std_logic_vector(pc_width-1 downto 0);
	signal pcin			: std_logic_vector(pc_width-1 downto 0);
	signal brdly		: std_logic_vector(pc_width-1 downto 0);

	signal off			: std_logic_vector(pc_width-1 downto 0);

	signal jfetch		: std_logic;		-- fetch next byte code as opcode
	signal jopdfetch	: std_logic;		-- fetch next byte code as operand

	signal ir			: std_logic_vector(i_width-1 downto 0);		-- instruction register

begin

--
--	jop instrcution fetch and branch
--

	cmp_rom: rom generic map (i_width, pc_width) port map(clk, pc, ir);
	cmp_bft: bcfetbl port map(pc, jfetch, jopdfetch);

	cmp_off: offtbl port map(ir(4 downto 0), off);

	dout <= ir;
	nxt <= jfetch;
	opd <= jopdfetch;


process(clk, reset, pc, off)

begin
	if (reset='1') then
		pc <= std_logic_vector(to_unsigned(0, pc_width));
		brdly <= std_logic_vector(to_unsigned(0, pc_width));

	elsif rising_edge(clk) then

		brdly <= std_logic_vector(unsigned(pc) + unsigned(off));
		pc <= pcin;

	end if;

end process;


process(jfetch, br, pcwait, jpaddr, brdly, pc)

begin

	if (jfetch='1') then
		pcin <= jpaddr;
	elsif (br='1') then
		pcin <= brdly;
	elsif (pcwait='1') then
		pcin <= pc;
	else
		pcin <= std_logic_vector(unsigned(pc) + 1);
	end if;

end process;

end rtl;

