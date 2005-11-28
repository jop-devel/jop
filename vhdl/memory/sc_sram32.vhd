--
--	sc_sram32.vhd
--
--	SimpCon compliant external memory interface
--	for 32-bit SRAM (e.g. Cyclone board)
--
--	Connection between mem_sc and the external memory bus
--
--	memory mapping
--	
--		000000-x7ffff	external SRAM (w mirror)	max. 512 kW (4*4 MBit)
--		080000-xfffff	external Flash (w mirror)	max. 512 kB (4 MBit)
--		100000-xfffff	external NAND flash
--
--	RAM: 32 bit word
--	ROM: 8 bit word (for flash programming)
--
--	todo:
--		make a version with Flash interface
--		
--
--	2005-11-22	first version
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;

entity sc_mem_if is
generic (ram_ws : integer; rom_cnt : integer; addr_bits : integer);

port (

	clk, reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

-- memory interface

	ram_addr	: out std_logic_vector(17 downto 0);
	ram_dout	: out std_logic_vector(31 downto 0);
	ram_din		: in std_logic_vector(31 downto 0);
	ram_dout_en	: out std_logic;
	ram_ncs		: out std_logic;
	ram_noe		: out std_logic;
	ram_nwe		: out std_logic;

--
--	config/program flash and big nand flash interface
--
	fl_a	: out std_logic_vector(18 downto 0);
	fl_d	: inout std_logic_vector(7 downto 0);
	fl_ncs	: out std_logic;
	fl_ncsb	: out std_logic;
	fl_noe	: out std_logic;
	fl_nwe	: out std_logic;
	fl_rdy	: in std_logic

);
end sc_mem_if;

architecture rtl of sc_mem_if is

--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1, rd2,
							wr1
						);
	signal state 		: state_type;
	signal next_state	: state_type;

	signal nwr_int		: std_logic;
	signal wait_state	: unsigned(3 downto 0);
	signal cnt			: unsigned(1 downto 0);

	signal dout_ena		: std_logic;
	signal rd_data_ena	: std_logic;

begin

	ram_dout_en <= dout_ena;

	rdy_cnt <= cnt;

--
--	Register memory address, write data and read data
--
process(clk, reset)
begin
	if reset='1' then

		ram_addr <= (others => '0');
		ram_dout <= (others => '0');
		rd_data <= (others => '0');

	elsif rising_edge(clk) then

		if rd='1' or wr='1' then
			ram_addr <= address(17 downto 0);
		end if;
		if wr='1' then
			ram_dout <= wr_data;
		end if;
		if rd_data_ena='1' then
			rd_data <= ram_din;
		end if;

	end if;
end process;

--
--	'delay' nwe 1/2 cycle -> change on falling edge
--
process(clk, reset)

begin
	if (reset='1') then
		ram_nwe <= '1';
--		ram_noe <= '1';
	elsif falling_edge(clk) then
		ram_nwe <= nwr_int;
--		ram_noe <= noe_int;
	end if;

end process;


--
--	next state logic
--
process(state, rd, wr, wait_state)

begin

	next_state <= state;


	case state is

		when idl =>
			if rd='1' then
				if ram_ws=0 then
					-- then we omit state rd1!
					next_state <= rd2;
				else
					next_state <= rd1;
				end if;
			elsif wr='1' then
				next_state <= wr1;
			end if;

		-- the WS state
		when rd1 =>
			if wait_state=2 then
				next_state <= rd2;
			end if;

		-- last read state
		when rd2 =>
			next_state <= idl;
			-- This should do to give us a pipeline
			-- level of 1 for read
			if rd='1' then
				if ram_ws=0 then
					-- then we omit state rd1!
					next_state <= rd2;
				else
					next_state <= rd1;
				end if;
			elsif wr='1' then
				next_state <= wr1;
			end if;
			
		-- the WS state
		when wr1 =>
-- TODO: check what happens on ram_ws=0
-- TODO: do we need a write pipelining?
--	not at the moment, but parhaps later when
--	we write the stack content to main memory
			if wait_state=1 then
				next_state <= idl;
			end if;

	end case;
				
end process;

--
--	state machine register
--	output register
--
process(clk, reset)

begin
	if (reset='1') then
		state <= idl;
		dout_ena <= '0';
		ram_ncs <= '1';
		ram_noe <= '1';
		rd_data_ena <= '0';
	elsif rising_edge(clk) then

		state <= next_state;
		dout_ena <= '0';
		ram_ncs <= '1';
		ram_noe <= '1';
		rd_data_ena <= '0';

		case next_state is

			when idl =>

			-- the wait state
			when rd1 =>
				ram_ncs <= '0';
				ram_noe <= '0';

			-- last read state
			when rd2 =>
				ram_ncs <= '0';
				ram_noe <= '0';
				rd_data_ena <= '1';
				
				
			-- the WS state
			when wr1 =>
				ram_ncs <= '0';
				dout_ena <= '1';

		end case;
					
	end if;
end process;

--
--	nwr combinatorial processing
--	for the negativ edge
--
process(next_state, state)
begin

	nwr_int <= '1';
	if next_state=wr1 then
		nwr_int <= '0';
	end if;

end process;

--
-- wait_state processing
-- cs delay, dout enable
--
process(clk, reset)
begin
	if (reset='1') then
		wait_state <= (others => '1');
		cnt <= "00";
	elsif rising_edge(clk) then

		wait_state <= wait_state-1;

		cnt <= "11";
		if next_state=idl then
			cnt <= "00";
		-- if wait_state<4 then
		elsif wait_state(3 downto 2)="00" then
			cnt <= wait_state(1 downto 0)-1;
		end if;

		if rd='1' then
			wait_state <= to_unsigned(ram_ws+1, 4);
			if ram_ws<3 then
				cnt <= to_unsigned(ram_ws+1, 2);
			else
				cnt <= "11";
			end if;
		elsif wr='1' then
			-- one more cycle for the write
			-- But in original mem32 this was only true
			-- for ram_cnt=2!
			if ram_ws<3 then
				cnt <= to_unsigned(ram_ws+1, 2);
			else
				cnt <= "11";
			end if;
			wait_state <= to_unsigned(ram_ws+1, 4);
--		else
--			-- do we need this?
--			-- we don't care about wait_state in state idle
--			if state=idl then
--				wait_state <= (others => '1');			-- keep it on max value
--			end if;
		end if;


	end if;
end process;

-- TODO: move Flash interface to a second WB interface

	fl_a <= (others => '0');
	fl_d <= (others => 'Z');
	fl_ncs <= '1';
	fl_ncsb <= '1';
	fl_noe <= '1';
	fl_nwe <= '1';
--	fl_rdy	: in std_logic

end rtl;
