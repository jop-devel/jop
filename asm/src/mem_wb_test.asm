//
//	mem_wb_test.asm
//
//	test for simulation
//
//
//	'special' constant for a version number
//	gets written in RAM at position 64
//	update it when changing .asm, .inc or .vhdl files
//
version		= 20050220


			nop
			nop
			ldi	127
			nop			// written in adr/read stage!
			stsp		// someting strange in stack.vhd A->B !!!
			nop
			ldi	-1
			nop


			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop


			ldi	3
			stmra
			wait
			wait
			ldmrd


			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop

// test mem interface
//
			ldi 15

			// this sequence takes 6 cycles with ram_cnt=3
			stmra				// start read ext. mem
			wait				// one for fetch
			wait				// one for decode
			ldmrd		 		// read ext. mem

			ldi 7				// read addr.for back to back wr/rd
			ldi	32				// write data
			ldi	16				// write address

			// this sequence takes 6 cycles with ram_cnt=3
			stmwa				// write ext. mem address
			stmwd				// write ext. mem data
			wait
			wait

			stmra				// start read ext. mem
			wait				// one for fetch
			wait				// one for decode
			ldmrd		 		// read ext. mem

			pop
			pop



			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop

			// back to back write
			ldi	33				// write data
			ldi	17				// write address

			ldi	32				// write data
			ldi	16				// write address

			// this sequence takes 6 cycles with ram_cnt=3
			stmwa				// write ext. mem address
			stmwd				// write ext. mem data
			wait
			wait

			stmwa				// write ext. mem address
			stmwd				// write ext. mem data
			wait
			wait


			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
			nop
