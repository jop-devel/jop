//
//  This file is a part of JOP, the Java Optimized Processor
//
//  Copyright (C) 2001-2008, Martin Schoeberl (martin@jopdesign.com)
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.
//

//
//	mulshft_sw.asm
//
//		'software' version of ishl, ishr, iushr and imul:
//		As replacement of mulshft.vhd if size problems.
//

ishl:
			ldi	31			// to be optimized!!!
			and	
			stm	a			// counter
			stm	b			// value

			ldm	a
			nop
			bz	ishl_end
			nop
			nop

ishl_loop:
			ldm	b			// shift
			dup
			add
			stm	b

			ldm	a
			ldi	-1
			add
			stm	a

			ldm	a
			nop
			bnz	ishl_loop
			nop
			nop
ishl_end:
			ldm	b nxt

ishr:
			ldi	31			// to be optimized!!!
			and	
			stm	a			// counter
			dup
        	ldi	-2147483648		//  0x80000000
			and
			stm	c			// sign
			stm	b			// value

			ldm	a
			nop
			bz	ishr_end
			nop
			nop

ishr_loop:
			ldm	b			// shift
			shr
			ldm	c
			or				// plus sign
			stm	b

			ldm	a
			ldi	-1
			add
			stm	a

			ldm	a
			nop
			bnz	ishr_loop
			nop
			nop
ishr_end:
			ldm	b nxt

iushr:
			ldi	31			// to be optimized!!!
			and	
			stm	a			// counter
			stm	b			// value

			ldm	a
			nop
			bz	iushr_end
			nop
			nop

iushr_loop:
			ldm	b			// shift
			shr
			stm	b

			ldm	a
			ldi	-1
			add
			stm	a

			ldm	a
			nop
			bnz	iushr_loop
			nop
			nop
iushr_end:
			ldm	b nxt

imul:
			stm	b
			stm	a
			ldm	a
        	ldi	-2147483648		//  0x80000000
			and
			dup					// make a positiv
			nop
			bz	imul_apos
			nop
			nop
			ldm	a
			ldi -1
			xor
			ldi 1
			add
			stm	a
imul_apos:
			ldm	b
        	ldi	-2147483648		//  0x80000000
			and
			dup					// make b positiv
			nop
			bz	imul_bpos
			nop
			nop
			ldm	b
			ldi -1
			xor
			ldi 1
			add
			stm	b
imul_bpos:
			xor					//	sign
			stm	e

			ldi	0
			stm	c
			ldi	32			//	loop counter
imul_loop:
			ldm	c
			dup
			add
			stm	c
			ldm	a
        	ldi	-2147483648		//  0x80000000
			and
			nop
			bz	imul_noadd
			nop
			nop
			ldm	c
			ldm	b
			add
			stm	c
imul_noadd:
			ldm	a
			dup
			add
			stm	a
			ldi	1
			sub
			dup
			nop
			bnz	imul_loop
			nop
			nop
			pop				// remove loop counter
			ldm	e
			nop
			bz	imul_nosign
			nop
			nop
			ldm	c
			ldi -1
			xor
			ldi 1
			add	nxt
imul_nosign:
			ldm	c	nxt
