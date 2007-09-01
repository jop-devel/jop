--
--	jop_config_s3k.vhd
--
--	package for S3K definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_config is

	constant clk_freq : integer := 50000000;

	-- constant for on-chip memory
	constant ram_width : integer := 8;	-- address bits of internal ram (sp,...)

end jop_config;

package body jop_config is

end jop_config;
