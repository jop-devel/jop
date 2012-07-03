library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;


use work.sc_pack.all;
use work.dram_pack.all;

entity sc_mem_if is
--  generic (addr_bits : integer);

  port (

    clk, reset : in std_logic;

    sc_mem_out : in  sc_out_type;
    sc_mem_in  : out sc_in_type;

-- memory interface

    dram_pll  : in    dram_pll_type;
    dram_ctrl : out   dram_ctrl_type;
    dram_data : inout dram_data_type

    );
end sc_mem_if;

architecture rtl of sc_mem_if is

  signal zeros : std_logic_vector(31 downto 0);
-------------------------------------------------------------------------------
-- State
-------------------------------------------------------------------------------   
  type   state_type is (s0, s1, s2, s3, s4, s5, s6, s7, s8, s9, s10, s11, s12, s13, s14, s15, s16, s17, s18, s19, s20, s21, s22, s23, s24, s25, s26, sref, sinit, sread, swrite);
-------------------------------------------------------------------------------
  type   amux_mode_type is (AMUX_NOP, AMUX_PALL, AMUX_ACT, AMUX_MRS, AMUX_PRE, AMUX_WRA, AMUX_RDA, AMUX_WR, AMUX_RD);
  type   dmux_mode_type is (DMUX_NOP, DMUX_WR, DMUX_RD);
-------------------------------------------------------------------------------
  type   cmd_descr_type is (NO_CMD, C_NOP, C_PALL, C_PRE, C_ACT, C_MRS, C_WR, C_WRA, C_RD, C_RDA, C_REF);
-------------------------------------------------------------------------------
  type   cmd_param_type is record
    delay_cnt : integer range 0 to 31;
    amux_mode : amux_mode_type;
    dmux_mode : dmux_mode_type;
    cmd       : cmd_descr_type;
    cs        : std_logic;
    ras       : std_logic;
    cas       : std_logic;
    we        : std_logic;
    addr      : std_logic_vector(DRAM_ADDR_WIDTH-1 downto 0);
    ba        : std_logic_vector(1 downto 0);
  end record;
-------------------------------------------------------------------------------
-- Command Interface record
-------------------------------------------------------------------------------  
  signal state             : state_type;
  signal int_next_state    : state_type;
  signal next_state        : state_type;
  signal trigger_state     : state_type;
  signal current_cmd       : cmd_descr_type;
  signal issue             : std_logic;
  signal next_issue        : std_logic;
  signal trigger_ready     : std_logic;
  signal trigger_ready_ack : std_logic;

  signal refresh_enable : std_logic;
  signal bl_bits        : std_logic_vector(2 downto 0);

  signal int_data         : std_logic_vector(15 downto 0);
  signal delay_count      : integer;
  signal next_delay_count : integer;

  signal cmd_iface       : cmd_param_type;
  signal write_burst     : integer;
  signal read_burst      : integer;
  signal dmux_delay      : integer;
  signal data_out_enable : std_logic;

  signal dmux_start_write       : std_logic;
  signal dmux_write_in_progress : std_logic;
  signal dmux_write_length      : integer;
  signal dmux_write_length_next : integer;

  signal dmux_start_read             : std_logic;
  signal dmux_read_in_progress       : std_logic;
  signal dmux_read_length            : integer;
  signal dmux_read_length_next       : integer;
  signal dmux_read_delay             : integer;
  signal dmux_read_delay_in_progress : std_logic;
  signal dmux_read_delay_next        : integer;

  signal bank   : std_logic_vector(1 downto 0);
  signal row    : std_logic_vector(11 downto 0);
  signal column : std_logic_vector(7 downto 0);

-------------------------------------------------------------------------------  
  procedure auto_refresh (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_REF;
    --
    p.delay_cnt <= 6;
    --
    p.dmux_mode <= DMUX_NOP;
    p.amux_mode <= AMUX_NOP;
    --
    p.cs        <= '0';
    p.ras       <= '0';
    p.cas       <= '0';
    p.we        <= '1';
    --
  end auto_refresh;
-------------------------------------------------------------------------------
  procedure nop (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_NOP;
    --
    p.delay_cnt <= 0;
    --
    p.dmux_mode <= DMUX_NOP;
    p.amux_mode <= AMUX_NOP;
    --
    p.cs        <= '0';
    p.ras       <= '1';
    p.cas       <= '1';
    p.we        <= '1';
    --
  end nop;
-------------------------------------------------------------------------------
  procedure activate_bank (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_ACT;
    --
    p.delay_cnt <= 2;                   --trcd
    --
    p.dmux_mode <= DMUX_NOP;
    p.amux_mode <= AMUX_ACT;
    --
    p.cs        <= '0';
    p.ras       <= '0';
    p.cas       <= '1';
    p.we        <= '1';
    --
  end activate_bank;
-------------------------------------------------------------------------------
  procedure precharge_all (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_PALL;
    --
    p.delay_cnt <= 2;
    --
    p.dmux_mode <= DMUX_NOP;
    p.amux_mode <= AMUX_PALL;
    --      
    p.cs        <= '0';
    p.ras       <= '0';
    p.cas       <= '1';
    p.we        <= '0';
    --
  end precharge_all;
-------------------------------------------------------------------------------
  procedure precharge (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_PRE;
    --
    p.delay_cnt <= 2;
    --
    p.dmux_mode <= DMUX_NOP;
    p.amux_mode <= AMUX_PRE;
    --      
    p.cs        <= '0';
    p.ras       <= '0';
    p.cas       <= '1';
    p.we        <= '0';
    --
  end precharge;
-------------------------------------------------------------------------------
  procedure set_mode_register (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_MRS;
    --
    p.delay_cnt <= 2;
    --
    p.dmux_mode <= DMUX_NOP;
    p.amux_mode <= AMUX_MRS;
    --      
    p.cs        <= '0';
    p.ras       <= '0';
    p.cas       <= '0';
    p.we        <= '0';
    --
  end set_mode_register;
-------------------------------------------------------------------------------
  procedure reada (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_RDA;
    --
    p.delay_cnt <= 8+4;
    --
    p.amux_mode <= AMUX_RDA;
    p.dmux_mode <= DMUX_RD;
    --      
    p.cs        <= '0';
    p.ras       <= '1';
    p.cas       <= '0';
    p.we        <= '1';
    --
  end reada;
-------------------------------------------------------------------------------
  procedure writea (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_WRA;
    --
    p.delay_cnt <= 8;
    --    
    p.amux_mode <= AMUX_WRA;
    p.dmux_mode <= DMUX_WR;
    --      
    p.cs        <= '0';
    p.ras       <= '1';
    p.cas       <= '0';
    p.we        <= '0';
    --
  end writea;
-------------------------------------------------------------------------------
  procedure read_nopre (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_RD;
    --
    p.delay_cnt <= 6;
    --
    p.amux_mode <= AMUX_RD;
    p.dmux_mode <= DMUX_RD;
    --      
    p.cs        <= '0';
    p.ras       <= '1';
    p.cas       <= '0';
    p.we        <= '1';
    --
  end read_nopre;
-------------------------------------------------------------------------------
  procedure write_nopre (signal p : inout cmd_param_type) is
  begin
    p.cmd       <= C_WR;
    --
    p.delay_cnt <= 4;
    --    
    p.amux_mode <= AMUX_WR;
    p.dmux_mode <= DMUX_WR;
    --      
    p.cs        <= '0';
    p.ras       <= '1';
    p.cas       <= '0';
    p.we        <= '0';
    --
  end write_nopre;

  signal clk_100                    : std_logic;
  signal clk_100_skew               : std_logic;
  signal enable_second_half_trigger : std_logic;
  signal enable_second_half         : std_logic;
  signal sync_0                     : std_logic;
  signal sync_1                     : std_logic;
  signal int_reset                  : std_logic;

  signal initial_wait_counter : unsigned(9 downto 0);
  signal initial_done         : std_logic;

  type sc_state_type is (sc_reset, sc_reset_initiate,
                         sc_idle,
                         sc_read_pending, sc_read_in_progress_low, sc_read_in_progress_high,
                         sc_write_pending, sc_write_in_progress_low, sc_write_in_progress_high);

  signal sc_state           : sc_state_type;
  signal sc_adr             : std_logic_vector(22 downto 0);
  signal sc_wr_data_low     : std_logic_vector(15 downto 0);
  signal sc_wr_data_high    : std_logic_vector(15 downto 0);
  signal sc_rd_data_low     : std_logic_vector(15 downto 0);
  signal sc_rd_data_high    : std_logic_vector(15 downto 0);
  signal sc_rdy_cnt         : std_logic_vector(1 downto 0);
  signal sc_rdy_cnt_delayed : std_logic_vector(1 downto 0);

  signal int_rdy_cnt         : std_logic_vector(1 downto 0);
  
  signal next_sc_state        : sc_state_type;
  signal next_sc_adr          : std_logic_vector(22 downto 0);
  signal next_sc_wr_data_low  : std_logic_vector(15 downto 0);
  signal next_sc_wr_data_high : std_logic_vector(15 downto 0);
  signal next_sc_rd_data_low  : std_logic_vector(15 downto 0);
  signal next_sc_rd_data_high : std_logic_vector(15 downto 0);
  signal next_sc_rdy_cnt      : std_logic_vector(1 downto 0);

  signal refresh_pending     : std_logic;
  signal refresh_pending_ack : std_logic;

  signal cross_clock_data_enable : std_logic;

  signal sc_wr_pending : std_logic;
  signal sc_rd_pending : std_logic;  
  signal next_sc_wr_pending : std_logic;
  signal next_sc_rd_pending : std_logic;  
begin
  int_reset    <= '1' when reset = '1' or dram_pll.locked = '0' else '0';
  clk_100      <= dram_pll.clk;
  clk_100_skew <= dram_pll.clk_skew;
  zeros        <= (others => '0');

-------------------------------------------------------------------------------
-- Debug
-------------------------------------------------------------------------------
  dram_ctrl.debug(0) <= clk_100;
  dram_ctrl.debug(1) <= clk_100_skew;
-------------------------------------------------------------------------------
-- SimpCON Frontend
-------------------------------------------------------------------------------

  --    if sc_mem_out.rd = '1' or sc_mem_out.wr = '1' then
  --      ram_addr <= sc_mem_out.address(addr_bits-2 downto 0) & "0";
  --      sc_mem_out.wr_data(31 downto 16);
  --      sc_mem_in.rd_data <= ram_din_reg;
  --sc_mem_in.rdy_cnt <= cnt;

-------------------------------------------------------------------------------
  sc_mem_in.rdy_cnt <= unsigned(int_rdy_cnt);
  process (int_reset, clk)
  begin  -- process
    if int_reset = '1' then             -- synchronous reset (active high)
      int_rdy_cnt <= "11";
    elsif clk'event and clk = '1' then  -- rising clock edge
      if sc_rdy_cnt = "00" or sc_rdy_cnt_delayed = "00" then
        int_rdy_cnt <= "00";
      elsif int_rdy_cnt <= "00" and (sc_mem_out.rd = '1' or sc_mem_out.wr = '1') then
        int_rdy_cnt <= "11"; 
      end if;
      sc_mem_in.rd_data <= sc_rd_data_high & sc_rd_data_low;
    end if;
  end process;

  process (clk_100, int_reset)
  begin  -- process
    if int_reset = '1' then             -- asynchronous reset (active low)
      sc_state             <= sc_reset;
      sc_adr               <= (others => '0');
      sc_wr_data_low       <= (others => '0');
      sc_wr_data_high      <= (others => '0');
      sc_rd_data_low       <= (others => '0');
      sc_rd_data_high      <= (others => '0');
      sc_rdy_cnt           <= "11";
      sc_rdy_cnt_delayed   <= "11";
      initial_wait_counter <= (others => '0');
      sync_0               <= '0';
      sync_1               <= '1';
      refresh_enable       <= '0';
      enable_second_half   <= '0';
      refresh_pending      <= '0';
      sc_wr_pending        <= '0';
      sc_rd_pending        <= '0';
    elsif clk_100'event and clk_100 = '1' then
      sc_state        <= next_sc_state;
      sc_adr          <= next_sc_adr;
      sc_wr_data_low  <= next_sc_wr_data_low;
      sc_wr_data_high <= next_sc_wr_data_high;
      sc_rd_data_low  <= next_sc_rd_data_low;
      sc_rd_data_high <= next_sc_rd_data_high;

      sc_rdy_cnt_delayed <= sc_rdy_cnt;
      sc_rdy_cnt         <= next_sc_rdy_cnt;

      sc_rd_pending <= next_sc_rd_pending;
      sc_wr_pending <= next_sc_wr_pending;            
      
      sync_0 <= sc_mem_out.rd or sc_mem_out.wr;  -- Connect to other clock domain
      sync_1 <= sync_0;                 -- Keep histroy for event detection    

      if enable_second_half_trigger = '1' then
        enable_second_half <= '0';
      else
        enable_second_half <= not enable_second_half;
      end if;

      initial_wait_counter <= initial_wait_counter + 1;

      if initial_wait_counter(5 downto 0) = "111111" and refresh_enable = '1' then
--        refresh_pending <= '1';
      end if;

      if refresh_pending_ack = '1' then
        refresh_pending <= '0';
      end if;

      if initial_done = '1' then
        refresh_enable <= '1';
      end if;
    end if;
  end process;

  -- turns to one in the second half of the slow clock period
  enable_second_half_trigger <= '1' when sync_0 = '1' and sync_1 = '0'                                else '0';
  cross_clock_data_enable    <= '1' when enable_second_half_trigger = '1' or enable_second_half = '1' else '0';

  process(sc_state, sc_rd_pending, sc_wr_pending, sc_adr, sc_wr_data_low, sc_wr_data_high, sc_rd_data_low, sc_rd_data_high, trigger_ready_ack, initial_wait_counter, sc_mem_out, cross_clock_data_enable, dmux_read_in_progress, dram_data, data_out_enable)
  begin  -- process
    next_sc_state        <= sc_state;
    next_sc_adr          <= sc_adr;
    next_sc_wr_data_low  <= sc_wr_data_low;
    next_sc_wr_data_high <= sc_wr_data_high;
    next_sc_rd_data_low  <= sc_rd_data_low;
    next_sc_rd_data_high <= sc_rd_data_high;
    next_sc_rdy_cnt      <= "11";
    next_sc_rd_pending   <= sc_rd_pending;
    next_sc_wr_pending   <= sc_wr_pending;

    trigger_ready <= '0';
    trigger_state <= s0;
    initial_done  <= '0';

    if sc_mem_out.rd = '1' and cross_clock_data_enable = '1' then
      next_sc_adr        <= sc_mem_out.address;
      next_sc_rd_pending <= '1';
    elsif sc_mem_out.wr = '1' and cross_clock_data_enable = '1' then
      next_sc_adr          <= sc_mem_out.address;
      next_sc_wr_data_low  <= sc_mem_out.wr_data(15 downto 0);
      next_sc_wr_data_high <= sc_mem_out.wr_data(31 downto 16);
      next_sc_wr_pending   <= '1';
    end if;

    case sc_state is
-------------------------------------------------------------------------------
-- Reset
-------------------------------------------------------------------------------
      when sc_reset =>
        if initial_wait_counter(8) = '1' then
          next_sc_state <= sc_reset_initiate;
        end if;

      when sc_reset_initiate =>
        trigger_ready <= '1';
        trigger_state <= sinit;
        next_sc_state <= sc_reset_initiate;

        if trigger_ready_ack = '1' then
          next_sc_state <= sc_idle;
        end if;

-------------------------------------------------------------------------------
-- Idle
-------------------------------------------------------------------------------
      when sc_idle =>
        initial_done <= '1';            
        if (sc_mem_out.rd = '1' and cross_clock_data_enable = '1') or sc_rd_pending = '1' then
          next_sc_state <= sc_read_pending;
        elsif (sc_mem_out.wr = '1' and cross_clock_data_enable = '1') or sc_wr_pending = '1' then
          next_sc_state <= sc_write_pending;
        end if;

-------------------------------------------------------------------------------
-- Read
-------------------------------------------------------------------------------
      when sc_read_pending =>
        next_sc_rd_pending <= '0';
        trigger_ready <= '1';
        trigger_state <= sread;

        if trigger_ready_ack = '1' then
          next_sc_state <= sc_read_in_progress_low;
        end if;

      when sc_read_in_progress_low =>

        if dmux_read_in_progress = '1' then
          next_sc_rd_data_high <= dram_data(15 downto 0);
          next_sc_state        <= sc_read_in_progress_high;
        end if;

      when sc_read_in_progress_high =>

        if dmux_read_in_progress = '1' then
          next_sc_rd_data_low <= dram_data(15 downto 0);
          next_sc_state       <= sc_idle;
          next_sc_rdy_cnt     <= "00";
        end if;

-------------------------------------------------------------------------------
-- Write
-------------------------------------------------------------------------------        
      when sc_write_pending =>
        next_sc_wr_pending <= '0';
        trigger_ready <= '1';
        trigger_state <= swrite;

        if trigger_ready_ack = '1' then
          next_sc_state <= sc_write_in_progress_low;
        end if;

      when sc_write_in_progress_low =>

        if data_out_enable = '1' then
          next_sc_state <= sc_write_in_progress_high;
        end if;

      when sc_write_in_progress_high =>

        if data_out_enable = '1' then
          next_sc_state   <= sc_idle;
          next_sc_rdy_cnt <= "00";
        end if;
        
      when others => null;
    end case;

  end process;

  column <= sc_adr(6 downto 0) & "0";
  row    <= sc_adr(18 downto 7);
  bank   <= sc_adr(20 downto 19);

  bl_bits <= "001";

-------------------------------------------------------------------------------
-- Waitstate logic and delay counter
-------------------------------------------------------------------------------

  next_delay_count <= (cmd_iface.delay_cnt - 1) when issue = '1' else  --inject
                                                                       --delay
                      (delay_count - 1) when delay_count > 0 else  -- decrement
                                                                   -- otherwise
                      0;                -- this value is used as a flag

  next_issue <= '1' when issue = '1' and cmd_iface.delay_cnt = 0 else  --singlecycle command
                '1' when issue = '0' and delay_count = 0 else  --multicycle
                                        --command finished
                '0';                    -- multicycle command running

-- Handshake trigger_ready
  refresh_pending_ack <= '1' when next_issue = '1' and next_state = s0 and refresh_pending = '1'                         else '0';
  trigger_ready_ack   <= '1' when next_issue = '1' and next_state = s0 and trigger_ready = '1' and refresh_pending = '0' else '0';
--  trigger_ready_ack   <= '1' when next_issue = '1' and next_state = s0 and trigger_ready = '1' else '0';  

  int_next_state <= sref when refresh_pending_ack = '1' else
                    trigger_state when trigger_ready_ack = '1' else
                    next_state    when next_issue = '1'        else
                    state;

-------------------------------------------------------------------------------
-- Register file
-------------------------------------------------------------------------------
  output : process (clk_100, int_reset)
  begin  -- process output
    if clk_100'event and clk_100 = '1' then  -- rising clock edge
      if int_reset = '1' then                -- synchronous reset (active high)

        state       <= s0;
        issue       <= '0';
        delay_count <= 0;
        current_cmd <= NO_CMD;

        dram_data <= (others => 'Z');

        dram_ctrl.ras_n <= '1';
        dram_ctrl.cas_n <= '1';
        dram_ctrl.cs_n  <= '1';
        dram_ctrl.we_n  <= '1';
        dram_ctrl.addr  <= (others => '0');
        dram_ctrl.ba    <= (others => '0');

        dmux_write_length           <= 0;
        dmux_write_in_progress      <= '0';
        dmux_read_length            <= 0;
        dmux_read_delay             <= 0;
        dmux_read_in_progress       <= '0';
        dmux_read_delay_in_progress <= '0';
        
      else
        delay_count <= next_delay_count;
        state       <= int_next_state;
        issue       <= next_issue;

        if dmux_start_write = '1' then
          if dmux_write_length_next > 0 then
            dmux_write_in_progress <= '1';
          end if;
          dmux_write_length <= dmux_write_length_next;
        else
          if dmux_write_length > 0 then
            dmux_write_length <= dmux_write_length - 1;
          end if;
          if dmux_write_in_progress = '1' then
            if dmux_write_length < 2 then
              dmux_write_in_progress <= '0';
            end if;
          end if;
        end if;

        if dmux_start_read = '1' then
          dmux_read_delay             <= dmux_read_delay_next;
          dmux_read_length            <= dmux_read_length_next;
          dmux_read_in_progress       <= '0';
          dmux_read_delay_in_progress <= '1';
        else
          if dmux_read_delay > 0 then
            dmux_read_delay_in_progress <= '1';
            dmux_read_delay             <= dmux_read_delay - 1;
          else
            dmux_read_delay_in_progress <= '0';
            if dmux_read_length > 0 then
              dmux_read_length <= dmux_read_length - 1;
            end if;
          end if;

          if dmux_read_delay = 1 then
            dmux_read_in_progress <= '1';
          end if;

          if dmux_read_in_progress = '1' then
            if dmux_read_length < 2 then
              dmux_read_in_progress <= '0';
            end if;
          end if;
        end if;


        if issue = '1' then
          dram_ctrl.cs_n  <= cmd_iface.cs;
          dram_ctrl.ras_n <= cmd_iface.ras;
          dram_ctrl.cas_n <= cmd_iface.cas;
          dram_ctrl.we_n  <= cmd_iface.we;
        else
          dram_ctrl.cs_n  <= '1';
          dram_ctrl.ras_n <= '1';
          dram_ctrl.cas_n <= '1';
          dram_ctrl.we_n  <= '1';
        end if;

        if data_out_enable = '1' or dmux_start_read = '1' or dmux_read_in_progress = '1' or dmux_read_delay_in_progress = '1' then
          dram_ctrl.dqm <= "00";
        else
          dram_ctrl.dqm <= "11";
        end if;

        dram_ctrl.addr <= cmd_iface.addr;
        dram_ctrl.ba   <= cmd_iface.ba;

        if data_out_enable = '1' then
          if sc_state = sc_write_in_progress_low then
            dram_data <= sc_wr_data_high;
          else
            dram_data <= sc_wr_data_low;
          end if;
        else
          dram_data <= (others => 'Z');
        end if;

        current_cmd <= cmd_iface.cmd;   -- for simulation purpose
      end if;
    end if;
  end process output;
  dram_ctrl.clk <= clk_100_skew;
  dram_ctrl.cke <= '1';


------------------------------------------------------------------------------
-- AMUX AMUX AMUX
------------------------------------------------------------------------------

  amux : process (int_reset, cmd_iface, row, column, bank, bl_bits)
  begin  -- process amux
    cmd_iface.addr <= (others => '0');
    cmd_iface.ba   <= (others => '0');

    case cmd_iface.amux_mode is
      when AMUX_NOP =>

      when AMUX_PALL =>
        cmd_iface.addr     <= (others => '0');
        cmd_iface.addr(10) <= '1';
        cmd_iface.ba       <= "00";

      when AMUX_PRE =>
        cmd_iface.addr <= (others => '0');
        cmd_iface.ba   <= bank;

      when AMUX_ACT =>
        cmd_iface.addr <= row;
        cmd_iface.ba   <= bank;

      when AMUX_RD =>
        cmd_iface.addr(7 downto 0) <= column;
        cmd_iface.addr(9 downto 8) <= "00";
        cmd_iface.addr(10)         <= '0';  -- NO Autoprecharge
        cmd_iface.ba               <= bank;

      when AMUX_WR =>
        cmd_iface.addr(7 downto 0) <= column;
        cmd_iface.addr(9 downto 8) <= "00";
        cmd_iface.addr(10)         <= '0';  -- NO Autoprecharge
        cmd_iface.ba               <= bank;
        
      when AMUX_RDA =>
        cmd_iface.addr(7 downto 0) <= column;
        cmd_iface.addr(9 downto 8) <= "00";
        cmd_iface.addr(10)         <= '1';  -- Autoprecharge
        cmd_iface.ba               <= bank;

      when AMUX_WRA =>
        cmd_iface.addr(7 downto 0) <= column;
        cmd_iface.addr(9 downto 8) <= "00";
        cmd_iface.addr(10)         <= '1';  -- Autoprecharge
        cmd_iface.ba               <= bank;
        
      when AMUX_MRS =>
        cmd_iface.addr             <= (others => '0');
        cmd_iface.addr(2 downto 0) <= bl_bits;
        cmd_iface.addr(3)          <= '0';   -- SEQUENTIAL
        cmd_iface.addr(6 downto 4) <= "010";  -- CAS Latency = 2 -- 011=3
        cmd_iface.addr(8 downto 7) <= "00";  -- Normal MRS
        cmd_iface.addr(9)          <= '0';  -- 0 -- Writes use Read-BL
        cmd_iface.ba               <= "00";
        
      when others => null;
    end case;
  end process amux;

-------------------------------------------------------------------------------
-- DMUX DMUX DMUX
-------------------------------------------------------------------------------
  dmux_comb : process (int_reset, cmd_iface, issue, bl_bits)
  begin  -- process
    dmux_start_write <= '0';
    dmux_start_read  <= '0';

    dmux_write_length_next <= 0;
    dmux_read_delay_next   <= 0;
    dmux_read_length_next  <= 0;

    if issue = '1' then
      case cmd_iface.dmux_mode is
        when DMUX_NOP =>

        when DMUX_WR =>
          dmux_start_write <= '1';

          case bl_bits is
            when "011" =>
              dmux_write_length_next <= 7;
            when "010" =>
              dmux_write_length_next <= 3;
            when "001" =>
              dmux_write_length_next <= 1;
            when "000" =>
              dmux_write_length_next <= 0;
            when others =>
              dmux_write_length_next <= 0;
          end case;
          
        when DMUX_RD =>
          dmux_start_read      <= '1';
          dmux_read_delay_next <= 2;    -- Use (Waitcycle)
          -- 1 = 50mhz CL=2
          -- 2 = 100mhz CL=2

          case bl_bits is
            when "011" =>
              dmux_read_length_next <= 8;
            when "010" =>
              dmux_read_length_next <= 4;
            when "001" =>
              dmux_read_length_next <= 2;
            when "000" =>
              dmux_read_length_next <= 1;
            when others =>
              dmux_read_length_next <= 0;
          end case;
          
        when others => null;
      end case;

    end if;
  end process;

-------------------------------------------------------------------------------
-- FIFO control signals for DRAM side 
-------------------------------------------------------------------------------
  data_out_enable <= (dmux_start_write or dmux_write_in_progress);

--  fifo_to_dram_rd <= '1' when data_out_enable = '1' else '0';

--  fifo_from_dram_wr <= '1' when dmux_read_in_progress = '1' else '0';

-------------------------------------------------------------------------------
-- State Sequencer
-------------------------------------------------------------------------------  
  seq_comb : process (state, cmd_iface, refresh_enable)
  begin  -- process seq_comb
    nop (cmd_iface); next_state <= s0;

    case state is
      when s0 =>
        if refresh_enable = '1' then
          auto_refresh (cmd_iface); next_state <= s0;
        else
          nop (cmd_iface); next_state <= s0;
        end if;
        
      when sinit =>
        precharge_all (cmd_iface); next_state <= s2;
      when s2 =>
        precharge_all (cmd_iface); next_state <= s3;
      when s3 =>
        precharge_all (cmd_iface); next_state <= s4;
      when s4 =>
        precharge_all (cmd_iface); next_state <= s5;
      when s5 =>
        precharge_all (cmd_iface); next_state <= s6;
      when s6 =>
        precharge_all (cmd_iface); next_state <= s7;
      when s7 =>
        precharge_all (cmd_iface); next_state <= s8;
      when s8 =>
        precharge_all (cmd_iface); next_state <= s9;

      when s9 =>
        auto_refresh (cmd_iface); next_state <= s10;
      when s10 =>
        auto_refresh (cmd_iface); next_state <= s11;
      when s11 =>
        auto_refresh (cmd_iface); next_state <= s12;
      when s12 =>
        auto_refresh (cmd_iface); next_state <= s13;
      when s13 =>
        auto_refresh (cmd_iface); next_state <= s14;
      when s14 =>
        auto_refresh (cmd_iface); next_state <= s15;
      when s15 =>
        auto_refresh (cmd_iface); next_state <= s16;
      when s16 =>
        auto_refresh (cmd_iface); next_state <= s17;
      when s17 =>
        set_mode_register (cmd_iface); next_state <= s19;
        
      when s19 =>
        precharge_all (cmd_iface); next_state <= s20;
      when s20 =>
        precharge_all (cmd_iface); next_state <= s21;
      when s21 =>
--          nop (cmd_iface); next_state <= s0;
        next_state <= s0;

      when swrite =>
        activate_bank (cmd_iface); next_state <= s22;
      when s22 =>
        write_nopre (cmd_iface); next_state <= s23;
      when s23 =>
        precharge(cmd_iface); next_state <= s0;
        
      when sread =>
        activate_bank (cmd_iface); next_state <= s25;
      when s25 =>
        read_nopre (cmd_iface); next_state <= s26;
      when s26 =>
        precharge(cmd_iface); next_state <= s0;

      when sref =>
        auto_refresh (cmd_iface); next_state <= s0;
        
      when others => null;
    end case;
  end process seq_comb;

end rtl;
