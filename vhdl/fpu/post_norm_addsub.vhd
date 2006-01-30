-------------------------------------------------------------------------------
--
-- Project:	<Floating Point Unit Core>
--  	
-- Description: post-normalization entity for the addition/subtraction unit
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

library ieee ;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_misc.all;

library work;
use work.fpupack.all;

entity post_norm_addsub is
	port(
			clk_i 			: in std_logic;
			opa_i 			: in std_logic_vector(FP_WIDTH-1 downto 0);
			opb_i 			: in std_logic_vector(FP_WIDTH-1 downto 0);
			fract_28_i		: in std_logic_vector(FRAC_WIDTH+4 downto 0);	-- carry(1) & hidden(1) & fraction(23) & guard(1) & round(1) & sticky(1)
			exp_i			: in std_logic_vector(EXP_WIDTH-1 downto 0);
			sign_i			: in std_logic;
			fpu_op_i		: in std_logic;
			rmode_i			: in std_logic_vector(1 downto 0);
			output_o		: out std_logic_vector(FP_WIDTH-1 downto 0);
			ine_o			: out std_logic
		);
end post_norm_addsub;


architecture rtl of post_norm_addsub is


signal s_opa_i, s_opb_i 	: std_logic_vector(FP_WIDTH-1 downto 0);
signal s_fract_28_i			: std_logic_vector(FRAC_WIDTH+4 downto 0);	
signal s_exp_i				: std_logic_vector(EXP_WIDTH-1 downto 0);
signal s_sign_i				: std_logic;
signal s_fpu_op_i			: std_logic;
signal s_rmode_i			: std_logic_vector(1 downto 0);
signal s_output_o			: std_logic_vector(FP_WIDTH-1 downto 0);
signal s_ine_o 				: std_logic;
signal s_overflow			: std_logic;
	
	
signal s_shr1, s_shr2, s_shl : std_logic;

signal s_expr1_9, s_expr2_9, s_expl_9	: std_logic_vector(EXP_WIDTH downto 0);
signal s_exp_shr1, s_exp_shr2, s_exp_shl : std_logic_vector(EXP_WIDTH-1 downto 0);

signal s_fract_shr1, s_fract_shr2, s_fract_shl	: std_logic_vector(FRAC_WIDTH+4 downto 0);
signal s_zeros	: std_logic_vector(5 downto 0);
signal shl_pos: std_logic_vector(5 downto 0);

signal s_fract_1, s_fract_2	: std_logic_vector(FRAC_WIDTH+4 downto 0);
signal s_exp_1, s_exp_2 : std_logic_vector(EXP_WIDTH-1 downto 0);

signal s_fract_rnd : std_logic_vector(FRAC_WIDTH+4 downto 0);	
signal s_roundup : std_logic;
	
signal s_infa, s_infb : std_logic;
signal s_nan_in, s_nan_op, s_nan_a, s_nan_b, s_nan_sign : std_logic;
	

begin
	
	-- Input Register
	--process(clk_i)
	--begin
	--	if rising_edge(clk_i) then	
			s_opa_i <= opa_i;
			s_opb_i <= opb_i;
			s_fract_28_i <= fract_28_i;
			s_exp_i <= exp_i;
			s_sign_i <= sign_i;
			s_fpu_op_i <= fpu_op_i;
			s_rmode_i <= rmode_i;
	--	end if;
	--end process;	

	-- Output Register
	--process(clk_i)
	--begin
	--	if rising_edge(clk_i) then	
			output_o <= s_output_o;
			ine_o <= s_ine_o;
	--	end if;
	--end process;
	
		
	-- check if shifting is needed
	s_shr1 <= s_fract_28_i(27);
	s_shl <= '1' when s_fract_28_i(27 downto 26)="00" and s_exp_i /= "00000000" else '0';
	
	-- stage 1a: right-shift (when necessary)
	s_expr1_9 <= "0"&s_exp_i + "000000001";
	s_fract_shr1 <= shr(s_fract_28_i, "1");
	s_exp_shr1 <= s_expr1_9(7 downto 0);
	
	-- stage 1b: left-shift (when necessary)
	
	process(clk_i)
	begin
		if rising_edge(clk_i) then
			-- count the leading zero's of fraction, needed for left-shift	
			s_zeros <= count_l_zeros(s_fract_28_i(26 downto 1));
		end if;
	end process;
	
	s_expl_9 <= ("0"&s_exp_i) - ("000"&s_zeros);
	shl_pos <= "000000" when s_exp_i="00000001" else s_zeros;
						 
	s_fract_shl <= shl(s_fract_28_i, shl_pos);
	s_exp_shl <= "00000000" when s_exp_i="00000001" else s_exp_i - ("00"&shl_pos);
	
	process(clk_i)
	begin
		if rising_edge(clk_i) then	
			if s_shr1='1' then
				s_fract_1 <= s_fract_shr1;
			elsif s_shl='1' then
				s_fract_1 <= s_fract_shl;
			else
				s_fract_1 <= s_fract_28_i;
			end if;
		end if;
	end process;
	
	process(clk_i)
	begin
		if rising_edge(clk_i) then	
			if s_shr1='1' then
				s_exp_1 <= s_exp_shr1;
			elsif s_shl='1' then
				s_exp_1 <= s_exp_shl; 
			else
				s_exp_1 <= s_exp_i;
			end if;
		end if;
	end process;
						 
	-- round
	s_roundup <= s_fract_1(2) and ((s_fract_1(1) or s_fract_1(0))or s_fract_1(3)) when s_rmode_i="00" else -- round to nearset even
							 (s_fract_1(2) or s_fract_1(1) or s_fract_1(0)) and (not s_sign_i) when s_rmode_i="10" else -- round up
							 (s_fract_1(2) or s_fract_1(1) or s_fract_1(0)) and (s_sign_i) when s_rmode_i="11" else -- round down
							 '0'; -- round to zero(truncate = no rounding)
	
	s_fract_rnd <= s_fract_1 + "0000000000000000000000001000" when s_roundup='1' else s_fract_1;
	
	-- stage 2: right-shift after rounding (when necessary)
	s_shr2 <= s_fract_rnd(27); 
	s_expr2_9 <= ("0"&s_exp_1) + "000000001";
	s_fract_shr2 <= shr(s_fract_rnd, "1");
	s_exp_shr2 <= s_expr2_9(7 downto 0);

	s_fract_2 <= s_fract_shr2 when s_shr2='1' else s_fract_rnd;
	s_exp_2 <= s_exp_shr2 when s_shr2='1' else s_exp_1;
	-------------
	
	s_infa <= '1' when s_opa_i(30 downto 23)="11111111"  else '0';
	s_infb <= '1' when s_opb_i(30 downto 23)="11111111"  else '0';

	s_nan_a <= '1' when (s_infa='1' and or_reduce (s_opa_i(22 downto 0))='1') else '0';
	s_nan_b <= '1' when (s_infb='1' and or_reduce (s_opb_i(22 downto 0))='1') else '0';
	s_nan_in <= '1' when s_nan_a='1' or  s_nan_b='1' else '0';
	s_nan_op <= '1' when (s_infa and s_infb)='1' and (s_opa_i(31) xor (s_fpu_op_i xor s_opb_i(31)) )='1' else '0'; -- inf-inf=Nan
	
	s_nan_sign <= s_sign_i when (s_nan_a and s_nan_b)='1' else
								s_opa_i(31) when s_nan_a='1' else 
								s_opb_i(31);
								
	-- check if result is inexact;
	s_ine_o <= (or_reduce(s_fract_28_i(2 downto 0)) or  or_reduce(s_fract_1(2 downto 0)) or or_reduce(s_fract_2(2 downto 0))) and not(s_nan_a or s_nan_b);	
	
	s_overflow <= (s_expr1_9(8) or s_expr2_9(8)) and not(s_nan_a or s_nan_b); -- if true, output is infinity
								
	process(s_sign_i, s_exp_2, s_fract_2, s_nan_in, s_nan_op, s_nan_sign, s_infa, s_infb, s_overflow)
	begin
		if (s_nan_in or s_nan_op)='1' then
			s_output_o <= s_nan_sign & QNAN;
		elsif (s_infa or s_infb)='1' or s_overflow='1' then
				s_output_o <= s_sign_i & INF;	
		else
				s_output_o <= s_sign_i & s_exp_2 & s_fract_2(25 downto 3);
		end if;
	end process;

	
	
	
end rtl;
