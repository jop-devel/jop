library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
entity add is
generic (width : integer := 32);
port (
	a, b		: in std_logic_vector(width-1 downto 0);
	dout		: out std_logic_vector(width-1 downto 0)
);
end add;
architecture rtl of add is
begin
process(a, b) begin
	dout <=	std_logic_vector(
			signed(a) +
			signed(b)
		);
end process;
end rtl;
