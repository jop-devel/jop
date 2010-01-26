library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity flags_ram is
	generic (
		way_bits		: integer
	);
	port (
		address		: in std_logic_vector(way_bits-1 downto 0);
		clock		: in std_logic;
		data		: in std_logic_vector(0 downto 0);
		wren		: in std_logic ;
		q		: out std_logic_vector(0 downto 0)
	);
end flags_ram;


architecture rtl of flags_ram is

signal flags		: std_logic_vector(2**way_bits-1 downto 0);
signal next_q		: std_logic;

begin

	sync: process (clock) is
	begin
	    if rising_edge(clock) then
	    	q(0) <= next_q;
	    	next_q <= flags(to_integer(unsigned(address)));
	    	if wren = '1' then	    		 
	    		flags(to_integer(unsigned(address))) <= data(0);
	    	end if; 
	    end if;
	end process sync;

end rtl;