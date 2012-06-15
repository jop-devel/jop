-- Library IEEE;
-- use IEEE.std_logic_1164.all;
-- use ieee.numeric_std.all;
-- 
-- use work.jop_types.all;
-- use work.sc_pack.all;
-- use work.jop_config.all;
-- use work.i2c_pkg.all;
-- use work.fifo_pkg.all;
-- 
-- 
-- entity sc_i2c is
-- 
-- generic (addr_bits : integer);
-- 
-- port (
-- 	clk		: in std_logic;
-- 	reset	: in std_logic;
-- 
-- --
-- --	SimpCon IO interface
-- --
-- 	address		: in std_logic_vector(addr_bits-1 downto 0); 
-- 	sc_rd		: in std_logic;
-- 	sc_rd_data	: out std_logic_vector(31 downto 0); 
-- 	
-- 	sc_wr		: in std_logic;
-- 	sc_wr_data	: in std_logic_vector(31 downto 0);
-- 	
-- 	sc_rdy_cnt	: out unsigned(1 downto 0);
-- 	
-- 	-- I2C interface
-- 	sda	: inout std_logic;
-- 	scl : inout std_logic 
-- 
--  );
-- 
-- end sc_i2c;
-- 
-- 
-- architecture sc_i2c_rtl of sc_i2c is
-- 
-- 	constant CONTROL		: std_logic_vector(3 downto 0):= "0000" ;
-- 	constant STATUS			: std_logic_vector(3 downto 0):= "0001";
-- 	constant SLADD			: std_logic_vector(3 downto 0):= "0010" ;
-- 	constant TX_FIFO		: std_logic_vector(3 downto 0):= "0011" ;
-- 	constant RX_FIFO		: std_logic_vector(3 downto 0):= "0100" ;
-- 	constant T_CON			: std_logic_vector(3 downto 0):= "0101" ;
-- --	constant TX_FIFO_STATUS	: std_logic_vector(3 downto 0):= "0101" ;
-- --	constant RX_FIFO_STATUS	: std_logic_vector(3 downto 0):= "0110" ;
-- --	constant MESSAGE		: std_logic_vector(3 downto 0):= "0111" ;
-- 	
-- 	component i2c
-- 	port (clk             : in    std_logic;
-- 	      reset           : in    std_logic;
-- 	      sda             : inout std_logic;
-- 	      scl             : inout std_logic;
-- 	      device_addr     : in    std_logic_vector (6 downto 0);
-- 	      masl            : in    std_logic;
-- 	      strt            : in    std_logic;
-- 	      txrx            : in    std_logic;
-- 	      busy            : out   std_logic;
-- 	      t_const         : in    timming;
-- 	      tx_fifo_wr_ena  : in    std_logic;
-- 	      tx_fifo_full    : out   std_logic;
-- 	      data_in         : in    std_logic_vector (7 downto 0);
-- 	      tx_fifo_occ_in  : out   std_logic_vector (3 downto 0);
-- 	      tx_fifo_occ_out : out   std_logic_vector (3 downto 0);
-- 	      rx_fifo_rd_ena  : in    std_logic;
-- 	      rx_fifo_empty   : out   std_logic;
-- 	      data_out        : out   std_logic_vector (7 downto 0);
-- 	      rx_fifo_occ_in  : out   std_logic_vector (3 downto 0);
-- 	      rx_fifo_occ_out : out   std_logic_vector (3 downto 0));
-- 	end component i2c;
--  	
-- 	type state_type is (single, idle, multi);
-- 	signal state : state_type; 
-- 	signal sc_rdy_cnt_int : unsigned(1 downto 0); 
-- 	
-- 	signal control_reg 			: std_logic_vector(2 downto 0);
-- 	signal status_reg 			: std_logic_vector(18 downto 0);
-- 	signal sladd_reg 			: std_logic_vector(6 downto 0);
-- 	--signal tx_fifo_status_reg	: std_logic_vector(9 downto 0);
-- 	--signal rx_fifo_status_reg	: std_logic_vector(9 downto 0);
-- 	--signal message_size_reg		: std_logic_vector (3 downto 0);
-- 	
-- 
-- 	signal tx_fifo_wren_int: std_logic; 
-- 	signal rx_fifo_rden_int: std_logic;
-- 	
-- 	signal rx_fifo_rd_data_int: std_logic_vector (7 downto 0);
-- 	signal t_const_int: timming;
-- 	
-- begin
-- 
-- IIC: component i2c
-- 	port map (
-- 		clk             => clk,
-- 		reset           => reset,
-- 		sda             => sda,
-- 		scl             => scl,
-- 		device_addr     => sladd_reg,
-- 		masl            => control_reg(0),
-- 		strt            => control_reg(1),
-- 		txrx            => control_reg(2),
-- 		busy            => status_reg(0),
-- 		t_const			=> t_const_int,
-- 		tx_fifo_wr_ena  => tx_fifo_wren_int,
-- 		tx_fifo_full    => status_reg(1),
-- 		data_in         => sc_wr_data(7 downto 0),
-- 		tx_fifo_occ_in  => status_reg(6 downto 3),
-- 		tx_fifo_occ_out => status_reg(10 downto 7),
-- 		rx_fifo_rd_ena  => rx_fifo_rden_int,
-- 		rx_fifo_empty   => status_reg(2),
-- 		data_out        => rx_fifo_rd_data_int,
-- 		rx_fifo_occ_in  => status_reg(14 downto 11),
-- 		rx_fifo_occ_out => status_reg(18 downto 15)
-- 		);
-- 
-- 	sc_rdy_cnt <= sc_rdy_cnt_int;
-- 	sc_rdy_cnt_int <= "01" when (address(3 downto 0) = RX_FIFO) else "00";
-- 	tx_fifo_wren_int <= '1' when (sc_wr = '1') and (address(3 downto 0) = TX_FIFO)
-- 							else '0';
-- 	rx_fifo_rden_int <= '1' when (sc_rd = '1') and (address(3 downto 0) = RX_FIFO)
-- 							else '0';
-- 							
-- 	 
-- 	process(CLK,RESET)
-- 	
-- 	begin
-- 	
-- 		if RESET = '1' then
-- 			sc_rd_data <= (others => '0'); 
-- 			control_reg <= (others => '0'); 
-- 			sladd_reg <= (others => '0');
-- 			state <= single;
-- 			
-- 		elsif rising_edge(clk) then
-- 		
-- 			case state is
-- 			
-- 				when single =>
-- 				
-- 					-- read
-- 					if sc_rd = '1' then
-- 						if sc_rdy_cnt_int = "00" then 
-- 			
-- 							case address(3 downto 0) is 
-- 							when CONTROL =>
-- 								sc_rd_data(2 downto 0) <= control_reg;
-- 						 	 	sc_rd_data(31 downto 11) <= (others => '0');
-- 							when STATUS =>
-- 								sc_rd_data(18 downto 0) <= status_reg;
-- 								sc_rd_data(31 downto 19) <= (others => '0');
-- 							when SLADD =>
-- 								sc_rd_data(6 downto 0) <= sladd_reg;
-- 						 	 	sc_rd_data(31 downto 7) <= (others => '0');
-- --	 						when RX_FIFO =>
-- -- 								sc_rd_data(7 downto 0) <= rx_fifo_rd_data_int;
-- -- 								sc_rd_data(31 downto 8) <= (others => '0');
-- 							when others =>
-- 								sc_rd_data <= (others => '0');
-- 							end case;
-- 							
-- 							state <= single;
-- 						
-- 						else
-- 							-- Hack. For some reason, xilinx block ram uses one extra cycle
-- 							-- for the first memory read.
-- 							state <= idle;  
-- 						end if;
-- 						
-- 					elsif sc_wr = '1' then
-- 						if sc_rdy_cnt_int = "00" then
-- 							case address(3 downto 0) is
-- 								-- Status register is read only
-- 								when CONTROL =>
-- 									control_reg(2 downto 0) <= sc_wr_data(2 downto 0);
-- 								when SLADD =>
-- 									sladd_reg <= sc_wr_data(6 downto 0);
-- 								when T_CON => 
-- 									t_const_int.HOLD_START <= sc_wr_data(7 downto 0);
-- 									t_const_int.T_HIGH <= sc_wr_data(7 downto 0);
-- 									t_const_int.T_LOW <= sc_wr_data(7 downto 0);
-- 									t_const_int.T_SUSTO <= sc_wr_data(15 downto 8);
-- 									t_const_int.DELAY_STOP <= sc_wr_data(23 downto 16);
-- 									t_const_int.T_WAIT  <= sc_wr_data(31 downto 24); 
-- 								when others =>
-- 									null;
-- 							end case;
-- 							
-- 							state <= single;
-- 							
-- 						else
-- 							state <= idle;
-- 						end if;
-- 					
-- 					end if;
-- 					
-- 			when idle =>
-- 				state <= multi;
-- 				
-- 			when multi =>
-- 			
-- 				case address(3 downto 0) is
-- 					when RX_FIFO =>
-- 						sc_rd_data(7 downto 0) <= rx_fifo_rd_data_int;
-- 						sc_rd_data(31 downto 8) <= (others => '0');
-- 					when others =>
-- 						sc_rd_data <= (others => '0');
-- 				end case;
-- 					
-- 				
-- 				state <= single; 
-- 					
-- 				
-- 			end case;
-- 					
-- 		
-- 			-- write
-- -- 			if sc_wr = '1' then	
-- -- 
-- -- 			end if;
-- 		end if;
-- 	
-- 	end process;
-- 
-- end sc_i2c_rtl;

Library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;

use work.jop_types.all;
use work.sc_pack.all;
use work.jop_config.all;
use work.i2c_pkg.all;
use work.fifo_pkg.all;


entity sc_i2c is

generic (addr_bits : integer);

port (
	clk		: in std_logic;
	reset	: in std_logic;

	--	SimpCon IO interface
	address		: in std_logic_vector(addr_bits-1 downto 0); 
	sc_rd		: in std_logic;
	sc_rd_data	: out std_logic_vector(31 downto 0); 
	
	sc_wr		: in std_logic;
	sc_wr_data	: in std_logic_vector(31 downto 0);
	
	sc_rdy_cnt	: out unsigned(1 downto 0);
	
	-- I2C interface
	sda	: inout std_logic;
	scl : inout std_logic 

 );

end sc_i2c;


architecture sc_i2c_rtl of sc_i2c is

	--TODO Replace fixed number of bits in address length
	constant CONTROL		: std_logic_vector(3 downto 0):= "0000" ;
	constant STATUS			: std_logic_vector(3 downto 0):= "0001";
	constant SLADD			: std_logic_vector(3 downto 0):= "0010" ;
	constant TX_FIFO		: std_logic_vector(3 downto 0):= "0011" ;
	constant RX_FIFO		: std_logic_vector(3 downto 0):= "0100" ;
	constant T_CON			: std_logic_vector(3 downto 0):= "0101" ;
--	constant TX_FIFO_STATUS	: std_logic_vector(3 downto 0):= "0101" ;
--	constant RX_FIFO_STATUS	: std_logic_vector(3 downto 0):= "0110" ;
-- 	constant MESSAGE		: std_logic_vector(3 downto 0):= "0111" ;
	

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
			 enable : in std_logic;
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

	signal control_reg 			: std_logic_vector(9 downto 0);
	signal status_reg 			: std_logic_vector(25 downto 0);
	signal sladd_reg 			: std_logic_vector(6 downto 0);
	--signal tx_fifo_status_reg	: std_logic_vector(9 downto 0);
	--signal rx_fifo_status_reg	: std_logic_vector(9 downto 0);
-- 	signal message_size_reg		: std_logic_vector (3 downto 0);
	

	signal tx_fifo_wren_int: std_logic; 
	signal rx_fifo_rden_int: std_logic;
	
	signal rx_fifo_rd_data_int: std_logic_vector (7 downto 0);
	signal t_const_int: timming;
	
	signal busy_read: std_logic;
	signal address_reg: std_logic_vector (addr_bits-1 downto 0);
	signal sc_rd_reg: std_logic;
	signal sc_rd_reg_1: std_logic;
begin 

IIC: component i2c
	port map (
		clk             => clk,
		reset           => reset,
		flush_fifo      => control_reg(8),
		sda             => sda,
		scl             => scl,
		device_addr     => sladd_reg,
		masl            => control_reg(0),
		strt            => control_reg(1),
		txrx            => control_reg(2), 
		message_size    => control_reg(6 downto 3), 
	    rep_start       => control_reg(7),
	    reset_rep_start => status_reg(19),
	    enable  => control_reg(9),
		busy            => status_reg(0),
		tr_progress 	=> status_reg(20),
		transaction		=> status_reg(21),
		slave_addressed => status_reg(24),
		data_valid      => status_reg(25),
		t_const			=> t_const_int,
		tx_fifo_wr_ena  => tx_fifo_wren_int,
		tx_fifo_full    => status_reg(1),
		tx_fifo_empty   => status_reg(22),
		data_in         => sc_wr_data(7 downto 0),
		tx_fifo_occ_in  => status_reg(6 downto 3),
		tx_fifo_occ_out => status_reg(10 downto 7),
		rx_fifo_rd_ena  => rx_fifo_rden_int,
		rx_fifo_empty   => status_reg(2),
		rx_fifo_full    => status_reg(23),
		data_out        => rx_fifo_rd_data_int,
		rx_fifo_occ_in  => status_reg(14 downto 11), 
		rx_fifo_occ_out => status_reg(18 downto 15)
		);

	sc_rdy_cnt <= "01" when busy_read = '1' else "00";
	
	tx_fifo_wren_int <= '1' when (sc_wr = '1') and (address(3 downto 0) = TX_FIFO)
							else '0';
	rx_fifo_rden_int <= '1' when (sc_rd = '1') and (address(3 downto 0) = RX_FIFO)
							else '0';
							
	 
	process(CLK,RESET)
	
	begin
	
		if RESET = '1' then
			sc_rd_data <= (others => '0'); 
			control_reg <= (others => '0'); 
			sladd_reg <= (others => '0');
			address_reg <= (others => '0');
			sc_rd_reg <= '0';
			busy_read <= '0';
			sc_rd_reg_1 <= '0';
		
		elsif rising_edge(clk) then
		
			address_reg <= address(3 downto 0);
	
			-- read
			if sc_rd = '1' then
				case address(3 downto 0) is 
					
					when CONTROL =>
					busy_read <= '0';
					sc_rd_reg <= '0';
					sc_rd_reg_1 <= '0';
					sc_rd_data(9 downto 0) <= control_reg;
					sc_rd_data(31 downto 10) <= (others => '0');

					when STATUS =>
					busy_read <= '0';
					sc_rd_reg <= '0';
					sc_rd_reg_1 <= '0';
					sc_rd_data(25 downto 0) <= status_reg;
					sc_rd_data(31 downto 26) <= (others => '0');

					when SLADD =>
					busy_read <= '0';
					sc_rd_reg <= '0';
					sc_rd_reg_1 <= '0';
					sc_rd_data(6 downto 0) <= sladd_reg;
			 	 	sc_rd_data(31 downto 7) <= (others => '0');

					when RX_FIFO =>
					busy_read <= '1';
					sc_rd_reg <= '1';
					sc_rd_reg_1 <= '0';

					when others =>
					busy_read <= '0';
					sc_rd_reg <= '0';
					sc_rd_reg_1 <= '0'; 
					sc_rd_data <= (others => '0');
					
				end case;

			elsif sc_rd_reg = '1' then
				busy_read <= '1';
				sc_rd_reg <= '0';
				sc_rd_reg_1 <= '1';

			elsif sc_rd_reg_1 = '1' then
				busy_read <= '0';
				sc_rd_reg <= '0';
				sc_rd_reg_1 <= '0';

				sc_rd_data(7 downto 0) <= rx_fifo_rd_data_int;
				sc_rd_data(31 downto 8) <= (others => '0');
							
			elsif sc_wr = '1' then
				
				busy_read <= '0';
				sc_rd_reg <= '0';
			
				case address(3 downto 0) is
				
					when CONTROL =>
					control_reg(9 downto 0) <= sc_wr_data(9 downto 0);
	
					when SLADD =>
					sladd_reg <= sc_wr_data(6 downto 0);
	
					when T_CON => 
					t_const_int.HOLD_START <= sc_wr_data(7 downto 0);
					t_const_int.T_HIGH <= sc_wr_data(7 downto 0);
					t_const_int.T_LOW <= sc_wr_data(7 downto 0);
					t_const_int.T_SUSTO <= sc_wr_data(15 downto 8);
					t_const_int.DELAY_STOP <= sc_wr_data(23 downto 16);
					t_const_int.T_WAIT  <= sc_wr_data(31 downto 24);
	
					when others =>
					null;
	
				end case;
					
			end if;

		end if;
	
	end process;

end sc_i2c_rtl;

