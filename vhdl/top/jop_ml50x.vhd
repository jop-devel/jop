--
--	jop_ml50x.vhd
--
--	top level for ML50x Virtex-5 board
--
--	2009-12-03	creation (copy from ml401)
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
	rom_cnt		: integer := 15;	-- not used here (at the moment)
	jpc_width	: integer := 11;	-- address bits of java bytecode pc = cache size
	block_bits	: integer := 4;		-- 2*block_bits is number of cache blocks
	spm_width	: integer := 0		-- size of scratchpad RAM (in number of address bits for 32-bit words)
);

port (
	clk		: in std_logic;
	
--
---- serial interface
--
	ser_txd			: out std_logic;
	ser_rxd			: in std_logic;

--
--	watchdog
--
	wd		: out std_logic;

	led		: out std_logic;
--
---==========================================================--
----===========Virtex-5 SRAM Port============================--
	sram_clk : out std_logic;
	
	sram_addr : out std_logic_vector(21 downto 0);
	
	sram_we_n : out std_logic;
	sram_oe_n : out std_logic;

	sram_data : inout std_logic_vector(31 downto 0);
	
	sram_bw0: out std_logic;
	sram_bw1 : out std_logic;
	
	sram_bw2 : out std_Logic;
	sram_bw3 : out std_logic;
	
	sram_adv_ld_n : out std_logic;
	sram_mode : out std_logic;
	sram_cen : out std_logic;
	sram_cen_test : out std_logic;
	sram_zz : out std_logic

);
end jop;

architecture rtl of jop is

component xc5pll is
port (
	clkin1_in		: in std_logic;
	clkout0_out		: out std_logic;
	locked_out		: out std_logic
);
end component;

--
--	Signals
--
	signal clk_int			: std_logic;

	signal int_res			: std_logic;
	signal res_cnt			: unsigned(2 downto 0) := "000";	-- for the simulation

	-- attribute altera_attribute : string;
	-- attribute altera_attribute of res_cnt : signal is "POWER_UP_LEVEL=LOW";

--
--	jopcpu connections
--
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

	signal ram_addr 		: std_logic_vector(17 downto 0);
	signal ram_dout			: std_logic_vector(31 downto 0);
--	signal ram_din			: std_logic_vector(31 downto 0);
	signal ram_dout_en		: std_logic;

-- not available at this board:
	signal ser_ncts			: std_logic;
	signal ser_nrts			: std_logic;

	signal cnt				: integer;
	signal toggle			: std_logic;

begin

--
-- SSRAM conncetion
-- 	A0 is not connected (only for 16-bit Flash)
-- 	A19-A21 go to NC pins

	sram_adv_ld_n <= '0';
	sram_mode <= '0';
	sram_cen <= '0';
	sram_addr <= "000" & ram_addr & "0";
	sram_zz <= '0';
	sram_clk <= not clk_int;
	sram_bw0 <= '0';
	sram_bw1 <= '0';
	sram_bw2 <= '0';
	sram_bw3 <= '0';


	ser_ncts <= '0';
	
              
	pll_inst: xc5pll
	port map (
		clkin1_in	 => clk,
		clkout0_out	 => clk_int,
		locked_out	=> open
	);
--	clk_int <= clk;

--
--	intern reset
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
--	A LED blinking at 0.5 Hz to show that the board is clocked.
--
process(clk_int, int_res)
begin
	if int_res='1' then
		toggle <= '0';
		cnt <= clk_freq;
	elsif rising_edge(clk_int) then
		cnt <= cnt-1;
		if cnt=0 then
			toggle <= not toggle;
			cnt <= clk_freq;
		end if;
	end if;
end process;

	led <= toggle;
--
--	components of jop
--

	wd <= wd_out;

	cpu: entity work.jopcpu
		generic map(
			jpc_width => jpc_width,
			block_bits => block_bits,
			spm_width => spm_width
		)
		port map(clk_int, int_res,
			sc_mem_out, sc_mem_in,
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req);

	io: entity work.scio 
		port map (clk_int, int_res,
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req,

			txd => ser_txd,
			rxd => ser_rxd,
			ncts => ser_ncts,
			nrts => ser_nrts,
			wd => wd_out,
			l => open,
			r => open,
			t => open,
			b => open
		);

	scm: entity work.sc_mem_if
		generic map (
			ram_ws => ram_cnt-1,
			addr_bits => 18
		)
		port map (clk_int, int_res,
			sc_mem_out, sc_mem_in,

			ram_addr => ram_addr,
			ram_dout => ram_dout,
			ram_din => sram_data,
			ram_dout_en	=> ram_dout_en,
			ram_ncs => open,
			ram_noe => sram_oe_n,
			ram_nwe => sram_we_n
		);

	process(ram_dout_en, ram_dout)
	begin
		if ram_dout_en='1' then
			sram_data <= ram_dout;
		else
			sram_data <= (others => 'Z');
		end if;
	end process;



end rtl;
