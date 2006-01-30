-------------------------------------------------------------------------------
--
-- Project:	<Floating Point Unit Core>
--  	
-- Description: test bench for the FPU core
-------------------------------------------------------------------------------
--
--				100101011010011100100
--				110000111011100100000
--				100000111011000101101
--				100010111100101111001
--				110000111011101101001
--				010000001011101001010
--				110100111001001100001
--				110111010000001100111
--				110110111110001011101
--				101110110010111101000
--				100000010111000000000
--
-- 	Author:		 Jidan Al-eryani 
-- 	E-mail: 	 jidan@gmx.net
--
--  Copyright (C) 2006
--
--	This source file may be used and distributed without        
--	restriction provided that this copyright statement is not   
--	removed from the file and that any derivative work contains 
--	the original copyright notice and the associated disclaimer.
--                                                           
--		THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY     
--	EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED   
--	TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS   
--	FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL THE AUTHOR      
--	OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,         
--	INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES    
--	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE   
--	GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR        
--	BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  
--	LIABILITY, WHETHER IN  CONTRACT, STRICT LIABILITY, OR TORT  
--	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT  
--	OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE         
--	POSSIBILITY OF SUCH DAMAGE. 
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use ieee.math_real.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_misc.all;




entity tb_fpu is
end tb_fpu;

architecture rtl of tb_fpu is

component fpu 
    port (
        clk_i       	: in std_logic;
        opa_i       	: in std_logic_vector(31 downto 0);   
        opb_i       	: in std_logic_vector(31 downto 0);
        fpu_op_i		: in std_logic_vector(2 downto 0);
        rmode_i 		: in std_logic_vector(1 downto 0);  
        output_o    	: out std_logic_vector(31 downto 0);
		ine_o 			: out std_logic;
        overflow_o  	: out std_logic;
        underflow_o 	: out std_logic;
        div_zero_o  	: out std_logic;
        inf_o			: out std_logic;
        zero_o			: out std_logic;
        qnan_o			: out std_logic;
        snan_o			: out std_logic;
        start_i	  		: in  std_logic;
        ready_o 		: out std_logic	
	);   
end component;

component fpu_y
   port( 
      clk         : in     std_logic  ;
      fpu_op      : in     std_logic_vector (2 downto 0) ;
      opa         : in     std_logic_vector (31 downto 0) ;
      opb         : in     std_logic_vector (31 downto 0) ;
      rmode       : in     std_logic_vector (1 downto 0) ;
      div_by_zero : out    std_logic  ;
      fpout       : out    std_logic_vector (31 downto 0) ;
      ine         : out    std_logic  ;
      inf         : out    std_logic  ;
      overflow    : out    std_logic  ;
      qnan        : out    std_logic  ;
      snan        : out    std_logic  ;
      underflow   : out    std_logic  ;
      zero        : out    std_logic
      );
end component;

signal clk_i : std_logic:= '0';
signal opa_i, opb_i : std_logic_vector(31 downto 0);
signal fpu_op_i		: std_logic_vector(2 downto 0);
signal rmode_i : std_logic_vector(1 downto 0);
signal output_o : std_logic_vector(31 downto 0);
signal start_i, ready_o : std_logic ; 
signal ine_o, overflow_o, underflow_o, div_zero_o, inf_o, zero_o, qnan_o, snan_o: std_logic;

signal fpout_y : std_logic_vector (31 downto 0) ;
signal ine_y, inf_y, overflow_y, qnan_y, snan_y, underflow_y, zero_y, div_by_zero_y : std_logic ;

constant CLK_PERIOD :time := 10 ns; -- period of clk period

begin


    -- instantiate the fpu
    i_fpu: fpu port map (
			clk_i => clk_i,
			opa_i => opa_i,
			opb_i => opb_i,
			fpu_op_i =>	fpu_op_i,
			rmode_i => rmode_i,	
			output_o => output_o,  
			ine_o => ine_o,
			overflow_o => overflow_o,
			underflow_o => underflow_o,		
        	div_zero_o => div_zero_o,
        	inf_o => inf_o,
        	zero_o => zero_o,		
        	qnan_o => qnan_o, 		
        	snan_o => snan_o,
        	start_i => start_i,
        	ready_o => ready_o);		
			
		
		-- instantiate the fpu
    i_fpu_y: fpu_y port map (
        clk => clk_i,
        fpu_op => fpu_op_i,
        opa => opa_i,
        opb => opb_i,         
        rmode => rmode_i,     
        div_by_zero => div_by_zero_y, 
        fpout => fpout_y,       
        ine => ine_y,          
        inf => inf_y,         
        overflow => overflow_y,   
        qnan => qnan_y,        
        snan => snan_y,       
        underflow => underflow_y,  
        zero => zero_y);	


    ---------------------------------------------------------------------------
    -- toggle clock
    ---------------------------------------------------------------------------
    clk_i <= not(clk_i) after 5 ns;


    verify : process  
    begin
    
--************* Add/Substract Test Vectors****************
    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000100000000000000000000000"; 
			opb_i <= "00000000100000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;

    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000110000000000000000000010"; 
			opb_i <= "00000000100000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "10000000000000000000000000000111"; 
			opb_i <= "10000000001111111111111111111000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;

    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000011111110100011111000101000"; 
			opb_i <= "00000111000101111000001111100110"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;		
			
    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "10001100111110100011111000101000"; 
			opb_i <= "10001100111101111000001111100110"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;		

    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000011111110100011111000101000"; 
			opb_i <= "01111111110000000000000000000001"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000011111110100011111000101000"; 
			opb_i <= "01111111100000000000000000000001"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000100100000000000000000010"; 
			opb_i <= "00000000100000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00100011001000000000000000000000"; 
			opb_i <= "00101001100100000000000000000100"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "01110011011100000000000000000011"; 
			opb_i <= "01101100100000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000000000011111111111111111"; 
			opb_i <= "00000000000000000000000000111111"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000111111111111111111111111111"; 
			opb_i <= "00000000000001000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "01110011011100000000000000000011"; 
			opb_i <= "01101100100000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
    		wait for CLK_PERIOD; start_i <= '1'; -- MIN #1
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "00000000000000000000000000000000"; 
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
			
    		wait for CLK_PERIOD; start_i <= '1'; -- MIN #2
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "00000000000000000000000000000000"; 
			opb_i <= "00000000000000000000000000000001"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;		
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- MAX
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111111011111111111111111111111"; 
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- INF
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111111100000000000000000000000"; 
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- QNaN
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111111111111111111111111111111"; 
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- SNaN
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111001111111111111111111111111"; 
			opb_i <= "01111111100000000000000000000001"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- inf + inf
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111111100000000000000000000000"; 
			opb_i <= "01111111100000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- -inf + inf
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "11111111100000000000000000000000"; 
			opb_i <= "01111111100000000000000000000000"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o(30 downto 0)=fpout_y(30 downto 0) and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
			--misc.
			--			  seeeeeeeefffffffffffffffffffffff
    		wait for CLK_PERIOD; start_i <= '1'; 
    		opa_i <= "11110011011100000000000000000011"; 
			opb_i <= "00000000111111111111111111111111"; 
			fpu_op_i <= "000";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;		
			
			
		-- substract--------------------
			
			    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000100000000000000000000000"; 
			opb_i <= "00000000100000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			-------
    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000110000000000000000000010"; 
			opb_i <= "00000000100000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "10000000000000000000000000000111"; 
			opb_i <= "10000000001111111111111111111000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;

    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000011111110100011111000101000"; 
			opb_i <= "00000111000101111000001111100110"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;		
			
    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "10001100111110100011111000101000"; 
			opb_i <= "10001100111101111000001111100110"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;		

    	--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000011111110100011111000101000"; 
			opb_i <= "01111111110000000000000000000001"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000011111110100011111000101000"; 
			opb_i <= "01111111100000000000000000000001"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;		
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000100100000000000000000010"; 
			opb_i <= "00000000100000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00100011001000000000000000000000"; 
			opb_i <= "00101001100100000000000000000100"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "01110011011100000000000000000011"; 
			opb_i <= "01101100100000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000000000011111111111111111"; 
			opb_i <= "00000000000000000000000000111111"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000111111111111111111111111111"; 
			opb_i <= "00000000000001000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--			  seeeeeeeefffffffffffffffffffffff
    	wait for CLK_PERIOD; start_i <= '1'; opa_i <= "01110011011100000000000000000011"; 
			opb_i <= "01101100100000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;

    		wait for CLK_PERIOD; start_i <= '1'; -- MIN #1
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "00000000000000000000000000000000"; 
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
			
    		wait for CLK_PERIOD; start_i <= '1'; -- MIN #2
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "00000000000000000000000000000000"; 
			opb_i <= "00000000000000000000000000000001"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;		
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- MAX
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111111011111111111111111111111"; 
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- INF
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111111100000000000000000000000"; 
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- QNaN
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111111111111111111111111111111"; 
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- SNaN
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111001111111111111111111111111"; 
			opb_i <= "01111111100000000000000000000001"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- inf + inf
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "01111111100000000000000000000000"; 
			opb_i <= "01111111100000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
			    		wait for CLK_PERIOD; start_i <= '1'; -- -inf + inf
    		--		  seeeeeeeefffffffffffffffffffffff	
    		opa_i <= "11111111100000000000000000000000"; 
			opb_i <= "01111111100000000000000000000000"; 
			fpu_op_i <= "001";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o(30 downto 0)=fpout_y(30 downto 0) and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
							
		
--************* Multiply Test Vectors************************************************************
		    
		    -- round to nearset even 
		    --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000100000000000000000000000"; 
			opb_i <= "00000000100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;	
			
			 --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01000000100000000000000000000000"; 
			opb_i <= "01000000100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- underflow = 1 when tiny(2^-127) and inexact
			
			-- 2^-127x0.1 * 2^0x1.0 = 2^-127x0.1 (-127 in dn = -126)
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000010000000000000000000000"; 
			opb_i <= "00111111100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- 2^-127x0.1 * 2^0x1.11 = 2^-127x0.1110
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000010000000000000000000000"; 
			opb_i <= "00111111111000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;

			-- 2^-127x0.1 * 2^-1x1.0 = 2^-127x0.01
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000010000000000000000000000"; 
			opb_i <= "00111111000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- 2^-127x0.1 * 2^-22x1.0 = 2^-127x0.0..01
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000010000000000000000000000"; 
			opb_i <= "00110100100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- 2^-127x0.1 * 2^-45x1.0 = 2^-127x0.0..00
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000010000000000000000000000"; 
			opb_i <= "00101001000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;

			-- 2^-127x0.1 * 2^-46x1.0 = 2^-127x0.0..00
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000010000000000000000000000"; 
			opb_i <= "00101000100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;

			-- 2^-127x0.1 * 2^-47x1.0 = 2^-127x0.0..00
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000010000000000000000000000"; 
			opb_i <= "00101000000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- 2^-126x1.0 * 2^-1x1.0 = 2^-127x1.0
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000100000000000000000000000"; 
			opb_i <= "00111111000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- 2^-126x1.0 * 2^-2x1.0 = 2^-127x0.1
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000100000000000000000000000"; 
			opb_i <= "00111110100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- 2^-126x1.0 * 2^-46x1.0 = 2^-127x0.0..0
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000100000000000000000000000"; 
			opb_i <= "00101000100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- 2^-126x1.0 * 2^-47x1.0 = 2^-127x0.0..0 - shr 46
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000100000000000000000000000"; 
			opb_i <= "00101000000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- 2^-126x1.0 * 2^-48x1.0 = 2^-127x0.0..0 - shr 46
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000100000000000000000000000"; 
			opb_i <= "00100111100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- e^128 x 1
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000000"; 
			opb_i <= "00111110100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			-- e^127 x 1
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000000"; 
			opb_i <= "00111110000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			-- and underflow_o=underflow_y
			report "Error!!!"
			severity failure;
			
			-- e^126 x 1
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000000"; 
			opb_i <= "00111101100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			 --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000111111100000000000000000000"; 
			opb_i <= "00000000100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			 --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000001111111111111111111111"; 
			opb_i <= "00000000000000000000000000010000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;	
			
			 --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; opa_i <= "10000000011101111111111111111101"; 
			opb_i <= "00111010000000000000000000000001"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- 2^-127x0.11 * 2^-0x1.0 = 2^-127x0.11
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000011000000000000000000000"; 
			opb_i <= "00111111100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;

			-- 2^-127x0.11 * 2^-1x1.0 = 2^-127x0.11
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000011000000000000000000000"; 
			opb_i <= "00111111000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;


			-- 2^-127x0.11 * 2^-2x1.11 
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000011000000000000000000000"; 
			opb_i <= "00111110111000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;
			
			
			-- 2^-127x0.11 * 2^-0x1.11 
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000011000000000000000000000"; 
			opb_i <= "00111111111000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y  and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y 
			report "Error!!!"
			severity failure;

			 --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000011110000000000000000000"; 
			opb_i <= "00111100111110000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			 --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; opa_i <= "01111111100000000000000000000000"; 
			opb_i <= "00000111000101111000001111100110"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;	
			
			 --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000011111110100011111000101000"; 
			opb_i <= "00000111000101111000001111100110"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			 --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; opa_i <= "00000000000000000000000000000000"; 
			opb_i <= "00000111000101111000001111100110"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;	
			
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000011000000000000000000000"; 
			opb_i <= "00111111111000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000110000000000000000000000"; 
			opb_i <= "00111111000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
			-- inf
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01111111110000000000000000000000"; 
			opb_i <= "00000000000000000000000000000001"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			
			-- ******* check rounding ********---
			
			--Round to nearst even
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000010000000000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000110000000000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001110000000000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000010000001000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000001000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000010000000000000000000000..1
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000001"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000000000000000000000000000..1
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000001"; 
			opb_i <= "00011001000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- round up
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000010000000000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "10";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000110000000000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001110000000000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "10";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000010000001000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000001000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "10";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 0000000000000000000000000100000000000000000000001
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000001"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "10";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000000000000000000000000000..1
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000001"; 
			opb_i <= "00011001000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "10";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;


			-- round down
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000010000000000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "11";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000110000000000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001110000000000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "11";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000010000001000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000001000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "11";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 0000000000000000000000000100000000000000000000001
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000001"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "11";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000000000000000000000000000..1
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000001"; 
			opb_i <= "00011001000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "11";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			
			-- round to zero
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000010000000000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "01";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000110000000000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001110000000000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "01";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;

			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000010000001000000000000000
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000001000000000000000"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "01";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 0000000000000000000000000100000000000000000000001
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000001"; 
			opb_i <= "00110010100000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "01";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			-- 			xx00000000000000000000000xxxxxxxxxxxxxxxxxxxxxxx
			-- fract2a= 000000000000000000000000000000000000000000000000..1
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000001100000000000000000000001"; 
			opb_i <= "00011001000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "01";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y and underflow_y=underflow_o
			report "Error!!!"
			severity failure;
			
			
			-- inf 2^100x1.0 * 2^128x1.0 =
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01110001100000000000000000000000"; 
			opb_i <= "01001101100000000000000000000001"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			-- inf * 0
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01111111110000000000000000000000"; 
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			-- Qnan * n
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01111111110000000000000000000001"; 
			opb_i <= "00000010000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			-- Snan * n
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01111111100000000000000000000001"; 
			opb_i <= "00000010000000000000000000000000"; 
			fpu_op_i <= "010";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			

--************* Division Test Vectors************************************************************	

		    -- round to nearset even 
		    --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01000001101010000000000000000000"; --21 
			opb_i <= "01000000010000000000000000000000"; --3
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	

		    --		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01000001111111111111111111111111";  
			opb_i <= "01000000000000000000000000000000"; 
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00111111100000000000000000000000";  
			opb_i <= "01000000010000000000000000000000"; 
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			-- 0 / x
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000000000000000000000000000";  
			opb_i <= "01000000010000000000000000000000"; 
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			-- x / 0
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01000000000000000000000000000000";  
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			
			-- overflow
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01000000000000000000000000000000";  
			opb_i <= "00000000010000000000000000000000"; 
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;	
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000010100000000000000000000";  
			opb_i <= "00000100010000000000000000000000"; 
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "10000000010100000000000000000000";  
			opb_i <= "00000000010000000000000000011000"; 
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o=fpout_y and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
			-- inf / inf
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01111111111111111111111111111111";  
			opb_i <= "01111111111111111111111111111111"; 
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o(30 downto 0)=fpout_y(30 downto 0) and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
			
						-- 0 / 0
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000000000000000000000000000";  
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "011";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o(30 downto 0)=fpout_y(30 downto 0) and ine_o=ine_y and overflow_o=overflow_y and underflow_o=underflow_y and inf_o=inf_y and zero_y=zero_o and div_zero_o=div_by_zero_y and qnan_o=qnan_y and snan_o=snan_y
			report "Error!!!"
			severity failure;
	
			
--************* Square-Root Test Vectors************************************************************

			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01000001000100000000000000000000";  --9
			opb_i <= "00000000000000000000000000000000"; 
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o= "01000000010000000000000000000000" and ine_o='0' and 
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' 
			and snan_o='0'
			report "Error!!!"
			severity failure;		
			
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01000001001000000000000000000000";  --10 
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="01000000010010100110001011000010" and ine_o='1' and 
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' and snan_o='0'
			report "Error!!!"
			severity failure;	
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01010001011011011000111001100111";  
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="01001000011101101001101100010011" and ine_o='1' and 
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' and snan_o='0'
			report "Error!!!"
			severity failure;
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01000001100000000000000000000000";  --16
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="01000000100000000000000000000000" and ine_o='0' and 
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' and snan_o='0'
			report "Error!!!"
			severity failure;
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01000000110000000000000000000000";  --6
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="01000000000111001100010001110001" and ine_o='1' and 
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' and snan_o='0'
			report "Error!!!"
			severity failure;
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01001101110110010011101101001010";  --455567687
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="01000110101001101100000000010000" and ine_o='1' and 
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' and snan_o='0'
			report "Error!!!"
			severity failure;
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "11001101110110010011101101001010";  -- - 455567687
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="11111111110000000000000000000001" and ine_o='0' and 
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='1' and snan_o='0'
			report "Error!!!"
			severity failure;
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000000000000000000000000001";  --MIN 
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="00011010001101010000010011110011" and ine_o='1' and
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' 
			and snan_o='0'
			report "Error!!!"
			severity failure;	

			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01111111011111111111111111111111";  --MAX
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="01011111011111111111111111111111" and ine_o='1' and
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' 
			and snan_o='0'
			report "Error!!!"
			severity failure;	
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "00000000000000000000000000000000";  -- +0
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="00000000000000000000000000000000" and ine_o='0' and
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' 
			and snan_o='0'
			report "Error!!!"
			severity failure;
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "10000000000000000000000000000000";  -- -0
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="10000000000000000000000000000000" and ine_o='0' and
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='0' 
			and snan_o='0'
			report "Error!!!"
			severity failure;	
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01111111100000000000000000000000";  -- inf
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="01111111100000000000000000000000" and ine_o='0' and
			overflow_o='0' and underflow_o='0' and inf_o='1' and div_zero_o='0' and qnan_o='0' 
			and snan_o='0'
			report "Error!!!"
			severity failure;	
			
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01111111110000000000000000000001";  -- qnan
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="01111111110000000000000000000001" and ine_o='0' and
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='1' 
			and snan_o='0'
			report "Error!!!"
			severity failure;	
			
			--		  seeeeeeeefffffffffffffffffffffff
		    wait for CLK_PERIOD; start_i <= '1'; 
		    opa_i <= "01111111100000000000000000000001";  -- snan
			fpu_op_i <= "100";
			rmode_i <= "00";
			wait for CLK_PERIOD; start_i <= '0'; wait until ready_o='1';
			assert output_o="01111111110000000000000000000001" and ine_o='0' and
			overflow_o='0' and underflow_o='0' and inf_o='0' and div_zero_o='0' and qnan_o='1' and snan_o='1'
			report "Error!!!"
			severity failure;



			assert false
			report "Success!!!"
			severity failure;	
				
		


    	wait;

    end process verify;

end rtl;