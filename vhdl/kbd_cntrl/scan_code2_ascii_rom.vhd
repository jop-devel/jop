--
--
--  This file is a part of PS2 Keyboard Controller Module 
--
--  Copyright (C) 2009, Matthias Wenzl (e0425388@student.tuwien.ac.at)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--

--this file provides the scan code to ascii lookup tables

library ieee;
use ieee.std_logic_1164.all;

entity rom_scancode2_ascii is
 generic (width :integer := 16; addr_width :integer := 24);
 port(
	clk	:in std_logic;
	address	:in std_logic_vector(addr_width-1 downto 0);
	q	:out std_logic_vector(width-1 downto 0)
);

end rom_scancode2_ascii;



architecture rtl of rom_scancode2_ascii is


	signal areg	:std_logic_vector(addr_width-1 downto 0);
	signal data	:std_logic_vector(width-1 downto 0);



begin

process(clk) begin

	if rising_edge(clk) then
		areg <= address;
	end if;

end process;

	 q <= data;


process(areg) begin

	case areg(8-1 downto 0) is
--	 when X"76" => data <= X"801b"; --ESC
 --0--  
--	 when X"05" => data <= X"8001"; --f1
 --1--  
--	 when X"06" => data <= X"8002"; --f2
 --2--  
--	 when X"04" => data <= X"8003"; --f3
 --3--  
--	 when X"0c" => data <= X"8004"; --f4
 --4--  
--	 when X"03" => data <= X"8005"; --f5
 --5--  
--	 when X"0b" => data <= X"8006"; --f6
 --6--  
--	 when X"83" => data <= X"8007"; --f7
 --7--  
--	 when X"0a" => data <= X"8008"; --f8
 --8--  
--	 when X"01" => data <= X"8009"; --f9
 --9--  
--	 when X"09" => data <= X"800a"; --f10
 --10--  
--	 when X"78" => data <= X"800b"; --f11
 --11--  
--	 when X"07" => data <= X"800c"; --f12
 --12--  
 	 when X"0e" => data <= X"0060"; --`
 
 --14--  
	 when X"16" => data <= X"0031"; --1
 --16--  
	 when X"1e" => data <= X"0032"; --2
 
 --18--  
	 when X"26" => data <= X"0033"; --3
 
 --20--  
	 when X"25" => data <= X"0034"; --4

 --22--  
	 when X"2e" => data <= X"0035"; --5
 
 --24--  
	 when X"36" => data <= X"0036"; --6
 
 --26--  
	 when X"3d" => data <= X"0037"; --7
 
 --28--  
	 when X"3e" => data <= X"0038"; --8
 
 --30--  
	 when X"46" => data <= X"0039"; --9
 
 --32--  
	 when X"45" => data <= X"0030"; --0
 
 --34--  
	 when X"4e" => data <= X"002d"; -- -
 
 --36--  
	 when X"55" => data <= X"002b"; --+
 
 --38--  
	 when X"5d" => data <= X"007c"; --|
 
 --40--  
--	 when X"66" => data <= X"0008"; --BACKSPACE 
 --41--  
	 when X"0d" => data <= X"0009"; --TAB
 --42--  
	 when X"1c" => data <= X"0061"; --a 
 
 --44--  
	 when X"32" => data <= X"0062"; --b
 
 --46--  
	 when X"21" => data <= X"0063"; --c
 
 --48--  
	 when X"23" => data <= X"0064"; --d
 
 --50--  
	 when X"24" => data <= X"0065"; --e

 --52--  
	 when X"2b" => data <= X"0066"; --f
 
 --54--  
	 when X"34" => data <= X"0067"; --g
 
 --56--  
	 when X"33" => data <= X"0068"; --h
 
 --58--  
	 when X"43" => data <= X"0069"; --i
 
 --60--  
	 when X"3b" => data <= X"006a"; --j
 
 --62--  
	 when X"42" => data <= X"006b"; --k
 
 --64--  
	 when X"4b" => data <= X"006c"; --l
 
 --66--  
	 when X"3a" => data <= X"006d"; --m
 
 --68--  
	 when X"31" => data <= X"006e"; --n
 
 --70--  
	 when X"44" => data <= X"006f"; --o
 
 --72--  
	 when X"4d" => data <= X"0070"; --p
 
 --74--  
 	 when X"15" => data <= X"0071"; --q
 
 --76--  
	 when X"2d" => data <= X"0072"; --r
 
 --78--  
	 when X"1b" => data <= X"0073"; --s
 
 --80--  
	 when X"2c" => data <= X"0074"; --t
 
 --82--  
	 when X"3c" => data <= X"0075"; --u
 
 --84--  
	 when X"2a" => data <= X"0076"; --v
 
 --86--  
	 when X"1d" => data <= X"0077"; --w
 
 --88--  
	 when X"22" => data <= X"0078"; --x
 
 --90--  
	 when X"35" => data <= X"0079"; --y
 
 --92--  
	 when X"1a" => data <= X"007a"; --z
 
 --94--  
	 when X"54" => data <= X"005b"; --[
 
 --96--  
	 when X"5b" => data <= X"005d"; --]
 
 --98--  
	 when X"5a" => data <= X"000d"; --ENTER
 --99--  
	 when X"4c" => data <= X"003b"; --;
 
 --101--  
	 when X"52" => data <= X"0027"; --'
 
 --103--  
  when X"41" => data <= X"002c"; --,
	 
 
 --105--  
	 when X"49" => data <= X"002e"; --.
 
 --107--  
	 when X"4a" => data <= X"002f"; --/
 
 --109--  
--	 when X"59" => data <= X"8010"; --RSHIFT 
 --110--  
--	 when X"14" => data <= X"8011"; --LCTRL
 --111--  
	-- when X"11" => data <= X"8012"; --ALT 
 --112--  
	 when X"29" => data <= X"0020"; --SPACE
  --115--  
--	 when X"7e" => data <= X"8016"; --SCRL 
  --120--  
--	 when X"77" => data <= X"801a"; --NUM LOCK
 
 --122--  
	 when X"7c" => data <= X"002a"; --*
 --123-- 
	 when X"7b" => data <= X"002d"; -- -
 --124--  
	 when X"69" => data <= X"0031"; --1
 --125--  
	 when X"72" => data <= X"0032"; --2
 --126--  
	 when X"7a" => data <= X"0033"; --3
 --127--  
	 when X"6b" => data <= X"0034"; --4
 --128--  
	 when X"73" => data <= X"0035"; --5
 --129--  
	 when X"74" => data <= X"0036"; --6
 --130--  
	 when X"6c" => data <= X"0037"; --7
 --131--  
	 when X"75" => data <= X"0038"; --8
 --132--  
	 when X"7d" => data <= X"0039"; --9
 --133--  
	 when X"70" => data <= X"0030"; --0
 --134--  
	 when X"71" => data <= X"002e"; -- .
 --135--  
	 when X"79" => data <= X"002b"; --+
 --137--  
	 when others => data <= (others => '0');
	end case;
end process;



end architecture rtl;

---------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;

entity rom_scancode2_ascii_shift is
 generic (width :integer := 16; addr_width :integer := 24);
 port(
	clk	:in std_logic;
	address	:in std_logic_vector(addr_width-1 downto 0);
	q	:out std_logic_vector(width-1 downto 0)
);

end rom_scancode2_ascii_shift;

architecture rtl of rom_scancode2_ascii_shift is



	signal areg	:std_logic_vector(addr_width-1 downto 0);
	signal data	:std_logic_vector(width-1 downto 0);



begin

process(clk) begin

	if rising_edge(clk) then
		areg <= address;
	end if;
end process;

	 q <= data;



process(areg) begin

	case areg(8-1 downto 0) is
	
 --13--  
  when X"0e" => data <= X"007e"; --~
	 
 
 --15--  
	 when X"16" => data <= X"0021"; --!
 
 --17--  
	 when X"1e" => data <= X"0040"; --@
 
 --19--  
	 when X"26" => data <= X"0023"; --#
 
 --21--  
	 when X"25" => data <= X"0024"; --$
 
 --23--  
	 when X"2e" => data <= X"0024"; --%
 
 --25--  
	 when X"36" => data <= X"005e"; --^
 
 --27--  
	 when X"3d" => data <= X"0026"; --&
 
 --29--  
	 when X"3e" => data <= X"002a"; --*
 
 --31--  
	 when X"46" => data <= X"0028"; --(
 
 --33--  
	 when X"45" => data <= X"0029"; --)
 
 --35--  
	 when X"4e" => data <= X"005f"; --_
 
 --37--  
	 when X"55" => data <= X"003d"; --=
 
 --39--  
	 when X"5d" => data <= X"005c"; --\
 
 --43--  
	 when X"1c" => data <= X"0041"; --A
 
 --45--  
	 when X"32" => data <= X"0042"; --B
 
 --47--  
	 when X"21" => data <= X"0043"; --C
 
 --49--  
	 when X"23" => data <= X"0044"; --D
 
 --51--  
	 when X"24" => data <= X"0045"; --E
 
 --53--  
	 when X"2b" => data <= X"0046"; --F
 
 --55--  
	 when X"34" => data <= X"0047"; --G
 
 --57--  
	 when X"33" => data <= X"0048"; --H
 
 --59--  
	 when X"43" => data <= X"0049"; --I
 
 --61--  
 	 when X"3b" => data <= X"004a"; --J
 
 --63--  
	 when X"42" => data <= X"004b"; --K
 
 --65--  
	 when X"4b" => data <= X"004c"; --L
 
 --67--  
	 when X"3a" => data <= X"004d"; --M
 
 --69--  
	 when X"31" => data <= X"004e"; --N
 
 --71--  
	 when X"44" => data <= X"004f"; --O
 
 --73--  
	 when X"4d" => data <= X"0050"; --P
 
 --75--  
	 when X"15" => data <= X"0051"; --Q
 
 --77--  
	 when X"2d" => data <= X"0052"; --R
 
 --79--  
	 when X"1b" => data <= X"0053"; --S
 
 --81--  
	 when X"2c" => data <= X"0054"; --T
 
 --83--  
	 when X"3c" => data <= X"0055"; --U
 
 --85--  
	 when X"2a" => data <= X"0056"; --V
 
 --87--  
	 when X"1d" => data <= X"0057"; --W
 
 --89--  
	 when X"22" => data <= X"0058"; --X
 
 --91--  
	 when X"35" => data <= X"0059"; --Y
 
 --93--  
	 when X"1a" => data <= X"005a"; --Z
 
 --95--  
	 when X"54" => data <= X"007b"; --{
 
 --97--  
	 when X"5b" => data <= X"007d"; --}
 
 
 --100--  
	 
  when X"4c" => data <= X"003a"; --:
 --102--  
  when X"52" => data <= X"0022"; --"
	 
 
 --104--  
 when X"41" => data <= X"003c"; --<
	
 
 --106--  
  when X"49" => data <= X"003e"; -->
	 
 --108--  
 when X"4a" => data <= X"003f"; --?
	   
	 --when X"5a" => data <= X"000d"; --ENTER
 
 --137--  
	 when others => data <= (others => '0');
	end case;
end process;


end architecture rtl;

