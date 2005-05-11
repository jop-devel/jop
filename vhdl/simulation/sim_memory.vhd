------------------------------------------------------------------
--
-- A very simple memory model
-- use variables instead of signals to save time and memory
--
------------------------------------------------------------------
------------------------------------------------------------------
library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

-- use IEEE.std_logic_textio.all;          -- I/O for logic types

entity memory is
	generic(add_bits : integer := 12;
		data_bits : integer := 32);
	port(
		addr	: in std_logic_vector(add_bits-1 downto 0);
		data	: inout std_logic_vector(data_bits-1 downto 0);
		ncs		: in std_logic;
		noe		: in std_logic;
		nwr		: in std_logic);
 
	subtype word is std_logic_vector(data_bits-1 downto 0);
	constant nwords : integer := 2 ** add_bits;
	type ram_type is array(0 to nwords-1) of word;
end;


architecture sim of memory is
	------------------------------
	shared variable ram : ram_type;
	------------------------------
	constant tAcc : time := 17 ns;
	constant tHold : time := 2 ns;

begin

memory:
process (addr, data, ncs, noe, nwr)
	variable address : natural;

begin
		address := to_integer(unsigned(addr));
		if noe='0' and ncs='0' then
			data <= ram(address) after tAcc;
		else
			data <= (others => 'Z') after tHold;
		end if;
		if ncs='0' and rising_edge(nwr) then
			ram(address) := data;
		end if;
end process;


-- initialize at start with a second process accessing
-- the shared variable ram

initialize:
process

	variable address	: natural;

	file memfile		: text is "mem_main.dat";
	variable memline	: line; 
	variable val		: integer;
variable x : integer;

	begin
--		write(output, "load main memory...");
		for address in 0 to nwords-1 loop
			if endfile(memfile) then
				exit;
			end if;
			readline(memfile, memline);
			read(memline, val);
			ram(address) := std_logic_vector(to_signed(val, data_bits));
		end loop;
		file_close(memfile);
		-- we're done, wait forever
		wait;
	end process initialize;

end architecture sim;

