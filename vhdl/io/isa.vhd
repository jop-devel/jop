--
--	isa.vhd
--
--	ISA bus for ethernet chip
--
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--	todo:
--
--
--	2003-09-23	new IO standard
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity isa is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	isa_d		: inout std_logic_vector(7 downto 0);
	isa_a		: out std_logic_vector(4 downto 0);
	isa_reset	: out std_logic;
	isa_nior	: out std_logic;
	isa_niow	: out std_logic
);
end isa;

architecture rtl of isa is

--
--	signal for isa data bus
--
	signal isa_data			: std_logic_vector(7 downto 0);
	signal isa_dir			: std_logic;		-- direction of isa_d ('1' means driving out)


begin

process(addr, rd, isa_d)

begin
	if addr=std_logic_vector(to_unsigned(io_addr+1, 4)) then
		dout <= std_logic_vector(to_unsigned(0, 24)) & isa_d;
	else
		dout <= (others => 'Z');
	end if;

end process;


process(clk, reset, wr, addr)

begin
	if (reset='1') then

		isa_data <= (others => '0');
		isa_a <= (others => '0');
		isa_reset <= '0';
		isa_nior <= '1';
		isa_niow <= '1';
		isa_dir <= '0';

	elsif rising_edge(clk) then

		if addr=std_logic_vector(to_unsigned(io_addr, 4))
			and wr='1' then
			isa_a <= din(4 downto 0);
			isa_reset <= din(5);
			isa_nior <= not din(6);
			isa_niow <= not din(7);
			isa_dir <= din(8);
		elsif addr=std_logic_vector(to_unsigned(io_addr+1, 4))
			and wr='1' then
			isa_data <= din(7 downto 0);
		end if;

	end if;
end process;

--
--	isa data bus
--
	isa_d <= isa_data when isa_dir='1' else "ZZZZZZZZ";

end rtl;
