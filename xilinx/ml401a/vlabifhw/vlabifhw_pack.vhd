--
-- Vlab Hardware Interface Package
-- vlabifhw_pack.vhd
--
--
-- Author: Jack Whitham
-- $Id: vlabifhw_pack.vhd,v 1.1 2008/08/09 12:28:51 jwhitham Exp $

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

package vlabifhw_pack is

    subtype Byte is std_logic_vector ( 7 downto 0 ) ;
    subtype Nibble is std_logic_vector ( 3 downto 0 ) ;

    constant VERSION_CODE           : Nibble := x"1" ;

    -- Concentrator
    constant TX_PACKET_INFO         : Byte := x"80";
    constant TX_PACKET_CHANNEL_DATA : Byte := x"81";
    constant TX_PACKET_READY        : Byte := x"82";
    constant TX_PACKET_OVERFLOW     : Byte := x"83";
    constant TX_PACKET_END_TX       : Byte := x"84";
    constant TX_ESCAPE_CODE         : Byte := x"87";
    constant TX_ESCAPE_MASK         : Byte := x"f8";

    constant RX_COMMAND_INFO        : Byte := x"70";
    constant RX_COMMAND_SEND_CHANNEL: Byte := x"71";

    -- Debugger
    constant RX_FREE_RUN_BIT        : Natural := 0;
    constant RX_CLOCK_BIT           : Natural := 1;
    constant RX_RESET_BIT           : Natural := 2;
    constant RX_FREE_RUN_BREAK_BIT  : Natural := 3;
    constant RX_CAPTURE_BIT         : Natural := 4;
    constant RX_READY_BIT           : Natural := 5;

    constant RX_COMMAND_CLOCK_STEP  : Byte := x"a0";
    constant RX_COMMAND_GET_DEBUG_CHAIN: Byte := x"a1";
    constant RX_COMMAND_SET_DEBUG_CHAIN: Byte := x"a2";
    constant RX_COMMAND_SET_CTRL    : Byte := x"a3";
    constant RX_COMMAND_NOP         : Byte := x"a4";

	type DC_Control_Wires is record
        dc_shift        : std_logic;
        dc_capture      : std_logic;
        dc_ready        : std_logic;
        dc_clock        : std_logic;
	end record;

	
end vlabifhw_pack;
