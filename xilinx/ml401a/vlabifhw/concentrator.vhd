--
-- Virtual Lab Hardware Interface
-- concentrator.vhd
--
-- The internal logic for the hardware interface.
-- All the buffering and external comms are provided by vlabhwif.vhd
-- There have to be at least two channels (see entity definition).
--
-- Author: Jack Whitham
-- $Id: concentrator.vhd,v 1.1 2008/08/09 12:28:51 jwhitham Exp $
-- 


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
            LAB_READY, LAB_WAIT, LAB_DISPATCH, LAB_INIT,
            LAB_INFO, LAB_INFO_1, 
            LAB_GET_CHANNELS, LAB_GET_CHANNELS_1, 
            LAB_GET_CHANNELS_2, LAB_GET_CHANNELS_3,
            LAB_SEND_CHANNEL, 
            LAB_SEND_CHANNEL_1, LAB_SEND_CHANNEL_2, LAB_SEND_CHANNEL_3,
            LAB_SEND_CHANNEL_4, LAB_SEND_CHANNEL_5, LAB_SEND_CHANNEL_6,
            PROC_SEND_TO_PC_LITERAL, 
            PROC_SEND_TO_PC_ESCAPED, PROC_SEND_TO_PC_ESCAPED_1,
            PROC_SEND_TO_PC_ESCAPED_2,
            PROC_RECEIVE_FROM_PC);

    signal activation_state         : Natural range 0 to 7 ;

    signal state                    : State_Type ;
    signal proc_return              : State_Type ;

    signal temp_register            : Byte ;


    signal counter                  : Natural range 0 to 15 := 7;

    signal in_mux_data              : Byte;
    signal in_mux_ack               : std_logic;
    signal in_mux_present           : std_logic;
    signal out_mux_wr               : std_logic;
    signal out_mux_rdy              : std_logic;
    signal int_active               : std_logic;
    signal data_waiting             : std_logic;
    signal chan_mux_setting         : Natural range 0 to 15;
    signal channel_number           : Nibble;

begin
   
    assert channels > 1 ;
    assert channels <= 16 ;

    active <= int_active;
    channel_number <= conv_std_logic_vector ( chan_mux_setting , 4 ) ;

    process ( clk , reset ) is
    begin
        if ( reset = '1' )
        then
            state <= INIT ;
            proc_return <= INIT ;
            int_active <= '0';
            counter <= 7;
            in_mux_ack <= '0';
            out_mux_wr <= '0';
            chan_mux_setting <= 1;
            in_buffer_ack ( 0 ) <= '0';
            out_channel_wr ( 0 ) <= '0';

        elsif (( clk = '1' )
        and ( clk'event ))
        then
            in_buffer_ack ( 0 ) <= '0';
            out_channel_wr ( 0 ) <= '0';
            in_mux_ack <= '0';
            out_mux_wr <= '0';

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
                    in_buffer_ack ( 0 ) <= '1';
                    temp_register <= in_buffer_data ( 7 downto 0 ) ;
                    state <= CHECK_ACTIVATION ;
                elsif (( in_mux_present = '1' )
                and ( out_channel_rdy ( 0 ) = '1' ))
                then
                    -- Data from virtual UART: relay to PC UART now
                    in_mux_ack <= '1';
                    out_channel_data ( 7 downto 0 ) <= in_mux_data ;
                    out_channel_wr ( 0 ) <= '1';
                    state <= INIT_1;
                end if ;
            when INIT_1 =>
                state <= INIT;

            when CHECK_ACTIVATION =>
                if (( counter /= 0 )
                and ((( activation_state = 0 ) and ( temp_register = x"f6" ))
                or (( activation_state = 1 ) and ( temp_register = x"6c" ))
                or (( activation_state = 2 ) and ( temp_register = x"61" ))
                or (( activation_state = 3 ) and ( temp_register = x"62" ))
                or (( activation_state = 4 ) and ( temp_register = x"70" ))))
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
                    if ( counter /= 0 )
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
                temp_register <= TX_PACKET_READY;
                state <= PROC_SEND_TO_PC_LITERAL;
                proc_return <= LAB_WAIT;
            
            when LAB_WAIT =>
                -- Wait for command
                if ( in_buffer_present ( 0 ) = '1' )
                then
                    -- Command from PC 
                    state <= PROC_RECEIVE_FROM_PC;
                    proc_return <= LAB_DISPATCH;
                elsif (( data_waiting = '1' )
                and ( buffers_clear = '1' )
                and ( out_channel_rdy ( 0 ) = '1' ))
                then
                    -- Data in one of the other channels
                    state <= LAB_GET_CHANNELS;
                end if;

            when LAB_DISPATCH =>
                case temp_register is
                when RX_COMMAND_INFO =>         state <= LAB_INFO ;
                when RX_COMMAND_SEND_CHANNEL => state <= LAB_SEND_CHANNEL ;
                when others =>                  state <= LAB_WAIT;
                end case ;

----------------------------------------------------------------------------- 
            when LAB_INFO =>
                -- Return information about the system.
                temp_register <= TX_PACKET_INFO;
                state <= PROC_SEND_TO_PC_LITERAL;
                proc_return <= LAB_INFO_1;

            when LAB_INFO_1 =>
                temp_register ( 7 downto 4 ) <= conv_std_logic_vector (
                            channels , 4 ) ;
                temp_register ( 3 downto 0 ) <= VERSION_CODE;
                state <= PROC_SEND_TO_PC_ESCAPED;
                proc_return <= LAB_READY;

----------------------------------------------------------------------------- 
            when LAB_GET_CHANNELS =>
                -- Receive data waiting in incoming channels
                -- (except channel 0, which is being used for PC comms)
                if ( in_mux_present = '0' )
                then
                    -- no more data in this channel
                    state <= LAB_GET_CHANNELS_1;
                else
                    -- data is present.. send a start of data packet
                    temp_register <= TX_PACKET_CHANNEL_DATA;
                    state <= PROC_SEND_TO_PC_LITERAL;
                    proc_return <= LAB_GET_CHANNELS_2;
                end if;

            when LAB_GET_CHANNELS_1 =>
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

            when LAB_GET_CHANNELS_2 =>
                -- now say *which* channel has the new data
                temp_register <= ( others => '0' );
                temp_register ( 7 downto 4 ) <= channel_number;
                
                counter <= 15 ;
                state <= PROC_SEND_TO_PC_ESCAPED;
                proc_return <= LAB_GET_CHANNELS_3;
            
            when LAB_GET_CHANNELS_3 =>
                temp_register <= TX_PACKET_END_TX ;
                state <= PROC_SEND_TO_PC_LITERAL;
                proc_return <= LAB_GET_CHANNELS_1; 

                if (( out_channel_rdy ( 0 ) = '0' ) -- output isn't ready
                or ( counter = 0 )                  -- sent enough bytes
                or ( in_mux_present = '0' ))        -- no more data
                then
                    null;
                else
                    -- relay byte and look for another
                    counter <= counter - 1;
                    temp_register <= in_mux_data;
                    in_mux_ack <= '1';
                    state <= PROC_SEND_TO_PC_ESCAPED;
                    proc_return <= LAB_GET_CHANNELS_3;
                end if;

----------------------------------------------------------------------------- 
            when LAB_SEND_CHANNEL =>
                -- Send a byte through a channel.
                -- Which channel? How many bytes?
                -- (I assume the host knows how many bytes it wants
                -- to send, saving some tedious messing about with
                -- escape codes.)
                state <= PROC_RECEIVE_FROM_PC;
                proc_return <= LAB_SEND_CHANNEL_1;

            when LAB_SEND_CHANNEL_1 =>
                chan_mux_setting <= conv_integer ( 
                            temp_register ( 3 downto 0 ) ) ;
                counter <= conv_integer ( 
                            temp_register ( 7 downto 4 ) ) ;
                state <= LAB_SEND_CHANNEL_2;

            when LAB_SEND_CHANNEL_2 =>
                if ( counter = 0 )
                then
                    -- End of data: exit loop
                    state <= LAB_READY;
                else
                    state <= PROC_RECEIVE_FROM_PC;
                    proc_return <= LAB_SEND_CHANNEL_3;
                    counter <= counter - 1;
                end if;
                    
            when LAB_SEND_CHANNEL_3 =>
                if ( out_mux_rdy = '1' )
                then
                    out_mux_wr <= '1';
                    state <= LAB_SEND_CHANNEL_2;
                else
                    -- Overflow: exit loop
                    state <= LAB_SEND_CHANNEL_4;
                end if;

            when LAB_SEND_CHANNEL_4 => 
                temp_register <= TX_PACKET_OVERFLOW;
                state <= PROC_SEND_TO_PC_LITERAL;
                proc_return <= LAB_SEND_CHANNEL_5;

            when LAB_SEND_CHANNEL_5 => 
                temp_register ( 7 downto 4 ) <= channel_number ;
                temp_register ( 3 downto 0 ) <= conv_std_logic_vector (
                            counter , 4 ) ;

                state <= PROC_SEND_TO_PC_ESCAPED;
                proc_return <= LAB_SEND_CHANNEL_6;

            when LAB_SEND_CHANNEL_6 => 
                -- Absorb bytes that overflowed
                if ( counter = 0 )
                then
                    -- End of overflow bytes: exit loop
                    state <= LAB_READY;
                else
                    state <= PROC_RECEIVE_FROM_PC;
                    proc_return <= LAB_SEND_CHANNEL_6;
                    counter <= counter - 1;
                end if;

----------------------------------------------------------------------------- 
            when PROC_SEND_TO_PC_LITERAL =>
                out_channel_data ( 7 downto 0 ) <= temp_register;
                if ( out_channel_rdy ( 0 ) = '1' )
                then
                    state <= proc_return;
                    out_channel_wr ( 0 ) <= '1';
                end if;
                
            when PROC_SEND_TO_PC_ESCAPED =>
                if (( temp_register and TX_ESCAPE_MASK ) = 
                        ( TX_ESCAPE_CODE and TX_ESCAPE_MASK ))
                then
                    -- escaping required
                    out_channel_data ( 7 downto 0 ) <= TX_ESCAPE_CODE ;
                    if ( out_channel_rdy ( 0 ) = '1' )
                    then
                        state <= PROC_SEND_TO_PC_ESCAPED_1 ;
                        out_channel_wr ( 0 ) <= '1';
                    end if;
                else
                    -- escaping not required
                    state <= PROC_SEND_TO_PC_LITERAL;
                end if;

            when PROC_SEND_TO_PC_ESCAPED_1 =>
                state <= PROC_SEND_TO_PC_ESCAPED_2;

            when PROC_SEND_TO_PC_ESCAPED_2 =>
                state <= PROC_SEND_TO_PC_LITERAL;

            when PROC_RECEIVE_FROM_PC =>
                if ( in_buffer_present ( 0 ) = '1' )
                then
                    temp_register <= in_buffer_data ( 7 downto 0 );
                    in_buffer_ack ( 0 ) <= '1' ;
                    state <= proc_return;
                end if;

----------------------------------------------------------------------------- 
            end case ;
        end if ;
    end process ;

    process ( in_buffer_data, in_buffer_present, chan_mux_setting,
            in_mux_ack, int_active, out_mux_wr, temp_register,
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
                        downto ( chan * 8 )) <= temp_register;

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

