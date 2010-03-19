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

 
ENTITY TestNoC IS
END TestNoC;
 
ARCHITECTURE behavior OF TestNoC IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT TDMANoC
    PORT(
         Clk : IN  std_logic;
         Rst : IN  std_logic;
         Addr : IN  sc_addr_type;
         wr : IN  sc_bit_type;
         wr_data : IN  sc_word_type;
         rd : IN  sc_bit_type;
         rd_data : OUT  sc_word_type;
         rdy_cnt : OUT  sc_rdy_cnt_type
        );
    END COMPONENT;
    

   --Inputs
   signal Clk : std_logic := '0';
   signal Rst : std_logic := '0';
   signal Addr : sc_addr_type;
   signal wr : sc_bit_type;
   signal wr_data : sc_word_type;
   signal rd : sc_bit_type;

 	--Outputs
   signal rd_data : sc_word_type;
   signal rdy_cnt : sc_rdy_cnt_type;
   constant Clk_period : TIME := 10ns;
 
BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: TDMANoC PORT MAP (
          Clk => Clk,
          Rst => Rst,
          Addr => Addr,
          wr => wr,
          wr_data => wr_data,
          rd => rd,
          rd_data => rd_data,
          rdy_cnt => rdy_cnt
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
		rd(0) <= '0'; wr(0) <= '0'; Addr(0) <= "00";
 		rd(1) <= '0'; wr(1) <= '0'; Addr(1) <= "00";
		rd(2) <= '0'; wr(2) <= '0'; Addr(2) <= "00";
		rd(3) <= '0'; wr(3) <= '0'; Addr(3) <= "00";
     -- hold reset state for 100ms.
		Rst <= '1';
      wait for 20ns;
		Rst <= '0';	

      wait for Clk_period*10;
      -- insert stimulus here 

	   rd(0) <= '1', '0' after Clk_period;
	   rd(1) <= '1', '0' after Clk_period;
	   rd(2) <= '1', '0' after Clk_period;
	   rd(3) <= '1', '0' after Clk_period;
      wait for Clk_period*3;
	  
	   -- send something from 1 to 3
		Addr(1) <= "10";
		wr_data(1) <= X"00000003"; 
		wr(1) <= '1', '0' after Clk_period;
      wait for Clk_period*3;

		Addr(1) <= "01";
		wr_data(1) <= X"00000005"; 
		wr(1) <= '1', '0' after Clk_period;
      wait for Clk_period*3;

		Addr(1) <= "11";
		wr_data(1) <= X"11111111"; 
		wr(1) <= '1', '0' after Clk_period;
      wait for Clk_period*3;
		Addr(1) <= "11";
		wr_data(1) <= X"22222222"; 
		wr(1) <= '1', '0' after Clk_period;
      wait for Clk_period*3;
		Addr(1) <= "11";
		wr_data(1) <= X"33333333"; 
		wr(1) <= '1', '0' after Clk_period;
      wait for Clk_period*3;
		Addr(1) <= "11";
		wr_data(1) <= X"44444444"; 
		wr(1) <= '1', '0' after Clk_period;
      wait for Clk_period*3;
		Addr(1) <= "11";
		wr_data(1) <= X"55555555"; 
		wr(1) <= '1', '0' after Clk_period;
      wait for Clk_period*10;

		-- let's read some from the destination
		Addr(3) <= "11";
		rd(3) <= '1','0' after Clk_period;
      wait for Clk_period*3;
		Addr(3) <= "00";
		rd(3) <= '1','0' after Clk_period;
      wait for Clk_period*3;
		Addr(3) <= "11";
		rd(3) <= '1','0' after Clk_period;
      wait for Clk_period*3;
		Addr(3) <= "11";
		rd(3) <= '1','0' after Clk_period;
      wait for Clk_period*3;
		Addr(3) <= "00";
		rd(3) <= '1','0' after Clk_period;
      wait for Clk_period*3;
		
      wait;
   end process;

END;
