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
--	jop_512x32.vhd
--
--	top level for a 512x32 SRMA board (e.g. Altera DE2 board)
--
--	2009-03-31	adapted from jop_256x16.vhd
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.sc_arbiter_pack.all;
use work.jop_config_global.all;
use work.jop_config.all;


entity jop is

generic (				
	ram_cnt		: integer := 3;		-- clock cycles for external ram
    --rom_cnt	: integer := 3;		-- clock cycles for external rom OK for 20 MHz
    rom_cnt		: integer := 15;	-- clock cycles for external rom for 100 MHz
	jpc_width	: integer := 12;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4;		-- 2*block_bits is number of cache blocks
	spm_width	: integer := 0		-- size of scratchpad RAM (in number of address bits for 32-bit words)
);

port (
	clk				: in std_logic;
	clk2				: in std_logic;
--
--	serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;
	oUART_CTS		: in std_logic;
	iUART_RTS		: out std_logic;
	
--
--	watchdog
--
	wd				: out std_logic;

--
--	only one ram bank
--
	oSRAM_A		 : out std_logic_vector(18 downto 0);		-- edit
	SRAM_DQ		 : inout std_logic_vector(31 downto 0);		-- edit
	oSRAM_CE1_N	 : out std_logic;
	oSRAM_OE_N	 : out std_logic;
	oSRAM_BE_N	 : out std_logic_vector(3 downto 0);
	oSRAM_WE_N	 : out std_logic;
	oSRAM_GW_N   : out std_logic;
	oSRAM_CLK	 : out std_logic;
	oSRAM_ADSC_N : out std_logic;
	oSRAM_ADSP_N : out std_logic;
	oSRAM_ADV_N	 : out std_logic;
	oSRAM_CE2	 : out std_logic;
	oSRAM_CE3_N  : out std_logic;

--	
--VGA output signals
--
	oVGA_HS :out std_logic;
    oVGA_VS :out std_logic;
    oVGA_BLANK_N :out std_logic;
    oVGA_SYNC_N :out std_logic;
    oVGA_CLOCK :out std_logic;
    oVGA_R :out std_logic_vector(9 downto 0);
    oVGA_G :out std_logic_vector(9 downto 0);
    oVGA_B :out std_logic_vector(9 downto 0);
    
--
--ps2 kbd controller i/o signals
--
	PS2_KBCLK :inout std_logic;
	PS2_KBDAT :inout std_logic;
	
	-- ps2 mouse clock and data i/o signals.
	PS2_MSCLK :inout std_logic;
	PS2_MSDAT :inout std_logic

);
end jop;

architecture rtl of jop is

--
--	components:
--

component pll is
generic (multiply_by : natural; divide_by : natural);
port (
	inclk0		: in std_logic;
	c0			: out std_logic
);
end component;

component crt_pll
	PORT
	(
		areset		: IN STD_LOGIC  := '0';
		inclk0		: IN STD_LOGIC  := '0';
		c0		: OUT STD_LOGIC ;
		locked		: OUT STD_LOGIC 
	);
end component;

component arbiter is
generic(
			addr_bits : integer;
			cpu_cnt	: integer);		-- number of masters for the arbiter
port (
			clk, reset	: in std_logic;			
			arb_out			: in arb_out_type(0 to cpu_cnt-1);
			arb_in			: out arb_in_type(0 to cpu_cnt-1);
			mem_out			: out sc_out_type;
			mem_in			: in sc_in_type
);
end component;

component vga_fb is
generic (sh_mem_start_address :integer := 16#78500#;
			sh_mem_end_address :integer := 16#7d000#); 
  port ( 
    reset :in std_logic;
    clk :in std_logic;
    pixel_clk :in std_logic;
    VGA_HS :out std_logic;
    VGA_VS :out std_logic;
    VGA_BLANK_N :out std_logic;
    VGA_SYNC_N :out std_logic;
    VGA_CLOCK :out std_logic;
    VGA_R :out std_logic_vector(9 downto 0);
    VGA_G :out std_logic_vector(9 downto 0);
    VGA_B :out std_logic_vector(9 downto 0);
	
	--simpcon  master interface
	address		: out std_logic_vector(22 downto 0);
	wr_data		: out std_logic_vector(31 downto 0);
	rd, wr		: out std_logic;
	rd_data		: in std_logic_vector(31 downto 0);
	rdy_cnt		: in unsigned(1 downto 0)
    );
end component;

--
--	Signals
--
	constant cpu_cnt :integer := 2; -- 1 cpu, one vga 
	signal reset,nxt_reset,reset_off,nxt_off :std_logic;
	signal clk_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	attribute altera_attribute : string;
	attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

--
--	jopcpu connections
--
	signal sc_arb_out		: arb_out_type(0 to cpu_cnt-1);
	signal sc_arb_in		: arb_in_type(0 to cpu_cnt-1);

	signal sc_mem_out		: sc_out_type;
	signal sc_mem_in		: sc_in_type;
	signal sc_io_out		: sc_out_type;
	signal sc_io_in			: sc_in_type;
	signal irq_in			: irq_bcf_type;
	signal irq_out			: irq_ack_type;
	signal exc_req			: exception_type;

--
--	IO interface
--
	signal ser_in			: ser_in_type;
	signal ser_out			: ser_out_type;
	signal wd_out			: std_logic;

	-- for generation of internal reset
-- memory interface

	signal ram_addr			: std_logic_vector(18 downto 0);	-- edit
	signal ram_dout			: std_logic_vector(31 downto 0);	-- edit
	signal ram_din			: std_logic_vector(31 downto 0);	-- edit
	signal ram_dout_en		: std_logic;
	signal ram_ncs			: std_logic;
	signal ram_noe			: std_logic;
	signal ram_nwe			: std_logic;

-- not available at this board:
	signal ser_ncts			: std_logic;
	signal ser_nrts			: std_logic;
	
-- remove the comment for RAM access counting
-- signal ram_count		: std_logic;

	signal locked :std_logic;
	signal pixel_clk :std_logic;
	
--signals for kbd ps2 controller
	signal kbd_clk_in,kbd_clk_out,kbd_data_in,kbd_data_out,kbd_data_oe,kbd_clk_oe :std_logic;
	
-- signals for mouse ps2 controller
    signal ms_clk_in, ms_clk_out, ms_data_in, ms_data_out, ms_data_oe, ms_clk_oe :std_logic;

begin

reset <= '0';

--mux for the tri-state ports of ps2 controller pins
tri_state_mux_kbd : process(kbd_clk_out,kbd_data_out,kbd_data_oe,kbd_clk_oe,PS2_KBCLK,PS2_KBDAT)
begin
	
	if(kbd_data_oe = '1') then
		PS2_KBDAT <= kbd_data_out;
	else
      PS2_KBDAT <= 'Z';
    end if;
      kbd_data_in <= PS2_KBDAT;
      
    if(kbd_clk_oe = '1') then
      PS2_KBCLK <= kbd_clk_out;
    else
      PS2_KBCLK <= 'Z';
    end if;
      kbd_clk_in <= PS2_KBCLK;

end process;

--ser_ncts <= '0';
--
--	intern reset
--	no extern reset, epm7064 has too less pins
--

process(clk_int)
begin
	if rising_edge(clk_int) then
		if (res_cnt/="111") then
			res_cnt <= res_cnt+1;
		end if;

		int_res <= not res_cnt(0) or not res_cnt(1) or not res_cnt(2);
	end if;
end process;

--
--	components of jop
--

	pll_inst : pll generic map(
		multiply_by => pll_mult,
		divide_by => pll_div
	)
	port map (
		inclk0	 => clk,
		c0	 => clk_int
	);
	
my_crt_pll : crt_pll
	PORT map
	(
		areset		=> reset,
		inclk0		=> clk2,
		c0		=> pixel_clk,
		locked		=> locked 
	);


	wd <= wd_out;

	cpu: entity work.jopcpu
		generic map(
			jpc_width => jpc_width,
			block_bits => block_bits,
			spm_width => spm_width
		)
		port map(clk_int, int_res,
			sc_arb_out(1), sc_arb_in(1),
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req);
			
	arbiter : entity work.arbiter
		generic map(
			addr_bits => SC_ADDR_SIZE,
			cpu_cnt	=> 2)		-- number of masters for the arbiter
		port map(
			clk => clk_int,
			reset => reset,			
			arb_out => sc_arb_out,
			arb_in => sc_arb_in,
			mem_out => sc_mem_out,
			mem_in => sc_mem_in
		);

	vga :entity work.vga_fb
		generic map (sh_mem_start_address => 16#78500#,
			sh_mem_end_address => 16#7d000#) 
		port map( 
			reset => reset,
			clk => clk_int,
			pixel_clk => pixel_clk,
			VGA_HS => oVGA_HS,
			VGA_VS  => oVGA_VS,
			VGA_BLANK_N => oVGA_BLANK_N,
			VGA_SYNC_N => oVGA_SYNC_N,
			VGA_CLOCK => oVGA_CLOCK,
			VGA_R => oVGA_R,
			VGA_G => oVGA_G,
			VGA_B => oVGA_B,
	
			--simpcon  master interface
			address	=> sc_arb_out(0).address,
			wr_data	=> sc_arb_out(0).wr_data,
			rd => sc_arb_out(0).rd,
			wr => sc_arb_out(0).wr,
			rd_data => sc_arb_in(0).rd_data,
			rdy_cnt => sc_arb_in(0).rdy_cnt
    );
    
    
	io: entity work.scio 
		port map (clk_int, int_res,
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req,

			txd => ser_txd,
			rxd => ser_rxd,
			ncts => oUART_CTS,
			nrts => iUART_RTS,
			wd => wd_out,
			l => open,
			r => open,
			t => open,
			b => open,
			
			--ps2 kbd pins	
			kbd_clk_in => kbd_clk_in,
			kbd_clk_out => kbd_clk_out,
			kbd_data_in => kbd_data_in,
			kbd_data_out => kbd_data_out,
			kbd_data_oe => kbd_data_oe,
			kbd_clk_oe => kbd_clk_oe,
			
			--ps2 mouse pins
			ps2_clk   => PS2_MSCLK,
			ps2_data  => PS2_MSDAT
			
			-- remove the comment for RAM access counting
			-- ram_cnt => ram_count
		);
		
	scm: entity work.sc_mem_if
		generic map (
			ram_ws => ram_cnt-1,
			addr_bits => 19			-- edit
		)
		port map (clk_int, int_res,
			sc_mem_out, sc_mem_in,

			ram_addr => ram_addr,
			ram_dout => ram_dout,
			ram_din => ram_din,
			ram_dout_en	=> ram_dout_en,
			ram_ncs => ram_ncs,
			ram_noe => ram_noe,
			ram_nwe => ram_nwe
		);

	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			SRAM_DQ <= ram_dout;
		else
			SRAM_DQ <= (others => 'Z');
		end if;
	end process;

	ram_din <= SRAM_DQ;
	
	-- remove the comment for RAM access counting
	-- ram_count <= ram_ncs;

--
--	To put this RAM address in an output register
--	we have to make an assignment (FAST_OUTPUT_REGISTER)
--
	oSRAM_A <= ram_addr;
	oSRAM_CE1_N <= ram_ncs;
	oSRAM_OE_N <= ram_noe;
	oSRAM_WE_N <= ram_nwe;
	oSRAM_BE_N <= (others => '0');
	oSRAM_GW_N <= '1';
	oSRAM_CLK <= clk_int;
	
	oSRAM_ADSC_N <= ram_ncs;
	oSRAM_ADSP_N <= '1';
	oSRAM_ADV_N	<= '1';
	
	oSRAM_CE2 <= not(ram_ncs);	
    oSRAM_CE3_N <= ram_ncs;
   

end rtl;
