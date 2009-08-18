library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

use work.sc_pack.all;

entity tmif is

-- generic (
-- 
-- 
-- );

port (
	clk					: in std_logic;
	reset				: in std_logic;
	
	--
	--	Commit logic
	--
	
	-- set until transaction finished/aborted
	commit_out_try			: out std_logic;
	commit_in_allow			: in std_logic;

	--
	--	Commit addresses
	--
	commit_in_address_valid	: in std_logic;
	commit_in_address		: in std_logic_vector(31 downto 0);

	--
	--	Memory IF to cpu
	--
	sc_cpu_out		: in sc_out_type;
	sc_cpu_in		: out sc_in_type;		
	-- memory access types
	-- TODO more hints about memory access type?

	--
	--	Memory IF to arbiter
	--
	sc_arb_out		: out sc_out_type;
	sc_arb_in		: in sc_in_type;

	--
	--	Rollback exception
	--
	exc_tm_rollback	: out std_logic
		
);

end tmif;

architecture rtl of tmif is

begin

end rtl;
