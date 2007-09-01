--
--	jop_config_100.vhd
--
--	package for 100MHz definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_config is

	-- constants for 20MHz input and 100MHz internal clock
	constant clk_freq : integer := 100000000;
	constant pll_mult : natural := 5;
	constant pll_div : natural := 1;

	-- constant for on-chip memory
	constant ram_width : integer := 8;	-- address bits of internal ram (sp,...)

end jop_config;

package body jop_config is

end jop_config;
