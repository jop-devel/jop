--
--	mul.vhd
--
--	booth multiplier
--	
--	resources on ACEX1K
--
--	
--		244 LCs only mul
--
--	2002-03-22	first version
--	2004-10-07	changed to Koljas version
--	2004-10-08	mul operands from a and b, single instruction
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;
use ieee.std_logic_unsigned.all;


entity mul is

generic (
	width		: integer := 32		-- one data word
);

port (
	clk			: in std_logic;

	ain			: in std_logic_vector(width-1 downto 0);
	bin			: in std_logic_vector(width-1 downto 0);
	wr			: in std_logic;		-- write starts multiplier
	dout		: out std_logic_vector(width-1 downto 0)
);
end mul;


architecture rtl of mul is
--
--	Signals
--
	signal count	: integer range 0 to width+1;  -- 5 luts can be saved by implementing this in a SR16
	signal ci    : integer range 0 to 1;
	signal a32	: std_logic;
	signal a, b, t, t2, s, s2: unsigned(width-1 downto 0);

begin

t <=     b when a(0) = '0' and a32 = '1' else
		  not	b when a(0) = '1' and a32 = '0' else
		  to_unsigned(0, 32);
ci <=   0 when a(0) = '0' and a32 = '1' else
		  1 when a(0) = '1' and a32 = '0' else
		  0;
t2 <= unsigned(s + t + to_unsigned(ci,width));

process(clk)

begin


	if rising_edge(clk) then
		if wr='1' then
			a <= unsigned(ain);
			b <= unsigned(bin);
			count <= width;
			s  <= to_unsigned(0,32);	-- reset unconditionally to save logic
			a32 <= '0';
		else
--
--	multiply
--			
			s <= t2(31) & t2(31 downto 1);
			a32 <= a(0);
			if count > 0 then
				a <= t2(0) & a(width-1 downto 1);
				count <= count - 1;
			end if;					
		end if;
	end if;
end process;

	dout <= std_logic_vector(a);

end rtl;
