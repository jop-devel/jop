--
--	mem_xs3.vhd
--
--	external memory interface
--	for Spartan-3 Starter Kit
--
--
--	memory mapping
--	
--		000000-x7ffff	external ram (w mirror)	max. 512 kW (4*4 MBit)
--
--	ram: 32 bit word
--
--	todo:
--		check timing for async mem (zero ws?)
--
--	2004-10-01	Version for Spartan board
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;


entity mem_xs3 is
generic (jpc_width : integer; ram_cnt : integer);

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
	ram_addr	: out std_logic_vector(17 downto 0);
	ram_nwe		: out std_logic;
	ram_noe		: out std_logic;

	rama_d		: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	ramb_d		: inout std_logic_vector(15 downto 0);
	ramb_ncs	: out std_logic;
	ramb_nlb	: out std_logic;
	ramb_nub	: out std_logic

);
end mem_xs3;

architecture rtl of mem_xs3 is

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


-- VIRTEX-II ONLY Block Select RAM

-- WARNING!!!!! WARNING!!!!!
-- Only four lines of INIT values have been included as an example.
-- The user is responsible for adding the correct number of INIT
-- values. Refer to the Libraries Guide for more information.


-- INITP: Parity Memory
-- INIT: Data Memory


----- Component RAMB16_S9_S36 -----
component RAMB16_S9_S36 
--
  generic (
	   WRITE_MODE_A : string := "WRITE_FIRST";
	   WRITE_MODE_B : string := "WRITE_FIRST";
	   INIT_A : bit_vector  := X"000";
	   SRVAL_A : bit_vector  := X"000";

	   INIT_B : bit_vector  := X"000000000";
	   SRVAL_B : bit_vector  := X"000000000";

	   INITP_00 : bit_vector := X"0000000000000000000000000000000000000000000000000000000000000000";
	   INITP_01 : bit_vector := X"0000000000000000000000000000000000000000000000000000000000000000";
	   INITP_02 : bit_vector := X"0000000000000000000000000000000000000000000000000000000000000000";
	   INITP_03 : bit_vector := X"0000000000000000000000000000000000000000000000000000000000000000";
	   INIT_00 : bit_vector := X"0000000000000000000000000000000000000000000000000000000000000000";
	   INIT_01 : bit_vector := X"0000000000000000000000000000000000000000000000000000000000000000";
	   INIT_02 : bit_vector := X"0000000000000000000000000000000000000000000000000000000000000000";
	   INIT_03 : bit_vector := X"0000000000000000000000000000000000000000000000000000000000000000"
  );
--
  port (DIA	: in STD_LOGIC_VECTOR (7 downto 0);
		DIB	: in STD_LOGIC_VECTOR (31 downto 0);
		DIPA	: in STD_LOGIC_VECTOR (0 downto 0);
		DIPB	: in STD_LOGIC_VECTOR (3 downto 0);
		ENA	: in STD_logic;
		ENB	: in STD_logic;
		WEA	: in STD_logic;
		WEB	: in STD_logic;
		SSRA   : in STD_logic;
		SSRB   : in STD_logic;
		CLKA   : in STD_logic;
		CLKB   : in STD_logic;
		ADDRA  : in STD_LOGIC_VECTOR (10 downto 0);
		ADDRB  : in STD_LOGIC_VECTOR (8 downto 0);
		DOA	: out STD_LOGIC_VECTOR (7 downto 0);
		DOB	: out STD_LOGIC_VECTOR (31 downto 0);
		DOPA	: out STD_LOGIC_VECTOR (0 downto 0);
		DOPB	: out STD_LOGIC_VECTOR (3 downto 0)
	   ); 

end component;

--
--	generated with Quartus wizzard:
--
--	COMPONENT altsyncram
--	GENERIC (
--		intended_device_family		: STRING;
--		operation_mode		: STRING;
--		width_a		: NATURAL;
--		widthad_a		: NATURAL;
--		numwords_a		: NATURAL;
--		width_b		: NATURAL;
--		widthad_b		: NATURAL;
--		numwords_b		: NATURAL;
--		lpm_type		: STRING;
--		width_byteena_a		: NATURAL;
--		outdata_reg_b		: STRING;
--		indata_aclr_a		: STRING;
--		wrcontrol_aclr_a		: STRING;
--		address_aclr_a		: STRING;
--		address_reg_b		: STRING;
--		address_aclr_b		: STRING;
--		outdata_aclr_b		: STRING;
--		read_during_write_mode_mixed_ports		: STRING
--	);
--	PORT (
--			wren_a	: IN STD_LOGIC ;
--			clock0	: IN STD_LOGIC ;
--			address_a	: IN STD_LOGIC_VECTOR (7 DOWNTO 0);
--			address_b	: IN STD_LOGIC_VECTOR (9 DOWNTO 0);
--			q_b	: OUT STD_LOGIC_VECTOR (7 DOWNTO 0);
--			data_a	: IN STD_LOGIC_VECTOR (31 DOWNTO 0)
--	);
--	END COMPONENT;
--


--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, wr1,
							bc1, bc2, bc3
						);
	signal state 		: state_type;

	signal mem_wr_addr		: std_logic_vector(20 downto 0);
	signal mem_wr_val		: std_logic_vector(31 downto 0);
	signal ram_data_ena		: std_logic;
	signal mem_bsy			: std_logic;

	signal nwr_int			: std_logic;

	signal ram_data			: std_logic_vector(31 downto 0);

	signal ram_d_ena		: std_logic;

	signal ram_cs, ram_oe	: std_logic;
	signal ram_int_addr		: std_logic_vector(17 downto 0);

	signal wait_state		: unsigned(3 downto 0);

--
--	values for bytecode read/cache
--
--	len is in words, 10 bits range is 'hardcoded' in JOPWriter.java
--	start is address in external memory (rest of the word)
--
	signal bc_len			: unsigned(9 downto 0);		-- length of method in words
	signal bc_start			: unsigned(17 downto 0);	-- memory address of bytecode
	signal bc_wr_addr		: unsigned(7 downto 0);		-- address for jbc
	signal bc_wr_data		: std_logic_vector(31 downto 0);	-- write data for jbc
	signal bc_wr_ena		: std_logic;

	signal bc_cnt			: unsigned(9 downto 0);		-- I can't use bc_len???

	-- the block ram is 2KB instead of 1KB!
	signal bram_addra		: std_logic_vector(jpc_width-1+1 downto 0);
	signal bram_addrb		: std_logic_vector(8 downto 0);

	constant bc_ram_cnt	: integer := ram_cnt;			-- a different constant for perf. tests

begin

	bsy <= mem_bsy;
	dout <= ram_data;
	bcstart <= (others => '0');	-- for now we load only at base 0

	-- change byte order for jbc memory (high byte first)
	bc_wr_data <= ram_data(7 downto 0) &
				ram_data(15 downto 8) &
				ram_data(23 downto 16) &
				ram_data(31 downto 24);

--	cmp_jbc: jbc generic map (8, jpc_width) port map(din, jbc_addr, jpc_wr, bc_wr, clk, jbc_data);


--
--	generated with Quartus wizzard:
--
--	cmp_jbc : altsyncram
--	GENERIC MAP (
--		intended_device_family => "Cyclone",
--		operation_mode => "DUAL_PORT",
--		width_a => 32,
--		widthad_a => 8,
--		numwords_a => 256,
--		width_b => 8,
--		widthad_b => 10,
--		numwords_b => 1024,
--		lpm_type => "altsyncram",
--		width_byteena_a => 1,
--		outdata_reg_b => "UNREGISTERED",
--		indata_aclr_a => "NONE",
--		wrcontrol_aclr_a => "NONE",
--		address_aclr_a => "NONE",
--		address_reg_b => "CLOCK0",
--		address_aclr_b => "NONE",
--		outdata_aclr_b => "NONE",
--		read_during_write_mode_mixed_ports => "DONT_CARE"
--	)
--	PORT MAP (
--		wren_a => bc_wr_ena,
--		clock0 => clk,
--		address_a => std_logic_vector(bc_wr_addr),
--		address_b => jbc_addr,
--		data_a => bc_wr_data,
--		q_b => jbc_data
--	);

	-- the block ram is 2KB instead of 1KB!
	bram_addra <= "0" & jbc_addr;
	bram_addrb <= "0" & std_logic_vector(bc_wr_addr);

	cmp_jbc : RAMB16_S9_S36 
	port map (
  		DIA => "00000000",
		DIB => bc_wr_data,
		DIPA => "0",
		DIPB => "0000",
		ENA => '1',
		ENB => '1',
		WEA => '0',
		WEB => bc_wr_ena,
		SSRA => '0',
		SSRB => '0',
		CLKA => clk,
		CLKB => clk,
		ADDRA => bram_addra,
		ADDRB => bram_addrb,
		DOA => jbc_data,
		DOB => open,
		DOPA => open,
		DOPB => open
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
		bc_start <= (others => '0');
	elsif rising_edge(clk) then
		if (mem_bc_rd='1') then
			bc_len <= unsigned(din(9 downto 0));
			bc_start <= unsigned(din(27 downto 10));
		end if;

	end if;
end process;

--
--	'delay' nwr 1/2 cycle -> change on falling edge
--
process(clk, reset, nwr_int)

begin
	if (reset='1') then
		ram_nwe <= '1';
	elsif falling_edge(clk) then
		ram_nwe <= nwr_int;
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
	ram_noe <= not ram_oe;
	ramb_ncs <= not ram_cs;

--
--	To put this in an output register
--	we have to make an assignment... With Xilinx too?
--
	ram_addr <= ram_int_addr;

--
--	state machine for external memory (single byte static ram, flash)
--
process(clk, reset, din, mem_wr_addr, mem_rd, mem_wr, mem_bc_rd)

begin
	if (reset='1') then
		state <= idl;
		ram_int_addr <= (others => '0');
		ram_d_ena <= '0';
		ram_cs <= '0';
		ram_oe <= '0';

		ram_data_ena <= '0';

		nwr_int <= '1';
		mem_wr_val <= std_logic_vector(to_unsigned(0, 32));
		mem_bsy <= '0';

		bc_wr_ena <= '0';

	elsif rising_edge(clk) then

		case state is

			when idl =>
				ram_d_ena <= '0';
				ram_cs <= '0';
				ram_oe <= '0';

				nwr_int <= '1';
				mem_bsy <= '0';

				bc_wr_ena <= '0';

				if (mem_rd='1') then
					ram_int_addr <= din(17 downto 0);
					ram_cs <= '1';
					ram_oe <= '1';
					wait_state <= to_unsigned(ram_cnt-1, 4);
					mem_bsy <= '1';
					state <= rd1;
				elsif (mem_wr='1') then
					mem_wr_val <= din;
					ram_int_addr <= mem_wr_addr(17 downto 0);
					ram_cs <= '1';
					wait_state <= to_unsigned(ram_cnt, 4);		-- one more for single cycle read
					mem_bsy <= '1';
					nwr_int <= '0';
					state <= wr1;
				elsif (mem_bc_rd='1') then
					mem_bsy <= '1';
					state <= bc1;
				end if;

--
--	memory read
--
			when rd1 =>
				wait_state <= wait_state-1;
				if wait_state="0010" then		-- ***** only on ram ?????
					mem_bsy <= '0';					-- release mem_bsy two cycle earlier
				end if;
				ram_data_ena <= '0';
				if wait_state="0001" then
					ram_data_ena <= '1';		-- was this for fl/nand/ram mux...
				end if;
				if wait_state="0000" then
					state <= idl;
					mem_bsy <= '0';
				end if;

--
--	memory write
--
			when wr1 =>
				wait_state <= wait_state-1;
				ram_d_ena <= '1';
				if wait_state="0001" then
					nwr_int <= '1';
-- only ram ???					mem_bsy <= '0';					-- release mem_bsy one cycle earlier
				end if;
				if wait_state="0000" then
					state <= idl;
					mem_bsy <= '0';
				end if;

--
--	bytecode read
--
			when bc1 =>
				ram_int_addr <= std_logic_vector(bc_start);
				bc_cnt <= bc_len;
				bc_wr_addr <= (others => '0');		-- we start at zero offset for now (no caching)
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
					ram_int_addr <= std_logic_vector(unsigned(ram_int_addr)+1);
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
					ram_int_addr <= std_logic_vector(unsigned(ram_int_addr)+1);
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

process(clk, reset, ram_data_ena)

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
