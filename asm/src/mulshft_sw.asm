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
