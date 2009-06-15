package wcet;



//import com.jopdesign.io.ControlChannel;
//import com.jopdesign.io.JeopardIOFactory;
import com.jopdesign.sys.Native;
import com.jopdesign.sys.Const;
/* coprocessor id 1 */
public class MacTestHWDrv {
public int mac1 (
		int size ,
		int[] alpha ,
		int[] beta )
{
int __hw_size; // 0x1
int __hw_alpha; // 0x2
int __hw_beta; // 0x3
// convert parameters
__hw_size = size;
__hw_alpha = Native.rdMem ( Native.toInt ( alpha ) );
__hw_beta = Native.rdMem ( Native.toInt ( beta ) );
// I/O address
int __cci_addr = Const.IO_BASE + 0x30;
// create messages
int __msg0 = 0x1010000 | (((__hw_size) >> 0) & 0xffff);
int __msg1 = 0x1810000 | (((__hw_size) >> 16) & 0xffff);
int __msg2 = 0x1020000 | (((__hw_alpha) >> 0) & 0xffff);
int __msg3 = 0x1820000 | (((__hw_alpha) >> 16) & 0xffff);
int __msg4 = 0x1030000 | (((__hw_beta) >> 0) & 0xffff);
int __msg5 = 0x1830000 | (((__hw_beta) >> 16) & 0xffff);
int __msg6 = 0x1000001;
int __msg7 = 0x1000000;
int __msg8 = 0xffff;
int __msg9 = 0x7fff0000;
int __msg10 = 0x1840000;
int __msg11 = 0x1040000;
// load parameters
Native.wrMem(__msg0, __cci_addr);
Native.wrMem(__msg1, __cci_addr);
Native.wrMem(__msg2, __cci_addr);
Native.wrMem(__msg3, __cci_addr);
Native.wrMem(__msg4, __cci_addr);
Native.wrMem(__msg5, __cci_addr);
// start
Native.wrMem(__msg6, __cci_addr);
// run (wait while busy)
int rc = 1;

    {   // _ccTransaction(0x1000000)
        int reply_masked = 0;
        int msg_masked = __msg7 & __msg9;
        while (( reply_masked != msg_masked ) || (( rc & 1 ) != 0 )) { // @WCA loop<=1278
            Native.wrMem(__msg7, __cci_addr);
            rc = Native.rdMem(__cci_addr);
            reply_masked = rc & __msg9;
        }
        rc &= __msg8;
    }
// get result
int __ret, __hw___ret = 0;

    {   // _ccTransaction(0x1840000)
        int reply_masked = 0;
        int msg_masked = __msg10 & __msg9;
        while (( reply_masked != msg_masked )) { // @WCA loop=1
            Native.wrMem(__msg10, __cci_addr);
            __hw___ret = Native.rdMem(__cci_addr);
            reply_masked = __hw___ret & __msg9;
        }
        __hw___ret &= __msg8;
    }

    {   // _ccTransaction(0x1040000)
        int reply_masked = 0;
        int msg_masked = __msg11 & __msg9;
        while (( reply_masked != msg_masked )) { // @WCA loop=1
            Native.wrMem(__msg11, __cci_addr);
            rc = Native.rdMem(__cci_addr);
            reply_masked = rc & __msg9;
        }
        rc &= __msg8;
    }
__hw___ret = rc | ( __hw___ret << 16 );
// convert result
__ret = __hw___ret;
return __ret;
}



public final static MacTestHWDrv INSTANCE = new MacTestHWDrv () ;
public static MacTestHWDrv getInstance () {
return MacTestHWDrv.INSTANCE;
}
protected MacTestHWDrv () {

}
}
