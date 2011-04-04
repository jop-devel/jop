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

use work.NoCTypes.ALL;


entity Receiver is
    Port ( Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
			  
			  -- my NoC id
			  slotID : in NoCAddr;
           nocIn : in  NoCPacket;
           nocOut : out  NoCPacket;
			  
			  -- interface with rcv FIFO
           rcvBufferFull : in  BOOLEAN;
           enq : out BOOLEAN;
           rcvData : out  STD_LOGIC_VECTOR (31 downto 0);
			  
			  isRcv : out BOOLEAN;
			  -- end of data seen
			  EoD : out BOOLEAN;
			  -- source address of the message
			  rcvSrc : out NoCAddr;
			  -- reset reception
			  ackEoD : in BOOLEAN
			  );
end Receiver;

architecture Behavioral of Receiver is

type RcvState is (RCVIdle, RCVRunning, RCVDone);

signal flagEoD, nxtflagEoD: BOOLEAN;
signal crts, nxts: RcvState; 

signal rcvSlot, nxtRcvSlot : NoCAddr;
signal aux_PisData: BOOLEAN;

-- counter, don't really need this.. since
-- if is EoD and bufferEmpty, we read all the words
-- signal rcvCount, nxtRcvCount : STD_LOGIC_VECTOR (31 downto 0);

begin

SYNC: process (Clk)
begin
	if (Clk'event and Clk = '1') then
      if (Rst = '1') then
 			crts <= RCVIdle;
         flagEoD <= False;
			rcvSlot <= (others => '0');
		else
			crts <= nxts;
			flagEoD <= nxtFlagEoD;
			rcvSlot <= nxtRcvSlot;
      end if;
   end if;	
end process;


-- this is the slot we need to receive from
--(rcvSlotID = nocIn.Src);

-- this is a data slot
aux_PisData <= (nocIn.pType = PTData) or (nocIn.pType = PTEoD);

COMB: process (crts, aux_PisData, nocIn, slotID, rcvBufferFull, flagEoD, rcvSlot, ackEoD)
begin
	nxts <= crts;
	enq <= False;
	rcvData <= (others => '0');
	nxtFlagEoD <= flagEoD;
	nocOut <= nocIn;	-- default
	nxtRcvSlot <= rcvSlot;
   case (crts) is 
      when RCVIdle =>
			-- check for new messages in ANY slot, except mine
			if(not flagEoD and nocIn.Dst = slotID and (not (nocIn.Src = slotID)) and aux_PisData) then
				-- should follow this slot
				-- (if the first word is not received, then any slot is ok again)
				nxtRcvSlot <= nocIn.Src;
				-- if we can store the message
				if(not rcvBufferFull) then
					-- store the word
					enq <= True;
					rcvData <= nocIn.Load;
					-- need to start a new reception
					-- only if it's EoD directly
					if nocIn.pType = PTData then 
						nxts <= RCVRunning;
					else
						nxts <= RCVDone; -- when nocIn.pType = PTEoD
					end if;
					-- send ack!
					nocOut.pType <= PTAck;
					-- update flagEoD if needed
					nxtFlagEoD <= (nocIn.pType = PTEoD);
				end if;	
			end if;
      when RCVRunning =>
         -- follow my slot now
			if(nocIn.Src = rcvSlot and aux_PisData) then
			-- do the receive
				if(not rcvBufferFull) then
					-- store the word
					enq <= True;
					rcvData <= nocIn.Load;
					-- is it the end of reception?
					if nocIn.pType = PTEoD then 
						nxts <= RCVDone;
						nxtFlagEoD <= True;
					end if;
					-- send ack!
					nocOut.pType <= PTAck;
				end if;	
			end if;
      when RCVDone =>
         if(ackEoD) then
				nxtFlagEoD <= False;
				nxts <= RCVIdle;
				-- might need to reset the FIFO
			end if;
      when others =>
			-- unknown state
   end case;

end process;

EoD <= flagEoD;
rcvSrc <= rcvSlot;	
isRcv <= not (crts = RCVIdle);

end Behavioral;

