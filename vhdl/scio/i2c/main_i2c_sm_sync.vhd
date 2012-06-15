-- Ver. 0.00.00e
-- Ver. 0.00.01e
-- 

library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

entity main_i2c_sm_sync is

  -- TODO Sample of SCL
  -- SCL should be sampled before they can be used as inputs

  port(

    clk      : in  std_logic;
    scl      : in  std_logic;           --! Input clock
    reset    : in  std_logic;           --! Reset signal
    sda_masl : out std_logic;  			--! Signal that controls the output tri-state buffer
    sda      : in  std_logic;           --! Input signal from SDA line  

    device_addr  : in  std_logic_vector(6 downto 0);
    i2c_stop     : out std_logic;
    i2c_rep_strt : out std_logic;
    busy         : out std_logic;
    message_size : in  std_logic_vector(3 downto 0);
    enable : in std_logic;


    idle_state : out std_logic;

    txrx      : in std_logic;
    masl      : in std_logic;
    rep_start : in std_logic;

    tr_progress     : out std_logic;
    transaction     : out std_logic;
    slave_addressed : out std_logic;
    data_valid      : out std_logic;
    rst_valid       : in  std_logic;

    --FIFO signals
    tx_fifo_rd_ena : out std_logic;
    data_in        : in  std_logic_vector (7 downto 0);
    tx_fifo_empty  : in  std_logic;

    rx_fifo_wr_ena : out std_logic;
    data_out       : out std_logic_vector (7 downto 0);
    rx_fifo_full   : in  std_logic;

    main_i2c_monitor : out std_logic_vector(7 downto 0)



    ); 

end entity main_i2c_sm_sync;

architecture master_sm_sync_rtl of main_i2c_sm_sync is

  type   i2c_state_type is (idle, tx_header, tx_data, rx_data, ack_header, send_ack, wait_ack);
  signal i2c_state : i2c_state_type;

  signal reset_bit_count : std_logic;
  signal bit_count       : std_logic_vector(3 downto 0);
  signal bit_count_ena   : std_logic;


  constant DATA_LENGTH      : std_logic_vector(3 downto 0) := "0111";
  constant NEAR_DATA_LENGTH : std_logic_vector(3 downto 0) := "0110";
  constant WR               : std_logic                    := '0';
  constant RESET_LEVEL      : std_logic                    := '1';

  signal tx_data_reg : std_logic_vector(7 downto 0);
  signal rx_data_reg : std_logic_vector(7 downto 0);
  signal load        : std_logic;
  signal store       : std_logic;
  signal shift_ena   : std_logic;


  signal start : std_logic;
  signal stop  : std_logic;

  signal header_ok  : std_logic;
  signal sda_slave  : std_logic;
  signal sda_master : std_logic;


  signal tx_fifo_empty_reg : std_logic;
  --signal rx_fifo_full_reg: std_logic;
  signal sda_int           : std_logic;

  signal RX_ALL                    : std_logic;
  signal received_byte_count       : integer range 0 to 16;
  signal inc_received_bytes        : std_logic;
  signal reset_received_byte_count : std_logic;

  signal transaction_s     : std_logic;
  signal slave_addressed_s : std_logic;
  signal data_valid_s      : std_logic;
  signal i2c_stop_s        : std_logic;

begin

-- BEGIN MONITOR SIGNALS ---

  main_i2c_monitor(0)          <= start;
  main_i2c_monitor(1)          <= stop;
  main_i2c_monitor(2)          <= reset_bit_count;
  main_i2c_monitor(3)          <= bit_count_ena;
  main_i2c_monitor(7 downto 4) <= bit_count;

-- END MONITOR SIGNALS ---

  transaction     <= transaction_s;
  slave_addressed <= slave_addressed_s;
  data_valid      <= data_valid_s;
  i2c_stop        <= i2c_stop_s;

  sample : process(clk, reset)

  begin

    if reset = RESET_LEVEL then
      sda_int <= '1';
    elsif clk'event and clk = '1' then

      if sda = '0' then
        sda_int <= '0';
      else
        sda_int <= '1';
      end if;

    end if;

  end process sample;


  start_detect : process(i2c_state, reset, sda)

  begin

    -- A HIGH to LOW transition on the SDA line while SCL is HIGH indicates
    -- a START condition. As soon as we leave the IDLE state we reset the
    -- start signal. In theory, SDA is not allowed to change during the high
    -- part of SCL so there should not be false starts. The start signal thus
    -- will be high until the next falling edge of SCL.
    if (reset = RESET_LEVEL) or (i2c_state = tx_header) then
      start <= '0';
    elsif sda'event and sda = '0' then
      if scl /= '0' then
        start <= '1';
      else
        start <= '0';
      end if;
    end if;
    
    
  end process start_detect;

  stop_detect : process(reset, sda, start)

  begin

    -- A LOW to HIGH transition on the SDA line while SCL is HIGH indicates
    -- a STOP condition. We should reset the stop signal when a start condi-
    -- tion is detected. In theory, SDA is not allowed to change during the 
    -- high part of SCL so there should not be false stops.
    if (reset = RESET_LEVEL) or (start = '1') then
      stop <= '0';
    elsif sda'event and sda /= '0' then
      if scl /= '0' then
        stop <= '1';
      else
        stop <= '0';
      end if;
    end if;
    
  end process stop_detect;

  bus_busy : process(reset, clk)

  begin
    
    if reset = RESET_LEVEL then
      busy <= '0';
    elsif clk'event and clk = '1' then
      
      if start = '1' then
        busy <= '1';
      end if;

      if stop = '1' then
        busy <= '0';
      end if;
      
    end if;
    
  end process bus_busy;


  valid : process(reset, transaction_s, rst_valid)

  begin

    if reset = RESET_LEVEL or rst_valid = '1' then
      data_valid_s <= '0';

    elsif falling_edge(transaction_s) then

      if slave_addressed_s = '1' then
        data_valid_s <= '1';
--      else
--        data_valid_s <= '0';
      end if;

    end if;

  end process valid;

  process(reset, header_ok, i2c_state, rst_valid)
  begin

    if (reset = RESET_LEVEL) or  (rst_valid = '1') then
      slave_addressed_s <= '0';

    elsif rising_edge(header_ok) then
      slave_addressed_s <= '1';
    end if;

  end process;

  sda_masl  <= sda_master when MASL = '1'                              else sda_slave;
  header_ok <= '1'        when (tx_data_reg(7 downto 1) = device_addr) else '0';

  idle_state <= '1' when i2c_state = idle else '0';

  tx_fifo_rd_ena <= load;               -- when stop_scl_int = '0' else '0';
  rx_fifo_wr_ena <= store;
  data_out       <= rx_data_reg;


  process(message_size, received_byte_count)

  begin

    if received_byte_count = to_integer(signed(message_size)) then
      RX_ALL <= '1';
    else
      RX_ALL <= '0';
    end if;

  end process;

  MAS_SDA : process(clk, reset)
  begin
    
    
    if reset = RESET_LEVEL then
      sda_master <= '1';
      sda_slave  <= '1';
      
    elsif clk'event and clk = '1' then
      
      if ((i2c_state = tx_header) or (i2c_state = tx_data)) then
        sda_master <= tx_data_reg(7);
      elsif (i2c_state = send_ack and rx_fifo_full = '0' and RX_ALL = '0') then
        sda_master <= '0';
      else
        sda_master <= '1';
      end if;

      if (i2c_state = ack_header) then
        if header_ok = '1' then
          sda_slave <= '0';
        end if;
      elsif (i2c_state = tx_data) then
        sda_slave <= tx_data_reg(7);
      elsif (i2c_state = send_ack and rx_fifo_full = '0') then
        sda_slave <= '0';
      else
        sda_slave <= '1';
      end if;
      
    end if;
    
  end process;


--   stop_scl <= '1' when ((tx_fifo_empty_reg = '1') and ((i2c_state = ack_header) or (i2c_state = send_ack) or (i2c_state = wait_ack))) else stop_scl_a; 

-- SCL_STOP: process(i2c_state, reset, tx_fifo_empty)
-- 
-- 
-- 
-- 
-- begin
-- -- 
--      if reset = RESET_LEVEL then
--              stop_scl_int <= '0';
-- -- 
--      elsif ((i2c_state = ack_header) or (i2c_state = send_ack) or (i2c_state = wait_ack)) then
--                      if (tx_fifo_empty = '1') then
--                              stop_scl_int <= '1';
--                      else
--                              stop_scl_int <= '0';
--                      end if;
--              else
--                      stop_scl_int <= '0';
--      end if;
-- --
-- --
-- --   elsif clk'event and clk = '1' then
-- -- -- --             stop_scl_a <= stop_scl_i;
-- -- --
-- -- --
-- --           if (tx_fifo_empty = '1') then
-- --                   if ((i2c_state = ack_header) or (i2c_state = send_ack) or (i2c_state = wait_ack)) then
-- --                           stop_scl_int <= '1';
-- --                   else
-- --                           stop_scl_int <= '0';
-- --                   end if;
-- --
-- --           else
-- --                   stop_scl_int <= '0';
-- --           end if;
-- -- --
-- --   end if;
-- -- 
-- end process SCL_STOP;


  MASTER_SM : process (reset, scl, stop)

  begin
    
    if reset = RESET_LEVEL or stop = '1' then
      i2c_state                 <= idle;
      i2c_stop_s                <= '0';
      inc_received_bytes        <= '0';
      reset_received_byte_count <= '1';
      i2c_rep_strt              <= '0';
      transaction_s             <= '0';
      
    elsif scl'event and scl = '0' then
      
      -- Default values
      inc_received_bytes        <= '0';
      reset_received_byte_count <= '0';
      i2c_rep_strt              <= '0';
      transaction_s             <= '1';

      case i2c_state is
        
        when idle =>
          
          reset_received_byte_count <= '1';

          if start = '1' then

--          	if data_valid_s = '0' then
		i2c_state     <= tx_header;
		transaction_s <= '1';
--            else
--            	i2c_state     <= idle;
--            	transaction_s <= '0';
--            end if;
          else
            i2c_state     <= idle;
            transaction_s <= '0';
          end if;
          
        when tx_header =>
          --TODO: This state should be used to input the header
          -- into a shift register when acting as a slave. When 
          -- acting as master, we shift out the header info.

          if bit_count = DATA_LENGTH then
            i2c_state <= ack_header;
          else
            i2c_state <= tx_header;
          end if;

        when ack_header =>

          --TODO: Here we should check the following: 
          -- 1. Arbitration lost
          
          if sda = '0' then
            
            if masl = '1' then

              if txrx = WR then
                if (tx_fifo_empty_reg = '0') then
                  i2c_state <= tx_data;
                else
                  i2c_state  <= idle;
                  i2c_stop_s <= '1';
                end if;
              else
                if (rx_fifo_full = '0') then
                  i2c_state <= rx_data;
                else
                  i2c_state  <= idle;
                  i2c_stop_s <= '1';
                end if;
              end if;
              
            else

              -- Slave mode, check that slave has been addressed
              if header_ok = '1' then
                if rx_data_reg(0) = '0' then
                  if (rx_fifo_full = '0') then
                    i2c_state <= rx_data;
                  else
                    -- TODO Hold the clock until data is freed from the buffer
                    --i2c_state <= idle;
                    i2c_state <= rx_data;
                  end if;
                else
                  if (tx_fifo_empty_reg = '0') then
                    i2c_state <= tx_data;
                  else
                    -- TODO Hold the clock until data is written to the buffer
                    --i2c_state <= idle;
                    i2c_state <= tx_data;
                  end if;
                end if;
              else
                i2c_state <= idle;
              end if;
              
            end if;
            
          else
            i2c_state  <= idle;
            i2c_stop_s <= '1';
          end if;
          
        when tx_data =>

          if start = '1' then
            i2c_state <= tx_header;
            transaction_s <= '0';
          else
            if bit_count = DATA_LENGTH then
              i2c_state <= wait_ack;
            else
              i2c_state <= tx_data;
            end if;
          end if;

        when rx_data =>
          
          if start = '1' then
            i2c_state <= tx_header;
            transaction_s <= '0';
          else

            if bit_count = NEAR_DATA_LENGTH then
              inc_received_bytes <= '1';
            end if;

            if bit_count = DATA_LENGTH then
              i2c_state <= send_ack;
            else
              i2c_state <= rx_data;
            end if;
          end if;
          
        when wait_ack =>
          
          if sda = '0' then

            if rep_start = '0' then

              if tx_fifo_empty_reg = '0' then
                i2c_state <= tx_data;
              else
                i2c_state  <= idle;
                -- This stop signal should be generated only in master mode
                -- but it does not hurt to do it on slave mode since its
                -- purpose is to stop the SCL clock and when operating in
                -- slave mode there is no generated SCL.
                i2c_stop_s <= '1';
              end if;

            else
              i2c_state    <= idle;
              i2c_rep_strt <= '1';


            end if;



          else
            -- This stop signal should be generated only in master mode
            -- but it does not hurt to do it on slave mode since its
            -- purpose is to stop the SCL clock
            i2c_state  <= idle;
            i2c_stop_s <= '1';
          end if;
          
        when send_ack =>

          --TODO: If the master-receiver cannot write more data
          --into a full buffer, it can hold down the SCL line to 
          -- stop the slave-transmitter until space is available.
          if MASL = '1' then
            if rep_start = '0' then

              if rx_fifo_full = '0' and RX_ALL = '0' then
                i2c_state <= rx_data;
              else
                i2c_state  <= idle;
                i2c_stop_s <= '1';
              end if;

            else
              i2c_state    <= idle;
              i2c_rep_strt <= '1';
            end if;

          else
            if rx_fifo_full = '0' then
              i2c_state <= rx_data;
            else
              i2c_state  <= idle;
              i2c_stop_s <= '1';
            end if;

          end if;
          
      end case;
      
    end if;
    
  end process MASTER_SM;


--   bit_cnt <= bit_count;

  process(scl, reset)

  begin

    if reset = RESET_LEVEL then
      bit_count   <= "0000";
      tx_data_reg <= (others => '0');

      --tx_fifo_empty_reg <= '0';

    elsif scl'event and scl = '0' then

      if reset_bit_count = '1' then
        bit_count <= "0000";
      elsif bit_count_ena = '1' then
        bit_count <= std_logic_vector(unsigned(bit_count) + 1);
      end if;

      -- load
      if load = '1' then
        tx_data_reg <= data_in;
        -- tx shift
      elsif shift_ena = '1' then
        tx_data_reg <= tx_data_reg(6 downto 0) & sda_int;
      end if;

--     elsif scl'event and scl /= '0' then
--     
--               if ((i2c_state = tx_header) or (i2c_state = rx_data) or (i2c_state = tx_data)) then
--                      tx_fifo_empty_reg <= tx_fifo_empty;
--               end if;

    end if;
    
  end process;

  process(scl, reset)

  begin

    if reset = RESET_LEVEL then
      rx_data_reg <= (others => '0');

    elsif scl'event and scl /= '0' then

      if shift_ena = '1' then
        rx_data_reg <= rx_data_reg(6 downto 0) & sda_int;
      end if;
      
    end if;
    
  end process;


  reset_bit_count <= '1' when (i2c_state = ack_header) or (i2c_state = wait_ack) or (i2c_state = idle) or (i2c_state = send_ack) or ((i2c_state = rx_data) and (start = '1')) or ((i2c_state = tx_data) and (start = '1'))
                     else '0';
  bit_count_ena <= '1' when (i2c_state = tx_header) or (i2c_state = rx_data) or (i2c_state = tx_data)
                   else '0';

  tr_progress <= bit_count_ena;

-- This process controls the shift register to avoid gated clocks.                                              
  process(clk, reset)

  begin
    if reset = RESET_LEVEL then
      shift_ena         <= '0';
      load              <= '0';
      tx_fifo_empty_reg <= '0';
      --rx_fifo_full_reg <= '0';
      
    elsif clk'event and clk = '1' then

--           tx_fifo_empty_reg <= tx_fifo_empty;


      -- Enable shifting only when transmitting/receiving data
      -- We sample the tx fifo empty flag in the non ACK states to avoid stopping the
      -- process due to the current write
      --if ((MASL = '1' and i2c_state = tx_header) or (i2c_state = rx_data) or (i2c_state = tx_data)) then
      if ((i2c_state = tx_header) or (i2c_state = rx_data) or (i2c_state = tx_data)) then
        shift_ena         <= '1';
        tx_fifo_empty_reg <= tx_fifo_empty;
        --rx_fifo_full_reg <= rx_fifo_full;
      else
        shift_ena <= '0';
      end if;

      -- The conditions to load new data from the buffer are:
      -- 1. If we are waiting for an acknowledge in TX mode (master or slave)
      -- 2. A slave transmitter acknowledging the header
      -- 3. A master transmitter waiting for an ack of the header just sent
      if ((i2c_state = wait_ack and rep_start = '0') or
          (i2c_state = ack_header and tx_data_reg(0) = '1' and MASL = '0') or
          (i2c_state = ack_header and TXRX = WR and MASL = '1') or
          (start = '1')) then
        load <= '1';
      else
        load <= '0';
      end if;

      -- The conditions to store new data into the buffer are:
      -- 1. If we are sending an acknowledge in RX mode (master or slave)
      if (i2c_state = send_ack) then
        store <= '1';
      else
        store <= '0';
      end if;
      
    end if;

  end process;

  process(reset, reset_received_byte_count, scl)

  begin

    if reset = RESET_LEVEL or reset_received_byte_count = '1' then
      received_byte_count <= 0;
    elsif scl'event and scl = '0' then

      if inc_received_bytes = '1' then
        received_byte_count <= received_byte_count + 1;
      end if;

    end if;

  end process;

end architecture master_sm_sync_rtl;



