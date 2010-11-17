library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.numeric_std.all;

use work.sc_pack.all;

entity null_cache is
port (
	clk, reset:	    in std_logic;

	cpu_out:		in sc_out_type;
	cpu_in:			out sc_in_type;

	mem_out:		out sc_out_type;
	mem_in:			in sc_in_type);
end null_cache;

architecture rtl of null_cache is

	-- what state we're in
	type STATE_TYPE is (idle, rd, wr);
	signal state, next_state : state_type;

begin

	forward: process (cpu_out, mem_in)
	begin  -- process forward
		mem_out <= cpu_out;
		if cpu_out.cache = bypass then
			mem_out.rd <= cpu_out.rd;
			mem_out.wr <= cpu_out.wr;			
		else
			mem_out.rd <= '0';
			mem_out.wr <= '0';
		end if;
		cpu_in <= mem_in;
	end process forward;

end rtl;
