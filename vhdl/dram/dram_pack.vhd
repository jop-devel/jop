library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

package dram_pack is
  constant DRAM_ADDR_WIDTH : integer := 12;
  constant DRAM_DATA_WIDTH : integer := 16;

  subtype dram_data_type is std_logic_vector(DRAM_DATA_WIDTH-1 downto 0);

  type dram_ctrl_type is record
    clk  : std_logic;

    cs_n : std_logic;    
    cke  : std_logic;

    dqm : std_logic_vector(1 downto 0);

    ras_n : std_logic;
    cas_n : std_logic;
    we_n : std_logic;
    
    addr : std_logic_vector(DRAM_ADDR_WIDTH-1 downto 0);
    ba   : std_logic_vector(1 downto 0);

    debug : std_logic_vector(1 downto 0);
  end record;

  type dram_pll_type is record
    clk : std_logic;
    clk_skew : std_logic;
    locked : std_logic;
  end record;

  type dram_cfg_type is record
                          ADDR_WIDTH : integer;
                          DATA_WIDTH : integer;
                        end record;
  
--  type sc__array_type is array (integer range <>) of sc__type;

end dram_pack;
