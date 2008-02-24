--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2007,2008, Christof Pitter
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




library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.sc_pack.all;
use work.sc_arbiter_pack.all;

entity tb_arbiter is
end tb_arbiter;

architecture test of tb_arbiter is
  
  component arbiter is
	generic (addr_bits : integer;
			 cpu_cnt	: integer);
	port (
			clk, reset	: in std_logic;			
			arb_out	: in arb_out_type(0 to cpu_cnt-1);
			arb_in	: out arb_in_type(0 to cpu_cnt-1);
			mem_out	: out sc_mem_out_type;
			mem_in	: in sc_in_type
	);
  end component;
	
	signal addr_bits : integer := 21;
	signal cpu_cnt : integer := 2;
	
	-- Stimulus Signals
	signal s_clk	: std_logic := '0'; 
	signal s_reset	: std_logic := '0';
	
	signal s_address_jop	: std_logic_vector(addr_bits-1 downto 0);
	signal s_wr_data_jop	: std_logic_vector(31 downto 0);
	signal s_rd_jop			: std_logic := '0';
	signal s_wr_jop			: std_logic := '0';
	
	signal s_address_vga	: std_logic_vector(addr_bits-1 downto 0);
	signal s_wr_data_vga	: std_logic_vector(31 downto 0);
  signal s_rd_vga			: std_logic := '0';		
  signal s_wr_vga			: std_logic := '0';
	
	signal s_rd_data_mem	: std_logic_vector(31 downto 0);
	signal s_rdy_cnt_mem	: unsigned(1 downto 0);
	
	-- Response Signals
	signal r_rd_data_jop	: std_logic_vector(31 downto 0);
	signal r_rdy_cnt_jop	: unsigned(1 downto 0);
	
	signal r_rd_data_vga	: std_logic_vector(31 downto 0);
	signal r_rdy_cnt_vga	: unsigned(1 downto 0);
	
	signal r_address_mem	: std_logic_vector(addr_bits-1 downto 0);
	signal r_wr_data_mem	: std_logic_vector(31 downto 0);
	signal r_rd_mem			: std_logic;
	signal r_wr_mem			: std_logic;

begin
	arbiter1: arbiter 
	generic map (
		addr_bits => 21,
		cpu_cnt => 2)
	port map(
		clk => s_clk,
		reset => s_reset,
		
		arb_out(0).address => s_address_vga,
		arb_out(0).wr_data => s_wr_data_vga,
		arb_out(0).rd => s_rd_vga,
		arb_out(0).wr => s_wr_vga,
		arb_out(1).address => s_address_jop,
		arb_out(1).wr_data => s_wr_data_jop,
		arb_out(1).rd => s_rd_jop,
		arb_out(1).wr => s_wr_jop,
		
		arb_in(0).rd_data => r_rd_data_vga,
		arb_in(0).rdy_cnt => r_rdy_cnt_vga,
		arb_in(1).rd_data => r_rd_data_jop,
		arb_in(1).rdy_cnt => r_rdy_cnt_jop,
		
		mem_out.address => r_address_mem,
		mem_out.wr_data => r_wr_data_mem,
		mem_out.rd => r_rd_mem,
		mem_out.wr => r_wr_mem,
		mem_in.rd_data => s_rd_data_mem,
		mem_in.rdy_cnt => s_rdy_cnt_mem
    );
 
	s_clk <= not s_clk after 5 ns;
	
	stim : process
	begin
	
	
-- Zugriff nacheinander von einem master jop
--		s_reset <= '1';
--		s_rdy_cnt_mem <= "00";
--		wait for 10 ns;
--		s_reset <= '0';
--		wait for 5 ns;
--		s_rdy_cnt_mem <= "00";
--		s_rd_jop <= '1';
--		s_address_jop <= "001001111111111111111"; -- 4ffff
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "10";
--		s_rd_jop <= '0';
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "01";
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "00";
--		s_rd_data_mem <= "10101010101010101010101010101010";
--		wait for 20 ns;
--		s_rd_jop <= '1';
--		s_address_jop <= "001001111111111111110"; -- 4fffe
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "10";
--		s_rd_jop <= '0';
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "01";
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "00";
--		s_rd_data_mem <= "01010101010101010101010101010101";
--		wait for 25 ns;	
	
-- Zugriff nacheinander von einem master vga
--		s_reset <= '1';
--		s_rdy_cnt_mem <= "00";
--		wait for 10 ns;
--		s_reset <= '0';
--		wait for 5 ns;
--		s_rdy_cnt_mem <= "00";
--		s_rd_vga <= '1';
--		s_address_vga <= "001001111111111111111"; -- 4ffff
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "10";
--		s_rd_vga <= '0';
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "01";
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "00";
--		s_rd_data_mem <= "10101010101010101010101010101010";
--		wait for 20 ns;
--		s_rd_vga <= '1';
--		s_address_vga <= "001001111111111111110"; -- 4fffe
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "10";
--		s_rd_vga <= '0';
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "01";
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "00";
--		s_rd_data_mem <= "01010101010101010101010101010101";
--		wait for 25 ns;

--	-- pipelined jop read
--	  s_reset <= '1';
--	  s_rdy_cnt_mem <= "00";
--	  wait for 10 ns;
--	  s_reset <= '0';
--	  wait for 5 ns;
--	  s_rdy_cnt_mem <= "00";
--	  s_rd_jop <= '1';
--	  s_address_jop <= "001001111111111111111"; -- 4ffff
--	  wait for 10 ns;
--	  s_rdy_cnt_mem <= "10";
--	  s_rd_jop <= '0';
--	  wait for 10 ns;
--	  s_rdy_cnt_mem <= "01";
--	  s_rd_jop <= '1';
--	  s_address_jop <= "001010000000000000000"; -- 50000
--	  wait for 10 ns;
--	  s_rd_jop <= '0';
--	  s_rdy_cnt_mem <= "10";
--	  s_rd_data_mem <= "10101010101010101010101010101010";
--	  wait for 10 ns;
--	  s_rdy_cnt_mem <= "01";
--	  s_rd_jop <= '1';
--	  s_address_jop <= "001001111111111111111"; -- 4ffff
--	  wait for 10 ns;
--	  s_rd_jop <= '0';
--	  s_rdy_cnt_mem <= "10";
--	  s_rd_data_mem <= "11111111111111111111111111111111";
--	  wait for 10 ns;
--	  s_rdy_cnt_mem <= "01";
--	  s_rd_jop <= '1';
--	  s_address_jop <= "001000000000000000000"; -- 40000
--	  wait for 10 ns;
--	  s_rd_jop <= '0';
--	  s_rdy_cnt_mem <= "10";
--	  s_rd_data_mem <= "01010101010101010101010101010101";  
--	  wait for 10 ns;
--	  s_rdy_cnt_mem <= "01";
--	  wait for 10 ns;
--	  s_rdy_cnt_mem <= "00";
--	  s_rd_data_mem <= "11111111111111111111111111111111";
--	  wait for 25 ns;  
--
-- -- schreiben und lesen von einem master
--		s_reset <= '1';
--		s_rdy_cnt_mem <= "00";
--		wait for 10 ns;
--		s_reset <= '0';
--		wait for 5 ns;
--		s_rdy_cnt_mem <= "00";
--		s_rd_jop <= '1';
--		s_address_jop <= "001001111111111111111"; -- 4ffff
--		wait for 10 ns;
--		s_rd_jop <= '0';
--		s_rdy_cnt_mem <= "10";
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "01";
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "00";	
--		s_rd_data_mem <= "10101010101010101010101010101010";	
--		wait for 10 ns;   
--		s_wr_jop <= '1';
--		s_wr_data_jop <= "01010101010101010101010101010101";  -- 5555555
--		s_address_jop <= "001001111111111111110"; -- 4fffe 
--		wait for 10 ns;
--		s_wr_jop <= '0';
--		s_rdy_cnt_mem <= "10";
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "01";                          
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "00";
--		wait for 10 ns;   
--		s_rd_jop <= '1';
--		s_address_jop <= "001001111111111111111"; -- 4ffff
--		wait for 10 ns;
--		s_rd_jop <= '0';
--		s_rdy_cnt_mem <= "10";
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "01";
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "00";	
--		s_rd_data_mem <= "11111111111111111111111111111111";	
--		wait for 25 ns;


 -- jop greift zu vga muss warten
  s_reset <= '1';
  s_rdy_cnt_mem <= "00";
  wait for 10 ns;
  s_reset <= '0';
  wait for 5 ns;
  s_rdy_cnt_mem <= "00";
  s_rd_jop <= '1';
  s_rd_vga <= '0';
  s_address_jop <= "001001111111111111111"; -- 4ffff
  wait for 10 ns;
  s_rdy_cnt_mem <= "10";
  s_rd_jop <= '0';
  s_rd_vga <= '0';
  wait for 10 ns;
  s_rdy_cnt_mem <= "01";
  s_rd_vga <= '1';
  s_address_vga <= "001010000000000000000"; -- 50000
  wait for 10 ns;
  s_rd_vga <= '0';
  s_rdy_cnt_mem <= "00";
  s_rd_data_mem <= "10101010101010101010101010101010";
  
  
  s_rd_jop <= '1';
  s_address_jop <= "111111111111111111111"; -- fffff
  wait for 10 ns;
  s_rdy_cnt_mem <= "10";
  s_rd_jop <= '0';
  wait for 10 ns;
  s_rdy_cnt_mem <= "01";
  wait for 10 ns;
  s_rdy_cnt_mem <= "00";
  s_rd_data_mem <= "11111111111111111111111111111111";
  wait for 10 ns;
  s_rdy_cnt_mem <= "10";
  wait for 10 ns;
  s_rdy_cnt_mem <= "01";
  wait for 10 ns;
  s_rdy_cnt_mem <= "00";
  s_rd_data_mem <= "00000000000000000000000000000000";
  
  
  
  wait for 10 ns;
  s_rdy_cnt_mem <= "10";
  wait for 10 ns;
  s_rdy_cnt_mem <= "01";
  wait for 10 ns;
  s_rdy_cnt_mem <= "00";
  s_rd_data_mem <= "11111111111111111111111111111110"; 
  s_rd_vga <= '1';
  s_address_vga <= "001000000000000000000"; -- 40000
  wait for 10 ns;
  s_rd_vga <= '0';
  s_rdy_cnt_mem <= "10";
  wait for 10 ns;
  s_rdy_cnt_mem <= "01";
  wait for 10 ns;
  s_rdy_cnt_mem <= "00";
  s_rd_data_mem <= "01010101010101010101010101010101";
  wait for 20 ns;
  
 
  s_rd_jop <= '1';
  s_address_jop <= "001001111111111111111"; -- 4ffff
  wait for 10 ns;
  s_rdy_cnt_mem <= "10";
  s_rd_jop <= '0';
  s_rd_vga <= '1';
  s_address_vga <= "001010000000000000000"; -- 50000
  wait for 10 ns;
  s_rd_vga <= '0';
  s_rdy_cnt_mem <= "01";
  wait for 10 ns;
  s_rdy_cnt_mem <= "00";
  s_rd_data_mem <= "10101010101010101010101010101010";
  wait for 10 ns;
  s_rdy_cnt_mem <= "10";
  wait for 10 ns;
  s_rdy_cnt_mem <= "01";
  wait for 10 ns;
  s_rdy_cnt_mem <= "00";
  s_rd_data_mem <= "11111111111111111111111111111111";
  
  
  wait for 20 ns;
  s_wr_jop <= '1';
  s_address_jop <= "011001111111100011111"; -- 0CFF1F
  s_wr_data_jop <= "11111111111111111111110000000000"; -- FFFFC00
  wait for 10 ns;
  s_wr_jop <= '0';
  s_rdy_cnt_mem <= "10";
  wait for 10 ns;
  s_rdy_cnt_mem <= "01";	
  wait for 10 ns;
  s_rd_vga <= '1';                                
  s_rdy_cnt_mem <= "00";                         
  s_address_vga <= "011010000110000011100"; --    
  wait for 10 ns;
  s_rd_vga <= '0';
  s_rdy_cnt_mem <= "10";
  wait for 10 ns;
  s_rdy_cnt_mem <= "01";
  s_rd_vga <= '1';
  s_address_vga <= "001010000000000000000"; -- 50000	
  wait for 10 ns;
  s_rd_vga <= '0';
  s_rdy_cnt_mem <= "10";
  s_rd_data_mem <= "11111111111111111111111111111110";
  wait for 10 ns;
  s_rdy_cnt_mem <= "01";
  wait for 10 ns;
  s_rdy_cnt_mem <= "00";    
  s_rd_data_mem <= "01010101111111111111010101010101"; -- 55fff555
  wait for 45 ns;        
 
--		-- gleichzeitiger Zugriff
--		s_reset <= '1';                                    
--		s_rdy_cnt_mem <= "00";                             
--		wait for 10 ns;                                    
--		s_reset <= '0';                                    
--		wait for 5 ns;                                     
--		s_rdy_cnt_mem <= "00";                             
--		s_rd_jop <= '1';                                   
--		s_rd_vga <= '1';                                   
--		s_address_jop <= "001001111111111111111"; -- 4ffff 
--		s_address_vga <= "001010000000000000000"; -- 50000 
--		wait for 10 ns;                                    
--		s_rdy_cnt_mem <= "10";                             
--		s_rd_jop <= '0';                                   
--		s_rd_vga <= '0';                                   
--		wait for 10 ns;                                    
--		s_rdy_cnt_mem <= "01";                    
--		wait for 10 ns;                                    
--		s_rdy_cnt_mem <= "00";                             
--		s_rd_data_mem <= "11111111111111111111111111111110";
--		wait for 10 ns;           
--		s_rdy_cnt_mem <= "10";                             
--		wait for 10 ns;              
--		s_rdy_cnt_mem <= "01";                             
--		wait for 10 ns;              
--		s_rdy_cnt_mem <= "00";
--		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..
--		s_rd_jop <= '1';                                   
--		s_rd_vga <= '1';                                   
--		s_address_jop <= "000011110000111100001"; 
--		s_address_vga <= "111111111111111111111";  -- FFFFF                                                  
--		wait for 10 ns; 
--		s_rd_jop <= '0';                                   
--		s_rd_vga <= '0';                 
--		s_rdy_cnt_mem <= "10";                             
--		wait for 10 ns;              
--		s_rdy_cnt_mem <= "01";                             
--		wait for 10 ns;
--		s_rdy_cnt_mem <= "00";   		              
--		s_rd_data_mem <= "01010101010101010101010101010101"; -- 5555..                                                           
--		wait for 10 ns;                                    
--		s_rdy_cnt_mem <= "10";                                                               
--		wait for 10 ns;                                                                      
--		s_rdy_cnt_mem <= "01";                             
--		wait for 10 ns;                                    
--		s_rdy_cnt_mem <= "00";                             
--		s_rd_data_mem <= "00110011001100110011001100110011"; -- 333..
--		s_rd_jop <= '1';                                   
--		s_rd_vga <= '1';                                   
--		s_address_jop <= "000100010001000100011";
--		s_address_vga <= "010101010101010101010";     
--		wait for 10 ns;                                    
--		s_rd_jop <= '0';                                   
--		s_rd_vga <= '0';                 
--		s_rdy_cnt_mem <= "10";                                   
--		wait for 10 ns;                                                                                                      
--		s_rdy_cnt_mem <= "01";                             
--		wait for 10 ns;                                    
--		s_rdy_cnt_mem <= "00";                             
--		s_rd_data_mem <= "10101010101010101010101010101010"; -- AA..
--		wait for 10 ns;                                    
--		s_rdy_cnt_mem <= "10";                                                               
--		wait for 10 ns;                                                                      
--		s_rdy_cnt_mem <= "01";                             
--		wait for 10 ns;                                    
--		s_rdy_cnt_mem <= "00";
--		s_rd_data_mem <= "01010101111111111111010101010101"; -- 55fff555
--		wait for 80 ns;                      

	end process stim;
	
end test;



















