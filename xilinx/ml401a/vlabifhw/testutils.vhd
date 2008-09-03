--
-- Copyright (C) 2008, Jack Whitham
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.


library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use work.vlabifhw_pack.all;

entity CounterUpDown is
        generic ( width : Natural );
        port (
            clk             : in std_logic;
            reset           : in std_logic;
            up              : in std_logic;
            down            : in std_logic;
            counter         : out std_logic_vector ( width - 1 downto 0 ) );
end entity CounterUpDown;

architecture b of CounterUpDown is
    signal counter_copy     : std_logic_vector ( width - 1 downto 0 ) ;
begin
    process ( reset , clk ) is
    begin
        if ( reset = '1' )
        then
            counter_copy <= ( others => '0' ) ;
        elsif ( clk = '1' )
        and ( clk'event )
        then
            if ( up = '1' )
            and ( down = '1' )
            then
                null;
            elsif ( up = '1' )
            then
                counter_copy <= counter_copy + 1;
            elsif ( down = '1' )
            then
                counter_copy <= counter_copy - 1;
            end if;
        end if;
    end process;
    counter <= counter_copy;
end architecture b;


library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use work.vlabifhw_pack.all;

entity Counter is
        port (
            clk             : in std_logic;
            reset           : in std_logic;
            pulse           : in std_logic;
            counter         : out std_logic_vector ( 31 downto 0 ) );
end entity Counter;

architecture b of Counter is
begin
    b2 : entity CounterUpDown
            generic map (
                width => 32 )
            port map (
                clk => clk,
                reset => reset,
                up => pulse,
                down => '0',
                counter => counter ) ;

end architecture b;

            
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use work.vlabifhw_pack.all;

entity Signal_Generator is
        port (
            clk             : in std_logic;
            reset           : in std_logic;
            out_channel_data: out std_logic_vector ( 7 downto 0 );
            out_channel_wr  : out std_logic;
            out_channel_rdy : in std_logic;
            activate        : in std_logic);
end entity Signal_Generator;

architecture b of Signal_Generator is
    signal noise        : std_logic_vector ( 11 downto 0 ) ;
    signal noise1       : std_logic_vector ( 11 downto 0 ) ;
    signal noise2       : std_logic_vector ( 11 downto 0 ) ;
    constant mtc        : std_logic_vector ( 11 downto 0 ) := 
            conv_std_logic_vector ( 7919 , 12 ) ;
begin
    process ( reset , clk ) is
    begin
        if ( reset = '1' )
        then
            out_channel_wr <= '0';
            noise <= conv_std_logic_vector ( 0, 12 );
            noise1 <= conv_std_logic_vector ( 7237, 12 );
            noise2 <= conv_std_logic_vector ( 6043, 12 );
        elsif ( clk = '1' )
        and ( clk'event )
        then
            out_channel_wr <= '0';
            if (( out_channel_rdy = '1' )
            and ( activate = '1' ))
            then
                for i in 0 to 7
                loop
                    out_channel_data ( i ) <= noise1 ( i + 0 )
                                xor noise2 ( 3 + i ) ;
                end loop;
                out_channel_wr <= '1';
                noise <= ( noise2 * mtc ) + 1 ;
                noise1 <= noise;
                noise2 <= noise1;
            end if;
        end if;
    end process;
end architecture b;


library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;
use work.vlabifhw_pack.all;

entity Checksum is
        port (
            clk             : in std_logic;
            reset           : in std_logic;
            activate        : in std_logic;
            in_channel_data : in std_logic_vector ( 7 downto 0 );
            in_channel_wr   : in std_logic;
            in_channel_rdy  : out std_logic;
            checksum        : out std_logic_vector ( 31 downto 0 );
            counter         : out std_logic_vector ( 31 downto 0 ) );
end entity Checksum;

architecture b of Checksum is
    signal receive_pulse    : std_logic;
    signal activate_d       : std_logic;
    signal checksum_copy    : std_logic_vector ( 31 downto 0 ) ;
    signal new_data         : std_logic_vector ( 31 downto 0 ) ;
begin
    c : entity Counter
        port map (
            clk => clk,
            reset => reset,
            pulse => receive_pulse,
            counter => counter );
        
    process ( reset , clk ) is
    begin
        if ( reset = '1' )
        then
            checksum_copy <= ( others => '0' ) ;
            receive_pulse <= '0';
            activate_d <= '0';
        elsif ( clk = '1' )
        and ( clk'event )
        then
            receive_pulse <= '0';
            if (( activate_d = '1' )
            and ( in_channel_wr = '1' ))
            then
                receive_pulse <= '1';
                checksum_copy <= checksum_copy + new_data;
            end if;
            activate_d <= activate;
        end if;
    end process;
    in_channel_rdy <= activate_d;
    new_data <= x"000000" & in_channel_data;
    checksum <= checksum_copy;

end architecture b;

