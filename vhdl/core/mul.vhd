--
--	mul.vhd
--
--	bit-serial multiplier
--	
--	resources on ACEX1K
--
--	
--		244 LCs only mul
--
--	2002-03-22	first version
--	2004-10-07	changed to Koljas version
--	2004-10-08	mul operands from a and b, single instruction
--      2008-02-15      changed from booth to bit-serial
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
	clk		: in std_logic;

	ain		: in std_logic_vector(width-1 downto 0);
	bin		: in std_logic_vector(width-1 downto 0);
	wr		: in std_logic;		-- write starts multiplier
	dout		: out std_logic_vector(width-1 downto 0)
);
end mul;


architecture rtl of mul is
--
--	Signals
--
	signal count : integer range 0 to width/2;
        signal p     : unsigned(width-1 downto 0);
        signal a, b  : unsigned(width-1 downto 0);
        
begin

process(clk)
  
variable prod : unsigned(width-1 downto 0);

begin
  if rising_edge(clk) then
    if wr='1' then
      p <= (others => '0');
      a <= unsigned(ain);
      b <= unsigned(bin);
    else

      prod := p;
      if b(0) = '1' then
        prod := prod + a;
      end if;          
      if b(1) = '1' then
        prod := (prod(width-1 downto 1) + a(width-2 downto 0)) & prod(0);
      end if;          
      p <= prod;

      a <= a(width-3 downto 0) & "00";
      b <= "00" & b(width-1 downto 2);
      
    end if;
  end if;
end process;

dout <= std_logic_vector(p);

end rtl;
