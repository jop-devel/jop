--
--
--  This file is a part of the VGA_fb Controller Module 
--
--  Copyright (C) 2009, Matthias Wenzl (e0425388@student.tuwien.ac.at)
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



-- **************************************
-- vga_fb entity
-- **************************************
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

entity vga_fb is
 generic (sh_mem_start_address :integer := 16#78500#; 
			sh_mem_end_address :integer := 16#7d000#); 
  port ( 
    reset :in std_logic;
    clk :in std_logic;
    pixel_clk :in std_logic;
    VGA_HS :out std_logic;
    VGA_VS :out std_logic;
    VGA_BLANK_N :out std_logic;
    VGA_SYNC_N :out std_logic;
    VGA_CLOCK :out std_logic;
    VGA_R :out std_logic_vector(9 downto 0);
    VGA_G :out std_logic_vector(9 downto 0);
    VGA_B :out std_logic_vector(9 downto 0);
	
	--simpcon  master interface
	address		: out std_logic_vector(22 downto 0);
	wr_data		: out std_logic_vector(31 downto 0);
	rd, wr		: out std_logic;
	rd_data		: in std_logic_vector(31 downto 0);
	rdy_cnt		: in unsigned(1 downto 0)
    );
end entity;
