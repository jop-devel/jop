--
--
--  This file is a part of PS2 Keyboard Controller Module 
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
-- ps2_interface entity
-- **************************************
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

entity ps2_interface is
  generic ( 
    SYS_CLK_FREQ :integer := 50000000;
    TIMEOUT_BIT_WIDTH :integer := 13
    );
  port ( 
    reset :in std_logic;
    clk :in std_logic;
    data_in :in std_logic_vector(7 downto 0);
    data_out :out std_logic_vector(7 downto 0);
    wr_word :in std_logic;
    rd_buf :in std_logic;
    rcv_rdy :out std_logic;
    snd_rdy :out std_logic;
    parity_error :out std_logic;
    ps_datain :in std_logic;
    ps_dataout :out std_logic;
    data_oe :out std_logic;
    ps_clkin :in std_logic;
    ps_clkout :out std_logic;
    clk_oe :out std_logic
    );
end entity;
