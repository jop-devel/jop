--
--	jop_types.vhd
--
--	package type definitions definitions
--

library ieee;
use ieee.std_logic_1164.all;

package jop_types is

	-- not usefull as it's inout
	type io_port_type is record
		l	: std_logic_vector(20 downto 1);
		r	: std_logic_vector(20 downto 1);
		t	: std_logic_vector(6 downto 1);
		b	: std_logic_vector(10 downto 1);
	end record;

	type ser_in_type is record
		rxd			: std_logic;
		ncts		: std_logic;
	end record;
	type ser_out_type is record
		txd			: std_logic;
		nrts		: std_logic;
	end record;

	type exception_type is record
		spov	: std_logic;
	end record;

	type irq_in_type is record
		irq			: std_logic;	-- interrupt request (positiv edge sensitive)
		irq_ena		: std_logic;	-- interrupt enable (pendig int is fired on ena)

		exc_int		: std_logic;	-- exception interrupt
	end record;

end jop_types;
