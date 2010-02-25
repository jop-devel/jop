-- megafunction wizard: %FIFO%
-- GENERATION: STANDARD
-- VERSION: WM1.0
-- MODULE: scfifo 

-- ============================================================
-- File Name: write_fifo_buffer.vhd
-- Megafunction Name(s):
-- 			scfifo
--
-- Simulation Library Files(s):
-- 			altera_mf
-- ============================================================
-- ************************************************************
-- THIS IS A WIZARD-GENERATED FILE. THIS FILE WAS EDITED!
--
-- 9.0 Build 235 06/17/2009 SP 2 SJ Web Edition
-- ************************************************************


--Copyright (C) 1991-2009 Altera Corporation
--Your use of Altera Corporation's design tools, logic functions 
--and other software and tools, and its AMPP partner logic 
--functions, and any output files from any of the foregoing 
--(including device programming or simulation files), and any 
--associated documentation or information are expressly subject 
--to the terms and conditions of the Altera Program License 
--Subscription Agreement, Altera MegaCore Function License 
--Agreement, or other applicable license agreement, including, 
--without limitation, that your use is for the sole purpose of 
--programming logic devices manufactured by Altera and sold by 
--Altera or its authorized distributors.  Please refer to the 
--applicable agreement for further details.


LIBRARY ieee;
USE ieee.std_logic_1164.all;

LIBRARY altera_mf;
USE altera_mf.all;

ENTITY write_fifo_buffer IS
	generic (
		addr_width		: integer;
		way_bits	: integer;
		intended_device_family	: string := "Cyclone II" -- TODO
	);
	PORT
	(
		clock		: IN STD_LOGIC ;
		data		: IN STD_LOGIC_VECTOR (addr_width-1 DOWNTO 0);
		rdreq		: IN STD_LOGIC ;
		sclr		: IN STD_LOGIC ;
		wrreq		: IN STD_LOGIC ;
		empty		: OUT STD_LOGIC ;
		q		: OUT STD_LOGIC_VECTOR (addr_width-1 DOWNTO 0)
	);
END write_fifo_buffer;


ARCHITECTURE SYN OF write_fifo_buffer IS

	SIGNAL sub_wire0	: STD_LOGIC ;
	SIGNAL sub_wire1	: STD_LOGIC_VECTOR (addr_width-1 DOWNTO 0);



	COMPONENT scfifo
	GENERIC (
		add_ram_output_register		: STRING;
		intended_device_family		: STRING;
		lpm_numwords		: NATURAL;
		lpm_showahead		: STRING;
		lpm_type		: STRING;
		lpm_width		: NATURAL;
		lpm_widthu		: NATURAL;
		overflow_checking		: STRING;
		underflow_checking		: STRING;
		use_eab		: STRING
	);
	PORT (
			rdreq	: IN STD_LOGIC ;
			sclr	: IN STD_LOGIC ;
			empty	: OUT STD_LOGIC ;
			clock	: IN STD_LOGIC ;
			q	: OUT STD_LOGIC_VECTOR (addr_width-1 DOWNTO 0);
			wrreq	: IN STD_LOGIC ;
			data	: IN STD_LOGIC_VECTOR (addr_width-1 DOWNTO 0)
	);
	END COMPONENT;

BEGIN
	empty    <= sub_wire0;
	q    <= sub_wire1(addr_width-1 DOWNTO 0);

	scfifo_component : scfifo
	GENERIC MAP (
		add_ram_output_register => "OFF",
		intended_device_family => intended_device_family,
		lpm_numwords => 2**way_bits,
		lpm_showahead => "ON",
		lpm_type => "scfifo",
		lpm_width => addr_width,
		lpm_widthu => way_bits,
		overflow_checking => "OFF",
		underflow_checking => "OFF",
		use_eab => "ON"
	)
	PORT MAP (
		rdreq => rdreq,
		sclr => sclr,
		clock => clock,
		wrreq => wrreq,
		data => data,
		empty => sub_wire0,
		q => sub_wire1
	);



END SYN;
