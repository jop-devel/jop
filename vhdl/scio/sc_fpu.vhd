-- This is the SimpCon interface to the FPU
--

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.std_logic_unsigned.all;
use ieee.numeric_std.all;

entity sc_fpu is
generic (ADDR_WIDTH : integer);

port (
	clk_i		: in std_logic;
	reset_i	: in std_logic;

-- SimpCon interface

	address_i		: in std_logic_vector(ADDR_WIDTH-1 downto 0);
	wr_data_i		: in std_logic_vector(31 downto 0);
	rd_i, wr_i		: in std_logic;
	rd_data_o		: out std_logic_vector(31 downto 0);
	rdy_cnt_o		: out unsigned(1 downto 0)

);
end sc_fpu;

architecture rtl of sc_fpu is

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
	
	signal opa_i, opb_i : std_logic_vector(31 downto 0);
	signal fpu_op_i		: std_logic_vector(2 downto 0);
	signal rmode_i : std_logic_vector(1 downto 0);
	signal output_o : std_logic_vector(31 downto 0);
	signal start_i, ready_o : std_logic ; 
	--signal ine_o, overflow_o, underflow_o, div_zero_o, inf_o, zero_o, qnan_o, snan_o: std_logic;

begin


    -- instantiate the fpu
    i_fpu: fpu port map (
			clk_i => clk_i,
			opa_i => opa_i,
			opb_i => opb_i,
			fpu_op_i =>	fpu_op_i,
			rmode_i => rmode_i,	
			output_o => output_o,  
			ine_o => open,
			overflow_o => open,
			underflow_o => open,		
        	div_zero_o => open,
        	inf_o => open,
        	zero_o => open,		
        	qnan_o => open, 		
        	snan_o => open,
        	start_i => start_i,
        	ready_o => ready_o);

rmode_i <= "00"; -- default rounding mode= round-to-nearest-even 
			
-- master reads from FPU
process(clk_i, reset_i)
begin

	if (reset_i='1') then
		start_i <= '0';
	elsif rising_edge(clk_i) then

		if rd_i='1' then
			-- that's our very simple address decoder
			if address_i="0011" then
				start_i <= '1';
			end if;
		else
			start_i <= '0';
		end if;
	end if;

end process;

-- set rdy_cnt
process(clk_i)
begin
	if (reset_i='1') then
		rdy_cnt_o <= "11";
	elsif rising_edge(clk_i) then
		if start_i='1' then
			rdy_cnt_o <= "11";
		elsif ready_o = '1' then
			rdy_cnt_o <= "00";
			rd_data_o <= output_o;
		end if;
	end if;
end process;


-- master writes to FPU
process(clk_i, reset_i)

begin

	if (reset_i='1') then
		opa_i <= (others => '0');
		opb_i <= (others => '0');
		fpu_op_i <= (others => '0');
	elsif rising_edge(clk_i) then

		if wr_i='1' then
			if address_i="0000" then
					opa_i <= wr_data_i;
			elsif address_i="0001" then
					opb_i <= wr_data_i;
			elsif address_i="0010" then
					fpu_op_i <=wr_data_i(2 downto 0); 
			end if;
		end if;

	end if;

end process;


end rtl;
