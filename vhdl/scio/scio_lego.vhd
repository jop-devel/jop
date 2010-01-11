--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
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
--	scio_lego.vhd
--
--	io devices for LEGO MindStorms
--
--
--	io address mapping:
--
--	IO Base is 0xffffff80 for 'fast' constants (bipush)
--
--		0x00 0-3		system clock counter, us counter, timer int, wd bit
--		0x10 0-1		uart (download)
--		0x30			LEGO interface
--
--	status word in uarts:
--		0	uart transmit data register empty
--		1	uart read data register full
--
--
--	todo:
--
--
--	2003-07-09	created
--	2005-08-27	ignore ncts on uart
--	2005-11-30	changed to SimpCon
--  2007-03-26	changed for Lego PCB
--  2007-05-15  adapted to SimpCon changes
--


library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;

entity scio is

generic (cpu_id : integer := 0; cpu_cnt : integer := 1);

    port (
        clk		: in std_logic;
        reset	: in std_logic;

--
--	SimpCon IO interface
--
	sc_io_out		: in sc_out_type;
	sc_io_in		: out sc_in_type;

--
--	Interrupts from IO devices
--
	irq_in			: out irq_bcf_type;
	irq_out			: in irq_ack_type;
	exc_req			: in exception_type;
	
-- CMP

	sync_out : in sync_out_type := NO_SYNC;
	sync_in	 : out sync_in_type;	

-- serial interface
-- slightly abused in this design

        txd			: out std_logic;
        rxd			: in std_logic;

-- not connected in this design
        ncts		: in std_logic;

-- ss3 in this design
        nrts		: out std_logic;

-- watch dog

        wd			: out std_logic;

-- core i/o pins
        l			: inout std_logic_vector(20 downto 1);
        r			: inout std_logic_vector(20 downto 1);
        t			: inout std_logic_vector(6 downto 1);
        b			: inout std_logic_vector(10 downto 1)
        );
end scio;


architecture rtl of scio is

    constant SLAVE_CNT : integer := 4;
    -- SLAVE_CNT <= 2**DECODE_BITS
    constant DECODE_BITS : integer := 2;
    -- number of bits that can be used inside the slave
    constant SLAVE_ADDR_BITS : integer := 4;

    type slave_bit is array(0 to SLAVE_CNT-1) of std_logic;
    signal sc_rd, sc_wr		: slave_bit;

    type slave_dout is array(0 to SLAVE_CNT-1) of std_logic_vector(31 downto 0);
    signal sc_dout			: slave_dout;

    type slave_rdy_cnt is array(0 to SLAVE_CNT-1) of unsigned(1 downto 0);
    signal sc_rdy_cnt		: slave_rdy_cnt;

    signal sel, sel_reg		: integer range 0 to 2**DECODE_BITS-1;

    signal nrts_ignored		: std_logic;

begin

--
--	unused and input pins tri state
--

    t(5) <= 'Z';	-- ss0
    t(6) <= 'Z';	-- ss1

    t(3) <= 'Z';	-- sclk
    nrts <= 'Z';	-- ss2

    l(3) <= 'Z';	-- sdi
    
    b(9) <= 'Z';	-- s2pi
    b(8) <= 'Z';	-- s1pi
    b(7) <= 'Z';	-- s0pi
    b(5) <= 'Z';	-- s2di
    b(2) <= 'Z';	-- s1di
    b(1) <= 'Z';	-- s0di
    
    l(2) <= 'Z';	-- sdo

    r(19 downto 14) <= (others => 'Z');	-- sd-card



    assert SLAVE_CNT <= 2**DECODE_BITS report "Wrong constant in scio";

	sel <= to_integer(unsigned(sc_io_out.address(SLAVE_ADDR_BITS+DECODE_BITS-1 downto SLAVE_ADDR_BITS)));

	-- What happens when sel_reg > SLAVE_CNT-1??
	sc_io_in.rd_data <= sc_dout(sel_reg);
	sc_io_in.rdy_cnt <= sc_rdy_cnt(sel_reg);

	--
	-- Connect SLAVE_CNT slaves
	--
	gsl: for i in 0 to SLAVE_CNT-1 generate

		sc_rd(i) <= sc_io_out.rd when i=sel else '0';
		sc_wr(i) <= sc_io_out.wr when i=sel else '0';

	end generate;

  --
	--	Register read and write mux selector
	--
	process(clk, reset)
	begin
		if (reset='1') then
			sel_reg <= 0;
		elsif rising_edge(clk) then
			if sc_io_out.rd='1' or sc_io_out.wr='1' then
				sel_reg <= sel;
			end if;
		end if;
	end process;
    
    sys: entity work.sc_sys generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
			cpu_id => cpu_id,
			cpu_cnt => cpu_cnt
        )
        port map(
            clk => clk,
            reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(0),
			wr => sc_wr(0),
			rd_data => sc_dout(0),
			rdy_cnt => sc_rdy_cnt(0),

			irq_in => irq_in,
			irq_out => irq_out,
			exc_req => exc_req,
			
			sync_out => sync_out,
			sync_in => sync_in,
			
			wd => wd
            );

    ua: entity work.sc_uart generic map (
        addr_bits => SLAVE_ADDR_BITS,
        clk_freq => clk_freq,
        baud_rate => 115200,
        txf_depth => 2,
        txf_thres => 1,
        rxf_depth => 2,
        rxf_thres => 1
        )
        port map(
            clk => clk,
            reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
            rd => sc_rd(1),
            wr => sc_wr(1),
            rd_data => sc_dout(1),
            rdy_cnt => sc_rdy_cnt(1),

            txd	 => txd,
            rxd	 => rxd,
            ncts => '0',
            nrts => nrts_ignored
            );

    -- slave 2 is reserved for USB and System.out writes to it!!!

    usb: entity work.sc_usb generic map (
        addr_bits => SLAVE_ADDR_BITS,
        clk_freq => clk_freq
        )
        port map(
            clk => clk,
            reset => reset,

            address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
            wr_data => sc_io_out.wr_data,
            rd => sc_rd(2),
            wr => sc_wr(2),
            rd_data => sc_dout(2),
            rdy_cnt => sc_rdy_cnt(2),

            data => r(8 downto 1),
            nrxf => r(9),
            ntxe => r(10),
            nrd => r(11),
            ft_wr => r(12),
            nsi => r(13)
            );	

    lego: entity work.sc_lego generic map (
        addr_bits => SLAVE_ADDR_BITS,
        clk_freq => clk_freq
        )
        port map(
            clk => clk,
            reset => reset,

            address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
            wr_data => sc_io_out.wr_data,
            rd => sc_rd(3),
            wr => sc_wr(3),
            rd_data => sc_dout(3),
            rdy_cnt => sc_rdy_cnt(3),

            -- LEGO interface

			-- speaker
			
			speaker => l(1),

            -- motor stuff
            
            m0en => l(4),
            m0dir => l(5),
            m0break => l(6),
            m0dia => l(13),
            m0doa => l(14),
            m0dib => l(15),
            m0dob => l(16),

            m1en => l(8),
            m1dir => l(9),
            m1break => l(7),
            m1dia => l(17),
            m1doa => l(18),
            m1dib => l(19),
            m1dob => l(20),

            m2en => l(10),
            m2dir => l(11),
            m2break => l(12),

            -- sensor stuff
            
            s0di => b(1),
            s0do => b(3),
            s0pi => b(7),
            s1di => b(2),
            s1do => b(4),
            s1pi => b(8),
            s2di => b(5),
            s2do => b(6),
            s2pi => b(9),

			-- microphone

            mic1do => b(10),
            mic1 => r(20),
            
            -- pld

            pld_strobe => t(1),
            pld_data => t(2),
            pld_clk	=> t(4)
            );

end rtl;
