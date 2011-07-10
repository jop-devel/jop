--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2011, Oleg Belousov (belousov.oleg@gmail.com)
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
--	top level for ZTEX USB-FPGA 1.11c module

library ieee;

use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;

library unisim;
use unisim.vcomponents.all;

entity top is

generic (
    ram_cnt	: integer := 4;		-- clock cycles for external ram
    rom_cnt	: integer := 15;	-- not used for S3K
    jpc_width	: integer := 11;	-- address bits of java bytecode pc = cache size
    block_bits	: integer := 4;		-- 2*block_bits is number of cache blocks = 4
    spm_width	: integer := 0		-- size of scratchpad RAM (in number of address bits for 32-bit words)
);

port (
    clk         	: in std_logic;
    rst         	: in std_logic;

    -- JOP serial interface

    jop_tx     		: out std_logic;
    jop_rx     		: in std_logic;

    -- DDR-SDRAM

    mcb3_dram_dq    	: inout std_logic_vector(15 downto 0);
    mcb3_rzq        	: inout std_logic;
    mcb3_dram_udqs  	: inout std_logic;
    mcb3_dram_dqs   	: inout std_logic;
    mcb3_dram_a     	: out std_logic_vector(12 downto 0);
    mcb3_dram_ba    	: out std_logic_vector(1 downto 0);
    mcb3_dram_cke   	: out std_logic;
    mcb3_dram_ras_n 	: out std_logic;
    mcb3_dram_cas_n 	: out std_logic;
    mcb3_dram_we_n  	: out std_logic;
    mcb3_dram_dm    	: out std_logic;
    mcb3_dram_udm   	: out std_logic;
    mcb3_dram_ck    	: out std_logic;
    mcb3_dram_ck_n  	: out std_logic
);
end top;

architecture rtl of top is

component mig_37
 generic(
    C3_P0_MASK_SIZE           : integer := 4;
    C3_P0_DATA_PORT_SIZE      : integer := 32;
    C3_P1_MASK_SIZE           : integer := 4;
    C3_P1_DATA_PORT_SIZE      : integer := 32;
    C3_MEMCLK_PERIOD          : integer := 5000;
    C3_RST_ACT_LOW            : integer := 0;
    C3_INPUT_CLK_TYPE         : string := "SINGLE_ENDED";
    C3_CALIB_SOFT_IP          : string := "TRUE";
    C3_SIMULATION             : string := "FALSE";
    DEBUG_EN                  : integer := 0;
    C3_MEM_ADDR_ORDER         : string := "ROW_BANK_COLUMN";
    C3_NUM_DQ_PINS            : integer := 16;
    C3_MEM_ADDR_WIDTH         : integer := 13;
    C3_MEM_BANKADDR_WIDTH     : integer := 2
);
    port (
   mcb3_dram_dq                            : inout  std_logic_vector(C3_NUM_DQ_PINS-1 downto 0);
   mcb3_dram_a                             : out std_logic_vector(C3_MEM_ADDR_WIDTH-1 downto 0);
   mcb3_dram_ba                            : out std_logic_vector(C3_MEM_BANKADDR_WIDTH-1 downto 0);
   mcb3_dram_cke                           : out std_logic;
   mcb3_dram_ras_n                         : out std_logic;
   mcb3_dram_cas_n                         : out std_logic;
   mcb3_dram_we_n                          : out std_logic;
   mcb3_dram_dm                            : out std_logic;
   mcb3_dram_udqs                          : inout  std_logic;
   mcb3_rzq                                : inout  std_logic;
   mcb3_dram_udm                           : out std_logic;
   c3_sys_clk                              : in  std_logic;
   c3_sys_rst_n                            : in  std_logic;
   c3_calib_done                           : out std_logic;
   c3_clk0                                 : out std_logic;
   c3_rst0                                 : out std_logic;
   mcb3_dram_dqs                           : inout  std_logic;
   mcb3_dram_ck                            : out std_logic;
   mcb3_dram_ck_n                          : out std_logic;
   c3_p0_cmd_clk                           : in std_logic;
   c3_p0_cmd_en                            : in std_logic;
   c3_p0_cmd_instr                         : in std_logic_vector(2 downto 0);
   c3_p0_cmd_bl                            : in std_logic_vector(5 downto 0);
   c3_p0_cmd_byte_addr                     : in std_logic_vector(29 downto 0);
   c3_p0_cmd_empty                         : out std_logic;
   c3_p0_cmd_full                          : out std_logic;
   c3_p0_wr_clk                            : in std_logic;
   c3_p0_wr_en                             : in std_logic;
   c3_p0_wr_mask                           : in std_logic_vector(C3_P0_MASK_SIZE - 1 downto 0);
   c3_p0_wr_data                           : in std_logic_vector(C3_P0_DATA_PORT_SIZE - 1 downto 0);
   c3_p0_wr_full                           : out std_logic;
   c3_p0_wr_empty                          : out std_logic;
   c3_p0_wr_count                          : out std_logic_vector(6 downto 0);
   c3_p0_wr_underrun                       : out std_logic;
   c3_p0_wr_error                          : out std_logic;
   c3_p0_rd_clk                            : in std_logic;
   c3_p0_rd_en                             : in std_logic;
   c3_p0_rd_data                           : out std_logic_vector(C3_P0_DATA_PORT_SIZE - 1 downto 0);
   c3_p0_rd_full                           : out std_logic;
   c3_p0_rd_empty                          : out std_logic;
   c3_p0_rd_count                          : out std_logic_vector(6 downto 0);
   c3_p0_rd_overflow                       : out std_logic;
   c3_p0_rd_error                          : out std_logic
);
end component;

-- System signals

signal clk_int			: std_logic;
signal rst0_int			: std_logic;
signal rst_int			: std_logic;
signal clk_mem			: std_logic;

-- DCM

signal dcm0_locked		: std_logic;
signal dcm0_clk_status		: std_logic_vector(2 downto 1);

-- MIG

signal mig_calib_done		: std_logic;
signal mig_cmd_en		: std_logic;
signal mig_cmd_instr		: std_logic_vector(2 downto 0);
signal mig_cmd_byte_addr	: std_logic_vector(29 downto 0);
signal mig_cmd_empty		: std_logic;
signal mig_cmd_bl		: std_logic_vector(5 downto 0);

signal mig_wr_en		: std_logic;
signal mig_wr_data		: std_logic_vector(31 downto 0);
signal mig_wr_empty		: std_logic;
signal mig_wr_count		: std_logic_vector(6 downto 0);

signal mig_rd_en		: std_logic;
signal mig_rd_data		: std_logic_vector(31 downto 0);
signal mig_rd_empty		: std_logic;
signal mig_rd_count		: std_logic_vector(6 downto 0);

-- jopcpu connections

signal sc_mem_out		: sc_out_type;
signal sc_mem_in		: sc_in_type;
signal sc_io_out		: sc_out_type;
signal sc_io_in			: sc_in_type;
signal irq_in			: irq_bcf_type;
signal irq_out			: irq_ack_type;
signal exc_req			: exception_type;

-- IO interface

signal ser_in			: ser_in_type;
signal ser_out			: ser_out_type;
signal wd_out			: std_logic;

begin
    rst0_int <= rst or (not dcm0_locked) or dcm0_clk_status(2);
    rst_int <= rst0_int or (not mig_calib_done);

    u_cpu: entity work.jopcpu
        generic map(
            jpc_width => jpc_width,
            block_bits => block_bits,
            spm_width => spm_width
        )
        port map(
    	    clk => clk_int, 
    	    reset => rst_int,

            sc_mem_out => sc_mem_out, 
            sc_mem_in => sc_mem_in,
            
            sc_io_out => sc_io_out, 
            sc_io_in => sc_io_in,
            
            irq_in => irq_in, 
            irq_out => irq_out, 
            exc_req => exc_req
        );

    u_io: entity work.scio
        port map (
    	    clk => clk_int, 
    	    reset => rst_int,
    	    
            sc_io_out => sc_io_out, 
            sc_io_in => sc_io_in,
            
            irq_in => irq_in, 
            irq_out => irq_out, 
            exc_req => exc_req,

            txd 	=> jop_tx,
            rxd 	=> jop_rx,
            ncts 	=> '0',
            nrts 	=> open,
            wd 		=> open,
            l 		=> open,
            r 		=> open,
            t 		=> open,
            b 		=> open
        );

    -- DDR DCM

    u_dcm0 : DCM_CLKGEN
	generic map (
    	    CLKFXDV_DIVIDE  => 4,        -- modify if other CLK than 50 MHz is desired
    	    CLKFX_DIVIDE    => 6,
    	    CLKFX_MULTIPLY  => 25,
    	    CLKFX_MD_MAX    => 0.0,
    	    CLKIN_PERIOD    => 20.833333,
    	    SPREAD_SPECTRUM => "NONE",
    	    STARTUP_WAIT    => FALSE 
	)
	port map (
    	    clkfx     => clk_mem,	-- 200 MHz = 48 MHz / CLKFX_DIVIDE * CLKFX_MULTIPLY
    	    clkfx180  => open,  
    	    clkfxdv   => open,      	-- can be used as system clock, 50 MHz = MEM_CLK / CLKFXDV_DIVIDE
    	    locked    => dcm0_locked,
    	    progdone  => open,
    	    status    => dcm0_clk_status, 
    	    clkin     => clk,
    	    freezedcm => '0',
    	    progclk   => '0',
    	    progdata  => '0',
    	    progen    => '0',
    	    rst       => rst
   );

    -- System clock DCM

    u_dcm1 : DCM_CLKGEN
	generic map (
    	    CLKFX_DIVIDE    => 16,
    	    CLKFX_MULTIPLY  => 31,
    	    CLKFX_MD_MAX    => 0.0,
    	    CLKIN_PERIOD    => 20.833333,
    	    SPREAD_SPECTRUM => "NONE",
    	    STARTUP_WAIT    => FALSE 
	)
	port map (
    	    clkfx     => clk_int,	-- 93 Mhz
    	    clkfx180  => open,  
    	    clkfxdv   => open,
    	    locked    => open,
    	    progdone  => open,
    	    status    => open, 
    	    clkin     => clk,
    	    freezedcm => '0',
    	    progclk   => '0',
    	    progdata  => '0',
    	    progen    => '0',
    	    rst       => rst
   );

    -- MIG 3.7

    u_mig_37 : mig_37
	port map (
	    mcb3_dram_dq       	=> mcb3_dram_dq,
	    mcb3_dram_a       	=> mcb3_dram_a,
	    mcb3_dram_ba       	=> mcb3_dram_ba,
	    mcb3_dram_ras_n    	=> mcb3_dram_ras_n,
	    mcb3_dram_cas_n    	=> mcb3_dram_cas_n,
	    mcb3_dram_we_n     	=> mcb3_dram_we_n,
	    mcb3_dram_cke      	=> mcb3_dram_cke,
	    mcb3_dram_ck       	=> mcb3_dram_ck,
	    mcb3_dram_ck_n     	=> mcb3_dram_ck_n,
	    mcb3_dram_dqs      	=> mcb3_dram_dqs,
	    mcb3_dram_udqs  	=> mcb3_dram_udqs,
	    mcb3_dram_udm  	=> mcb3_dram_udm,
	    mcb3_dram_dm  	=> mcb3_dram_dm,
    	    mcb3_rzq         	=> mcb3_rzq,

	    c3_sys_clk		=> clk_mem,
	    c3_sys_rst_n    	=> rst0_int,

	    c3_clk0		=> open,
	    c3_rst0		=> open,
	    c3_calib_done      	=> mig_calib_done,
  
    	    c3_p0_cmd_clk       => clk_int,
	    c3_p0_cmd_en        => mig_cmd_en,
	    c3_p0_cmd_instr     => mig_cmd_instr,
	    c3_p0_cmd_bl        => mig_cmd_bl,
	    c3_p0_cmd_byte_addr => mig_cmd_byte_addr,
	    c3_p0_cmd_empty     => mig_cmd_empty,
	    c3_p0_cmd_full      => open,

	    c3_p0_wr_clk        => clk_int,
	    c3_p0_wr_en         => mig_wr_en,
	    c3_p0_wr_mask       => ( others => '0'),
	    c3_p0_wr_data       => mig_wr_data,
	    c3_p0_wr_full       => open,
	    c3_p0_wr_empty      => mig_wr_empty,
	    c3_p0_wr_count      => mig_wr_count,
	    c3_p0_wr_underrun   => open,
	    c3_p0_wr_error      => open,

	    c3_p0_rd_clk        => clk_int,
	    c3_p0_rd_en         => mig_rd_en,
	    c3_p0_rd_data       => mig_rd_data,
	    c3_p0_rd_full       => open,
	    c3_p0_rd_empty      => mig_rd_empty,
	    c3_p0_rd_count      => mig_rd_count,
	    c3_p0_rd_overflow   => open,
	    c3_p0_rd_error      => open
	);

    u_scm: entity work.sc2mig
        port map (
	    clk => clk_int, 
	    reset => rst_int, 

            sc_mem_out => sc_mem_out, 
            sc_mem_in => sc_mem_in,
            
            mig_cmd_en => mig_cmd_en,
            mig_cmd_instr => mig_cmd_instr,
            mig_cmd_byte_addr => mig_cmd_byte_addr,
            mig_cmd_empty => mig_cmd_empty,
            mig_cmd_bl => mig_cmd_bl,
            
            mig_wr_en => mig_wr_en,
            mig_wr_data => mig_wr_data,
            mig_wr_empty => mig_wr_empty,
            mig_wr_count => mig_wr_count,

            mig_rd_en => mig_rd_en,
            mig_rd_data => mig_rd_data,
            mig_rd_empty => mig_rd_empty,
            mig_rd_count => mig_rd_count
        );

end rtl;
