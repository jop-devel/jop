library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
entity sub is
generic (width : integer := 32);
port (
	a, b		: in std_logic_vector(width-1 downto 0);
	dout		: out std_logic_vector(width-1 downto 0)
);
end sub;
architecture rtl of sub is
begin
process(a, b) begin
	dout <=	std_logic_vector(
			signed(b) -
			signed(a)
		);
end process;
end rtl;
