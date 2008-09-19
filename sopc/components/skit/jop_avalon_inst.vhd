--Legal Notice: (C)2007 Altera Corporation. All rights reserved.  Your
--use of Altera Corporation's design tools, logic functions and other
--software and tools, and its AMPP partner logic functions, and any
--output files any of the foregoing (including device programming or
--simulation files), and any associated documentation or information are
--expressly subject to the terms and conditions of the Altera Program
--License Subscription Agreement or other applicable license agreement,
--including, without limitation, that your use is for the sole purpose
--of programming logic devices manufactured by Altera and sold by Altera
--or its authorized distributors.  Please refer to the applicable
--agreement for further details.


-- turn off superfluous VHDL processor warnings 
-- altera message_level Level1 
-- altera message_off 10034 10035 10036 10037 10230 10240 10030 

library altera;
use altera.altera_europa_support_lib.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_unsigned.all;

entity jop_avalon_inst is 
        port (
              -- inputs:
                 signal clk : IN STD_LOGIC;
                 signal readdata : IN STD_LOGIC_VECTOR (31 DOWNTO 0);
                 signal reset : IN STD_LOGIC;
                 signal ser_rxd : IN STD_LOGIC;
                 signal waitrequest : IN STD_LOGIC;

              -- outputs:
                 signal address : OUT STD_LOGIC_VECTOR (25 DOWNTO 0);
                 signal byteenable : OUT STD_LOGIC_VECTOR (3 DOWNTO 0);
                 signal read : OUT STD_LOGIC;
                 signal ser_txd : OUT STD_LOGIC;
                 signal wd : OUT STD_LOGIC;
                 signal write : OUT STD_LOGIC;
                 signal writedata : OUT STD_LOGIC_VECTOR (31 DOWNTO 0)
              );
end entity jop_avalon_inst;


architecture europa of jop_avalon_inst is
component jop_avalon is 
           generic (
                    addr_bits : integer := 24;
                    block_bits : integer := 4;
                    jpc_width : integer := 12
                    );
           port (
                 -- inputs:
                    signal clk : IN STD_LOGIC;
                    signal readdata : IN STD_LOGIC_VECTOR (31 DOWNTO 0);
                    signal reset : IN STD_LOGIC;
                    signal ser_rxd : IN STD_LOGIC;
                    signal waitrequest : IN STD_LOGIC;

                 -- outputs:
                    signal address : OUT STD_LOGIC_VECTOR (25 DOWNTO 0);
                    signal byteenable : OUT STD_LOGIC_VECTOR (3 DOWNTO 0);
                    signal read : OUT STD_LOGIC;
                    signal ser_txd : OUT STD_LOGIC;
                    signal wd : OUT STD_LOGIC;
                    signal write : OUT STD_LOGIC;
                    signal writedata : OUT STD_LOGIC_VECTOR (31 DOWNTO 0)
                 );
end component jop_avalon;

                signal internal_address :  STD_LOGIC_VECTOR (25 DOWNTO 0);
                signal internal_byteenable :  STD_LOGIC_VECTOR (3 DOWNTO 0);
                signal internal_read :  STD_LOGIC;
                signal internal_ser_txd :  STD_LOGIC;
                signal internal_wd :  STD_LOGIC;
                signal internal_write :  STD_LOGIC;
                signal internal_writedata :  STD_LOGIC_VECTOR (31 DOWNTO 0);

begin

  --the_jop_avalon, which is an e_instance
  the_jop_avalon : jop_avalon
    generic map(
      addr_bits => 24,
      block_bits => 4,
      jpc_width => 12
    )
    port map(
      address => internal_address,
      byteenable => internal_byteenable,
      read => internal_read,
      ser_txd => internal_ser_txd,
      wd => internal_wd,
      write => internal_write,
      writedata => internal_writedata,
      clk => clk,
      readdata => readdata,
      reset => reset,
      ser_rxd => ser_rxd,
      waitrequest => waitrequest
    );


  --vhdl renameroo for output signals
  address <= internal_address;
  --vhdl renameroo for output signals
  byteenable <= internal_byteenable;
  --vhdl renameroo for output signals
  read <= internal_read;
  --vhdl renameroo for output signals
  ser_txd <= internal_ser_txd;
  --vhdl renameroo for output signals
  wd <= internal_wd;
  --vhdl renameroo for output signals
  write <= internal_write;
  --vhdl renameroo for output signals
  writedata <= internal_writedata;

end europa;

