--
--	cyc_conf_init.vhd
--
--	dummy version of cyc_conf_init with all address pins tristatet.
--	
--	resources on MAX7064
--
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.std_logic_unsigned.all;				-- this is not standard

entity cyc_conf_init is

port (
	clk			: in std_logic;							-- 20 MHz ?

	a			: out std_logic_vector(17 downto 0);	-- ROM adr
	d			: in std_logic_vector(7 downto 0);		-- ROM data

	nconfig		: out std_logic;						-- Cyclone nConfig
	conf_done	: in std_logic;							-- Cyclone conf_done

	dclk		: out std_logic;						-- Cyclone dclk
	data		: out std_logic;						-- Cyclone serial data

	nreset		: in std_logic							-- reset from watchdog
);
end cyc_conf_init ;

architecture rtl of cyc_conf_init is

begin

	a <= (others => 'Z');
	nconfig <= '1';
	data <= '0';
	dclk <= '0';

end rtl;
