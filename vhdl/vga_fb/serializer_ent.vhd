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
-- serializer entity
-- **************************************
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

entity serializer_1to4 is
  generic ( 
    PARALLEL_WIDTH :integer := 32;
    SERIALIZED_WIDTH :integer := 8
    );
  port ( 
    clk :in std_logic;
    reset :in std_logic;
    enable :in std_logic;
    ld :in std_logic;
    d_in :in std_logic_vector(PARALLEL_WIDTH-1 downto 0);
    d_out :out std_logic_vector(SERIALIZED_WIDTH-1 downto 0);
    empty :out std_logic
    );
end entity;
