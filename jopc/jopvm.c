/**
 * jopvm.c
 *
 * nach Vorlage von Martin Schoeberls com.jopdesign.tools.JopSim
 *
 * Nils Hagge
 *
 *
 *	2003-10-11 nh Erstelldatum
 *	2003-12-05 ms ...
 */
 
#include <stdio.h> 
#include <string.h> 
#ifdef __nios__
#include "nios.h"
#else
#include <time.h> 
#endif
 
#define STATISTICS(op) /* nichts */
#define DEBUG(op) /* op oder nichts */
 
#define MAX_MEM 200000
#define MAX_JAVA 16384
#define MAX_BC 1024
#define MAX_STACK 256

#define SYS_INT 0xf0

#ifdef INCJAVA
int prog[] = {
	#include "java.bin"
};
#endif

int mem[MAX_MEM];
char bc[MAX_BC];
int stack[MAX_STACK];


int maxInstr, instrCnt;

int pc, cp, vp, sp, mp;
int heap, jjp, jjhp, moncnt;

//
//	simulate timer interrupt
//
static int nextTimerInt;
static int intPend;
static int interrupt;
static int intEna;

struct 
{
	char *name;
	int len;
	enum { IMP_ASM, IMP_JAVA, IMP_NO } imp;
	int cnt;
} jopInstruction[] =
{
	{"nop", 1, IMP_ASM, 1},		/* 0x00 */
	{"aconst_null", 1, IMP_JAVA, 1},	/* 0x01 */
	{"iconst_m1", 1, IMP_ASM, 1},					// 0x02
	{"iconst_0", 1, IMP_ASM, 1},		// 0x03
	{"iconst_1", 1, IMP_ASM, 1},		// 0x04
	{"iconst_2", 1, IMP_ASM, 1},		// 0x05
	{"iconst_3", 1, IMP_ASM, 1},		// 0x06
	{"iconst_4", 1, IMP_ASM, 1},		// 0x07

	{"iconst_5", 1, IMP_ASM, 1},		// 0x08
	{"lconst_0", 1, IMP_NO, 1},		// 0x09
	{"lconst_1", 1, IMP_NO, 1},		// 0x0A
	{"fconst_0", 1, IMP_NO, 1},		// 0x0B
	{"fconst_1", 1, IMP_NO, 1},		// 0x0C
	{"fconst_2", 1, IMP_NO, 1},		// 0x0D
	{"dconst_0", 1, IMP_NO, 1},		// 0x0E
	{"dconst_1", 1, IMP_NO, 1},		// 0x0F

	{"bipush", 2, IMP_ASM, 2},		// 0x10
	{"sipush", 3, IMP_ASM, 3},		// 0x11
	{"ldc", 2, IMP_ASM, 2},		// 0x12
	{"ldc_w", 3, IMP_NO, 1},		// 0x13
	{"ldc2_w", 3, IMP_ASM, 1},		// 0x14
	{"iload", 2, IMP_ASM, 2},		// 0x15
	{"lload", 2, IMP_NO, 1},		// 0x16
	{"fload", 2, IMP_NO, 1},		// 0x17

	{"dload", 2, IMP_NO, 1},		// 0x18
	{"aload", 2, IMP_ASM, 2},		// 0x19
	{"iload_0", 1, IMP_ASM, 1},		// 0x1A
	{"iload_1", 1, IMP_ASM, 1},		// 0x1B
	{"iload_2", 1, IMP_ASM, 1},		// 0x1C
	{"iload_3", 1, IMP_ASM, 1},		// 0x1D
	{"lload_0", 1, IMP_NO, 1},		// 0x1E
	{"lload_1", 1, IMP_NO, 1},		// 0x1F

	{"lload_2", 1, IMP_NO, 1},		// 0x20
	{"lload_3", 1, IMP_NO, 1},		// 0x21
	{"fload_0", 1, IMP_NO, 1},		// 0x22
	{"fload_1", 1, IMP_NO, 1},		// 0x23
	{"fload_2", 1, IMP_NO, 1},		// 0x24
	{"fload_3", 1, IMP_NO, 1},		// 0x25
	{"dload_0", 1, IMP_NO, 1},		// 0x26
	{"dload_1", 1, IMP_NO, 1},		// 0x27

	{"dload_2", 1, IMP_NO, 1},		// 0x28
	{"dload_3", 1, IMP_NO, 1},		// 0x29
	{"aload_0", 1, IMP_ASM, 1},		// 0x2A
	{"aload_1", 1, IMP_ASM, 1},		// 0x2B
	{"aload_2", 1, IMP_ASM, 1},		// 0x2C
	{"aload_3", 1, IMP_ASM, 1},		// 0x2D
	{"iaload", 1, IMP_ASM, 17},		// 0x2E
	{"laload", 1, IMP_NO, 1},		// 0x2F

	{"faload", 1, IMP_NO, 1},		// 0x30
	{"daload", 1, IMP_NO, 1},		// 0x31
	{"aaload", 1, IMP_NO, 1},		// 0x32
	{"baload", 1, IMP_NO, 1},		// 0x33
	{"caload", 1, IMP_NO, 1},		// 0x34
	{"saload", 1, IMP_NO, 1},		// 0x35
	{"istore", 2, IMP_ASM, 2},		// 0x36
	{"lstore", 2, IMP_NO, 1},		// 0x37

	{"fstore", 2, IMP_NO, 1},		// 0x38
	{"dstore", 2, IMP_NO, 1},		// 0x39
	{"astore", 2, IMP_ASM, 2},		// 0x3A
	{"istore_0", 1, IMP_ASM, 1},		// 0x3B
	{"istore_1", 1, IMP_ASM, 1},		// 0x3C
	{"istore_2", 1, IMP_ASM, 1},		// 0x3D
	{"istore_3", 1, IMP_ASM, 1},		// 0x3E
	{"lstore_0", 1, IMP_NO, 1},		// 0x3F

	{"lstore_1", 1, IMP_NO, 1},		// 0x40
	{"lstore_2", 1, IMP_NO, 1},		// 0x41
	{"lstore_3", 1, IMP_NO, 1},		// 0x42
	{"fstore_0", 1, IMP_NO, 1},		// 0x43
	{"fstore_1", 1, IMP_NO, 1},		// 0x44
	{"fstore_2", 1, IMP_NO, 1},		// 0x45
	{"fstore_3", 1, IMP_NO, 1},		// 0x46
	{"dstore_0", 1, IMP_NO, 1},		// 0x47

	{"dstore_1", 1, IMP_NO, 1},		// 0x48
	{"dstore_2", 1, IMP_NO, 1},		// 0x49
	{"dstore_3", 1, IMP_NO, 1},		// 0x4A
	{"astore_0", 1, IMP_ASM, 1},		// 0x4B
	{"astore_1", 1, IMP_ASM, 1},		// 0x4C
	{"astore_2", 1, IMP_ASM, 1},		// 0x4D
	{"astore_3", 1, IMP_ASM, 1},		// 0x4E
	{"iastore", 1, IMP_ASM, 18},		// 0x4F

	{"lastore", 1, IMP_NO, 1},		// 0x50
	{"fastore", 1, IMP_NO, 1},		// 0x51
	{"dastore", 1, IMP_NO, 1},		// 0x52
	{"aastore", 1, IMP_NO, 1},		// 0x53
	{"bastore", 1, IMP_NO, 1},		// 0x54
	{"castore", 1, IMP_NO, 1},		// 0x55
	{"sastore", 1, IMP_NO, 1},		// 0x56
	{"pop", 1, IMP_ASM, 1},		// 0x57

	{"pop2", 1, IMP_NO, 1},		// 0x58
	{"dup", 1, IMP_ASM, 1},		// 0x59
	{"dup_x1", 1, IMP_NO, 1},		// 0x5A
	{"dup_x2", 1, IMP_NO, 1},		// 0x5B
	{"dup2", 1, IMP_ASM, 1},		// 0x5C
	{"dup2_x1", 1, IMP_NO, 1},		// 0x5D
	{"dup2_x2", 1, IMP_NO, 1},		// 0x5E
	{"swap", 1, IMP_NO, 1},		// 0x5F

	{"iadd", 1, IMP_ASM, 1},		// 0x60
	{"ladd", 1, IMP_NO, 1},		// 0x61
	{"fadd", 1, IMP_NO, 1},		// 0x62
	{"dadd", 1, IMP_NO, 1},		// 0x63
	{"isub", 1, IMP_ASM, 1},		// 0x64
	{"lsub", 1, IMP_NO, 1},		// 0x65
	{"fsub", 1, IMP_NO, 1},		// 0x66
	{"dsub", 1, IMP_NO, 1},		// 0x67

	{"imul", 1, IMP_ASM, 800},		// 0x68
	{"lmul", 1, IMP_NO, 1},		// 0x69
	{"fmul", 1, IMP_NO, 1},		// 0x6A
	{"dmul", 1, IMP_NO, 1},		// 0x6B
	{"idiv", 1, IMP_ASM, 1300},		// 0x6C
	{"ldiv", 1, IMP_NO, 1},		// 0x6D
	{"fdiv", 1, IMP_NO, 1},		// 0x6E
	{"ddiv", 1, IMP_NO, 1},		// 0x6F

	{"irem", 1, IMP_ASM, 1300},		// 0x70
	{"lrem", 1, IMP_NO, 1},		// 0x71
	{"frem", 1, IMP_NO, 1},		// 0x72
	{"drem", 1, IMP_NO, 1},		// 0x73
	{"ineg", 1, IMP_ASM, 4},		// 0x74
	{"lneg", 1, IMP_NO, 1},		// 0x75
	{"fneg", 1, IMP_NO, 1},		// 0x76
	{"dneg", 1, IMP_NO, 1},		// 0x77

	{"ishl", 1, IMP_ASM, 1},		// 0x78
	{"lshl", 1, IMP_NO, 1},		// 0x79
	{"ishr", 1, IMP_ASM, 1},		// 0x7A
	{"lshr", 1, IMP_NO, 1},		// 0x7B
	{"iushr", 1, IMP_ASM, 1},					// 0x7C
	{"lushr", 1, IMP_NO, 1},					// 0x7D
	{"iand", 1, IMP_ASM, 1},		// 0x7E
	{"land", 1, IMP_NO, 1},		// 0x7F

	{"ior", 1, IMP_ASM, 1},		// 0x80
	{"lor", 1, IMP_NO, 1},		// 0x81
	{"ixor", 1, IMP_ASM, 1},		// 0x82
	{"lxor", 1, IMP_NO, 1},		// 0x83
	{"iinc", 3, IMP_ASM, 11},		// 0x84
	{"i2l", 1, IMP_NO, 1},		// 0x85
	{"i2f", 1, IMP_NO, 1},		// 0x86
	{"i2d", 1, IMP_NO, 1},		// 0x87

	{"l2i", 1, IMP_NO, 1},		// 0x88
	{"l2f", 1, IMP_NO, 1},		// 0x89
	{"l2d", 1, IMP_NO, 1},		// 0x8A
	{"f2i", 1, IMP_NO, 1},		// 0x8B
	{"f2l", 1, IMP_NO, 1},		// 0x8C
	{"f2d", 1, IMP_NO, 1},		// 0x8D
	{"d2i", 1, IMP_NO, 1},		// 0x8E
	{"d2l", 1, IMP_NO, 1},		// 0x8F

	{"d2f", 1, IMP_NO, 1},		// 0x90
	{"i2b", 1, IMP_NO, 1},		// 0x91
	{"i2c", 1, IMP_ASM, 2},		// 0x92
	{"i2s", 1, IMP_NO, 1},		// 0x93
	{"lcmp", 1, IMP_NO, 1},		// 0x94
	{"fcmpl", 1, IMP_NO, 1},					// 0x95
	{"fcmpg", 1, IMP_NO, 1},		// 0x96
	{"dcmpl", 1, IMP_NO, 1},		// 0x97

	{"dcmpg", 1, IMP_NO, 1},		// 0x98
	{"ifeq", 3, IMP_ASM, 4},		// 0x99
	{"ifne", 3, IMP_ASM, 4},		// 0x9A
	{"iflt", 3, IMP_ASM, 4},		// 0x9B
	{"ifge", 3, IMP_ASM, 4},		// 0x9C
	{"ifgt", 3, IMP_ASM, 4},		// 0x9D
	{"ifle", 3, IMP_ASM, 4},		// 0x9E
	{"if_icmpeq", 3, IMP_ASM, 4},		// 0x9F

	{"if_icmpne", 3, IMP_ASM, 4},		// 0xA0
	{"if_icmplt", 3, IMP_ASM, 4},		// 0xA1
	{"if_icmpge", 3, IMP_ASM, 4},		// 0xA2
	{"if_icmpgt", 3, IMP_ASM, 4},		// 0xA3
	{"if_icmple", 3, IMP_ASM, 4},		// 0xA4
	{"if_acmpeq", 3, IMP_ASM, 4},		// 0xA5
	{"if_acmpne", 3, IMP_ASM, 4},		// 0xA6
	{"goto", 3, IMP_ASM, 4},		// 0xA7

	{"jsr", 3, IMP_NO, 1},		// 0xA8
	{"ret", 2, IMP_NO, 1},		// 0xA9
	{"tableswitch", 0, IMP_NO, 1},	// 0xAA
	{"lookupswitch", 0, IMP_NO, 1},	// 0xAB
	{"ireturn", 1, IMP_ASM, 12},		// 0xAC
	{"lreturn", 1, IMP_NO, 1},		// 0xAD
	{"freturn", 1, IMP_NO, 1},		// 0xAE
	{"dreturn", 1, IMP_NO, 1},		// 0xAF

	{"areturn", 1, IMP_ASM, 1},		// 0xB0
	{"return", 1, IMP_ASM, 10},		// 0xB1
	{"getstatic", 3, IMP_ASM, 11},	// 0xB2		// derzeit!!!
	{"putstatic", 3, IMP_ASM, 11},	// 0xB3
	{"getfield", 3, IMP_ASM, 30},		// 0xB4
	{"putfield", 3, IMP_ASM, 30},		// 0xB5
	{"invokevirtual", 3, IMP_ASM, 30},	// 0xB6
	{"invokespecial", 3, IMP_ASM, 30},	// 0xB7

	{"invokestatic", 3, IMP_ASM, 30},	// 0xB8	cnt ????
	{"invokeinterface", 5, IMP_NO, 1},	// 0xB9
	{"unused_ba", 1, IMP_NO, 1},		// 0xBA
	{"new", 3, IMP_NO, 30},		// 0xBB
	{"newarray", 2, IMP_ASM, 26},		// 0xBC	// mit mem!!
	{"anewarray", 3, IMP_NO, 1},		// 0xBD
	{"arraylength", 1, IMP_ASM, 18},	// 0xBE		// mit mem!!
	{"athrow", 1, IMP_NO, 1},		// 0xBF

	{"checkcast", 3, IMP_NO, 1},		// 0xC0
	{"instanceof", 3, IMP_NO, 1},		// 0xC1
	{"monitorenter", 1, IMP_NO, 1},	// 0xC2
	{"monitorexit", 1, IMP_NO, 1},	// 0xC3
	{"wide", 0, IMP_NO, 1},		// 0xC4
	{"multianewarray", 4, IMP_NO, 1},	// 0xC5
	{"ifnull", 3, IMP_NO, 1},		// 0xC6
	{"ifnonnull", 3, IMP_NO, 1},		// 0xC7

	{"goto_w", 5, IMP_NO, 1},		// 0xC8
	{"jsr_w", 5, IMP_NO, 1},		// 0xC9
	{"breakpoint", 1, IMP_NO, 1},		// 0xCA

/**
 *	reserved instructions
 */
 
	{"resCB", 1, IMP_NO, 1},		// 0xCB
	{"resCC", 1, IMP_NO, 1},		// 0xCC
	{"resCD", 1, IMP_NO, 1},		// 0xCD
	{"resCE", 1, IMP_NO, 1},		// 0xCE
	{"resCF", 1, IMP_NO, 1},		// 0xCF

	{"jopsys_null", 1, IMP_NO, 1},	// 0xD0
	{"jopsys_rd", 1, IMP_ASM, 3},		// 0xD1
	{"jopsys_wr", 1, IMP_ASM, 3},		// 0xD2
	{"jopsys_rdmem", 1, IMP_ASM, 15},	// 0xD3
	{"jopsys_wrmem", 1, IMP_ASM, 15},	// 0xD4
	{"jopsys_rdint", 1, IMP_ASM, 8},	// 0xD5
	{"jopsys_wrint", 1, IMP_ASM, 8},	// 0xD6
	{"jopsys_getsp", 1, IMP_ASM, 3},	// 0xD7
	{"jopsys_setsp", 1, IMP_ASM, 4},	// 0xD8
	{"jopsys_getvp", 1, IMP_ASM, 1},	// 0xD9
	{"jopsys_setvp", 1, IMP_ASM, 2},	// 0xDA
	{"jopsys_int2ext", 1, IMP_ASM, 100},	// 0xDB
	{"jopsys_ext2int", 1, IMP_ASM, 100},	// 0xDC
	{"resDD", 1, IMP_NO, 1},		// 0xDD
	{"resDE", 1, IMP_NO, 1},		// 0xDE
	{"resDF", 1, IMP_NO, 1},		// 0xDF

	{"resE0", 1, IMP_NO, 1},		// 0xE0
	{"resE1", 1, IMP_NO, 1},		// 0xE1
	{"resE2", 1, IMP_NO, 1},		// 0xE2
	{"resE3", 1, IMP_NO, 1},		// 0xE3
	{"resE4", 1, IMP_NO, 1},		// 0xE4
	{"resE5", 1, IMP_NO, 1},		// 0xE5
	{"resE6", 1, IMP_NO, 1},		// 0xE6
	{"resE7", 1, IMP_NO, 1},		// 0xE7
	{"resE8", 1, IMP_NO, 1},		// 0xE8
	{"resE9", 1, IMP_NO, 1},		// 0xE9
	{"resEA", 1, IMP_NO, 1},		// 0xEA
	{"resEB", 1, IMP_NO, 1},		// 0xEB
	{"resEC", 1, IMP_NO, 1},		// 0xEC
	{"resED", 1, IMP_NO, 1},		// 0xED
	{"resEE", 1, IMP_NO, 1},		// 0xEE
	{"resEF", 1, IMP_NO, 1},		// 0xEF

	{"sys_int", 1, IMP_ASM, 1},		// 0xF0
	{"resF1", 1, IMP_NO, 1},		// 0xF1
	{"resF2", 1, IMP_NO, 1},		// 0xF2
	{"resF3", 1, IMP_NO, 1},		// 0xF3
	{"resF4", 1, IMP_NO, 1},		// 0xF4
	{"resF5", 1, IMP_NO, 1},		// 0xF5
	{"resF6", 1, IMP_NO, 1},		// 0xF6
	{"resF7", 1, IMP_NO, 1},		// 0xF7
	{"resF8", 1, IMP_NO, 1},		// 0xF8
	{"resF9", 1, IMP_NO, 1},		// 0xF9
	{"resFA", 1, IMP_NO, 1},		// 0xFA
	{"resFB", 1, IMP_NO, 1},		// 0xFB
	{"resFC", 1, IMP_NO, 1},		// 0xFC
	{"resFD", 1, IMP_NO, 1},		// 0xFD
	{"sys_noim", 1, IMP_ASM, 1},		/* 0xFE */
	{"sys_init", 1, IMP_ASM, 1}		/* 0xFF */
};


int readMem(int addr)
{
	STATISTICS(rdMemCnt++;)
	STATISTICS(ioCnt += 12;)
	
	if(!(0 <= addr && addr <= heap))	/* <= nicht < aber warum? */
		printf("wrong read address %d (max %d).\n", addr, heap), exit(1);
	
	return mem[addr];
}

void writeMem(int addr, int data) 
{
	STATISTICS(wrMemCnt++;)
	STATISTICS(ioCnt += 12;)

	if(!(0 <= addr && addr <= heap))
		printf("wrong write address %d (max %d).\n", addr, heap), exit(1);

	mem[addr] = data;
}

void invokestaticAddr(int);

void init(char *filename)
{
	int ptr;
	
	int i;
	long l;
#ifndef INCJAVA
	FILE *fp;
#endif
	char buf[10000];
	char *tok;



	heap = 0;
	moncnt = 1;

#ifdef INCJAVA
	for (heap=0; heap<(sizeof(prog)/sizeof(int)); ++heap) {
		mem[heap] = prog[heap];
	}
#else
	if ((fp=fopen(filename,"r"))==NULL) {
		printf("Error opening %s\n", filename);
		exit(-1);
	}

	while (fgets(buf, sizeof(buf), fp)!=NULL) {
		for (i=0; i<strlen(buf); ++i) {
			if (buf[i]=='/') {
				buf[i]=0;
				break;
			}
		}
		tok = strtok(buf, " ,\t");
		while (tok!=NULL) {
			if (sscanf(tok, "%ld", &l)==1) {
				mem[heap++] = l;
			}
			tok = strtok(NULL, " ,\t");
			if (heap>=MAX_JAVA) {
				printf("too many words (%d/%d)\n", heap, MAX_JAVA);
				exit(-1);
			}
		}
	}

	fclose(fp);
#endif

	printf("%d words of byte code (%d KB)\n", mem[0], mem[0]/256);
	printf("%d words extern ram (%d KB)\n", heap, heap/256);
#ifndef __nios__
	printf("This machine runns at %d MHz\n", getMHz());
#endif

	nextTimerInt = 0;
	intPend = 0;
	interrupt = 0;
	intEna = 0;

	pc = vp = 0;
	sp = 128;
	maxInstr = instrCnt = 0;
	
	ptr = readMem(0);
	jjp = readMem(ptr + 1);
	jjhp = readMem(ptr + 2);
	
	invokestaticAddr(ptr);
}

void loadBc(int start, int len) 
{
	/* high byte of word is first bc */
	int i;
	for(i = 0; i < len; ++i) 
	{
		unsigned int val = readMem(start+i);
		int j;
		for(j = 0; j < 4; ++j) 
		{
			bc[i * 4 + (3 - j)] = val;
			val >>= 8;
		}
	}
	STATISTICS(rdBcCnt += len;)
	STATISTICS(simCach(start, len);)
}


void invoke(int new_mp) 
{
	int old_vp = vp;
	int old_cp = cp;
	int old_mp = mp;
	int start, len, locals, args, old_sp;
	
	mp = new_mp;
	start = readMem(mp);
	len = start & 0x03ff;
	start = (unsigned int) start >> 10;
	cp = readMem(mp + 1);
	locals = ((unsigned int) cp >> 5) & 0x01f;
	args = cp & 0x01f;
	cp = (unsigned int) cp >> 10;

	old_sp = sp - args;
	vp = old_sp+1;
	sp += locals;

// System.out.println("inv: start: "+start+" len: "+len+" locals: "+locals+" args: "+args+" cp: "+cp);

	stack[++sp] = old_sp;
	stack[++sp] = pc;
	stack[++sp] = old_vp;
	stack[++sp] = old_cp;
	stack[++sp] = old_mp;

	loadBc(start, len);
	pc = 0;
}

void noim(int instr) 
{
/*
	printf("Bytecode %s nicht implementiert (mp = %d, pc = %d)\n",
		jopInstruction[instr].name, mp, pc);
*/
	 
	invoke(jjp + (instr << 1));
}

/**
 *	call function in JVM.java with constant on stack
 */

int readOpd16u();

void jjvmConst(int instr) 
{
	int idx = readOpd16u();
	int val = readMem(cp+idx);	/* read constant */
	stack[++sp] = val;						/* push on stack */
	invoke(jjp+(instr<<1));
}

void dump() 
{
	printf("vp = %d, sp = %d, pc = %d, Stack=[..., %d, %d, %d]\n",
		vp, sp, pc, stack[sp-2], stack[sp-1], stack[sp]);	
}


int readOpd16u()
{
	int idx;
	STATISTICS(instrBytesCnt += 2;)
	idx = ((bc[pc] << 8) | (bc[pc + 1] & 255)) & 0xFFFF;
	pc += 2;
	return idx;
}

int readOpd16s()
{
	int i = readOpd16u();
	if(i & 0x8000)
		i |= 0xFFFF0000;
	return i;
}

int readOpd8s()
{
	STATISTICS(instrBytes++;)
	return bc[pc++];
}

int readOpd8u()
{
	STATISTICS(instrBytes++;)
	return bc[pc++];
}

/*
	public static final int IO_CNT = 0;
	public static final int IO_INT_ENA = 0;
	public static final int IO_US_CNT = 1;
	public static final int IO_TIMER = 1;
	public static final int IO_SWINT = 2;
	public static final int IO_WD = 3;

	public static final int IO_STATUS = 4;
	public static final int IO_UART = 5;
*/
#define Native_IO_CNT 0
#define Native_IO_INT_ENA 0
#define Native_IO_US_CNT 1
#define Native_IO_TIMER 1
#define Native_IO_SWINT 2
#define Native_IO_WD 3

#define Native_IO_STATUS 4
#define Native_IO_UART 5

#ifdef __nios__

int usCnt() {

	return na_mhz_counter->np_cnt_val;
}

#else

#ifdef _MSC_VER
_int64 readTSC (void)
{
	_int64 t;
	unsigned int a,b;
	unsigned int *c = (unsigned int *)&t;
	_asm {
		_emit 0x0f;
		_emit 0x31;
		mov a,eax;
		mov b,edx;
	}
	c[0]=a;c[1]=b;
	return t;
}
#endif

#ifdef __GNUC__
long long readTSC (void)
{

  long long t;
  __asm__ __volatile__ (".byte 0x0f, 0x31" : "=A" (t));

  return t;      
}
#endif


int usCntClock() {
	return (int) (clock()*(1000000/CLOCKS_PER_SEC));
}

static long divide;

int getMHz() {

	int i, tim;
#ifdef _MSC_VER
	_int64 tsc1, tsc2; 
#endif
#ifdef __GNUC__
	long long tsc1, tsc2;
#endif

	i = usCntClock();
	while (i == (tim=usCntClock()))		// wait for a tick
		;
	tsc1 = readTSC();

	tim +=1000000;
	while (tim-usCntClock() > 0)		// wait one second
		;
	tsc2 = readTSC();
	tsc2 -= tsc1;
	tim = (tsc2+500000)/1000000;

	divide = tim;

	return tim;
}

int usCnt() {

	return (int) (readTSC()/divide);
}

#endif __nios__

void sysRd() 
{
	int addr = stack[sp];
	switch(addr) 
	{
		case Native_IO_STATUS:
			stack[sp] = -1;
			break;
		case Native_IO_UART:
			stack[sp] = 'u';
			break;
		case Native_IO_CNT:
#ifdef __nios__
			stack[sp] = usCnt()*20;
#else
			stack[sp] = readTSC(); // processor clock
#endif
			break;
		case Native_IO_US_CNT:
			stack[sp] = usCnt();
			break;
		case 11:
			stack[sp] = 0 /* ioCnt/24000 */;
			break;
		default:
			stack[sp] = 0;
	}
}

void sysWr() {

	int addr = stack[sp--];
	int val = stack[sp--];
	switch(addr) {

		case Native_IO_UART:
			// printf("\t->%c<-\n", val);
			printf("%c", val);
			fflush(stdout);
			break;
		case Native_IO_INT_ENA:
			intEna = val;
			break;
		case Native_IO_TIMER:
			intPend = 0;		// reset pending interrupt
			interrupt = 0;		// for shure ???
			nextTimerInt = val;
			break;
		case Native_IO_SWINT:
			if (!intPend) {
				interrupt = 1;
				intPend = 1;
			}
			break;
	}
}

/**
 *	start of JVM :-)
 */

void invokestatic();
	
void invokespecial() 
{
	invokestatic();				/* what's the difference? */
}

void invokevirtual() 
{
	int idx = readOpd16u();
	unsigned int off = readMem(cp+idx);			/* index in vt and arg count (-1) */
	int args = off & 0xff;
	int ref = stack[sp-args];
	int vt = readMem(ref-1);
	off = (unsigned int) off >> 8;
	
DEBUG(printf("invokevirtual: off = %d, args = %d, ref = %d, vt = %d, addr = %d\n",
		off, args, ref, vt, vt + off);)

	invoke(vt + off);
}

	void invokeinterface() {

		int idx, args, ref, vt, it, mp;
		unsigned int off;

		idx = readOpd16u();
		readOpd16u();				// read historical argument count and 0

		off = readMem(cp+idx);			// index in interface table

		args = off & 0xff;				// this is args count without obj-ref
		off = (unsigned int) off >> 8;
		ref = stack[sp-args];

		vt = readMem(ref-1);			// pointer to virtual table in obj-1
		it = readMem(vt-1);				// pointer to interface table one befor vt

		mp = readMem(it+off);
// System.out.println("invint: off: "+off+" args: "+args+" ref: "+ref+" vt: "+vt+" mp: "+(mp));
		invoke(mp);
	}

void invokestatic()
{
	int idx = readOpd16u();
	invokestaticAddr(cp + idx);
}

void invokestaticAddr(int ptr)
{
	invoke(readMem(ptr));
}

/**
 *	return wie es sein sollte (oder doch nicht?)
 */

void vreturn() 
{
	unsigned int start;
	int len;
DEBUG(dump();)
	
	mp = stack[sp--];
	cp = stack[sp--];
	vp = stack[sp--];
	pc = stack[sp--];
	sp = stack[sp]; /* Nils */

DEBUG(dump();)
	
	start = readMem(mp);
	len = start & 0x03ff;
	start = (unsigned int) start >> 10;
	// cp = readMem(mp+1)>>>10;

DEBUG(printf("start = %d, len = %d\n", start, len);)
	loadBc(start, len);
}

void ireturn() 
{
	int val = stack[sp--];
	vreturn();
	stack[++sp] = val;
}

/**
 *
 */

void putstatic() 
{
	int idx = readOpd16u();
	int addr = readMem(cp + idx);	// not now
// System.out.println("putstatic address: "+addr+" TOS: "+stack[sp]);
	writeMem(addr, stack[sp--]);
}

void getstatic() 
{
	int idx = readOpd16u();
	int addr = readMem(cp + idx);	// not now
// System.out.println("getstatic address: "+addr+" TOS: "+stack[sp]);
	stack[++sp] = readMem(addr);
}

void putfield() 
{
	int idx = readOpd16u();
	int off = readMem(cp+idx);
	int val = stack[sp--];
	writeMem(stack[sp--] + off, val);
}

void getfield() 
{
	int idx = readOpd16u();
	int off = readMem(cp + idx);
	stack[sp] = readMem(stack[sp] + off);
}

void interprete() 
{
	int new_pc;
	int ref, val, idx;
	int old_pc = -1;
	int old_mp = -1;
	char spc[80];

	int a, b, c, d;

	for (;;) 
	{
		int instr;
		
		/*
		 *	check for endless loop and stop
		 */
/*
		if(pc == old_pc && mp == old_mp) 
		{
			printf("\nendless loop\n");
			break;
		}
		
		old_pc = pc;
		old_mp = mp;
*/
		
		if(maxInstr != 0 && instrCnt >= maxInstr)
			break;

		/**
		 *	statistic
		 */
		STATISTICS(++instrBytesCnt;)
		++instrCnt;
		STATISTICS(if(sp > maxSp) maxSp = sp;)

		instr = bc[pc++] & 0x0ff;

//
//	interrupt handling
//
		if ((nextTimerInt-usCnt()<0) && !intPend) {
			intPend = 1;
			interrupt = 1;
		}
		if (interrupt && intEna) {
			instr = SYS_INT;
			interrupt = 0;		// reset int
		}

		STATISTICS(bcStat[instr]++;)
		STATISTICS(ioCnt += JopInstr.cnt(instr);)

		sprintf(spc, "%4d %s", pc - 1, jopInstruction[instr].name);
DEBUG(printf("%s\n", spc);)

		switch(instr) {

			case 0:		// nop
				break;
			case 1:		// aconst_null
				stack[++sp] = 0;
				break;
			case 2:		// iconst_m1
				stack[++sp] = -1;
				break;
			case 3:		// iconst_0
				stack[++sp] = 0;
				break;
			case 4:		// iconst_1
				stack[++sp] = 1;
				break;
			case 5:		// iconst_2
				stack[++sp] = 2;
				break;
			case 6:		// iconst_3
				stack[++sp] = 3;
				break;
			case 7:		// iconst_4
				stack[++sp] = 4;
				break;
			case 8:		// iconst_5
				stack[++sp] = 5;
				break;
			case 9 :		// lconst_0
				stack[++sp] = 0;
				stack[++sp] = 0;
				break;
			case 10 :		// lconst_1
				stack[++sp] = 0;
				stack[++sp] = 1;
				break;
			case 11 :		// fconst_0
				noim(11);
				break;
			case 12 :		// fconst_1
				noim(12);
				break;
			case 13 :		// fconst_2
				noim(13);
				break;
			case 14 :		// dconst_0
				noim(14);
				break;
			case 15 :		// dconst_1
				noim(15);
				break;
			case 16 :		// bipush
				stack[++sp] = readOpd8s();
				break;
			case 17 :		// sipush
				stack[++sp] = readOpd16s();
				break;
			case 18 :		// ldc
				stack[++sp] = readMem(cp+readOpd8u());
				break;
			case 19 :		// ldc_w
				stack[++sp] = readMem(cp+readOpd16u());
				break;
			case 20 :		// ldc2_w
				idx = readOpd16u();
				stack[++sp] = readMem(cp+idx);
				stack[++sp] = readMem(cp+idx+1);
				break;
			case 25 :		// aload
			case 23 :		// fload
			case 21 :		// iload
				idx = readOpd8u();
				stack[++sp] = stack[vp+idx];
				break;
			case 22 :		// lload
				noim(22);
				break;
			case 24 :		// dload
				noim(24);
				break;
			case 42 :		// aload_0
			case 34 :		// fload_0
			case 26 :		// iload_0
				stack[++sp] = stack[vp];
				break;
			case 43 :		// aload_1
			case 35 :		// fload_1
			case 27 :		// iload_1
				stack[++sp] = stack[vp+1];
				break;
			case 44 :		// aload_2
			case 36 :		// fload_2
			case 28 :		// iload_2
				stack[++sp] = stack[vp+2];
				break;
			case 45 :		// aload_3
			case 37 :		// fload_3
			case 29 :		// iload_3
				stack[++sp] = stack[vp+3];
				break;
			case 30 :		// lload_0
				stack[++sp] = stack[vp];
				stack[++sp] = stack[vp+1];
				break;
			case 31 :		// lload_1
				stack[++sp] = stack[vp+1];
				stack[++sp] = stack[vp+2];
				break;
			case 32 :		// lload_2
				stack[++sp] = stack[vp+2];
				stack[++sp] = stack[vp+3];
				break;
			case 33 :		// lload_3
				stack[++sp] = stack[vp+3];
				stack[++sp] = stack[vp+4];
				break;
			case 38 :		// dload_0
				noim(38);
				break;
			case 39 :		// dload_1
				noim(39);
				break;
			case 40 :		// dload_2
				noim(40);
				break;
			case 41 :		// dload_3
				noim(41);
				break;
			case 50 :		// aaload
			case 51 :		// baload
			case 52 :		// caload
			case 48 :		// faload
			case 46 :		// iaload
			case 53 :		// saload
				ref = stack[sp--];	// index
				ref += stack[sp--];	// ref
				stack[++sp] = readMem(ref);
				break;
			case 47 :		// laload
				noim(47);
				break;
			case 49 :		// daload
				noim(49);
				break;
			case 58 :		// astore
			case 56 :		// fstore
			case 54 :		// istore
				idx = readOpd8u();
				stack[vp+idx] = stack[sp--];
				break;
			case 55 :		// lstore
				noim(55);
				break;
			case 57 :		// dstore
				noim(57);
				break;
			case 75 :		// astore_0
			case 67 :		// fstore_0
			case 59 :		// istore_0
				stack[vp] = stack[sp--];
				break;
			case 76 :		// astore_1
			case 68 :		// fstore_1
			case 60 :		// istore_1
				stack[vp+1] = stack[sp--];
				break;
			case 77 :		// astore_2
			case 69 :		// fstore_2
			case 61 :		// istore_2
				stack[vp+2] = stack[sp--];
				break;
			case 78 :		// astore_3
			case 70 :		// fstore_3
			case 62 :		// istore_3
				stack[vp+3] = stack[sp--];
				break;
			case 63 :		// lstore_0
				stack[vp+1] = stack[sp--];
				stack[vp] = stack[sp--];
				break;
			case 64 :		// lstore_1
				stack[vp+2] = stack[sp--];
				stack[vp+1] = stack[sp--];
				break;
			case 65 :		// lstore_2
				stack[vp+3] = stack[sp--];
				stack[vp+2] = stack[sp--];
				break;
			case 66 :		// lstore_3
				stack[vp+4] = stack[sp--];
				stack[vp+3] = stack[sp--];
				break;
			case 71 :		// dstore_0
				noim(71);
				break;
			case 72 :		// dstore_1
				noim(72);
				break;
			case 73 :		// dstore_2
				noim(73);
				break;
			case 74 :		// dstore_3
				noim(74);
				break;
			case 83 :		// aastore
			case 84 :		// bastore
			case 85 :		// castore
			case 81 :		// fastore
			case 79 :		// iastore
			case 86 :		// sastore
				val = stack[sp--];	// value
				ref = stack[sp--];	// index
				ref += stack[sp--];	// ref
				writeMem(ref, val);
				break;
			case 80 :		// lastore
				noim(80);
				break;
			case 82 :		// dastore
				noim(82);
				break;
			case 87 :		// pop
				sp--;
				break;
			case 88 :		// pop2
				sp--;
				sp--;
				break;
			case 89 :		// dup
				val = stack[sp];
				stack[++sp] = val;
				break;
			case 90 :		// dup_x1
				a = stack[sp--];
				b = stack[sp--];
				stack[++sp] = a;
				stack[++sp] = b;
				stack[++sp] = a;
				break;
			case 91 :		// dup_x2
				noim(91);
				break;
			case 92 :		// dup2
				a = stack[sp--];
				b = stack[sp--];
				stack[++sp] = b;
				stack[++sp] = a;
				stack[++sp] = b;
				stack[++sp] = a;
				break;
			case 93 :		// dup2_x1
				noim(93);
				break;
			case 94 :		// dup2_x2
				noim(94);
				break;
			case 95 :		// swap
				noim(95);
				break;
			case 96 :		// iadd
				val = stack[sp-1] + stack[sp];
				stack[--sp] = val;
				break;
			case 97 :		// ladd
				noim(97);
				break;
			case 98 :		// fadd
				noim(98);
				break;
			case 99 :		// dadd
				noim(99);
				break;
			case 100 :		// isub
				val = stack[sp-1] - stack[sp];
				stack[--sp] = val;
				break;
			case 101 :		// lsub
				noim(101);
				break;
			case 102 :		// fsub
				noim(102);
				break;
			case 103 :		// dsub
				noim(103);
				break;
			case 104 :		// imul
				val = stack[sp-1] * stack[sp];
				stack[--sp] = val;
				break;
			case 105 :		// lmul
				noim(105);
				break;
			case 106 :		// fmul
				noim(106);
				break;
			case 107 :		// dmul
				noim(107);
				break;
			case 108 :		// idiv
				val = stack[sp-1] / stack[sp];
				stack[--sp] = val;
				break;
			case 109 :		// ldiv
				noim(109);
				break;
			case 110 :		// fdiv
				noim(110);
				break;
			case 111 :		// ddiv
				noim(111);
				break;
			case 112 :		// irem
				val = stack[sp-1] % stack[sp];
				stack[--sp] = val;
				break;
			case 113 :		// lrem
				noim(113);
				break;
			case 114 :		// frem
				noim(114);
				break;
			case 115 :		// drem
				noim(115);
				break;
			case 116 :		// ineg
				stack[sp] = -stack[sp];
				break;
			case 117 :		// lneg
				noim(117);
				break;
			case 118 :		// fneg
				noim(118);
				break;
			case 119 :		// dneg
				noim(119);
				break;
			case 120 :		// ishl
				val = stack[sp-1] << stack[sp];
				stack[--sp] = val;
				break;
			case 121 :		// lshl
				noim(121);
				break;
			case 122 :		// ishr
				val = stack[sp-1] >> stack[sp];
				stack[--sp] = val;
				break;
			case 123 :		// lshr
				noim(123);
				break;
			case 124 :		// iushr
				val = stack[sp-1] >> stack[sp];
				stack[--sp] = val;
				break;
			case 125 :		// lushr
				noim(125);
				break;
			case 126 :		// iand
				val = stack[sp-1] & stack[sp];
				stack[--sp] = val;
				break;
			case 127 :		// land
				noim(127);
				break;
			case 128 :		// ior
				val = stack[sp-1] | stack[sp];
				stack[--sp] = val;
				break;
			case 129 :		// lor
				noim(129);
				break;
			case 130 :		// ixor
				val = stack[sp-1] ^ stack[sp];
				stack[--sp] = val;
				break;
			case 131 :		// lxor
				noim(131);
				break;
			case 132 :		// iinc
				idx = readOpd8u();
				stack[vp+idx] = stack[vp+idx]+readOpd8s();
				break;
			case 133 :		// i2l
				noim(133);
				break;
			case 134 :		// i2f
				noim(134);
				break;
			case 135 :		// i2d
				noim(135);
				break;
			case 136 :		// l2i
				val = stack[sp];	// low part
				--sp;				// drop high word
				stack[sp] = val;	// low on stack
				break;
			case 137 :		// l2f
				noim(137);
				break;
			case 138 :		// l2d
				noim(138);
				break;
			case 139 :		// f2i
				noim(139);
				break;
			case 140 :		// f2l
				noim(140);
				break;
			case 141 :		// f2d
				noim(141);
				break;
			case 142 :		// d2i
				noim(142);
				break;
			case 143 :		// d2l
				noim(143);
				break;
			case 144 :		// d2f
				noim(144);
				break;
			case 145 :		// i2b
				noim(145);
				break;
			case 146 :		// i2c
				stack[sp] = stack[sp] & 0x0ffff;
				break;
			case 147 :		// i2s
				noim(147);
				break;
			case 148 :		// lcmp
				noim(148);
				break;
			case 149 :		// fcmpl
				noim(149);
				break;
			case 150 :		// fcmpg
				noim(150);
				break;
			case 151 :		// dcmpl
				noim(151);
				break;
			case 152 :		// dcmpg
				noim(152);
				break;
			case 153 :		// ifeq
			case 198 :		// ifnull
				new_pc = pc-1;
				new_pc += readOpd16s(); /* Nils */
				sp--;
				if (stack[sp+1] == 0) pc = new_pc;
				break;
			case 154 :		// ifne
			case 199 :		// ifnonnull
				new_pc = pc-1;
				new_pc += readOpd16s(); /* Nils */
				sp--;
				if (stack[sp+1] != 0) pc = new_pc;
				break;
			case 155 :		// iflt
				new_pc = pc-1; 
				new_pc += readOpd16s(); /* Nils */
				sp--;
				if (stack[sp+1] < 0) pc = new_pc;
				break;
			case 156 :		// ifge
				new_pc = pc-1; 
				new_pc += readOpd16s();
				sp--;
				if (stack[sp+1] >= 0) pc = new_pc;
				break;
			case 157 :		// ifgt
				new_pc = pc-1;
				new_pc += readOpd16s();
				sp--;
				if (stack[sp+1] > 0) pc = new_pc;
				break;
			case 158 :		// ifle
				new_pc = pc-1;
				new_pc += readOpd16s();
				sp--;
				if (stack[sp+1] <= 0) pc = new_pc;
				break;
			case 159 :		// if_icmpeq
			case 165 :		// if_acmpeq
				new_pc = pc-1;
				new_pc += readOpd16s();
				sp -= 2;
				if (stack[sp+1] == stack[sp+2]) pc = new_pc;
				break;
			case 160 :		// if_icmpne
			case 166 :		// if_acmpne
				new_pc = pc-1;
				new_pc += readOpd16s();
				sp -= 2;
				if (stack[sp+1] != stack[sp+2]) pc = new_pc;
				break;
			case 161 :		// if_icmplt
				new_pc = pc-1;
				new_pc += readOpd16s(); /* Nils */
				sp -= 2;
				if (stack[sp+1] < stack[sp+2]) pc = new_pc;
				break;
			case 162 :		// if_icmpge
				new_pc = pc-1;
				new_pc += readOpd16s();
				sp -= 2;
				if (stack[sp+1] >= stack[sp+2]) pc = new_pc;
				break;
			case 163 :		// if_icmpgt
				new_pc = pc-1;
				new_pc += readOpd16s();
				sp -= 2;
				if (stack[sp+1] > stack[sp+2]) pc = new_pc;
				break;
			case 164 :		// if_icmple
				new_pc = pc-1;
				new_pc += readOpd16s();
				sp -= 2;
				if (stack[sp+1] <= stack[sp+2]) pc = new_pc;
				break;
			case 167 :		// goto
				new_pc = pc-1;
				new_pc += readOpd16s();
				pc = new_pc;
				break;
			case 168 :		// jsr
				noim(168);
				break;
			case 169 :		// ret
				noim(169);
				break;
			case 170 :		// tableswitch
				noim(170);
				break;
			case 171 :		// lookupswitch
				noim(171);
				break;
			case 176 :		// areturn
			case 174 :		// freturn
			case 172 :		// ireturn
				ireturn();
				break;
			case 173 :		// lreturn
				noim(173);
				break;
			case 175 :		// dreturn
				noim(175);
				break;
			case 177 :		// return
				vreturn();
				break;
			case 178 :		// getstatic
				getstatic();
				break;
			case 179 :		// putstatic
				putstatic();
				break;
			case 180 :		// getfield
				getfield();
				break;
			case 181 :		// putfield
				putfield();
				break;
			case 182 :		// invokevirtual
				invokevirtual();
				break;
			case 183 :		// invokespecial
				invokespecial();
				break;
			case 184 :		// invokestatic
				invokestatic();
				break;
			case 185 :		// invokeinterface
				noim(185);
				break;
			case 186 :		// unused_ba
				noim(186);
				break;
			case 187 :		// new
				jjvmConst(187);

/*	use function in JVM.java
				idx = readOpd16u();
				val = readMem(cp+idx);	// pointer to class struct
				writeMem(heap, val+2);	// pointer to method table on objectref-1
				++heap;
				val = readMem(val);		// instance size
// TODO init object to zero
				stack[++sp] = heap;		// objectref
				heap += val;
System.out.println("new heap: "+heap);
*/
				break;
			case 188 :		// newarray
				readOpd8u();		// ignore typ
				val = stack[sp--];	// count from stack
				writeMem(heap, val);
				++heap;
		 // geaendert in (Nils)
		 //	 writeMem(heap++, val);
				stack[++sp] = heap;	// ref to first element
				heap += val;
// System.out.println("newarray heap: "+heap);
				break;
			case 189 :		// anewarray
				jjvmConst(189);
				break;
			case 190 :		// arraylength
				ref = stack[sp--];	// ref from stack
				--ref;				// point to count
				stack[++sp] = readMem(ref);
				break;
			case 191 :		// athrow
				noim(191);
				break;
			case 192 :		// checkcast
				noim(192);
				break;
			case 193 :		// instanceof
				noim(193);
				break;
			case 194 :		// monitorenter
				sp--;		// we don't use the objref
				intEna = 0;
				++moncnt;
				// noim(194);
				break;
			case 195 :		// monitorexit
				sp--;		// we don't use the objref
				--moncnt;
				if (moncnt==0) {
					intEna = 1;
				}
				// noim(195);
				break;
			case 196 :		// wide
				noim(196);
				break;
			case 197 :		// multianewarray
				noim(197);
				break;
			case 200 :		// goto_w
				noim(200);
				break;
			case 201 :		// jsr_w
				noim(201);
				break;
			case 202 :		// breakpoint
				noim(202);
				break;
			case 203 :		// resCB
				noim(203);
				break;
			case 204 :		// resCC
				noim(204);
				break;
			case 205 :		// resCD
				noim(205);
				break;
			case 206 :		// resCE
				noim(206);
				break;
			case 207 :		// resCF
				noim(207);
				break;
			case 208 :		// jopsys_null
				noim(208);
				break;
			case 209 :		// jopsys_rd
				sysRd();
				break;
			case 210 :		// jopsys_wr
				sysWr();
				break;
			case 211 :		// jopsys_rdmem
				ref = stack[sp--];
				stack[++sp] = readMem(ref);
				break;
			case 212 :		// jopsys_wrmem
				ref = stack[sp--];
				val = stack[sp--];
				writeMem(ref, val);
				break;
			case 213 :		// jopsys_rdint
				ref = stack[sp--];
//
//	first variables in jvm.asm
//
//	mp		?		// pointer to method struct
//	cp		?		// pointer to constants
//	heap	?		// start of heap
//
//	jjp		?		// pointer to meth. table of Java JVM functions
//	jjhp	?		// pointer to meth. table of Java JVM help functions
//
//	moncnt	?		// counter for monitor

				if(ref == 0) 
					val = mp;
				else if(ref == 1) 
					val = cp;
				else if(ref == 2)
					val = heap;
				else if(ref == 3)
					val = jjp;
				else if(ref == 4)
					val = jjhp;
				else if(ref == 5)
					val = moncnt;
				else 
					val = stack[ref];
	
				stack[++sp] = val;
				break;
			case 214:		// jopsys_wrint
				ref = stack[sp--];
				val = stack[sp--];
				if(ref == 0) 
					mp = val;
				else if(ref == 1)
					cp = val;
				else if(ref == 2)
					heap = val;
				else if(ref == 3)
					jjp = val;
				else if(ref == 4)
					jjhp = val;
				else if(ref == 5)
					moncnt = val;
				else
					stack[ref] = val;
				break;
			case 215:		// jopsys_getsp
				val = sp;
				stack[++sp] = val;
				break;
			case 216:		// jopsys_setsp
				val = stack[sp--];
				sp = val;
				break;
			case 217:		// jopsys_getvp
				stack[++sp] = vp;
				break;
			case 218 :		// jopsys_setvp
				vp = stack[sp--];
				break;
			case 219 :		// jopsys_int2ext
// public static native void int2extMem(int intAdr, int extAdr, int cnt);
				a = stack[sp--];
				b = stack[sp--];
				c = stack[sp--];
				for(; a>=0; --a) {
					writeMem(b+a, stack[c+a]);
				}
				break;
			case 220 :		// jopsys_ext2int
// public static native void ext2intMem(int extAdr, int intAdr, int cnt);
				a = stack[sp--];
				b = stack[sp--];
				c = stack[sp--];
				for(; a>=0; --a) {
					stack[b+a] = readMem(c+a);
				}
				break;
			case 221 :		// resDD
				noim(221);
				break;
			case 222 :		// resDE
				noim(222);
				break;
			case 223 :		// resDF
				noim(223);
				break;
			case 224 :		// resE0
				noim(224);
				break;
			case 225 :		// resE1
				noim(225);
				break;
			case 226 :		// resE2
				noim(226);
				break;
			case 227 :		// resE3
				noim(227);
				break;
			case 228 :		// resE4
				noim(228);
				break;
			case 229 :		// resE5
				noim(229);
				break;
			case 230 :		// resE6
				noim(230);
				break;
			case 231 :		// resE7
				noim(231);
				break;
			case 232 :		// resE8
				noim(232);
				break;
			case 233 :		// resE9
				noim(233);
				break;
			case 234 :		// resEA
				noim(234);
				break;
			case 235 :		// resEB
				noim(235);
				break;
			case 236 :		// resEC
				noim(236);
				break;
			case 237 :		// resED
				noim(237);
				break;
			case 238 :		// resEE
				noim(238);
				break;
			case 239 :		// resEF
				noim(239);
				break;
			case 240 :		// sys_int
				--pc;		// correct wrong increment on jpc
				invoke(jjhp);	// interrupt() is at offset 0
				break;
			case 241 :		// resF1
				noim(241);
				break;
			case 242 :		// resF2
				noim(242);
				break;
			case 243 :		// resF3
				noim(243);
				break;
			case 244 :		// resF4
				noim(244);
				break;
			case 245 :		// resF5
				noim(245);
				break;
			case 246 :		// resF6
				noim(246);
				break;
			case 247 :		// resF7
				noim(247);
				break;
			case 248 :		// resF8
				noim(248);
				break;
			case 249 :		// resF9
				noim(249);
				break;
			case 250 :		// resFA
				noim(250);
				break;
			case 251 :		// resFB
				noim(251);
				break;
			case 252 :		// resFC
				noim(252);
				break;
			case 253 :		// resFD
				noim(253);
				break;
			case 254 :		// sys_noim
				noim(254);
				break;
			case 255 :		// sys_init
				noim(255);
				break;
			default:
				noim(instr);
		}
	}
}


int main(int argc, char **arg)
{
	int i;
	if (sizeof(int) != 4) {
		printf("wrong int size\n");
		exit(-1);
	}
	init(arg[1]);
	interprete();
	return 0;
}
