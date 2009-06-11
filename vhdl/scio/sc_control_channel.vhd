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

    cc_out_data : out std_logic_vector(31 downto 0);
    cc_out_wr   : out std_logic;
    cc_out_rdy  : in std_logic;

    cc_in_data  : in std_logic_vector(31 downto 0);
    cc_in_wr    : in std_logic;
    cc_in_rdy   : out std_logic
);
end sc_control_channel;

architecture rtl of sc_control_channel is

    signal incoming_message     : std_logic_vector(31 downto 0);
    signal outgoing_message     : std_logic_vector(31 downto 0);
    signal send_ack, send_flag  : std_logic;
    signal queue_message        : std_logic_vector(31 downto 0);
    signal queue_flag           : std_logic;
    signal wait_states          : Natural range 0 to 7;

    type StateType is ( IDLE, RELAY, SEND, AWAIT_REPLY, AWAIT_REPLY_RELAY );

    signal state                : StateType;

begin

    rdy_cnt <=   "10" when wait_states = 3
            else "01" when wait_states = 2
            else "00" when wait_states = 1
            else "11" ;

    process ( clk , reset ) is
    begin
        if ( reset = '1' ) 
        then
            send_flag <= '0';
            queue_flag <= '0' ;
            wait_states <= 0;

        elsif ( clk = '1' )
        and ( clk'event )
        then
            if ( send_ack = '1' )
            then
                send_flag <= '0';
            end if;

            -- wait_states is used to match the memory timing assumed by the 
            -- WCA tool; it has no other function and can be kept at zero.
            if ( wait_states /= 0 )
            then
                wait_states <= wait_states - 1;
            end if;

            if ( rd = '1' ) 
            then
                wait_states <= 4;  
            elsif ( wr = '1' ) 
            then
                wait_states <= 5;
                if ( send_flag = '1' )
                then
                    -- enqueue for transmission 
                    queue_message <= wr_data;
                    queue_flag <= '1';
                else
                    -- send now
                    outgoing_message <= wr_data;
                    send_flag <= '1';
                end if;
            elsif ((( send_flag = '0' ) 
                or ( send_ack = '1' ))
            and ( queue_flag = '1' ))
            then
                -- load outgoing message register from queue
                -- (or empty)
                queue_flag <= '0';
                send_flag <= '1';
                outgoing_message <= queue_message;
            end if;
        end if;
    end process;

    rd_data <= incoming_message;

    process ( clk , reset ) is
    begin
        if ( reset = '1' ) 
        then
            state <= IDLE;
            cc_in_rdy <= '0';
            cc_out_wr <= '0';
            send_ack <= '0';

        elsif ( clk = '1' )
        and ( clk'event )
        then
            cc_in_rdy <= '0';
            cc_out_wr <= '0';
            send_ack <= '0';

            case state is
            when IDLE =>
                if ( send_flag = '1' )
                then
                    -- A message to be sent
                    cc_out_data <= outgoing_message;
                    send_ack <= '1';
                    state <= SEND;
                elsif ( cc_in_wr = '1' )
                then
                    -- Relay incoming message since we are not
                    -- waiting for a message
                    cc_out_data <= cc_in_data;
                    state <= RELAY;
                else
                    -- Ready for CC data 
                    cc_in_rdy <= '1';
                end if;

            when RELAY =>
                if ( cc_out_rdy = '1' )
                then
                    cc_out_wr <= '1';
                    state <= IDLE;
                end if;

            when SEND =>
                if ( cc_out_rdy = '1' )
                then
                    cc_out_wr <= '1';
                    state <= AWAIT_REPLY;
                end if;

            when AWAIT_REPLY =>
                if ( cc_in_wr = '1' )
                then
                    -- Examine incoming message
                    if ( cc_in_data ( 30 downto 16 ) = 
                              outgoing_message ( 30 downto 16 ) )
                    then
                        -- Correct message
                        incoming_message <= cc_in_data;
                        state <= IDLE;
                    else
                        -- Wrong message (for someone else)
                        cc_out_data <= cc_in_data;
                        state <= AWAIT_REPLY_RELAY;
                    end if;
                else
                    -- Ready for CC data 
                    cc_in_rdy <= '1';
                end if;
                
            when AWAIT_REPLY_RELAY =>
                if ( cc_out_rdy = '1' )
                then
                    cc_out_wr <= '1';
                    state <= AWAIT_REPLY;
                end if;
            end case;
        end if;
    end process;

end rtl;

