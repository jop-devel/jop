--
--	hdart.vhd
--
--	8-N-1 half duplex serial interface
--	
--	wr, rd should be one cycle long => trde, rdrf goes 0 one cycle later
--
--	Author: Martin Schoeberl	martin@good-ear.com
--
--
--	resources on ACEX1K30-3
--
--		100 LCs, max 90 MHz
--
--
--	todo:
--		use cts and rts
--
--
--	2000-12-02	first working version
--	2002-01-06	changed tdr and rdr to fifos.
--	2002-01-07	copy from uart.vhd for rts setup time (100 us)
--	2002-05-09	common state for rx/tx, no receive during transmit
--	2002-05-15	changed clkdiv calculation
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;

entity hdart is

--generic (clk_freq : integer := 24000000; baud_rate : integer := 9600);
generic (clk_freq : integer := 24000000; baud_rate : integer := 115200);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	txd		: out std_logic;
	rxd		: in std_logic;

	din		: in std_logic_vector(7 downto 0);		-- send data
	dout	: out std_logic_vector(7 downto 0);		-- rvc data

	wr		: in std_logic;			-- send data
	tdre	: out std_logic;		-- transmit data register empty

	rd		: in std_logic;			-- read data
	rdrf	: out std_logic;		-- receive data register full

	cts		: in std_logic;
	rts		: out std_logic
);
end hdart ;

architecture rtl of hdart is

component fifo is

generic (width : integer; depth : integer);
port (
	clk		: in std_logic;
	reset	: in std_logic;

	din		: in std_logic_vector(width-1 downto 0);
	dout	: out std_logic_vector(width-1 downto 0);

	rd		: in std_logic;
	wr		: in std_logic;

	empty	: out std_logic;
	full	: out std_logic
);
end component;

	type hdart_state_type		is (s0, srx1, srx2, stx1, stx2, stx3);
	signal hdart_state 		: hdart_state_type;

	signal tf_dout		: std_logic_vector(7 downto 0); -- fifo out
	signal tf_rd		: std_logic;
	signal tf_empty		: std_logic;
	signal tf_full		: std_logic;

	signal tsr			: std_logic_vector(9 downto 0); -- tx shift register

	signal tx_clk		: std_logic;

	signal rf_wr		: std_logic;
	signal rf_empty		: std_logic;
	signal rf_full		: std_logic;

	signal rx_buf		: std_logic_vector(2 downto 0);	-- sync in, filter
	signal rx_d			: std_logic;					-- rx serial data
	
	signal rsr			: std_logic_vector(9 downto 0); -- rx shift register

	signal rx_clk		: std_logic;
	signal rx_clk_ena	: std_logic;

	constant clk16_cnt	: integer := (clk_freq/baud_rate+8)/16-1;
	constant rtswait	: integer := clk_freq/10000;		-- 100us 


begin

--
--	serial clock
--
process(clk, reset)

	variable clk16		: integer range 0 to clk16_cnt;
	variable clktx		: unsigned(3 downto 0);
	variable clkrx		: unsigned(3 downto 0);

begin
	if (reset='1') then
		clk16 := 0;
		clktx := "0000";
		clkrx := "0000";
		tx_clk <= '0';
		rx_clk <= '0';
		rx_buf <= "111";

	elsif rising_edge(clk) then

		if (clk16=clk16_cnt) then		-- 16 x serial clock
			clk16 := 0;
--
--	tx clock
--
			clktx := clktx + 1;
			if (clktx="0000") then
				tx_clk <= '1';
			else
				tx_clk <= '0';
			end if;
--
--	rx clock
--
			if (rx_clk_ena='1') then
				clkrx := clkrx + 1;
				if (clkrx="1000") then	-- 'middle' of bit
					rx_clk <= '1';
				else
					rx_clk <= '0';
				end if;
			else
				clkrx := "0000";
			end if;
--
--	sync in filter buffer with 16x serial clock
--
			rx_buf(0) <= rxd;
			rx_buf(2 downto 1) <= rx_buf(1 downto 0);
		else
			clk16 := clk16 + 1;
			tx_clk <= '0';
			rx_clk <= '0';
		end if;


	end if;

end process;

--
--	filter rxd
--
	with rx_buf select
		rx_d <=	'0' when "000",
				'0' when "001",
				'0' when "010",
				'1' when "011",
				'0' when "100",
				'1' when "101",
				'1' when "110",
				'1' when "111",
				'X' when others;

	txd <= tsr(0);

--
--	transmit fifo
--
--		transmit fifo is one byte longer because transmit register is not used during 100 us rts enable time.
--		could be changed when setting in state 0 and state 3 (-> remove state 2 ?).
--
	cmp_tf: fifo generic map (8, 4)
			port map (clk, reset, din, tf_dout, tf_rd, wr, tf_empty, tf_full);

	tdre <= not tf_full;

--
--	receive fifo
--
	cmp_rf: fifo generic map (8, 3)
			port map (clk, reset, rsr(8 downto 1), dout, rd, rf_wr, rf_empty, rf_full);

	rdrf <= not rf_empty;

--
--	state machine for actual shift out and in
--		no receive during send (half duplex bus)
--
process(clk, reset)

	variable i : integer range 0 to 10;
	variable w : integer range 0 to rtswait;

begin

	if (reset='1') then
		hdart_state <= s0;
		tsr <= "1111111111";
		tf_rd <= '0';
		rts <= '0';
		w := 0;
		rsr <= "0000000000";
		rf_wr <= '0';
		rx_clk_ena <= '0';

	elsif rising_edge(clk) then

		case hdart_state is

--
--	idle state
--
			when s0 =>
				rts <= '0';
				w := 0;
				i := 0;
				rf_wr <= '0';

				if tf_empty='0' then			-- transmit has priority 
					hdart_state <= stx1;
				else
					if (rx_d='0') then
						rx_clk_ena <= '1';
						hdart_state <= srx1;
					else
						rx_clk_ena <= '0';
					end if;
				end if;

--
--	receive states
--
			when srx1 =>
				if (rx_clk='1') then
					rsr(9) <= rx_d;
					rsr(8 downto 0) <= rsr(9 downto 1);
					i := i+1;
					if (i=10) then
						hdart_state <= srx2;
					end if;
				end if;
					
			when srx2 =>
				rx_clk_ena <= '0';
				if rsr(0)='0' and rsr(9)='1' then	-- start and stop ok?
					if rf_full='0' then
						rf_wr <= '1';
						hdart_state <= s0;
					end if;
				else
					hdart_state <= s0;
				end if;

--
--	transmit states
--
			when stx1 =>
				rts <= '1';
				if w=rtswait then
					hdart_state <= stx2;
				else
					w := w+1;
				end if;

			when stx2 =>
				i := 0;
				w := 0;
				tsr <= tf_dout & '0' & '1';
				tf_rd <= '1';
				hdart_state <= stx3;

			when stx3 =>
				tf_rd <= '0';
				if tx_clk='1' then
					tsr(9) <= '1';
					tsr(8 downto 0) <= tsr(9 downto 1);
					i := i+1;
					if i=10 then
						if tf_empty='0' then		-- transmit back to back
							hdart_state <= stx2;	-- no new rts time
						else
							hdart_state <= s0;
						end if;
					end if;
				end if;
				
		end case;
	end if;

end process;

end rtl;
