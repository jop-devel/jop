
-- VHDL Test Bench Created from source file mul.vhd -- 18:13:53 09/08/2004
--
-- Notes: 
-- This testbench has been automatically generated using types std_logic and
-- std_logic_vector for the ports of the unit under test.  Xilinx recommends 
-- that these types always be used for the top-level I/O of a design in order 
-- to guarantee that the testbench will bind correctly to the post-implementation 
-- simulation model.
--
LIBRARY ieee;
USE ieee.std_logic_1164.ALL;
USE ieee.numeric_std.ALL;
--USE ieee.std_logic_signed.ALL;

ENTITY mul_multest_vhd_tb IS
END mul_multest_vhd_tb;

ARCHITECTURE behavior OF mul_multest_vhd_tb IS 

	COMPONENT mul
	PORT(
		clk : IN std_logic;
		din : IN std_logic_vector(31 downto 0);
		wr_a : IN std_logic;
		wr_b : IN std_logic;          
		dout : OUT std_logic_vector(31 downto 0)
		);
	END COMPONENT;

	SIGNAL clk :  std_logic := '0';
	SIGNAL din :  std_logic_vector(31 downto 0);
	SIGNAL wr_a :  std_logic;
	SIGNAL wr_b :  std_logic;
	SIGNAL dout :  std_logic_vector(31 downto 0);
	SIGNAL lfsr1 : signed(31 downto 0) :=     X"00000001";
	SIGNAL lfsr2 : signed(32 downto 0) := "0"&X"00000001";

BEGIN

	uut: mul PORT MAP(
		clk => clk,
		din => din,
		wr_a => wr_a,
		wr_b => wr_b,
		dout => dout
	);


-- *** Test Bench - User Defined Section ***
   clkproc : PROCESS
   BEGIN		
      wait for 5 ns; -- will wait forever
		clk <= not clk;
   END PROCESS;

	tb : PROCESS
	VARIABLE temp : signed(63 downto 0);
   BEGIN		
    	for i in 1 to 1000 loop
		  din <= std_logic_vector(lfsr1);
		  wr_a <= '1';
		  wr_b <= '0';
		  wait until rising_edge(clk);
		  din <= std_logic_vector(lfsr2(31 downto 0));
		  wr_a <= '0'; 
		  wr_b <= '1'; 
		  wait until rising_edge(clk);
		  wr_a <= '0'; 
		  wr_b <= '0'; 
		  for j in 1 to 36 loop
		  	 wait until rising_edge(clk);
		  end loop;
		  temp := lfsr1 * lfsr2(31 downto 0);
		  assert (signed(dout) = temp(31 downto 0)) report "wrong";
		  wait until rising_edge(clk);
		  lfsr1 <= lfsr1(30 downto 0) & (lfsr2(32) xnor lfsr2(31)); 
		  lfsr2 <= lfsr2(31 downto 0) & (lfsr1(31) xnor lfsr1(30)); 
		   wait until rising_edge(clk);
		end loop;
   END PROCESS;
-- *** End Test Bench - User Defined Section ***

END;
