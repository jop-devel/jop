--
--	sc_mac16.vhd
--
--	A simple MAC unit with a SimpCon interface
--	
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--	resources on Cyclone
--
--		xx LCs, max xx MHz
--
--
--	2006-02-12	first version
--
--	todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity sc_mac16 is
generic (addr_bits : integer);

port (
	clk		: in std_logic;
	reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0)

);
end sc_mac16;

architecture rtl of sc_mac16 is

	signal opds			: std_logic_vector(31 downto 0);
	signal res			: unsigned(31 downto 0);
	signal mac			: unsigned(31 downto 0);

	type state_type		is (idle, mul, add);
	signal state 		: state_type;

begin

	rdy_cnt <= "00";	-- no wait states, we are hopefully fast enough

--
--	SimpCon read and write
--
process(clk, reset)

begin

	if (reset='1') then
		opds <= (others => '0');
		mac <= (others => '0');
		rd_data <= (others => '0');

	elsif rising_edge(clk) then

		if wr='1' then
			opds <= wr_data;
		end if;

		-- get MAC result and clear the accumulator
		if rd='1' then
			rd_data <= std_logic_vector(mac);
			mac <= (others => '0');
		end if;
	end if;

	case state is

		when idle =>
			if wr='1' then
				state <= mul;
			end if;

		when mul =>
			res <= unsigned(opds(31 downto 16)) * unsigned(opds(15 downto 0));
			state <= add;

		when add =>
			mac <= mac + res;
			state <= idle;
				
	end case;

end process;


end rtl;
