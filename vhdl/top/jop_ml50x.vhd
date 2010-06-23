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

---=========================================================---
---=========================================================---

--
--	I/O pins of board TODO: change this and io for xilinx board!
--
--	io_b	: inout std_logic_vector(10 downto 1);
--	io_l	: inout std_logic_vector(20 downto 1);
--	io_r	: inout std_logic_vector(20 downto 1);
--	io_t	: inout std_logic_vector(6 downto 1)
);
end jop;

architecture rtl of jop is
-- use signals to make compiler happy when I/O ports are missing
--	signal sram_clk : std_logic;
--	signal sram_addr : std_logic_vector(21 downto 0);
--	
--	signal sram_we_n : std_logic;
--	signal sram_oe_n : std_logic;
--
--	signal sram_data : std_logic_vector(31 downto 0);
--	
--	signal sram_bw0: std_logic;
--	signal sram_bw1 : std_logic;
--	
--	signal sram_bw2 : std_Logic;
--	signal sram_bw3 : std_logic;
--	
--	signal sram_adv_ld_n : std_logic;
--	signal sram_mode : std_logic;
--	signal sram_cen : std_logic;
--	signal sram_cen_test : std_logic;
--	signal sram_zz : std_logic;


--=======================================================================
--Create alias for simple naming convention for Virtex-5 SRAM============
--======================================================================
alias virtex_ram_addr : std_logic_vector(21 downto 0) is sram_addr;
alias	ram_nwe		: std_logic is sram_we_n;
alias	ram_noe		: std_logic is sram_oe_n;
alias	rama_d		: std_logic_vector(15 downto 0) is sram_data(15 downto 0);
alias	rama_nlb	: std_logic is sram_bw0;
alias	rama_nub	: std_logic is sram_bw1;
alias	ramb_d		: std_logic_vector(15 downto 0) is sram_data(31 downto 16);
alias	ramb_nlb	: std_logic is sram_bw2;
alias	ramb_nub	: std_logic is sram_bw3;
signal rama_ncs : std_logic;
signal ramb_ncs : std_logic;
--=========================================================================


---------original JOP ram address port used to----------------
----generate 23 bit address width for Virtex-4 SRAM-----------
signal ram_addr 		: std_logic_vector(21 downto 0);
--------------------------------------------------------------
--------------------------------------------------------------

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

	signal ram_dout			: std_logic_vector(31 downto 0);
	signal ram_din			: std_logic_vector(31 downto 0);
	signal ram_dout_en		: std_logic;
	signal ram_ncs			: std_logic;

-- not available at this board:
	signal ser_ncts			: std_logic;
	signal ser_nrts			: std_logic;

	signal cnt				: integer;
	signal toggle			: std_logic;

begin

--================================================--
--============VIRTEX 4 SRAM SIGNALS===============--

sram_adv_ld_n <= '0';
sram_mode <= '0';
sram_cen <= '0';
virtex_ram_addr <= ram_addr;
sram_zz <= '0';
sram_clk <= not clk_int;
--================================================--
--================================================-- 


	ser_ncts <= '0';
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
	clk_int <= clk;

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
			addr_bits => 22
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
			rama_d <= ram_dout(15 downto 0);
			ramb_d <= ram_dout(31 downto 16);
		else
			rama_d <= (others => 'Z');
			ramb_d <= (others => 'Z');
		end if;
	end process;

	ram_din <= ramb_d & rama_d;

--
--	To put this RAM address in an output register
--	we have to make an assignment (FAST_OUTPUT_REGISTER)
--
	rama_ncs <= ram_ncs;
	rama_nlb <= '0';
	rama_nub <= '0';

	ramb_ncs <= ram_ncs;
	ramb_nlb <= '0';
	ramb_nub <= '0';

end rtl;
