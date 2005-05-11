--
--	dummy PLL for the simulation
--

LIBRARY ieee;
USE ieee.std_logic_1164.all;

ENTITY pll IS
	generic (multiply_by : natural; divide_by : natural);
	PORT
	(
		inclk0		: IN STD_LOGIC  := '0';
		c0		: OUT STD_LOGIC 
	);
END pll;


ARCHITECTURE SYN OF pll IS

BEGIN

    assert multiply_by = 1 and divide_by = 1 
		report "PLL factors have to be 1 for dummy PLL" severity ERROR;
	c0 <= inclk0;

END SYN;
