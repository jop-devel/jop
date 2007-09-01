--
--	jop_config_de2.vhd
--
--	package for DE2 definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_config is

	-- constants for 50MHz input clock
	constant clk_freq : integer := 100000000;
	constant pll_mult : natural := 2;
	constant pll_div : natural := 1;

	-- constant for on-chip memory
	constant ram_width : integer := 8;	-- address bits of internal ram (sp,...)

end jop_config;

package body jop_config is

end jop_config;
