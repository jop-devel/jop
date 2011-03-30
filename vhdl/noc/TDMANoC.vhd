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


entity TDMANoC is
	 Generic (
				Nodes: integer := 4;
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
           rdy_cnt : out sc_rdy_cnt_type(0 to Nodes-1)
	 );
end TDMANoC;

architecture Behavioral of TDMANoC is

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

signal nReg : sc_io_type(0 to Nodes-1); 

begin


 AllNodes: for i in 0 to Nodes-1 generate
 
   node: entity work.TDMANode 
		generic map ( CONV_STD_LOGIC_VECTOR(i,NOCADDRBITS), BufferSize, BufferAddrBits)
		port map (Clk, Rst, Addr(i), wr(i), wr_data(i), rd(i), rd_data(i), rdy_cnt(i), nReg(i), nReg( (i+1) mod Nodes));
 
 end generate;

end Behavioral;

