--
--	mem32.vhd
--
--	external memory interface
--
--
--	memory mapping
--	
--		000000-x7ffff	external ram (w mirror)	max. 512 kW (4*4 MBit)
--		080000-xfffff	external rom (w mirror)	max. 512 kB (4 MBit)
--		100000-xfffff	external NAND flash
--
--	ram: 32 bit word
--	rom: 8 bit word (for flash programming)
--
--	todo:
--		check timing for async mem (zero ws?)
--
--	2002-12-02	wait instruction for memory
--	2003-02-24	version for Cycore
--	2003-03-07	leave last cs of flash/NAND.
--	2003-07-09	extra tri state signal for data out (Quartus bug)
--				leave ram addr driving (no tri state)
--	2004-01-10	release mem_bsy two cycle earlier
--	2004-09-11	move data mux and mul to extension
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity mem32 is
-- generic (jpc_width : integer; ram_cnt : integer; rom_cnt : integer);
generic (jpc_width : integer := 10; ram_cnt : integer := 3; rom_cnt : integer := 15);

port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(31 downto 0);

	mem_rd		: in std_logic;
	mem_wr		: in std_logic;
	mem_addr_wr	: in std_logic;
	dout		: out std_logic_vector(31 downto 0);

	bsy			: out std_logic;

-- jbc connections

	jbc_addr	: in std_logic_vector(jpc_width-1 downto 0);
	jbc_data	: out std_logic_vector(7 downto 0);
	jpc_wr		: in std_logic;
	bc_wr		: in std_logic;

--
--	two ram banks
--
	rama_a		: out std_logic_vector(17 downto 0);
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_noe	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	rama_nwe	: out std_logic;
	ramb_a		: out std_logic_vector(17 downto 0);
	ramb_d		: inout std_logic_vector(15 downto 0);
	ramb_ncs	: out std_logic;
	ramb_noe	: out std_logic;
	ramb_nlb	: out std_logic;
	ramb_nub	: out std_logic;
	ramb_nwe	: out std_logic;

--
--	config/program flash and big nand flash
--
	fl_a	: out std_logic_vector(18 downto 0);
	fl_d	: inout std_logic_vector(7 downto 0);
	fl_ncs	: out std_logic;
	fl_ncsb	: out std_logic;
	fl_noe	: out std_logic;
	fl_nwe	: out std_logic;
	fl_rdy	: in std_logic

);
end mem32;

architecture rtl of mem32 is

--
--	jbc component (use technology specific vhdl-file (ajbc/xjbc))
--
--	dual port ram
--	wraddr and wrena registered and delayed
--	rdaddr is registered
--	indata registered
--	outdata is unregistered
--
component jbc is
generic (width : integer; addr_width : integer);
port (
	data		: in std_logic_vector(31 downto 0);
	rdaddress	: in std_logic_vector(jpc_width-1 downto 0);
	wr_addr		: in std_logic;									-- load start address (=jpc)
	wren		: in std_logic;
	clock		: in std_logic;

	q			: out std_logic_vector(7 downto 0)
);
end component;


--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, wr1
						);
	signal state 		: state_type;

	signal mem_wr_addr		: std_logic_vector(20 downto 0);
	signal mem_wr_val		: std_logic_vector(31 downto 0);
	signal mem_rd_val		: std_logic_vector(31 downto 0);
	signal mem_bsy			: std_logic;

	signal nwr_int			: std_logic;
	signal ram_access		: std_logic;
	signal nand_access		: std_logic;

	signal sel_flash		: std_logic;

	signal fl_d_ena			: std_logic;
	signal ram_d_ena		: std_logic;

	signal ram_cs, ram_oe	: std_logic;
	signal ram_addr			: std_logic_vector(17 downto 0);

begin

	bsy <= mem_bsy;
	dout <= mem_rd_val;


	cmp_jbc: jbc generic map (8, jpc_width) port map(din, jbc_addr, jpc_wr, bc_wr, clk, jbc_data);



--
--	wr_addr write
--		one cycle after io write (address is avaliable one cycle before ex stage)
--
process(clk, reset, din, mem_addr_wr)

begin
	if (reset='1') then

		mem_wr_addr <= std_logic_vector(to_unsigned(0, 21));

	elsif rising_edge(clk) then

		if (mem_addr_wr='1') then
			mem_wr_addr <= din(20 downto 0);	-- store write address
		end if;

	end if;
end process;


--
--	'delay' nwr 1/2 cycle -> change on falling edge
--
process(clk, reset, nwr_int)

begin
	if (reset='1') then
		rama_nwe <= '1';
		ramb_nwe <= '1';
	elsif falling_edge(clk) then
		rama_nwe <= nwr_int;
		ramb_nwe <= nwr_int;
	end if;

end process;

--
--	leave last ncs. Only toggle between two flashs.
--
	fl_ncsb <= not sel_flash;
	fl_ncs <= sel_flash;

--
--	tristate output
--
process(fl_d_ena, mem_wr_val)

begin
	if (fl_d_ena='1') then
		fl_d <= mem_wr_val(7 downto 0);
	else
		fl_d <= (others => 'Z');
	end if;
end process;

process(ram_d_ena, mem_wr_val)

begin
	if (ram_d_ena='1') then
		rama_d <= mem_wr_val(15 downto 0);
		ramb_d <= mem_wr_val(31 downto 16);
	else
		rama_d <= (others => 'Z');
		ramb_d <= (others => 'Z');
	end if;
end process;


	rama_nlb <= '0';
	rama_nub <= '0';
	ramb_nlb <= '0';
	ramb_nub <= '0';
	rama_ncs <= not ram_cs;
	rama_noe <= not ram_oe;
	ramb_ncs <= not ram_cs;
	ramb_noe <= not ram_oe;

--
--	To put this in an output register
--	we have to make an assignment (FAST_OUTPUT_REGISTER)
--
	rama_a <= ram_addr;
	ramb_a <= ram_addr;

--
--	state machine for external memory (single byte static ram, flash)
--
process(clk, reset, din, mem_wr_addr, mem_rd, mem_wr)

	variable i : integer range 0 to 31;

begin
	if (reset='1') then
		state <= idl;
		ram_addr <= (others => '0');
		ram_d_ena <= '0';
		ram_cs <= '0';
		ram_oe <= '0';

		-- fl_a <= (others => 'Z');
		fl_a <= (others => '0');
		fl_d_ena <= '0';
		sel_flash <= '0';					-- select AMD flash as default
		fl_noe <= '1';

		nwr_int <= '1';
		fl_nwe <= '1';
		ram_access <= '1';
		nand_access <= '0';
		mem_rd_val <= std_logic_vector(to_unsigned(0, 32));
		mem_wr_val <= std_logic_vector(to_unsigned(0, 32));
		mem_bsy <= '0';

	elsif rising_edge(clk) then

		case state is

			when idl =>
				ram_d_ena <= '0';
				ram_cs <= '0';
				ram_oe <= '0';

-- leave the addr. pins.
--				fl_a <= (others => 'Z');
				fl_d_ena <= '0';
--
--	leave last ncs down, NAND needs somtimes continous ncs.
--
--				sel_flash <= '1';

				fl_noe <= '1';

				nwr_int <= '1';
				fl_nwe <= '1';
				ram_access <= '1';
				nand_access <= '0';
				mem_bsy <= '0';

				if (mem_rd='1') then
					if (din(20 downto 19) = "00") then	-- ram
						ram_addr <= din(17 downto 0);
						ram_cs <= '1';
						ram_oe <= '1';
						ram_access <= '1';
						i := ram_cnt;
					elsif (din(20 downto 19) = "01") then	-- flash
						fl_a <= din(18 downto 0);
						sel_flash <= '0';
						fl_noe <= '0';
						ram_access <= '0';
						i := rom_cnt;
--
--	TODO see request on nCS for NAND in data sheet.
--	Must be coninous for some operations.
--
					else								-- NAND
						fl_a <= din(18 downto 0);
						sel_flash <= '1';
						fl_noe <= '0';
						ram_access <= '0';
--
--	could be easier when using "11" for rdy signal on read
--
						nand_access <= '1';
						i := rom_cnt;
					end if;
					mem_bsy <= '1';
					state <= rd1;
				elsif (mem_wr='1') then
					mem_wr_val <= din;
					if (mem_wr_addr(20 downto 19) = "00") then	-- ram
						ram_addr <= mem_wr_addr(17 downto 0);
						ram_cs <= '1';
						ram_access <= '1';
						i := ram_cnt+1;			-- one more for single cycle read
					elsif (mem_wr_addr(20 downto 19) = "01") then	-- flash
						fl_a <= mem_wr_addr(18 downto 0);
						sel_flash <= '0';
						ram_access <= '0';
						i := rom_cnt;
					else								-- NAND
						fl_a <= mem_wr_addr(18 downto 0);
						sel_flash <= '1';
						ram_access <= '0';
						i := rom_cnt;
					end if;
					mem_bsy <= '1';
					nwr_int <= '0';
					state <= wr1;
				end if;

--
--	memory read
--
			when rd1 =>
				i := i-1;
				if (i=2) then		-- ***** only on ram ?????
					mem_bsy <= '0';					-- release mem_bsy two cycle earlier
				end if;
				if (i=0) then
					state <= idl;
					mem_bsy <= '0';
					--
					--	this mux costs about 6ns, tsu is 5.2ns (could be negativ!)
					--
					if (ram_access='1') then
						mem_rd_val <= ramb_d & rama_d;
					else
						if (nand_access='1') then
							mem_rd_val <= std_logic_vector(to_unsigned(0, 32-9)) & fl_rdy & fl_d;
						else
							mem_rd_val <= std_logic_vector(to_unsigned(0, 32-8)) & fl_d;
						end if;
					end if;
				end if;

--
--	memory write
--
			when wr1 =>
				i := i-1;
				if (ram_access='1') then
					ram_d_ena <= '1';
				else
					fl_nwe <= '0';
					fl_d_ena <= '1';
				end if;
				if (i=1) then
					fl_nwe <= '1';
					nwr_int <= '1';
-- only ram ???					mem_bsy <= '0';					-- release mem_bsy one cycle earlier
				end if;
				if (i=0) then
					fl_nwe <= '1';
					state <= idl;
					mem_bsy <= '0';
				end if;

		end case;
					
	end if;
end process;


end rtl;
