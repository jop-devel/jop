--
--	mem_trenz.vhd
--
--	external memory interface
--	for Trenz Spartan-3 Retro board
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
--	2004-11-15	Version for Trenz board
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;


entity mem_trenz is
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
--	only one ram bank
--
	ram_addr	: out std_logic_vector(17 downto 0);
	ram_nwe		: out std_logic;
	ram_noe		: out std_logic;

	ram_d		: inout std_logic_vector(15 downto 0);
	ram_ncs		: out std_logic;
	ram_nlb		: out std_logic;
	ram_nub		: out std_logic
);
end mem_trenz;

architecture rtl of mem_trenz is

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
							idl, rd1, rd2, wr1, wr2,
							bc1, bc2
						);
	signal state 		: state_type;

	signal mem_wr_addr		: std_logic_vector(20 downto 0);
	signal mem_wr_val		: std_logic_vector(31 downto 0);
	signal ram_data_hena	: std_logic;
	signal ram_data_lena	: std_logic;
	signal mem_bsy			: std_logic;

	signal nwr_int			: std_logic;

	signal ram_data			: std_logic_vector(31 downto 0);

	signal ram_d_lena		: std_logic;
	signal ram_d_hena		: std_logic;

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

-- for trenz board in two times bc_len (16-bit interface)
	signal bc_cnt			: unsigned(10 downto 0);		-- I can't use bc_len???

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

process(ram_d_lena, ram_d_hena, mem_wr_val)

begin
	if ram_d_lena='1' then
		ram_d <= mem_wr_val(15 downto 0);
	elsif ram_d_hena='1' then
		ram_d <= mem_wr_val(31 downto 16);
	else
		ram_d <= (others => 'Z');
	end if;
end process;


	ram_nlb <= '0';
	ram_nub <= '0';
	ram_ncs <= not ram_cs;
	ram_noe <= not ram_oe;

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
		ram_d_lena <= '0';
		ram_d_hena <= '0';
		ram_cs <= '0';
		ram_oe <= '0';

		ram_data_hena <= '0';
		ram_data_lena <= '0';

		nwr_int <= '1';
		mem_wr_val <= std_logic_vector(to_unsigned(0, 32));
		mem_bsy <= '0';

		bc_wr_ena <= '0';

	elsif rising_edge(clk) then

		case state is

			when idl =>
				ram_d_lena <= '0';
				ram_d_hena <= '0';
				ram_cs <= '0';
				ram_oe <= '0';

				nwr_int <= '1';
				mem_bsy <= '0';

				bc_wr_ena <= '0';

				if (mem_rd='1') then
					ram_int_addr <= din(16 downto 0) & "0";
					ram_cs <= '1';
					ram_oe <= '1';
					wait_state <= to_unsigned(ram_cnt-1, 4);
					mem_bsy <= '1';
					state <= rd1;
				elsif (mem_wr='1') then
					mem_wr_val <= din;
					ram_int_addr <= mem_wr_addr(16 downto 0) & "0";
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
				ram_data_hena <= '0';
				ram_data_lena <= '0';
				if wait_state="0001" then
					ram_data_hena <= '1';		-- was this for fl/nand/ram mux...
				end if;
				if wait_state="0000" then
					wait_state <= to_unsigned(ram_cnt-1, 4);
					state <= rd2;
					ram_int_addr(0) <= '1';
				end if;

			when rd2 =>
				wait_state <= wait_state-1;
--				if wait_state="0010" then		-- ***** only on ram ?????
--					mem_bsy <= '0';					-- release mem_bsy two cycle earlier
--				end if;
				ram_data_hena <= '0';
				ram_data_lena <= '0';
				if wait_state="0001" then
					ram_data_lena <= '1';		-- was this for fl/nand/ram mux...
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
				ram_d_hena <= '1';
				ram_d_lena <= '0';
				if wait_state="0001" then
					nwr_int <= '1';
				end if;
				if wait_state="0000" then
					state <= wr2;
					wait_state <= to_unsigned(ram_cnt, 4);		-- one more for single cycle read
					ram_int_addr(0) <= '1';
					nwr_int <= '0';
				end if;

			when wr2 =>
				wait_state <= wait_state-1;
				ram_d_hena <= '0';
				ram_d_lena <= '1';
				if wait_state="0001" then
					nwr_int <= '1';
				end if;
				if wait_state="0000" then
					state <= idl;
					mem_bsy <= '0';
				end if;

--
--	bytecode read
--
			when bc1 =>
				ram_int_addr <= std_logic_vector(bc_start(16 downto 0)) & "0";
				bc_cnt <= bc_len & "0";				-- count in 16-bit words
				bc_wr_addr <= (others => '0');		-- we start at zero offset for now (no caching)
				bc_wr_ena <= '0';
				ram_cs <= '1';
				ram_oe <= '1';
				wait_state <= to_unsigned(bc_ram_cnt, 4);
				state <= bc2;

-- this version reads one more word, but I don't care at the moment
-- and we do not pipeline read and bc write -- just too lazy at the moment
			when bc2 =>
				bc_wr_ena <= '0';
				wait_state <= wait_state-1;
				ram_data_hena <= '0';
				ram_data_lena <= '0';
				if wait_state="0010" then
					-- use last counter bit to slect part of 32 bit word
					ram_data_hena <= not ram_int_addr(0);
					ram_data_lena <= ram_int_addr(0);
				end if;
				if wait_state="0001" then
					-- full word is ready
					if ram_int_addr(0) = '1' then
						bc_wr_ena <= '1';
					end if;
				end if;
				if wait_state="0000" then
					wait_state <= to_unsigned(bc_ram_cnt, 4);
					ram_int_addr <= std_logic_vector(unsigned(ram_int_addr)+1);
					bc_cnt <= bc_cnt-1;
					-- increment bc address only on every second 16-bit word
					if ram_int_addr(0) = '1' then
						bc_wr_addr <= bc_wr_addr+1;		-- next jbc address
					end if;
				else
					if bc_cnt="0000000000" then
						state <= idl;
					end if;
				end if;

		end case;
					
	end if;
end process;

process(clk, reset, ram_data_hena, ram_data_lena)

begin
	if (reset='1') then
--		ram_data <= std_logic_vector(to_unsigned(0, 32));
ram_data <= (others => '1');
	elsif rising_edge(clk) then
		if ram_data_hena='1' then
			ram_data(31 downto 16) <= ram_d;
		end if;
		if ram_data_lena='1' then
			ram_data(15 downto 0) <= ram_d;
		end if;
	end if;
end process;

end rtl;
