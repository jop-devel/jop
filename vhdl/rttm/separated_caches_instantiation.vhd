library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;

entity separated_caches_instantiation is
generic (
	addr_width		: integer := 19;
	write_way_bits	: integer := 4;
	read_way_bits	: integer := 5
);
port (
	clk, reset		: in std_logic;
	
	transaction_start: in std_logic;
	
	addr			: in std_logic_vector(addr_width-1 downto 0);
	wr_wr			: in std_logic;
	wr_rd			: in std_logic;
	
	hit_wr			: out std_logic;
	hit_rd			: out std_logic;
	line			: out unsigned(write_way_bits-1 downto 0);
	newline			: out unsigned(write_way_bits downto 0);
			
	shift 			: in std_logic;
	lowest_addr		: out std_logic_vector(addr_width-1 downto 0)
);
end separated_caches_instantiation;

architecture rtl of separated_caches_instantiation is



begin


	--
	--	Tags instantiation
	--
	
	write_tag: entity work.tag
		generic map(
			addr_width => addr_width,
			way_bits => write_way_bits
		)
		port map(
			clk => clk,
			reset => reset,
			
			transaction_start => transaction_start,
			
			addr => addr,
			wr => wr_wr,
			hit => hit_wr,
			line => line,
			newline => newline,
			
			shift => shift,
			lowest_addr => lowest_addr
		);
		
		read_tag: entity work.tag
		generic map(
			addr_width => addr_width,
			way_bits => read_way_bits
		)
		port map(
			clk => clk,
			reset => reset,
			
			transaction_start => transaction_start,
			
			addr => addr,
			wr => wr_rd,
			hit => hit_rd,
			line => open,
			newline => open,
			
			shift => '0',
			lowest_addr => open
		);

end architecture rtl;
