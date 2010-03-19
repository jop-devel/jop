--
--
--  This file is a part of an evaluation of CSP with a JOP CMP
--
--  Copyright (C) 2010, Flavius Gruian (Flavius.Gruian@cs.lth.se)
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

LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.std_logic_unsigned.all;
USE ieee.numeric_std.ALL;

use work.NoCTypes.ALL;
 
ENTITY TestReceiver IS
END TestReceiver;
 
ARCHITECTURE behavior OF TestReceiver IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT Receiver
    PORT(
         Clk : IN  std_logic;
         Rst : IN  std_logic;
         slotID : IN  NoCAddr;
         nocIn : IN  NoCPacket;
         nocOut : OUT  NoCPacket;
         rcvBufferFull : IN  BOOLEAN;
         enq : OUT  BOOLEAN;
         rcvData : OUT  std_logic_vector(31 downto 0);
         EoD : OUT  BOOLEAN;
         rcvSrc : OUT  NoCAddr;
         ackEoD : IN BOOLEAN
        );
    END COMPONENT;
    

   --Inputs
   signal Clk : std_logic := '0';
   signal Rst : std_logic := '0';
   signal slotID : NoCAddr := "001";
   signal nocIn : NoCPacket := (Src => (others => '0'), Dst => (others => '0'), pType => PTNil, Load => (others =>'0'));
   signal rcvBufferFull : BOOLEAN := False;
   signal ackEoD : BOOLEAN := False;

 	--Outputs
   signal nocOut : NoCPacket;
   signal enq : BOOLEAN;
   signal rcvData : std_logic_vector(31 downto 0);
   signal EoD : BOOLEAN;
   signal rcvSrc : NoCAddr;
   constant Clk_period : TIME := 10ns;
 

BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: Receiver PORT MAP (
          Clk => Clk,
          Rst => Rst,
          slotID => slotID,
          nocIn => nocIn,
          nocOut => nocOut,
          rcvBufferFull => rcvBufferFull,
          enq => enq,
          rcvData => rcvData,
          EoD => EoD,
          rcvSrc => rcvSrc,
          ackEoD => ackEoD
        );
 
   -- No clocks detected in port list. Replace <clock> below with 
   -- appropriate port name 
 
    clk_process :process
   begin
		Clk <= '0';
		wait for Clk_period/2;
		Clk <= '1';
		wait for Clk_period/2;
   end process;
 

   -- Stimulus process
   stim_proc: process
   begin		
      -- hold reset state for 100ms.
		Rst <= '1';
      wait for 20ns;
		Rst <= '0';	

      wait for  Clk_period*10;

      -- insert stimulus here 
		nocIn <= (Src => "011", Dst => "001", pType => PTData, Load => X"CAFEBABE"),
					(Src => (others => '0'), Dst => (others => '0'), pType => PTNil, Load => (others =>'0')) after Clk_period,
					(Src => "001", Dst => "001", pType => PTEoD, Load => X"BEBECACA") after Clk_period*3,
					(Src => "011", Dst => "001", pType => PTEoD, Load => X"DEADBEEF") after Clk_period*4,
					(Src => (others => '0'), Dst => (others => '0'), pType => PTNil, Load => (others =>'0')) after Clk_period*5;
		ackEoD <= True after Clk_period*6,
					 False after Clk_period*7;
		
      wait;
   end process;

END;
