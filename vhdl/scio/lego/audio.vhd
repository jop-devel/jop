--
--	audio.vhd
--
--	PCM audio playback support
--  Code inspired by Jean P. Nicolle.
--  http://www.fpga4fun.com/PWM_DAC.html
--
--
--
--	todo:
--
--
--  2007-05-15  created
--


library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity audio is
	generic (
		input_width: integer);
	port (
   		clk: in std_logic;
		reset: in std_logic;
		input: in std_logic_vector(input_width-1 downto 0);
		output: out std_logic);
end audio;

architecture rtl of audio is

	signal counter: std_logic_vector(input_width-1 downto 0);
	
begin
	sync: process(clk, reset)
	begin
		if reset = '1' then
			counter <= (others => '0');
		else		
	    	if rising_edge(clk) then      
				counter <= counter + 1;
			end if;
		end if;
	end process;

	output <= '1' when counter <= input else '0';
end rtl;
