--
--	mac_coprocessor.vhd
--
--	MAC (multiply/accumulate coprocessor) that implements a basic MAC 
--  hardware method and a version query mechanism.
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

use work.jop_types.all;
use work.sc_pack.all;

entity mac_coprocessor is
    generic (
            id              : std_logic_vector(7 downto 0);
            version         : std_logic_vector(15 downto 0)
        );
    port (
            clk             : in std_logic;
            reset           : in std_logic;

            sc_mem_out		: out sc_out_type;
            sc_mem_in		: in sc_in_type;

            cc_out_data     : out std_logic_vector(31 downto 0);
            cc_out_wr       : out std_logic;
            cc_out_rdy      : in std_logic;

            cc_in_data      : in std_logic_vector(31 downto 0);
            cc_in_wr        : in std_logic;
            cc_in_rdy       : out std_logic
        );

end entity mac_coprocessor;


architecture cp of mac_coprocessor is

    constant zero : std_logic_vector ( 7 downto 0 ) := x"00" ;
    constant max_count : std_logic_vector ( 7 downto 0 ) := x"FF" ;

    signal cc_reg_full          : std_logic;
    signal out_message_counter  : std_logic_vector ( 7 downto 0 );
    signal in_message_left      : std_logic_vector ( 7 downto 0 );
    signal in_message_is_from   : std_logic_vector ( 7 downto 0 );
    signal cp_start, cp_load    : std_logic;
    signal cp_waiting           : std_logic;
    signal cp_payload           : std_logic;
    signal cc_register          : std_logic_vector ( 31 downto 0 );
    signal data_register        : std_logic_vector ( 31 downto 0 );
    signal address_register     : std_logic_vector ( 31 downto 0 );
    signal pointer_1            : std_logic_vector ( 31 downto 0 );
    signal pointer_2            : std_logic_vector ( 31 downto 0 );
    signal counter              : std_logic_vector ( 31 downto 0 );
    signal mult_1               : std_logic_vector ( 31 downto 0 );
    signal mult_2               : std_logic_vector ( 31 downto 0 );
    signal mult_output          : std_logic_vector ( 31 downto 0 );
    signal mult_start           : std_logic;
    signal mult_active          : std_logic;
    signal mult_reset           : std_logic;
    signal partial_1            : std_logic_vector ( 63 downto 0 );
    signal partial_2            : std_logic_vector ( 63 downto 0 );
    signal partial_3            : std_logic_vector ( 63 downto 0 );
    signal partial_4            : std_logic_vector ( 63 downto 0 );
    signal partial_5            : std_logic_vector ( 63 downto 0 );
    signal ready_1              : std_logic;
    signal ready_2              : std_logic;
    signal ready_3              : std_logic;
    signal ready_4              : std_logic;
    signal ready_5              : std_logic;

    type OpcodeType is ( 
            CHANNEL_IS_OPEN,
            PASS_MESSAGE,
            PASS_MESSAGE_1,
            REPORT_VERSION,
            REPORT_VERSION_1,
            CALL_METHOD,
            WAIT_RUN,
            WAIT_INPUT,
            WAIT_OUTPUT,
            WAIT_READ,
            WAIT_READ_1,
            WAIT_WRITE,
            WAIT_WRITE_1,
            METHOD_MAC_P1,
            METHOD_MAC_P2,
            METHOD_MAC_P3,
            METHOD_MAC,
            METHOD_MAC_1,
            METHOD_MAC_2,
            METHOD_MAC_R1,
            METHOD_MAC_R2 );


    signal opcode, when_ready   : OpcodeType;

begin

    process(clk, reset) is
        variable advance : boolean;
    begin
        if reset = '1' 
        then
            when_ready <= CHANNEL_IS_OPEN;
            opcode <= CHANNEL_IS_OPEN;
            cc_out_wr <= '0';
            cc_in_rdy <= '0';
            cc_register <= ( others => '0' ) ;
            cc_out_data <= ( others => '0' ) ;
            in_message_left <= zero ;
            in_message_is_from <= zero ;
            mult_start <= '0' ;
            mult_reset <= '1';

        elsif rising_edge(clk) 
        then
            cc_in_rdy <= '0';
            cc_out_wr <= '0';
            mult_start <= '0' ;
            mult_reset <= '0';

            case opcode is
            when CHANNEL_IS_OPEN =>
                -- Receive a message header word
                if ( cc_in_wr = '1' )
                then
                    cc_register <= cc_in_data;
                    in_message_left <= cc_in_data ( 7 downto 0 ) ;

                    if ( cc_in_data ( 23 downto 16 ) = id ) 
                    then
                        -- message is for this co-processor
                        in_message_is_from <= cc_in_data ( 31 downto 24 ) ;

                        case cc_in_data ( 15 downto 8 ) is -- message type
                        when x"01" => 
                                opcode <= REPORT_VERSION;
                        when x"03" => 
                                when_ready <= CALL_METHOD;
                                opcode <= WAIT_INPUT;
                        when others => 
                                when_ready <= PASS_MESSAGE;
                                opcode <= WAIT_OUTPUT;
                        end case;
                    else
                        -- message header to be relayed
                        when_ready <= PASS_MESSAGE;
                        opcode <= WAIT_OUTPUT;
                    end if;
                else
                    cc_in_rdy <= '1';
                end if;

            when PASS_MESSAGE =>
                -- Get a new message payload word, if any remain.
                if ( in_message_left = zero )
                then
                    opcode <= CHANNEL_IS_OPEN;
                else
                    opcode <= WAIT_INPUT;
                    when_ready <= PASS_MESSAGE_1;
                    in_message_left <= in_message_left - 1;
                end if;

            when PASS_MESSAGE_1 =>
                -- Relay this word
                opcode <= WAIT_OUTPUT;
                when_ready <= PASS_MESSAGE;

            when REPORT_VERSION =>
                -- Write version header
                cc_register <= id & in_message_is_from & x"0201";
                opcode <= WAIT_OUTPUT;
                when_ready <= REPORT_VERSION_1;
                
            when REPORT_VERSION_1 =>
                -- Write version payload
                cc_register <= x"0000" & version;
                opcode <= WAIT_OUTPUT;
                when_ready <= CHANNEL_IS_OPEN;
            
            when CALL_METHOD =>
                -- Dispatch
                in_message_left <= in_message_left - 1;
                opcode <= WAIT_INPUT;
                case cc_register ( 7 downto 0 ) is
                when x"01" =>
                        when_ready <= METHOD_MAC_P1;
                when others =>
                        when_ready <= PASS_MESSAGE;
                        opcode <= WAIT_OUTPUT;
                end case;

            when WAIT_RUN =>
                opcode <= when_ready;

            when WAIT_INPUT =>
                if ( cc_in_wr = '1' )
                then
                    cc_register <= cc_in_data ;
                    opcode <= when_ready;
                else
                    cc_in_rdy <= '1';
                end if;

            when WAIT_OUTPUT =>
                if ( cc_out_rdy = '1' )
                then
                    cc_out_data <= cc_register;
                    cc_out_wr <= '1';
                    opcode <= when_ready;
                end if ;

            when WAIT_READ => 
                -- Read signal is sent to memory subsystem
                -- (this happens as soon as WAIT_READ is reached)
                opcode <= WAIT_READ_1;

            when WAIT_WRITE => 
                -- Write signal is sent to memory subsystem
                -- (this happens as soon as WAIT_WRITE is reached)
                opcode <= WAIT_WRITE_1;

            when WAIT_READ_1|WAIT_WRITE_1 => 
                if (( sc_mem_in.rdy_cnt(1) = '0' )
                and ( sc_mem_in.rdy_cnt(0) = '0' ))
                then
                    if ( opcode = WAIT_READ_1 )
                    then
                        data_register <= sc_mem_in.rd_data;
                    end if ;
                    opcode <= when_ready;
                end if;

            when METHOD_MAC_P1 =>
                -- parameter 1 is in the register
                opcode <= WAIT_INPUT;
                when_ready <= METHOD_MAC_P2;
                pointer_1 <= cc_register;
                
            when METHOD_MAC_P2 =>
                -- parameter 2 is in the register
                opcode <= WAIT_INPUT;
                when_ready <= METHOD_MAC_P3;
                pointer_2 <= cc_register;

            when METHOD_MAC_P3 =>
                -- parameter 3 is in the register
                opcode <= METHOD_MAC;
                counter <= cc_register;
                mult_reset <= '1';

            when METHOD_MAC =>
                address_register <= pointer_1;
                pointer_1 <= pointer_1 + 1;
                data_register <= mult_output;
                opcode <= WAIT_READ;
                when_ready <= METHOD_MAC_1;

            when METHOD_MAC_1 =>
                mult_1 <= data_register;
                address_register <= pointer_2;
                pointer_2 <= pointer_2 + 1;
                data_register <= mult_output;
                opcode <= WAIT_READ;
                when_ready <= METHOD_MAC_2;
                counter <= counter - 1;

            when METHOD_MAC_2 =>
                mult_2 <= data_register;
                mult_start <= '1' ;
                address_register <= pointer_1;
                pointer_1 <= pointer_1 + 1;
                data_register <= mult_output;
                if ( counter = x"00000000" )
                then
                    opcode <= METHOD_MAC_R1;
                else
                    opcode <= WAIT_READ;
                    when_ready <= METHOD_MAC_1;
                end if;

            when METHOD_MAC_R1 =>
                -- Write return header
                cc_register <= id & in_message_is_from & x"0401";
                opcode <= WAIT_OUTPUT;
                when_ready <= METHOD_MAC_R2;
                
            when METHOD_MAC_R2 =>
                -- Write result
                if ( mult_active = '0' )
                then
                    cc_register <= mult_output;
                    opcode <= WAIT_OUTPUT;
                    when_ready <= CHANNEL_IS_OPEN;
                end if ;
                
            end case;
        end if;
    end process;

    process ( clk ) is
    begin
        if rising_edge(clk) 
        then
            if ( mult_reset = '1' )
            then
                mult_output <= ( others => '0' ) ;
            elsif ( ready_5 = '1' )
            then
                mult_output <= mult_output + partial_5 ( 31 downto 0 ) ;
            end if ;

            partial_5 <= partial_4 ;
            ready_5 <= ready_4 ;

            partial_4 <= partial_3 ;
            ready_4 <= ready_3 ;

            partial_3 <= partial_2 ;
            ready_3 <= ready_2 ;

            partial_2 <= partial_1 ;
            ready_2 <= ready_1 ;

            partial_1 <= mult_1 * mult_2 ;
            ready_1 <= mult_start ;

        end if ;
    end process ;

    mult_active <= mult_start or ready_1 or ready_2
            or ready_3 or ready_4 or ready_5 ;

    sc_mem_out.rd <= '1' when opcode = WAIT_READ else '0' ;
    sc_mem_out.wr <= '1' when opcode = WAIT_WRITE else '0' ;
    sc_mem_out.wr_data <= data_register;
    sc_mem_out.address <= address_register ( SC_ADDR_SIZE - 1 downto 0 );
    sc_mem_out.atomic <= '0' ;

end architecture cp;

