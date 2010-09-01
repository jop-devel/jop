--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
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
--	dm9000a.vhd
--
--
--	2010-06-17	created
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;

entity dm9000a is
port (
	clk		: in std_logic;
	reset	: in std_logic;

--
--	SimpCon IO interface
--
	sc_rd		: in std_logic;
	sc_rd_data	: out std_logic_vector(31 downto 0);
	
	sc_wr		: in std_logic;
	sc_wr_data	: in std_logic_vector(31 downto 0);
	
	sc_rdy_cnt	: out unsigned(1 downto 0);

--
--	Control signals
--

	oENET_CMD			: out std_logic;
	oENET_IOR_N		: out std_logic;
	oENET_IOW_N		: out std_logic;
	oENET_RESET_N	: out std_logic;
	oENET_CS_N		: out std_logic;
	iENET_INT			: in std_logic;

--
--	Data signal
--

	ENET_D				: inout std_logic_vector(15 downto 0)

 );
end dm9000a;


architecture rtl of dm9000a is

	signal data_dir						: std_logic; --'1' is driveout
	signal local_ENET_D				: std_logic_vector(15 downto 0);

begin

	sc_rdy_cnt <= "00";
	
	
	oENET_CS_N <= '0';

	process(CLK,RESET)
	begin
		if RESET = '1' then
			oENET_CMD <= '0';
			oENET_RESET_N <= '0';
			oENET_IOR_N <= '1';
			oENET_IOW_N <= '1';
			sc_rd_data <= (others => '0');
			data_dir <= '0';
			local_ENET_D <= (others => '0');

		elsif rising_edge(clk) then
			if sc_rd = '1' then
				sc_rd_data(31 downto 22) <= (others => '0');
				sc_rd_data(21) <= iENET_INT;
				sc_rd_data(20 downto 16) <= (others => '0');
				sc_rd_data(15 downto 0) <= ENET_D;
			end if;
			if sc_wr = '1' then
				oENET_RESET_N <= not sc_wr_data(20);
				oENET_CMD <= sc_wr_data(19);
				data_dir <= sc_wr_data(18);
				oENET_IOR_N <= not sc_wr_data(17);
				oENET_IOW_N <= not sc_wr_data(16);
				local_ENET_D <= sc_wr_data(15 downto 0);
			end if;
		end if;
	end process;
	
	
	-- Tristate:
	ENET_D <= local_ENET_D when data_dir = '1' else (others => 'Z');

end rtl;
