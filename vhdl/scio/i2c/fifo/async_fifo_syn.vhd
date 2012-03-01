--
-- VHDL Architecture async_fifo_lib.async_fifo.behavioral
--
-- Created:
--          by - Juan.UNKNOWN (WIN32)
--          at - 13:25:43 09/18/2009
--
--
--
LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.std_logic_arith.all;
USE work.fifo_pkg.all;

ENTITY async_fifo IS
  
  port (reset         : in std_logic;
        wclk          : in std_logic; 
        rclk          : in std_logic;
        write_enable  : in std_logic;
        read_enable   : in std_logic;
        fifo_occu_in  : out std_logic_vector(FIFO_ADD_WIDTH-1 downto 0);
        fifo_occu_out : out std_logic_vector(FIFO_ADD_WIDTH-1 downto 0);
        write_data_in : in std_logic_vector(7 downto 0);
        read_data_out : out std_logic_vector(7 downto 0);
        full          : out std_logic; -- not in the original specification
        empty         : out std_logic  -- but very usefull. 
        );
  
END ENTITY async_fifo;

--
ARCHITECTURE synth OF async_fifo IS

-- signal int_rptr_sync: std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
-- signal int_wptr_sync: std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
signal int_wptr: std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
signal int_rptr: std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
signal int_waddress: std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
signal int_raddress: std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
signal int_ren: std_logic;
--signal int_wen: std_logic;
signal int_wen: std_logic_vector(0 downto 0);  

COMPONENT FIFO_write_control IS
  
port (wclk           : in std_logic;
      reset          : in std_logic;
      write_enable   : in std_logic;
      rptr_sync      : in std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
      fifo_occu_in   : out std_logic_vector((FIFO_ADD_WIDTH - 1) downto 0);
      full           : out std_logic;
      wptr           : out std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
      waddr          : out std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
      wen            : out std_logic_vector(0 downto 0)
      --wen            : out std_logic
      );
        
END COMPONENT;

COMPONENT FIFO_read_control IS

port (rclk           : in std_logic;
      reset          : in std_logic;
      read_enable    : in std_logic;
      wptr_sync      : in std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
      fifo_occu_out  : out std_logic_vector((FIFO_ADD_WIDTH - 1) downto 0);
      empty          : out std_logic;
      rptr           : out std_logic_vector ((FIFO_ADD_WIDTH) downto 0);
      raddr          : out std_logic_vector ((FIFO_ADD_WIDTH - 1) downto 0);
      ren            : out std_logic
      );

END COMPONENT;

-- COMPONENT synchronizer IS
--   
--   port(clk_1    : in std_logic;
--        clk_2    : in std_logic;
--        reset    : in std_logic;
--        ptr      : in std_logic_vector((FIFO_ADD_WIDTH) downto 0);
--        sync_ptr : out std_logic_vector((FIFO_ADD_WIDTH) downto 0)
--        );
--         
-- END COMPONENT;

-- component dual_port_ram_alt
-- 	port (data      : in  STD_LOGIC_VECTOR (7 DOWNTO 0);
-- 	      rdaddress : in  STD_LOGIC_VECTOR (3 DOWNTO 0);
-- 	      rdclock   : in  STD_LOGIC;
-- 	      rden      : in  STD_LOGIC := '1';
-- 	      wraddress : in  STD_LOGIC_VECTOR (3 DOWNTO 0);
-- 	      wrclock   : in  STD_LOGIC;
-- 	      wren      : in  STD_LOGIC := '1';
-- 	      q         : out STD_LOGIC_VECTOR (7 DOWNTO 0));
-- 	end component dual_port_ram_alt;


component dual_port_ram_xil 
	port (clka  : in  std_logic;
	      wea   : in  std_logic_vector (0 downto 0);
	      addra : in  std_logic_vector (3 downto 0);
	      dina  : in  std_logic_vector (7 downto 0);
	      clkb  : in  std_logic;
	      enb   : in  std_logic;
	      addrb : in  std_logic_vector (3 downto 0);
	      doutb : out std_logic_vector (7 downto 0));
	end component dual_port_ram_xil;

BEGIN
-----------------------------------------------------------------------------------
WR_CTRL: FIFO_write_control  

      port map(wclk           => wclk,
               reset          => reset,
               write_enable   => write_enable,
               rptr_sync      => int_rptr,
               fifo_occu_in   => fifo_occu_in,
               full           => full,-- what to do whith this??
               wptr           => int_wptr,
               waddr          => int_waddress,
               wen            => int_wen
               );
-----------------------------------------------------------------------------------
RD_CTRL: FIFO_read_control

      port map (rclk           =>  rclk,
                reset          =>  reset,
                read_enable    =>  read_enable,
                wptr_sync      =>  int_wptr,
                fifo_occu_out  =>  fifo_occu_out,
                empty          =>  empty,-- what to do with this??
                rptr           =>  int_rptr,
                raddr          =>  int_raddress,
                ren            =>  int_ren
                );
-----------------------------------------------------------------------------------
-- WR_SYNC: synchronizer
--   
--       port map (clk_1    =>  wclk,
--                 clk_2    =>  rclk,
--                 reset    =>  reset,
--                 ptr      =>  int_wptr,
--                 sync_ptr =>  int_wptr_sync
--                 );
-----------------------------------------------------------------------------------
-- RD_SYNC: synchronizer
--           
--        port map (clk_1    =>  rclk,
--                  clk_2    =>  wclk,
--                  reset    =>  reset,
--                  ptr      =>  int_rptr,
--                  sync_ptr =>  int_rptr_sync
--                  );
-----------------------------------------------------------------------------------
-- MEM: dual_port_ram_alt 
-- 
--        port map (data		=>  write_data_in,
--                  rdaddress	=> int_raddress,
--                  rdclock	=>  rclk,
--                  rden		=>  int_ren,
--                  wraddress	=>  int_waddress,
--                  wrclock	=>  wclk,
--                  wren	  	=>  int_wen,
--                  q		    =>  read_data_out
--                  );

MEM: component dual_port_ram_xil

	port map (
		clka  => wclk,
		wea   => int_wen,
		addra => int_waddress,
		dina  => write_data_in,
		clkb  => rclk,
		enb   => int_ren,
		addrb => int_raddress,
		doutb => read_data_out 
		);
------------------------------------------------------------------------------------

END ARCHITECTURE synth;


