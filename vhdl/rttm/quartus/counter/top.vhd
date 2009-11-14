library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity top is

port (
	clk: std_logic;
	reset: std_logic;

	port_valid: std_logic_vector(31 downto 0);
	port_was_read: std_logic_vector(31 downto 0);
	port_count: out std_logic_vector(4 downto 0)	
	);
end top;

architecture rtl of top is

signal valid: std_logic_vector(31 downto 0);
signal was_read: std_logic_vector(31 downto 0);
signal count: std_logic_vector(4 downto 0);

	function summer(valid: std_logic_vector(31 downto 0)) return 
		std_logic_vector is
		variable var_count: unsigned(4 downto 0);
	begin
		for i in 0 to 31 loop
			if valid(i) = '1' then
				var_count := var_count + 1;
			end if;
		end loop;
		
		return std_logic_vector(var_count);
	end summer;

begin

	process(clk) is	
	begin
		if rising_edge(clk) then
			valid <= port_valid;
			was_read <= port_was_read;
			port_count <= count;
		end if;
	end process;
	

	process(reset, clk) is		
	begin
		if reset = '1' then
			count <= (others => '0');
		elsif rising_edge(clk) then
			count <= summer(valid and was_read);
		end if;
	end process;

end architecture rtl;