--
--	jbc.vhd
--
--	byte code memory for JOP3
--
--	write does NOT work!!!
--
--	use technology specific file instead (ajbc.vhd or xjbc.vhd)
--
--

Library IEEE ;
use IEEE.std_logic_1164.all ;
use IEEE.std_logic_arith.all ;
use IEEE.std_logic_unsigned.all ;

entity jbc is
generic (width : integer := 8; addr_width : integer := 10);
port (
	data		: in std_logic_vector(width-1 downto 0);
	wraddress	: in std_logic_vector(addr_width-1 downto 0);
	rdaddress	: in std_logic_vector(addr_width-1 downto 0);
	wren		: in std_logic;
	clock		: in std_logic;

	q			: out std_logic_vector(width-1 downto 0)
);
end jbc ;

--
--	registered wraddress, wren
--	unregistered din
--	unregistered rdaddress
--	unregistered dout
--
architecture rtl of jbc is

	type mem_type is array (0 to 255) of std_logic_vector(width-1 downto 0);

	signal mem 		: mem_type;

	signal wraddr	: std_logic_vector(addr_width-1 downto 0);
	signal rdaddr	: std_logic_vector(addr_width-1 downto 0);
	signal wr_ena	: std_logic;

	signal addr	: std_logic_vector(15 downto 0);

begin

	addr <= "000000" & rdaddr;

process(addr) begin

	q <= x"00";		-- nop

	case addr is

		when x"0000" => q <= x"04";	-- iconst_1
		when x"0001" => q <= x"05";	-- iconst_2
		when x"0002" => q <= x"60";	-- iadd
		when x"0003" => q <= x"07";	-- iconts_4
		when x"0004" => q <= x"64";	-- isub
		when x"0005" => q <= x"3b";	-- istore_0

		when others => null;
	end case;
end process;


process(clock)
begin
	if rising_edge(clock) then
		wraddr <= wraddress;
		wr_ena <= wren;
	end if;
end process;

	rdaddr <= rdaddress;

process(clock, wr_ena)
begin
	if rising_edge(clock) then
		if (wr_ena = '1') then
			mem(conv_integer(unsigned(wraddr))) <= data;
		end if ;
	end if;
end process;

end rtl;
