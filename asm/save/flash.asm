// ************** change for load from flash *********************
			ldm addr
			stmra				// read ext. mem, mem_bsy comes one cycle later
			ldm	addr
			ldi	1
			add
			stm	addr
			wait
			wait
			ldmrd		 		// read ext. mem
// ************** end change for load from flash *********************
