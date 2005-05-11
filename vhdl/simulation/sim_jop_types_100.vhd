--
--	sim_jop_types_100.vhd
--
--	package for 100MHz definitions
--	for ModelSim
--


package jop_types is

	-- constants for 100MHz testbench clock
	constant clk_freq : integer := 100000000;
	constant pll_mult : natural := 1;
	constant pll_div : natural := 1;

end jop_types;

package body jop_types is

end jop_types;
