library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use ieee.std_logic_unsigned.all;
use ieee.std_logic_misc.all;
use ieee.math_real.all;

entity cam is

generic (sc_width : integer := 4; data_width : integer := 32; data_depth : integer := 32);
	port (
		clock		: in std_logic;
		reset	: in std_logic;		
		--
		--	SimpCon IO interface
		--
		sc_rd		: in std_logic;
		sc_rd_data	: out std_logic_vector(data_width-1 downto 0);
		
		sc_wr		: in std_logic;
		sc_wr_data	: in std_logic_vector(data_width-1 downto 0);
		
		sc_address		: in std_logic_vector(sc_width-1 downto 0)
	
	);
end cam;

architecture rtl of cam is
	signal match, empty  		: std_logic_vector(data_depth-1 downto 0);
	type DATA_ARRAY is array (data_depth-1 downto 0) of std_logic_vector(data_width-1 downto 0);
	signal addresses  : DATA_ARRAY;
	signal matched : std_logic;
	signal current_address : std_logic_vector(data_width-1 downto 0);
	--signal empty_position, match_position : std_logic_vector(integer(ceil(log2(real(data_depth))))-1 downto 0);
	signal empty_position, match_position : integer range 0 to data_depth-1;
begin

	comparator: for i in 0 to data_depth-1 generate
		match(i) <= '1' when (addresses(i) = current_address and empty(i) = '0') else '0';
	end generate;

	matched <= OR_REDUCE(match);
	
	process(empty)
		variable vempty:	std_logic_vector(data_depth-1 downto 0);
	begin
		vempty := empty;
		for i in 0 to data_depth-1 loop
			if (vempty(i) = '1') then
				empty_position <= i;
			end if;
		end loop;
	end process;
	
	match_encoder: process(match)
		variable vmatch:	std_logic_vector(data_depth-1 downto 0);
	begin
		vmatch := match;
		for i in 0 to data_depth-1 loop
			if (vmatch(i) = '1') then
				match_position <= i;
			end if;
		end loop;
	end process;
	
	empty_encoder: process(clock,reset)
   begin
		if(reset='1') then
			empty <= (others => '1');
			current_address <= (others => '0');
			for i in 0 to data_depth-1 loop
				addresses(i) <= (others => '0');
			end loop;
		elsif(rising_edge(clock)) then
			if(sc_address(0) = '0') then
				if(sc_wr = '1') then
					--write address
					current_address <= sc_wr_data;
				elsif(sc_rd = '1') then
					--read result
					if(matched = '0') then
						--store non-existant address and value
						addresses(empty_position) <= current_address;
						empty(empty_position) <= '0';
						sc_rd_data <= '0' & std_logic_vector(to_unsigned(empty_position, data_depth-1));
					else
						--return existing value
						sc_rd_data <= '1' & std_logic_vector(to_unsigned(match_position, data_depth-1));
					end if;
				end if;
			else
				if(sc_wr = '1') then
					--clear entry
					empty(match_position) <= '1';
				end if;
			end if;
		end if;
   end process;

end rtl;

