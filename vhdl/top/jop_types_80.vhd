--
--	jop_types_80.vhd
--
--	package for 80MHz definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_types is

	-- constants for 20MHz input and 80MHz internal clock
	constant clk_freq : integer := 80000000;
	constant pll_mult : natural := 4;
	constant pll_div : natural := 1;

	type io_ports is record
		l	: std_logic_vector(20 downto 1);
		r	: std_logic_vector(20 downto 1);
		t	: std_logic_vector(6 downto 1);
		b	: std_logic_vector(10 downto 1);
	end record;

end jop_types;

package body jop_types is

end jop_types;
