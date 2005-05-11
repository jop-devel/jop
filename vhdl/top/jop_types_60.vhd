--
--	jop_types_60.vhd
--
--	package for 60MHz definitions
--


package jop_types is

	-- constants for 20MHz input and 60MHz internal clock
	constant clk_freq : integer := 60000000;
	constant pll_mult : natural := 3;
	constant pll_div : natural := 1;

end jop_types;

package body jop_types is

end jop_types;
