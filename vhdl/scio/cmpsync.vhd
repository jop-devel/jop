library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;

entity cmpsync is
generic (
		cpu_cnt : integer
	);
port (
	sync_in_array  : in sync_in_array_type(0 to cpu_cnt-1);
	sync_out_array : out sync_out_array_type(0 to cpu_cnt-1)
);
end cmpsync;


architecture rtl of cmpsync is

begin

process(sync_in_array)
begin
	for i in 0 to cpu_cnt-1 loop
		sync_out_array(i).s_out <= sync_in_array(0).s_in;
	end loop;
end process;
	
end rtl;





