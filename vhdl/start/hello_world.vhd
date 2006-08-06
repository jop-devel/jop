	--
	--	hello_world.vhd
	--
	--	The 'Hello World' example for FPGA programming.
	--
	--	Author: Martin Schoeberl (martin@jopdesign.com)
	--
	--	2006-08-04	created
	--
	
	library ieee;
	use ieee.std_logic_1164.all;
	use ieee.numeric_std.all;
	
	entity hello_world is
	
	port (
		clk		: in std_logic;
		led		: out std_logic
	);
	end hello_world;
	
	architecture rtl of hello_world is
	
		constant CLK_FREQ : integer := 20000000;
		constant BLINK_FREQ : integer := 1;
		constant CNT_MAX : integer := CLK_FREQ/BLINK_FREQ/2-1;
	
		signal cnt		: unsigned(24 downto 0);
		signal blink	: std_logic;
	
	begin
	
		process(clk)
		begin
	
			if rising_edge(clk) then
				if cnt=CNT_MAX then
					cnt <= (others => '0');
					blink <= not blink;
				else
					cnt <= cnt + 1;
				end if;
			end if;
	
		end process;
	
		led <= blink;
	
	end rtl;
