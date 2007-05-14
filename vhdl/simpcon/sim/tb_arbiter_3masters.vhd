

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
	signal cpu_cnt : integer := 3;
	
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
  
  signal s_address_m3	: std_logic_vector(addr_bits-1 downto 0);
	signal s_wr_data_m3	: std_logic_vector(31 downto 0);
  signal s_rd_m3			: std_logic := '0';		
  signal s_wr_m3			: std_logic := '0';
	
	signal s_rd_data_mem	: std_logic_vector(31 downto 0);
	signal s_rdy_cnt_mem	: unsigned(1 downto 0);
	
	-- Response Signals
	signal r_rd_data_jop	: std_logic_vector(31 downto 0);
	signal r_rdy_cnt_jop	: unsigned(1 downto 0);
	
	signal r_rd_data_vga	: std_logic_vector(31 downto 0);
	signal r_rdy_cnt_vga	: unsigned(1 downto 0);
		
	signal r_rd_data_m3	: std_logic_vector(31 downto 0);
	signal r_rdy_cnt_m3	: unsigned(1 downto 0);
	
	signal r_address_mem	: std_logic_vector(addr_bits-1 downto 0);
	signal r_wr_data_mem	: std_logic_vector(31 downto 0);
	signal r_rd_mem			: std_logic;
	signal r_wr_mem			: std_logic;

begin
	arbiter1: arbiter 
	generic map (
		addr_bits => 21,
		cpu_cnt => 3)
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
		arb_out(2).address => s_address_m3,
		arb_out(2).wr_data => s_wr_data_m3,
		arb_out(2).rd => s_rd_m3,
		arb_out(2).wr => s_wr_m3,
		 
		arb_in(0).rd_data => r_rd_data_vga,
		arb_in(0).rdy_cnt => r_rdy_cnt_vga,
		arb_in(1).rd_data => r_rd_data_jop,
		arb_in(1).rdy_cnt => r_rdy_cnt_jop,
		arb_in(2).rd_data => r_rd_data_m3,
		arb_in(2).rdy_cnt => r_rdy_cnt_m3,
		
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
--   s_reset <= '1';
--   s_rdy_cnt_mem <= "00";
--   wait for 10 ns;
--   s_reset <= '0';
--   wait for 5 ns;
--   s_rdy_cnt_mem <= "00";
--   s_rd_jop <= '1';
--   s_rd_vga <= '0';
--   s_address_jop <= "001001111111111111111"; -- 4ffff
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "10";
--   s_rd_jop <= '0';
--   s_rd_vga <= '0';
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "01";
--   s_rd_vga <= '1';
--   s_address_vga <= "001010000000000000000"; -- 50000
--   wait for 10 ns;
--   s_rd_vga <= '0';
--   s_rdy_cnt_mem <= "00";
--   s_rd_data_mem <= "10101010101010101010101010101010";
--   
--   
--   s_rd_jop <= '1';
--   s_address_jop <= "111111111111111111111"; -- fffff
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "10";
--   s_rd_jop <= '0';
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "01";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "00";
--   s_rd_data_mem <= "11111111111111111111111111111111";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "10";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "01";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "00";
--   s_rd_data_mem <= "00000000000000000000000000000000";
--   
--   
--   
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "10";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "01";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "00";
--   s_rd_data_mem <= "11111111111111111111111111111110"; 
--   s_rd_vga <= '1';
--   s_address_vga <= "001000000000000000000"; -- 40000
--   wait for 10 ns;
--   s_rd_vga <= '0';
--   s_rdy_cnt_mem <= "10";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "01";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "00";
--   s_rd_data_mem <= "01010101010101010101010101010101";
--   wait for 20 ns;
--   
--  
--   s_rd_jop <= '1';
--   s_address_jop <= "001001111111111111111"; -- 4ffff
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "10";
--   s_rd_jop <= '0';
--   s_rd_vga <= '1';
--   s_address_vga <= "001010000000000000000"; -- 50000
--   wait for 10 ns;
--   s_rd_vga <= '0';
--   s_rdy_cnt_mem <= "01";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "00";
--   s_rd_data_mem <= "10101010101010101010101010101010";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "10";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "01";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "00";
--   s_rd_data_mem <= "11111111111111111111111111111111";
--   
--   
--   wait for 20 ns;
--   s_wr_jop <= '1';
--   s_address_jop <= "011001111111100011111"; -- 0CFF1F
--   s_wr_data_jop <= "11111111111111111111110000000000"; -- FFFFC00
--   wait for 10 ns;
--   s_wr_jop <= '0';
--   s_rdy_cnt_mem <= "10";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "01";	
--   wait for 10 ns;
--   s_rd_vga <= '1';                                
--   s_rdy_cnt_mem <= "00";                         
--   s_address_vga <= "011010000110000011100"; --    
--   wait for 10 ns;
--   s_rd_vga <= '0';
--   s_rdy_cnt_mem <= "10";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "01";
--   s_rd_vga <= '1';
--   s_address_vga <= "001010000000000000000"; -- 50000	
--   wait for 10 ns;
--   s_rd_vga <= '0';
--   s_rdy_cnt_mem <= "10";
--   s_rd_data_mem <= "11111111111111111111111111111110";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "01";
--   wait for 10 ns;
--   s_rdy_cnt_mem <= "00";    
--   s_rd_data_mem <= "01010101111111111111010101010101"; -- 55fff555
--   wait for 45 ns;        
 
		-- gleichzeitiger Zugriff
		s_reset <= '1';                                    
		s_rdy_cnt_mem <= "00";                             
		wait for 10 ns;                                    
		s_reset <= '0';                                    
		wait for 5 ns;                                     
		s_rdy_cnt_mem <= "00";                             
		s_rd_jop <= '1';                                   
		s_rd_vga <= '1';                                   
		s_rd_m3 <= '1'; 
		s_address_jop <= "001001111111111111111"; -- 4ffff 
		s_address_vga <= "001010000000000000000"; -- 50000 
		s_address_m3 <= "111111111111111111111"; -- fffff 
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "10";                             
		s_rd_jop <= '0';                                   
		s_rd_vga <= '0';                             
		s_rd_m3 <= '0';                                         
		wait for 10 ns;                                    
		s_rdy_cnt_mem <= "01";
		s_rd_vga <= '1';
		s_address_vga <= "001000000000000000000"; -- 40000
		wait for 10 ns;  
		s_rd_vga <= '0';
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
		s_rd_vga <= '1';
		s_address_vga <= "000100010001000100010"; -- 02222   
		wait for 10 ns;	
		s_rd_vga <= '0';
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
		
		
		
		
		
		
		
		
		
		s_rd_jop <= '1';                                   
		s_rd_vga <= '1';                                   
		s_address_jop <= "000011110000111100001"; 
		s_address_vga <= "111111111111111111111";  -- FFFFF                                                  
		wait for 10 ns; 
		s_rd_jop <= '0';                                   
		s_rd_vga <= '0';                 
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
		s_rd_jop <= '1';                                   
		s_rd_vga <= '1';                                   
		s_address_jop <= "000100010001000100011";
		s_address_vga <= "010101010101010101010";     
		wait for 10 ns;                                    
		s_rd_jop <= '0';                                   
		s_rd_vga <= '0';                 
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

	end process stim;
	
end test;



















