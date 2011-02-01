--
--	jop_ml401a.vhd
--
--	top level for ML401 Virtex-4 with co-processor extensions
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
use work.vlabifhw_pack.all;


entity jop is

generic (
    debugger    : boolean := true;
	ram_cnt		: integer := 4;		-- clock cycles for external ram
	rom_cnt		: integer := 15;	-- not used for S3K
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

-- second serial interface for debug chain
	debug_ser_txd   : out std_logic;
	debug_ser_rxd	: in std_logic;

--
--	watchdog
--
	wd		: out std_logic;
--
---==========================================================--
----===========Virtex-4 SRAM Port============================--
	sram_clk : out std_logic;
	sram_feedback_clk : out std_logic;
	
	sram_addr : out std_logic_vector(22 downto 0);
	
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
--=======================================================================
--Create alias for simple naming convention for Virtex-4 SRAM============
--======================================================================
alias virtex_ram_addr : std_logic_vector(22 downto 0) is sram_addr;
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
signal ram_addr 		: std_logic_vector(17 downto 0);
--------------------------------------------------------------
--------------------------------------------------------------

--
--	Signals
--
	signal clk_int, clk2    : std_logic;

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


    signal cc_out_data      : std_logic_vector(31 downto 0);
    signal cc_out_wr        : std_logic;
    signal cc_out_rdy       : std_logic;
    signal cc_in_data       : std_logic_vector(31 downto 0);
    signal cc_in_wr         : std_logic;
    signal cc_in_rdy        : std_logic;

    signal int_res2         : std_logic;
    signal break_command    : std_logic_vector(2 downto 0);
    signal breakpoint       : std_logic;
    signal dc_out           : std_logic;
    signal dc_in            : std_logic;
    signal dc_control       : DC_Control_Wires;
    signal start            : std_logic;
    signal busy             : std_logic;
    signal grab             : std_logic;
    signal running          : std_logic;
    signal reading          : std_logic;
    signal terminal         : std_logic;
    signal rdyc0            : std_logic_vector(1 downto 0);
    signal rdyc1            : std_logic_vector(1 downto 0);
    signal rdyc2            : std_logic_vector(1 downto 0);

    constant master_cnt : Integer := 2;

	signal sc_arb_out		: arb_out_type(0 to master_cnt-1);
	signal sc_arb_in		: arb_in_type(0 to master_cnt-1);

    signal io_r	            : std_logic_vector(20 downto 1);
begin

--================================================--
--============VIRTEX 4 SRAM SIGNALS===============--

sram_feedback_clk <= not clk2;
sram_adv_ld_n <= '0';
sram_mode <= '0';
sram_cen <= '0';
virtex_ram_addr <= "00000" & ram_addr;
sram_zz <= '0';
sram_clk <= not clk2;
--================================================--
--================================================-- 


ndbg : if ( not debugger ) generate
	debug_ser_txd <= debug_ser_rxd;
    process(clk)
    begin
        if rising_edge(clk) then
            clk2 <= not clk2;
        end if;
    end process;

end generate ndbg;

dbg : if ( debugger ) generate
    vlhw : entity vlabifhw
        generic map (
            ext_channels => 1 ,
            fifo_depth => 1,
            clock_freq => 100e6 )
        port map (
            -- External hardware connections
            clk => clk,
            reset => '0',
            hw_tx => debug_ser_txd,
            hw_rx => debug_ser_rxd,

            -- Internal connections: these are not used in this example.
            out_channel_data => open,
            out_channel_wr => open,
            out_channel_rdy => "1",

            in_channel_data => x"00",
            in_channel_wr => "0",
            in_channel_rdy => open,

            -- Activation signal: not used
            active => open,

            -- Controls for device under test
            debug_clock => clk2,
            debug_reset => int_res2,
            breakpoint => breakpoint,

            dc_control => dc_control,
            dc_out => dc_out,
            dc_in => dc_in
        );

    -- Automatically generated component
    dc : entity Autogen_Debug_Entity
        port map (
            break_command => break_command,
            breakpoint => breakpoint,

            cc_out_data => cc_out_data,
            cc_out_wr => cc_out_wr,
            cc_out_rdy => cc_out_rdy,

            cc_in_data => cc_in_data,
            cc_in_wr => cc_in_wr,
            cc_in_rdy => cc_in_rdy,

            start => start,
            busy => busy,
            grab => grab,
            running => running,
            reading => reading,
            terminal => terminal,

            sc_arb_out_0_rd => sc_arb_out(0).rd,
            sc_arb_out_0_wr => sc_arb_out(0).wr,
            sc_arb_out_0_wr_data => sc_arb_out(0).wr_data,
            sc_arb_out_0_atomic => sc_arb_out(0).atomic,
            sc_arb_out_0_address => sc_arb_out(0).address,
            sc_arb_in_0_rd_data => sc_arb_in(0).rd_data,
            sc_arb_in_0_rdy_cnt => rdyc0,
            sc_arb_out_1_rd => sc_arb_out(1).rd,
            sc_arb_out_1_wr => sc_arb_out(1).wr,
            sc_arb_out_1_wr_data => sc_arb_out(1).wr_data,
            sc_arb_out_1_atomic => sc_arb_out(1).atomic,
            sc_arb_out_1_address => sc_arb_out(1).address,
            sc_arb_in_1_rd_data => sc_arb_in(1).rd_data,
            sc_arb_in_1_rdy_cnt => rdyc1,
            sc_mem_out_rd => sc_mem_out.rd,
            sc_mem_out_wr => sc_mem_out.wr,
            sc_mem_out_wr_data => sc_mem_out.wr_data,
            sc_mem_out_atomic => sc_mem_out.atomic,
            sc_mem_out_address => sc_mem_out.address,
            sc_mem_in_rd_data => sc_mem_in.rd_data,
            sc_mem_in_rdy_cnt => rdyc2,

            dc_control => dc_control,
            dc_in => dc_out,
            dc_out => dc_in ) ;

    rdyc0 <= std_logic_vector ( sc_arb_in(0).rdy_cnt ) ;
    rdyc1 <= std_logic_vector ( sc_arb_in(1).rdy_cnt ) ;
    rdyc2 <= std_logic_vector ( sc_mem_in.rdy_cnt ) ;

    process ( break_command,
            cc_out_wr, cc_in_wr, cc_in_rdy, start, sc_arb_out ) is
    begin
        case break_command is
        when "000" => breakpoint <= sc_arb_out(0).rd;
        when "001" => breakpoint <= sc_arb_out(0).wr;
        when "010" => breakpoint <= sc_arb_out(1).rd;
        when "011" => breakpoint <= sc_arb_out(1).wr;
        when "100" => breakpoint <= sc_arb_out(0).rd or sc_arb_out(0).wr 
                        or ( not sc_arb_in(0).rdy_cnt(0) ) ;
        when others => breakpoint <= '0';
        end case;
    end process;

end generate dbg;

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

		int_res <= int_res2 or (
            not res_cnt(0) or not res_cnt(1) or not res_cnt(2)) ;
	end if;
end process;

    cp1 : entity mac_coprocessor
        port map (
                clk => clk_int,
                reset => int_res,

                start => start,
                busy => busy,
                grab => grab,
                running => running,
                reading => reading,
                terminal => terminal,

                sc_mem_out => sc_arb_out(1), 
                sc_mem_in => sc_arb_in(1),

                cc_out_data => cc_out_data,
                cc_out_wr => cc_out_wr,
                cc_out_rdy => cc_out_rdy,

                cc_in_data => cc_in_data,
                cc_in_wr => cc_in_wr,
                cc_in_rdy => cc_in_rdy
            );

--
--	components of jop
--
	clk_int <= clk2;

	wd <= wd_out;

	cpu: entity work.jopcpu
		generic map(
			jpc_width => jpc_width,
			block_bits => block_bits,
			spm_width => spm_width
		)
		port map(clk_int, int_res,
			sc_arb_out(0), sc_arb_in(0),
			sc_io_out, sc_io_in,
			irq_in, irq_out, exc_req);

	arbiter: entity work.arbiter
		generic map(
			addr_bits => SC_ADDR_SIZE,
			cpu_cnt => master_cnt
		)
		port map(clk_int, int_res,
			sc_arb_out, sc_arb_in,
			sc_mem_out, sc_mem_in
			);

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
			r => io_r,
			t => open,
			b => open,

            cc_out_data => cc_in_data,
            cc_out_wr => cc_in_wr,
            cc_out_rdy => cc_in_rdy,
 
            cc_in_data => cc_out_data,
            cc_in_wr => cc_out_wr,
            cc_in_rdy => cc_out_rdy
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

    io_r ( 9 ) <= '1' ;
    io_r ( 10 ) <= '1' ;

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
