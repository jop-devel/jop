--
--	sim_uart.vhd
--
--	Serial interface for the simulation (pre SimpCon version)
--
--	Just prints the send character to stdout.
--	
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--
--	2005-02-21	creation
--


library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity uart is

generic (io_addr : integer; clk_freq : integer;
	baud_rate : integer;
	txf_depth : integer; txf_thres : integer;
	rxf_depth : integer; rxf_thres : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;
	addr	: in std_logic_vector(3 downto 0);
	din		: in std_logic_vector(31 downto 0);
	wr		: in std_logic;
	dout	: out std_logic_vector(31 downto 0);
	rd		: in std_logic;

	txd		: out std_logic;
	rxd		: in std_logic;
	ncts	: in std_logic;
	nrts	: out std_logic
);
end uart ;

architecture sim of uart is

--
--	signals for uart connection
--
	signal ua_wr, tdre		: std_logic;
	signal rdrf				: std_logic;
	signal char				: std_logic_vector(7 downto 0);

begin

--
--	io handling
--
process(addr, rdrf, tdre)

begin
	if addr=std_logic_vector(to_unsigned(io_addr, 4)) then
		dout <= std_logic_vector(to_unsigned(0, 30)) & rdrf & tdre;
	elsif addr=std_logic_vector(to_unsigned(io_addr+1, 4)) then
		dout <= std_logic_vector(to_unsigned(0, 32));
	else
		dout <= (others => 'Z');
	end if;

end process;

	rdrf <= '0';	-- we don't receive characters in the simulation
	tdre <= '1';	-- the sender is always free in the simulation

process(clk)

	variable l : line;

begin
	ua_wr <= '0';
	if rising_edge(clk) then
		if addr=std_logic_vector(to_unsigned(io_addr+1, 4))
			and wr='1' then

			ua_wr <= '1';
			char <= din(7 downto 0);
			write(l, character'val(to_integer(unsigned(din(7 downto 0)))));
			writeline(output, l);
		end if;
	end if;
end process;


end sim;
