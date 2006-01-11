--
--	sim_jop_config_100.vhd
--
--	package for 100MHz definitions
--	for ModelSim
--

library ieee;
use ieee.std_logic_1164.all;

package jop_config is

	-- constants for 100MHz testbench clock
	constant clk_freq : integer := 100000000;
	constant pll_mult : natural := 1;
	constant pll_div : natural := 1;

end jop_config;

package body jop_config is

end jop_config;
