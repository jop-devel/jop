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
 
ENTITY TestNode IS
END TestNode;
 
ARCHITECTURE behavior OF TestNode IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT TDMANode
	 	 Generic (
			  myAddr : NoCAddr := (others => '0');
			  BufferSize: integer := 4;
			  BufferAddrBits: integer := 2
	 );
    PORT(
         Clk : IN  std_logic;
         Rst : IN  std_logic;
         Addr : IN  std_logic_vector(1 downto 0);
         wr : IN  std_logic;
         wr_data : IN  std_logic_vector(31 downto 0);
         rd : IN  std_logic;
         rd_data : OUT  std_logic_vector(31 downto 0);
         rdy_cnt : OUT  std_logic_vector(1 downto 0);
         nocIn : IN  NoCPacket;
         nocOut : OUT  NoCPacket
        );
    END COMPONENT;
    

   --Inputs
   signal Clk : std_logic := '0';
   signal Rst : std_logic := '0';
   signal Addr : std_logic_vector(1 downto 0) := (others => '0');
   signal wr : std_logic := '0';
   signal wr_data : std_logic_vector(31 downto 0) := (others => '0');
   signal rd : std_logic := '0';
   signal nocIn : NoCPacket;

 	--Outputs
   signal rd_data : std_logic_vector(31 downto 0);
   signal rdy_cnt : std_logic_vector(1 downto 0);
   signal nocOut :  NoCPacket := (Src => (others => '0'), Dst => (others => '0'), pType => PTNil, Load => (others =>'0'));
   constant Clk_period : TIME := 10ns;

BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: TDMANode 
	GENERIC MAP (
		myAddr => "001",
		BufferSize => 4,
		BufferAddrBits => 2
		
	)
	PORT MAP (
          Clk => Clk,
          Rst => Rst,
          Addr => Addr,
          wr => wr,
          wr_data => wr_data,
          rd => rd,
          rd_data => rd_data,
          rdy_cnt => rdy_cnt,
          nocIn => nocIn,
          nocOut => nocOut
        );
 
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

      wait for Clk_period*10;

      -- insert stimulus here 
      -- insert stimulus here 
		nocIn <= (Src => "011", Dst => "001", pType => PTData, Load => X"CAFEBABE"),
					(Src => (others => '0'), Dst => (others => '0'), pType => PTNil, Load => (others =>'0')) after Clk_period,
					(Src => "001", Dst => "001", pType => PTEoD, Load => X"BEBECACA") after Clk_period*3,
					(Src => "011", Dst => "001", pType => PTEoD, Load => X"DEADBEEF") after Clk_period*4,
					(Src => (others => '0'), Dst => (others => '0'), pType => PTNil, Load => (others =>'0')) after Clk_period*5;
--		ackEoD <= True after Clk_period*6,
--					 False after Clk_period*7;
		wait for Clk_period*10;
		
		-- try to read status
		Addr <= "00";
		rd <= '1', '0' after Clk_period;

		-- read one word
		wait for Clk_period*3;
		Addr <= "11";
		rd <= '1', '0' after Clk_period;
	

		-- read one word
		wait for Clk_period*3;
		Addr <= "11";
		rd <= '1', '0' after Clk_period;

		wait for Clk_period*3;
		-- try to read status
		Addr <= "00";
		rd <= '1', '0' after Clk_period;
		
		wait for Clk_period*3;
	   -- reset receive
		Addr <= "00";
		wr <= '1', '0' after Clk_period;
		
		wait for Clk_period*3;
		-- try to read status
		Addr <= "00";
		rd <= '1', '0' after Clk_period;

		wait for Clk_period*3;
		-- try some sends
		-- dest address
		Addr <= "10";
		wr_data <= X"00000003";
		wr <= '1', '0' after Clk_period;
	
		wait for Clk_period*3;
		
		-- send 5 words!
		Addr <= "01";
		wr_data <= X"00000005";
		wr <= '1', '0' after Clk_period;
		
		wait for Clk_period*3;
		Addr <= "11";
		wr_data <= X"FEEDCACA";
		wr <= '1', '0' after Clk_period;		
		
		wait for Clk_period*3;
		Addr <= "11";
		wr_data <= X"BEEFBABE";
		wr <= '1', '0' after Clk_period;		

		wait for Clk_period*3;
		Addr <= "11";
		wr_data <= X"CAFEFEED";
		wr <= '1', '0' after Clk_period;		

		wait for Clk_period*3;
		Addr <= "11";
		wr_data <= X"BBBBAAAA";
		wr <= '1', '0' after Clk_period;		

		wait for Clk_period*3;
		Addr <= "11";
		wr_data <= X"DEAFBABA";
		wr <= '1', '0' after Clk_period;	
		
		wait for Clk_period*5;
		nocIn <= (Src => "001", Dst => "000", pType => PTNil, Load => X"01010101"),
					(Src => "001", Dst => (others => '0'), pType => PTAck, Load => (others =>'0')) after Clk_period;

      wait;
   end process;

END;
