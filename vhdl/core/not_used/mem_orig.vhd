--
--	mem.vhd
--
--	external memory interface and multiplyer
--	from mem to memeth in the old way!!!
--
--	TODO TODO split mem and io for different versions!!!!!!!!!
--
--	external memory and IO for JOP3 (jopcore version)
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
--		x00000-x7ffff	external ram (w mirror)	max. 512 kW (4*4 MBit)
--		x80000-xfffff	external rom (w mirror)	max. 512 kB (4 MBit)
--
--	ram: 32 bit word
--		mapping ain(16 downto 0) to a(18 downto 2), ignoring ain(18,17)
--	rom: 8 bit word (for flash programming)
--		mapping ain(18 downto 0) to a(18 downto 0)
--
--	todo:
--		ff against meta stability on all inputs
--		mem access to ram is low byte first. this is a little bit inconsistent.
--
--
--	2002-01-03	copy from memio.vhd for bb project
--	2002-01-06	added rs485 uart
--	2001-02-01	some changes in io port definitions
--	2002-05-07	added 'hw' mul
--	2002-07-27	io definitions for baseio
--	2002-08-02	second uart (use first for download and debug)
--	2002-11-01	removed second uart
--	2002-12-01	ram_cnt to 2 (for 20 MHz)
--	2002-12-01	moved all io to ioeth.vhd
--	2002-12-02	wait instruction for memory
--
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity mem is
generic (width : integer; ioa_width : integer; ram_cnt : integer; rom_cnt : integer);

port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	addr		: in std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: in std_logic;

	bsy			: out std_logic;
	dout		: out std_logic_vector(width-1 downto 0);

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nrd			: out std_logic;
	nwr			: out std_logic;

-- io interface

	io_din		: in std_logic_vector(width-1 downto 0);
	io_rd		: out std_logic;
	io_wr		: out std_logic;
	io_addr_wr	: out std_logic
);
end mem;

architecture rtl of mem is

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
							idl, rd1, rd2, rd3, rd4, wr1, wr2, wr3, wr4
						);
	signal state 		: state_type;

	signal mem_wr_addr		: std_logic_vector(19 downto 0);
	signal mem_dout			: std_logic_vector(width-1 downto 0);
	signal mem_din			: std_logic_vector(width-1 downto 0);
	signal mem_rd			: std_logic;
	signal mem_wr			: std_logic;
	signal mem_bsy			: std_logic;

	signal addr_wr			: std_logic;
	signal nwr_int			: std_logic;
	signal ram_access		: std_logic;

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

		mem_wr_addr <= std_logic_vector(to_unsigned(0, 20));

	elsif rising_edge(clk) then

		if (addr_wr='1') then
			mem_wr_addr <= din(19 downto 0);	-- store write address
		end if;

	end if;
end process;


--
--	'delay' nwr 1/2 cycle -> change on falling edge
--
process(clk, reset, nwr_int)

begin
	if (reset='1') then
		nwr <= '1';
	elsif falling_edge(clk) then
		nwr <= nwr_int;
	end if;

end process;

--
--	state machine for external memory (single byte static ram, flash)
--
process(clk, reset, din, mem_wr_addr, mem_rd, mem_wr)

	variable i : integer range 0 to 7;

begin
	if (reset='1') then
		state <= idl;
		a <= "ZZZZZZZZZZZZZZZZZZZ";
		d <= "ZZZZZZZZ";
		nram_cs <= '1';
		nrom_cs <= '1';
		nrd <= '1';
		nwr_int <= '1';
		ram_access <= '1';
		mem_din <= std_logic_vector(to_unsigned(0, width));
		mem_dout <= std_logic_vector(to_unsigned(0, width));
		mem_bsy <= '0';

	elsif rising_edge(clk) then

		case state is

			when idl =>
				a <= "ZZZZZZZZZZZZZZZZZZZ";
				d <= "ZZZZZZZZ";
				nrd <= '1';
				nwr_int <= '1';
				nram_cs <= '1';
				nrom_cs <= '1';
				ram_access <= '1';
				mem_bsy <= '0';

				if (mem_rd='1') then
					if (din(19)='1') then
						a <= din(18 downto 0);
						nrom_cs <= '0';
						ram_access <= '0';
						i := rom_cnt;
					else
						a <= din(16 downto 0) & "00";
						nram_cs <= '0';
						ram_access <= '1';
						i := ram_cnt;
					end if;
					mem_bsy <= '1';
					nrd <= '0';
					state <= rd1;
				elsif (mem_wr='1') then
					mem_dout <= din;
					if (mem_wr_addr(19)='1') then
						a <= mem_wr_addr(18 downto 0);
						nrom_cs <= '0';
						ram_access <= '0';
						i := rom_cnt;
					else
						a <= mem_wr_addr(16 downto 0) & "00";
						nram_cs <= '0';
						ram_access <= '1';
						i := ram_cnt+1;			-- one more for single cycle read
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
				if (i=0) then
					if (ram_access='1') then
						state <= rd2;
						mem_din(7 downto 0) <= d;
						a(1 downto 0) <= "01";
						i := ram_cnt;
					else
						state <= idl;
						mem_bsy <= '0';
						mem_din <= std_logic_vector(to_unsigned(0, width-8)) & d;
					end if;
				end if;

			when rd2 =>
				i := i-1;
				if (i=0) then
					state <= rd3;
					mem_din(15 downto 8) <= d;
					a(1 downto 0) <= "10";
					i := ram_cnt;
				end if;
					
			when rd3 =>
				i := i-1;
				if (i=0) then
					state <= rd4;
					mem_din(23 downto 16) <= d;
					a(1 downto 0) <= "11";
					i := ram_cnt;
				end if;
					
			when rd4 =>
				i := i-1;
				if (i=1) then
					mem_bsy <= '0';					-- release mem_bsy one cycle earlier
				end if;
				if (i=0) then
					state <= idl;
					mem_din(31 downto 24) <= d;
				end if;
--
--	memory write
--
			when wr1 =>
				i := i-1;
				d <= mem_dout(7 downto 0);
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					if (ram_access='1') then
						nwr_int <= '0';
						state <= wr2;
						a(1 downto 0) <= "01";
						i := ram_cnt+1;
					else
						state <= idl;
						mem_bsy <= '0';
					end if;
				end if;

			when wr2 =>
				i := i-1;
				d <= mem_dout(15 downto 8);
				nwr_int <= '0';
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					state <= wr3;
					nwr_int <= '0';
					a(1 downto 0) <= "10";
					i := ram_cnt+1;
				end if;
					
			when wr3 =>
				i := i-1;
				d <= mem_dout(23 downto 16);
				if (i=1) then
					nwr_int <= '1';
				end if;
				if (i=0) then
					state <= wr4;
					nwr_int <= '0';
					a(1 downto 0) <= "11";
					i := ram_cnt+1;
				end if;
					
			when wr4 =>
				i := i-1;
				d <= mem_dout(31 downto 24);
				if (i=1) then
					nwr_int <= '1';
					mem_bsy <= '0';					-- release mem_bsy one cycle earlier
				end if;
				if (i=0) then
					state <= idl;
					nwr_int <= '1';
				end if;
					
		end case;
					
	end if;
end process;


end rtl;
