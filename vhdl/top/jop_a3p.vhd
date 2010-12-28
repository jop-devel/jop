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
--	jop_a3p.vhd
--
--	top level for Actel A3P board / ProASIC3
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config_global.all;
use work.jop_config.all;


entity jop is

generic (
	ram_cnt		: integer := 4;		-- clock cycles for external ram
	rom_cnt		: integer := 15;	-- not used for S3K
	jpc_width	: integer := 11;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4;		-- 2*block_bits is number of cache blocks
	spm_width	: integer := 0		-- size of scratchpad RAM (in number of address bits for 32-bit words)
);

port (
	clk		: in std_logic;
	reset           : in std_logic;
--
--	watchdog
--
	wd		: out std_logic;
        A               : out std_logic;
        B               : out std_logic;
        C               : out std_logic;
        D               : out std_logic
);
end jop;

architecture rtl of jop is

  component actelpll
    port(
      POWERDOWN, CLKA : in std_logic;
      LOCK, GLA : out std_logic
      );
  end component;
  
--
--	Signals
--
	signal clk_int			: std_logic;
        signal lock                     : std_logic;
	signal int_res			: std_logic;
  
--
--	jopcpu connections
--
	signal sc_io_out		: sc_out_type;
	signal sc_io_in			: sc_in_type;
	signal irq_in			: irq_bcf_type;
	signal irq_out			: irq_ack_type;
	signal exc_req			: exception_type;

--
--	IO interface
--
	signal wd_out			: std_logic;

--
--      dummy signals
--
        signal sc_mem_in                : sc_in_type;
        signal rxd                      : std_logic;
        signal ncts                     : std_logic;

--
--      synchronizing reset signal
--
        signal reset_sync               : std_logic;
        signal reset_buf1               : std_logic;
        signal reset_buf2               : std_logic;
        signal reset_dly                : std_logic;
        signal reset_cnt                : unsigned(8 downto 0);
  
begin

--
--      dummy signals
--
sc_mem_in.rd_data <= (others => '0');
sc_mem_in.rdy_cnt <= "00";

rxd <= '1';
ncts <= '0';

--
--	synchronize reset
--

process(clk_int)
begin
  if rising_edge(clk_int) then
    
    int_res <= reset_sync;
    reset_sync <= reset_cnt(0) or reset_cnt(1) or reset_cnt(2) or reset_cnt(3) or
                  reset_cnt(4) or reset_cnt(5) or reset_cnt(6) or reset_cnt(7) or
                  reset_cnt(8) or reset_buf2;

    if reset_buf2 = '1' and reset_dly /= reset_buf2 then
      int_res <= '0';                   -- force edge
      reset_cnt <= (others => '1');
    elsif reset_cnt /= X"000" then
      reset_cnt <= reset_cnt - 1;                   
    end if;
    
    reset_dly <= reset_buf2;
    reset_buf2 <= reset_buf1;
    reset_buf1 <= reset;
    
  end if;
end process;

--
--	components of jop
--

wd <= wd_out;
A <= reset;
B <= reset_sync;
C <= lock;
D <= clk_int;

pll: actelpll
  port map (
    POWERDOWN => '1',
    CLKA      => clk,
    LOCK      => lock,
    GLA       => clk_int );

cpu: entity work.jopcpu
  generic map(
    jpc_width => jpc_width,
	block_bits => block_bits,
	spm_width => spm_width )
  port map(
    clk => clk_int,
    reset => int_res,
    sc_mem_out => open,
    sc_mem_in => sc_mem_in,
    sc_io_out => sc_io_out,
    sc_io_in => sc_io_in,
    irq_in => irq_in,
    irq_out => irq_out,
    exc_req => exc_req);

io: entity work.scio 
  port map (
    clk => clk_int,
    reset => int_res,
    sc_io_out => sc_io_out,
    sc_io_in => sc_io_in,
    irq_in => irq_in,
    irq_out => irq_out,
    exc_req => exc_req,
    txd => open,
    rxd => rxd,
    ncts => ncts,
    nrts => open,
    wd => wd_out,
    l => open,
    r => open,
    t => open,
    b => open );

end rtl;
