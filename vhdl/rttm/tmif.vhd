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
	commit_try			: out std_logic;
	-- valid one cycle after commit_try set
	commit_allow		: in std_logic;
	-- not needed(?)
-- 	-- set while anyone is committing
-- 	commit_happening	: in std_logic;

	--
	--	Rollback triggers
	--
	
	--
	--	Commit addresses
	--
	commit_address_valid: in std_logic;
	commit_address		: in std_logic_vector(31 downto 0);

	--
	--	Memory IF to cpu
	--
	sc_cpu_mem_out		: in sc_out_type;
	sc_cpu_mem_in		: out sc_in_type;		
	-- memory access types
	nc					: in std_logic;
	-- TODO more hints about memory access type?

	--
	--	Memory IF to arbiter
	--
	sc_arb_mem_out		: out sc_out_type;
	sc_arb_mem_in		: in sc_in_type;

	--
	--	Rollback exception
	--
	exc_trans_rollback	: out std_logic
		
);

end tmif;
