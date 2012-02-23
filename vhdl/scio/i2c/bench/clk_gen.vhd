library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use ieee.std_logic_unsigned.all;


entity clk_gen is

	generic( period : time;
			 phase	: time); 
									   
	port( clk_o : out std_logic
	);
end entity clk_gen;


architecture clk_gen_arch of clk_gen is

signal clk_s : std_logic := '1';
	
begin

clk_o <= clk_s after phase;

CLOCK: process

begin
	
	wait for period/2;
	clk_s <= not clk_s;
	
end process CLOCK;
	
end architecture clk_gen_arch; 