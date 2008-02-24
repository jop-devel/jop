--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2007, Christof Pitter
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--


--
--	cyc3_sopc_jop.vhd
--
--	Author: CP
--
--	top level for SOPC/JOP experiments
--



library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity cyc3_sopc_jop is
generic( addr_bits	: integer := 20;	-- address range for the avalon interface
		 data_bits	: integer := 32
);
port (
	clk			: in std_logic;

-- SSRAM memory interface	  
	
	clk_SSRAM : out std_logic; 
	-- ZZ    : out STD_LOGIC;                                   
	-- Mode  : out STD_LOGIC;                                   
	ADDR  : out STD_LOGIC_VECTOR ((addr_bits -1) downto 0); -- Address
	-- nGW   : out STD_LOGIC;                                   
	nBWE  : out STD_LOGIC;             --                      
	nBWd  : out STD_LOGIC;             --                      
	nBWc  : out STD_LOGIC;             --                      
	nBWb  : out STD_LOGIC;             --                      
	nBWa  : out STD_LOGIC;             --                      
	nCE1  : out STD_LOGIC;                                   
	-- CE2   : out STD_LOGIC;                                   
	-- nCE3  : out STD_LOGIC;                                   
	-- nADSP : out STD_LOGIC;									                                   
	nADSC : out STD_LOGIC;             -- Address Strobe Contr.  
	-- nADV  : out STD_LOGIC;                                  
	nOE   : out STD_LOGIC;				-- Output Enable                                
	DQ   : INOUT STD_LOGIC_VECTOR ((data_bits-1) downto 0);	-- Data  
	
	wd			: out std_logic
);
end cyc3_sopc_jop;

architecture rtl of cyc3_sopc_jop is

	component cyc3_pll is
	--generic (multiply_by : natural; divide_by : natural);
	port (
		inclk0		: in std_logic;
		c0			: out std_logic;
		c1			: out std_logic
	);
	end component;

	signal clk_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	-- for generation of internal reset
	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

	signal byte_nena		: std_logic_vector(1 downto 0);
	signal address			: std_logic_vector(18 downto 0);

	signal reset_n			: std_logic;

	signal ba, dqm			: std_logic_vector(1 downto 0);
	
	-- NO Serial Interface available on the board
	signal ser_rxd			: std_logic := '0';
	signal ser_txd			: std_logic := '0';
	
begin

	--
	--	internal reset
	--	no external reset needed
	--
	process(clk_int)
	begin
		if rising_edge(clk_int) then
			if (res_cnt/="111") then
				res_cnt <= res_cnt+1;
			end if;

			int_res <= not res_cnt(0) or not res_cnt(1) or not res_cnt(2);
		end if;
	end process;

	reset_n <= not int_res;

	pll_ssram : cyc3_pll
	--generic map(
	--	multiply_by => pll_mult,
	--	divide_by => pll_div
	--)
	port map (
		inclk0 => clk,
		c0 => clk_int,
		c1 => clk_SSRAM		-- -3ns
	);

	--	the SOPC generated top level
	jop: work.jop_system port map (
		clk => clk_int,
		reset_n => reset_n,
		
		-- the_jop_avalon_1
        ser_rxd_to_the_jop_avalon_1 => ser_rxd, -- we do not have a serial interface
        ser_txd_from_the_jop_avalon_1 => ser_txd,
        wd_from_the_jop_avalon_1 => wd,

        -- the_tristate_bridge_avalon_slave
        address_to_the_ssram => ADDR(19 downto 0),
        adsc_n_to_the_ssram => nADSC,
        bw_n_to_the_ssram(0) => nBWa,
		bw_n_to_the_ssram(1) => nBWb,
		bw_n_to_the_ssram(2) => nBWc,
		bw_n_to_the_ssram(3) => nBWd,
        bwe_n_to_the_ssram => nBWE,
        chipenable1_n_to_the_ssram => nCE1,
        data_to_and_from_the_ssram => DQ(31 downto 0),
        outputenable_n_to_the_ssram => nOE
		
		
	);
	

end rtl;
