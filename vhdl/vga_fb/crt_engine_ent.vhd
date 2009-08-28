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
-- crt_engine entity
-- **************************************
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use ieee.numeric_std.all;

library WORK;
use WORK.all;

entity crt_engine is
  generic ( 
    DAC_WIDTH :integer := 10;
    WORD_WIDTH :integer := 32;
    ADDR_WIDTH :integer := 32;
    HSC_WIDTH :integer := 10;
    VSC_WIDTH :integer := 10;
    HS_PULSE_TOP :integer := 96;
    HS_FPORCH_TOP :integer := 16;
    HS_PIXEL_TOP :integer :=640;
    HS_BPORCH_TOP :integer := 48;
    VS_PULSE_TOP :integer := 2;
    VS_FPORCH_TOP :integer := 10;
    VS_LINE_TOP :integer := 480;
    VS_BPORCH_TOP :integer := 33
    );
  port ( 
    reset :in std_logic;
    async_fifo_reset :in std_logic;
    pixel_clk :in std_logic;
    ld_serializer :in std_logic;
    fifo2crt :in std_logic_vector(WORD_WIDTH-1 downto 0);
    dac_clk :out std_logic;
    serializer_empty :out std_logic;
    v_blank :out std_logic;
    h_blank :out std_logic;
    H_SYNC :out std_logic;
    V_SYNC :out std_logic;
    R :out std_logic_vector(DAC_WIDTH-1 downto 0);
    G :out std_logic_vector(DAC_WIDTH-1 downto 0);
    B :out std_logic_vector(DAC_WIDTH-1 downto 0)
    );
end entity;
