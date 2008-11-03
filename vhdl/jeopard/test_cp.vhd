

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_unsigned.all;

use work.jop_types.all;
use work.sc_pack.all;

entity test_cp is
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
end entity test_cp;

architecture cp of test_cp is
    constant num_units : Integer := 12 ;

    type param_array is array ( 1 to num_units ) of 
                                    std_logic_vector ( 31 downto 0 );
    type logic_array is array ( 1 to num_units ) of std_logic;
    type memory_array is array ( 0 to (( num_units * 4 ) - 1 )) of
                                    std_logic_vector ( 31 downto 0 );

    signal method_testXX_param_address: param_array;
    signal method_testXX_param_data   : param_array;
    signal method_testXX_param_write  : param_array;
    signal method_testXX_param_delay  : param_array;
    signal method_testXX_return       : param_array;
    signal method_testXX_counter      : param_array;
    signal method_testXX_start        : logic_array;
    signal method_testXX_running      : logic_array;
    signal memory                     : memory_array;
begin
    m : entity test_cp_if 
        port map (
            clk => clk,
            reset => reset,

            method_test01_param_address => method_testXX_param_address ( 1 ),
            method_test01_param_data => method_testXX_param_data ( 1 ),
            method_test01_param_write => method_testXX_param_write ( 1 ),
            method_test01_param_delay => method_testXX_param_delay ( 1 ),
            method_test01_return => method_testXX_return ( 1 ),
            method_test01_start => method_testXX_start ( 1 ),
            method_test01_running => method_testXX_running ( 1 ),
            method_test02_param_address => method_testXX_param_address ( 2 ),
            method_test02_param_data => method_testXX_param_data ( 2 ),
            method_test02_param_write => method_testXX_param_write ( 2 ),
            method_test02_param_delay => method_testXX_param_delay ( 2 ),
            method_test02_return => method_testXX_return ( 2 ),
            method_test02_start => method_testXX_start ( 2 ),
            method_test02_running => method_testXX_running ( 2 ),
            method_test03_param_address => method_testXX_param_address ( 3 ),
            method_test03_param_data => method_testXX_param_data ( 3 ),
            method_test03_param_write => method_testXX_param_write ( 3 ),
            method_test03_param_delay => method_testXX_param_delay ( 3 ),
            method_test03_return => method_testXX_return ( 3 ),
            method_test03_start => method_testXX_start ( 3 ),
            method_test03_running => method_testXX_running ( 3 ),
            method_test04_param_address => method_testXX_param_address ( 4 ),
            method_test04_param_data => method_testXX_param_data ( 4 ),
            method_test04_param_write => method_testXX_param_write ( 4 ),
            method_test04_param_delay => method_testXX_param_delay ( 4 ),
            method_test04_return => method_testXX_return ( 4 ),
            method_test04_start => method_testXX_start ( 4 ),
            method_test04_running => method_testXX_running ( 4 ),
            method_test05_param_address => method_testXX_param_address ( 5 ),
            method_test05_param_data => method_testXX_param_data ( 5 ),
            method_test05_param_write => method_testXX_param_write ( 5 ),
            method_test05_param_delay => method_testXX_param_delay ( 5 ),
            method_test05_return => method_testXX_return ( 5 ),
            method_test05_start => method_testXX_start ( 5 ),
            method_test05_running => method_testXX_running ( 5 ),
            method_test06_param_address => method_testXX_param_address ( 6 ),
            method_test06_param_data => method_testXX_param_data ( 6 ),
            method_test06_param_write => method_testXX_param_write ( 6 ),
            method_test06_param_delay => method_testXX_param_delay ( 6 ),
            method_test06_return => method_testXX_return ( 6 ),
            method_test06_start => method_testXX_start ( 6 ),
            method_test06_running => method_testXX_running ( 6 ),
            method_test07_param_address => method_testXX_param_address ( 7 ),
            method_test07_param_data => method_testXX_param_data ( 7 ),
            method_test07_param_write => method_testXX_param_write ( 7 ),
            method_test07_param_delay => method_testXX_param_delay ( 7 ),
            method_test07_return => method_testXX_return ( 7 ),
            method_test07_start => method_testXX_start ( 7 ),
            method_test07_running => method_testXX_running ( 7 ),
            method_test08_param_address => method_testXX_param_address ( 8 ),
            method_test08_param_data => method_testXX_param_data ( 8 ),
            method_test08_param_write => method_testXX_param_write ( 8 ),
            method_test08_param_delay => method_testXX_param_delay ( 8 ),
            method_test08_return => method_testXX_return ( 8 ),
            method_test08_start => method_testXX_start ( 8 ),
            method_test08_running => method_testXX_running ( 8 ),
            method_test09_param_address => method_testXX_param_address ( 9 ),
            method_test09_param_data => method_testXX_param_data ( 9 ),
            method_test09_param_write => method_testXX_param_write ( 9 ),
            method_test09_param_delay => method_testXX_param_delay ( 9 ),
            method_test09_return => method_testXX_return ( 9 ),
            method_test09_start => method_testXX_start ( 9 ),
            method_test09_running => method_testXX_running ( 9 ),
            method_test10_param_address => method_testXX_param_address ( 10 ),
            method_test10_param_data => method_testXX_param_data ( 10 ),
            method_test10_param_write => method_testXX_param_write ( 10 ),
            method_test10_param_delay => method_testXX_param_delay ( 10 ),
            method_test10_return => method_testXX_return ( 10 ),
            method_test10_start => method_testXX_start ( 10 ),
            method_test10_running => method_testXX_running ( 10 ),
            method_test11_param_address => method_testXX_param_address ( 11 ),
            method_test11_param_data => method_testXX_param_data ( 11 ),
            method_test11_param_write => method_testXX_param_write ( 11 ),
            method_test11_param_delay => method_testXX_param_delay ( 11 ),
            method_test11_return => method_testXX_return ( 11 ),
            method_test11_start => method_testXX_start ( 11 ),
            method_test11_running => method_testXX_running ( 11 ),
            method_test12_param_address => method_testXX_param_address ( 12 ),
            method_test12_param_data => method_testXX_param_data ( 12 ),
            method_test12_param_write => method_testXX_param_write ( 12 ),
            method_test12_param_delay => method_testXX_param_delay ( 12 ),
            method_test12_return => method_testXX_return ( 12 ),
            method_test12_start => method_testXX_start ( 12 ),
            method_test12_running => method_testXX_running ( 12 ),

            cc_out_data => cc_out_data,
            cc_out_wr => cc_out_wr,
            cc_out_rdy => cc_out_rdy,

            cc_in_data => cc_in_data,
            cc_in_wr => cc_in_wr,
            cc_in_rdy => cc_in_rdy);
   


            
    process ( clk ) is
        variable index      : Integer;
    begin
        if ( reset = '1' )
        then
            for i in 1 to num_units
            loop
                method_testXX_running ( i ) <= '0';
                method_testXX_counter ( i ) <= ( others => '0' );
            end loop;
        elsif ( clk = '1' )
        and ( clk'event )
        then
            for i in 1 to num_units
            loop
                if ( method_testXX_start ( i ) = '1' )
                then
                    index := conv_integer ( 
                        method_testXX_param_address ( i )( 1 downto 0 ) ) + 
                        (( i - 1 ) * 4 ) ;

                    method_testXX_return ( i ) <= memory ( index ) ;
                    method_testXX_running ( i ) <= '1';
                    if ( method_testXX_param_write ( i )( 0 ) = '1' )
                    then
                        memory ( index ) <= method_testXX_param_data ( i );
                    end if;
                    method_testXX_counter ( i ) <= 
                            method_testXX_param_delay ( i );
                elsif ( method_testXX_running ( i ) = '1' )
                then
                    if ( method_testXX_counter ( i ) = x"00000000" )
                    then
                        method_testXX_running ( i ) <= '0';
                    else
                        method_testXX_counter ( i ) <=
                            method_testXX_counter ( i ) - 1;
                    end if;
                end if;
            end loop;
        end if;
    end process;
                
end architecture cp ;


