--
--	testint.vhd
--
--


library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity testint is

port (
	clk, reset	: in std_logic;

	timer_int	: in std_logic;
	yield_int	: in std_logic;
	int_ack		: in std_logic;

	int_ena		: in std_logic;
	jfetch		: in std_logic;

	timer_out	: out std_logic;
	yield_out	: out std_logic;
	irq_out	: out std_logic;

	trig_out	: out std_logic;
	int_pend_out	: out std_logic;
	sys_int_out	: out std_logic

);
end testint;

architecture rtl of testint is

	signal timer		: std_logic;
	signal yield		: std_logic;
	signal irq			: std_logic;

	signal irq_dly		: std_logic;
	signal trig			: std_logic;
	signal int_pend		: std_logic;
	signal sys_int		: std_logic;


begin

--	signal test out
	timer_out <= timer;
	yield_out <= yield;
	irq_out <= irq;
	trig_out <= trig;
	int_pend_out <= int_pend;
	sys_int_out <= sys_int;

--
--	int processing from timer and yield request
--
process(clk, reset) begin

	if (reset='1') then
		timer <= '0';
		yield <= '0';
	elsif rising_edge(clk) then
		if int_ack='1' then
			timer <= '0';
			yield <= '0';
		else
			if timer_int='1' then
				timer <= '1';
			end if;
			if yield_int='1' then
				yield <= '1';
			end if;
		end if;
	end if;

end process;

	irq <= timer or yield;

--
--	int processing at bytecode fetch level
--
process(clk, reset) begin

	if (reset='1') then
		irq_dly <= '0';
		int_pend <= '0';
	elsif rising_edge(clk) then

		irq_dly <= irq;
		if trig='1' then
			int_pend <= '1';
		elsif sys_int='1' then
			int_pend <= '0';
		end if;
	end if;

end process;

	trig <= irq and not irq_dly;
	sys_int <= int_pend and jfetch and int_ena;

end rtl;
