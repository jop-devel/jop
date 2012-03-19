--
--	scio_DE2-70_reprap.vhd
--
--
--	Author: Tórur Biskopstø Strøm
--


Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;
use work.wb_pack.all;

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

	txd			: out std_logic;
	rxd			: in std_logic;
	ncts		: in std_logic;
	nrts		: out std_logic;
	
--
--	LEDs
--
	oLEDR		: out std_logic_vector(17 downto 0);
--	oLEDG		: out std_logic_vector(7 downto 0);
	
--
--	Switches
--
	iSW			: in std_logic_vector(17 downto 0);
	
--
-- Expansion header
--
	GPIO_0	: inout std_logic_vector(31 downto 0);
	
-- watch dog

	wd			: out std_logic;
-- add led and switch pins
-- core i/o pins
	l			: inout std_logic_vector(20 downto 1);
	r			: inout std_logic_vector(20 downto 1);
	t			: inout std_logic_vector(6 downto 1);
	b			: inout std_logic_vector(10 downto 1)

    
-- remove the comment for RAM access counting
-- ram_cnt 	: in std_logic
 );
end scio;


architecture rtl of scio is

	constant SLAVE_CNT : integer := 6;
	-- SLAVE_CNT <= 2**DECODE_BITS
	-- take care of USB address 0x20!
	constant DECODE_BITS : integer := 3;
	-- number of bits that can be used inside the slave
	constant SLAVE_ADDR_BITS : integer := 4;

	type slave_bit is array(0 to SLAVE_CNT-1) of std_logic;
	signal sc_rd, sc_wr		: slave_bit;

	type slave_dout is array(0 to SLAVE_CNT-1) of std_logic_vector(31 downto 0);
	signal sc_dout			: slave_dout;

	type slave_rdy_cnt is array(0 to SLAVE_CNT-1) of unsigned(1 downto 0);
	signal sc_rdy_cnt		: slave_rdy_cnt;

	signal sel, sel_reg		: integer range 0 to 2**DECODE_BITS-1;
	
	-- The integer value should match the constant value set in Const.java file, fx.
	-- if LEDSW_SLAVE = 3, then Const.LS_BASE = IO_BASE+0x30. The USB address is set
	-- equal to 0x20.
	constant SYS_SLAVE 		: integer := 0;
	constant UART_SLAVE 	: integer := 1;
	constant LEDSW_SLAVE 	: integer := 4;
	constant EXPH_SLAVE 	: integer := 3;
	constant IIC			: integer := 5;
	
	-- remove the comment for RAM access counting 
	-- signal ram_count : std_logic;
	
	signal wb_in : wb_master_in_type;
	signal wb_out : wb_master_out_type;
	
	signal scl_pad_i,scl_pad_o,scl_padoen_o,sda_pad_i,sda_pad_o,sda_padoen_o  : std_logic;   

begin

--
--	unused and input pins tri state
--
	l <= (others => 'Z');
	r <= (others => 'Z');
	t <= (others => 'Z');
	b <= (others => 'Z');

	assert SLAVE_CNT <= 2**DECODE_BITS report "Wrong constant in scio";

	sel <= to_integer(unsigned(sc_io_out.address(SLAVE_ADDR_BITS+DECODE_BITS-1 downto SLAVE_ADDR_BITS)));

	-- What happens when sel_reg > SLAVE_CNT-1??
	sc_io_in.rd_data <= sc_dout(sel_reg);
	sc_io_in.rdy_cnt <= sc_rdy_cnt(sel_reg);

	-- default for unused USB device
	sc_dout(2) <= (others => '0');
	sc_rdy_cnt(2) <= (others => '0');

	--
	-- Connect SLAVE_CNT simple test slaves
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
			rd => sc_rd(SYS_SLAVE),
			wr => sc_wr(SYS_SLAVE),
			rd_data => sc_dout(SYS_SLAVE),
			rdy_cnt => sc_rdy_cnt(SYS_SLAVE),

			irq_in => irq_in,
			irq_out => irq_out,
			exc_req => exc_req,
			
			sync_out => sync_out,
			sync_in => sync_in,
			
			wd => wd
			-- remove the comment for RAM access counting
			-- ram_count => ram_count
		);
		
	-- remove the comment for RAM access counting
	-- ram_count <= ram_cnt;

	ua: entity work.sc_uart generic map (
			addr_bits => SLAVE_ADDR_BITS,
			clk_freq => clk_freq,
			baud_rate => 115200,
			txf_depth => 32,
			txf_thres => 1,
			rxf_depth => 32,
			rxf_thres => 1
		)
		port map(
			clk => clk,
			reset => reset,

			address => sc_io_out.address(SLAVE_ADDR_BITS-1 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(UART_SLAVE),
			wr => sc_wr(UART_SLAVE),
			rd_data => sc_dout(UART_SLAVE),
			rdy_cnt => sc_rdy_cnt(UART_SLAVE),

			txd	 => txd,
			rxd	 => rxd,
			ncts => '0',
			nrts => nrts
	);
	
	lw : entity work.led_switch
	port map
	(
		clk => clk,
		reset => reset,
		
		sc_rd => sc_rd(LEDSW_SLAVE),
		sc_rd_data => sc_dout(LEDSW_SLAVE),
		sc_wr => sc_wr(LEDSW_SLAVE),
		sc_wr_data => sc_io_out.wr_data,
		sc_rdy_cnt => sc_rdy_cnt(LEDSW_SLAVE),
		
		oLEDR => oLEDR,
		iSW => iSW
	);
	
	eh : entity work.expansionheader
	port map
	(
		clk => clk,
		reset => reset,
		
		sc_rd => sc_rd(EXPH_SLAVE),
		sc_rd_data => sc_dout(EXPH_SLAVE),
		sc_wr => sc_wr(EXPH_SLAVE),
		sc_wr_data => sc_io_out.wr_data,
		sc_rdy_cnt => sc_rdy_cnt(EXPH_SLAVE),
		GPIO_0 => GPIO_0
	);
	
	
	wb: entity work.sc2wb generic map (
			addr_bits => 3
		)
		port map(
			clk => clk,
			reset => reset,

			address => sc_io_out.address(2 downto 0),
			wr_data => sc_io_out.wr_data,
			rd => sc_rd(IIC),
			wr => sc_wr(IIC),
			rd_data => sc_dout(IIC),
			rdy_cnt => sc_rdy_cnt(IIC),

			wb_out => wb_out,
			wb_in => wb_in
	);
	
	i2c: entity work.i2c_master_top 
		port map(
			wb_clk_i => clk,               -- master clock input
			wb_rst_i => reset,                 -- synchronous active high reset
			wb_adr_i => wb_out.adr_o(2 downto 0), -- lower address bits
			wb_dat_i => wb_out.dat_o(7 downto 0), -- Databus input
			wb_dat_o => wb_in.dat_i(7 downto 0), -- Databus output
			wb_we_i => wb_out.we_o, -- Write enable input
			wb_stb_i => wb_out.stb_o, -- Strobe signals / core select signal
			wb_cyc_i => wb_out.cyc_o, -- Valid bus cycle input
			wb_ack_o => wb_in.ack_i, -- Bus cycle acknowledge output
			wb_inta_o => open, -- interrupt request output signal

			-- i2c lines
			scl_pad_i => scl_pad_i,
			scl_pad_o => scl_pad_o,
			scl_padoen_o => scl_padoen_o,
			sda_pad_i => sda_pad_i,
			sda_pad_o => sda_pad_o,
			sda_padoen_o => sda_padoen_o
	);
	
	--All devices connected to SDA and SCL must have open drain or open collector outputs.
	GPIO_0(29) <= scl_pad_o when (scl_padoen_o = '0') else 'Z'; 
	GPIO_0(27) <= sda_pad_o when (sda_padoen_o = '0') else 'Z'; 
	scl_pad_i <= GPIO_0(29); 
	sda_pad_i <= GPIO_0(27);
	
end rtl;
