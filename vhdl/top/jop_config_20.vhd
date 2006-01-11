--
--	jop_config_20.vhd
--
--	package for 20MHz definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_config is

	-- constants for 20MHz input and 20MHz internal clock
	constant clk_freq : integer := 20000000;
	constant pll_mult : natural := 1;
	constant pll_div : natural := 1;

end jop_config;

package body jop_config is

end jop_config;
