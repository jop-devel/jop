--
--	blink.vhd
--
--	simple blinking watchdog led.
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity blink is

port (
	clk				: in std_logic;
	wd				: out std_logic;
--
--	dummy input pins for EP1C6 on board with EP1C12 pinout
--	EP1C12 has additional GND and VCCINT pins.
--
	dummy_gnd		: in std_logic_vector(5 downto 0);
	dummy_vccint	: in std_logic_vector(5 downto 0)
);
end blink;

architecture rtl of blink is

	signal cnt		: unsigned(24 downto 0);

begin

	process(clk)
	begin

		if rising_edge(clk) then
			cnt <= cnt + 1;
		end if;

	end process;

	wd <= std_logic(cnt(24));

end rtl;
