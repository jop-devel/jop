--
--	mem32.vhd
--
--	external memory interface (for the Cyclone board)
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
--	2004-09-14	flash/ram mux after data register in dout path
--	2004-12-08	release mem_bsy three cycle earlier on write
--	2005-01-11	Added cache
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

LIBRARY altera_mf;
USE altera_mf.altera_mf_components.all;

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
	mem_bc_rd	: in std_logic;
	dout		: out std_logic_vector(31 downto 0);
	bcstart		: out std_logic_vector(31 downto 0); 	-- start of method in bc cache

	bsy			: out std_logic;

-- jbc connections

	jbc_addr	: in std_logic_vector(jpc_width-1 downto 0);
	jbc_data	: out std_logic_vector(7 downto 0);

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

component cache is
generic (jpc_width : integer);

port (

	clk, reset	: in std_logic;

	bc_len		: in std_logic_vector(9 downto 0);		-- length of method in words
	bc_addr		: in std_logic_vector(17 downto 0);		-- memory address of bytecode

	find		: in std_logic;							-- start lookup

	bcstart		: out std_logic_vector(jpc_width-3 downto 0); 	-- start of method in bc cache

	rdy			: out std_logic;						-- lookup finished
	in_cache	: out std_logic							-- method is in cache

);
end component;

--
--	jbc component (use technology specific vhdl-file (ajbc/xjbc))
--
--	dual port ram
--	wraddr and wrena registered and delayed
--	rdaddr is registered
--	indata registered
--	outdata is unregistered
--
--component jbc is
--generic (width : integer; addr_width : integer);
--port (
--	data		: in std_logic_vector(31 downto 0);
--	rdaddress	: in std_logic_vector(jpc_width-1 downto 0);
--	wr_addr		: in std_logic;									-- load start address (=jpc)
--	wren		: in std_logic;
--	clock		: in std_logic;
--
--	q			: out std_logic_vector(7 downto 0)
--);
--end component;

--
--	generated with Quartus wizzard:
--
	COMPONENT altsyncram
	GENERIC (
		intended_device_family		: STRING;
		operation_mode		: STRING;
		width_a		: NATURAL;
		widthad_a		: NATURAL;
		numwords_a		: NATURAL;
		width_b		: NATURAL;
		widthad_b		: NATURAL;
		numwords_b		: NATURAL;
		lpm_type		: STRING;
		width_byteena_a		: NATURAL;
		outdata_reg_b		: STRING;
		indata_aclr_a		: STRING;
		wrcontrol_aclr_a		: STRING;
		address_aclr_a		: STRING;
		address_reg_b		: STRING;
		address_aclr_b		: STRING;
		outdata_aclr_b		: STRING;
		read_during_write_mode_mixed_ports		: STRING
	);
	PORT (
			wren_a	: IN STD_LOGIC ;
			clock0	: IN STD_LOGIC ;
			address_a	: IN STD_LOGIC_VECTOR (7 DOWNTO 0);
			address_b	: IN STD_LOGIC_VECTOR (9 DOWNTO 0);
			q_b	: OUT STD_LOGIC_VECTOR (7 DOWNTO 0);
			data_a	: IN STD_LOGIC_VECTOR (31 DOWNTO 0)
	);
	END COMPONENT;



--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, wr1,
							bc0, bc1, bc2, bc3
						);
	signal state 		: state_type;

	signal mem_wr_addr		: std_logic_vector(20 downto 0);
	signal mem_wr_val		: std_logic_vector(31 downto 0);
	signal mem_rd_mux		: std_logic_vector(31 downto 0);
	signal ram_data_ena		: std_logic;
	signal mem_bsy			: std_logic;

	signal nwr_int			: std_logic;
	signal ram_access		: std_logic;
	signal nand_access		: std_logic;

	signal ram_data			: std_logic_vector(31 downto 0);
	signal flash_data		: std_logic_vector(8 downto 0);

	signal sel_flash		: std_logic;

	signal fl_d_ena			: std_logic;
	signal ram_d_ena		: std_logic;

	signal ram_cs, ram_oe	: std_logic;
	signal ram_addr			: std_logic_vector(17 downto 0);

	signal wait_state		: unsigned(3 downto 0);

--
--	values for bytecode read/cache
--
--	len is in words, 10 bits range is 'hardcoded' in JOPWriter.java
--	start is address in external memory (rest of the word)
--
	signal bc_len			: unsigned(9 downto 0);				-- length of method in words
	signal bc_mem_start		: unsigned(17 downto 0);			-- memory address of bytecode
	signal bc_wr_addr		: unsigned(7 downto 0);				-- address for jbc
	signal bc_wr_data		: std_logic_vector(31 downto 0);	-- write data for jbc
	signal bc_wr_ena		: std_logic;

	signal bc_cnt			: unsigned(9 downto 0);				-- I can't use bc_len???

	constant bc_ram_cnt	: integer := ram_cnt;					-- a different constant for perf. tests

--
--	signals for cache connection
--
	signal cache_rdy		: std_logic;
	signal cache_in_cache	: std_logic;
	signal cache_bcstart	: std_logic_vector(jpc_width-3 downto 0);

begin

	bsy <= mem_bsy;
	dout <= mem_rd_mux;
	bcstart <= std_logic_vector(to_unsigned(0, 32-jpc_width)) & cache_bcstart & "00";

	-- change byte order for jbc memory (high byte first)
	bc_wr_data <= ram_data(7 downto 0) &
				ram_data(15 downto 8) &
				ram_data(23 downto 16) &
				ram_data(31 downto 24);


	cmp_cache: cache generic map (jpc_width) port map(
		clk, reset,
		std_logic_vector(bc_len), std_logic_vector(bc_mem_start),
		mem_bc_rd,
		cache_bcstart,
		cache_rdy, cache_in_cache
	);



--	cmp_jbc: jbc generic map (8, jpc_width) port map(din, jbc_addr, jpc_wr, bc_wr, clk, jbc_data);


--
--	generated with Quartus wizzard:
--
	cmp_jbc : altsyncram
	GENERIC MAP (
		intended_device_family => "Cyclone",
		operation_mode => "DUAL_PORT",
		width_a => 32,
		widthad_a => 8,
		numwords_a => 256,
		width_b => 8,
		widthad_b => 10,
		numwords_b => 1024,
		lpm_type => "altsyncram",
		width_byteena_a => 1,
		outdata_reg_b => "UNREGISTERED",
		indata_aclr_a => "NONE",
		wrcontrol_aclr_a => "NONE",
		address_aclr_a => "NONE",
		address_reg_b => "CLOCK0",
		address_aclr_b => "NONE",
		outdata_aclr_b => "NONE",
		read_during_write_mode_mixed_ports => "DONT_CARE"
	)
	PORT MAP (
		wren_a => bc_wr_ena,
		clock0 => clk,
		address_a => std_logic_vector(bc_wr_addr),
		address_b => jbc_addr,
		data_a => bc_wr_data,
		q_b => jbc_data
	);



--
--	wr_addr write
--		one cycle after io write (address is avaliable one cycle before ex stage)
--
process(clk, reset, din, mem_addr_wr)

begin
	if (reset='1') then

		mem_wr_addr <= (others => '0');

	elsif rising_edge(clk) then

		if (mem_addr_wr='1') then
			mem_wr_addr <= din(20 downto 0);	-- store write address
		end if;

	end if;
end process;

process(clk, reset, din) begin

	if (reset='1') then
		bc_len <= (others => '0');
		bc_mem_start <= (others => '0');
	elsif rising_edge(clk) then
		if (mem_bc_rd='1') then
			bc_len <= unsigned(din(9 downto 0));
			bc_mem_start <= unsigned(din(27 downto 10));
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
process(clk, reset, din, mem_wr_addr, mem_rd, mem_wr, mem_bc_rd)

begin
	if (reset='1') then
		state <= idl;
		ram_addr <= (others => '0');
		ram_d_ena <= '0';
		ram_cs <= '0';
		ram_oe <= '0';

		ram_data_ena <= '0';

		-- fl_a <= (others => 'Z');
		fl_a <= (others => '0');
		fl_d_ena <= '0';
		sel_flash <= '0';					-- select AMD flash as default
		fl_noe <= '1';

		nwr_int <= '1';
		fl_nwe <= '1';
		ram_access <= '1';
		nand_access <= '0';
		mem_wr_val <= std_logic_vector(to_unsigned(0, 32));
		mem_bsy <= '0';

		bc_wr_ena <= '0';

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
				nand_access <= '0';
				mem_bsy <= '0';

				bc_wr_ena <= '0';

				if (mem_rd='1') then
					if (din(20 downto 19) = "00") then	-- ram
						ram_addr <= din(17 downto 0);
						ram_cs <= '1';
						ram_oe <= '1';
						ram_access <= '1';
						wait_state <= to_unsigned(ram_cnt-1, 4);
					elsif (din(20 downto 19) = "01") then	-- flash
						fl_a <= din(18 downto 0);
						sel_flash <= '0';
						fl_noe <= '0';
						ram_access <= '0';
						wait_state <= to_unsigned(rom_cnt-1, 4);
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
						wait_state <= to_unsigned(rom_cnt-1, 4);
					end if;
					mem_bsy <= '1';
					state <= rd1;
				elsif (mem_wr='1') then
					mem_wr_val <= din;
					if (mem_wr_addr(20 downto 19) = "00") then	-- ram
						ram_addr <= mem_wr_addr(17 downto 0);
						ram_cs <= '1';
						ram_access <= '1';
						wait_state <= to_unsigned(ram_cnt, 4);		-- one more for single cycle read
					elsif (mem_wr_addr(20 downto 19) = "01") then	-- flash
						fl_a <= mem_wr_addr(18 downto 0);
						sel_flash <= '0';
						ram_access <= '0';
						wait_state <= to_unsigned(rom_cnt-1, 4);
					else								-- NAND
						fl_a <= mem_wr_addr(18 downto 0);
						sel_flash <= '1';
						ram_access <= '0';
						wait_state <= to_unsigned(rom_cnt-1, 4);
					end if;
					mem_bsy <= '1';
					nwr_int <= '0';
					state <= wr1;
				elsif (mem_bc_rd='1') then
					mem_bsy <= '1';
					ram_access <= '1';
					state <= bc0;
				end if;

--
--	memory read
--
			when rd1 =>
				wait_state <= wait_state-1;
				if wait_state="0010" then	
					mem_bsy <= '0';			-- release mem_bsy two cycles earlier
				end if;	
				ram_data_ena <= '0';
				if wait_state="0001" then
					if ram_access='1' then
						ram_data_ena <= '1';
					end if;
				end if;
				if wait_state="0000" then
					if (nand_access='1') then
						flash_data <= fl_rdy & fl_d;
					else
						flash_data <= "0" & fl_d;
					end if;
					state <= idl;
					mem_bsy <= '0';
				end if;

--
--	memory write
--
			when wr1 =>
				wait_state <= wait_state-1;
				if (ram_access='1') then
					ram_d_ena <= '1';
				else
					fl_nwe <= '0';
					fl_d_ena <= '1';
				end if;
				if wait_state="0011" then
					mem_bsy <= '0';			-- release mem_bsy three cycles earlier
				end if;
				if wait_state="0001" then
					fl_nwe <= '1';
					nwr_int <= '1';
				end if;
				if wait_state="0000" then
					fl_nwe <= '1';
					state <= idl;
					mem_bsy <= '0';
				end if;

--
--	bytecode read
--
			-- cache lookup
			when bc0 =>
				if cache_rdy = '1' then
					if cache_in_cache = '1' then
						state <= idl;
					else
						state <= bc1;
					end if;
				end if;

			-- not in cache
			when bc1 =>
				ram_addr <= std_logic_vector(bc_mem_start);
				bc_cnt <= bc_len;
				bc_wr_addr <= unsigned(cache_bcstart);
				bc_wr_ena <= '0';
				ram_cs <= '1';
				ram_oe <= '1';
				wait_state <= to_unsigned(bc_ram_cnt-1, 4);
				state <= bc2;

			when bc2 =>
				bc_wr_ena <= '0';
				wait_state <= wait_state-1;
				ram_data_ena <= '0';
				if wait_state="0001" then
					ram_data_ena <= '1';
				end if;
				if wait_state="0000" then
					wait_state <= to_unsigned(bc_ram_cnt-1, 4);
					ram_addr <= std_logic_vector(unsigned(ram_addr)+1);
					bc_cnt <= bc_cnt-1;
					state <=bc3;
				end if;

-- this version reads one more word, but I don't care at the moment
			when bc3 =>
				bc_wr_ena <= '0';
				wait_state <= wait_state-1;
				ram_data_ena <= '0';
				if wait_state="0001" then
					ram_data_ena <= '1';
				end if;
				if wait_state="0000" then
					wait_state <= to_unsigned(bc_ram_cnt-1, 4);
					ram_addr <= std_logic_vector(unsigned(ram_addr)+1);
					bc_cnt <= bc_cnt-1;
					bc_wr_addr <= bc_wr_addr+1;		-- next jbc address
				else
					bc_wr_ena <= '1';				-- write former word
					if bc_cnt="0000000000" then
						state <= idl;
					end if;
				end if;

		end case;
					
	end if;
end process;

process(ram_access, ram_data, flash_data)

begin
	--
	--	this mux costs about 6ns, tsu is 5.2ns (could be negativ!)
	--
	-- we could move this mux after the two registers befor dout
	if (ram_access='1') then
		mem_rd_mux <= ram_data;
	else
		mem_rd_mux <= std_logic_vector(to_unsigned(0, 32-9)) & flash_data;
	end if;
end process;

process(clk, reset, mem_rd_mux, ram_data_ena)

begin
	if (reset='1') then
		ram_data <= std_logic_vector(to_unsigned(0, 32));
	elsif rising_edge(clk) then
		if ram_data_ena='1' then
			ram_data <= ramb_d & rama_d;
		end if;
	end if;
end process;

end rtl;
