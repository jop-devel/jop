library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use ieee.std_logic_unsigned.all;

entity rst_gen is

	generic( delay : time);
	port( rst_o : out std_logic
	);
end entity rst_gen;

architecture RTL of rst_gen is

signal rst_s: std_logic := '0';

begin

rst_o <= rst_s;

RESET: process

variable started: boolean := false;

begin
	
	if (started = false) then
		wait for delay;
		rst_s <= '1';
		started := true;
	else
		rst_s <= '0';
	end if;
	
	wait for 2*delay;
	
end process RESET;


end architecture RTL;
