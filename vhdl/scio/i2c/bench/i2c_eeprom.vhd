--==========================================================================================================-- 
--                                                                                                          -- 
--                         Copyright (c) 2002 by IBM , Inc.   All rights reserved.                          -- 
--                                                                                                          -- 
--                      Martin Neumann mneumann@de.ibm.com - IBM EF Boeblingen GERMANY                      -- 
--                                                                                                          -- 
--==========================================================================================================-- 
--  File name: I2C_EEPROM.vhd                                                                               -- 
--  Designer : M. Neumann                                                                                   -- 
--  Description:  Simulation of a series 24Cxx I2C EEPROM (24C01, 24C02, 24C04, 24C08 and 24C16)            -- 
--                                                                                                          -- 
--==========================================================================================================-- 
 
library IEEE; 
  use IEEE.std_logic_1164.all; 
  use IEEE.std_logic_unsigned.all;   -- function conv_integer 
 
entity I2C_EEPROM is 
 generic (device : string(1 to 5) := "24C16");  --select from 24C16, 24C08, 24C04, 24C02 and 24C01 
 port ( 
  STRETCH            : IN    time := 1 ns;      --pull SCL low for this time-value; 
  E0                 : IN    std_logic := 'L';  --leave unconnected for 24C16, 24C08, 24C04 
  E1                 : IN    std_logic := 'L';  --leave unconnected for 24C16, 24C08 
  E2                 : IN    std_logic := 'L';  --leave unconnected for 24C16 
  WC                 : IN    std_logic := 'L';  --tie high to disable write mode 
  SCL                : INOUT std_logic; 
  SDA                : INOUT std_logic 
); 
END I2C_EEPROM; 
 
 
--==========================================================================================================-- 
 
architecture SIM of I2C_EEPROM is 
 
   type      memory_array  is array(0 to 2047) of std_logic_vector(7 downto 0); 
   type      I2C_STATE     is (IDLE, ID_CODE, ID_ACK, WR_ADDR, AR_ACK, WR_DATA, WR_ACK, RD_DATA, RD_ACK); 
 
   function addr_bits(name : STRING(1 to 5)) return positive is 
   begin 
     if    device="24C16" then return 11; 
     elsif device="24C08" then return 10; 
     elsif device="24C04" then return  9; 
     elsif device="24C02" then return  8; 
     else                      return  7; 
     end if; 
   end addr_bits; 
 
 
   constant  ADDR_MSB      : positive := addr_bits(device) -1; 
   signal    ADDR_WORD     : std_logic_vector(10 downto 0); 
   signal    BIT_PTR       : natural; 
   signal    MEM_ADDR      : natural; 
   signal    MEM_DATA      : memory_array; 
   signal    THIS_STATE    : I2C_STATE; 
   signal    NEXT_STATE    : I2C_STATE; 
   signal    SCL_IN        : std_logic; 
   signal    SCL_OLD       : std_logic; 
   signal    SCL_OUT       : std_logic; 
   signal    SDA_IN        : std_logic; 
   signal    SDA_OUT       : std_logic; 
   signal    START_DET     : std_logic; 
   signal    STOP_DET      : std_logic; 
   signal    DEVICE_SEL    : std_logic; 
   signal    RD_MODE       : std_logic; 
   signal    WRITE_EN      : std_logic; 
   signal    RECV_BYTE     : std_logic_vector(7 downto 0); 
   signal    XMIT_BYTE     : std_logic_vector(7 downto 0); 
 
begin 
 
  SCL_IN <= '1' when SCL='1' or SCL='H' else 
            '0' when SCL='0' else 'X'; 
 
  SDA_IN <= '1' when SDA='1' or SDA='H' else 
            '0' when SDa='0' else 'X'; 
 
  SCL_OLD <= SCL_IN after 10 ns; 
 
  p_START_DET : 
  process (SCL_IN, SCL_OLD, SDA_IN, THIS_STATE) 
  begin 
    if SDA_IN'event and SDA_IN ='0' and SCL_IN ='1' and SCL_OLD ='1' then 
      START_DET <= '1'; 
    elsif SCL_IN'event and SCL_IN ='1' and THIS_STATE=ID_CODE then 
      START_DET <='0'; 
    end if; 
  end process; 
 
  p_STOP_DET : 
  process (SCL_IN, SCL_OLD, SDA_IN, THIS_STATE) 
  begin 
    if SDA_IN'event and SDA_IN ='1' and SCL_IN ='1' and SCL_OLD ='1' then 
      STOP_DET  <= '1'; 
    elsif THIS_STATE =IDLE then 
      STOP_DET  <='0' after 30 ns; 
    end if; 
  end process; 
 
  p_THIS_STATE : 
  process (STOP_DET, SCL_IN) 
  begin 
    if (STOP_DET ='1') then 
      THIS_STATE <= IDLE; 
    elsif SCL_IN'event and SCL_IN='0' then 
      THIS_STATE <= NEXT_STATE; 
    end if; 
  end process; 
 
  p_NEXT_STATE : 
  process(START_DET, THIS_STATE, BIT_PTR, SDA_IN, DEVICE_SEL, RD_MODE) 
  begin 
    if START_DET ='1'                         then NEXT_STATE <= ID_CODE; 
    else 
      case THIS_STATE is 
        when ID_CODE => if    (BIT_PTR > 0)   then NEXT_STATE <= ID_CODE; 
                        else                       NEXT_STATE <= ID_ACK; 
                        end if; 
        when ID_ACK  => if    (DEVICE_SEL='1' 
                           and RD_MODE='1')   then NEXT_STATE <= RD_DATA; 
                        elsif (DEVICE_SEL='1' 
                           and RD_MODE='0')   then NEXT_STATE <= WR_ADDR; 
                        else                       NEXT_STATE <= IDLE;
                        end if; 
        when WR_ADDR => if    (BIT_PTR > 0)   then NEXT_STATE <= WR_ADDR; 
                        else                       NEXT_STATE <= AR_ACK; 
                        end if; 
        when AR_ACK  =>                            NEXT_STATE <= WR_DATA; 
        when WR_DATA => if    (BIT_PTR > 0)   then NEXT_STATE <= WR_DATA; 
                        else                       NEXT_STATE <= WR_ACK; 
                        end if; 
        when WR_ACK  =>                            NEXT_STATE <= WR_DATA; 
        when RD_DATA => if    (BIT_PTR > 0)   then NEXT_STATE <= RD_DATA; 
                        else                       NEXT_STATE <= RD_ACK; 
                        end if; 
        when RD_ACK  => if    (SDA_IN ='0')   then NEXT_STATE <= RD_DATA; 
                        else                       NEXT_STATE <= IDLE; 
                        end if; 
        when others  =>                            NEXT_STATE <= IDLE; 
      end case; 
    end if; 
  end process; 
 
  RECV_BYTE(0) <= SDA_IN; 
 
  p_RECV_BYTE : 
  process begin wait until SCL_IN'event and SCL_IN ='0'; 
    RECV_BYTE(7 downto 1) <= RECV_BYTE(6 downto 0); 
  end process; 
 
  p_RD_MODE : 
  process begin wait until SCL_IN'event and SCL_IN ='0'; 
    if NEXT_STATE=ID_ACK then 
      RD_MODE <= RECV_BYTE(0); 
    end if; 
  end process; 
 
  p_DEVICE_SEL : 
  process begin wait until SCL_IN'event and SCL_IN ='0'; 
    if NEXT_STATE=ID_ACK then 
      if (device="24C16" and RECV_BYTE(7 downto 4)="1010") 
      or (device="24C08" and RECV_BYTE(7 downto 4)="1010" and E2      =RECV_BYTE(3)) 
      or (device="24C04" and RECV_BYTE(7 downto 4)="1010" and E2&E1   =RECV_BYTE(3 downto 2)) 
      or (device="24C02" and RECV_BYTE(7 downto 4)="1010" and E2&E1&E0=RECV_BYTE(3 downto 1)) 
      or (device="24C01" and RECV_BYTE(7 downto 4)="1010" and E2&E1&E0=RECV_BYTE(3 downto 1)) then 
        DEVICE_SEL <= '1'; 
      end if; 
    else 
      DEVICE_SEL <= '0'; 
    end if; 
  end process; 
 
  WRITE_EN <= '1' when WC='0' or WC='L' else '0'; 
 
  with THIS_STATE select 
    SDA_OUT <= XMIT_BYTE(BIT_PTR) after 30 ns when RD_DATA, 
               not DEVICE_SEL     after 30 ns when ID_ACK, 
               '0'                after 30 ns when AR_ACK, 
               not WRITE_EN       after 30 ns when WR_ACK, 
               'Z'                after 30 ns when others; 
 
  SDA <= '0' when SDA_OUT = '0' else 'Z'; 
 
  p_SCL_OUT : 
  process begin 
  SCL_OUT <= 'Z'; 
  wait until SCL_IN'event and SCL_IN ='0'; 
    SCL_OUT <= '0'; 
    wait for STRETCH; 
    SCL_OUT <= 'Z'; 
  end process; 
 
  SCL <= SCL_OUT; 
 
  p_BIT_PTR : 
  process(SCL_IN, START_DET) 
  begin 
    if START_DET ='1' then 
      BIT_PTR <= 7; 
    elsif SCL_IN'event and SCL_IN ='0' then 
      if    NEXT_STATE=ID_ACK  or NEXT_STATE=AR_ACK  or NEXT_STATE=WR_ACK  or NEXT_STATE=RD_ACK  then 
        BIT_PTR <= 8; 
      elsif NEXT_STATE=ID_CODE or NEXT_STATE=WR_ADDR or NEXT_STATE=WR_DATA or NEXT_STATE=RD_DATA then 
        BIT_PTR <= BIT_PTR -1; 
      end if; 
    end if; 
  end process;  
 
  ADDR_WORD(7 downto 0) <= RECV_BYTE; 
 
  p_MEM_ADDR : 
  process 
   constant ADDR_MAX : positive := 2**(ADDR_MSB+1) -1; 
  begin 
    wait until SCL_IN'event and SCL_IN='0'; 
    if NEXT_STATE = ID_ACK then 
      ADDR_WORD(10 downto 8) <= RECV_BYTE(3 downto 1); 
    end if; 
    if NEXT_STATE = AR_ACK then 
      MEM_ADDR <= conv_integer(ADDR_WORD(ADDR_MSB downto 0)); 
    elsif (NEXT_STATE=RD_ACK) then 
      if MEM_ADDR = ADDR_MAX then 
        MEM_ADDR <= 0; 
      else 
        MEM_ADDR <= MEM_ADDR +1; 
      end if; 
    elsif (THIS_STATE=WR_ACK and SDA_IN='0') then        -- update Wr-pointer after writing completed 
      if MEM_ADDR MOD 16 = 15 then      -- Page max ! 
        MEM_ADDR <= MEM_ADDR -15; 
      else 
        MEM_ADDR <= MEM_ADDR +1; 
      end if; 
    end if; 
  end process; 
 
  p_MEM_WR : 
  process begin wait until SCL_IN'event and SCL_IN='0'; 
    if  NEXT_STATE=WR_ACK and WRITE_EN='1' then 
      MEM_DATA(MEM_ADDR) <= RECV_BYTE; 
    end if; 
  end process; 
 
  XMIT_BYTE <= MEM_DATA(MEM_ADDR); 
 
--==========================================================================================================-- 
 
end SIM; 
 
   configuration CFG_I2C_EEPROM of I2C_EEPROM is 
    for SIM 
    end for; 
   end CFG_I2C_EEPROM; 
--========================================= END OF I2C_EEPROM ===============================================-- 
