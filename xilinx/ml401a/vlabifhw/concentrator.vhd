--
-- Virtual Lab Hardware Interface
-- concentrator.vhd
--
-- The internal logic for the hardware interface.
-- All the buffering and external comms are provided by vlabhwif.vhd
-- There have to be at least two channels (see entity definition).
--
-- Author: Jack Whitham
-- $Id: concentrator.vhd,v 1.3 2008/11/03 11:41:29 jwhitham Exp $
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

entity concentrator is
    generic ( channels        : Integer );
    port (
            -- External hardware connections
            clk                 : in std_logic;
            reset               : in std_logic;

            -- Connections for devices under test: channels
            -- Channel 0 is a UART link to the PC
            -- Channel 1 acts as a virtual UART for the device under test
            in_buffer_data      : in std_logic_vector 
                                    (( 8 * channels ) - 1 downto 0 );
            in_buffer_present   : in std_logic_vector ( channels - 1 downto 0 );
            in_buffer_ack       : out std_logic_vector 
                                    ( channels - 1 downto 0 );
            out_channel_data    : out std_logic_vector 
                                    (( 8 * channels ) - 1 downto 0 );
            out_channel_wr      : out std_logic_vector 
                                    ( channels - 1 downto 0 );
            out_channel_rdy     : in std_logic_vector ( channels - 1 downto 0 );

            -- Information about the channels
            buffers_clear       : in std_logic;

            -- Activation signal
            active              : out std_logic
        );
end entity concentrator ;


architecture vl of concentrator is

    type State_Type is ( INIT, CHECK_ACTIVATION, INIT_1,
            LAB_READY, LAB_WAIT, LAB_DISPATCH, LAB_INIT, LAB_READY_1,
            LAB_INFO, LAB_INFO_1, LAB_INFO_2,
            LAB_GET_CHANNELS, 
            LAB_GET_CHANNELS_2, LAB_GET_CHANNELS_3, LAB_GET_CHANNELS_6, 
            LAB_SEND_CHANNEL, 
            LAB_SEND_CHANNEL_2, LAB_SEND_CHANNEL_3,
            LAB_SEND_CHANNEL_5, LAB_SEND_CHANNEL_6);

    type Command_Type is ( CMD_STANDBY, 
            CMD_SEND_TO_PC_LITERAL, 
            CMD_SEND_TO_PC_ESCAPED, CMD_SEND_TO_PC_ESCAPED_1,
            CMD_SEND_TO_PC_ESCAPED_2,
            CMD_RECEIVE_FROM_PC);

    type Disp_Type is ( DISP_INFO, DISP_WAIT, DISP_SEND_CHANNEL);

    signal activation_state         : Natural range 0 to 7 ;

    signal state                    : State_Type ;
    signal command                  : Command_Type ;

    signal input_reg, output_reg    : Byte ;


    signal counter                  : Natural range 0 to 15 := 7;

    signal in_mux_data              : Byte;
    signal in_mux_ack               : std_logic;
    signal in_mux_present           : std_logic;
    signal out_mux_wr               : std_logic;
    signal out_mux_rdy              : std_logic;
    signal int_active               : std_logic;
    signal complete                 : std_logic;
    signal dwbc_rdy                 : std_logic;
    signal data_waiting             : std_logic;
    signal chan_mux_setting         : Natural range 0 to 15;
    signal channel_number           : Nibble;


begin
   
    assert channels > 1 ;
    assert channels <= 16 ;

    active <= int_active;
    channel_number <= conv_std_logic_vector ( chan_mux_setting , 4 ) ;

    process ( clk , reset ) is
        variable next_command   : Command_Type;
        variable received       : Disp_Type;
        variable correct        : Boolean;
        variable counter_zero   : Boolean;
    begin
        if ( reset = '1' )
        then
            state <= INIT ;
            command <= CMD_STANDBY;
            int_active <= '0';
            counter <= 7;
            in_mux_ack <= '0';
            out_mux_wr <= '0';
            chan_mux_setting <= 1;
            in_buffer_ack ( 0 ) <= '0';
            out_channel_wr ( 0 ) <= '0';
            complete <= '0';

        elsif (( clk = '1' )
        and ( clk'event ))
        then
            in_buffer_ack ( 0 ) <= '0';
            out_channel_wr ( 0 ) <= '0';
            in_mux_ack <= '0';
            out_mux_wr <= '0';
            complete <= '0';

            correct := ((( activation_state = 0 ) and ( input_reg = x"f6" ))
                or (( activation_state = 1 ) and ( input_reg = x"6c" ))
                or (( activation_state = 2 ) and ( input_reg = x"61" ))
                or (( activation_state = 3 ) and ( input_reg = x"62" ))
                or (( activation_state = 4 ) and ( input_reg = x"70" ))) ;

            case input_reg is
            when RX_COMMAND_INFO =>         
                received := DISP_INFO;
            when RX_COMMAND_SEND_CHANNEL => 
                received := DISP_SEND_CHANNEL;
            when others =>                  
                received := DISP_WAIT;
            end case ;
            next_command := CMD_STANDBY;
            counter_zero := ( counter = 0 ) ;

            case state is
            when INIT =>
                -- Virtual lab inactive state
                -- In this state I am waiting for a magic activation
                -- code which will activate
                -- the virtual lab features. I will discard anything
                -- that arrives through channels >= 2. I will relay
                -- information between channels 0 and 1.
                int_active <= '0';
                chan_mux_setting <= 1;
                if (( in_buffer_present ( 0 ) = '1' )
                and ( out_mux_rdy = '1' ))
                then
                    -- Data from PC UART - check for activation code
                    next_command := CMD_RECEIVE_FROM_PC;
                    state <= CHECK_ACTIVATION;
                elsif (( in_mux_present = '1' )
                and ( out_channel_rdy ( 0 ) = '1' ))
                then
                    -- Data from virtual UART: relay to PC UART now
                    next_command := CMD_SEND_TO_PC_LITERAL;
                    output_reg <= in_mux_data;
                    in_mux_ack <= '1';
                    state <= INIT_1;
                end if ;

            when INIT_1 =>
                if ( complete = '1' )
                then
                    state <= INIT;
                end if;

            when CHECK_ACTIVATION =>
                if ( complete = '0' )
                then
                    null;
                elsif (( not counter_zero ) and correct )
                then
                    -- Activation code being received from PC
                    if ( activation_state = 4 )
                    then
                        state <= LAB_INIT;
                    else
                        state <= INIT ;
                    end if ;
                    activation_state <= activation_state + 1 ;
                else
                    -- Relay PC UART data to virtual UART
                    out_mux_wr <= '1';
                    activation_state <= 0 ;
                    if ( not counter_zero )
                    then
                        counter <= counter - 1;
                    end if;
                    state <= INIT;
                end if ;

----------------------------------------------------------------------------- 
            when LAB_INIT =>
                chan_mux_setting <= channels - 1;
                int_active <= '1';
                state <= LAB_READY;

            when LAB_READY =>
                -- Send READY packet
                output_reg <= TX_PACKET_READY;
                state <= LAB_READY_1;
                next_command := CMD_SEND_TO_PC_LITERAL;

            when LAB_READY_1 =>
                if ( complete = '1' )
                then
                    state <= LAB_WAIT;
                end if;
            
            when LAB_WAIT =>
                -- Wait for command
                if ( in_buffer_present ( 0 ) = '1' )
                then
                    -- Command from PC 
                    next_command := CMD_RECEIVE_FROM_PC;
                    state <= LAB_DISPATCH;
                elsif ( dwbc_rdy = '1' )
                then
                    -- Data in one of the other channels
                    state <= LAB_GET_CHANNELS;
                end if;

            when LAB_DISPATCH =>
                if ( complete = '1' )
                then
                    case received is
                    when DISP_INFO =>         
                        state <= LAB_INFO ;
                    when DISP_SEND_CHANNEL => 
                        next_command := CMD_RECEIVE_FROM_PC;
                        state <= LAB_SEND_CHANNEL;
                    when others =>                  
                        state <= LAB_WAIT;
                    end case ;
                end if ;

----------------------------------------------------------------------------- 
            when LAB_INFO =>
                -- Return information about the system.
                output_reg <= TX_PACKET_INFO;
                next_command := CMD_SEND_TO_PC_LITERAL;
                state <= LAB_INFO_1;

            when LAB_INFO_1 =>
                if ( complete = '1' )
                then
                    output_reg ( 7 downto 4 ) <= conv_std_logic_vector (
                                channels , 4 ) ;
                    output_reg ( 3 downto 0 ) <= VERSION_CODE;
                    next_command := CMD_SEND_TO_PC_ESCAPED;
                    state <= LAB_INFO_2;
                end if;

            when LAB_INFO_2 =>
                if ( complete = '1' )
                then
                    state <= LAB_READY;
                end if;

----------------------------------------------------------------------------- 
            when LAB_GET_CHANNELS =>
                -- Receive data waiting in incoming channels
                -- (except channel 0, which is being used for PC comms)
                if ( in_mux_present = '0' )
                then
                    -- Ensure that we test every channel; always
                    -- "round robin" to the next channel after
                    -- attempting to receive data from one.
                    if ( chan_mux_setting = 1 )
                    then
                        chan_mux_setting <= channels - 1;
                    else
                        chan_mux_setting <= chan_mux_setting - 1;
                    end if;
                    state <= LAB_WAIT;
                else
                    -- data is present.. send a start of data packet
                    output_reg <= TX_PACKET_CHANNEL_DATA;
                    next_command := CMD_SEND_TO_PC_LITERAL;
                    state <= LAB_GET_CHANNELS_2;

                end if;

            when LAB_GET_CHANNELS_2 =>
                -- now say *which* channel has the new data
                if ( complete = '1' )
                then
                    output_reg <= ( others => '0' );
                    output_reg ( 7 downto 4 ) <= channel_number;
                    
                    counter <= 15 ;
                    next_command := CMD_SEND_TO_PC_ESCAPED;
                    state <= LAB_GET_CHANNELS_3;
                end if;
            
            when LAB_GET_CHANNELS_3 =>
                if ( complete = '0' )
                then
                    null;
                elsif (( out_channel_rdy ( 0 ) = '0' ) -- output isn't ready
                or ( counter_zero )                  -- sent enough bytes
                or ( in_mux_present = '0' ))        -- no more data
                then 
                    -- end transmission
                    output_reg <= TX_PACKET_END_TX ;
                    next_command := CMD_SEND_TO_PC_LITERAL;
                    state <= LAB_GET_CHANNELS_6; 
                else
                    -- relay byte and look for another
                    counter <= counter - 1;
                    output_reg <= in_mux_data;
                    in_mux_ack <= '1';
                    next_command := CMD_SEND_TO_PC_ESCAPED;
                end if;

            when LAB_GET_CHANNELS_6 =>
                if ( complete = '1' )
                then
                    -- Round robin
                    if ( chan_mux_setting = 1 )
                    then
                        chan_mux_setting <= channels - 1;
                    else
                        chan_mux_setting <= chan_mux_setting - 1;
                    end if;
                    state <= LAB_WAIT ;
                end if;

----------------------------------------------------------------------------- 
            when LAB_SEND_CHANNEL =>
                -- Send a byte through a channel.
                if ( complete = '1' )
                then
                    chan_mux_setting <= conv_integer ( 
                                input_reg ( 3 downto 0 ) ) ;
                    counter <= conv_integer ( 
                                input_reg ( 7 downto 4 ) ) ;
                    state <= LAB_SEND_CHANNEL_2;
                end if;

            when LAB_SEND_CHANNEL_2 =>
                if ( counter_zero )
                then
                    -- End of data: exit loop
                    state <= LAB_READY;
                else
                    next_command := CMD_RECEIVE_FROM_PC;
                    state <= LAB_SEND_CHANNEL_3;
                end if;
                    
            when LAB_SEND_CHANNEL_3 =>
                if ( complete = '1' )
                then
                    counter <= counter - 1;
                    if ( out_mux_rdy = '1' )
                    then
                        out_mux_wr <= '1';
                        state <= LAB_SEND_CHANNEL_2;
                    else
                        -- Overflow: exit loop
                        output_reg <= TX_PACKET_OVERFLOW;
                        next_command := CMD_SEND_TO_PC_LITERAL;
                        state <= LAB_SEND_CHANNEL_5;
                    end if;
                end if;

            when LAB_SEND_CHANNEL_5 => 
                if ( complete = '1' )
                then
                    output_reg ( 7 downto 4 ) <= channel_number ;
                    output_reg ( 3 downto 0 ) <= conv_std_logic_vector (
                                counter , 4 ) ;

                    next_command := CMD_SEND_TO_PC_ESCAPED;
                    state <= LAB_SEND_CHANNEL_6;
                end if;

            when LAB_SEND_CHANNEL_6 => 
                -- Absorb bytes that overflowed
                if ( complete = '0' )
                then
                    null;
                elsif ( counter_zero )
                then
                    -- End of overflow bytes: exit loop
                    state <= LAB_READY;
                else
                    next_command := CMD_RECEIVE_FROM_PC;
                    counter <= counter - 1;
                    state <= LAB_SEND_CHANNEL_6;
                end if;

----------------------------------------------------------------------------- 
            end case ;

            case command is
----------------------------------------------------------------------------- 
            when CMD_STANDBY =>
                case next_command is
                when CMD_SEND_TO_PC_LITERAL =>
                        command <= CMD_SEND_TO_PC_LITERAL;
                when CMD_SEND_TO_PC_ESCAPED =>
                        command <= CMD_SEND_TO_PC_ESCAPED;
                when CMD_RECEIVE_FROM_PC =>
                        command <= CMD_RECEIVE_FROM_PC;
                when others =>
                        null;
                end case;

            when CMD_SEND_TO_PC_LITERAL =>
                out_channel_data ( 7 downto 0 ) <= output_reg;
                if ( out_channel_rdy ( 0 ) = '1' )
                then
                    complete <= '1';
                    command <= CMD_STANDBY;
                    out_channel_wr ( 0 ) <= '1';
                end if;
                
            when CMD_SEND_TO_PC_ESCAPED =>
                if (( output_reg and TX_ESCAPE_MASK ) = 
                        ( TX_ESCAPE_CODE and TX_ESCAPE_MASK ))
                then
                    -- escaping required
                    out_channel_data ( 7 downto 0 ) <= TX_ESCAPE_CODE ;
                    if ( out_channel_rdy ( 0 ) = '1' )
                    then
                        command <= CMD_SEND_TO_PC_ESCAPED_1 ;
                        out_channel_wr ( 0 ) <= '1';
                    end if;
                else
                    -- escaping not required
                    command <= CMD_SEND_TO_PC_LITERAL;
                end if;

            when CMD_SEND_TO_PC_ESCAPED_1 =>
                command <= CMD_SEND_TO_PC_ESCAPED_2;

            when CMD_SEND_TO_PC_ESCAPED_2 =>
                command <= CMD_SEND_TO_PC_LITERAL;

            when CMD_RECEIVE_FROM_PC =>
                if ( in_buffer_present ( 0 ) = '1' )
                then
                    input_reg <= in_buffer_data ( 7 downto 0 );
                    in_buffer_ack ( 0 ) <= '1' ;
                    complete <= '1';
                    command <= CMD_STANDBY;
                end if;

----------------------------------------------------------------------------- 
            end case;
            dwbc_rdy <= data_waiting and buffers_clear 
                    and out_channel_rdy ( 0 );
        end if ;
    end process ;

    process ( in_buffer_data, in_buffer_present, chan_mux_setting,
            in_mux_ack, int_active, out_mux_wr, input_reg,
            out_channel_rdy ) is
    begin
        data_waiting <= '0';
        in_mux_present <= '0';
        in_mux_data <= in_buffer_data ( 15 downto 8 ) ;
        out_mux_rdy <= '1';
        for chan in 1 to ( channels - 1 )
        loop
            in_buffer_ack ( chan ) <= '0';
            out_channel_wr ( chan ) <= '0';
            out_channel_data (( chan * 8 ) + 7 
                        downto ( chan * 8 )) <= input_reg;

            if ( in_buffer_present ( chan ) = '1' )
            then
                data_waiting <= '1';
            end if;
            if (( int_active = '0' ) and ( chan > 1 ))
            then
                -- Discard data from channels 2 upwards
                in_buffer_ack ( chan ) <= in_buffer_present ( chan ) ;
            end if;
            if ( chan = chan_mux_setting )
            then
                in_mux_present <= in_buffer_present ( chan ) ;
                in_mux_data <= in_buffer_data (( chan * 8 ) + 7 
                                downto ( chan * 8 )) ;
                in_buffer_ack ( chan ) <= in_mux_ack;
                out_channel_wr ( chan ) <= out_mux_wr;
                out_mux_rdy <= out_channel_rdy ( chan );
            end if;
        end loop;
    end process;

end architecture vl ;

