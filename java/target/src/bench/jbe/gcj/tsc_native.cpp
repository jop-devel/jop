#include <gcj/cni.h>
#include "TSC.h"

	jlong jbe::gcj::TSC::read(void) {

		long long t;
		__asm__ __volatile__ (".byte 0x0f, 0x31" : "=A" (t));

		return (jlong) t;      
	}
