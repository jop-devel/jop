--
-- Debugging device
-- debugger.vhd
--
-- Provides:
--      debug chain interface
--      debug clock and reset
--      parallel output
--
-- Author: Jack Whitham
-- $Id: debugger.vhd,v 1.3 2008/11/03 11:41:29 jwhitham Exp $
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

entity debugger is
generic (
    less_features               : Boolean := false );
port (
	clk		                    : in std_logic;
	reset	                    : in std_logic;

    -- Data buses
    out_channel_data            : out std_logic_vector ( 7 downto 0 );
    out_channel_wr              : out std_logic;
    out_channel_rdy             : in std_logic;

    in_channel_data             : in std_logic_vector ( 7 downto 0 );
    in_channel_wr               : in std_logic;
    in_channel_rdy              : out std_logic;

    -- Controls for device under test
    debug_clock                 : out std_logic;
    debug_reset                 : out std_logic;
    breakpoint                  : in std_logic;

    -- Debug chain input/output 
    dc_control                  : out DC_Control_Wires;
    dc_out                      : out std_logic;
    dc_in                       : in std_logic
);
end debugger ;

architecture rtl of debugger is

    signal counter16                : Natural range 0 to 65535;
    signal counter3                 : Natural range 0 to 7;
    signal mode_set                 : Boolean;
    signal in_channel_reg_full      : std_logic;
    signal in_channel_reg           : Byte;
    signal in_channel_present       : std_logic;
    signal in_channel_ack           : std_logic;
    signal free_run                 : std_logic;
    signal free_run_to_breakpoint   : std_logic;
    signal temp_reg                 : Byte;
    signal debug_clock_1            : std_logic;

    type State_Type is (
            INIT, DISPATCH, ECHO,
            LAB_SET_CTRL, 
            LAB_GET_DEBUG_CHAIN, LAB_GET_DEBUG_CHAIN_1A,
            LAB_GET_DEBUG_CHAIN_2, LAB_GET_DEBUG_CHAIN_3,
            LAB_CLOCK_STEP, LAB_CLOCK_STEP_1, LAB_CLOCK_STEP_2);

    type Command_Type is ( CMD_STANDBY,
            CMD_LOAD_COUNTER, CMD_LOAD_COUNTER_1,
            CMD_SEND_TO_PC, CMD_RECEIVE_FROM_PC );

    signal state                    : State_Type;
    signal command                  : Command_Type;
    signal complete                 : std_logic;

begin

    process ( clk, reset ) is
        variable next_command       : Command_Type;
    begin
        if ( reset = '1' )
        then
            debug_reset <= '1';
            debug_clock_1 <= '0';
            dc_control.dc_shift <= '0' ;
            dc_control.dc_capture <= '0' ;
            dc_control.dc_ready <= '0' ;
            dc_control.dc_clock <= '0' ;

            out_channel_wr <= '0';
            in_channel_ack <= '0' ;

            mode_set <= false;
            state <= INIT;
            command <= CMD_STANDBY;

        elsif ( clk = '1' )
        and ( clk'event )
        then
            out_channel_wr <= '0';
            in_channel_ack <= '0' ;

            if ( not less_features )
            then
                -- less_features disables clock control
                if (( free_run = '1' )
                or (( free_run_to_breakpoint = '1' )
                    and ( breakpoint = '0' )))
                then
                    debug_clock_1 <= not debug_clock_1;
                end if;
            end if;

            complete <= '0';
            next_command := CMD_STANDBY;

            case state is
            when INIT =>
                -- Wait for a command on the input channel
                next_command := CMD_RECEIVE_FROM_PC;
                state <= ECHO;
                -- Reset control registers
                mode_set <= false;
                dc_control.dc_shift <= '0' ;
                dc_control.dc_clock <= '1' ;

            when ECHO =>
                if ( complete = '1' )
                then
                    next_command := CMD_SEND_TO_PC;
                    state <= DISPATCH;
                    dc_control.dc_clock <= '0' ;
                    dc_control.dc_capture <= '0' ;
                    dc_control.dc_ready <= '0' ;
                end if ;

            when DISPATCH =>
                if ( complete = '1' )
                then
                    case temp_reg is
                    when RX_COMMAND_CLOCK_STEP =>   
                            if ( less_features )
                            then
                                -- Clock step feature is disabled
                                state <= INIT;
                            else
                                state <= LAB_CLOCK_STEP;
                                next_command := CMD_LOAD_COUNTER;
                            end if;
                    when RX_COMMAND_GET_DEBUG_CHAIN => 
                            next_command := CMD_LOAD_COUNTER;
                            state <= LAB_GET_DEBUG_CHAIN;
                    when RX_COMMAND_SET_DEBUG_CHAIN => 
                            mode_set <= true;
                            next_command := CMD_LOAD_COUNTER;
                            state <= LAB_GET_DEBUG_CHAIN;
                    when RX_COMMAND_SET_CTRL =>     
                            next_command := CMD_RECEIVE_FROM_PC;
                            state <= LAB_SET_CTRL;
                    when others => -- including NOP
                            state <= INIT;
                    end case ;
                end if ;

----------------------------------------------------------------------------- 
            when LAB_SET_CTRL =>
                -- Set the control lines
                if ( complete = '1' )
                then
                    if ( not less_features )
                    then
                        -- less_features disables clock control
                        free_run <= temp_reg ( RX_FREE_RUN_BIT );
                        debug_clock_1 <= temp_reg ( RX_CLOCK_BIT );
                        debug_reset <= temp_reg ( RX_RESET_BIT );
                        free_run_to_breakpoint <= 
                                temp_reg ( RX_FREE_RUN_BREAK_BIT );
                    end if;
                    dc_control.dc_capture <= temp_reg ( RX_CAPTURE_BIT );
                    dc_control.dc_ready <= temp_reg ( RX_READY_BIT );
                    state <= INIT;
                end if;

----------------------------------------------------------------------------- 
            when LAB_GET_DEBUG_CHAIN =>
                -- Send/receive the debug chain contents
                -- Start by reading the number of bytes
                if ( complete = '1' )
                then
                    if ( mode_set )
                    then
                        -- Receive first byte of contents
                        next_command := CMD_RECEIVE_FROM_PC;
                        state <= LAB_GET_DEBUG_CHAIN_1A;
                    else
                        -- Go ahead
                        state <= LAB_GET_DEBUG_CHAIN_2;
                    end if;
                    counter3 <= 7;
                    dc_control.dc_shift <= '1' ;
                end if;

            when LAB_GET_DEBUG_CHAIN_1A =>
                if ( complete = '1' )
                then
                    state <= LAB_GET_DEBUG_CHAIN_2;
                end if;

            when LAB_GET_DEBUG_CHAIN_2 =>
                dc_control.dc_clock <= '0' ;
                dc_out <= temp_reg ( 7 ) ;
                temp_reg ( 7 downto 1 ) <= temp_reg ( 6 downto 0 ) ;
                temp_reg ( 0 ) <= dc_in ;
                state <= LAB_GET_DEBUG_CHAIN_3;

            when LAB_GET_DEBUG_CHAIN_3 =>
                dc_control.dc_clock <= '1' ;
                if ( counter3 = 0 )
                then
                    if ( counter16 = 0 )
                    then
                        -- Finished
                        state <= INIT;
                    else
                        if ( mode_set )
                        then
                            next_command := CMD_RECEIVE_FROM_PC;
                        else
                            next_command := CMD_SEND_TO_PC;
                        end if;
                        state <= LAB_GET_DEBUG_CHAIN_1A;
                        counter3 <= 7;
                        counter16 <= counter16 - 1;
                    end if;
                else
                    state <= LAB_GET_DEBUG_CHAIN_2;
                    counter3 <= counter3 - 1;
                end if;

----------------------------------------------------------------------------- 

            when LAB_CLOCK_STEP =>
                -- Step the debug clock by up to 65535 cycles
                if ( complete = '1' )
                then
                    state <= LAB_CLOCK_STEP_1 ;
                end if;

            when LAB_CLOCK_STEP_1 =>
                if ( counter16 = 0 )
                then
                    state <= INIT;
                else
                    debug_clock_1 <= '1';
                    state <= LAB_CLOCK_STEP_2 ;
                end if;

            when LAB_CLOCK_STEP_2 =>
                debug_clock_1 <= '0';
                counter16 <= counter16 - 1;
                state <= LAB_CLOCK_STEP_1 ;

----------------------------------------------------------------------------- 
            end case;

            case command is
----------------------------------------------------------------------------- 
            when CMD_STANDBY =>
                case next_command is
                when CMD_LOAD_COUNTER =>
                        if ( less_features )
                        then
                            -- Causes 8 bit load
                            command <= CMD_LOAD_COUNTER_1 ;
                        else
                            command <= CMD_LOAD_COUNTER ;
                        end if;
                when CMD_SEND_TO_PC =>
                        command <= CMD_SEND_TO_PC ;
                when CMD_RECEIVE_FROM_PC =>
                        command <= CMD_RECEIVE_FROM_PC ;
                when others =>
                        null;
                end case;

            when CMD_LOAD_COUNTER =>
                if ( in_channel_present = '1' )
                then
                    temp_reg <= in_channel_reg ( 7 downto 0 );
                    in_channel_ack <= '1' ;
                    command <= CMD_LOAD_COUNTER_1 ;
                end if;

            when CMD_LOAD_COUNTER_1 =>
                if ( in_channel_present = '1' )
                then
                    if ( less_features )
                    then
                        -- Causes 8 bit load
                        counter16 <= conv_integer ( 
                            in_channel_reg ( 7 downto 0 ) ) ;
                    else
                        counter16 <= conv_integer ( temp_reg &
                            in_channel_reg ( 7 downto 0 ) ) ;
                    end if;
                    in_channel_ack <= '1' ;
                    command <= CMD_STANDBY;
                    complete <= '1';
                end if;

            when CMD_SEND_TO_PC =>
                if ( out_channel_rdy = '1' )
                then
                    command <= CMD_STANDBY;
                    complete <= '1';
                    out_channel_wr <= '1';
                end if;
                
            when CMD_RECEIVE_FROM_PC =>
                if ( in_channel_present = '1' )
                then
                    temp_reg <= in_channel_reg ( 7 downto 0 );
                    in_channel_ack <= '1' ;
                    command <= CMD_STANDBY;
                    complete <= '1';
                end if;
----------------------------------------------------------------------------- 
            end case;
        end if;
    end process;

    out_channel_data ( 7 downto 0 ) <= temp_reg;

    process ( clk, reset ) is
    begin
        if ( reset = '1' )
        then
            in_channel_reg_full <= '0';
            in_channel_reg <= ( others => '0' );
        elsif ( clk = '1' )
        and ( clk'event )
        then  
            if (( in_channel_reg_full = '0' )
            and ( in_channel_wr = '1' ))
            then
                in_channel_reg <= in_channel_data;
                in_channel_reg_full <= '1';
            end if;
            if ( in_channel_ack = '1' )
            then
                in_channel_reg_full <= '0';
            end if;
        end if;
    end process;

    in_channel_present <= in_channel_reg_full and not in_channel_ack ;
    in_channel_rdy <= ( not in_channel_present ) and not in_channel_wr ;
    debug_clock <= debug_clock_1 ;

end architecture rtl;

