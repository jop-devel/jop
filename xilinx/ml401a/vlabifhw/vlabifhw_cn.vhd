--
-- Virtual Lab Hardware Interface, with clock neutral channels
--
-- Author: Jack Whitham
-- $Id: vlabifhw_cn.vhd,v 1.3 2008/11/03 11:41:29 jwhitham Exp $
-- 
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

entity vlabifhw_cn is
    generic (
            baud_rate       : Integer := 115200;
            fifo_depth      : Integer := 16;
            less_features   : Boolean := false;
            ext_channels    : Integer;
            clock_freq      : Integer
        );
    port (
            -- External hardware connections
            clk             : in std_logic;
            reset           : in std_logic;
            hw_tx           : out std_logic;
            hw_rx           : in std_logic;

            -- Connections for devices under test: channels
            in_channel_clks : in std_logic_vector 
                                    ( ext_channels - 1 downto 0 );
            in_channel_data : in std_logic_vector 
                                    (( 8 * ext_channels ) - 1 downto 0 );
            in_channel_wr   : in std_logic_vector 
                                    ( ext_channels - 1 downto 0 );
            in_channel_rdy  : out std_logic_vector 
                                    ( ext_channels - 1 downto 0 );
            out_channel_clks: in std_logic_vector 
                                    ( ext_channels - 1 downto 0 );
            out_channel_data: out std_logic_vector 
                                    (( 8 * ext_channels ) - 1 downto 0 );
            out_channel_wr  : out std_logic_vector 
                                    ( ext_channels - 1 downto 0 );
            out_channel_rdy : in std_logic_vector 
                                    ( ext_channels - 1 downto 0 );
            -- Activation signal
            active          : out std_logic;

            -- Controls for device under test
            debug_clock     : out std_logic;
            debug_reset     : out std_logic;
            breakpoint      : in std_logic;

            -- Debug chain input/output 
            dc_control      : out DC_Control_Wires;
            dc_out          : out std_logic;
            dc_in           : in std_logic
        );
end entity vlabifhw_cn ;



architecture vl of vlabifhw_cn is
    constant channels               : Natural := ext_channels;
    constant leftdata               : Natural := ( channels * 8 ) - 1 ;
    constant leftrdy                : Natural := channels - 1 ;
    constant leftdata2              : Natural := ( channels * 16 ) - 1 ;
    constant leftrdy2               : Natural := ( channels * 2 ) - 1 ;

    signal c_in_channel_data        : std_logic_vector ( leftdata downto 0 ) ;
    signal c_in_channel_rdy         : std_logic_vector ( leftrdy downto 0 ) ;
    signal c_in_channel_wr          : std_logic_vector ( leftrdy downto 0 ) ;
    signal c_out_channel_data       : std_logic_vector ( leftdata downto 0 ) ;
    signal c_out_channel_rdy        : std_logic_vector ( leftrdy downto 0 ) ;
    signal c_out_channel_wr         : std_logic_vector ( leftrdy downto 0 ) ;

    signal source_data              : std_logic_vector ( leftdata2 downto 0 ) ;
    signal source_wr                : std_logic_vector ( leftrdy2 downto 0 ) ;
    signal source_rdy               : std_logic_vector ( leftrdy2 downto 0 ) ;
    signal source_clks              : std_logic_vector ( leftrdy2 downto 0 ) ;
    signal target_data              : std_logic_vector ( leftdata2 downto 0 ) ;
    signal target_wr                : std_logic_vector ( leftrdy2 downto 0 ) ;
    signal target_rdy               : std_logic_vector ( leftrdy2 downto 0 ) ;
    signal target_clks              : std_logic_vector ( leftrdy2 downto 0 ) ;

    type State_Type is ( IN_READY, IN_RECEIVED, OUT_READY, OUT_RESET ) ;

begin
    vl : entity vlabifhw
        generic map (
            baud_rate => baud_rate,
            fifo_depth => fifo_depth,
            ext_channels => channels,
            less_features => less_features,
            clock_freq => clock_freq)
        port map (
            -- External hardware connections
            clk => clk,
            reset => reset,
            hw_tx => hw_tx,
            hw_rx => hw_rx,

            -- Connections for devices under test: channels
            in_channel_data => c_in_channel_data,
            in_channel_wr => c_in_channel_wr,
            in_channel_rdy => c_in_channel_rdy,
            out_channel_data => c_out_channel_data,
            out_channel_wr => c_out_channel_wr,
            out_channel_rdy => c_out_channel_rdy,
            -- Activation signal
            active => active,

            -- Controls for device under test
            debug_clock => debug_clock,
            debug_reset => debug_reset,
            breakpoint => breakpoint,

            -- Debug chain input/output 
            dc_control => dc_control,
            dc_out => dc_out,
            dc_in => dc_in);

    gather : for chan in 0 to ( channels - 1 )
    generate
        t1 : block
            constant leftslice          : Natural := ( chan * 8 ) + 7;
            constant rightslice         : Natural := ( chan * 8 ) ;
            constant off                : Natural := channels * 8 ;
        begin
            source_data ( leftslice downto rightslice ) 
                    <= c_out_channel_data ( leftslice downto rightslice ) ;
            source_wr ( chan ) <= c_out_channel_wr ( chan ) ;
            c_out_channel_rdy ( chan ) <= source_rdy ( chan ) ;
            source_clks ( chan ) <= clk ;

            out_channel_data ( leftslice downto rightslice ) 
                    <= target_data ( leftslice downto rightslice ) ;
            out_channel_wr ( chan ) <= target_wr ( chan ) ;
            target_rdy ( chan ) <= out_channel_rdy ( chan ) ;
            target_clks ( chan ) <= out_channel_clks ( chan ) ;

            c_in_channel_data ( leftslice downto rightslice ) 
                    <= target_data ( off + leftslice downto off + rightslice ) ;
            c_in_channel_wr ( chan ) <= target_wr ( chan + channels ) ;
            target_rdy ( chan + channels ) <= c_in_channel_rdy ( chan ) ;
            target_clks ( chan + channels ) <= clk ;

            source_clks ( chan + channels ) <= in_channel_clks ( chan ) ;
            source_data ( off + leftslice downto off + rightslice ) 
                    <= in_channel_data ( leftslice downto rightslice ) ;
            source_wr ( chan + channels ) <= in_channel_wr ( chan ) ;
            in_channel_rdy ( chan ) <= source_rdy ( chan + channels ) ;
        end block t1 ;
    end generate gather ;


    cross_clock : for chan in 0 to (( channels * 2 ) - 1 )
    generate
        local : block
            constant leftslice          : Natural := ( chan * 8 ) + 7;
            constant rightslice         : Natural := ( chan * 8 ) ;
            signal state                : State_Type ;
            signal is_sent              : std_logic ;
        begin
            source_rdy ( chan ) <= '1' when ( state = IN_READY )
                            else '0' ;

            process ( reset , target_clks ) is
            begin
                if ( reset = '1' )
                then
                    is_sent <= '0' ;
                    target_wr ( chan ) <= '0' ;

                elsif ( target_clks ( chan ) = '1' )
                and ( target_clks ( chan )'event )
                then
                    target_wr ( chan ) <= '0' ;
                    if (( state = OUT_READY )
                    and ( is_sent = '0' )
                    and ( target_rdy ( chan ) = '1' ))
                    then
                        is_sent <= '1';
                        target_wr ( chan ) <= '1' ;
                    end if ; 
                    if ( state = OUT_RESET )
                    then
                        is_sent <= '0' ;
                    end if ;
                end if ;
            end process ;

            process ( reset , source_clks ) is
            begin
                if ( reset = '1' )
                then
                    state <= IN_READY ;
                elsif ( source_clks ( chan ) = '1' )
                and ( source_clks ( chan )'event )
                then
                    case state is
                    when IN_READY =>
                        if ( source_wr ( chan ) = '1' )
                        then
                            state <= IN_RECEIVED ;
                            target_data ( leftslice downto rightslice ) 
                                        <= source_data ( leftslice 
                                                        downto rightslice ) ;
                        end if ; 
                    when IN_RECEIVED =>
                        state <= OUT_READY ;
                    when OUT_READY =>
                        if ( is_sent = '1' )
                        then
                            state <= OUT_RESET ;
                        end if ;
                    when OUT_RESET =>
                        if ( is_sent = '0' )
                        then
                            state <= IN_READY ;
                        end if ;
                    end case ;
                end if ;
            end process ;
        end block local ;
    end generate cross_clock ;



end architecture vl ;


