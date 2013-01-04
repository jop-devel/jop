Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.i2c_pkg.all;
use work.fifo_pkg.all;

entity sc_i2c is
	generic(addr_bits : integer := 4);

	port(
		clk        : in    std_logic;
		reset      : in    std_logic;

		--	SimpCon IO interface
		address    : in    std_logic_vector(addr_bits - 1 downto 0);
		sc_rd      : in    std_logic;
		sc_rd_data : out   std_logic_vector(31 downto 0);

		sc_wr      : in    std_logic;
		sc_wr_data : in    std_logic_vector(31 downto 0);

		sc_rdy_cnt : out   unsigned(1 downto 0);

		-- Interrupt request
		irq        : out   std_logic;

		-- I2C interface
		sda        : inout std_logic;
		scl        : inout std_logic
	);

end sc_i2c;

architecture sc_i2c_rtl of sc_i2c is

	--TODO Replace fixed number of bits in address length
	constant CONTROL    : std_logic_vector(3 downto 0) := "0000";
	constant STATUS     : std_logic_vector(3 downto 0) := "0001";
	constant DEVADD     : std_logic_vector(3 downto 0) := "0010";
	constant MESSAGE    : std_logic_vector(3 downto 0) := "0011";
	constant DATA_WRITE : std_logic_vector(3 downto 0) := "0100";
	constant DATA_READ  : std_logic_vector(3 downto 0) := "0101";
	constant TIME_CON_H : std_logic_vector(3 downto 0) := "0110";
	constant TIME_CON_L : std_logic_vector(3 downto 0) := "0111";
	constant TX_OCCUP   : std_logic_vector(3 downto 0) := "1000";
	constant RX_OCCUP   : std_logic_vector(3 downto 0) := "1001";

	constant CONTROL_SIZE : integer := 8;
	constant STATUS_SIZE  : integer := 7;

	signal control_reg : std_logic_vector(CONTROL_SIZE - 1 downto 0);
	signal control_t   : control_type;

	signal status_reg : std_logic_vector(STATUS_SIZE - 1 downto 0);
	signal status_t   : status_type;

	signal dev_address_reg : std_logic_vector(I2C_DEV_ADDR_SIZE - 1 downto 0);
	signal msg_size_reg    : std_logic_vector(I2C_MSG_SIZE - 1 downto 0);

	signal tx_occu_reg  : std_logic_vector(31 downto 0);
	alias tx_occ_rd_reg : std_logic_vector(15 downto 0) is tx_occu_reg(31 downto 16);
	alias tx_occ_wr_reg : std_logic_vector(15 downto 0) is tx_occu_reg(15 downto 0);

	signal rx_occu_reg  : std_logic_vector(31 downto 0);
	alias rx_occ_rd_reg : std_logic_vector(15 downto 0) is rx_occu_reg(31 downto 16);
	alias rx_occ_wr_reg : std_logic_vector(15 downto 0) is rx_occu_reg(15 downto 0);

	signal th_reg : std_logic_vector(31 downto 0);
	signal tl_reg : std_logic_vector(31 downto 0);

	alias t_hold_start_reg : std_logic_vector(7 downto 0) is th_reg(31 downto 24);
	alias t_rstart_reg     : std_logic_vector(7 downto 0) is th_reg(23 downto 16);
	alias t_low_reg        : std_logic_vector(7 downto 0) is th_reg(15 downto 8);
	alias t_high_reg       : std_logic_vector(7 downto 0) is th_reg(7 downto 0);
	alias t_half_high_reg  : std_logic_vector(7 downto 0) is tl_reg(31 downto 24);
	alias t_susto_reg      : std_logic_vector(7 downto 0) is tl_reg(23 downto 16);
	alias t_wait_reg       : std_logic_vector(7 downto 0) is tl_reg(15 downto 8);
	signal timing_t        : timing_type;

	signal rx_fifo_rden   : std_logic;
	signal tx_fifo_wren   : std_logic;
	signal sc_rd_data_int : std_logic_vector(I2C_DATA_SIZE - 1 downto 0);
	signal busy_read      : std_logic;
	signal sc_rd_reg      : std_logic;

begin
	control_t.TX_FLUSH <= control_reg(7);
	control_t.RX_FLUSH <= control_reg(6);
	control_t.ACK      <= control_reg(5);
	control_t.STRT     <= control_reg(4);
	control_t.STOP     <= control_reg(3);
	control_t.MASL     <= control_reg(2);
	control_t.RSTA     <= control_reg(1);
	control_t.ENABLE   <= control_reg(0);

	status_reg(6) <= status_t.TBUF_ERR;
	status_reg(5) <= status_t.RBUF_ERR;
	status_reg(4) <= status_t.ACK_ERR;
	status_reg(3) <= status_t.HDR_ERR;
	status_reg(2) <= status_t.BUSY;
	status_reg(1) <= status_t.STOP;
	status_reg(0) <= status_t.DATA_RDY;

	timing_t.T_HOLD_START <= t_hold_start_reg;
	timing_t.T_RSTART     <= t_rstart_reg;
	timing_t.T_LOW        <= t_low_reg;
	timing_t.T_HIGH       <= t_high_reg;
	timing_t.T_HALF_HIGH  <= t_half_high_reg;
	timing_t.T_SUSTO      <= t_susto_reg;
	timing_t.T_WAIT       <= t_wait_reg;

	I2C : entity work.i2c
		generic map(TBUFF_ADD => 5,
			        RBUFF_ADD => 5)
		port map(clk                  => clk,
			     reset                => reset,
			     sda                  => sda,
			     scl                  => scl,
			     control              => control_t,
			     status               => status_t,
			     address              => dev_address_reg(6 downto 0),
			     msg_size             => msg_size_reg(15 downto 0),
			     tx_fifo_occupancy_rd => tx_occ_rd_reg,
			     tx_fifo_occupancy_wr => tx_occ_wr_reg,
			     rx_fifo_occupancy_rd => rx_occ_rd_reg,
			     rx_fifo_occupancy_wr => rx_occ_wr_reg,
			     t_const              => timing_t,
			     irq                  => irq,
			     read_enable          => rx_fifo_rden,
			     read_data_out        => sc_rd_data_int,
			     write_enable         => tx_fifo_wren,
			     write_data_in        => sc_wr_data(I2C_DATA_SIZE - 1 downto 0)
		);

	sc_rdy_cnt   <= "01" when busy_read = '1' else "00";
	tx_fifo_wren <= '1' when (sc_wr = '1') and (address(3 downto 0) = DATA_WRITE) else '0';
	rx_fifo_rden <= '1' when (sc_rd = '1') and (address(3 downto 0) = DATA_READ) else '0';

	process(clk, reset)
	begin
		if reset = '1' then
			sc_rd_data      <= (others => '0');
			control_reg     <= (others => '0');
			dev_address_reg <= (others => '0');
			th_reg          <= (others => '0');
			tl_reg          <= (others => '0');

			sc_rd_reg <= '0';
			busy_read <= '0';

		elsif rising_edge(clk) then

			-- read
			if sc_rd = '1' then
				sc_rd_reg <= '0';
				busy_read <= '0';

				case address(3 downto 0) is
					when CONTROL =>
						sc_rd_data(CONTROL_SIZE - 1 downto 0) <= control_reg;
						sc_rd_data(31 downto CONTROL_SIZE)    <= (others => '0');

					when STATUS =>
						sc_rd_data(STATUS_SIZE - 1 downto 0) <= status_reg;
						sc_rd_data(31 downto STATUS_SIZE)    <= (others => '0');

					when DEVADD =>
						sc_rd_data(I2C_DEV_ADDR_SIZE - 1 downto 0) <= dev_address_reg;
						sc_rd_data(31 downto I2C_DEV_ADDR_SIZE)    <= (others => '0');

					when MESSAGE =>
						sc_rd_data(I2C_MSG_SIZE - 1 downto 0) <= msg_size_reg;
						sc_rd_data(31 downto I2C_MSG_SIZE)    <= (others => '0');

					when TIME_CON_H =>
						sc_rd_data <= th_reg;

					when TIME_CON_L =>
						sc_rd_data <= tl_reg;

					when TX_OCCUP =>
						sc_rd_data <= tx_occu_reg;

					when RX_OCCUP =>
						sc_rd_data <= rx_occu_reg;

					when DATA_READ =>
						busy_read <= '1';
						sc_rd_reg <= '1';

					when others =>
						sc_rd_reg  <= '0';
						busy_read  <= '0';
						sc_rd_data <= (others => '0');

				end case;

			elsif sc_rd_reg = '1' then
				busy_read <= '0';
				sc_rd_reg <= '0';

				sc_rd_data(31 downto I2C_DATA_SIZE)    <= (others => '0');
				sc_rd_data(I2C_DATA_SIZE - 1 downto 0) <= sc_rd_data_int;

			elsif sc_wr = '1' then
				busy_read <= '0';
				sc_rd_reg <= '0';

				case address(3 downto 0) is
					when CONTROL =>
						control_reg <= sc_wr_data(CONTROL_SIZE - 1 downto 0);

					when DEVADD =>
						dev_address_reg <= sc_wr_data(I2C_DEV_ADDR_SIZE - 1 downto 0);

					when MESSAGE =>
						msg_size_reg <= sc_wr_data(I2C_MSG_SIZE - 1 downto 0);

					when TIME_CON_H =>
						th_reg <= sc_wr_data;

					when TIME_CON_L =>
						tl_reg <= sc_wr_data;

					when others =>
						null;

				end case;

			end if;

		end if;

	end process;

end sc_i2c_rtl;
