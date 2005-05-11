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
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;


entity mul is

port (
	clk			: in std_logic;

	din			: in std_logic_vector(31 downto 0);
	wr_a		: in std_logic;
	wr_b		: in std_logic;		-- write to b starts multiplier
	dout		: out std_logic_vector(31 downto 0)
);
end mul;


architecture rtl of mul is

--
--	Signals
--
	signal b				: std_logic_vector(31 downto 0);


begin

process(clk, wr_a, wr_b)

	variable count	: integer range 0 to 32;
	variable pa		: signed((2*32) downto 0);
	variable a_1	: std_logic;
	alias p			: signed(32 downto 0) is pa((2*32) downto 32);

begin

	if rising_edge(clk) then

		if wr_a='1' then
			p := (others => '0');
			pa(32-1 downto 0) := signed(din);

		elsif wr_b='1' then
			b <= din;
			a_1 := '0';
			count := 32;

		else
--
--	multiply
--
			if count > 0 then
				case std_ulogic_vector'(pa(0), a_1) is
					when "01" =>
						p := p + signed(b);
					when "10" =>
						p := p - signed(b);
					when others =>
						null;
				end case;
				a_1 := pa(0);
				pa := shift_right(pa, 1);
				count := count - 1;
			end if;

		end if;
	end if;

	dout <= std_logic_vector(pa(32-1 downto 0));


end process;

end rtl;
