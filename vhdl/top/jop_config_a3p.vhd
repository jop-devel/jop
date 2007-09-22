--
--	jop_config_a3p.vhd
--
--	package for Actel's A3P board
--

library ieee;
use ieee.std_logic_1164.all;

package jop_config is

	constant clk_freq : integer := 40000000;

	-- constant for on-chip memory
	constant ram_width : integer := 8;	-- address bits of internal ram (sp,...)

end jop_config;

package body jop_config is

end jop_config;
