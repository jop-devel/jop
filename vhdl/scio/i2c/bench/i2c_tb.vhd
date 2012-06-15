library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

library work;
use work.i2c_pkg.all;

entity i2c_tb is
end entity i2c_tb;


architecture sim of i2c_tb is

  component I2C_EEPROM
    generic (device : string (1 to 5) := "24C16");
    port (STRETCH : in    time      := 1 ns;
          E0      : in    std_logic := 'L';
          E1      : in    std_logic := 'L';
          E2      : in    std_logic := 'L';
          WC      : in    std_logic := 'L';
          SCL     : inout std_logic;
          SDA     : inout std_logic);
  end component I2C_EEPROM;

  component i2c
    port(clk             : in    std_logic;
         reset           : in    std_logic;
         flush_fifo      : in    std_logic;
         sda             : inout std_logic;
         scl             : inout std_logic;
         device_addr     : in    std_logic_vector(6 downto 0);
         masl            : in    std_logic;
         strt            : in    std_logic;
         txrx            : in    std_logic;
         message_size    : in    std_logic_vector(3 downto 0);
         rep_start       : in    std_logic;
         reset_rep_start : out   std_logic;
         busy            : out   std_logic;
         tr_progress     : out   std_logic;
         transaction     : out   std_logic;
         slave_addressed : out   std_logic;
         data_valid      : out   std_logic;
         t_const         : in    timming;
         tx_fifo_wr_ena  : in    std_logic;
         tx_fifo_full    : out   std_logic;
         tx_fifo_empty   : out   std_logic;
         data_in         : in    std_logic_vector(7 downto 0);
         tx_fifo_occ_in  : out   std_logic_vector(3 downto 0);
         tx_fifo_occ_out : out   std_logic_vector(3 downto 0);
         rx_fifo_rd_ena  : in    std_logic;
         rx_fifo_empty   : out   std_logic;
         rx_fifo_full    : out   std_logic;
         data_out        : out   std_logic_vector(7 downto 0);
         rx_fifo_occ_in  : out   std_logic_vector(3 downto 0);
         rx_fifo_occ_out : out   std_logic_vector(3 downto 0));
  end component i2c;

  component clk_gen
    generic (period : time;
             phase  : time);
    port (clk_o : out std_logic);
  end component clk_gen;

  component rst_gen
    generic (delay :     time);
    port (rst_o    : out std_logic);
  end component rst_gen;

  signal clk_m             : std_logic;
  signal reset_m           : std_logic;
  signal sda_int           : std_logic;
  signal scl_int           : std_logic;
  signal device_addr_m     : std_logic_vector (6 downto 0);
  signal masl_m            : std_logic;
  signal strt_m            : std_logic;
  signal txrx_m            : std_logic;
  signal message_size_m    : std_logic_vector (3 downto 0);
  signal busy_m            : std_logic;
  signal tr_progress_m     : std_logic;
  signal t_const_m         : timming;
  signal tx_fifo_wr_ena_m  : std_logic;
  signal tx_fifo_full_m    : std_logic;
  signal data_in_m         : std_logic_vector (7 downto 0);
  signal tx_fifo_occ_in_m  : std_logic_vector (3 downto 0);
  signal tx_fifo_occ_out_m : std_logic_vector (3 downto 0);
  signal rx_fifo_rd_ena_m  : std_logic;
  signal rx_fifo_empty_m   : std_logic;
  signal data_out_m        : std_logic_vector (7 downto 0);
  signal rx_fifo_occ_in_m  : std_logic_vector (3 downto 0);
  signal rx_fifo_occ_out_m : std_logic_vector (3 downto 0);



  type   arr is array(0 to 15) of std_logic_vector(7 downto 0);
  signal data_in : arr;

  constant WR          : std_logic                    := '0';
  constant RD          : std_logic                    := '1';
  constant DEST_ADDR   : std_logic_vector(6 downto 0) := "1010000";
  constant DEST_ADDR_2 : std_logic_vector(6 downto 0) := "1110001";

  signal clk_s             : std_logic;
  signal reset_s           : std_logic;
  signal device_addr_s     : std_logic_vector (6 downto 0);
  signal masl_s            : std_logic;
  signal txrx_s            : std_logic;
  signal strt_s            : std_logic;
  signal message_size_s    : std_logic_vector (3 downto 0);
  signal busy_s            : std_logic;
  signal t_const_s         : timming;
  signal tx_fifo_wr_ena_s  : std_logic;
  signal tx_fifo_full_s    : std_logic;
  signal data_in_s         : std_logic_vector (7 downto 0);
  signal tx_fifo_occ_out_s : std_logic_vector (3 downto 0);
  signal tx_fifo_occ_in_s  : std_logic_vector (3 downto 0);
  signal rx_fifo_rd_ena_s  : std_logic := '0';
  signal rx_fifo_empty_s   : std_logic;
  signal data_out_s        : std_logic_vector (7 downto 0);
  signal rx_fifo_occ_out_s : std_logic_vector (3 downto 0);
  signal rx_fifo_occ_in_s  : std_logic_vector (3 downto 0);



  signal rep_start_m                      : std_logic;
  signal rep_start_s                      : std_logic;
  signal reset_rep_start_s                : std_logic;
  signal reset_rep_start_m                : std_logic;
  signal tr_progress_s                    : std_logic;
  signal transaction_s, transaction_m     : std_logic;
  signal tx_fifo_empty_s, tx_fifo_empty_m : std_logic;
  signal rx_fifo_full_s, rx_fifo_full_m   : std_logic;
  signal flush_fifo_s, flush_fifo_m       : std_logic;

  signal slave_addressed_m, slave_addressed_s : std_logic;
  signal data_valid_m, data_valid_s           : std_logic;
  
  
begin

  scl_int <= 'H';
  sda_int <= 'H';

  CLK_MA : component clk_gen
    generic map (
      period => 1 us,
      phase  => 10 ns
      )
    port map (
      clk_o => clk_m
      );

  RESET_MA : component rst_gen
    generic map (
      delay => 10 us
      )
    port map (
      rst_o => reset_m
      );

  I2C_MA : component i2c
    port map (
      clk             => clk_m,
      reset           => reset_m,
      flush_fifo      => flush_fifo_m,
      sda             => sda_int,
      scl             => scl_int,
      device_addr     => device_addr_m,
      masl            => masl_m,
      strt            => strt_m,
      txrx            => txrx_m,
      message_size    => message_size_m,
      rep_start       => rep_start_m,
      reset_rep_start => reset_rep_start_m,
      busy            => busy_m,
      tr_progress     => tr_progress_m,
      transaction     => transaction_m,
      slave_addressed => slave_addressed_m,
      data_valid      => data_valid_m,
      t_const         => t_const_m,
      tx_fifo_wr_ena  => tx_fifo_wr_ena_m,
      tx_fifo_full    => tx_fifo_full_m,
      tx_fifo_empty   => tx_fifo_empty_m,
      data_in         => data_in_m,
      tx_fifo_occ_in  => tx_fifo_occ_in_m,
      tx_fifo_occ_out => tx_fifo_occ_out_m,
      rx_fifo_rd_ena  => rx_fifo_rd_ena_m,
      rx_fifo_empty   => rx_fifo_empty_m,
      rx_fifo_full    => rx_fifo_full_m,
      data_out        => data_out_m,
      rx_fifo_occ_in  => rx_fifo_occ_in_m,
      rx_fifo_occ_out => rx_fifo_occ_out_m
      );

  CLK_SL : component clk_gen
    generic map (
      period => 2 us,
      phase  => 25 ns
      )
    port map (
      clk_o => clk_s
      );

  RESET_SL : component rst_gen
    generic map (
      delay => 15 us
      )
    port map (
      rst_o => reset_s
      );

  I2C_SL : component i2c
    port map (
      clk             => clk_s,
      reset           => reset_s,
      flush_fifo      => flush_fifo_s,
      sda             => sda_int,
      scl             => scl_int,
      device_addr     => device_addr_s,
      masl            => masl_s,
      strt            => strt_s,
      txrx            => txrx_s,
      message_size    => message_size_s,
      rep_start       => rep_start_s,
      reset_rep_start => reset_rep_start_s,
      busy            => busy_s,
      tr_progress     => tr_progress_s,
      transaction     => transaction_s,
      slave_addressed => slave_addressed_s,
      data_valid      => data_valid_s,
      t_const         => t_const_s,
      tx_fifo_wr_ena  => tx_fifo_wr_ena_s,
      tx_fifo_full    => tx_fifo_full_s,
      tx_fifo_empty   => tx_fifo_empty_s,
      data_in         => data_in_s,
      tx_fifo_occ_in  => tx_fifo_occ_in_s,
      tx_fifo_occ_out => tx_fifo_occ_out_s,
      rx_fifo_rd_ena  => rx_fifo_rd_ena_s,
      rx_fifo_empty   => rx_fifo_empty_s,
      rx_fifo_full    => rx_fifo_full_s,
      data_out        => data_out_s,
      rx_fifo_occ_in  => rx_fifo_occ_in_s,
      rx_fifo_occ_out => rx_fifo_occ_out_s
      );

  EEPROM : component I2C_EEPROM
    generic map (
      device => "24C01"
      )
    port map (
      STRETCH => 0 us,
      E0      => 'L',
      E1      => 'L',
      E2      => 'L',
      WC      => 'L',
      SCL     => scl_int,
      SDA     => sda_int
      );

  process

  begin

    wait until reset_m = '1';
    wait until reset_m = '0';

    -- Master initial configuration
    masl_m         <= '1';
    txrx_m         <= WR;
    device_addr_m  <= "1100110";
    message_size_m <= "0011";
    rep_start_m    <= '0';

    t_const_m.T_HIGH     <= std_logic_vector(to_unsigned(4, t_const_m.T_HIGH'length));
    t_const_m.T_LOW      <= std_logic_vector(to_unsigned(5, t_const_m.T_LOW'length));
    t_const_m.HOLD_START <= std_logic_vector(to_unsigned(5, t_const_m.HOLD_START'length));
    t_const_m.DELAY_STOP <= std_logic_vector(to_unsigned(2, t_const_m.DELAY_STOP'length));
    t_const_m.T_SUSTO    <= std_logic_vector(to_unsigned(4, t_const_m.T_SUSTO'length));
    t_const_m.T_WAIT     <= std_logic_vector(to_unsigned(8, t_const_m.T_WAIT'length));

    -- Slave initial configuration
    masl_s         <= '0';
    txrx_s         <= WR;               -- Don't care
    device_addr_s  <= "1110001";
    message_size_s <= "0000";           -- Don't care
    rep_start_s    <= '0';

    t_const_m.T_HIGH     <= std_logic_vector(to_unsigned(4, t_const_m.T_HIGH'length));
    t_const_m.T_LOW      <= std_logic_vector(to_unsigned(5, t_const_m.T_LOW'length));
    t_const_m.HOLD_START <= std_logic_vector(to_unsigned(5, t_const_m.HOLD_START'length));
    t_const_m.DELAY_STOP <= std_logic_vector(to_unsigned(2, t_const_m.DELAY_STOP'length));
    t_const_m.T_SUSTO    <= std_logic_vector(to_unsigned(4, t_const_m.T_SUSTO'length));
    t_const_m.T_WAIT     <= std_logic_vector(to_unsigned(8, t_const_m.T_WAIT'length));

    --wait for 5 us;
    wait until reset_s = '0';
    wait for 5 us;

    data_in(0)  <= DEST_ADDR_2 & WR;
    data_in(1)  <= "00000000";          --0
    data_in(2)  <= "10000000";          --0
    data_in(3)  <= "10000001";          --1
    data_in(4)  <= "10000010";          --2
    data_in(5)  <= "10000011";          --3
    data_in(6)  <= "10000100";          --4
    data_in(7)  <= DEST_ADDR & WR;
    data_in(8)  <= "00000000";          --0
    data_in(9)  <= DEST_ADDR & RD;
    data_in(10) <= DEST_ADDR & WR;
    data_in(11) <= "01000000";
    data_in(12) <= "01000001";
    data_in(13) <= "01000010";
    data_in(14) <= "01000011";
    data_in(15) <= "01000100";

--      for i in 1 to 15 loop
--              data_in(i) <= std_logic_vector(to_unsigned(i, data_in(i)'length));
--      end loop;

--              data_in(1) <= "1010000" & RD;



    -- Byte write
    for i in 0 to 15 loop
      wait until clk_m = '1';
      tx_fifo_wr_ena_m <= '1';
      data_in_m        <= data_in(i);
      wait until clk_m = '0';
    end loop;


    wait until clk_m = '1';
    tx_fifo_wr_ena_m <= '0';
    strt_m           <= '1';

    wait until busy_m = '1';
    strt_m <= '0';

    -- Wait untill we have tranmitted a certain number of bytes and then
    -- indicate a repeated start
    wait until tx_fifo_occ_in_m = "1000" and tr_progress_m = '1';
    rep_start_m <= '1';
    txrx_m      <= WR;

    wait until reset_rep_start_m = '1';
    rep_start_m <= '0';

    wait until tx_fifo_occ_in_m = "0110" and tr_progress_m = '1';
    rep_start_m <= '1';
    txrx_m      <= RD;

    wait until reset_rep_start_m = '1';
    rep_start_m <= '0';

    wait until data_valid_s = '1';
    wait until clk_m = '1';
    rx_fifo_rd_ena_s <= '1';
    wait until clk_m = '0';
    wait until clk_m = '1';
    rx_fifo_rd_ena_s <= '0';

    wait until rx_fifo_occ_in_m = "0011";
    rep_start_m <= '1';
    txrx_m      <= WR;

    wait until reset_rep_start_m = '1';
    rep_start_m <= '0';

--      wait until tx_fifo_occ_in_m = "0000";
--
--      for i in 0 to 6 loop
--      wait until clk_m = '1';
--      tx_fifo_wr_ena_m <= '1';
--      data_in_m <= data_in(i);
--      wait until clk_m = '0';
--      end loop;
--
--      rep_start_m <= '1';
--
--      wait until reset_rep_start_m = '0';
--      rep_start_m <= '0';


    wait for 600 us;

    -- Write to slave tx buffer
    wait until clk_s = '1';
    tx_fifo_wr_ena_s <= '1';
    data_in_s        <= data_in(2);
    wait until clk_s = '0';
    wait until clk_s = '1';
    tx_fifo_wr_ena_s <= '0';
    wait until clk_s = '0';

    wait for 600 us;


  end process;
  
  
  
  
end architecture sim;
