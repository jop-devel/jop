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
-- $Id: debugger.vhd,v 1.1 2008/08/09 12:28:51 jwhitham Exp $


library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

use work.vlabifhw_pack.all;

entity debugger is
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
    signal temp_register            : Byte;
    signal debug_clock_1            : std_logic;

    type State_Type is (
            INIT, DISPATCH, ECHO,
            LAB_SET_CTRL, LAB_SET_CTRL_1,
            LAB_GET_DEBUG_CHAIN, LAB_GET_DEBUG_CHAIN_1,
            LAB_GET_DEBUG_CHAIN_2, LAB_GET_DEBUG_CHAIN_3,
            LAB_GET_DEBUG_CHAIN_4,
            LAB_CLOCK_STEP, LAB_CLOCK_STEP_1, LAB_CLOCK_STEP_2,
            PROC_LOAD_COUNTER, PROC_LOAD_COUNTER_1,
            PROC_SEND_TO_PC, PROC_RECEIVE_FROM_PC );

    signal state, proc_return       : State_Type;

begin

    process ( clk, reset ) is
    begin
        if ( reset = '1' )
        then
            debug_reset <= '1';
            debug_clock_1 <= '0';
            dc_control.dc_shift <= '0' ;
            dc_control.dc_capture <= '0' ;
            dc_control.dc_ready <= '0' ;
            dc_control.dc_clock <= '0' ;
            dc_out <= '0' ;

            out_channel_data <= ( others => '0' ) ;
            out_channel_wr <= '0';
            in_channel_ack <= '0' ;

            mode_set <= false;
            state <= INIT;
            proc_return <= INIT;
        elsif ( clk = '1' )
        and ( clk'event )
        then
            out_channel_wr <= '0';
            in_channel_ack <= '0' ;

            if (( free_run = '1' )
            or (( free_run_to_breakpoint = '1' )
                and ( breakpoint = '0' )))
            then
                debug_clock_1 <= not debug_clock_1;
            end if;

            case state is
            when INIT =>
                -- Wait for a command on the input channel
                state <= PROC_RECEIVE_FROM_PC;
                proc_return <= ECHO;
                -- Reset control registers
                mode_set <= false;
                dc_control.dc_shift <= '0' ;
                dc_control.dc_clock <= '1' ;
                dc_out <= '0' ;

            when ECHO =>
                state <= PROC_SEND_TO_PC;
                proc_return <= DISPATCH;
                dc_control.dc_clock <= '0' ;
                dc_control.dc_capture <= '0' ;
                dc_control.dc_ready <= '0' ;

            when DISPATCH =>
                case temp_register is
                when RX_COMMAND_CLOCK_STEP =>   
                        state <= LAB_CLOCK_STEP;
                when RX_COMMAND_GET_DEBUG_CHAIN => 
                        state <= LAB_GET_DEBUG_CHAIN;
                when RX_COMMAND_SET_DEBUG_CHAIN => 
                        mode_set <= true;
                        state <= LAB_GET_DEBUG_CHAIN;
                when RX_COMMAND_SET_CTRL =>     
                        state <= LAB_SET_CTRL;
                when others => -- including NOP
                        state <= INIT;
                end case ;

----------------------------------------------------------------------------- 
            when LAB_SET_CTRL =>
                -- Set the control lines
                state <= PROC_RECEIVE_FROM_PC;
                proc_return <= LAB_SET_CTRL_1;

            when LAB_SET_CTRL_1 =>
                free_run <= temp_register ( RX_FREE_RUN_BIT );
                debug_clock_1 <= temp_register ( RX_CLOCK_BIT );
                debug_reset <= temp_register ( RX_RESET_BIT );
                free_run_to_breakpoint <= 
                        temp_register ( RX_FREE_RUN_BREAK_BIT );
                dc_control.dc_capture <= temp_register ( RX_CAPTURE_BIT );
                dc_control.dc_ready <= temp_register ( RX_READY_BIT );
                state <= INIT;

----------------------------------------------------------------------------- 
            when LAB_GET_DEBUG_CHAIN =>
                -- Send/receive the debug chain contents
                -- Start by reading the number of bytes
                state <= PROC_LOAD_COUNTER;
                proc_return <= LAB_GET_DEBUG_CHAIN_1;
                
            when LAB_GET_DEBUG_CHAIN_1 =>
                if ( mode_set )
                then
                    -- Receive first byte of contents
                    state <= PROC_RECEIVE_FROM_PC;
                else
                    -- Go ahead
                    state <= LAB_GET_DEBUG_CHAIN_2;
                end if;
                proc_return <= LAB_GET_DEBUG_CHAIN_2;
                counter3 <= 7;
                dc_control.dc_shift <= '1' ;

            when LAB_GET_DEBUG_CHAIN_2 =>
                dc_out <= temp_register ( 7 ) ;
                temp_register ( 7 downto 1 ) <= 
                            temp_register ( 6 downto 0 ) ;
                temp_register ( 0 ) <= dc_in ;
                if ( not mode_set )
                then
                    -- There is no need for this - it is stupid.
                    null;
                    --dc_out <= '1';
                end if ;
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
                            state <= PROC_RECEIVE_FROM_PC;
                        else
                            state <= PROC_SEND_TO_PC;
                        end if;
                        proc_return <= LAB_GET_DEBUG_CHAIN_4;
                        counter3 <= 7;
                        counter16 <= counter16 - 1;
                    end if;
                else
                    state <= LAB_GET_DEBUG_CHAIN_4;
                    counter3 <= counter3 - 1;
                end if;

            when LAB_GET_DEBUG_CHAIN_4 =>
                dc_control.dc_clock <= '0' ;
                state <= LAB_GET_DEBUG_CHAIN_2;

----------------------------------------------------------------------------- 
            when LAB_CLOCK_STEP =>
                -- Step the debug clock by up to 65535 cycles
                state <= PROC_LOAD_COUNTER;
                proc_return <= LAB_CLOCK_STEP_1;

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
            when PROC_LOAD_COUNTER =>
                if ( in_channel_present = '1' )
                then
                    temp_register <= in_channel_reg ( 7 downto 0 );
                    in_channel_ack <= '1' ;
                    state <= PROC_LOAD_COUNTER_1 ;
                end if;

            when PROC_LOAD_COUNTER_1 =>
                if ( in_channel_present = '1' )
                then
                    counter16 <= conv_integer ( temp_register &
                            in_channel_reg ( 7 downto 0 ) ) ;
                    in_channel_ack <= '1' ;
                    state <= proc_return;
                end if;

            when PROC_SEND_TO_PC =>
                out_channel_data ( 7 downto 0 ) <= temp_register;
                if ( out_channel_rdy = '1' )
                then
                    state <= proc_return;
                    out_channel_wr <= '1';
                end if;
                
            when PROC_RECEIVE_FROM_PC =>
                if ( in_channel_present = '1' )
                then
                    temp_register <= in_channel_reg ( 7 downto 0 );
                    in_channel_ack <= '1' ;
                    state <= proc_return;
                end if;
----------------------------------------------------------------------------- 
            end case;
        end if;
    end process;

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

