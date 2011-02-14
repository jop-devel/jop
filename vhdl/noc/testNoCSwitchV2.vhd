--------------------------------------------------------------------------------
-- Company: 
-- Engineer:
--
-- Create Date:   16:29:25 02/10/2011
-- Design Name:   
-- Module Name:   C:/ISE_projects/JOP/jop/vhdl/paper/nexys2_csp/testNoCSwitchV2.vhd
-- Project Name:  jop
-- Target Device:  
-- Tool versions:  
-- Description:   
-- 
-- VHDL Test Bench Created by ISE for module: NoCSwitchV2
-- 
-- Dependencies:
-- 
-- Revision:
-- Revision 0.01 - File Created
-- Additional Comments:
--
-- Notes: 
-- This testbench has been automatically generated using types std_logic and
-- std_logic_vector for the ports of the unit under test.  Xilinx recommends
-- that these types always be used for the top-level I/O of a design in order
-- to guarantee that the testbench will bind correctly to the post-implementation 
-- simulation model.
--------------------------------------------------------------------------------
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
 
-- Uncomment the following library declaration if using
-- arithmetic functions with Signed or Unsigned values
--USE ieee.numeric_std.ALL;

use work.NoCTypes.ALL; 

 
ENTITY testNoCSwitchV2 IS
END testNoCSwitchV2;
 
ARCHITECTURE behavior OF testNoCSwitchV2 IS 
 
    -- Component Declaration for the Unit Under Test (UUT)
 
    COMPONENT NoCSwitchV2
    PORT(
         ClkA : IN  std_logic;
         ClkB : IN  std_logic;
         Rst : IN  std_logic;
         nocAIn : IN  NoCPacket;
         nocAOut : OUT  NoCPacket;
         nocBIn : IN  NoCPacket;
         nocBOut : OUT  NoCPacket
        );
    END COMPONENT;
    

   --Inputs
   signal ClkA : std_logic := '0';
   signal ClkB : std_logic := '0';
   signal Rst : std_logic := '0';
   signal nocAIn : NoCPacket := (Src => "111", Dst => (others => '1'), pType => PTNil, Load => (others =>'0'));
   signal nocBIn : NoCPacket := (Src => "000", Dst => (others => '0'), pType => PTNil, Load => (others =>'0'));

 	--Outputs
   signal nocAOut : NoCPacket;
   signal nocBOut : NoCPacket;

   -- Clock period definitions
   constant ClkA_period : time := 10 ns;
   constant ClkB_period : time := 10 ns;
 
BEGIN
 
	-- Instantiate the Unit Under Test (UUT)
   uut: NoCSwitchV2 PORT MAP (
          ClkA => ClkA,
          ClkB => ClkB,
          Rst => Rst,
          nocAIn => nocAIn,
          nocAOut => nocAOut,
          nocBIn => nocBIn,
          nocBOut => nocBOut
        );

   -- Clock process definitions
   ClkA_process :process
   begin
		ClkA <= '1';
		wait for ClkA_period/2;
		ClkA <= '0';
		wait for ClkA_period/2;
   end process;
 
   ClkB_process :process
   begin
		ClkB <= '1';
		wait for ClkB_period/2;
		ClkB <= '0';
		wait for ClkB_period/2;
   end process;
 

   -- Stimulus process
   stim_proc: process
   begin		
 		Rst <= '1';
      -- hold reset state for 100 ns.
      wait for 100 ns;	
		Rst <= '0';
		
      wait for ClkA_period*10;

	   nocAIn <=(Src => "100", Dst => "101", pType => PTEoD, Load => X"DEADBEEF");
	   nocBIn <=(Src => "001", Dst => "010", pType => PTData, Load => X"CAFEBABE");
	
		wait for ClkA_period;
		
	   nocAIn <=(Src => "101", Dst => "000", pType => PTEoD, Load => X"FEEDFACE");
	   nocBIn <=(Src => "000", Dst => "010", pType => PTNil, Load => (others => '0'));
		
		wait for ClkA_period;
		
	   nocAIn <=(Src => "110", Dst => "100", pType => PTNil, Load => (others => '0'));
	   nocBIn <=(Src => "011", Dst => "110", pType => PTEoD, Load => (others => '0'));

		wait for ClkA_period;
		
	   nocAIn <=(Src => "010", Dst => "100", pType => PTEoD, Load => X"CAFEBABE");
	   nocBIn <=(Src => "100", Dst => "011", pType => PTAck, Load => (others => '0'));
		
		wait for 5*ClkA_period;
		
	   nocAIn <=(Src => "011", Dst => "110", pType => PTAck, Load => X"CAFEBABE");
	   nocBIn <=(Src => "010", Dst => "000", pType => PTNil, Load => (others => '0'));

      -- insert stimulus here
		wait for ClkA_period;
		
--	   nocAIn <=(Src => "010", Dst => "100", pType => PTEoD, Load => X"CAFEBABE");
	   nocBIn <=(Src => "011", Dst => "000", pType => PTNil, Load => (others => '0'));
		
      wait;
  end process;

END;
