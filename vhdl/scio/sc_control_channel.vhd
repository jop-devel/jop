--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2008, Jack Whitham
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
--	sc_control_channel.vhd
--
--  32 bit parallel interface for the control channel;
--  mimics a serial port UART device. Data is sent in a packet
--  form (with a header word and zero or more payload words).
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned."+";
use ieee.numeric_std.all;

entity sc_control_channel is
generic (addr_bits : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

-- SimpCon interface

	address		: in std_logic_vector(addr_bits-1 downto 0);
	wr_data		: in std_logic_vector(31 downto 0);
	rd, wr		: in std_logic;
	rd_data		: out std_logic_vector(31 downto 0);
	rdy_cnt		: out unsigned(1 downto 0);

    inhibit     : in std_logic;
    cc_out_data : out std_logic_vector(31 downto 0);
    cc_out_wr   : out std_logic;
    cc_out_rdy  : in std_logic;

    cc_in_data  : in std_logic_vector(31 downto 0);
    cc_in_wr    : in std_logic;
    cc_in_rdy   : out std_logic
);
end sc_control_channel;

architecture rtl of sc_control_channel is

	signal ua_wr, tdre		: std_logic;
	signal rdrf		        : std_logic;
	signal cc_in_full       : std_logic;
	signal cc_in_reg        : std_logic_vector(31 downto 0);
	signal cc_out_wr_d      : std_logic;
    signal read_counter     : std_logic_vector(7 downto 0);

begin
	rdy_cnt <= "00";	-- no wait states

    process(clk, reset)
    begin
        if (reset='1') then
            rd_data <= ( others => '0' ) ;
            cc_in_full <= '0';
            cc_in_reg <= ( others => '0' ) ;
            cc_out_wr <= '0';
            cc_out_wr_d <= '0';
            read_counter <= ( others => '0' ) ;

        elsif rising_edge(clk) then

            if cc_in_wr = '1' then
                cc_in_reg <= cc_in_data;
                cc_in_full <= '1';
            end if;

            cc_out_wr <= cc_out_wr_d;
            cc_out_wr_d <= '0';
            if rd='1' then
                rd_data <= ( others => '0' ) ;
                -- UART-style address decoder:
                -- 0: control
                -- 1: data
                if address(0)='0' then
                    rd_data(1 downto 0) <= rdrf & tdre;
                    read_counter <= read_counter + 1 ;
                else
                    rd_data <= cc_in_reg;
                    cc_in_full <= '0';
                end if;
            elsif ua_wr = '1' then
                cc_out_data <= wr_data;
                cc_out_wr_d <= '1';
            end if;
        end if;
    end process;

	-- write is on address offest 1
	ua_wr <= wr and address(0);
    tdre <= cc_out_rdy and not inhibit;
    rdrf <= cc_in_full or inhibit;
    cc_in_rdy <= not cc_in_full ;


end rtl;

