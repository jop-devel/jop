--
--	simple_sopc_jop.vhd
--
--	Author: Martin Schoeberl (martin@jopdesign.com)
--
--	top level for SOPC/JOP experiments
--
--	Just the minimum version with a 256x16 SRAM, boot UART,
--	and a watchdog LED
--
--	2006-08-10	created from jopcyc.vhd
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity simple_sopc_jop is

port (
	clk			: in std_logic;

	--
	--	serial interface to download the
	--	Java application and System.in/out
	--
	ser_rxd		: in std_logic;
	ser_txd		: out std_logic;

	--
	--	only one ram bank
	--
	rama_a		: out std_logic_vector(17 downto 0);
	rama_d		: inout std_logic_vector(15 downto 0);
	rama_ncs	: out std_logic;
	rama_noe	: out std_logic;
	rama_nlb	: out std_logic;
	rama_nub	: out std_logic;
	rama_nwe	: out std_logic;

	wd			: out std_logic
);
end simple_sopc_jop;

architecture rtl of simple_sopc_jop is

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	-- for generation of internal reset
	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

	signal byte_nena		: std_logic_vector(1 downto 0);
	signal address			: std_logic_vector(18 downto 0);

	signal reset_n			: std_logic;

begin

	--
	--	internal reset
	--	no external reset needed
	--
	process(clk)
	begin
		if rising_edge(clk) then
			if (res_cnt/="111") then
				res_cnt <= res_cnt+1;
			end if;

			int_res <= not res_cnt(0) or not res_cnt(1) or not res_cnt(2);
		end if;
	end process;

	reset_n <= not int_res;

	--	the SOPC generated top level
	jop: work.jop_system port map (
		clk => clk,
		reset_n => reset_n,
		ser_rxd_to_the_jop_avalon_0 => ser_rxd,
		ser_txd_from_the_jop_avalon_0 => ser_txd,
		wd_from_the_jop_avalon_0 => wd,

		-- the_tri_state_bridge_0_avalon_slave
		chipselect_n_to_the_ext_ram => rama_ncs,
		read_n_to_the_ext_ram => rama_noe,
		tri_state_bridge_0_address => address,
		tri_state_bridge_0_byteenablen => byte_nena,
		tri_state_bridge_0_data => rama_d,
		write_n_to_the_ext_ram => rama_nwe
	);
	
	rama_nlb <= byte_nena(0);
	rama_nub <= byte_nena(1);

	-- A0 from the avalon interface is NC on 16-bit SRAM
	rama_a <= address(18 downto 1);

end rtl;
