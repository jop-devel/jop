--
--	jopcore.vhd
--
--	top level for new borad
--
--	2002-03-28	creation
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

-- library exemplar;					-- for pin attributes
-- use exemplar.exemplar_1164.all;

entity jopcore is

generic (
	clk_freq	: integer := 20000000;	-- 20 MHz clock frequency for uart
	width		: integer := 32;	-- one data word
	ioa_width	: integer := 3		-- address bits of internal io
);

!!!!!! change to new mem/io interface !!!!!!!!

port (
	clk, reset	: in std_logic;

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	ram_a17		: out std_logic;
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nmem_rd		: out std_logic;
	nmem_wr		: out std_logic;

--	WD
	wd			: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	ncts		: in std_logic;
	nrts		: out std_logic;

-- io
	l			: out std_logic_vector(20 downto 1);
	r			: out std_logic_vector(20 downto 1);
	t			: out std_logic_vector(6 downto 1);
	b			: out std_logic_vector(10 downto 1)

);
--	attribute pin_number of clk 	: signal is "55";
--	attribute pin_number of reset 	: signal is "56";
--	attribute array_pin_number of a 	: signal is (
--		"44", "47", "62", "42", "46", "60", "65", "51", "130",
--		"59", "49", "69", "70", "38", "72", "140", "120", "138", "121" 
--	);
--	attribute pin_number of ram_a17	: signal is "67";
--	attribute array_pin_number of d 	: signal is (
--		"116", "114", "113", "112", "111", "110", "109", "118"
--	);
--	attribute pin_number of nram_cs	: signal is "117";
--	attribute pin_number of nrom_cs	: signal is "131";
--	attribute pin_number of nmem_rd	: signal is "128";
--	attribute pin_number of nmem_wr	: signal is "48";
--
--	attribute pin_number of wd		: signal is "43";
end jopcore;

architecture rtl of jopcore is

--
--	components:
--

component core is
port (
	clk, reset	: in std_logic;

-- memio connection

	din			: in std_logic_vector(width-1 downto 0);
	addr		: out std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: out std_logic;

	dout		: out std_logic_vector(width-1 downto 0)
);
end component;

component memio is
generic (clk_freq : integer; width : integer; ioa_width : integer);
port (

-- jop interface

	clk, reset	: in std_logic;

	din			: in std_logic_vector(width-1 downto 0);
	addr		: in std_logic_vector(ioa_width-1 downto 0);
	rd, wr		: in std_logic;

	dout		: out std_logic_vector(width-1 downto 0);

-- external mem interface

	a			: out std_logic_vector(18 downto 0);
	d			: inout std_logic_vector(7 downto 0);
	nram_cs		: out std_logic;
	nrom_cs		: out std_logic;
	nrd			: out std_logic;
	nwr			: out std_logic;

-- serial interface

	txd			: out std_logic;
	rxd			: in std_logic;
	cts			: in std_logic;
	rts			: out std_logic;

-- io ports
	wd			: out std_logic

);
end component;

--
--	Signals
--

	signal memio_din		: std_logic_vector(width-1 downto 0);
	signal memio_addr		: std_logic_vector(ioa_width-1 downto 0);
	signal memio_rd			: std_logic;
	signal memio_wr			: std_logic;

	signal memio_dout		: std_logic_vector(width-1 downto 0);

	signal aint				: std_logic_vector(18 downto 0);

	signal rts, cts			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0);

begin

	int_res <= reset or not res_cnt(0) or not res_cnt(1) or not res_cnt(2);

	l <= (others => 'Z');
	r <= (others => 'Z');
	t <= (others => 'Z');
	b <= (others => 'Z');

process(clk)
begin
	if rising_edge(clk) then
		if (res_cnt/="111") then
			res_cnt <= res_cnt+1;
		end if;
	end if;
end process;

	cmp_core: core 
		port map (clk, int_res,
			memio_dout, memio_addr,
			memio_rd, memio_wr, memio_din
		);

	cmp_memio: memio generic map (clk_freq, width, ioa_width)
		port map (clk, int_res, memio_din, memio_addr, memio_rd, memio_wr, memio_dout,
			aint, d, nram_cs, nrom_cs, nmem_rd, nmem_wr,
			txd, rxd, cts, rts,
			wd
		);

	a <= aint;
	ram_a17 <= '1';						-- CS1 for 128kB ram
	-- ram_a17 <= aint(17);				-- for 512kB ram

	nrts <= not rts;
	cts <= not ncts;

end rtl;
