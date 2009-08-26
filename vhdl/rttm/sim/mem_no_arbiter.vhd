library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;
use work.sc_arbiter_pack.all;

entity mem_no_arbiter is

generic (
	-- size of main memory simulation in 32-bit words.
	-- change it to less memory to speedup the simulation
	-- minimum is 64 KB, 14 bits
	MEM_BITS	: integer
);	
port (
	clk: std_logic;
	reset: std_logic;
	sc_mem_out: sc_out_type;
	sc_mem_in: out sc_in_type
);

end mem_no_arbiter;

architecture behav of mem_no_arbiter is

--
--	Configuration
--



constant ram_cnt		: integer := 2;		-- clock cycles for external ram
constant rom_cnt		: integer := 10;	-- clock cycles for external rom for 100 MHz


signal fl_d	: std_logic_vector(7 downto 0);

-- memory interface

	signal ram_addr			: std_logic_vector(17 downto 0);
--	signal ram_dout			: std_logic_vector(31 downto 0);
--	signal ram_din			: std_logic_vector(31 downto 0);
	signal ram_dout_en	: std_logic;
	signal ram_ncs			: std_logic;
	signal ram_noe			: std_logic;
	signal ram_nwe			: std_logic;
	
	signal ram_data			: std_logic_vector(31 downto 0);

begin

	cmp_mem_if: entity work.sc_mem_if(rtl)
	generic map (
		ram_ws => ram_cnt-1,
		rom_ws => rom_cnt-1
		)
	port map (
		clk => clk,
		reset => reset,
		sc_mem_out => sc_mem_out,
		sc_mem_in => sc_mem_in,
		ram_addr => ram_addr,
		ram_dout => ram_data,
		ram_din => ram_data,
		ram_dout_en => ram_dout_en,
		ram_ncs => ram_ncs,
		ram_noe => ram_noe,
		ram_nwe => ram_nwe,
		fl_d => fl_d,
		fl_rdy => '1'
		);



	main_mem: entity work.memory generic map(MEM_BITS, 32) port map(
			addr => ram_addr(MEM_BITS-1 downto 0),
			data => ram_data,
			ncs => ram_ncs,
			noe => ram_noe,
			nwr => ram_nwe
			);
						
end architecture behav;