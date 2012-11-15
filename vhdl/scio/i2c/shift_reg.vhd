--! @file shift_reg.vhd
--! @brief A simple shift register, parallel in, parallel out with load  
--! @details 
--! @author    Juan Ricardo Rios, jrri@imm.dtu.dk
--! @version   
--! @date      2011-2012
--! @copyright GNU Public License.

library IEEE;
use IEEE.std_logic_1164.all;
use ieee.numeric_std.all;
use work.i2c_pkg.all;


entity shift_reg is

  generic(SIZE : integer;
  		  EDGE : std_logic
  			
  );

  port (

    clk : in std_logic;
    rst : in std_logic;

    shift_in  : in  std_logic;
    shift_out : out std_logic;

    data_in   : in std_logic_vector(SIZE-1 downto 0);
    data_out : out std_logic_vector(SIZE-1 downto 0);
    load   : in std_logic;
    enable : in std_logic

    );

end entity;

architecture sh_reg_arch of shift_reg is
  
  signal data_reg : std_logic_vector(SIZE-1 downto 0);
  
begin

	shift_out <= data_reg(SIZE-1);
	data_out <= data_reg;
  
  process(clk, rst)

  begin

    if rst = RESET_LEVEL then

      data_reg <= (others => '0');
      
    elsif clk'event and clk = EDGE then
      
      if load = '1' then
        data_reg <= data_in;
      elsif enable = '1' then
        data_reg <= data_reg(SIZE-2 downto 0) & shift_in;
      end if;
      
    end if;
    
  end process;
  
end architecture sh_reg_arch;