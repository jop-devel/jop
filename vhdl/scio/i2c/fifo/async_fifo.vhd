--! @file fifo.vhd
--! @brief Synchronous or asynchronous fifo buffer  
--! @details 
--! @author    Juan Ricardo Rios, jrri@imm.dtu.dk
--! @version   
--! @date      2009-2012
--! @copyright GNU Public License.

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
USE work.fifo_pkg.all;

ENTITY async_fifo IS

	generic(asynch: string:= "TRUE";	--! If set to TRUE, the fifo works in async mode (a synchronizer block is generated)
  			 ADDRESS  	: integer  := 3;
  			 DATA_SIZE : integer := 8);
  			 
  port (reset         : in std_logic;
        wclk          : in std_logic; 
        rclk          : in std_logic;
        
        fifo_in : in fifo_in_type;
        fifo_out : out fifo_out_type
        
        );
  
END ENTITY async_fifo;

--
ARCHITECTURE fifo_arch OF async_fifo IS

signal int_rptr_sync: std_logic_vector ((ADDRESS) downto 0);
signal int_wptr_sync: std_logic_vector ((ADDRESS) downto 0);
signal int_wptr: std_logic_vector ((ADDRESS) downto 0);
signal int_rptr: std_logic_vector ((ADDRESS) downto 0);
signal int_waddress: std_logic_vector ((ADDRESS - 1) downto 0);
signal int_raddress: std_logic_vector ((ADDRESS - 1) downto 0);
signal int_ren: std_logic;
signal int_wen: std_logic;

BEGIN

WR_CTRL: entity work.write_control
	
	generic map (ADDRESS => ADDRESS)

	port map(wclk         => wclk,
		     reset        => reset,
		     write_enable => fifo_in.write_enable,
		     rptr_sync    => int_rptr_sync,
		     flush => fifo_in.flush,
		     occupancy_wr   => fifo_out.occupancy_wr,
		     full         => fifo_out.full,
		     wptr         => int_wptr,
		     waddr        => int_waddress,
		     wen          => int_wen);

RD_CTRL: entity work.read_control

	generic map (ADDRESS => ADDRESS)

	port map(rclk        => rclk,
		     reset       => reset,
		     read_enable => fifo_in.read_enable,
		     wptr_sync   => int_wptr_sync,
		     flush => fifo_in.flush,
		     occupancy_rd   => fifo_out.occupancy_rd,
		     empty       => fifo_out.empty,
		     rptr        => int_rptr,
		     raddr       => int_raddress,
		     ren         => int_ren);

MEM:  entity work.dual_port_ram
 	generic map (WORD_SIZE  => DATA_SIZE,
 		 		 ADDRESS  => ADDRESS)

	port map(wclk        => wclk,
		     rclk        => rclk,
		     data          => fifo_in.write_data_in,
		     write_address => int_waddress,
		     read_address  => int_raddress,
		     we            => int_wen,
		     re			 => int_ren,
		     q             => fifo_out.read_data_out);

ASYNC: if asynch = "TRUE" generate
		WR_SYNC: entity work.synchronizer
		
			generic map (ADDRESS => ADDRESS)
		
			port map(clk_1    => wclk,
				     clk_2    => rclk,
				     reset    => reset,
				     ptr      => int_wptr,
				     sync_ptr => int_wptr_sync);
				     
		RD_SYNC: entity work.synchronizer
		
			generic map (ADDRESS => ADDRESS)
		
			port map(clk_1    => rclk,
				     clk_2    => wclk,
				     reset    => reset,
				     ptr      => int_rptr,
				     sync_ptr => int_rptr_sync);
				     
		end generate;
		
SYNC:	if asynch = "FALSE" generate
			int_wptr_sync <= int_wptr;
			int_rptr_sync <= int_rptr;
		end generate;


END ARCHITECTURE fifo_arch;