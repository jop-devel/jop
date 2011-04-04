--
--
--  This file is a part of an evaluation of CSP with a JOP CMP
--
--  Copyright (C) 2010, Flavius Gruian )Flavius.Gruian@cs.lth.se)
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

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;


entity SizedFIFOF is
	 Generic (
		SIZE: integer := 4;
		ADDRBITS: integer := 2;
		DATABITS: integer := 32
	 );
    Port ( Clk : in  STD_LOGIC;
			  Rst : in STD_LOGIC;
			  DataIn : in  STD_LOGIC_VECTOR (DATABITS-1 downto 0);
           Enq : in  STD_LOGIC;
           First : out  STD_LOGIC_VECTOR (DATABITS-1 downto 0);
           Deq : in  STD_LOGIC;
           Full : out  STD_LOGIC;
           Empty : out  STD_LOGIC);

end SizedFIFOF;

architecture Behavioral of SizedFIFOF is

type RegFileType is array (SIZE-1 downto 0) of STD_LOGIC_VECTOR (DATABITS-1 downto 0);
signal Stack: RegFileType;
signal PushAdr: STD_LOGIC_VECTOR(ADDRBITS-1 downto 0);
signal PopAdr: STD_LOGIC_VECTOR(ADDRBITS-1 downto 0);

-- how many do i have in the queue?
signal count: STD_LOGIC_VECTOR(ADDRBITS downto 0);

signal isFull: STD_LOGIC;
signal isEmpty: STD_LOGIC;
signal canEnq: STD_LOGIC;
signal canDeq: STD_LOGIC;

begin

assert 2**ADDRBITS = SIZE report "Size must be 2**address bits in SizedFIFOF!";

isFull <= '1' when count = CONV_STD_LOGIC_VECTOR(SIZE,ADDRBITS+1) else '0';
isEmpty <= '1' when count = CONV_STD_LOGIC_VECTOR(0,ADDRBITS+1) else '0';

-- simultaneous push pop should work, even if the
-- stack is empty or full

canEnq <= '1' when (Enq = '1' and (isFull = '0' or Deq = '1')) else '0';
canDeq <= '1' when (Deq = '1' and (isEmpty = '0' or Enq = '1')) else '0';

GetData: process (Clk)
begin
   if (Clk'event and Clk = '1') then
		-- no check for Full here
      if (canEnq = '1') then
         Stack(conv_integer(PushAdr)) <= DataIn;
      end if;
   end if;
end process;

AdrUpd: process (Clk)
begin
	  if (Clk'event and Clk = '1') then
      if (Rst = '1') then
			count <= (others => '0');
         PushAdr <= (others => '0');
			PopAdr <= (others => '0');
		else
			-- no check for empty/full yet
			if(canEnq = '1') then
				PushAdr <= PushAdr + 1;
			end if;
			if(canDeq = '1') then
				PopAdr <= PopAdr + 1;
			end if;
			
			-- count update
			if (canEnq = '1' and Deq = '0') then
				count <= count + 1;
			elsif (canDeq = '1' and Enq = '0') then
				count <= count - 1;
			end if;
      end if;
   end if;

end process;

First <= Stack(conv_integer(PopAdr));

Empty <= isEmpty;
Full <= isFull;
	
end Behavioral;

