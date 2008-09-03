

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

use work.jop_types.all;
use work.sc_pack.all;

entity mac_coprocessor is
    port (
            clk             : in std_logic;
            reset           : in std_logic;

            sc_mem_out		: out sc_out_type;
            sc_mem_in		: in sc_in_type;

            start           : out std_logic;
            busy            : out std_logic;
            running         : out std_logic;
            reading         : out std_logic;
            terminal        : out std_logic;
            grab            : out std_logic;


            cc_out_data     : out std_logic_vector(31 downto 0);
            cc_out_wr       : out std_logic;
            cc_out_rdy      : in std_logic;

            cc_in_data      : in std_logic_vector(31 downto 0);
            cc_in_wr        : in std_logic;
            cc_in_rdy       : out std_logic
        );
end entity mac_coprocessor;

architecture cp of mac_coprocessor is
    signal method_mac1_param_size   : std_logic_vector ( 31 downto 0 );
    signal method_mac1_param_alpha  : std_logic_vector ( 23 downto 0 );
    signal method_mac1_param_beta   : std_logic_vector ( 23 downto 0 );
    signal method_mac1_return       : std_logic_vector ( 31 downto 0 );
    signal method_mac1_start        : std_logic;
    signal method_mac1_running      : std_logic;
    signal size                     : std_logic_vector ( 31 downto 0 );
    signal pointer_1                : std_logic_vector ( 23 downto 0 );
    signal pointer_2                : std_logic_vector ( 23 downto 0 );
    signal address_register         : std_logic_vector ( 23 downto 0 );
    signal mult_busy_0              : std_logic;
    signal mult_busy_1              : std_logic;
    signal mult_busy_2              : std_logic;
    signal mult_busy_3              : std_logic;
    signal mult_busy_4              : std_logic;
    signal mult_busy_5              : std_logic;
    signal mult_stage_1             : std_logic_vector ( 31 downto 0 );
    signal mult_stage_2             : std_logic_vector ( 31 downto 0 );
    signal mult_stage_3             : std_logic_vector ( 31 downto 0 );
    signal mult_stage_4             : std_logic_vector ( 31 downto 0 );
    signal total                    : std_logic_vector ( 31 downto 0 );
    signal mult_in_1                : std_logic_vector ( 31 downto 0 );
    signal mult_in_2                : std_logic_vector ( 31 downto 0 );
    signal clear, read, capture_1   : std_logic;
    signal capture_2, mult_busy     : std_logic;
    signal mem_ready                : std_logic;

    type State_Type is ( STANDBY, MAC_1, MAC_2, MAC_3, MAC_4 ) ;

    signal state                    : State_Type;

begin
    m : entity mac_coprocessor_if 
        port map (
            clk => clk,
            reset => reset,

            method_mac1_param_size => method_mac1_param_size,
            method_mac1_param_alpha => method_mac1_param_alpha,
            method_mac1_param_beta => method_mac1_param_beta,
            method_mac1_return => method_mac1_return,
            method_mac1_start => method_mac1_start,
            method_mac1_running => method_mac1_running,

            cc_out_data => cc_out_data,
            cc_out_wr => cc_out_wr,
            cc_out_rdy => cc_out_rdy,

            cc_in_data => cc_in_data,
            cc_in_wr => cc_in_wr,
            cc_in_rdy => cc_in_rdy);
    
    process ( clk , reset ) is
    begin
        if ( reset = '1' )
        then
            size <= ( others => '0' ) ;
            pointer_1 <= ( others => '0' ) ;
            pointer_2 <= ( others => '0' ) ;
            address_register <= ( others => '0' ) ;
            state <= STANDBY;
            
        elsif ( clk = '1' )
        and ( clk'event )
        then
            clear <= '0';
            read <= '0';
            capture_1 <= '0';
            capture_2 <= '0';

            case state is
            when STANDBY =>
                if ( method_mac1_start = '1' )
                then
                    size <= method_mac1_param_size;
                    pointer_1 <= method_mac1_param_alpha;
                    pointer_2 <= method_mac1_param_beta;
                    state <= MAC_1;
                end if;
                
            when MAC_1 =>
                address_register <= pointer_1;
                pointer_1 <= pointer_1 + 1;
                clear <= '1';
                read <= '1';
                state <= MAC_2;

            when MAC_2 =>
                if ( mem_ready = '1' )
                then
                    address_register <= pointer_2;
                    pointer_2 <= pointer_2 + 1;
                    size <= size - 1;
                    capture_1 <= '1';
                    read <= '1';
                    state <= MAC_3;
                end if;

            when MAC_3 =>
                if ( mem_ready = '1' )
                then
                    address_register <= pointer_1;
                    pointer_1 <= pointer_1 + 1;
                    capture_2 <= '1';
                    if ( conv_integer ( size ) = 0 )
                    then
                        state <= MAC_4;
                    else
                        read <= '1';
                        state <= MAC_2;
                    end if;
                end if;

            when MAC_4 =>
                if ( mult_busy = '0' )
                then
                    state <= STANDBY;
                end if;
            end case ;
        end if;
    end process;

    method_mac1_running <= '1' when state /= STANDBY else '0';
    method_mac1_return <= total;

    process ( clk , reset ) is
    begin
        if ( reset = '1' )
        then
            mult_busy_0 <= '0';
            total <= ( others => '0' );
            mult_in_1 <= ( others => '0' );
            
        elsif ( clk = '1' )
        and ( clk'event )
        then
            mult_busy_0 <= '0';
            mult_in_2 <= ( others => '0' ) ;
            if ( capture_1 = '1' )
            then
                mult_in_1 <= sc_mem_in.rd_data;
            end if;
            if ( capture_2 = '1' )
            then
                mult_in_2 <= sc_mem_in.rd_data;
                mult_busy_0 <= '1';
            end if;
            mult_stage_1 <= mult_in_1 * mult_in_2 ;
            mult_stage_2 <= mult_stage_1;
            mult_stage_3 <= mult_stage_2;
            mult_stage_4 <= mult_stage_3;
            total <= total + mult_stage_4;
            mult_busy_1 <= mult_busy_0;
            mult_busy_2 <= mult_busy_1;
            mult_busy_3 <= mult_busy_2;
            mult_busy_4 <= mult_busy_3;
            mult_busy_5 <= mult_busy_4;
            if ( clear = '1' )
            then
                total <= ( others => '0' ) ;
            end if;
        end if;
    end process;
                
    mult_busy <= capture_2 or mult_busy_0 or mult_busy_1 or
                        mult_busy_2 or mult_busy_3 or
                        mult_busy_4 or mult_busy_5;
    mem_ready <= '1' when (( sc_mem_in.rdy_cnt(1) = '0' )
                and ( sc_mem_in.rdy_cnt(0) = '0' )
                and ( read = '0' )) else '0';

    sc_mem_out.rd <= read;
    sc_mem_out.wr <= '0';
    sc_mem_out.wr_data <= ( others => '0' );
    sc_mem_out.atomic <= '0' ;

    process ( address_register ) is
    begin
        sc_mem_out.address <= ( others => '0' ) ;
        sc_mem_out.address <= address_register ( SC_ADDR_SIZE - 1 downto 0 );
    end process ;

    grab <= capture_1 or capture_2;
    start <= method_mac1_start;
    busy <= mult_busy;
    running <= method_mac1_running;
    terminal <= '1' when ( state = MAC_4 ) else '0' ;
    reading <= '1' when ( state = MAC_2 ) or ( state = MAC_3 ) else '0' ;

end architecture cp ;


