--
--	rom.vhd
--
--	microinstruction memory for JOP3
--
--	use technology specific file instead (arom.vhd or xrom.vhd)
--
--

Library IEEE ;
use IEEE.std_logic_1164.all ;
use IEEE.std_logic_arith.all ;
use IEEE.std_logic_unsigned.all ;

entity rom is
generic (width : integer := 8; addr_width : integer := 9);
port (
	address	: in std_logic_vector(addr_width-1 downto 0);

	q			: out std_logic_vector(width-1 downto 0)
);
end rom;

--
--	unregistered rdaddress
--	unregistered dout
--
architecture rtl of rom is

	signal addr	: std_logic_vector(15 downto 0);

begin

	addr <= "0000000" & address;

process(addr) begin

	q <= x"84ff";					-- not impl loop
	case addr is

		when x"0000" => q <= x"8200";	-- nop

		when x"0002" => q <= x"d2ff";	-- iconst_m1
		when x"0003" => q <= x"d200";	-- iconst_0
		when x"0004" => q <= x"d201";	-- iconst_1
		when x"0005" => q <= x"d202";	-- iconst_2
		when x"0006" => q <= x"d203";	-- iconst_3
		when x"0007" => q <= x"d204";	-- iconst_4
		when x"0008" => q <= x"d205";	-- iconst_5

--
--	130..144
--
		when x"0010" => q <= x"8530";	-- bipush (nop, jp, opd fetch)
		when x"0130" => q <= x"ea01";	-- ld opd (8s)

		when x"0011" => q <= x"8531";	-- sipush (nop, jp, opd fetch)
		when x"0131" => q <= x"8100";	-- nop, opd fetch
		when x"0132" => q <= x"ea02";	-- ld opd (16s)

		when x"0012" => q <= x"8533";	-- ldc (nop, jp, opd fetch)
		when x"0133" => q <= x"e206";	-- ld (cp+idx)

		when x"0015" => q <= x"8534";	-- iload (nop, jp, opd fetch)
		when x"0134" => q <= x"e205";	-- ld (vp+idx)

		when x"001a" => q <= x"e200";	-- iload_0
		when x"001b" => q <= x"e201";	-- iload_1
		when x"001c" => q <= x"e202";	-- iload_2
		when x"001d" => q <= x"e203";	-- iload_3

		when x"0036" => q <= x"8535";	-- istore (nop, jp, opd fetch)
		when x"0135" => q <= x"6205";	-- st (vp+idx)

		when x"003b" => q <= x"6200";	-- istore_0
		when x"003c" => q <= x"6201";	-- istore_1
		when x"003d" => q <= x"6202";	-- istore_2
		when x"003e" => q <= x"6203";	-- istore_3

		when x"0057" => q <= x"0200";	-- pop
		when x"0059" => q <= x"fa00";	-- dup
		when x"0060" => q <= x"2200";	-- iadd
		when x"0064" => q <= x"2a00";	-- isub
		when x"007e" => q <= x"0a00";	-- iand
		when x"0080" => q <= x"1200";	-- ior
		when x"0082" => q <= x"1a00";	-- ixor

-- iinc
		when x"0084" => q <= x"8436";	-- nop, jp
		when x"0136" => q <= x"d801";	-- ld vp
		when x"0137" => q <= x"f900";	-- dup, opd fetch
		when x"0138" => q <= x"e801";	-- ld opd (8s)		-- works only for idx < 128 (sign ext)!
		when x"0139" => q <= x"2000";	-- add
		when x"013a" => q <= x"5900";	-- st vp, opd fetch
		when x"013b" => q <= x"e801";	-- ld opd (8s)
		when x"013c" => q <= x"e000";	-- iload_0
		when x"013d" => q <= x"2000";	-- add
		when x"013e" => q <= x"6000";	-- istore_0
		when x"013f" => q <= x"5800";	-- st vp
		when x"0140" => q <= x"8200";	-- nop, fetch

		when x"0074" => q <= x"8441";	-- nop, jp
		when x"0141" => q <= x"d0ff";	-- ld -1
		when x"0142" => q <= x"1800";	-- xor
		when x"0143" => q <= x"d001";	-- ld 1
		when x"0144" => q <= x"2200";	-- add, fetch
--
--	100..126 branch
--
		when x"0099" => q <= x"8500";	-- ifeq (nop, jp, opd fetch)
		when x"0100" => q <= x"7900";	-- jbr eq, opd fetch
		when x"0101" => q <= x"8000";	-- nop
		when x"0102" => q <= x"8200";	-- nop, fetch 
		when x"009a" => q <= x"8503";	-- ifne (nop, jp, opd fetch)
		when x"0103" => q <= x"7901";	-- jbr ne, opd fetch
		when x"0104" => q <= x"8000";	-- nop
		when x"0105" => q <= x"8200";	-- nop, fetch 
		when x"009b" => q <= x"8506";	-- iflt (nop, jp, opd fetch)
		when x"0106" => q <= x"7902";	-- jbr lt, opd fetch
		when x"0107" => q <= x"8000";	-- nop
		when x"0108" => q <= x"8200";	-- nop, fetch 
		when x"009c" => q <= x"8509";	-- ifge (nop, jp, opd fetch)
		when x"0109" => q <= x"7903";	-- jbr ge, opd fetch
		when x"010a" => q <= x"8000";	-- nop
		when x"010b" => q <= x"8200";	-- nop, fetch 
		when x"009d" => q <= x"850c";	-- ifgt (nop, jp, opd fetch)
		when x"010c" => q <= x"7904";	-- jbr gt, opd fetch
		when x"010d" => q <= x"8000";	-- nop
		when x"010e" => q <= x"8200";	-- nop, fetch 
		when x"009e" => q <= x"850f";	-- ifle (nop, jp, opd fetch)
		when x"010f" => q <= x"7905";	-- jbr le, opd fetch
		when x"0110" => q <= x"8000";	-- nop
		when x"0111" => q <= x"8200";	-- nop, fetch 

		when x"009f" => q <= x"8512";	-- if_icmpeq (nop, jp, opd fetch)
		when x"0112" => q <= x"7908";	-- jbr icmpeq, opd fetch
		when x"0113" => q <= x"0000";	-- pop
		when x"0114" => q <= x"8200";	-- nop, fetch 
		when x"00a0" => q <= x"8515";	-- if_icmpne (nop, jp, opd fetch)
		when x"0115" => q <= x"7909";	-- jbr icmpne, opd fetch
		when x"0116" => q <= x"0000";	-- pop
		when x"0117" => q <= x"8200";	-- nop, fetch 
		when x"00a1" => q <= x"8518";	-- if_icmplt (nop, jp, opd fetch)
		when x"0118" => q <= x"790a";	-- jbr icmplt, opd fetch
		when x"0119" => q <= x"0000";	-- pop
		when x"011a" => q <= x"8200";	-- nop, fetch 
		when x"00a2" => q <= x"851b";	-- if_icmpge (nop, jp, opd fetch)
		when x"011b" => q <= x"790b";	-- jbr icmpge, opd fetch
		when x"011c" => q <= x"0000";	-- pop
		when x"011d" => q <= x"8200";	-- nop, fetch 
		when x"00a3" => q <= x"851e";	-- if_icmpgt (nop, jp, opd fetch)
		when x"011e" => q <= x"790c";	-- jbr icmpgt, opd fetch
		when x"011f" => q <= x"0000";	-- pop
		when x"0120" => q <= x"8200";	-- nop, fetch 
		when x"00a4" => q <= x"8521";	-- if_icmple (nop, jp, opd fetch)
		when x"0121" => q <= x"790d";	-- jbr icmple, opd fetch
		when x"0122" => q <= x"0000";	-- pop
		when x"0123" => q <= x"8200";	-- nop, fetch 

		when x"00a7" => q <= x"8524";	-- goto (nop, jp, opd fetch)
		when x"0124" => q <= x"9100";	-- jbr goto, opd fetch
		when x"0125" => q <= x"8000";	-- nop
		when x"0126" => q <= x"8200";	-- nop, fetch 

--
--	invoke 'system' functions:
--
--		1	int rd(int adr)
--		2	void wr(int adr, int val)
--		3	int status()
--		4	int uart_rd()
--		5	void uart_wr(int val)
--		6	int ecp_rd()
--		7	void ecp_wr(int val)
--		8	int cnt()
--		9	int ms()
--
		when x"00b8" => q <= x"8550";	-- nop, jp, opd fetch
		when x"0150" => q <= x"8100";	-- nop, opd fetch
		when x"0151" => q <= x"e006";	-- ld (cp+idx)	-- ignore index > 255 !
		when x"0152" => q <= x"f800";	-- dup
		when x"0153" => q <= x"d003";	-- ld 3
		when x"0154" => q <= x"1800";	-- xor
		when x"0155" => q <= x"8000";	-- nop		-- one cycle wait for zf
		when x"0156" => q <= x"3000";	-- bz 160
		when x"0157" => q <= x"8000";	-- nop
		when x"0158" => q <= x"8090";	-- nop address for bz! real jump 2 cycles later
		when x"0159" => q <= x"f800";	-- dup
		when x"015a" => q <= x"d004";	-- ld 4
		when x"015b" => q <= x"1800";	-- xor
		when x"015c" => q <= x"8000";	-- nop
		when x"015d" => q <= x"3000";	-- bz
		when x"015e" => q <= x"8000";	-- nop
		when x"015f" => q <= x"8092";	-- nop, br address
		when x"0160" => q <= x"f800";	-- dup
		when x"0161" => q <= x"d005";	-- ld 5
		when x"0162" => q <= x"1800";	-- xor
		when x"0163" => q <= x"8000";	-- nop
		when x"0164" => q <= x"3000";	-- bz
		when x"0165" => q <= x"8000";	-- nop
		when x"0166" => q <= x"8094";	-- nop, br address
		when x"0167" => q <= x"f800";	-- dup
		when x"0168" => q <= x"d006";	-- ld 6
		when x"0169" => q <= x"1800";	-- xor
		when x"016a" => q <= x"8000";	-- nop
		when x"016b" => q <= x"3000";	-- bz
		when x"016c" => q <= x"8000";	-- nop
		when x"016d" => q <= x"8096";	-- nop, br address
		when x"016e" => q <= x"f800";	-- dup
		when x"016f" => q <= x"d007";	-- ld 7
		when x"0170" => q <= x"1800";	-- xor
		when x"0171" => q <= x"8000";	-- nop
		when x"0172" => q <= x"3000";	-- bz
		when x"0173" => q <= x"8000";	-- nop
		when x"0174" => q <= x"8098";	-- nop, br address
		when x"0175" => q <= x"f800";	-- dup
		when x"0176" => q <= x"d008";	-- ld 8
		when x"0177" => q <= x"1800";	-- xor
		when x"0178" => q <= x"8000";	-- nop
		when x"0179" => q <= x"3000";	-- bz
		when x"017a" => q <= x"8000";	-- nop
		when x"017b" => q <= x"809a";	-- nop, br address
		when x"017c" => q <= x"f800";	-- dup
		when x"017d" => q <= x"d009";	-- ld 9
		when x"017e" => q <= x"1800";	-- xor
		when x"017f" => q <= x"8000";	-- nop
		when x"0180" => q <= x"3000";	-- bz
		when x"0181" => q <= x"8000";	-- nop
		when x"0182" => q <= x"809c";	-- nop, br address

		when x"0183" => q <= x"0200";	-- pop, fetch		-- no function found
	
--  functions:

		when x"0190" => q <= x"0000";	-- pop
		when x"0191" => q <= x"ca01";	-- ld status, fetch
-- when x"0191" => q <= x"d204"; -- ld 4, fetch
		when x"0192" => q <= x"0000";	-- pop
		when x"0193" => q <= x"ca02";	-- ld uart, fetch
		when x"0194" => q <= x"0000";	-- pop
		when x"0195" => q <= x"4a02";	-- st uart, fetch
		when x"0196" => q <= x"0000";	-- pop
		when x"0197" => q <= x"ca03";	-- ld ecp, fetch
		when x"0198" => q <= x"0000";	-- pop
		when x"0199" => q <= x"4a03";	-- st ecp, fetch
		when x"019a" => q <= x"0000";	-- pop
		when x"019b" => q <= x"ca0a";	-- ld cnt, fetch
		when x"019c" => q <= x"0000";	-- pop
		when x"019d" => q <= x"ca0b";	-- ld ms, fetch

		when others => null;
	end case;
end process;

end rtl;
