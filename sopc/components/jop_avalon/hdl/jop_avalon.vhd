--
--	jop_avalon.vhd
--
--	top level for Avalon (SPOC Builder) version
--
--	2006-08-10	adapted from jop_256x16.vhd
--
--
library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity jop_avalon is

generic (
	addr_bits	: integer := 24;	-- address range for the avalone interface
	jpc_width	: integer := 12;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4		-- 2*block_bits is number of cache blocks
);

port (
	clk, reset		: in std_logic;
--
--	serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;

--
--	watchdog
--
	wd				: out std_logic;

--
-- Avalon interface
--

	address		: out std_logic_vector((addr_bits-1+2) downto 0);
	writedata	: out std_logic_vector(31 downto 0);
	byteenable	: out std_logic_vector(3 downto 0);
	readdata	: in std_logic_vector(31 downto 0);
	read		: out std_logic;
	write		: out std_logic;
	waitrequest	: in std_logic

);
end jop_avalon;

