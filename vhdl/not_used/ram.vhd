--
--	ram.vhd
--
--	internal memory for JOP3
--		without init content
--
--	use technology specific file instead (aram.vhd or xram.vhd)
--

Library IEEE ;
use IEEE.std_logic_1164.all ;
use IEEE.std_logic_arith.all ;
use IEEE.std_logic_unsigned.all ;

entity ram is
generic (width : integer := 32; addr_width : integer := 8);
port (
	data		: in std_logic_vector(width-1 downto 0);
	wraddress	: in std_logic_vector(addr_width-1 downto 0);
	rdaddress	: in std_logic_vector(addr_width-1 downto 0);
	wren		: in std_logic;
	clock		: in std_logic;

	q			: out std_logic_vector(width-1 downto 0)
);
end ram ;

--
--	registered wraddress, wren
--	unregistered din
--	registered rdaddress
--	unregistered dout
--
architecture rtl of ram is

	type mem_type is array (0 to 255) of std_logic_vector(width-1 downto 0);

	signal mem 		: mem_type;

	signal wraddr	: std_logic_vector(addr_width-1 downto 0);
	signal rdaddr	: std_logic_vector(addr_width-1 downto 0);
	signal wr_ena	: std_logic;

begin

process(clock)
begin
	if rising_edge(clock) then
		wraddr <= wraddress;
		wr_ena <= wren;
		rdaddr <= rdaddress;
	end if;
end process;

process(data, wr_ena)
begin
	if (wr_ena = '1') then
		mem(conv_integer(unsigned(wraddr))) <= data;
	end if ;
end process;

	q <= mem(conv_integer(unsigned(rdaddr)));

end rtl;
