--
-- Virtual Lab Hardware Interface
-- vlabifhw.vhd
--
-- Generic object; provides 
--      N 8-bit channels 
--      debug chain interface
--      debug clock and reset
--
-- Author: Jack Whitham
-- $Id: vlabifhw.vhd,v 1.3 2008/11/03 11:41:29 jwhitham Exp $
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

entity vlabifhw is
    generic (
            baud_rate       : Integer := 115200;
            fifo_depth      : Integer := 4;
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
            in_channel_data : in std_logic_vector 
                                    (( 8 * ext_channels ) - 1 downto 0 );
            in_channel_wr   : in std_logic_vector 
                                    ( ext_channels - 1 downto 0 );
            in_channel_rdy  : out std_logic_vector 
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
end entity vlabifhw ;



architecture vl of vlabifhw is

    constant channels               : Natural := ext_channels + 2;
    constant leftdata               : Natural := ( channels * 8 ) - 1 ;
    constant leftrdy                : Natural := channels - 1 ;

    signal out_dbg_channel_data     : Byte;
    signal out_dbg_channel_wr       : std_logic;
    signal out_dbg_channel_rdy      : std_logic;
    signal in_dbg_channel_data      : Byte;
    signal in_dbg_channel_wr        : std_logic;
    signal in_dbg_channel_rdy       : std_logic;
    signal out_uart_channel_data    : Byte;
    signal out_uart_channel_wr      : std_logic;
    signal out_uart_channel_rdy     : std_logic;
    signal in_uart_channel_data     : Byte;
    signal in_uart_channel_wr       : std_logic;
    signal in_uart_channel_rdy      : std_logic;
    signal c_in_buffer_data         : std_logic_vector ( leftdata downto 0 ) ;
    signal c_in_buffer_present      : std_logic_vector ( leftrdy downto 0 ) ;
    signal c_in_buffer_ack          : std_logic_vector ( leftrdy downto 0 ) ;
    signal c_out_channel_data       : std_logic_vector ( leftdata downto 0 ) ;
    signal c_out_channel_rdy        : std_logic_vector ( leftrdy downto 0 ) ;
    signal c_out_channel_wr         : std_logic_vector ( leftrdy downto 0 ) ;
    signal int_reset                : std_logic;
    signal buffers_clear            : std_logic;
    signal tx_buffer_has_data       : std_logic;
    signal reset_counter            : Natural range 0 to 7 := 0;


    function Get_Buf_Size ( chan : Natural ) return Natural is
    begin
        if (( chan = 0 ) or ( chan = ( channels - 1 )))
        then
            return 1;
        else
            return fifo_depth;
        end if;
    end function Get_Buf_Size;
begin

    c : entity concentrator
        generic map (
                channels => channels )
        port map (
                clk => clk,
                reset => int_reset,
                in_buffer_data => c_in_buffer_data,
                in_buffer_ack => c_in_buffer_ack,
                in_buffer_present => c_in_buffer_present,
                out_channel_data => c_out_channel_data,
                out_channel_wr => c_out_channel_wr,
                out_channel_rdy => c_out_channel_rdy,
                buffers_clear => buffers_clear,
                active => active );

   
    buffering : for chan in 0 to ( channels - 1 )
    generate
        local : block
            constant leftslice          : Natural := ( chan * 8 ) + 7;
            constant rightslice         : Natural := ( chan * 8 ) ;
            constant c_fifo_depth       : Natural := Get_Buf_Size ( chan ) ;

            signal in_din               : Byte;
            signal in_dout              : Byte;
            signal in_rd                : std_logic;
            signal in_wr                : std_logic;
            signal in_rdy               : std_logic;
            signal in_full              : std_logic;
            signal in_has_data          : std_logic;
            signal in_empty             : std_logic;
            signal out_din              : Byte;
            signal out_dout             : Byte;
            signal out_rd               : std_logic;
            signal out_wr               : std_logic;
            signal out_rdy              : std_logic;
            signal out_full             : std_logic;
            signal out_has_data         : std_logic;
            signal out_empty            : std_logic;
            signal t_out_channel_data   : Byte;
            signal t_out_channel_wr     : std_logic;
            signal t_out_channel_rdy    : std_logic;
            
        begin
            -- Input stage. Outward-facing.
            process ( in_channel_data, in_uart_channel_data,
                    in_channel_wr, in_uart_channel_wr,
                    in_rdy ) is
            begin
                if ( chan = 0 )
                then
                    in_din <= in_uart_channel_data;
                    in_wr <= in_uart_channel_wr;
                    in_uart_channel_rdy <= in_rdy; 
                elsif ( chan = ( channels - 1 ))
                then
                    in_din <= in_dbg_channel_data;
                    in_wr <= in_dbg_channel_wr;
                    in_dbg_channel_rdy <= in_rdy;
                else
                    in_din <= in_channel_data ( 
                                leftslice - 8 downto rightslice - 8 );
                    in_wr <= in_channel_wr ( chan - 1 );
                    in_channel_rdy ( chan - 1 ) <= in_rdy
                                    and not in_channel_wr ( chan - 1 );
                end if ;
            end process ;

            -- Input stage. FIFO.
            in_rdy <= not in_full;
            in_has_data <= not in_empty;
            in_fifo : entity fifo
                generic map (
                    width => 8,
                    depth => c_fifo_depth,
                    thres => 1 )
                port map (
                    clk => clk,
                    reset => int_reset,
                    din	=> in_din,
                    dout => in_dout,

                    rd => in_rd,
                    wr => in_wr,

                    empty => in_empty,
                    full => in_full,
                    half => open);



            -- Input stage. Inward-facing.
            in_rd <= c_in_buffer_ack ( chan );
            c_in_buffer_data ( leftslice downto rightslice ) <= in_dout;
            c_in_buffer_present ( chan ) <= in_has_data;

            -- Output stage. Inward-facing.
            out_din <= c_out_channel_data ( leftslice downto rightslice );
            out_wr <= c_out_channel_wr ( chan );
            c_out_channel_rdy ( chan ) <= out_rdy;

            -- Output stage. FIFO.
            out_rdy <= not out_full;
            out_has_data <= not out_empty;
            out_fifo : entity fifo
                generic map (
                    width => 8,
                    depth => c_fifo_depth,
                    thres => 1 )
                port map (
                    clk => clk,
                    reset => int_reset,
                    din	=> out_din,
                    dout => out_dout,

                    rd => out_rd,
                    wr => out_wr,

                    empty => out_empty,
                    full => out_full,
                    half => open);

            -- Output stage. Conversion register.
            process ( clk, int_reset ) is
            begin
                if ( int_reset = '1' )
                then
                    t_out_channel_data <= x"00";
                    t_out_channel_wr <= '0';
                    out_rd <= '0';

                elsif (( clk = '1' )
                and ( clk'event ))
                then
                    t_out_channel_wr <= '0';
                    out_rd <= '0';

                    if (( out_has_data = '1' )
                    and ( t_out_channel_rdy = '1' ))
                    then
                        t_out_channel_data <= out_dout;
                        t_out_channel_wr <= '1';
                        out_rd <= '1';
                    end if;
                end if;
            end process;
                        

            -- Output stage. Outward-facing.
            process ( out_uart_channel_rdy , out_uart_channel_rdy , 
                    tx_buffer_has_data , t_out_channel_data , 
                    t_out_channel_wr , out_empty ) is
            begin
                if ( chan = 0 )
                then
                    buffers_clear <= out_empty 
                            and ( not tx_buffer_has_data );
                    out_uart_channel_data <= t_out_channel_data ;
                    out_uart_channel_wr <= t_out_channel_wr ;
                    t_out_channel_rdy <= out_uart_channel_rdy ;
                elsif ( chan = ( channels - 1 ))
                then
                    out_dbg_channel_data <= t_out_channel_data ;
                    out_dbg_channel_wr <= t_out_channel_wr ;
                    t_out_channel_rdy <= out_dbg_channel_rdy ;
                else
                    out_channel_data ( leftslice - 8 downto rightslice - 8 ) 
                                    <= t_out_channel_data ;
                    out_channel_wr ( chan - 1 ) <= t_out_channel_wr ;
                    t_out_channel_rdy <= out_channel_rdy ( chan - 1 ) 
                                    and not t_out_channel_wr ;
                end if ;
            end process ;
        end block local ;
    end generate buffering ;

    pc_uart : entity chuart
        generic map (
            txf_depth => 1,
            txf_thres => 1,
            rxf_depth => 24,
            rxf_thres => 1,
            clk_freq => clock_freq,
            baud_rate => baud_rate )
        port map (
            clk => clk,
            reset => int_reset,

            -- Status information
            rx_buffer_full => open,
            rx_buffer_half_full => open,
            rx_buffer_overflow => open,
            rx_data_present => open,
            tx_buffer_full => open,
            tx_buffer_half_full => tx_buffer_has_data,

            -- Data buses
            out_channel_data => in_uart_channel_data,
            out_channel_wr => in_uart_channel_wr,
            out_channel_rdy => in_uart_channel_rdy,

            in_channel_data => out_uart_channel_data, 
            in_channel_wr => out_uart_channel_wr,
            in_channel_rdy => out_uart_channel_rdy,

            -- External connections
            txd => hw_tx,
            rxd	=> hw_rx,
            ncts => '0',
            nrts => open );

    d : entity debugger 
        generic map (
            less_features => less_features )
        port map (
            clk => clk,
            reset => int_reset,

            out_channel_data => in_dbg_channel_data,
            out_channel_wr => in_dbg_channel_wr,
            out_channel_rdy => in_dbg_channel_rdy,
 
            in_channel_data => out_dbg_channel_data,
            in_channel_wr => out_dbg_channel_wr,
            in_channel_rdy => out_dbg_channel_rdy,

            debug_clock => debug_clock,
            debug_reset => debug_reset,
            breakpoint => breakpoint,

            dc_control => dc_control,
            dc_out => dc_out,
            dc_in => dc_in
        );


    process ( clk, reset ) is
    begin
        if ( reset = '1' )
        then
            int_reset <= '1';
            reset_counter <= 0;
        elsif (( clk = '1' )
        and ( clk'event ))
        then
            if ( reset_counter = 7 )
            then
                int_reset <= '0';
            else
                reset_counter <= reset_counter + 1;
                int_reset <= '1';
            end if;
        end if;
    end process;

end architecture vl ;


