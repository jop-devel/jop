--
--	mem32.vhd
--
--	external memory interface and multiplyer
--
--		addr, wr are one cycle earlier than data
--		dout one cycle after read (ior)
--
--	resources on ACEX1K30-3
--
--	first mapping (for ldp, stp):
--
--		0	io-address
--		1	data read/write
--		2	st	mem_rd_addr		start read
--		2	ld	mem_rd_data		read data
--		3	st	mem_wr_addr		store write address
--		4	st	mem_wr_data		start write
--		5	ld	mul result
--		5	st	mul operand a
--		6	st	mul operand b and start mul
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
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity mem32 is
generic (width : integer; ioa_width : integer; ram_cnt : integer; rom_cnt : integer);

port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	addr		: in std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: in std_logic;

	bsy			: out std_logic;
	dout		: out std_logic_vector(width-1 downto 0);

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
	fl_rdy	: in std_logic;

-- io interface

	io_din		: in std_logic_vector(width-1 downto 0);
	io_rd		: out std_logic;
	io_wr		: out std_logic;
	io_addr_wr	: out std_logic
);
end mem32;

architecture rtl of mem32 is

component mul is

generic (width : integer);
port (
	clk			: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	wr_a		: in std_logic;
	wr_b		: in std_logic;		-- write to b starts multiplier
	dout		: out std_logic_vector(width-1 downto 0)
);
end component mul;

--
--	signals for mulitiplier
--
	signal mul_dout				: std_logic_vector(width-1 downto 0);
	signal mul_wra, mul_wrb		: std_logic;

--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, wr1
						);
	signal state 		: state_type;

	signal mem_wr_addr		: std_logic_vector(20 downto 0);
	signal mem_dout			: std_logic_vector(width-1 downto 0);
	signal mem_din			: std_logic_vector(width-1 downto 0);
	signal mem_rd			: std_logic;
	signal mem_wr			: std_logic;
	signal mem_bsy			: std_logic;

	signal addr_wr			: std_logic;
	signal nwr_int			: std_logic;
	signal ram_access		: std_logic;
	signal nand_access		: std_logic;

	signal sel_flash		: std_logic;

	signal fl_d_ena			: std_logic;
	signal ram_d_ena		: std_logic;

begin

	cmp_mul : mul generic map (width)
			port map (clk,
				din, mul_wra, mul_wrb,
				mul_dout
		);

	bsy <= mem_bsy;

--
--	read
--
process(clk, reset, rd, addr)
begin
	if (reset='1') then
		dout <= std_logic_vector(to_unsigned(0, width));
		io_rd <= '0';
	elsif rising_edge(clk) then
		dout <= std_logic_vector(to_unsigned(0, width));
		io_rd <= '0';

--
--	TODO: Do I need rd='1' in this MUX?
--
		if (rd='1') then
			if (addr="010") then
				dout <= mem_din;
			elsif (addr="101") then
				dout <= mul_dout;
			else
				dout <= io_din;
				io_rd <= '1';
			end if;
		end if;
	end if;
end process;


--
--	write
--
process(clk, reset, rd, wr)
begin
	if (reset='1') then
		io_addr_wr <= '0';
		mem_rd <= '0';
		mem_wr <= '0';
		addr_wr <= '0';
		mul_wra <= '0';
		mul_wrb <= '0';
		io_wr <= '0';


	elsif rising_edge(clk) then
		io_addr_wr <= '0';
		mem_rd <= '0';
		mem_wr <= '0';
		addr_wr <= '0';
		mul_wra <= '0';
		mul_wrb <= '0';
		io_wr <= '0';

		if (wr='1') then
			if (addr="000") then
				io_addr_wr <= '1';		-- store real io address
			elsif (addr="010") then
				mem_rd <= '1';			-- start read
			elsif (addr="011") then
				addr_wr <= '1';			-- store write address
			elsif (addr="100") then
				mem_wr <= '1';			-- start write
			elsif (addr="101") then
				mul_wra <= '1';
			elsif (addr="110") then
				mul_wrb <= '1';
			else
				io_wr <= '1';
			end if;
		end if;
	end if;
end process;

--
--	wr_addr write
--		one cycle after io write (address is avaliable one cycle before ex stage)
--
process(clk, reset, din, addr_wr)

begin
	if (reset='1') then

		mem_wr_addr <= std_logic_vector(to_unsigned(0, 21));

	elsif rising_edge(clk) then

		if (addr_wr='1') then
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
process(fl_d_ena, mem_dout)

begin
	if (fl_d_ena='1') then
		fl_d <= mem_dout(7 downto 0);
	else
		fl_d <= (others => 'Z');
	end if;
end process;

process(ram_d_ena, mem_dout)

begin
	if (ram_d_ena='1') then
		rama_d <= mem_dout(15 downto 0);
		ramb_d <= mem_dout(31 downto 16);
	else
		rama_d <= (others => 'Z');
		ramb_d <= (others => 'Z');
	end if;
end process;



--
--	state machine for external memory (single byte static ram, flash)
--
process(clk, reset, din, mem_wr_addr, mem_rd, mem_wr)

	variable i : integer range 0 to 31;

begin
	if (reset='1') then
		state <= idl;
		rama_a <= (others => '0');
		ram_d_ena <= '0';
		rama_ncs <= '1';
		rama_noe <= '1';
		rama_nlb <= '1';
		rama_nub <= '1';
		ramb_a <= (others => '0');
		ramb_ncs <= '1';
		ramb_noe <= '1';
		ramb_nlb <= '1';
		ramb_nub <= '1';

		-- fl_a <= (others => 'Z');
		fl_a <= (others => '0');
		fl_d_ena <= '0';
		sel_flash <= '0';					-- select AMD flash as default
		fl_noe <= '1';

		nwr_int <= '1';
		fl_nwe <= '1';
		ram_access <= '1';
		nand_access <= '0';
		mem_din <= std_logic_vector(to_unsigned(0, width));
		mem_dout <= std_logic_vector(to_unsigned(0, width));
		mem_bsy <= '0';

	elsif rising_edge(clk) then

		case state is

			when idl =>
				ram_d_ena <= '0';
				rama_ncs <= '1';
				rama_noe <= '1';
				rama_nlb <= '1';
				rama_nub <= '1';
				ramb_ncs <= '1';
				ramb_noe <= '1';
				ramb_nlb <= '1';
				ramb_nub <= '1';

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
						rama_a <= din(17 downto 0);
						rama_ncs <= '0';
						rama_noe <= '0';
						rama_nlb <= '0';
						rama_nub <= '0';
						ramb_a <= din(17 downto 0);
						ramb_ncs <= '0';
						ramb_noe <= '0';
						ramb_nlb <= '0';
						ramb_nub <= '0';
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
					mem_dout <= din;
					if (mem_wr_addr(20 downto 19) = "00") then	-- ram
						rama_a <= mem_wr_addr(17 downto 0);
						rama_ncs <= '0';
						rama_nlb <= '0';
						rama_nub <= '0';
						ramb_a <= mem_wr_addr(17 downto 0);
						ramb_ncs <= '0';
						ramb_nlb <= '0';
						ramb_nub <= '0';
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
						mem_din <= ramb_d & rama_d;
					else
						if (nand_access='1') then
							mem_din <= std_logic_vector(to_unsigned(0, width-9)) & fl_rdy & fl_d;
						else
							mem_din <= std_logic_vector(to_unsigned(0, width-8)) & fl_d;
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
