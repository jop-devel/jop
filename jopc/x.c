#include <stdio.h>
#include <time.h>

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

int usCntClock() {
	return (int) clock()*(1000000/CLOCKS_PER_SEC);
}

static int divide;

int getMHz() {

	int i, tim;
	_int64 tsc1, tsc2; 

	i = usCntClock();
	while (i == (tim=usCntClock()))		// wait for a tick
		;
	tsc1 = readTSC();

	tim +=1000000;
	while (tim-usCntClock() > 0)		// wait one second
		;
	tsc2 = readTSC();
	tsc2 -= tsc1;
	tim = tsc2/1000000;

	divide = tim;

	return tim;
}

int usCnt() {

	return (int) (readTSC()/divide);
}

main(int argc, char *argv[]) {

	int i, j, tim, last;

	printf("%d\n", getMHz());

	for (i=0; i<100; ++i) {

		j = usCntClock();
		while (j == usCntClock())		// wait for a tick
			;

		tim = (int) (usCnt());
		printf("%ld ", tim-last);
		last = tim;
	}
}
