--
--  This file is part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2007, Peter Hilber
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


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.lego_pack.all;

entity sc_lego_tb is
    
end sc_lego_tb;

architecture behav of sc_lego_tb is

signal clk : std_logic := '0';
signal reset     : std_logic;

signal address   : std_logic_vector(4-1 downto 0) := "0000";

signal wr_data   : std_logic_vector(31 downto 0) := "00000000000000000000000000000000";
signal rd, wr  : std_logic := '0';
signal rd_data   : std_logic_vector(31 downto 0);
signal rdy_cnt   : unsigned(1 downto 0);
signal ma_en     : std_logic;
signal ma_l1     : std_logic;
signal ma_l2     : std_logic;
signal ma_l1_sdi : std_logic := '0';
signal ma_l1_sdo : std_logic;
signal ma_l2_sdi : std_logic := '0';
signal ma_l2_sdo : std_logic;
signal mb_en     : std_logic;
signal mb_l1     : std_logic;
signal mb_l2     : std_logic;
signal s1_pow    : std_logic;
signal s1_sdi    : std_logic := '0';
signal s1_sdo    : std_logic;
signal btn_a     : std_logic := '0';
signal btn_b     : std_logic := '0';
signal led1      : std_logic;

begin  -- behav
    dut: entity work.sc_lego
        generic map (
                addr_bits => 4,
                clk_freq  => 80000000)
        port map (
                clk       => clk,
                reset     => reset,
                address   => address,
                wr_data   => wr_data,
                rd        => rd,
                wr        => wr,
                rd_data   => rd_data,
                rdy_cnt   => rdy_cnt,
                ma_en     => ma_en,
                ma_l1     => ma_l1,
                ma_l2     => ma_l2,
                ma_l1_sdi => ma_l1_sdi,
                ma_l1_sdo => ma_l1_sdo,
                ma_l2_sdi => ma_l2_sdi,
                ma_l2_sdo => ma_l2_sdo,
                mb_en     => mb_en,
                mb_l1     => mb_l1,
                mb_l2     => mb_l2,
                s1_pow    => s1_pow,
                s1_sdi    => s1_sdi,
                s1_sdo    => s1_sdo,
                btn_a     => btn_a,
                btn_b     => btn_b,
                led1      => led1);

clock: process
begin
    wait for 10 ns;
    clk <= not clk;
end process;

proc_reset: process
begin
    reset <= '1';
    wait for 50 ns;
    reset <= '0';
    wait;
end process;

signals: process
begin  
    wait for 0.1 us;

    address <= "0001";
    wr_data(lego_motor_state'high downto 0) <= std_logic_vector(LEGO_MOTOR_STATE_FORWARD);
    wr_data(31 downto lego_motor_state'high+1) <= (others => '1');
    --wr_data(lego_motor_state'high+1+18 downto lego_motor_state'high+1) <= (14 => '1', others => '0');
    --wr_data(18+lego_motor_state'high+1+1) <= '1';
    wr <= '1';
    
    wait;
end process;

waveforms: process
begin
    ma_l1_sdi <= '0';
    wait for 100 ns;
    ma_l1_sdi <= '1';
    wait for 100 ns;
end process;

end behav;











