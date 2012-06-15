library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.i2c_pkg.all;

entity i2c is
  port(

    clk        : in std_logic;
    reset      : in std_logic;
    flush_fifo : in std_logic;

    sda : inout std_logic;
    scl : inout std_logic;

    device_addr : in std_logic_vector(6 downto 0);

    masl            : in  std_logic;
    strt            : in  std_logic;
    txrx            : in  std_logic;
    message_size    : in  std_logic_vector(3 downto 0);
    rep_start       : in  std_logic;
    reset_rep_start : out std_logic;
    enable : in std_logic;

    busy            : out std_logic;
    tr_progress     : out std_logic;
    transaction     : out std_logic;
    slave_addressed : out std_logic;
    data_valid      : out std_logic;

    t_const : in timming;

    -- FIFO signals
    tx_fifo_wr_ena  : in  std_logic;
    tx_fifo_full    : out std_logic;
    tx_fifo_empty   : out std_logic;
    data_in         : in  std_logic_vector(7 downto 0);
    tx_fifo_occ_in  : out std_logic_vector(3 downto 0);
    tx_fifo_occ_out : out std_logic_vector(3 downto 0);

    rx_fifo_rd_ena  : in  std_logic;
    rx_fifo_empty   : out std_logic;
    rx_fifo_full    : out std_logic;
    data_out        : out std_logic_vector(7 downto 0);
    rx_fifo_occ_in  : out std_logic_vector(3 downto 0);
    rx_fifo_occ_out : out std_logic_vector(3 downto 0)

    );


end entity i2c;


architecture i2c_rtl of i2c is
  
  component scl_sm_sync
    port (clk             : in  std_logic;
          reset           : in  std_logic;
          scl_oe          : out std_logic;
          scl_int         : in  std_logic;
          busy            : in  std_logic;
          idle_state      : in  std_logic;
--            bit_cnt         : in  std_logic_vector (3 downto 0);
          master_stop     : in  std_logic;
          i2c_rep_strt    : in  std_logic;
          reset_rep_start : out std_logic;
          sda_scl         : out std_logic;
          sda_ctrl        : out std_logic;
          masl            : in  std_logic;
          strt            : in  std_logic;
          t_const         : in  timming
          --scl_monitor     : out std_logic_vector (10 downto 0)
          );
  end component scl_sm_sync;

  component main_i2c_sm_sync
    port (clk             : in  std_logic;
          scl             : in  std_logic;
          reset           : in  std_logic;
          sda_masl        : out std_logic;
          sda             : in  std_logic;
          device_addr     : in  std_logic_vector (6 downto 0);
          i2c_stop        : out std_logic;
          i2c_rep_strt    : out std_logic;
          busy            : out std_logic;
          message_size    : in  std_logic_vector (3 downto 0);
          enable : in std_logic;
          idle_state      : out std_logic;
          txrx            : in  std_logic;
          masl            : in  std_logic;
          rep_start       : in  std_logic;
          tr_progress     : out std_logic;
          transaction     : out std_logic;
          slave_addressed : out std_logic;
          data_valid      : out std_logic;
          rst_valid       : in  std_logic;
          tx_fifo_rd_ena  : out std_logic;
          data_in         : in  std_logic_vector (7 downto 0);
          tx_fifo_empty   : in  std_logic;
          rx_fifo_wr_ena  : out std_logic;
          data_out        : out std_logic_vector (7 downto 0);
          rx_fifo_full    : in  std_logic
          --main_i2c_monitor : out std_logic_vector (7 downto 0)
          );
  end component main_i2c_sm_sync;

  component async_fifo
    port (reset         : in  std_logic;
          flush_fifo    : in  std_logic;
          wclk          : in  std_logic;
          rclk          : in  std_logic;
          write_enable  : in  std_logic;
          read_enable   : in  std_logic;
          fifo_occu_in  : out std_logic_vector (3 downto 0);
          fifo_occu_out : out std_logic_vector (3 downto 0);
          write_data_in : in  std_logic_vector (7 downto 0);
          read_data_out : out std_logic_vector (7 downto 0);
          full          : out std_logic;
          empty         : out std_logic);
  end component async_fifo;

  constant RESET_LEVEL : std_logic := '1';

  type tx_fifo_control_state_type is (idle, read, waiting);
  type rx_fifo_control_state_type is (idle, write, waiting);

  signal tx_fifo_control_state      : tx_fifo_control_state_type;
  signal tx_fifo_control_next_state : tx_fifo_control_state_type;

  signal rx_fifo_control_state      : rx_fifo_control_state_type;
  signal rx_fifo_control_next_state : rx_fifo_control_state_type;

  signal busy_int        : std_logic;
  signal scl_oe_int      : std_logic;
--   signal bit_cnt_int     : std_logic_vector (3 downto 0);
  signal master_stop_int : std_logic;
  signal sda_scl_int     : std_logic;
  signal sda_ctrl_int    : std_logic;
  signal sda_masl_int    : std_logic;
  signal sda_oe          : std_logic;

  --signal ack_state_int: std_logic;
  signal idle_state_int     : std_logic;
  signal tx_fifo_rd_ena_int : std_logic;
  signal tx_fifo_empty_int  : std_logic;
  signal rx_fifo_wr_ena_int : std_logic;
  signal i2c_data_out       : std_logic_vector (7 downto 0);
  signal rx_fifo_full_int   : std_logic;
  signal i2c_data_in        : std_logic_vector (7 downto 0);
  signal tx_fifo_read       : std_logic;
  signal rx_fifo_write      : std_logic;

  signal scl_int : std_logic;


  --- BEGIN CHIPSCOPE -----

--  component chipscope_icon
--  PORT (
--    CONTROL0 : INOUT STD_LOGIC_VECTOR(35 DOWNTO 0)
--       );
--
--end component;
--
--component chipscope_ila
--  PORT (
--    CONTROL : INOUT STD_LOGIC_VECTOR(35 DOWNTO 0);
--    CLK : IN STD_LOGIC;
--    TRIG0 : IN STD_LOGIC_VECTOR(31 DOWNTO 0));
--
--end component;
--
--signal trig : std_logic_vector(31 downto 0);
--signal control_int : STD_LOGIC_VECTOR(35 DOWNTO 0);
--
---- Synplicity black box declaration
--attribute syn_black_box : boolean;
--attribute syn_black_box of chipscope_icon: component is true;
--attribute syn_black_box of chipscope_ila: component is true;
--
--signal scl_monitor_int: std_logic_vector (10 downto 0);
--signal master_monitor_int: std_logic_vector (7 downto 0);
--
  --- END CHIPSCOPE -----


  signal i2c_rep_strt_int : std_logic;
begin


  --- BEGIN CHIPSCOPE -----

--  trig(0) <= sda;
--  trig(1) <= scl;
--  trig(2) <= sda_oe;
--  trig(3) <= scl_oe_int;
--  trig(4) <= sda_ctrl_int;
--  trig(5) <= sda_scl_int;
--  trig(6) <= sda_masl_int;
----   trig(7) <= tx_fifo_read;
--  trig(7) <= rx_fifo_rd_ena;
--  trig(8) <= rx_fifo_full_int;
--  --trig(8) <= rx_fifo_rd_ena;
--  trig(9) <= master_stop_int;
--  trig(10) <= busy_int;
--  trig(11) <= masl;
--  trig(12) <= strt;
--  trig(23 downto 13) <=  scl_monitor_int;
--  trig(31 downto 24) <= master_monitor_int; 
--

--  CHS_0 : chipscope_icon
--  port map (
--    CONTROL0 => control_int);
--  
--  CHS_1 : chipscope_ila
--  port map (
--    CONTROL => control_int,
--    CLK => clk,
--    TRIG0 => trig);
--  

  --- END CHIPSCOPE -----

  
  sample_scl : process(clk, reset)

  begin

    if reset = RESET_LEVEL then
      scl_int <= '1';
    elsif clk'event and clk = '1' then

      if scl = '0' then
        scl_int <= '0';
      else
        scl_int <= '1';
      end if;

    end if;

  end process sample_scl;


  SCL_PROC : component scl_sm_sync
    port map (
      clk             => clk,
      reset           => reset,
      scl_oe          => scl_oe_int,
      scl_int         => scl_int,
      busy            => busy_int,
      idle_state      => idle_state_int,
--       bit_cnt     => bit_cnt_int,
      master_stop     => master_stop_int,
      i2c_rep_strt    => i2c_rep_strt_int,
      reset_rep_start => reset_rep_start,
      sda_scl         => sda_scl_int,
      sda_ctrl        => sda_ctrl_int,
      masl            => masl,
      strt            => strt,
      t_const         => t_const
      --scl_monitor  => scl_monitor_int
      );


--TODO Rename 
-- sda_masl to sda_i2c
-- master_sm_sync to i2c_sm_sync
  I2C_PROC : component main_i2c_sm_sync
    port map (
      clk             => clk,
      scl             => scl,
      reset           => reset,
      sda_masl        => sda_masl_int,
      sda             => sda,
      device_addr     => device_addr,
      i2c_stop        => master_stop_int,
      i2c_rep_strt    => i2c_rep_strt_int,
      busy            => busy_int,
      message_size    => message_size,
      enable  => enable,
      idle_state      => idle_state_int,
      txrx            => txrx,
      masl            => masl,
      rep_start       => rep_start,
      tr_progress     => tr_progress,
      transaction     => transaction,
      slave_addressed => slave_addressed,
      data_valid      => data_valid,
      rst_valid       => rx_fifo_rd_ena,
      tx_fifo_rd_ena  => tx_fifo_rd_ena_int,
      data_in         => i2c_data_in,
      tx_fifo_empty   => tx_fifo_empty_int,
      rx_fifo_wr_ena  => rx_fifo_wr_ena_int,
      data_out        => i2c_data_out,
      rx_fifo_full    => rx_fifo_full_int
      --main_i2c_monitor => master_monitor_int
      );

  scl    <= '0'         when scl_oe_int = '0'   else 'Z';
  sda_oe <= sda_scl_int when sda_ctrl_int = '1' else sda_masl_int;
  sda    <= '0'         when sda_oe = '0'       else 'Z';

  busy <= busy_int;

  TX_FIFO_C : process(clk, reset, tx_fifo_control_state, tx_fifo_rd_ena_int)
  begin
    
    tx_fifo_control_next_state <= tx_fifo_control_state;

    case tx_fifo_control_state is
      
      when idle =>
        tx_fifo_read <= '0';
        if tx_fifo_rd_ena_int = '1' then
          tx_fifo_control_next_state <= read;
        end if;
        
      when read =>
        tx_fifo_read               <= '1';
        tx_fifo_control_next_state <= waiting;
        
      when waiting =>
        tx_fifo_read <= '0';
        if tx_fifo_rd_ena_int = '1' then
          tx_fifo_control_next_state <= waiting;
        else
          tx_fifo_control_next_state <= idle;
        end if;
        
    end case;

    if reset = RESET_LEVEL then
      tx_fifo_control_state <= idle;
    elsif clk'event and clk = '1' then
      tx_fifo_control_state <= tx_fifo_control_next_state;
    end if;
    
  end process TX_FIFO_C;

  tx_fifo_empty <= tx_fifo_empty_int;

  TX_FIFO : component async_fifo

    port map (
      reset         => reset,
      flush_fifo    => flush_fifo,
      wclk          => clk,             -- uP clock domain
      rclk          => clk,             -- I2C clock domain
      write_enable  => tx_fifo_wr_ena,
      read_enable   => tx_fifo_read,
      fifo_occu_in  => tx_fifo_occ_in,
      fifo_occu_out => tx_fifo_occ_out,
      write_data_in => data_in,
      read_data_out => i2c_data_in,
      full          => tx_fifo_full,
      empty         => tx_fifo_empty_int
      );

-- RX FIFO controller
  RX_FIFO_C : process(clk, reset, rx_fifo_control_state, rx_fifo_wr_ena_int)
  begin
    
    rx_fifo_control_next_state <= rx_fifo_control_state;


    case rx_fifo_control_state is
      
      when idle =>
        rx_fifo_write <= '0';
        if rx_fifo_wr_ena_int = '1' then
          rx_fifo_control_next_state <= write;
        end if;
        
      when write =>
        rx_fifo_write              <= '1';
        rx_fifo_control_next_state <= waiting;
        
      when waiting =>
        rx_fifo_write <= '0';
        if rx_fifo_wr_ena_int = '1' then
          rx_fifo_control_next_state <= waiting;
        else
          rx_fifo_control_next_state <= idle;
        end if;

    end case;

    if reset = RESET_LEVEL then
      rx_fifo_control_state <= idle;
    elsif clk'event and clk = '1' then
      rx_fifo_control_state <= rx_fifo_control_next_state;
    end if;
    
  end process RX_FIFO_C;

  rx_fifo_full <= rx_fifo_full_int;

  RX_FIFO : component async_fifo

    port map (
      reset         => reset,
      flush_fifo    => flush_fifo,
      wclk          => clk,             -- I2C clock domain
      rclk          => clk,             -- uP clock domain
      write_enable  => rx_fifo_write,
      read_enable   => rx_fifo_rd_ena,
      fifo_occu_in  => rx_fifo_occ_in,
      fifo_occu_out => rx_fifo_occ_out,
      write_data_in => i2c_data_out,
      read_data_out => data_out,
      full          => rx_fifo_full_int,
      empty         => rx_fifo_empty
      );

end architecture i2c_rtl;
