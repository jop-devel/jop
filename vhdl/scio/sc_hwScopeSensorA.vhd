Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;

entity sc_hwScopeSensorA is
port (
	clk		: in std_logic;
	reset	: in std_logic;

--
--	SimpCon IO interface
--
	sc_rd		: in std_logic;
	sc_rd_data	: out std_logic_vector(31 downto 0); 
	
	sc_wr		: in std_logic;
	sc_wr_data	: in std_logic_vector(31 downto 0);
	
	sc_rdy_cnt	: out unsigned(1 downto 0)

 );

end sc_hwScopeSensorA;


architecture rtl of sc_hwScopeSensorA is

	constant hwSensorA: std_logic_vector(31 downto 0) := X"DEADBEEF";

begin

	sc_rdy_cnt <= "00";
	
	process(CLK,RESET)
	
	begin
	
		if RESET = '1' then
			sc_rd_data <= (others => '0');
	
		elsif rising_edge(clk) then
		
			-- read
			if sc_rd = '1' then
				sc_rd_data <= hwSensorA;
			end if;
		
			-- write	
		-- 	if sc_wr = '1' then
		-- 		up_down_reg <= sc_wr_data(31);
		-- 	end if;
			
		end if;
	
	end process;

end rtl;
