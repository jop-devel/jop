--
--	cyciotest.vhd
--
--	top level for cyciotestlone board
--
--	2002-06-27:	xxx LCs, xx.x MHz
--	2002-08-08:	2431 LCs simpler sigdel
--
--	2002-12-23	creation
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity cyciotest is

generic (
	clk_freq	: integer := 20000000;	-- 20 MHz clock frequency
	width		: integer := 32;	-- one data word
	ioa_width	: integer := 3;		-- address bits of internal io
	ram_cnt		: integer := 2;		-- clock cyciotestles for external ram
	rom_cnt		: integer := 3		-- clock cyciotestles for external rom
);

port (
	fl_a	: out std_logic_vector(18 downto 0);
	fl_d	: inout std_logic_vector(7 downto 0);
	fl_ncs	: out std_logic;
	fl_ncsb	: out std_logic;
	fl_noe	: out std_logic;
	fl_nwe	: out std_logic;
	fl_rdy	: in std_logic;
	io_b	: inout std_logic_vector(10 downto 1);
	io_l	: inout std_logic_vector(20 downto 1);
	io_r	: inout std_logic_vector(20 downto 1);
	io_t	: inout std_logic_vector(6 downto 1);
	clk		: in std_logic;
	freeio	: in std_logic;
	wd		: out std_logic;
--
---- serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;
	ser_ncts		: in std_logic;
	ser_nrts		: out std_logic;

	rama_a	: out std_logic_vector(17 downto 0);
	rama_d	: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_noe	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	rama_nwe	: out std_logic;
	ramb_a	: out std_logic_vector(17 downto 0);
	ramb_d	: inout std_logic_vector(15 downto 0);
	ramb_ncs	: out std_logic;
	ramb_noe	: out std_logic;
	ramb_nlb	: out std_logic;
	ramb_nub	: out std_logic;
	ramb_nwe	: out std_logic


);
end cyciotest;

architecture rtl of cyciotest is

	signal ar			: unsigned(18 downto 0);	-- adress register
	signal reset			: std_logic;
	signal res_cnt			: unsigned(2 downto 0);

begin

	fl_a <= std_logic_vector(ar);

--
--	intern reset
--
	reset <= not res_cnt(0) or not res_cnt(1) or not res_cnt(2);

process(clk)
begin
	if rising_edge(clk) then
		if (res_cnt/="111") then
			res_cnt <= res_cnt+1;
		end if;
	end if;
end process;

process(clk, reset)

begin

	if reset='1' then
		ar <= (others => '0');
	else
		if rising_edge(clk) then
			ar <= ar + 1;
		end if;
	end if;

end process;



end rtl;
