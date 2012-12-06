--! @file read_control.vhd
--! @brief Asynchronous fifo read control  
--! @details 
--! @author    Juan Ricardo Rios, jrri@imm.dtu.dk
--! @version   
--! @date      2009-2012
--! @copyright GNU Public License.

-- VHDL Architecture async_fifo_lib.FIFO_read_control.syn
--
-- Created:
--          by - Juan.UNKNOWN (WIN32)
--          at - 01:37:13 09/23/2009
--
--
--
LIBRARY ieee;
USE ieee.std_logic_1164.all;
USE ieee.numeric_std.all;
USE work.fifo_pkg.all;

ENTITY read_control IS
	generic(ADDRESS : integer := 3);

	port(rclk         : in  std_logic;
		 reset        : in  std_logic;
		 read_enable  : in  std_logic;
		 wptr_sync    : in  std_logic_vector((ADDRESS) downto 0);
		 flush	: in std_logic;
		 occupancy_rd : out std_logic_vector(15 downto 0);
		 empty        : out std_logic;
		 rptr         : out std_logic_vector((ADDRESS) downto 0);
		 raddr        : out std_logic_vector((ADDRESS - 1) downto 0);
		 ren          : out std_logic
	);

END ENTITY read_control;

--

ARCHITECTURE rd_ctrl_arch OF read_control IS
	constant FIFO_SIZE : integer := (2 ** ADDRESS);

	signal int_rptr   : std_logic_vector((ADDRESS) downto 0);
	signal counter_en : std_logic;
	signal rd_add     : std_logic_vector((ADDRESS - 1) downto 0);
	signal wr_add     : std_logic_vector((ADDRESS - 1) downto 0);
	signal rd_MSB     : std_logic;
	signal wr_MSB     : std_logic;

BEGIN
	rd_add <= int_rptr((ADDRESS - 1) downto 0);
	wr_add <= wptr_sync((ADDRESS - 1) downto 0);
	rd_MSB <= int_rptr(ADDRESS);
	wr_MSB <= wptr_sync(ADDRESS);

	rptr  <= int_rptr;
	raddr <= rd_add;

	rd_ctrl : process(rd_add, rd_MSB, read_enable, wr_add, wr_MSB)
	begin
		occupancy_rd(15 downto ADDRESS) <= (others => '0');

		if wr_MSB = rd_MSB then
			occupancy_rd(ADDRESS - 1 downto 0) <= std_logic_vector(unsigned(wr_add) - unsigned(rd_add));

			if rd_add = wr_add then
				empty      <= '1';
				counter_en <= '0';
				ren        <= '0';
			else
				empty      <= '0';
				ren        <= read_enable;
				counter_en <= read_enable;
			end if;

		else
			if rd_add = wr_add then
				occupancy_rd(ADDRESS - 1 downto 0) <= (others => '1');
			else
				occupancy_rd(ADDRESS - 1 downto 0) <= std_logic_vector(FIFO_SIZE - unsigned(rd_add) + unsigned(wr_add));
			end if;

			empty      <= '0';
			ren        <= read_enable;
			counter_en <= read_enable;

		end if;

	end process;

	process(rclk, reset)
	begin
		if reset = '1' then
			int_rptr <= (others => '0');

		elsif rclk'event and rclk = '1' then
			if flush = '1' then
				int_rptr <= (others => '0');
			elsif counter_en = '1' then
				int_rptr <= std_logic_vector(unsigned(int_rptr) + 1);
			end if;

		end if;

	end process;

END ARCHITECTURE rd_ctrl_arch;