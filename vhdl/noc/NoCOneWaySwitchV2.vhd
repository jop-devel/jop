----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    12:13:58 02/09/2011 
-- Design Name: 
-- Module Name:    NoCOneWaySwitch - Behavioral 
-- Project Name: 
-- Target Devices: 
-- Tool versions: 
-- Description: 
--
-- Dependencies: 
--
-- Revision: 
-- Revision 0.01 - File Created
-- Additional Comments: 
--
----------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

-- Uncomment the following library declaration if using
-- arithmetic functions with Signed or Unsigned values
--use IEEE.NUMERIC_STD.ALL;

-- Uncomment the following library declaration if instantiating
-- any Xilinx primitives in this code.
--library UNISIM;
--use UNISIM.VComponents.all;

use work.NoCTypes.ALL;


entity NoCOneWaySwitchV2 is
	 Generic (
			  NoCMask: NoCAddr := "100";
			  NoCAID: NoCAddr := "100";
			  NoCBID: NoCAddr := "000";
			  BufferSize: integer := 2;
			  BufferAddrBits: integer := 1
	 );
	Port (  Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
	 -- NoCA IO
           nocAIn : in  NoCPacket;
			  nocAOut : out  NoCPacket;

	 -- NoCB IO
           nocBIn : in  NoCPacket;
           nocBOut : out  NoCPacket
	);
end NoCOneWaySwitchV2;

architecture Behavioral of NoCOneWaySwitchV2 is

type SwState is (WaitForA2B, WaitForASlotInB, WaitForReplyFromB, WaitForASlotInA);
signal crts, nxts : SwState;

signal regA2B, nxt_regA2B, regReplyA2B, nxt_regReplyA2B: NoCPacket;

begin


SYNC: process (Clk)
begin
	if (Clk'event and Clk = '1') then
      if (Rst = '1') then
 			crts <= WaitForA2B;
			regA2B <= (Src => NoCBID, Dst => NoCBID, pType => PTNil, Load => (others =>'0'));
			regReplyA2B <= (Src => NoCBID, Dst => NoCBID, pType => PTNil, Load => (others =>'0'));			
		else
			crts <= nxts;
			regA2B <= nxt_regA2B;
			regReplyA2B <= nxt_regReplyA2B;
      end if;
   end if;	
end process;


COMB: process (crts, nocAIn, nocBIn, regA2B, regReplyA2B)
begin
 nxts <= crts;
 nxt_regA2B <= regA2B;
 nxt_regReplyA2B <= regReplyA2B;
 nocBOut <= nocBIn;
 nocAOut <= nocAIn;
 case(crts) is
		when WaitForA2B =>
			-- this is when we detect an attempt of sending something from
			-- NoC A to B
			if((nocAIn.Dst AND NoCMask) = NoCBID) then
				nxt_regA2B <= nocAIn;
				nxts <= WaitForASlotInB;
			end if;
		when WaitForASlotInB =>
			-- here we detect that we can inject the frame into the B noc
			if((nocBIn.Src AND NocMask) = NoCAID) then
				nocBOut <= regA2B;
				nxts <= WaitForReplyFromB;
			end if;
		when WaitForReplyFromB =>
			-- here we should detect that the sent frame returns (with Ack or not)
			if((nocBIn.Src = regA2B.Src) AND (nocBIn.Dst = regA2B.Dst)) then
				-- store the reply
				nxt_regReplyA2B <= nocBIn;
				nxts <= WaitForASlotInA;
			end if;
		when WaitForASlotInA =>
			-- here we can finaly reply in A with the result we got from B
			if(nocAIn.Src = regReplyA2B.Src) then
				nocAOut <= regReplyA2B;
				nxts <= WaitForA2B;
			end if;
      when others =>
			-- unknown state
 end case;

end process;

end Behavioral;

