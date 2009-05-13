-- tm.vhdl created on 7:9  2008.0.8

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity tm is

generic (
	addr_width	: integer := 18;	-- address bits of cachable memory
	way_bits	: integer := 7		-- 2**way_bits is number of entries
);
port (
	clk, reset	: in std_logic;
	addr		: in std_logic_vector(addr_width-1 downto 0);
	wr			: in std_logic;
	rd			: in std_logic;
	din			: in std_logic_vector(31 downto 0);
	dout		: out std_logic_vector(31 downto 0)
);
end tm;

architecture rtl of tm is 

	constant lines		: integer := 2**way_bits;

	signal line_addr	: unsigned(way_bits-1 downto 0);
	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	signal use_addr		: std_logic_vector(addr_width-1 downto 0);

	type tag_array is array (0 to lines-1) of std_logic_vector(addr_width-1 downto 0);
	signal tag			: tag_array;
	type data_array is array (0 to lines-1) of std_logic_vector(31 downto 0);
	signal data			: data_array;

	-- pointer to next block to be used on a miss
	signal nxt			: unsigned(way_bits-1 downto 0);
	signal hit			: std_logic;
	
begin

process(clk, reset)
begin

	if (reset='1') then

		nxt <= (others => '0');
		hit <= '0';
		for i in 0 to lines-1 loop
			tag(i) <= (others => '0');
		end loop;

	elsif rising_edge(clk) then

		
		for i in 0 to lines-1 loop
			if tag(i) = addr then
				line_addr <= to_unsigned(i, way_bits);
				hit <= '1';
--				exit;		-- not much difference using exit or not...
			end if;
		end loop;
		
		if wr='1' and hit='0' then
			tag(to_integer(nxt)) <= addr;
			data(to_integer(nxt)) <= din;
			nxt <= nxt + 1;
		end if;

		dout <= data(to_integer(line_addr));

	end if;
end process;

--	dout(way_bits-1 downto 0) <= line_addr;

end;
	
