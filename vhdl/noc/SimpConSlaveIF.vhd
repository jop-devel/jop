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

entity SimpConSlaveIF is
	 Generic (
			  myAddr : NoCAddr := (others => '0')
	 );
    Port ( Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
			  -- SimpCon Signals
           Addr : in  STD_LOGIC_VECTOR (1 downto 0);
           wr : in  STD_LOGIC;
           wr_data : in  STD_LOGIC_VECTOR (31 downto 0);
           rd : in  STD_LOGIC;
           rd_data : out  STD_LOGIC_VECTOR (31 downto 0);
           rdy_cnt : out  STD_LOGIC_VECTOR (1 downto 0);
			  -- towards the rest of the system
			  
			  -- from the Receiver part, and rcvFIFO
			  isRcv : in STD_LOGIC;
			  isRcvBufferFull : in STD_LOGIC; 
			  isRcvBufferEmpty : in STD_LOGIC;
			  isEoD : in STD_LOGIC;
			  rcvSrc : in STD_LOGIC_VECTOR (NOCADDRBITS-1 downto 0);
           deq : out STD_LOGIC;
           rcvFirst : in STD_LOGIC_VECTOR (31 downto 0);
			  resetRcv : out STD_LOGIC;
			  
			  isSndBufferFull : in STD_LOGIC; 
			  isSndBufferEmpty : in STD_LOGIC;
			  isSnd : in STD_LOGIC;
			  setSndCount : out STD_LOGIC;
			  sndCount : out STD_LOGIC_VECTOR (31 downto 0);
			  setSndDst : out STD_LOGIC;
			  sndDst : out STD_LOGIC_VECTOR (NOCADDRBITS-1 downto 0);
			  enq : out STD_LOGIC;
			  sndData : out STD_LOGIC_VECTOR (31 downto 0)
			);
end SimpConSlaveIF;

architecture Behavioral of SimpConSlaveIF is

signal ird, iwr : STD_LOGIC;
signal rdy, nxtrdy, iAddr : STD_LOGIC_VECTOR (1 downto 0);
signal rd_d, nxt_rd_d : STD_LOGIC_VECTOR(31 downto 0);
signal iwr_data : STD_LOGIC_VECTOR(31 downto 0);


begin

rdy_cnt <= rdy;
rd_data <= rd_d;

SYNC: process (Clk)
begin
	if (Clk'event and Clk = '1') then
      if (Rst = '1') then
 			rdy <= "00";
			rd_d <= X"00000000";
			ird <= '0';
			iwr <= '0';
			iwr_data <= X"00000000";
		else
			rdy <= nxtrdy;
			rd_d <= nxt_rd_d;
			if(rdy = "00") then -- ready to serve a request
				ird <= rd;
				iwr <= wr;
				if(wr = '1') then	
					iwr_data <= wr_data;
				end if;
				-- something to do
				if(rd = '1' or wr = '1') then
					iAddr <= Addr;
					rdy <= "11";
				end if;
			end if;
      end if;
   end if;	
end process;


BUSREAD:process(ird,iAddr,isSnd,isRcv,isEoD,
			isRcvBufferFull,isRcvBufferEmpty,isSndBufferFull,isSndBufferEmpty,rcvSrc,rdy,rd_d,rcvFirst,
			iwr_data, iwr)
variable flags,onebyte: STD_LOGIC_VECTOR(7 downto 0) := (others => '0');
begin

nxtrdy <= rdy;
nxt_rd_d <= rd_d;
deq <= '0';
enq <= '0';
sndCount <= iwr_data;
setSndCount <= '0';
sndData <= iwr_data;
sndDst <= iwr_data(NOCADDRBITS-1 downto 0);
setSndDst <= '0';
resetRcv <= '0';

if(not rdy = "00") then

if(ird = '1') then
	-- read registers
	case(iAddr) is
		when "00" =>
			-- read Status
			-- Byte0 : NoC address
			-- Byte1 : flags
			flags(0) := isSnd or isRcv;
			flags(1) := isSnd;
			flags(2) := isRcv;
			flags(3) := isEoD;
			flags(4) := isSndBufferEmpty;
			flags(5) := isSndBufferFull;
			flags(6) := isRcvBufferEmpty;
			flags(7) := isRcvBufferFull;
			onebyte := (others => '0');
			onebyte(NOCADDRBITS-1 downto 0) := myAddr;
			nxt_rd_d <= X"0000" & flags & onebyte;
			nxtrdy <= "00"; -- done
		when "01" => 
			-- read Count, not implemented anymore
			-- counting is done in hw
			nxtrdy <= "00";
		when "10" =>
			-- read Source
			onebyte := (others => '0');
			onebyte(NOCADDRBITS-1 downto 0) := rcvSrc;
			nxt_rd_d <= X"000000" & onebyte;
			nxtrdy <= "00";
		when "11" =>
			-- read Data
			if(isRcvBufferEmpty = '1') then
				-- cannot do anything, must wait
			else
				-- i can dequeue and save the value for the bus
				deq <= '1';
				nxt_rd_d <= rcvFirst;
				nxtrdy <= "00";
			end if;
		when others => null;
	end case;

elsif (iwr = '1') then
	-- the write part
	case(iAddr) is
		when "00" =>
			-- reset RCV
			resetRCV <= '1';
			nxtrdy <= "00";
		when "01" => 
			-- write SND Count
			setSndCount <= '1';
			nxtrdy <= "00";
		when "10" =>
			-- write SND Dest
			setSndDst <= '1';
			nxtrdy <= "00";
		when "11" =>
			-- write Data
			if(isSndBufferFull = '1') then
				-- cannot do anything, must wait
			else
				-- i can enqueue data
				enq <= '1';
				nxtrdy <= "00";
			end if;
		when others => null;
	end case;
end if;

end if;

end process;

end Behavioral;

