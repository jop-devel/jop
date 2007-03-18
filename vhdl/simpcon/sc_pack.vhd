--
--	sc_pack.vhd
--
--	Package for SimpCon defines
--
--	Author: Martin Schoeberl (martin@jopdesign.com)
--	
--
--	2007-03-16  first version
--
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

package sc_pack is

	constant MEM_ADDR_SIZE : integer := 21;
	constant IO_ADDR_SIZE : integer := 7;
	constant RDY_CNT_SIZE : integer := 2;

	type sc_mem_out_type is record
		address		: std_logic_vector(MEM_ADDR_SIZE-1 downto 0);
		wr_data		: std_logic_vector(31 downto 0);
		rd			: std_logic;
		wr			: std_logic;
	end record;

	type sc_io_out_type is record
		address		: std_logic_vector(IO_ADDR_SIZE-1 downto 0);
		wr_data		: std_logic_vector(31 downto 0);
		rd			: std_logic;
		wr			: std_logic;
	end record;

	type sc_in_type is record
		rd_data		: std_logic_vector(31 downto 0);
		rdy_cnt		: unsigned(RDY_CNT_SIZE-1 downto 0);
	end record;

end sc_pack;
