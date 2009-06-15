

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

use work.jop_types.all;
use work.sc_pack.all;

entity bitcount_maxsearch is
    port (
            clk             : in std_logic;
            reset           : in std_logic;

            sc_mem_out		  : out sc_out_type;
            sc_mem_in		    : in sc_in_type;

            cc_out_data     : out std_logic_vector(31 downto 0);
            cc_out_wr       : out std_logic;
            cc_out_rdy      : in std_logic;

            cc_in_data      : in std_logic_vector(31 downto 0);
            cc_in_wr        : in std_logic;
            cc_in_rdy       : out std_logic
        );
end entity bitcount_maxsearch;

architecture cp of bitcount_maxsearch is
    signal method_bitcount_param_size   : std_logic_vector ( 31 downto 0 );
    signal method_bitcount_param_data   : std_logic_vector ( 23 downto 0 );
    signal method_bitcount_return       : std_logic_vector ( 31 downto 0 );
    signal method_bitcount_start        : std_logic;
    signal method_bitcount_running      : std_logic;
    signal method_maxsearch_param_size  : std_logic_vector ( 31 downto 0 );
    signal method_maxsearch_param_data  : std_logic_vector ( 23 downto 0 );
    signal method_maxsearch_return      : std_logic_vector ( 31 downto 0 );
    signal method_maxsearch_start       : std_logic;
    signal method_maxsearch_running     : std_logic;
    signal size                     : std_logic_vector ( 31 downto 0 );
    signal pointer                  : std_logic_vector ( 23 downto 0 );
    signal address                  : std_logic_vector ( 23 downto 0 );
    signal total                    : std_logic_vector ( 31 downto 0 );
    signal data                     : std_logic_vector ( 31 downto 0 );
    signal clear, read, capture     : std_logic;
    signal ready_count              : integer range 0 to 3;
    signal pipeline_ready           : std_logic;
    signal pipeline_ready_1         : std_logic;
    signal pipeline_ready_2         : std_logic;
    signal data_ready               : std_logic;

    type State_Type is ( STANDBY, BCMS_1, BCMS_2, BCMS_3 ) ;
    type Mode_Type is ( BITCOUNT, MAXSEARCH ) ;

    signal state                    : State_Type;
    signal mode                     : Mode_Type;

    subtype BC_Value is Natural range 0 to 31 ;
    signal bc_data                  : BC_Value ;

    function bit_count ( slice : std_logic_vector ) return BC_Value is
        variable x : Natural ;
    begin
        if ( slice'left = slice'right )
        then
            if ( slice ( slice'left ) = '1' )
            then
                return 1;
            else
                return 0;
            end if;
        else
            x := ( slice'left + slice'right ) / 2 ;
            return bit_count ( slice ( slice'left downto x + 1 ))
                + bit_count ( slice ( x downto slice'right )) ;
        end if;
    end function bit_count ;
            
        
begin
    m : entity bitcount_maxsearch_if 
        port map (
            clk => clk,
            reset => reset,

            method_bitcount_param_size => method_bitcount_param_size,
            method_bitcount_param_data => method_bitcount_param_data,
            method_bitcount_return => method_bitcount_return,
            method_bitcount_start => method_bitcount_start,
            method_bitcount_running => method_bitcount_running,

            method_maxsearch_param_size => method_maxsearch_param_size,
            method_maxsearch_param_data => method_maxsearch_param_data,
            method_maxsearch_return => method_maxsearch_return,
            method_maxsearch_start => method_maxsearch_start,
            method_maxsearch_running => method_maxsearch_running,

            cc_out_data => cc_out_data,
            cc_out_wr => cc_out_wr,
            cc_out_rdy => cc_out_rdy,

            cc_in_data => cc_in_data,
            cc_in_wr => cc_in_wr,
            cc_in_rdy => cc_in_rdy);
    
    -- Registered part of bitcount state machine
    process ( clk , reset ) is
    begin
        if ( reset = '1' )
        then
            size <= ( others => '0' ) ;
            pointer <= ( others => '0' ) ;
            pipeline_ready_2 <= '0';
            pipeline_ready_1 <= '0';
            state <= STANDBY;
            mode <= BITCOUNT ;
            
        elsif ( clk = '1' )
        and ( clk'event )
        then
            pipeline_ready_2 <= pipeline_ready_1;
            pipeline_ready_1 <= pipeline_ready;

            case state is
            when STANDBY =>
                if ( method_bitcount_start = '1' )
                then
                    size <= method_bitcount_param_size;
                    pointer <= method_bitcount_param_data;
                    state <= BCMS_1;
                    mode <= BITCOUNT;
                elsif ( method_maxsearch_start = '1' )
                then
                    size <= method_maxsearch_param_size;
                    pointer <= method_maxsearch_param_data;
                    state <= BCMS_1;
                    mode <= MAXSEARCH;
                end if;
                
            when BCMS_1 =>
                -- Start transaction 0
                pointer <= pointer + 1;
                state <= BCMS_2;
                size <= size - 1;

            when BCMS_2 =>
                if ( pipeline_ready = '1' )
                then
                    -- Start transaction N+1
                    pointer <= pointer + 1;
                end if;
                if ( data_ready = '1' )
                then
                    -- End transaction N
                    if ( conv_integer ( size ) = 0 )
                    then
                        state <= BCMS_3;
                    else
                        state <= BCMS_2;
                        size <= size - 1;
                    end if;
                end if;
            when BCMS_3 =>
                state <= STANDBY;
            end case ;
        end if;
    end process;

    -- Combinatorial part of state machine
    process ( state, pointer, pipeline_ready, data_ready ) is
    begin
        clear <= '0';
        read <= '0';
        capture <= '0';
        address <= pointer;

        case state is
        when STANDBY|BCMS_3 =>
            null;

        when BCMS_1 =>
            -- Begin transaction 0
            clear <= '1';
            read <= '1';

        when BCMS_2 =>
            -- End transaction N-1, start N
            capture <= data_ready;
            read <= pipeline_ready;
        end case ;
    end process;

    -- Ready signal generator
    process ( state, ready_count, pipeline_ready_2 ) is
    begin
        data_ready <= pipeline_ready_2;
        pipeline_ready <= '0';
        if ( state /= BCMS_2 ) 
        then
            pipeline_ready <= '0' ;
            data_ready <= '0';
        elsif ( ready_count = 1 )
        then
            pipeline_ready <= '1' ;
        elsif ( ready_count = 0 )
        then
            pipeline_ready <= '1' ;
            data_ready <= '1';
        end if;
    end process;

    -- Function unit
    process ( clk , reset, clear ) is
    begin
        if (( reset = '1' )
        or ( clear = '1' ))
        then
            total <= ( others => '0' );
            data <= ( others => '0' ) ;
            bc_data <= 0 ;
            
        elsif ( clk = '1' )
        and ( clk'event )
        then
            if ( capture = '1' )
            then
                data <= sc_mem_in.rd_data;
            else
                data <= ( others => '0' ) ;
            end if;

            bc_data <= bit_count ( data ) ;

            case mode is
            when BITCOUNT =>
                total <= total + bc_data ;
            when MAXSEARCH =>
                if ( data ( 31 ) = '1' )
                then
                    -- negative - ignore
                elsif ( data > total )
                then
                    total <= data ;
                end if ;
            end case ;
        end if;
    end process;

    method_bitcount_running <= '1' 
            when (( state /= STANDBY ) and ( mode = BITCOUNT )) else '0';
    method_maxsearch_running <= '1' 
            when (( state /= STANDBY ) and ( mode = MAXSEARCH )) else '0';
    method_bitcount_return <= total;
    method_maxsearch_return <= total;

    ready_count <= conv_integer ( std_logic_vector ( sc_mem_in.rdy_cnt ) ) ;

    sc_mem_out.rd <= read;
    sc_mem_out.wr <= '0';
    sc_mem_out.wr_data <= total; -- ( others => '0' );
    sc_mem_out.atomic <= '0' ;

    process ( address ) is
    begin
        sc_mem_out.address <= ( others => '0' ) ;
        sc_mem_out.address <= address ( SC_ADDR_SIZE - 1 downto 0 );
    end process ;


end architecture cp ;


