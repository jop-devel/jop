----------------------------------------------------------------------------------
-- Company: 
-- Engineer: 
-- 
-- Create Date:    15:54:27 02/18/2011 
-- Design Name: 
-- Module Name:    NoCRing - Behavioral 
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

-- Uncomment the following library declaration if using
-- arithmetic functions with Signed or Unsigned values
--use IEEE.NUMERIC_STD.ALL;

-- Uncomment the following library declaration if instantiating
-- any Xilinx primitives in this code.
--library UNISIM;
--use UNISIM.VComponents.all;

library IEEE;
use IEEE.STD_LOGIC_1164.ALL;
use IEEE.STD_LOGIC_ARITH.ALL;
use IEEE.STD_LOGIC_UNSIGNED.ALL;

use work.NoCTypes.ALL;


entity NoCOpenRing is
	 Generic (
				Nodes: integer := 4;
				FirstNodeAddress: integer := 0;
	 			BufferSize: integer := 4;
			   BufferAddrBits: integer := 2
	 );
    Port ( Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
			  -- arrays of signals for SimpCon
			  Addr : in sc_addr_type(0 to Nodes-1);
           wr : in sc_bit_type(0 to Nodes-1);
           wr_data : in sc_word_type(0 to Nodes-1);
           rd : in sc_bit_type(0 to Nodes-1);
           rd_data : out sc_word_type(0 to Nodes-1);
           rdy_cnt : out sc_rdy_cnt_type(0 to Nodes-1);
			  -- noc in and out, good to connect any switches
           nocIn : in  NoCPacket;
           nocOut : out  NoCPacket
	 );
end NoCOpenRing;

architecture Behavioral of NoCOpenRing is

component TDMANode is
	 Generic (
			  myAddr : NoCAddr := (others => '0');
			  BufferSize: integer := 4;
			  BufferAddrBits: integer := 2
	 );
	Port (  Clk : in  STD_LOGIC;
           Rst : in  STD_LOGIC;
			  -- SimpCon Signals
           Addr : in  STD_LOGIC_VECTOR (1 downto 0);
           wr : in  STD_LOGIC;
           wr_data : in  STD_LOGIC_VECTOR (31 downto 0);
           rd : in  STD_LOGIC;
           rd_data : out  STD_LOGIC_VECTOR (31 downto 0);
           rdy_cnt : out  STD_LOGIC_VECTOR (1 downto 0);
			  
			  -- NoC IO
           nocIn : in  NoCPacket;
           nocOut : out  NoCPacket
	);
end component;

signal nReg : sc_io_type(0 to Nodes); 

begin
 
 nReg(0) <= nocIn;
 nocOut <= nReg(Nodes);

 AllNodes: for i in 0 to Nodes-1 generate
 
   node: entity work.TDMANode 
		generic map ( CONV_STD_LOGIC_VECTOR(FirstNodeAddress+i,NOCADDRBITS), BufferSize, BufferAddrBits)
		port map (Clk, Rst, Addr(i), wr(i), wr_data(i), rd(i), rd_data(i), rdy_cnt(i), nReg(i), nReg(i+1));
 
 end generate;

end Behavioral;

