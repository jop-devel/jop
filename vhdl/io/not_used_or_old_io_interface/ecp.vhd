--
--	ecp.vhd
--
--	ecp peripheral interface
--	
--	wr, rd should be one cycle long => trde, rdrf goes 0 one cycle later
--
--
--			   ___     ___     ___     ___     ___     ___     ___    
--	clk		__/   \___/   \___/   \___/   \___/   \___/   \___/   \___
--	
--			     _______                                 _______
--	wr		____/       \_______________________________/       \_____
--	
--			            ________________                         _____
--	trdf	___________/                \_______________________/     
--	
--			                     _______
--	tdr_rd	____________________/       \_____________________________
--
--
--
--	Author: Martin Schoeberl	martin@good-ear.com
--
--
--	resources on ACEX1K30-3
--
--		xx LCs, max 90 MHz
--
--
--	todo:
--		terminate, recover???
--		glitch filter on control lines
--		wait timing depends on input clk
--		set data out 'serial' to reduce ground bounce
--
--
--	2000-12-29	first version
--	2001-02-03	first working version (max. about 125kb !!! on Acer)
--	2001-02-04	error with tdrf and tdr_rd corrected
--	2001-02-15	added spike filter for control lines, change data bits 'serial'
--				'dirty' termination in all forward states
--	2001-07-18	spike in a sperate file, arch to rtl
--


library ieee ;
use ieee.std_logic_1164.all ;
use ieee.numeric_std.all ;

entity ecp is

port (
	clk		: in std_logic;
	reset	: in std_logic;

	lpt_d	: inout std_logic_vector(7 downto 0);
	lpt_s	: out std_logic_vector(7 downto 3);
	lpt_c	: in std_logic_vector(3 downto 0);

	din		: in std_logic_vector(7 downto 0);		-- send data
	dout	: out std_logic_vector(7 downto 0);		-- rvc data

	wr		: in std_logic;			-- send data
	tdre	: out std_logic;		-- transmit data register empty

	rd		: in std_logic;			-- read data
	rdrf	: out std_logic			-- receive data register full
);
end ecp ;

architecture rtl of ecp is

component spike is

port (
	clk		: in std_logic;
	reset	: in std_logic;

	in_sp	: in std_logic;
	out_sp	: out std_logic

);
end component spike ;

	signal c				: std_logic_vector(3 downto 0);

	type ecp_state_type		is (spp, ng0, ng1, ng2, ng3, fwi, fw0, fw1,
								rv0, rv1, rv2, rvs);
	signal ecp_state 		: ecp_state_type;

	type ecp_tdr_state_type	is (s0, s1);
	signal ecp_tdr_state 	: ecp_tdr_state_type;

	type ecp_rdr_state_type	is (s0, s1);
	signal ecp_rdr_state 	: ecp_rdr_state_type;

	signal tdr			: std_logic_vector(7 downto 0); -- tx buffer
	signal tr			: std_logic_vector(7 downto 0); -- tx register
	signal tdrf			: std_logic;					-- tdr has valid data
	signal tdr_rd		: std_logic;					-- tdr was read

	signal rdr			: std_logic_vector(7 downto 0); -- rx buffer
	signal rr			: std_logic_vector(7 downto 0); -- rx register
	signal rdre			: std_logic;					-- rdr is empty
	signal rdr_wr		: std_logic;					-- rdr was written


begin


--
--	sync in and spike filter for control lines
--
	sp1:    for i in 3 downto 0 generate
		spb: spike port map (clk, reset, lpt_c(i), c(i));
		end generate sp1;

--
--	state machine for tdr
--
process(clk, reset)

begin

	if (reset='1') then
		ecp_tdr_state <= s0;
		tdrf <= '0';
		tdr <= "00000000";
	elsif rising_edge(clk) then

		case ecp_tdr_state is

			when s0 =>
				if (wr='1') then
					tdr <= din;
					tdrf <= '1';
					ecp_tdr_state <= s1;
				end if;

			when s1 =>
				if (tdr_rd='1') then
					tdrf <= '0';
					ecp_tdr_state <= s0;
				end if;

		end case;
	end if;

end process;

--
--	state machine for rdr
--
process(clk, reset)

begin

	if (reset='1') then
		ecp_rdr_state <= s0;
		rdre <= '1';
	elsif rising_edge(clk) then

		case ecp_rdr_state is

			when s0 =>
				if (rdr_wr='1') then
					rdre <= '0';
					ecp_rdr_state <= s1;
				end if;

			when s1 =>
				if (rd='1') then
					rdre <= '1';
					ecp_rdr_state <= s0;
				end if;

		end case;
	end if;

end process;

--
--	state machine for ecp
--
process(clk, reset)

	variable i : integer range 0 to 15;
	variable ser : integer range 0 to 7;

begin

	if (reset='1') then
		ecp_state <= spp;
		tr <= "00000000";
		tdr_rd <= '0';
		rdr <= "00000000";
		rr <= "00000000";
		rdr_wr <= '0';
		lpt_s <= "01001";

	elsif rising_edge(clk) then

		case ecp_state is

			when spp =>
				tdr_rd <= '0';
				rdr_wr <= '0';
				lpt_s <= "01001";

				if (c(1)='0' and c(3)='1') then
					ecp_state <= ng0;
				end if;

			when ng0 =>
				lpt_s <= "00111";
				if (c(0)='0') then
					ecp_state <= ng1;
				end if;

			when ng1 =>
				lpt_s <= "00111";
				rr <= lpt_d;
				i := 0;
				if (c(0)='1' and c(1)='1') then
					if (rr="00010000") then
						ecp_state <= ng2;
					else
						ecp_state <= spp;
					end if;
				end if;

			when ng2 =>
				lpt_s <= "00011";
				i := i+1;
				if (i=15) then
					ecp_state <= ng3;
				end if;

			when ng3 =>
				lpt_s <= "01011";
				if (c(1)='0') then
					ecp_state <= fwi;
				end if;

			when fwi =>					-- wait for data (HostClk low)
				lpt_s <= "01111";
				rdr_wr <= '0';
				ser := 0;
				if (c(3)='0') then
					ecp_state <= spp;		-- termination hanshake missing
				elsif (c(0)='0') then
					ecp_state <= fw0;		-- stall if not rdy missing
				elsif (c(1)='0' and c(2)='0') then
					ecp_state <= rv0;
				end if;

			when fw0 =>					-- latch data and wait for HostClk high
				lpt_s <= "11111";
				rr <= lpt_d;
				if (c(3)='0') then
					ecp_state <= spp;		-- termination hanshake missing
				elsif (c(0)='1') then
					ecp_state <= fw1;
				end if;

			when fw1 =>					-- PeriphAck and wait for rdre
				lpt_s <= "01111";
				if (c(3)='0') then
					ecp_state <= spp;		-- termination hanshake missing
				elsif (rdre='1') then
					rdr <= rr;
					rdr_wr <= '1';
					ecp_state <= fwi;
										-- stall missing
				end if;

			when rv0 =>					-- wait for data (tdrf)
				lpt_s <= "11011";
				i := 0;
				if (c(2)='1') then
					ecp_state <= rvs;
				elsif (tdrf='1') then
					tr(ser) <= tdr(ser);	-- set 'long' data lines serial to reduce
					if (ser = 7) then		-- ground bounce
						ecp_state <= rv1;
						tdr_rd <= '1';
					else
						ser := ser + 1;
					end if;
				end if;

			when rv1 =>					-- PeriphClk low and wait for HostAck high
				lpt_s <= "10011";
				tdr_rd <= '0';
				ser := 0;
				if (c(2)='1') then
					ecp_state <= rvs;
				elsif (c(1)='1') then
					ecp_state <= rv2;
				end if;

			when rv2 =>					-- PeriphClk high and wait for HostAck low
				lpt_s <= "11011";
				if (c(2)='1') then
					ecp_state <= rvs;
				elsif (c(1)='0') then
					ecp_state <= rv0;
				end if;

			when rvs =>
				tdr_rd <= '0';			-- for shure ???
				lpt_s <= "01011";
				i := i+1;
				if (i=15) then
					ecp_state <= fwi;
				end if;

		end case;
	end if;

end process;

--
--	outputs
--
process(lpt_c(2), lpt_c(3), ecp_state, tr)

begin

	if (lpt_c(2)='0' and lpt_c(3)='1' and (ecp_state=rv0 or ecp_state=rv1 or
						ecp_state=rv2)) then
		lpt_d <= tr;
	else
		lpt_d <= "ZZZZZZZZ";
	end if;

end process;

	dout <= rdr;
	tdre <= not tdrf;
	rdrf <= not rdre;

end architecture rtl;


