--
--
--  This file is a part of PS2 Keyboard Controller Module 
--
--  Copyright (C) 2009, Matthias Wenzl (e0425388@student.tuwien.ac.at)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.jhjkdhjkfh
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
-- kbd_cntrl entity
-- **************************************
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

entity kbd_cntrl is
  generic (
    addr_bits :integer := 23;
    CLK_FREQ :integer := 50000000;
    TIMEOUT_REG_WIDTH :integer := 13
    );
  port ( 
    clk : in std_logic;
    reset : in std_logic;
    address		: in std_logic_vector(addr_bits-1 downto 0);
    wr_data		: in std_logic_vector(31 downto 0);
    rd : in std_logic;
    wr		: in std_logic;
    rd_data		: out std_logic_vector(31 downto 0);
    rdy_cnt		: out unsigned(1 downto 0);
    kbd_clk_oe :out std_logic;
    kbd_data_oe :out std_logic;
    kbd_clk_in :in std_logic;
    kbd_clk_out :out std_logic;
    kbd_data_in :in std_logic;
    kbd_data_out :out std_logic
    );
end entity;
