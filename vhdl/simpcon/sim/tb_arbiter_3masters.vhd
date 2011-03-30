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
			mem_out	: out sc_out_type;
			mem_in	: in sc_in_type
	);
  end component;
	
	signal addr_bits : integer := SC_ADDR_SIZE;
	signal cpu_cnt : integer := 3;
	
	-- Stimulus Signals
	signal s_clk	: std_logic := '0'; 
	signal s_reset	: std_logic := '0';
	
	signal s_address_m0	: std_logic_vector(addr_bits-1 downto 0);
	signal s_wr_data_m0	: std_logic_vector(31 downto 0);
	signal s_rd_m0			: std_logic := '0';
	signal s_wr_m0			: std_logic := '0';
	
	signal s_address_m1	: std_logic_vector(addr_bits-1 downto 0);
	signal s_wr_data_m1	: std_logic_vector(31 downto 0);
  signal s_rd_m1			: std_logic := '0';		
  signal s_wr_m1			: std_logic := '0';
  
  signal s_address_m2	: std_logic_vector(addr_bits-1 downto 0);
	signal s_wr_data_m2	: std_logic_vector(31 downto 0);
  signal s_rd_m2			: std_logic := '0';		
  signal s_wr_m2			: std_logic := '0';
	
	signal s_rd_data_mem	: std_logic_vector(31 downto 0);
	signal s_rdy_cnt_mem	: unsigned(1 downto 0);
	
	-- Response Signals
	signal r_rd_data_m0	: std_logic_vector(31 downto 0);
	signal r_rdy_cnt_m0	: unsigned(1 downto 0);
	
	signal r_rd_data_m1	: std_logic_vector(31 downto 0);
	signal r_rdy_cnt_m1	: unsigned(1 downto 0);
		
	signal r_rd_data_m2	: std_logic_vector(31 downto 0);
	signal r_rdy_cnt_m2	: unsigned(1 downto 0);
	
	signal r_address_mem	: std_logic_vector(addr_bits-1 downto 0);
	signal r_wr_data_mem	: std_logic_vector(31 downto 0);
	signal r_rd_mem			: std_logic;
	signal r_wr_mem			: std_logic;
	signal r_atomic		: std_logic;
	signal r_cache		: sc_cache_type;
	signal r_tm_cache	: std_logic;
	signal r_tm_broadcast: std_logic;
	signal r_cinval : std_logic;

begin
	
	arbiter1: arbiter 
	generic map (
		addr_bits => SC_ADDR_SIZE,
		cpu_cnt => 3)
	port map(
		clk => s_clk,
		reset => s_reset,
	
		arb_out(0).address => s_address_m0,
		arb_out(0).wr_data => s_wr_data_m0,
		arb_out(0).rd => s_rd_m0,
		arb_out(0).wr => s_wr_m0,
		arb_out(0).atomic => '0',
		arb_out(0).cache => bypass,
		arb_out(0).tm_cache => '0',
		arb_out(0).tm_broadcast => '0',
		arb_out(0).cinval => '0',
		arb_out(1).address => s_address_m1,
		arb_out(1).wr_data => s_wr_data_m1,
		arb_out(1).rd => s_rd_m1,
		arb_out(1).wr => s_wr_m1,
	   arb_out(1).atomic => '0',
		arb_out(1).cache => bypass,
		arb_out(1).tm_cache => '0',
		arb_out(1).tm_broadcast => '0',
		arb_out(1).cinval => '0',
		arb_out(2).address => s_address_m2,
		arb_out(2).wr_data => s_wr_data_m2,
		arb_out(2).rd => s_rd_m2,
		arb_out(2).wr => s_wr_m2,
		arb_out(2).atomic => '0',
		arb_out(2).cache => bypass,
		arb_out(2).tm_cache => '0',
		arb_out(2).tm_broadcast => '0',
		arb_out(2).cinval => '0',		
		arb_in(0).rd_data => r_rd_data_m0,
		arb_in(0).rdy_cnt => r_rdy_cnt_m0, 
		arb_in(1).rd_data => r_rd_data_m1,
		arb_in(1).rdy_cnt => r_rdy_cnt_m1,
		arb_in(2).rd_data => r_rd_data_m2,
		arb_in(2).rdy_cnt => r_rdy_cnt_m2,
		
		mem_out.address => r_address_mem,
		mem_out.wr_data => r_wr_data_mem,
		mem_out.rd => r_rd_mem,
		mem_out.wr => r_wr_mem,
		mem_out.atomic => r_atomic,
		mem_out.cache => r_cache,
		mem_out.tm_cache => r_tm_cache,
		mem_out.tm_broadcast => r_tm_broadcast,
		mem_out.cinval => r_cinval,
		mem_in.rd_data => s_rd_data_mem,
		mem_in.rdy_cnt => s_rdy_cnt_mem
    );
 
	s_clk <= not s_clk after 5 ns;
	
	stim : process
	begin

 --Zugriff nacheinander von JOP0
		s_reset <= '1';
		s_rdy_cnt_mem <= "00";
		wait for 10 ns;
		s_reset <= '0';
		wait for 5 ns;
		s_rdy_cnt_mem <= "00";
		s_rd_m0 <= '1';
		s_address_m0 <= "00001001111111111111111"; -- 4ffff
		wait for 10 ns;
		s_rdy_cnt_mem <= "10";
		s_rd_m0 <= '0';
		wait for 10 ns;
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "10101010101010101010101010101010";
		s_rd_m0 <= '1';
		s_address_m0 <= "00001001111111111111110"; -- 4fffe
		wait for 10 ns;
		s_rdy_cnt_mem <= "10";
		s_rd_m0 <= '0';
		wait for 10 ns;
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01010101010101010101010101010101";
		wait for 30 ns;	

 --Zugriff nacheinander von JOP1
		
		s_rd_m1 <= '1';
		s_address_m1 <= "00001001111111111111111"; -- 4ffff
		wait for 10 ns;
		s_rdy_cnt_mem <= "10";
		s_rd_m1 <= '0';
		wait for 10 ns;
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "10101010101010101010101010101010";
		s_rd_m1 <= '1';
		s_address_m1 <= "00001001111111111111110"; -- 4fffe
		wait for 10 ns;
		s_rdy_cnt_mem <= "10";
		s_rd_m1 <= '0';
		wait for 10 ns;
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01010101010101010101010101010101";
		wait for 30 ns;
		
 --Zugriff nacheinander von JOP2
		
		s_rd_m2 <= '1';
		s_address_m2 <= "00001001111111111111111"; -- 4ffff
		wait for 10 ns;
		s_rdy_cnt_mem <= "10";
		s_rd_m2 <= '0';
		wait for 10 ns;
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "10101010101010101010101010101010";
		s_rd_m2 <= '1';
		s_address_m2 <= "00001001111111111111110"; -- 4fffe
		wait for 10 ns;
		s_rdy_cnt_mem <= "10";
		s_rd_m2 <= '0';
		wait for 10 ns;
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01010101010101010101010101010101";
		wait for 30 ns;

	-- pipelined JOP0 read

	  s_rd_m0 <= '1';
	  s_address_m0 <= "00001001111111111111111"; -- 4ffff
	  wait for 10 ns;
	  s_rdy_cnt_mem <= "10";
	  s_rd_m0 <= '0';
	  wait for 10 ns;
	  s_rdy_cnt_mem <= "01";
	  s_rd_m0 <= '1';
	  s_address_m0 <= "00001010000000000000000"; -- 50000
	  wait for 10 ns;
	  s_rd_m0 <= '0';
	  s_rdy_cnt_mem <= "10";
	  s_rd_data_mem <= "10101010101010101010101010101010";
	  wait for 10 ns;
	  s_rdy_cnt_mem <= "01";
	  s_rd_m0 <= '1';
	  s_address_m0 <= "00001001111111111111111"; -- 4ffff
	  wait for 10 ns;
	  s_rd_m0 <= '0';
	  s_rdy_cnt_mem <= "10";
	  s_rd_data_mem <= "11111111111111111111111111111111";
	  wait for 10 ns;
	  s_rdy_cnt_mem <= "01";
	  s_rd_m0 <= '1';
	  s_address_m0 <= "00001000000000000000000"; -- 40000
	  wait for 10 ns;
	  s_rd_m0 <= '0';
	  s_rdy_cnt_mem <= "10";
	  s_rd_data_mem <= "01010101010101010101010101010101";  
	  wait for 10 ns;
	  s_rdy_cnt_mem <= "01";
	  wait for 10 ns;
	  s_rdy_cnt_mem <= "00";
	  s_rd_data_mem <= "11111111111111111111111111111111";
	  wait for 30 ns;  

 -- read and write JOP0

		s_rd_m0 <= '1';
		s_address_m0 <= "00001001111111111111111"; -- 4ffff
		wait for 10 ns;
		s_rd_m0 <= '0';
		s_rdy_cnt_mem <= "10";
		wait for 10 ns;
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";	
		s_rd_data_mem <= "10101010101010101010101010101010";	  
		s_wr_m0 <= '1';
		s_wr_data_m0 <= "01010101010101010101010101010101";  -- 5555555
		s_address_m0 <= "00001001111111111111110"; -- 4fffe 
		wait for 10 ns;
		s_wr_m0 <= '0';
		s_rdy_cnt_mem <= "10";
		wait for 10 ns;
		s_rdy_cnt_mem <= "01";                          
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";   
		s_rd_m0 <= '1';
		s_address_m0 <= "00001001111111111111111"; -- 4ffff
		wait for 10 ns;
		s_rd_m0 <= '0';
		s_rdy_cnt_mem <= "10";
		wait for 10 ns;
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";	
		s_rd_data_mem <= "11111111111111111111111111111111";	
		wait for 30 ns;


	-- JOP0 and JOP1 simultaneous access
   s_rdy_cnt_mem <= "00";
   s_rd_m0 <= '1';
   s_rd_m1 <= '0';
   s_address_m0 <= "00001001111111111111111"; -- 4ffff
   wait for 10 ns;
   s_rdy_cnt_mem <= "10";
   s_rd_m0 <= '0';
   s_rd_m1 <= '0';
   wait for 10 ns;
   s_rdy_cnt_mem <= "01";
   s_rd_m1 <= '1';
   s_address_m1 <= "00001010000000000000000"; -- 50000
   wait for 10 ns;
   s_rd_m1 <= '0';
   s_rdy_cnt_mem <= "00";
   s_rd_data_mem <= "10101010101010101010101010101010";
   
   
   s_rd_m0 <= '1';
   s_address_m0 <= "00111111111111111111111"; -- fffff
   wait for 10 ns;
   s_rdy_cnt_mem <= "10";
   s_rd_m0 <= '0';
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
   s_rd_m1 <= '1';
   s_address_m1 <= "00001000000000000000000"; -- 40000
   wait for 10 ns;
   s_rd_m1 <= '0';
   s_rdy_cnt_mem <= "10";
   wait for 10 ns;
   s_rdy_cnt_mem <= "01";
   wait for 10 ns;
   s_rdy_cnt_mem <= "00";
   s_rd_data_mem <= "01010101010101010101010101010101";
   wait for 20 ns;
   
  
   s_rd_m0 <= '1';
   s_address_m0 <= "00001001111111111111111"; -- 4ffff
   wait for 10 ns;
   s_rdy_cnt_mem <= "10";
   s_rd_m0 <= '0';
   s_rd_m1 <= '1';
   s_address_m1 <= "00001010000000000000000"; -- 50000
   wait for 10 ns;
   s_rd_m1 <= '0';
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
   s_wr_m0 <= '1';
   s_address_m0 <= "00011001111111100011111"; -- 0CFF1F
   s_wr_data_m0 <= "11111111111111111111110000000000"; -- FFFFC00
   wait for 10 ns;
   s_wr_m0 <= '0';
   s_rdy_cnt_mem <= "10";
   wait for 10 ns;
   s_rdy_cnt_mem <= "01";	
   wait for 10 ns;
   s_rd_m1 <= '1';                                
   s_rdy_cnt_mem <= "00";                         
   s_address_m1 <= "00011010000110000011100"; --    
   wait for 10 ns;
   s_rd_m1 <= '0';
   s_rdy_cnt_mem <= "10";
   wait for 10 ns;
   s_rdy_cnt_mem <= "01";
   s_rd_m1 <= '1';
   s_address_m1 <= "00001010000000000000000"; -- 50000	
   wait for 10 ns;
   s_rd_m1 <= '0';
   s_rdy_cnt_mem <= "10";
   s_rd_data_mem <= "11111111111111111111111111111110";
   wait for 10 ns;
   s_rdy_cnt_mem <= "01";
   wait for 10 ns;
   s_rdy_cnt_mem <= "00";    
   s_rd_data_mem <= "01010101111111111111010101010101"; -- 55fff555
   wait for 40 ns;        

		-- gleichzeitiger Zugriff von allen 3CPUs
                                   
		s_rdy_cnt_mem <= "00";                             
		s_rd_m0 <= '1';                                   
		s_rd_m1 <= '1';                                   
		s_rd_m2 <= '1'; 
		s_address_m0 <= "00001001111111111111111"; -- 4ffff 
		s_address_m1 <= "00001010000000000000000"; -- 50000 
		s_address_m2 <= "00111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '0';                             
		s_rd_m2 <= '0';                                         
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "01";
		s_rd_m1 <= '1';
		s_address_m1 <= "00001000000000000000000"; -- 40000
		wait for 10 ns;  
		s_rd_m1 <= '0';
		s_rdy_cnt_mem <= "10";        
		s_rd_data_mem <= "00110000001100000011000000110000"; -- 404..                      
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..	
		s_rd_m1 <= '1';
		s_address_m1 <= "00000100010001000100010"; -- 02222   
		wait for 10 ns;	
		s_rd_m1 <= '0';
		s_rdy_cnt_mem <= "10";                                         
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
		s_rd_data_mem <= "01110111011101110111011101110111";
		wait for 30 ns;
		
		
		
		s_rd_m0 <= '1';                                   
		s_rd_m1 <= '1';                                   
		s_address_m0 <= "00000011110000111100001"; 
		s_address_m1 <= "00111111111111111111111";  -- FFFFF                                                  
		wait for 10 ns; 
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '0';                 
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";                             
		wait for 10 ns;
		s_rdy_cnt_mem <= "00";   		              
		s_rd_data_mem <= "01010101010101010101010101010101"; -- 5555..                                                           
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                                                               
		wait for 10 ns;                                                                      
		s_rdy_cnt_mem <= "01";                             
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "00";                             
		s_rd_data_mem <= "00110011001100110011001100110011"; -- 333..
		s_rd_m0 <= '1';                                   
		s_rd_m1 <= '1';                                   
		s_address_m0 <= "00000100010001000100011";
		s_address_m1 <= "00010101010101010101010";     
		wait for 10 ns;                                    
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '0';                 
		s_rdy_cnt_mem <= "10";                                   
		wait for 10 ns;                                                                                                      
		s_rdy_cnt_mem <= "01";                             
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "00";                             
		s_rd_data_mem <= "10101010101010101010101010101010"; -- AA..
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                                                               
		wait for 10 ns;                                                                      
		s_rdy_cnt_mem <= "01";                             
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01010101111111111111010101010101"; -- 55fff555
		wait for 80 ns;                  



		-- gleichzeitiger Zugriff von CPU1 und CPU2		
                                
		s_rdy_cnt_mem <= "00";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '1';                                   
		s_rd_m2 <= '1';  
		s_address_m1 <= "00001010000000000000000"; -- 50000 
		s_address_m2 <= "00111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '0';                             
		s_rd_m2 <= '0';                                         
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;         
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..	
		wait for 80 ns;	


		-- gleichzeitiger Zugriff von CPU0 und CPU2		
                           
		s_rd_m0 <= '1';                                   
		s_rd_m1 <= '0';                                   
		s_rd_m2 <= '1';  
		s_address_m0 <= "00001010000000000000000"; -- 50000 
		s_address_m2 <= "00111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '0';                             
		s_rd_m2 <= '0';                                         
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;         
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..	
		wait for 80 ns;	


		-- gleichzeitiger Zugriff von CPU0 und CPU1		
                                  
		s_rdy_cnt_mem <= "00";                             
		s_rd_m0 <= '1';                                   
		s_rd_m1 <= '1';                                   
		s_rd_m2 <= '0';  
		s_address_m0 <= "00001010000000000000000"; -- 50000 
		s_address_m1 <= "00111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '0';                             
		s_rd_m2 <= '0';                                         
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;         
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..	
		wait for 80 ns;	

--------------------------------------------------------------------------------------------		

		-- gleichzeitiger Zugriff von CPU1 und CPU2 dann CPU0
                               
		s_rdy_cnt_mem <= "00";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '1';                                   
		s_rd_m2 <= '1';  
		s_address_m1 <= "00001010000000000000000"; -- 50000 
		s_address_m2 <= "00111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                             
		s_rd_m0 <= '1';
		s_address_m0 <= "00001000000000000000000"; -- 40000                                       
		s_rd_m1 <= '0';                             
		s_rd_m2 <= '0';                                         
		wait for 10 ns;                                    
		s_rd_m0 <= '0';                              
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rd_m0 <= '0';                                         
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..	
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
 		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 20 ns;
		
		-- gleichzeitiger Zugriff von CPU1 und CPU2 dann CPU0		                                   
                                
		s_rdy_cnt_mem <= "00";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '1';                                   
		s_rd_m2 <= '1';  
		s_address_m1 <= "00001010000000000000000"; -- 50000 
		s_address_m2 <= "00111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '0';                             
		s_rd_m2 <= '0';                                         
		wait for 10 ns;                                    
		s_rd_m0 <= '1';
		s_address_m0 <= "00001000000000000000000"; -- 40000                                  
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rd_m0 <= '0';                                         
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..	
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
 		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 15 ns;		
		
		-- gleichzeitiger Zugriff von CPU1 und CPU2 dann CPU0		                                   
		s_rdy_cnt_mem <= "00";                             
		wait for 10 ns;                                    
		s_reset <= '0';                                    
		wait for 5 ns;                                     
		s_rdy_cnt_mem <= "00";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '1';                                   
		s_rd_m2 <= '1';  
		s_address_m1 <= "00001010000000000000000"; -- 50000 
		s_address_m2 <= "00111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '0';                             
		s_rd_m2 <= '0';                                         
		wait for 10 ns;                                                                
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rd_m0 <= '1';
		s_address_m0 <= "00001000000000000000000"; -- 40000                                            
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;
		s_rd_m0 <= '0';           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..	
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
 		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 15 ns;		
		
		-- gleichzeitiger Zugriff von CPU1 und CPU2 dann CPU0		                                   
		s_rdy_cnt_mem <= "00";                             
		wait for 10 ns;                                    
		s_reset <= '0';                                    
		wait for 5 ns;                                     
		s_rdy_cnt_mem <= "00";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '1';                                   
		s_rd_m2 <= '1';  
		s_address_m1 <= "00001010000000000000000"; -- 50000 
		s_address_m2 <= "00111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                             
		s_rd_m0 <= '0';                                   
		s_rd_m1 <= '0';                             
		s_rd_m2 <= '0';                                         
		wait for 10 ns;                                                                
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;                                          
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;
		s_rd_m0 <= '1';
		s_address_m0 <= "00001000000000000000000"; -- 40000           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;  
		s_rd_m0 <= '0';              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..	
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
 		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 80 ns;		

		-- CPU1 and CPU2 and later CPU3
		                                
		s_rdy_cnt_mem <= "00";                             
		s_rd_m0 <= '1';                                   
		s_rd_m1 <= '1';                                   
		s_rd_m2 <= '0';  
		s_address_m1 <= "00001010000000000000000"; -- 50000 
		s_address_m2 <= "00111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                                                                  
		s_rd_m1 <= '0';                             
		s_rd_m0 <= '0';                                         
		wait for 10 ns;                                                                 
		s_rdy_cnt_mem <= "01";
		wait for 10 ns;
		s_rd_m2 <= '1';
		s_address_m2 <= "00001000000000000000000"; -- 40000                                           
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;           
		s_rd_m2 <= '0';  
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
		s_rd_data_mem <= "11110000111100001111000011110000"; -- F0F..	
		wait for 10 ns;           
		s_rdy_cnt_mem <= "10";                             
		wait for 10 ns;              
		s_rdy_cnt_mem <= "01";  		                     
		wait for 10 ns;              
		s_rdy_cnt_mem <= "00";
			s_rd_data_mem <= "01110000011100000111000001110000"; -- 707..                    
		wait for 10 ns;


		s_rd_m1 <= '1';
		s_address_m1 <= "00000100010001000100010"; -- 02222   
		wait for 10 ns;	
		s_rd_m1 <= '0';
		s_rdy_cnt_mem <= "10";                                         
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
		s_rd_data_mem <= "01110111011101110111011101110111";
		wait for 295 ns;
    

	end process stim;
	
end test;



















