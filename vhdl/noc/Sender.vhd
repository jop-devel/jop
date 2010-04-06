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


entity Sender is
    Port ( Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
			  
			  -- my NoC id
			  slotID : in NoCAddr;
           nocIn : in  NoCPacket;
           nocOut : out  NoCPacket;
			  
			  -- interface with rcv FIFO
           sndBufferEmpty : in  BOOLEAN;
           deq : out BOOLEAN;
           sndData : in STD_LOGIC_VECTOR (31 downto 0);

			  -- destination address of the message
			  sndDst : in NoCAddr;
			  setSndDst : in BOOLEAN;
			  -- how many words to send
			  sndCnt : in STD_LOGIC_VECTOR (31 downto 0);
			  -- also causes start sending!
			  setSndCnt : in BOOLEAN;
			  --
			  isSending : out BOOLEAN
			  );
end Sender;

architecture Behavioral of Sender is

type SndState is (SNDIdle, SNDFirstWord, SNDRestWords);

signal crts, nxts : SndState;
signal sndCount, nxtSndCount : STD_LOGIC_VECTOR (31 downto 0);
signal sndDest, nxtSndDest : NoCAddr;

begin

SYNC: process (Clk)
begin
	if (Clk'event and Clk = '1') then
      if (Rst = '1') then
 			crts <= SNDIdle;
			sndCount <= (others => '0');
			sndDest <= (others => '0');			
		else
			crts <= nxts;
			sndCount <= nxtSndCount;
			sndDest <= nxtSndDest;
      end if;
   end if;	
end process;



COMB: process (crts,sndCount,sndDest,nocIn,sndBufferEmpty,setSndDst,setSndCnt,sndDst,sndCnt,slotID,sndData)
begin
 nxts <= crts;
 nxtSndCount <= sndCount;
 nxtSndDest	<=	sndDest;
 deq <= False;
 nocOut <= nocIn;
 
 case(crts) is
		when SNDIdle =>
			if(setSndDst) then
				nxtSndDest <= sndDst;
			end if;
			if(setSndCnt) then
				-- save dest, count and go to the next state
				nxts <= SNDFirstWord;
				nxtSndCount <= sndCnt;
			end if;
		when SNDFirstWord =>
			-- only do something in my own slot
			if(nocIn.Src = slotID) then
				-- send stuff if there is any in the fifo
				if(not sndBufferEmpty) then
					-- dequeue
					deq <= True;
					-- send the data
					nocOut.Load <= sndData;
					nocOut.Dst <= sndDest;
					-- this is data or EoD packet
					if(sndCount = 1) then
						nocOut.pType <= PTEoD;						
					else
						nocOut.pType <= PTData;
					end if;
					-- update count
					nxtSndCount <= sndCount - 1;
					-- send the rest of the message
					nxts <= SNDRestWords;
				end if;
			end if;
		when SNDRestWords => -- here i must wait for ack, and send if possible
			-- only do something in my own slot
			if(nocIn.Src = slotID) then
			
				if(sndCount = 0) then -- no more stuff to send, just ack needed
					if(nocIn.pType = PTAck) then
						-- got it!
						nxts <= SNDIdle;
					end if;
				else
					-- still words to send
					if(not sndBufferEmpty and nocIn.pType = PTAck) then
						-- dequeue
						deq <= True;
						-- send the data
						nocOut.Load <= sndData;
						nocOut.Dst <= sndDest;
						-- this is data or EoD packet
						if(sndCount = 1) then
							nocOut.pType <= PTEoD;						
						else
							nocOut.pType <= PTData;
						end if;
						-- update count
						nxtSndCount <= sndCount - 1;						
					end if;
				end if;

			end if;
      when others =>
			-- unknown state
 end case;

end process;


isSending <= not (crts = SNDIdle);

end Behavioral;

