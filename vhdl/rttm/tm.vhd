-- tm.vhdl created on 7:9  2008.0.8

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;

entity tm is

generic (
	addr_width	: integer := 18;	-- address bits of cachable memory
	way_bits	: integer := 5		-- 2**way_bits is number of entries
);
port (
	clk, reset	: in std_logic;
	from_cpu		: in sc_out_type;
	to_cpu			: out sc_in_type;
--	to_mem			: out sc_out_type;
--	from_mem		: in sc_in_type;

	x : out std_logic
);
end tm;

architecture rtl of tm is 

	constant lines		: integer := 2**way_bits;

	signal line_addr	: unsigned(way_bits-1 downto 0);
	-- tag_width can be used to reduce cachable area - saves a lot in the comperators
	signal use_addr		: std_logic_vector(addr_width-1 downto 0);

	type tag_array is array (0 to lines-1) of std_logic_vector(addr_width-1 downto 0);
	type valid_array is array (0 to lines-1) of std_logic;
	signal tag			: tag_array;
	signal valid		: valid_array;
	type data_array is array (0 to lines-1) of std_logic_vector(31 downto 0);
	signal data			: data_array;

	-- pointer to next block to be used on a miss
	signal nxt			: unsigned(way_bits-1 downto 0);
	signal hit			: std_logic;

	signal from_cpu_dly: sc_out_type;
	signal rd_dly, rd_hit: std_logic;
	signal reg_data, save_data: std_logic_vector(31 downto 0);

	
begin

process(clk, reset)
begin

	if reset='1' then

		nxt <= (others => '0');
		hit <= '0';
		to_cpu.rdy_cnt <= "00";
		rd_hit <= '0';	
		for i in 0 to lines-1 loop
			valid(i) <= '0';
		end loop;

	elsif rising_edge(clk) then


		hit <= '0';
		to_cpu.rdy_cnt <= "00";
		rd_hit <= '0';	

		from_cpu_dly <= from_cpu;
		rd_dly <= from_cpu_dly.rd;

		if from_cpu.wr='1' or from_cpu.rd='1' then
			to_cpu.rdy_cnt <= "01";
		end if;

		-- hit detection
		for i in 0 to lines-1 loop
			if tag(i) = from_cpu.address then
				line_addr <= to_unsigned(i, way_bits);
				hit <= '1';
				exit;		-- not much difference using exit or not...
			end if;
		end loop;
		
		-- write in the next cycle
		if from_cpu_dly.wr='1' then
			if hit='1' then
				data(to_integer(line_addr)) <= from_cpu_dly.wr_data;
				valid(to_integer(line_addr)) <= '1';
			else
				data(to_integer(nxt)) <= from_cpu_dly.wr_data;
				tag(to_integer(nxt)) <= from_cpu_dly.address;
				valid(to_integer(nxt)) <= '1';
				nxt <= nxt + 1;
			end if;
		end if;

		-- another cycle delay to infer on-chip memory
		reg_data <= data(to_integer(line_addr));
		if from_cpu_dly.rd='1' then
			if valid(to_integer(line_addr))='1' and hit='1' then
				rd_hit <= '1';	
			end if;
		end if;

		if rd_hit='1' then
			save_data <= reg_data;
		end if;

	end if;
end process;

process (rd_hit, reg_data, save_data)
begin
	if rd_hit='1' then
		to_cpu.rd_data <= reg_data;
	else
		to_cpu.rd_data <= save_data;
	end if;
end process;

end;
	
