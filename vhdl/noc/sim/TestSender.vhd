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

 
ENTITY TestSender IS
END TestSender;
 
ARCHITECTURE behavior OF TestSender IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT Sender
    PORT(
         Clk : IN  std_logic;
         Rst : IN  std_logic;
         slotID : IN  NoCAddr;
         nocIn : IN  NoCPacket;
         nocOut : OUT  NoCPacket;
         sndBufferEmpty : IN  BOOLEAN;
         deq : OUT  BOOLEAN;
         sndData : IN  std_logic_vector(31 downto 0);
         sndDst : IN  NoCAddr;
         setSndDst : IN  BOOLEAN;
         sndCnt : IN  std_logic_vector(31 downto 0);
         setSndCnt : IN  BOOLEAN;
         isSending : OUT  BOOLEAN
        );
    END COMPONENT;
    

   --Inputs
   signal Clk : std_logic := '0';
   signal Rst : std_logic := '0';
   signal slotID : NoCAddr := "001";
   signal nocIn : NoCPacket := (Src => "001", Dst => (others => '0'), pType => PTNil, Load => (others =>'0'));
   signal sndBufferEmpty : BOOLEAN := True;
   signal sndData : std_logic_vector(31 downto 0) := (others => '0');
   signal sndDst : NoCAddr := (others => '0');
   signal setSndDst : BOOLEAN := False;
   signal sndCnt : std_logic_vector(31 downto 0) := (others => '0');
   signal setSndCnt : BOOLEAN := False;

 	--Outputs
   signal nocOut : NoCPacket;
   signal deq : BOOLEAN;
   signal isSending :BOOLEAN;
   constant Clk_period:TIME := 1ns;
 
BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: Sender PORT MAP (
          Clk => Clk,
          Rst => Rst,
          slotID => slotID,
          nocIn => nocIn,
          nocOut => nocOut,
          sndBufferEmpty => sndBufferEmpty,
          deq => deq,
          sndData => sndData,
          sndDst => sndDst,
          setSndDst => setSndDst,
          sndCnt => sndCnt,
          setSndCnt => setSndCnt,
          isSending => isSending
        );
 
   -- No clocks detected in port list. Replace <clock> below with 
   -- appropriate port name 
 
 
   Clk_process :process
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
		sndDst <= "011";
		setSndDst <= True, False after Clk_period;
	
		sndCnt <= X"00000002";
		setSndCnt <= True after Clk_period, False after Clk_period*2;
	
		sndBufferEmpty <= False after Clk_period*5, True after Clk_period*6, False after Clk_period*7, True after Clk_period*8;
		sndData <= X"CAFEBABE", X"DEADBEEF" after Clk_period*6;
		
		nocIn <= (Src => "001", Dst => (others => '0'), pType => PTAck, Load => (others =>'0')) after Clk_period*7,
		         (Src => "001", Dst => (others => '0'), pType => PTNil, Load => (others =>'0')) after Clk_period*9;
      wait;
   end process;

END;
