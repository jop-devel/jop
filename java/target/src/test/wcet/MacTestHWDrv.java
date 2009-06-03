
package wcet;


import com.jopdesign.io.JeopardIOFactory;
import com.jopdesign.sys.Native;

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
__hw_alpha = _dereference ( alpha ) ;
__hw_beta = _dereference ( beta ) ;
// load parameters
control_channel.write(0x1010000 | (((__hw_size) >> 0) & 0xffff));
control_channel.write(0x1810000 | (((__hw_size) >> 16) & 0xffff));
control_channel.write(0x1020000 | (((__hw_alpha) >> 0) & 0xffff));
control_channel.write(0x1820000 | (((__hw_alpha) >> 16) & 0xffff));
control_channel.write(0x1030000 | (((__hw_beta) >> 0) & 0xffff));
control_channel.write(0x1830000 | (((__hw_beta) >> 16) & 0xffff));
// start
control_channel.write(0x1000001);
// run (wait while busy)
while ( 0 != ( _ccTransaction(0x1000000) & 1 )) // @WCA loop<=200
{ /* yield */ }
// get result
int __ret;
int __hw___ret = _ccTransaction(0x1040000) | ( _ccTransaction(0x1840000) << 16 );
// convert result
__ret = __hw___ret;
return __ret;
}


    static private MacTestHWCC control_channel ;

    private int _ccTransaction ( int msg )
    {
        int reply = 0;
        while (( reply & 0x7fff0000 ) != ( msg & 0x7fff0000 )) // @WCA loop<=2
        {
            control_channel.write(msg);
            reply = control_channel.read();
        } 
        /* assert high bit = 1 */
        return reply & 0xffff;
    }
                    
    private int _dereference ( int [] a )
    {
        return Native.rdMem ( Native.toInt ( a ) ) ;
    }

    public MacTestHWDrv () {}

    static {
      control_channel = new MacTestHWCC () ;
    }
}
