--! @file dual_port_ram.vhd
--! @brief A parameterized, inferable, true dual-port, dual-clock block RAM in VHDL.
--! @details 
--! @author    Juan Ricardo Rios, jrri@imm.dtu.dk
--! @version   
--! @date      2011-2012
--! @copyright GNU Public License.

library ieee;
use ieee.std_logic_1164.all;
--use ieee.std_logic_unsigned.all;
use ieee.numeric_std.all;
use work.fifo_pkg.all;

entity dual_port_ram is

 generic(WORD_SIZE: integer := DATA_SIZE;
 		 ADDRESS: integer := 3);

  port (
    wclk        : in  std_logic;
    rclk        : in  std_logic;
    data          : in  std_logic_vector (WORD_SIZE-1 downto 0);
    write_address : in  std_logic_vector(ADDRESS-1 downto 0);
    read_address  : in  std_logic_vector(ADDRESS-1 downto 0);
    we            : in  std_logic;
    re            : in  std_logic;
    q             : out std_logic_vector (WORD_SIZE-1 downto 0)
    );

end dual_port_ram;

architecture dual_ram_arch of dual_port_ram is

  type   MEM is array((2**ADDRESS)-1 downto 0) of std_logic_vector(DATA_SIZE-1 downto 0);
  signal ram_block        : MEM;
  signal read_address_reg : std_logic_vector(ADDRESS-1 downto 0);

begin

  process (wclk)
  
  begin
  
    if (wclk'event and wclk = '1') then
    
      if (we = '1') then
        ram_block(to_integer(unsigned(write_address))) <= data;
      end if;
  
    end if;
  
  end process;

  process (rclk)
  
  begin
  
    if (rclk'event and rclk = '1') then
    
		read_address_reg <= read_address;

      	if (re = '1') then
	    	q <= ram_block(to_integer(unsigned(read_address_reg)));
   		end if;
    
    end if;
  
  end process;

end dual_ram_arch;

--entity dual_port_ram is
--
--port (
--    -- Port A
--    a_clk   : in  std_logic;
--    a_wr    : in  std_logic;
--    a_rd      : in  std_logic;
--    a_addr  : in  std_logic_vector(ADDRESS-1 downto 0);
--    a_din   : in  std_logic_vector(DATA_SIZE-1 downto 0);
--    a_dout  : out std_logic_vector(DATA_SIZE-1 downto 0);
--           
--    -- Port B
--    b_clk   : in  std_logic;
--    b_wr    : in  std_logic;
--    b_rd      : in  std_logic;
--    b_addr  : in  std_logic_vector(ADDRESS-1 downto 0);
--    b_din   : in  std_logic_vector(DATA_SIZE-1 downto 0);
--    b_dout  : out std_logic_vector(DATA_SIZE-1 downto 0)
--);
--end dual_port_ram;
-- 
--architecture rtl of dual_port_ram is
--    -- Shared memory
--    type mem_type is array ( (2**ADDRESS)-1 downto 0 ) of std_logic_vector(DATA_SIZE-1 downto 0);
--    shared variable mem : mem_type;
--      begin
--       
--      -- Port A
--      process(a_clk)
--      begin
--          if(a_clk'event and a_clk='1') then
--              
--              --if(a_rd='1') then
--              
--              if(a_wr='1') then
--                  mem(conv_integer(a_addr)) := a_din;
--              end if;
--              
----            if(a_rd='1') then
--                      a_dout <= mem(conv_integer(a_addr));
--             -- end if;
--              
--          end if;
--      end process;
--       
--      -- Port B
--      process(b_clk)
--      begin
--          if(b_clk'event and b_clk='1') then
--
--              --if(b_rd='1') then
--              
--              if(b_wr='1') then
--                  mem(conv_integer(b_addr)) := b_din;
--              end if;
--
----            if(b_rd='1') then
--                      b_dout <= mem(conv_integer(b_addr));
--                      --end if;                       
--                      
--          end if;
--      end process;
--       
--      end rtl;

