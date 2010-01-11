--
--
--  This file is a part of JOP, the Java Optimized Processor
--
--  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
--
--  This program is free software: you can redistribute it and/or modify
--  it under the terms of the GNU General Public License as published by
--  the Free Software Foundation, either version 3 of the License, or
--  (at your option) any later version.
--
--  This program is distributed in the hope that it will be useful,
--  but WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
--  GNU General Public License for more details.
--
--  You should have received a copy of the GNU General Public License
--  along with this program.  If not, see <http://www.gnu.org/licenses/>.
--


--
--	bytecode.vhd
--
--	Show bytecode mnemonic in the simulation
--
--	Author: Martin Schoeberl	martin@jopdesign.com
--
--
--
--	2007-12-22	creation
--


library std;
use std.textio.all;

library ieee;
use ieee.std_logic_1164.all;
use ieee.numeric_std.all;

entity bytecode is

port (jinstr : in std_logic_vector(7 downto 0));
end bytecode;

architecture sim of bytecode is

	type bcval is (
			nop, aconst_null, iconst_m1, iconst_0, iconst_1, iconst_2, iconst_3, iconst_4, 
			iconst_5, lconst_0, lconst_1, fconst_0, fconst_1, fconst_2, dconst_0, dconst_1, 
			bipush, sipush, ldc, ldc_w, ldc2_w, iload, lload, fload, 
			dload, aload, iload_0, iload_1, iload_2, iload_3, lload_0, lload_1, 
			lload_2, lload_3, fload_0, fload_1, fload_2, fload_3, dload_0, dload_1, 
			dload_2, dload_3, aload_0, aload_1, aload_2, aload_3, iaload, laload, 
			faload, daload, aaload, baload, caload, saload, istore, lstore, 
			fstore, dstore, astore, istore_0, istore_1, istore_2, istore_3, lstore_0, 
			lstore_1, lstore_2, lstore_3, fstore_0, fstore_1, fstore_2, fstore_3, dstore_0, 
			dstore_1, dstore_2, dstore_3, astore_0, astore_1, astore_2, astore_3, iastore, 
			lastore, fastore, dastore, aastore, bastore, castore, sastore, pop, 
			pop2, dup, dup_x1, dup_x2, dup2, dup2_x1, dup2_x2, swap, 
			iadd, ladd, fadd, dadd, isub, lsub, fsub, dsub, 
			imul, lmul, fmul, dmul, idiv, ldiv, fdiv, ddiv, 
			irem, lrem, frem, drem, ineg, lneg, fneg, dneg, 
			ishl, lshl, ishr, lshr, iushr, lushr, iand, land, 
			ior, lor, ixor, lxor, iinc, i2l, i2f, i2d, 
			l2i, l2f, l2d, f2i, f2l, f2d, d2i, d2l, 
			d2f, i2b, i2c, i2s, lcmp, fcmpl, fcmpg, dcmpl, 
			dcmpg, ifeq, ifne, iflt, ifge, ifgt, ifle, if_icmpeq, 
			if_icmpne, if_icmplt, if_icmpge, if_icmpgt, if_icmple, if_acmpeq, if_acmpne, goto, 
			jsr, ret, tableswitch, lookupswitch, ireturn, lreturn, freturn, dreturn, 
			areturn, returnx, getstatic, putstatic, getfield, putfield, invokevirtual, invokespecial, 
			invokestatic, invokeinterface, unused_ba, newx, newarray, anewarray, arraylength, athrow, 
			checkcast, instanceof, monitorenter, monitorexit, wide, multianewarray, ifnull, ifnonnull, 
			goto_w, jsr_w, breakpoint, resCB, jopsys_inval, resCD, resCE, resCF, 
			jopsys_null, jopsys_rd, jopsys_wr, jopsys_rdmem, jopsys_wrmem, jopsys_rdint, jopsys_wrint, jopsys_getsp, 
			jopsys_setsp, jopsys_getvp, jopsys_setvp, jopsys_int2ext, jopsys_ext2int, jopsys_nop, jopsys_invoke, jopsys_cond_move, 
			getstatic_ref, putstatic_ref, getfield_ref, putfield_ref, getstatic_long, putstatic_long, getfield_long, putfield_long, 
			jopsys_memcpy, jopsys_getfield, jopsys_putfield, resEB, invokesuper, resED, jopsys_getstatic, jopsys_putstatic, 
			sys_int, sys_exc, resF2, resF3, resF4, resF5, resF6, resF7, 
			resF8, resF9, resFA, resFB, resFC, resFD, sys_noim, sys_init 
	);
	signal val : bcval;

begin

	val <= bcval'val(to_integer(unsigned(jinstr)));

end sim;
