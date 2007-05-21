--
--      lego_pack.vhd
--
--      For Lego PCB related stuff
--      
--      Author: Peter Hilber                    peter.hilber@student.tuwien.ac.at
--
--
--      2006-12-07      created for Lego PCB
--
--      todo:
--
--

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

package lego_pack is
    subtype lego_motor_state is unsigned(1 downto 0);

    constant LEGO_MOTOR_STATE_OFF : lego_motor_state := "00";
    constant LEGO_MOTOR_STATE_FORWARD : lego_motor_state := "01";
    constant LEGO_MOTOR_STATE_BACKWARD : lego_motor_state := "10";
    constant LEGO_MOTOR_STATE_BRAKE : lego_motor_state := "11";

end lego_pack;
