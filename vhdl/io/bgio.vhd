--
--	bgio.vhd
--
--	io for bg263
--
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--	todo:
--
--
--	2003-07-05	new IO standard
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity bgio is

generic (io_addr : integer; clk_freq : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	key_in	: in std_logic_vector(3 downto 0);
	key_out	: out std_logic_vector(3 downto 0);
	led		: out std_logic;
	rela	: out std_logic;
	relb	: out std_logic;
	m_dtr	: out std_logic;
	i		: in std_logic
);
end bgio;

architecture rtl of bgio is

	signal key_inreg		: std_logic_vector(3 downto 0);
	signal key_outreg		: std_logic_vector(3 downto 0);
	signal ireg				: std_logic;

begin

process(addr, rd, key_inreg, ireg)

begin
	if addr=std_logic_vector(to_unsigned(io_addr, 4)) then
		dout <= std_logic_vector(to_unsigned(0, 28)) & key_inreg;
	elsif addr=std_logic_vector(to_unsigned(io_addr+1, 4)) then
		dout <= std_logic_vector(to_unsigned(0, 31)) & ireg;
	else
		dout <= (others => 'Z');
	end if;

end process;


process(clk, reset, wr, addr)

begin
	if (reset='1') then

		key_outreg <= (others => '0');
		led <= '0';
		rela <= '0';
		relb <= '0';

	elsif rising_edge(clk) then

		if addr=std_logic_vector(to_unsigned(io_addr, 4))
			and wr='1' then
			key_outreg <= din(3 downto 0);
		elsif addr=std_logic_vector(to_unsigned(io_addr+1, 4))
			and wr='1' then
			led <= din(0);
			rela <= din(1);
			relb <= din(2);
			m_dtr <= din(3);
		end if;

	end if;
end process;

--
--	keyboard
--
	key_out(0) <= 'Z' when key_outreg(0)='0' else '0';	-- inv. OC output
	key_out(1) <= 'Z' when key_outreg(1)='0' else '0';
	key_out(2) <= 'Z' when key_outreg(2)='0' else '0';
	key_out(3) <= 'Z' when key_outreg(3)='0' else '0';
	

--
--	register inputs
--
process(clk, key_in, i)

begin
	if rising_edge(clk) then
		key_inreg <= not key_in;			-- invert keyboard (log 1 activ)
		ireg <= i;
	end if;
end process;

end rtl;
