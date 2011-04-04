----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    16:37:54 02/08/2011 
-- Design Name: 
-- Module Name:    NoCSwitch - Behavioral 
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

entity NoCSwitchV2 is
	 Generic (
			  NoCMask: NoCAddr := "100";
			  NoCAID: NoCAddr := "000";
			  NoCBID: NoCAddr := "100";
			  BufferSize: integer := 2;
			  BufferAddrBits: integer := 1
	 );
	Port (  ClkA : in  STD_LOGIC;
		     ClkB : in STD_LOGIC;
           Rst : in  STD_LOGIC;
	 -- NoCA IO
           nocAIn : in  NoCPacket;
           nocAOut : out  NoCPacket;	
	 -- NoCB IO
           nocBIn : in  NoCPacket;
           nocBOut : out  NoCPacket
	);
	
	
end NoCSwitchV2;

architecture Behavioral of NoCSwitchV2 is

component NoCOneWaySwitchV2 is
	 Generic (
			  NoCMask: NoCAddr;
			  NoCAID: NoCAddr;
			  NoCBID: NoCAddr;
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
end component NoCOneWaySwitchV2;

signal nocAOutA2B, nocBOutA2B, nocBOutB2A, nocAOutB2A, inocBOut, inocAOut: NoCPacket;

begin

A2Bswitch: NoCOneWaySwitchV2 
generic map (
	 NoCMask => NoCMask,
	 NoCAID => NoCAID,
	 NoCBID => NoCBID,
	 BufferSize => BufferSize,
	 BufferAddrBits => BufferAddrBits
)
port map (
	 Clk => ClkA,
	 Rst => Rst,
    nocAIn => nocAIn,
	 nocAOut => nocAOutA2B,
	 nocBIn => nocBIn,
	 nocBOut => nocBOutA2B
);

B2Aswitch: NoCOneWaySwitchV2 
generic map (
	 NoCMask => NoCMask,
	 NoCAID => NoCBID,
	 NoCBID => NoCAID,
	 BufferSize => BufferSize,
	 BufferAddrBits => BufferAddrBits
)
port map (
	 Clk => ClkB,
	 Rst => Rst,
	 nocAIn => nocBIn,
	 nocAOut => nocBOutB2A,
	 nocBIn => nocAIn,
	 nocBOut => nocAOutB2A
);


process (ClkB)
begin
	if rising_edge(ClkB) then
		if (Rst = '1') then
			inocBOut <= (Src => NoCAID, Dst => NoCAID, pType => PTNil, Load => (others =>'0'));		
		else if((nocBIn.Src AND NoCMask) = NoCAID) then
				inocBOut <= nocBOutA2B;
			else
				inocBOut <= nocBOutB2A;
			end if;
		end if;
   end if;	
end process;


process (ClkA)
begin
	if rising_edge(ClkA) then
		if (Rst = '1') then
			inocAOut <= (Src => NoCBID, Dst => NoCBID, pType => PTNil, Load => (others =>'0'));		
		else if((nocAIn.Src AND NoCMask) = NoCBID) then
				inocAOut <= nocAOutB2A;
			else
				inocAOut <= nocAOutA2B;
			end if;
		end if;
   end if;	
end process;

nocBOut <= inocBOut;
nocAOut <= inocAOut;

end Behavioral;

