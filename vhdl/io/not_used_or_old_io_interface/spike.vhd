--
--	spike filter with sync in
--

library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;

entity spike is

port (
	clk		: in std_logic;
	reset	: in std_logic;

	in_sp	: in std_logic;
	out_sp	: out std_logic

);
end spike;

architecture rtl of spike is

	signal in_buf	   : std_logic_vector(3 downto 0); -- sync in, filter

begin

	process(clk, reset)
	begin
		if (reset='1') then
			in_buf <= "0000";
		elsif rising_edge(clk) then
			in_buf(0) <= in_sp;
			in_buf(3 downto 1) <= in_buf(2 downto 0);
		end if;
	end process;

	with in_buf(3 downto 1) select
		out_sp <=
			'0' when "000",
			'0' when "001",
			'0' when "010",
			'1' when "011",
			'0' when "100",
			'1' when "101",
			'1' when "110",
			'1' when "111",
			'X' when others;

end architecture rtl;

