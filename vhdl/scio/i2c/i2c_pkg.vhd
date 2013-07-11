--! @file i2c_pkg.vhd
--! @brief Auxiliary functions and constants for the main I2C design   
--! @details 
--! @author    Juan Ricardo Rios, jrri@imm.dtu.dk
--! @version   
--! @date      2011-2012
--! @copyright GNU Public License.

library IEEE;
use IEEE.STD_LOGIC_1164.all;

--! Auxiliary functions and constants for the main I2C design
package i2c_pkg is
	constant RESET_LEVEL : std_logic := '1'; --! Reset level, default logic '1'

	constant I2C_DEV_ADDR_SIZE : integer                      := 7; --! Slave address size, 7 bits
	constant I2C_HDR_SIZE      : integer range 0 to  15      := 8; --! Header length, 8 bits
	constant I2C_DATA_SIZE     : integer range 0 to  8       := 8; --! Size of data word, 8 bits
	constant I2C_MSG_SIZE      : integer                      := 16; --! 
	constant I2C_RD_TRANS      : std_logic                    := '1'; --! Read transaction, from master viewpoint
	constant I2C_WR_TRANS      : std_logic                    := '0'; --! Write transaction, from master viewpoint

	constant TRIGGER_SIZE : integer := 18;

	type monitor is record
		control : std_logic_vector(35 downto 0);
		trigger : std_logic_vector(TRIGGER_SIZE downto 0);
	end record;

	type timing_type is record
		T_HOLD_START : std_logic_vector(7 downto 0); --! Number of clock cycles between the START condition and the first header bit transmitted
		T_RSTART     : std_logic_vector(7 downto 0);
		T_LOW        : std_logic_vector(7 downto 0); --! Number of clock cycles the SCL clock stays in the logic low level
		T_HIGH       : std_logic_vector(7 downto 0); --! Number of clock cycles the SCL clock stays in the logic high level
		T_HALF_HIGH  : std_logic_vector(7 downto 0); --! An approximation to half the high SCL interval 
		T_SUSTO      : std_logic_vector(7 downto 0); --! Number of clock cycles to wait before setting SDA to high to signal the stop condition
		T_WAIT       : std_logic_vector(7 downto 0); --! Number of clock cycles to wait before attempting a new transmission
	end record;

	type control_type is record
		ACK      : std_logic;           --! Value of the returned ACK (Slave/Master mode)
		STRT     : std_logic;           --! Generate a start condition (Master mode).
		STOP     : std_logic;           --! Generate a stop condition (Master mode) 
		MASL     : std_logic;           --! Master (1) or SLave operation (0).
		RSTA     : std_logic;           --! Repeated start condition (Master mode).
		ENABLE   : std_logic;           --! Enable device (Slave mode).
		TX_FLUSH : std_logic;           --! Flush transmit buffer.
		RX_FLUSH : std_logic;           --! Flush receive buffer.
	end record;

	type status_type is record
		TBUF_ERR : std_logic;           --! Transmit buffer error
		RBUF_ERR : std_logic;           --! Receive buffer error
		ACK_ERR  : std_logic;           --! Acknowledgment error
		HDR_ERR  : std_logic;           --! Slave address error
		BUSY     : std_logic;           --! I2C bus busy 
		STOP     : std_logic;           --! Indicates a STOP condition has been received
		DATA_RDY : std_logic;           --! Data is ready to be read in RX buffer when in slave receiver mode
	end record;

	type sda_control_in_type is record
		START  : std_logic;
		STOP   : std_logic;
		RSTART : std_logic;
	end record;

	type sda_control_out_type is record
		SET_SDA      : std_logic;
		SDA_ENA      : std_logic;
		SCL_ARB_LOST : std_logic;
	end record;

end package i2c_pkg;