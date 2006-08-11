--
--	jop_types.vhd
--
--	package type definitions definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_types is

	type io_ports is record
		l	: std_logic_vector(20 downto 1);
		r	: std_logic_vector(20 downto 1);
		t	: std_logic_vector(6 downto 1);
		b	: std_logic_vector(10 downto 1);
	end record;

	type exception_type is record
		spov	: std_logic;
	end record;

end jop_types;

package body jop_types is

end jop_types;
