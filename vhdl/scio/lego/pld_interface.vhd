--
--      pld_interface.vhd
--
--      Communicates with PLD over serial line
--      
--      Author: Peter Hilber                    peter.hilber@student.tuwien.ac.at
--
--
--
--      2007-03-26      created for Lego PCB
--
--      todo:
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;
use work.lego_pld_pack.all;

entity pld_interface is
    
    port (
        clk                  : in    std_logic;
        reset                : in    std_logic;
        out_pins              : in FORWARDED_PINS;
        in_pins             : out FORWARDED_PINS;

        -- to pld

        pld_strobe           : buffer std_logic;
        pld_clk              : out   std_logic;
        data                 : inout std_logic);

end pld_interface;

architecture rtl of pld_interface is

    signal state: FORWARDED_PINS_INDEX_TYPE;
    signal next_state : FORWARDED_PINS_INDEX_TYPE;

    signal in_pins_only_inputs : FORWARDED_PINS;

begin  -- rtl

    pld_clk <= clk;
    
    pld_strobe <= '1' when reset = '1' or state = FORWARDED_PINS'high else '0';
    
    async: process (state, pld_strobe)
    begin  -- process async
        if pld_strobe = '1' or state = FORWARDED_PINS'high then
            next_state <= FORWARDED_PINS'low;
        else
            next_state <= state+1;
        end if;
    end process async;

    sync: process (clk, reset)
    begin  -- process sync
        if reset = '1' then           -- asynchronous reset (active high)
            -- TODO
        elsif rising_edge(clk) then     -- rising clock edge
            if FORWARDED_PINS_DIRECTIONS(state) = din then
                in_pins_only_inputs(state) <= data;
            end if;
            
            if FORWARDED_PINS_DIRECTIONS(next_state) = dout then
                data <= out_pins(next_state);
            else
                data <= 'Z';
            end if;            

            state <= next_state;
        end if;
    end process sync;

    g_pins: for i in FORWARDED_PINS'range generate
        forward_input_pins: if FORWARDED_PINS_DIRECTIONS(i) = din generate
            in_pins(i) <= '0' when reset = '1' else in_pins_only_inputs(i);
        end generate forward_input_pins;
        
        reset_unused_input_pins: if FORWARDED_PINS_DIRECTIONS(i) = dout generate
            in_pins(i) <= '0';
        end generate reset_unused_input_pins;
    end generate g_pins;
end rtl;
