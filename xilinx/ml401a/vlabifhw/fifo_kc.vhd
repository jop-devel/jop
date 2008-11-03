--
--  
--  fifo_kc.vhd
--  This file implements the same interface as fifo_ms.vhd, but it
--  uses Ken Chapman's bbfifo_16x8 component (shipped with Picoblaze)
--  instead of Martin Schoeberl's pure VHDL. bbfifo_16x8 only works on
--  Xilinx devices and has a non-free licence, but it takes up about six
--  times less space.
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


library ieee;
use ieee.std_logic_1164.all;

entity fifo_kc is

generic (
    width : integer := 8; 
    depth : integer := 4; 
    thres : integer := 2);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	din		: in std_logic_vector(width-1 downto 0);
	dout	: out std_logic_vector(width-1 downto 0);

	rd		: in std_logic;
	wr		: in std_logic;

	empty	: out std_logic;
	full	: out std_logic;
	half	: out std_logic
);
end fifo_kc ;

architecture rtl of fifo_kc is

    constant links      : Natural := depth / 16;

    component bbfifo_16x8 is
    Port (       data_in : in std_logic_vector(7 downto 0);
                data_out : out std_logic_vector(7 downto 0);
                   reset : in std_logic;               
                   write : in std_logic; 
                    read : in std_logic;
                    full : out std_logic;
               half_full : out std_logic;
            data_present : out std_logic;
                     clk : in std_logic);
    end component bbfifo_16x8;

    type dlarray is array ( Natural range 0 to ( links + 1 )) of 
                                    std_logic_vector ( 7 downto 0 ) ;
    signal data_link        : dlarray;
    signal write_link       : std_logic_vector (( links + 1 )  downto 0 ) ;
    signal full_link        : std_logic_vector (( links + 1 )  downto 0 ) ;
    signal present_link     : std_logic_vector (( links + 1 )  downto 0 ) ;


begin

    assert width = 8;

    data_link ( 0 ) <= din;
    dout <= data_link ( links + 1 );
    write_link ( 0 ) <= wr;
    
    gc : for segment in 0 to links
    generate
        gc1 : block
            signal read     : std_logic;
            signal half_seg : std_logic;
        begin
            kcfifo : bbfifo_16x8
                port map (
                    data_in => data_link ( segment ),
                    data_out => data_link ( segment + 1 ),
                    reset => reset,
                    write => write_link ( segment ),
                    read => read,
                    full => full_link ( segment ),
                    half_full => half_seg,
                    data_present => present_link ( segment ),
                    clk => clk);
           
            process ( clk ) is
            begin
                if ( clk = '1' )
                and ( clk'event )
                then
                    if ( segment < links )
                    then
                        -- not the last segment
                        -- forward data between segments
                        read <= write_link ( segment + 1 );
                        write_link ( segment + 1 ) <= '0';

                        if (( present_link ( segment ) = '1' )
                        and ( full_link ( segment + 1 ) = '0' )
                        and ( write_link ( segment + 1 ) = '0' )
                        and ( read = '0' ))
                        then
                            write_link ( segment + 1 ) <= '1';
                        end if;
                    end if;
                end if;
            end process;

            process ( rd, half_seg, present_link, full_link ) is
            begin
                if ( segment = links )
                then
                    -- last segment: empty? rd? half full?
                    read <= rd;
                    half <= half_seg;
                    empty <= not present_link ( segment );
                end if;
                if ( segment = 0 )
                then
                    -- first segment: full?
                    full <= full_link ( segment );
                end if;
            end process;
        end block gc1;
    end generate gc;

	
end rtl;

