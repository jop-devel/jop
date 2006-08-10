--
--	sc_sram16.vhd
--
--	SimpCon compliant external memory interface
--	for 16-bit SRAM (e.g. Altera DE2 board or
--	half of Cycore).
--
--	High 16-bit word is at lower address
--
--	Connection between mem_sc and the external memory bus
--
--	memory mapping
--	
--		000000-x7ffff	external SRAM (w mirror)	max. 512 kW (4*4 MBit)
--
--	RAM: 16 bit word
--
--
--	2006-08-01	Adapted from sc_ram32.vhd
--	2006-08-08	Hardcoded 2 cycle memory interface
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;

entity sc_mem_if is
generic (ram_ws : integer; addr_bits : integer);

port (

	clk, reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

-- memory interface

	ram_addr	: out std_logic_vector(addr_bits-1 downto 0);
	ram_dout	: out std_logic_vector(15 downto 0);
	ram_din		: in std_logic_vector(15 downto 0);
	ram_dout_en	: out std_logic;
	ram_ncs		: out std_logic;
	ram_noe		: out std_logic;
	ram_nwe		: out std_logic

);
end sc_mem_if;

architecture rtl of sc_mem_if is

--
--	signals for mem interface
--
	type state_type		is (
							idl, rd1_h, rd2_h, rd_idl, rd1_l, rd2_l,
							wr1_h, wr2_h, wr_idl, wr1_l, wr2_l
						);
	signal state 		: state_type;
	signal next_state	: state_type;

	signal nwr_int		: std_logic;
	signal wait_state	: unsigned(3 downto 0);
	signal cnt			: unsigned(1 downto 0);

	signal dout_ena		: std_logic;
	signal rd_data_ena_h	: std_logic;
	signal rd_data_ena_l	: std_logic;
	signal inc_addr			: std_logic;
	signal wr_low			: std_logic;

	signal ram_dout_low	: std_logic_vector(15 downto 0);
	signal ram_din_low		: std_logic_vector(15 downto 0);

	signal ram_din_reg	: std_logic_vector(31 downto 0);

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
--		rd_data <= (others => '0');
--		ram_din_reg <= (others => '0');
		ram_dout_low <= (others => '0');

	elsif rising_edge(clk) then

		if rd='1' or wr='1' then
			ram_addr <= address(addr_bits-2 downto 0) & "0";
		end if;
		if inc_addr='1' then
			ram_addr(0) <= '1';
		end if;
		if wr='1' then
			ram_dout <= wr_data(31 downto 16);
			ram_dout_low <= wr_data(15 downto 0);
		end if;
		if wr_low='1' then
			ram_dout <= ram_dout_low;
		end if;
		if rd_data_ena_h='1' then
			ram_din_reg(15 downto 0) <= ram_din;
--			rd_data(31 downto 16) <= ram_din;
		end if;
		if rd_data_ena_l='1' then
			-- move first word to higher half
			ram_din_reg(31 downto 16) <= ram_din_reg(15 downto 0);
			-- read second word
			ram_din_reg(15 downto 0) <= ram_din;
--			rd_data(15 downto 0) <= ram_din;
		end if;

	end if;
end process;

	rd_data <= ram_din_reg;

--
--	'delay' nwe 1/2 cycle -> change on falling edge
--
process(clk, reset)

begin
	if (reset='1') then
		ram_nwe <= '1';
	elsif falling_edge(clk) then
		ram_nwe <= nwr_int;
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
				next_state <= rd1_h;
			elsif wr='1' then
				next_state <= wr1_h;
			end if;

		when rd1_h =>
			next_state <= rd2_h;

		when rd2_h =>
			next_state <= rd_idl;

		when rd_idl =>
			next_state <= rd1_l;

		when rd1_l =>
			next_state <= rd2_l;

		when rd2_l =>
			next_state <= idl;
			
		when wr1_h =>
			next_state <= wr2_h;

		when wr2_h =>
			next_state <= wr_idl;

		when wr_idl =>
			next_state <= wr1_l;

		when wr1_l =>
			next_state <= wr2_l;

		when wr2_l =>
			next_state <= idl;

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
		rd_data_ena_h <= '0';
		rd_data_ena_l <= '0';
		inc_addr <= '0';
		wr_low <= '0';
		nwr_int <= '1';

	elsif rising_edge(clk) then

		state <= next_state;
		dout_ena <= '0';
		ram_ncs <= '1';
		ram_noe <= '1';
		rd_data_ena_h <= '0';
		rd_data_ena_l <= '0';
		inc_addr <= '0';
		wr_low <= '0';
		nwr_int <= '1';

		case next_state is

			when idl =>

			-- the wait state
			when rd1_h =>
				ram_ncs <= '0';
				ram_noe <= '0';

			when rd2_h =>
				ram_ncs <= '0';
				ram_noe <= '0';
				rd_data_ena_h <= '1';
				
			when rd_idl =>
				ram_ncs <= '0';
				ram_noe <= '0';
				inc_addr <= '1';

			when rd1_l =>
				ram_ncs <= '0';
				ram_noe <= '0';

			when rd2_l =>
				ram_ncs <= '0';
				ram_noe <= '0';
				rd_data_ena_l <= '1';
				
			when wr1_h =>
				ram_ncs <= '0';
				dout_ena <= '1';
				nwr_int <= '0';

			when wr2_h =>
				ram_ncs <= '0';
				dout_ena <= '1';

			when wr_idl =>
				ram_ncs <= '1';
				dout_ena <= '1';
				inc_addr <= '1';
				wr_low <= '1';

			when wr1_l =>
				ram_ncs <= '0';
				dout_ena <= '1';
				nwr_int <= '0';

			when wr2_l =>
				ram_ncs <= '0';
				dout_ena <= '1';

		end case;
					
	end if;
end process;

--
-- wait_state processing
--
process(clk, reset)
begin
	if (reset='1') then
		cnt <= "00";
	elsif rising_edge(clk) then

		cnt <= "11";
		if next_state=idl then
			cnt <= "00";
		end if;

	end if;
end process;

end rtl;
