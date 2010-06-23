package org.bouncycastle.crypto.engines;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * an implementation of the AES (Rijndael), from FIPS-197.
 * <p>
 * For further details see: <a href="http://csrc.nist.gov/encryption/aes/">http://csrc.nist.gov/encryption/aes/</a>.
 *
 * This implementation is based on optimizations from Dr. Brian Gladman's paper and C code at
 * <a href="http://fp.gladman.plus.com/cryptography_technology/rijndael/">http://fp.gladman.plus.com/cryptography_technology/rijndael/</a>
 *
 * There are three levels of tradeoff of speed vs memory
 * Because java has no preprocessor, they are written as three separate classes from which to choose
 *
 * The fastest uses 8Kbytes of static tables to precompute round calculations, 4 256 word tables for encryption
 * and 4 for decryption.
 *
 * The middle performance version uses only one 256 word table for each, for a total of 2Kbytes,
 * adding 12 rotate operations per round to compute the values contained in the other tables from
 * the contents of the first
 *
 * The slowest version uses no static tables at all and computes the values
 * in each round.
 * <p>
 * This file contains the slowest performance version with no static tables
 * for round precomputation, but it has the smallest foot print.
 *
 */
public class AESLightEngine implements BlockCipher {

  // The S box
  private static final byte[]  S;
  static {
    final String  Sdef =  
        "\143\174\167\173\362\153\157\305" +
        "\060\001\147\053\376\327\253\166" +
        "\312\202\311\175\372\131\107\360" +
        "\255\324\242\257\234\244\162\300" +
        "\267\375\223\046\066\077\367\314" +
        "\064\245\345\361\161\330\061\025" +
        "\004\307\043\303\030\226\005\232" +
        "\007\022\200\342\353\047\262\165" +
        "\011\203\054\032\033\156\132\240" +
        "\122\073\326\263\051\343\057\204" +
        "\123\321\000\355\040\374\261\133" +
        "\152\313\276\071\112\114\130\317" +
        "\320\357\252\373\103\115\063\205" +
        "\105\371\002\177\120\074\237\250" +
        "\121\243\100\217\222\235\070\365" +
        "\274\266\332\041\020\377\363\322" +
        "\315\014\023\354\137\227\104\027" +
        "\304\247\176\075\144\135\031\163" +
        "\140\201\117\334\042\052\220\210" +
        "\106\356\270\024\336\136\013\333" +
        "\340\062\072\012\111\006\044\134" +
        "\302\323\254\142\221\225\344\171" +
        "\347\310\067\155\215\325\116\251" +
        "\154\126\364\352\145\172\256\010" +
        "\272\170\045\056\034\246\264\306" +
        "\350\335\164\037\113\275\213\212" +
        "\160\076\265\146\110\003\366\016" +
        "\141\065\127\271\206\301\035\236" +
        "\341\370\230\021\151\331\216\224" +
        "\233\036\207\351\316\125\050\337" +
        "\214\241\211\015\277\346\102\150" +
        "\101\231\055\017\260\124\273\026";
    int  i;
    final byte[]  r = new byte[i=256];
    while(--i >= 0)  r[i] = (byte)Sdef.charAt(i);
    S = r;
  }
    
  // The inverse S-box
  private static final byte[]  Si;
  static {
    final String  Sidef =  
        "\122\011\152\325\060\066\245\070" +
        "\277\100\243\236\201\363\327\373" +
        "\174\343\071\202\233\057\377\207" +
        "\064\216\103\104\304\336\351\313" +
        "\124\173\224\062\246\302\043\075" +
        "\356\114\225\013\102\372\303\116" +
        "\010\056\241\146\050\331\044\262" +
        "\166\133\242\111\155\213\321\045" +
        "\162\370\366\144\206\150\230\026" +
        "\324\244\134\314\135\145\266\222" +
        "\154\160\110\120\375\355\271\332" +
        "\136\025\106\127\247\215\235\204" +
        "\220\330\253\000\214\274\323\012" +
        "\367\344\130\005\270\263\105\006" +
        "\320\054\036\217\312\077\017\002" +
        "\301\257\275\003\001\023\212\153" +
        "\072\221\021\101\117\147\334\352" +
        "\227\362\317\316\360\264\346\163" +
        "\226\254\164\042\347\255\065\205" +
        "\342\371\067\350\034\165\337\156" +
        "\107\361\032\161\035\051\305\211" +
        "\157\267\142\016\252\030\276\033" +
        "\374\126\076\113\306\322\171\040" +
        "\232\333\300\376\170\315\132\364" +
        "\037\335\250\063\210\007\307\061" +
        "\261\022\020\131\047\200\354\137" +
        "\140\121\177\251\031\265\112\015" +
        "\055\345\172\237\223\311\234\357" +
        "\240\340\073\115\256\052\365\260" +
        "\310\353\273\074\203\123\231\141" +
        "\027\053\004\176\272\167\326\046" +
        "\341\151\024\143\125\041\014\175";
    int  i;
    final byte[]  r = new byte[i=256];
    while(--i >= 0)  r[i] = (byte)Sidef.charAt(i);
    Si = r;
  }

    // vector used in calculating key schedule (powers of x in GF(256))
    private static final int[] rcon = {
         0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8, 0xab, 0x4d, 0x9a,
         0x2f, 0x5e, 0xbc, 0x63, 0xc6, 0x97, 0x35, 0x6a, 0xd4, 0xb3, 0x7d, 0xfa, 0xef, 0xc5, 0x91 };

    private int shift(
        int     r,
        int     shift)
    {
        return (r >>> shift) | (r << -shift);
    }

    /* multiply four bytes in GF(2^8) by 'x' {02} in parallel */

    private static final int m1 = 0x80808080;
    private static final int m2 = 0x7f7f7f7f;
    private static final int m3 = 0x0000001b;

    private int FFmulX(int x)
    {
        return (((x & m2) << 1) ^ (((x & m1) >>> 7) * m3));
    }

    /* 
       The following defines provide alternative definitions of FFmulX that might
       give improved performance if a fast 32-bit multiply is not available.
       
       private int FFmulX(int x) { int u = x & m1; u |= (u >> 1); return ((x & m2) << 1) ^ ((u >>> 3) | (u >>> 6)); } 
       private static final int  m4 = 0x1b1b1b1b;
       private int FFmulX(int x) { int u = x & m1; return ((x & m2) << 1) ^ ((u - (u >>> 7)) & m4); } 

    */

    private int mcol(int x)
    {
        int f2 = FFmulX(x);
        return f2 ^ shift(x ^ f2, 8) ^ shift(x, 16) ^ shift(x, 24);
    }

    private int inv_mcol(int x)
    {
        int f2 = FFmulX(x);
        int f4 = FFmulX(f2);
        int f8 = FFmulX(f4);
        int f9 = x ^ f8;
        
        return f2 ^ f4 ^ f8 ^ shift(f2 ^ f9, 8) ^ shift(f4 ^ f9, 16) ^ shift(f9, 24);
    }


    private int subWord(int x)
    {
        return (S[x&255]&255 | ((S[(x>>8)&255]&255)<<8) | ((S[(x>>16)&255]&255)<<16) | S[(x>>24)&255]<<24);
    }

    /**
     * Calculate the necessary round keys
     * The number of calculations depends on key size and block size
     * AES specified a fixed block size of 128 bits and key sizes 128/192/256 bits
     * This code is written assuming those are the only possible values
     */
    private int[] generateWorkingKey(
                                    byte[] key,
                                    boolean forEncryption)
    {
        int         KC = key.length / 4;  // key length in words
        int         t;
        
        if (((KC != 4) && (KC != 6) && (KC != 8)) || ((KC * 4) != key.length))
        {
            throw new IllegalArgumentException("Key length not 128/192/256 bits.");
        }

        ROUNDS = KC + 6;  // This is not always true for the generalized Rijndael that allows larger block sizes
        final int[]  W = new int[4*(ROUNDS+1)];   // 4 words in a block
        
        //
        // copy the key into the round key array
        //
        
        t = 0;
        int i = 0;
        while (i < key.length)
            {
	      W[t] = (key[i]&0xff) | ((key[i+1]&0xff) << 8) | ((key[i+2]&0xff) << 16) | (key[i+3] << 24);
	      i+=4;
	      t++;
            }
        
        //
        // while not enough round key material calculated
        // calculate new values
        //
        int k = (ROUNDS + 1) << 2;
        for (i = KC; (i < k); i++)
            {
                int temp = W[i-1];
                if ((i % KC) == 0)
                {
                    temp = subWord(shift(temp, 8)) ^ rcon[(i / KC)-1];
                }
                else if ((KC > 6) && ((i % KC) == 4))
                {
                    temp = subWord(temp);
                }
                
                W[i] = W[i-KC] ^ temp;
            }

        if (!forEncryption)
        {
            for (int j = 1; j < ROUNDS; j++)
            {
                for (i = 0; i < 4; i++) 
                {
		  W[(j<<2)+i] = inv_mcol(W[(j<<2)+i]);
                }
            }
        }

        return W;
    }

    private int         ROUNDS;
    private int[]       WorkingKey = null;
    private int         C0, C1, C2, C3;
    private boolean     forEncryption;

    private static final int BLOCK_SIZE = 16;

    /**
     * default constructor - 128 bit block size.
     */
    public AESLightEngine()
    {
    }

    /**
     * initialise an AES cipher.
     *
     * @param forEncryption whether or not we are for encryption.
     * @param params the parameters required to set up the cipher.
     * @exception IllegalArgumentException if the params argument is
     * inappropriate.
     */
    public void init(
        boolean           forEncryption,
        CipherParameters  params)
    {
        if (params instanceof KeyParameter)
        {
            WorkingKey = generateWorkingKey(((KeyParameter)params).getKey(), forEncryption);
            this.forEncryption = forEncryption;
            return;
        }

        throw new IllegalArgumentException("invalid parameter passed to AES init - "); // + params.getClass().getName());
    }

    public String getAlgorithmName()
    {
        return "AES";
    }

    public int getBlockSize()
    {
        return BLOCK_SIZE;
    }

    public int processBlock(
        byte[] in,
        int inOff,
        byte[] out,
        int outOff)
    {
        if (WorkingKey == null)
        {
            throw new IllegalStateException("AES engine not initialised");
        }

        if ((inOff + (32 / 2)) > in.length)
        {
            throw new DataLengthException("input buffer too short");
        }

        if ((outOff + (32 / 2)) > out.length)
        {
            throw new DataLengthException("output buffer too short");
        }

        if (forEncryption)
        {
            unpackBlock(in, inOff);
            encryptBlock(WorkingKey);
            packBlock(out, outOff);
        }
        else
        {
            unpackBlock(in, inOff);
            decryptBlock(WorkingKey);
            packBlock(out, outOff);
        }

        return BLOCK_SIZE;
    }

    public void reset()
    {
    }

    private void unpackBlock(
        byte[]      bytes,
        int         off)
    {
        int     index = off;

        C0 = (bytes[index++] & 0xff);
        C0 |= (bytes[index++] & 0xff) << 8;
        C0 |= (bytes[index++] & 0xff) << 16;
        C0 |= bytes[index++] << 24;

        C1 = (bytes[index++] & 0xff);
        C1 |= (bytes[index++] & 0xff) << 8;
        C1 |= (bytes[index++] & 0xff) << 16;
        C1 |= bytes[index++] << 24;

        C2 = (bytes[index++] & 0xff);
        C2 |= (bytes[index++] & 0xff) << 8;
        C2 |= (bytes[index++] & 0xff) << 16;
        C2 |= bytes[index++] << 24;

        C3 = (bytes[index++] & 0xff);
        C3 |= (bytes[index++] & 0xff) << 8;
        C3 |= (bytes[index++] & 0xff) << 16;
        C3 |= bytes[index++] << 24;
    }

    private void packBlock(
        byte[]      bytes,
        int         off)
    {
        int     index = off;

        bytes[index++] = (byte)C0;
        bytes[index++] = (byte)(C0 >> 8);
        bytes[index++] = (byte)(C0 >> 16);
        bytes[index++] = (byte)(C0 >> 24);

        bytes[index++] = (byte)C1;
        bytes[index++] = (byte)(C1 >> 8);
        bytes[index++] = (byte)(C1 >> 16);
        bytes[index++] = (byte)(C1 >> 24);

        bytes[index++] = (byte)C2;
        bytes[index++] = (byte)(C2 >> 8);
        bytes[index++] = (byte)(C2 >> 16);
        bytes[index++] = (byte)(C2 >> 24);

        bytes[index++] = (byte)C3;
        bytes[index++] = (byte)(C3 >> 8);
        bytes[index++] = (byte)(C3 >> 16);
        bytes[index++] = (byte)(C3 >> 24);
    }

    private void encryptBlock(int[] KW)
    {
        int r, r0, r1, r2, r3;

        C0 ^= KW[0];
        C1 ^= KW[1];
        C2 ^= KW[2];
        C3 ^= KW[3];

        for (r = 1; r < ROUNDS - 1;)
        {
	  r0 = mcol((S[C0&255]&255) ^ ((S[(C1>>8)&255]&255)<<8) ^ ((S[(C2>>16)&255]&255)<<16) ^ (S[(C3>>24)&255]<<24)) ^ KW[(r<<2)];
	  r1 = mcol((S[C1&255]&255) ^ ((S[(C2>>8)&255]&255)<<8) ^ ((S[(C3>>16)&255]&255)<<16) ^ (S[(C0>>24)&255]<<24)) ^ KW[(r<<2)+1];
	  r2 = mcol((S[C2&255]&255) ^ ((S[(C3>>8)&255]&255)<<8) ^ ((S[(C0>>16)&255]&255)<<16) ^ (S[(C1>>24)&255]<<24)) ^ KW[(r<<2)+2];
	  r3 = mcol((S[C3&255]&255) ^ ((S[(C0>>8)&255]&255)<<8) ^ ((S[(C1>>16)&255]&255)<<16) ^ (S[(C2>>24)&255]<<24)) ^ KW[(r++<<2)+3];
	  C0 = mcol((S[r0&255]&255) ^ ((S[(r1>>8)&255]&255)<<8) ^ ((S[(r2>>16)&255]&255)<<16) ^ (S[(r3>>24)&255]<<24)) ^ KW[(r<<2)+0];
	  C1 = mcol((S[r1&255]&255) ^ ((S[(r2>>8)&255]&255)<<8) ^ ((S[(r3>>16)&255]&255)<<16) ^ (S[(r0>>24)&255]<<24)) ^ KW[(r<<2)+1];
	  C2 = mcol((S[r2&255]&255) ^ ((S[(r3>>8)&255]&255)<<8) ^ ((S[(r0>>16)&255]&255)<<16) ^ (S[(r1>>24)&255]<<24)) ^ KW[(r<<2)+2];
	  C3 = mcol((S[r3&255]&255) ^ ((S[(r0>>8)&255]&255)<<8) ^ ((S[(r1>>16)&255]&255)<<16) ^ (S[(r2>>24)&255]<<24)) ^ KW[(r++<<2)+3];
        }

        r0 = mcol((S[C0&255]&255) ^ ((S[(C1>>8)&255]&255)<<8) ^ ((S[(C2>>16)&255]&255)<<16) ^ (S[(C3>>24)&255]<<24)) ^ KW[(r<<2)+0];
        r1 = mcol((S[C1&255]&255) ^ ((S[(C2>>8)&255]&255)<<8) ^ ((S[(C3>>16)&255]&255)<<16) ^ (S[(C0>>24)&255]<<24)) ^ KW[(r<<2)+1];
        r2 = mcol((S[C2&255]&255) ^ ((S[(C3>>8)&255]&255)<<8) ^ ((S[(C0>>16)&255]&255)<<16) ^ (S[(C1>>24)&255]<<24)) ^ KW[(r<<2)+2];
        r3 = mcol((S[C3&255]&255) ^ ((S[(C0>>8)&255]&255)<<8) ^ ((S[(C1>>16)&255]&255)<<16) ^ (S[(C2>>24)&255]<<24)) ^ KW[(r++<<2)+3];

        // the final round is a simple function of S

        C0 = (S[r0&255]&255) ^ ((S[(r1>>8)&255]&255)<<8) ^ ((S[(r2>>16)&255]&255)<<16) ^ (S[(r3>>24)&255]<<24) ^ KW[(r<<2)+0];
        C1 = (S[r1&255]&255) ^ ((S[(r2>>8)&255]&255)<<8) ^ ((S[(r3>>16)&255]&255)<<16) ^ (S[(r0>>24)&255]<<24) ^ KW[(r<<2)+1];
        C2 = (S[r2&255]&255) ^ ((S[(r3>>8)&255]&255)<<8) ^ ((S[(r0>>16)&255]&255)<<16) ^ (S[(r1>>24)&255]<<24) ^ KW[(r<<2)+2];
        C3 = (S[r3&255]&255) ^ ((S[(r0>>8)&255]&255)<<8) ^ ((S[(r1>>16)&255]&255)<<16) ^ (S[(r2>>24)&255]<<24) ^ KW[(r<<2)+3];

    }

    private void decryptBlock(int[] KW)
    {
        int r, r0, r1, r2, r3;

        C0 ^= KW[ ROUNDS<<2   ];
        C1 ^= KW[(ROUNDS<<2)+1];
        C2 ^= KW[(ROUNDS<<2)+2];
        C3 ^= KW[(ROUNDS<<2)+3];

        for (r = ROUNDS-1; r>1;)
        {
            r0 = inv_mcol((Si[C0&255]&255) ^ ((Si[(C3>>8)&255]&255)<<8) ^ ((Si[(C2>>16)&255]&255)<<16) ^ (Si[(C1>>24)&255]<<24)) ^ KW[(r<<2)+0];
            r1 = inv_mcol((Si[C1&255]&255) ^ ((Si[(C0>>8)&255]&255)<<8) ^ ((Si[(C3>>16)&255]&255)<<16) ^ (Si[(C2>>24)&255]<<24)) ^ KW[(r<<2)+1];
            r2 = inv_mcol((Si[C2&255]&255) ^ ((Si[(C1>>8)&255]&255)<<8) ^ ((Si[(C0>>16)&255]&255)<<16) ^ (Si[(C3>>24)&255]<<24)) ^ KW[(r<<2)+2];
            r3 = inv_mcol((Si[C3&255]&255) ^ ((Si[(C2>>8)&255]&255)<<8) ^ ((Si[(C1>>16)&255]&255)<<16) ^ (Si[(C0>>24)&255]<<24)) ^ KW[(r--<<2)+3];
            C0 = inv_mcol((Si[r0&255]&255) ^ ((Si[(r3>>8)&255]&255)<<8) ^ ((Si[(r2>>16)&255]&255)<<16) ^ (Si[(r1>>24)&255]<<24)) ^ KW[(r<<2)+0];
            C1 = inv_mcol((Si[r1&255]&255) ^ ((Si[(r0>>8)&255]&255)<<8) ^ ((Si[(r3>>16)&255]&255)<<16) ^ (Si[(r2>>24)&255]<<24)) ^ KW[(r<<2)+1];
            C2 = inv_mcol((Si[r2&255]&255) ^ ((Si[(r1>>8)&255]&255)<<8) ^ ((Si[(r0>>16)&255]&255)<<16) ^ (Si[(r3>>24)&255]<<24)) ^ KW[(r<<2)+2];
            C3 = inv_mcol((Si[r3&255]&255) ^ ((Si[(r2>>8)&255]&255)<<8) ^ ((Si[(r1>>16)&255]&255)<<16) ^ (Si[(r0>>24)&255]<<24)) ^ KW[(r--<<2)+3];
        }

        r0 = inv_mcol((Si[C0&255]&255) ^ ((Si[(C3>>8)&255]&255)<<8) ^ ((Si[(C2>>16)&255]&255)<<16) ^ (Si[(C1>>24)&255]<<24)) ^ KW[(r<<2)+0];
        r1 = inv_mcol((Si[C1&255]&255) ^ ((Si[(C0>>8)&255]&255)<<8) ^ ((Si[(C3>>16)&255]&255)<<16) ^ (Si[(C2>>24)&255]<<24)) ^ KW[(r<<2)+1];
        r2 = inv_mcol((Si[C2&255]&255) ^ ((Si[(C1>>8)&255]&255)<<8) ^ ((Si[(C0>>16)&255]&255)<<16) ^ (Si[(C3>>24)&255]<<24)) ^ KW[(r<<2)+2];
        r3 = inv_mcol((Si[C3&255]&255) ^ ((Si[(C2>>8)&255]&255)<<8) ^ ((Si[(C1>>16)&255]&255)<<16) ^ (Si[(C0>>24)&255]<<24)) ^ KW[(r<<2)+3];

        // the final round's table is a simple function of Si

        C0 = (Si[r0&255]&255) ^ ((Si[(r3>>8)&255]&255)<<8) ^ ((Si[(r2>>16)&255]&255)<<16) ^ (Si[(r1>>24)&255]<<24) ^ KW[0];
        C1 = (Si[r1&255]&255) ^ ((Si[(r0>>8)&255]&255)<<8) ^ ((Si[(r3>>16)&255]&255)<<16) ^ (Si[(r2>>24)&255]<<24) ^ KW[1];
        C2 = (Si[r2&255]&255) ^ ((Si[(r1>>8)&255]&255)<<8) ^ ((Si[(r0>>16)&255]&255)<<16) ^ (Si[(r3>>24)&255]<<24) ^ KW[2];
        C3 = (Si[r3&255]&255) ^ ((Si[(r2>>8)&255]&255)<<8) ^ ((Si[(r1>>16)&255]&255)<<16) ^ (Si[(r0>>24)&255]<<24) ^ KW[3];
    }
}
