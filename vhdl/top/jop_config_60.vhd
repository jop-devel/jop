--
--	jop_config_60.vhd
--
--	package for 60MHz definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_config is

	-- constants for 20MHz input and 60MHz internal clock
	constant clk_freq : integer := 60000000;
	constant pll_mult : natural := 3;
	constant pll_div : natural := 1;

end jop_config;

package body jop_config is

end jop_config;
