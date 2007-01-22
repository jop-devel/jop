(load-option 'format)

(define aops '(("+" . "SoftFloat.float32_add")
	       ("-" . "SoftFloat.float32_sub")
	       ("*" . "SoftFloat.float32_mul")
	       ("/" . "SoftFloat.float32_div")
	       ("%" . "SoftFloat.float32_rem")
	       ))

(define uops '(("Math.round" . "SoftFloat.float32_to_int32")
	       ("(int)" . "SoftFloat.float32_to_int32_round_to_zero")
	       ))

(define cops '(("fcmpl" . "SoftFloat.float32_cmpl")
	       ("fcmpg" . "SoftFloat.float32_cmpg")
	       ))

(define aops* '("+" "-" "*" "/" "%"))
(define cops* '("<" ">" "<=" ">=" "==" "!="))
(define uops* '("-" "(int)" "Math.round"))

(define nums '("Float.NaN"
	       "Float.MAX_VALUE" "Float.MIN_VALUE"
	       "Float.NEGATIVE_INFINITY" "Float.POSITIVE_INFINITY"
	       "0.0f" "1.0f" "-1.0f"
	       "-126.0f" "-24.0f" "-2.0f"
	       "2.0f" "24.0f" "127.0f"
 	       "3.14159f" "2.71828f"

 	       "Float.intBitsToFloat(0x00800000)"
 	       "Float.intBitsToFloat(0x33800000)"
 	       "Float.intBitsToFloat(0x3E800000)"
 	       "Float.intBitsToFloat(0x3F000000)"
 	       "Float.intBitsToFloat(0x3F800000)"
 	       "Float.intBitsToFloat(0x40000000)"
 	       "Float.intBitsToFloat(0x40800000)"
 	       "Float.intBitsToFloat(0x4B800000)"
 	       "Float.intBitsToFloat(0x7F000000)"
 	       "Float.intBitsToFloat(0x80800000)"
 	       "Float.intBitsToFloat(0xB3800000)"
 	       "Float.intBitsToFloat(0xBE800000)"
 	       "Float.intBitsToFloat(0xBF000000)"
 	       "Float.intBitsToFloat(0xBF800000)"
 	       "Float.intBitsToFloat(0xC0000000)"
 	       "Float.intBitsToFloat(0xC0800000)"
 	       "Float.intBitsToFloat(0xCB800000)"
 	       "Float.intBitsToFloat(0xFE800000)"
 	       "Float.intBitsToFloat(0x00000000)"
 	       "Float.intBitsToFloat(0x00000001)"
 	       "Float.intBitsToFloat(0x00000002)"
 	       "Float.intBitsToFloat(0x00000004)"
 	       "Float.intBitsToFloat(0x00000008)"
 	       "Float.intBitsToFloat(0x00000010)"
 	       "Float.intBitsToFloat(0x00000020)"
 	       "Float.intBitsToFloat(0x00000040)"
 	       "Float.intBitsToFloat(0x00000080)"
 	       "Float.intBitsToFloat(0x00000100)"
 	       "Float.intBitsToFloat(0x00000200)"
 	       "Float.intBitsToFloat(0x00000400)"
 	       "Float.intBitsToFloat(0x00000800)"
 	       "Float.intBitsToFloat(0x00001000)"
 	       "Float.intBitsToFloat(0x00002000)"
 	       "Float.intBitsToFloat(0x00004000)"
 	       "Float.intBitsToFloat(0x00008000)"
 	       "Float.intBitsToFloat(0x00010000)"
 	       "Float.intBitsToFloat(0x00020000)"
 	       "Float.intBitsToFloat(0x00040000)"
 	       "Float.intBitsToFloat(0x00080000)"
 	       "Float.intBitsToFloat(0x00100000)"
 	       "Float.intBitsToFloat(0x00200000)"
 	       "Float.intBitsToFloat(0x00400000)"
 	       "Float.intBitsToFloat(0x00800000)"
 	       "Float.intBitsToFloat(0x01000000)"
 	       "Float.intBitsToFloat(0x02000000)"
 	       "Float.intBitsToFloat(0x04000000)"
 	       "Float.intBitsToFloat(0x08000000)"
 	       "Float.intBitsToFloat(0x10000000)"
 	       "Float.intBitsToFloat(0x20000000)"
 	       "Float.intBitsToFloat(0x40000000)"
 	       "Float.intBitsToFloat(0x80000000)"
 	       "Float.intBitsToFloat(0xC0000000)"
 	       "Float.intBitsToFloat(0xE0000000)"
 	       "Float.intBitsToFloat(0xF0000000)"
 	       "Float.intBitsToFloat(0xF8000000)"
 	       "Float.intBitsToFloat(0xFC000000)"
 	       "Float.intBitsToFloat(0xFE000000)"
 	       "Float.intBitsToFloat(0xFF000000)"
 	       "Float.intBitsToFloat(0xFF800000)"
 	       "Float.intBitsToFloat(0xFFC00000)"
 	       "Float.intBitsToFloat(0xFFE00000)"
 	       "Float.intBitsToFloat(0xFFF00000)"
 	       "Float.intBitsToFloat(0xFFF80000)"
 	       "Float.intBitsToFloat(0xFFFC0000)"
 	       "Float.intBitsToFloat(0xFFFE0000)"
 	       "Float.intBitsToFloat(0xFFFF0000)"
 	       "Float.intBitsToFloat(0xFFFF8000)"
 	       "Float.intBitsToFloat(0xFFFFC000)"
 	       "Float.intBitsToFloat(0xFFFFE000)"
 	       "Float.intBitsToFloat(0xFFFFF000)"
 	       "Float.intBitsToFloat(0xFFFFF800)"
 	       "Float.intBitsToFloat(0xFFFFFC00)"
 	       "Float.intBitsToFloat(0xFFFFFE00)"
 	       "Float.intBitsToFloat(0xFFFFFF00)"
 	       "Float.intBitsToFloat(0xFFFFFF80)"
	       "Float.intBitsToFloat(0xFFFFFFC0)"
	       "Float.intBitsToFloat(0xFFFFFFE0)"
	       "Float.intBitsToFloat(0xFFFFFFF0)"
	       "Float.intBitsToFloat(0xFFFFFFF8)"
	       "Float.intBitsToFloat(0xFFFFFFFC)"
	       "Float.intBitsToFloat(0xFFFFFFFE)"
	       "Float.intBitsToFloat(0xFFFFFFFF)"
	       "Float.intBitsToFloat(0xFFFFFFFE)"
	       "Float.intBitsToFloat(0xFFFFFFFD)"
	       "Float.intBitsToFloat(0xFFFFFFFB)"
	       "Float.intBitsToFloat(0xFFFFFFF7)"
	       "Float.intBitsToFloat(0xFFFFFFEF)"
	       "Float.intBitsToFloat(0xFFFFFFDF)"
	       "Float.intBitsToFloat(0xFFFFFFBF)"
	       "Float.intBitsToFloat(0xFFFFFF7F)"
	       "Float.intBitsToFloat(0xFFFFFEFF)"
	       "Float.intBitsToFloat(0xFFFFFDFF)"
	       "Float.intBitsToFloat(0xFFFFFBFF)"
	       "Float.intBitsToFloat(0xFFFFF7FF)"
	       "Float.intBitsToFloat(0xFFFFEFFF)"
	       "Float.intBitsToFloat(0xFFFFDFFF)"
	       "Float.intBitsToFloat(0xFFFFBFFF)"
	       "Float.intBitsToFloat(0xFFFF7FFF)"
	       "Float.intBitsToFloat(0xFFFEFFFF)"
	       "Float.intBitsToFloat(0xFFFDFFFF)"
	       "Float.intBitsToFloat(0xFFFBFFFF)"
	       "Float.intBitsToFloat(0xFFF7FFFF)"
	       "Float.intBitsToFloat(0xFFEFFFFF)"
	       "Float.intBitsToFloat(0xFFDFFFFF)"
	       "Float.intBitsToFloat(0xFFBFFFFF)"
	       "Float.intBitsToFloat(0xFF7FFFFF)"
	       "Float.intBitsToFloat(0xFEFFFFFF)"
	       "Float.intBitsToFloat(0xFDFFFFFF)"
	       "Float.intBitsToFloat(0xFBFFFFFF)"
	       "Float.intBitsToFloat(0xF7FFFFFF)"
	       "Float.intBitsToFloat(0xEFFFFFFF)"
	       "Float.intBitsToFloat(0xDFFFFFFF)"
	       "Float.intBitsToFloat(0xBFFFFFFF)"
	       "Float.intBitsToFloat(0x7FFFFFFF)"
	       "Float.intBitsToFloat(0x3FFFFFFF)"
	       "Float.intBitsToFloat(0x1FFFFFFF)"
	       "Float.intBitsToFloat(0x0FFFFFFF)"
	       "Float.intBitsToFloat(0x07FFFFFF)"
	       "Float.intBitsToFloat(0x03FFFFFF)"
	       "Float.intBitsToFloat(0x01FFFFFF)"
	       "Float.intBitsToFloat(0x00FFFFFF)"
	       "Float.intBitsToFloat(0x007FFFFF)"
	       "Float.intBitsToFloat(0x003FFFFF)"
	       "Float.intBitsToFloat(0x001FFFFF)"
	       "Float.intBitsToFloat(0x000FFFFF)"
	       "Float.intBitsToFloat(0x0007FFFF)"
	       "Float.intBitsToFloat(0x0003FFFF)"
	       "Float.intBitsToFloat(0x0001FFFF)"
	       "Float.intBitsToFloat(0x0000FFFF)"
	       "Float.intBitsToFloat(0x00007FFF)"
	       "Float.intBitsToFloat(0x00003FFF)"
	       "Float.intBitsToFloat(0x00001FFF)"
	       "Float.intBitsToFloat(0x00000FFF)"
	       "Float.intBitsToFloat(0x000007FF)"
	       "Float.intBitsToFloat(0x000003FF)"
	       "Float.intBitsToFloat(0x000001FF)"
	       "Float.intBitsToFloat(0x000000FF)"
	       "Float.intBitsToFloat(0x0000007F)"
	       "Float.intBitsToFloat(0x0000003F)"
	       "Float.intBitsToFloat(0x0000001F)"
	       "Float.intBitsToFloat(0x0000000F)"
	       "Float.intBitsToFloat(0x00000007)"
	       "Float.intBitsToFloat(0x00000003)"
	       "Float.intBitsToFloat(0x80000000)"
	       "Float.intBitsToFloat(0x80000001)"
	       "Float.intBitsToFloat(0x80000002)"
	       "Float.intBitsToFloat(0x80000004)"
	       "Float.intBitsToFloat(0x80000008)"
	       "Float.intBitsToFloat(0x80000010)"
	       "Float.intBitsToFloat(0x80000020)"
	       "Float.intBitsToFloat(0x80000040)"
	       "Float.intBitsToFloat(0x80000080)"
	       "Float.intBitsToFloat(0x80000100)"
	       "Float.intBitsToFloat(0x80000200)"
	       "Float.intBitsToFloat(0x80000400)"
	       "Float.intBitsToFloat(0x80000800)"
	       "Float.intBitsToFloat(0x80001000)"
	       "Float.intBitsToFloat(0x80002000)"
	       "Float.intBitsToFloat(0x80004000)"
	       "Float.intBitsToFloat(0x80008000)"
	       "Float.intBitsToFloat(0x80010000)"
	       "Float.intBitsToFloat(0x80020000)"
	       "Float.intBitsToFloat(0x80040000)"
	       "Float.intBitsToFloat(0x80080000)"
	       "Float.intBitsToFloat(0x80100000)"
	       "Float.intBitsToFloat(0x80200000)"
	       "Float.intBitsToFloat(0x80400000)"
	       "Float.intBitsToFloat(0x80800000)"
	       "Float.intBitsToFloat(0x81000000)"
	       "Float.intBitsToFloat(0x82000000)"
	       "Float.intBitsToFloat(0x84000000)"
	       "Float.intBitsToFloat(0x88000000)"
	       "Float.intBitsToFloat(0x90000000)"
	       "Float.intBitsToFloat(0xA0000000)"
	       "Float.intBitsToFloat(0xC0000000)"
	       "Float.intBitsToFloat(0x80000000)"
	       "Float.intBitsToFloat(0xBFFFFFFF)"
	       "Float.intBitsToFloat(0x9FFFFFFF)"
	       "Float.intBitsToFloat(0x8FFFFFFF)"
	       "Float.intBitsToFloat(0x87FFFFFF)"
	       "Float.intBitsToFloat(0x83FFFFFF)"
	       "Float.intBitsToFloat(0x81FFFFFF)"
	       "Float.intBitsToFloat(0x80FFFFFF)"
	       "Float.intBitsToFloat(0x807FFFFF)"
	       "Float.intBitsToFloat(0x803FFFFF)"
	       "Float.intBitsToFloat(0x801FFFFF)"
	       "Float.intBitsToFloat(0x800FFFFF)"
	       "Float.intBitsToFloat(0x8007FFFF)"
	       "Float.intBitsToFloat(0x8003FFFF)"
	       "Float.intBitsToFloat(0x8001FFFF)"
	       "Float.intBitsToFloat(0x8000FFFF)"
	       "Float.intBitsToFloat(0x80007FFF)"
	       "Float.intBitsToFloat(0x80003FFF)"
	       "Float.intBitsToFloat(0x80001FFF)"
	       "Float.intBitsToFloat(0x80000FFF)"
	       "Float.intBitsToFloat(0x800007FF)"
	       "Float.intBitsToFloat(0x800003FF)"
	       "Float.intBitsToFloat(0x800001FF)"
	       "Float.intBitsToFloat(0x800000FF)"
	       "Float.intBitsToFloat(0x8000007F)"
	       "Float.intBitsToFloat(0x8000003F)"
	       "Float.intBitsToFloat(0x8000001F)"
	       "Float.intBitsToFloat(0x8000000F)"
	       "Float.intBitsToFloat(0x80000007)"
	       "Float.intBitsToFloat(0x80000003)"

	       "Float.intBitsToFloat(0xabfb2c81)"
	       "Float.intBitsToFloat(0xeb21fece)"
	       "Float.intBitsToFloat(0x361b388b)"
	       "Float.intBitsToFloat(0x7621d144)"
	       "Float.intBitsToFloat(0x838b1c87)"
	       "Float.intBitsToFloat(0x4476555a)"
	       "Float.intBitsToFloat(0xac7cc276)"
	       "Float.intBitsToFloat(0xec4111fe)"
	       "Float.intBitsToFloat(0x83bd1c1b)"
	       "Float.intBitsToFloat(0x43bd69ec)"
	       "Float.intBitsToFloat(0xb08711b2)"
	       "Float.intBitsToFloat(0x70e7fc01)"
	       "Float.intBitsToFloat(0xb6c4c126)"
	       "Float.intBitsToFloat(0xf84a5aed)"
	       "Float.intBitsToFloat(0x9e1b55f9)"
	       "Float.intBitsToFloat(0x5e99b660)"

	       "Float.intBitsToFloat(0xbfc02e00)"
	       "Float.intBitsToFloat(0xc2020300)"
	       "Float.intBitsToFloat(0xc22a0300)"
	       "Float.intBitsToFloat(0xc2020300)"
	       "Float.intBitsToFloat(0xc1780200)"
	       "Float.intBitsToFloat(0xc1e40400)"
	       "Float.intBitsToFloat(0xbfc02c00)"
	       "Float.intBitsToFloat(0xc1180d00)"

	       "Float.intBitsToFloat(0xc2df0100)"
	       "Float.intBitsToFloat(0xc19c0000)"
	       "Float.intBitsToFloat(0xc2950100)"
	       "Float.intBitsToFloat(0xc2a30100)"
	       "Float.intBitsToFloat(0xc2c30100)"
	       "Float.intBitsToFloat(0x3effffff)"

))

(define (format_aop file o a b)
  (format file "    {\n")
  (format file "      int pc_val = Float.floatToRawIntBits(~A ~A ~A);\n" a (car o) b)
  (format file "      int jop_val = ~A (Float.floatToRawIntBits(~A), ~
                                        Float.floatToRawIntBits(~A));\n" (cdr o) a b)
  (format file "      if ((Float.isNaN(Float.intBitsToFloat(pc_val)) != Float.isNaN(Float.intBitsToFloat(jop_val))) ~
                          || (!Float.isNaN(Float.intBitsToFloat(pc_val)) && (pc_val != jop_val))) {\n")
  (format file "        System.err.println(\"(\"+Integer.toHexString(Float.floatToRawIntBits(~A))+\"~A\" ~
                                                +Integer.toHexString(Float.floatToRawIntBits(~A))+\") != ~
                                               ~A (\"+Integer.toHexString(Float.floatToRawIntBits(~A))+\",\" ~
                                                     +Integer.toHexString(Float.floatToRawIntBits(~A))+\")\");\n"
	  a (car o) b (cdr o) a b)
  (format file "        System.err.println(Integer.toHexString(pc_val)+\" != \"~
                                          +Integer.toHexString(jop_val));\n")
  (format file "      }\n")
  (format file "    }\n"))
  
(define (format_cop file o a b)	       
  (format file "    {\n")
  (format file "       int pc_val = ~A (~A, ~A);\n" (car o) a b)
  (format file "       int jop_val = ~A (Float.floatToRawIntBits(~A), ~
                                         Float.floatToRawIntBits(~A));\n" (cdr o) a b)
  (format file "       if (pc_val != jop_val) {\n")
  (format file "         System.err.println(\"~A (\"+Integer.toHexString(Float.floatToRawIntBits(~A))+\",\" ~
                                                    +Integer.toHexString(Float.floatToRawIntBits(~A))+\") != ~
                                              ~A (\"+Integer.toHexString(Float.floatToRawIntBits(~A))+\",\" ~
                                                    +Integer.toHexString(Float.floatToRawIntBits(~A))+\")\");\n"
	  (car o) a b (cdr o) a b)
  (format file "         System.err.println(Integer.toHexString(pc_val)+\" != \"~
                                           +Integer.toHexString(jop_val));\n")
  (format file "       }\n")
  (format file "    }\n"))

(define (format_uop file o a)
  (format file "    {\n")
  (format file "       int pc_val = ~A (~A);\n" (car o) a)
  (format file "       int jop_val = ~A (Float.floatToRawIntBits(~A));\n" (cdr o) a)
  (format file "       if (pc_val != jop_val) {\n")
  (format file "         System.err.println(\"~A (\"+Integer.toHexString(Float.floatToRawIntBits(~A))+\") != ~
                                              ~A (\"+Integer.toHexString(Float.floatToRawIntBits(~A))+\")\");\n"
	  (car o) a (cdr o) a)
  (format file "         System.err.println(Integer.toHexString(pc_val)+\" != \"~
                                           +Integer.toHexString(jop_val));\n")
  (format file "       }\n")
  (format file "    }\n"))

(define (format_comparisons file)
  (format file "  private static int fcmpl(float a, float b) {\n")
  (format file "  if (Float.isNaN(a)) return -1;\n")
  (format file "  if (Float.isNaN(b)) return -1;\n")
  (format file "  if (a == b) return 0;\n")
  (format file "  if (a < b) return -1;\n")
  (format file "  return 1;\n")
  (format file "  }\n\n")

  (format file "  private static int fcmpg(float a, float b) {\n")
  (format file "  if (Float.isNaN(a)) return 1;\n")
  (format file "  if (Float.isNaN(b)) return 1;\n")
  (format file "  if (a == b) return 0;\n")
  (format file "  if (a < b) return -1;\n")
  (format file "  return 1;\n")
  (format file "  }\n\n"))
  

(call-with-output-file "GenFloatTestDef.java"
  (lambda (file)  
    (format file "package test;\n")
    (format file "import java.io.*;\n")
    (format file "import java.util.*;\n")

    (format file "import com.jopdesign.sys.SoftFloat;\n\n")

    (format file "public class GenFloatTestDef {\n\n")

    (format_comparisons file)

    (format file "  public static void main (String[] args) {\n\n")

    (format file "  float [] nums = { ~A" (car nums))
    (for-each (lambda (n) (format file ",\n\t~A" n)) (cdr nums))
    (format file "  };\n\n")

    (format file "  System.err.println(\"Testing predefined values...\");\n")

    (for-each
     (lambda (o)
       (format file "  for (int i=0; i<nums.length; i++) {\n")
       (format file "    for (int k=0; k<nums.length; k++) {\n")
       (format_aop file o "nums[i]" "nums[k]")
       (format file "    }\n")
       (format file "  }\n")
       (format file "  System.err.println(\"Testing ~A finished.\");\n" (cdr o)))
     aops)

    (for-each
     (lambda (o)
       (format file "  for (int i=0; i<nums.length; i++) {\n")
       (format file "    for (int k=0; k<nums.length; k++) {\n")
       (format_cop file o "nums[i]" "nums[k]")
       (format file "    }\n")
       (format file "  }\n")
       (format file "  System.err.println(\"Testing ~A finished.\");\n" (cdr o)))
     cops)

    (for-each
     (lambda (o)
       (format file "  for (int i=0; i<nums.length; i++) {\n")
       (format_uop file o "nums[i]")
       (format file "  }\n")
       (format file "  System.err.println(\"Testing ~A finished.\");\n" (cdr o)))
     uops)

    (format file "    System.err.println(\"All tests finished.\");\n")

    (format file "  }\n}\n")))


(call-with-output-file "GenFloatTestRnd.java"
  (lambda (file)  
    (format file "package test;\n")
    (format file "import java.io.*;\n")
    (format file "import java.util.*;\n")

    (format file "import com.jopdesign.sys.SoftFloat;\n\n")

    (format file "public class GenFloatTestRnd {\n\n")

    (format_comparisons file)

    (format file "  static long l = 0;\n\n")

    (format file "  public static void main (String[] args) {\n\n")

    (format file "  System.err.println(\"Testing random values...\");\n")

    (format file "  final long tim = System.currentTimeMillis();\n")		
    (format file "  new Thread() {\n")
    (format file "    public void run() {\n")
    (format file "      for (;;) {\n")
    (format file "        System.out.print((l/1000000)+\" M \");\n")
    (format file "        long diff = System.currentTimeMillis()-tim;\n")
    (format file "        diff /= 1000;\n")
    (format file "        if (diff!=0) {\n")
    (format file "          System.out.print((l/diff)+\"/s\");\n")
    (format file "        }\n")
    (format file "        try { Thread.sleep(1000); } catch(Exception e) {}\n")
    (format file "        System.out.print(\"\\r\");\n")
    (format file "      }\n")
    (format file "    }\n")
    (format file "  }.start();\n")

    (format file "  Random rnd = new Random();\n")
    (format file "  float a = 0;\n")
    (format file "  float b = 0;\n")

    (format file "  for (l = 0; true; ++l) {\n")

    (format file "    a = Float.intBitsToFloat(rnd.nextInt());\n")
    (format file "    b = Float.intBitsToFloat(rnd.nextInt());\n")

    (for-each
     (lambda (o)
       (format_aop file o "a" "b"))
     aops)

    (for-each
     (lambda (o)
       (format_cop file o "a" "b"))
     cops)
    
    (for-each
     (lambda (o)
       (format_uop file o "a"))
     uops)

    (format file "    }\n")

    (format file "  }\n}\n")))

(call-with-output-file "../../../target/src/test/fpu/GenFloatTestSim.java"
  (lambda (file)  
    (format file "package fpu;\n")
    (format file "public class GenFloatTestSim {\n\n")

    (format file "  static float [] nums = { ~A" (car nums))
    (for-each (lambda (n) (format file ",\n\t~A" n)) (cdr nums))
    (format file "  };\n\n")

    (format file "  public static void main (String[] args) {\n\n")
    
    (format file "  System.out.println(\"Testing floating-point operations...\");\n")

    (for-each
     (lambda (o)
       (format file "  for (int i=0; i<nums.length; i++) {\n")
       (format file "    for (int k=0; k<nums.length; k++) {\n")
       (format file "      System.out.println(Integer.toHexString(Float.floatToIntBits(nums[i] ~A nums[k])));\n" o)
       (format file "    }\n")
       (format file "  }\n")
       (format file "  System.out.println(\"Testing ~A finished.\");\n" o))
     aops*)

    (for-each
     (lambda (o)
       (format file "  for (int i=0; i<nums.length; i++) {\n")
       (format file "    for (int k=0; k<nums.length; k++) {\n")
       (format file "      if (nums[i] ~A nums[k])\n" o)
       (format file "        System.err.println(\"true\");\n")
       (format file "      else\n")
       (format file "        System.err.println(\"false\");\n")
       (format file "    }\n")
       (format file "  }\n")
       (format file "  System.out.println(\"Testing ~A finished.\");\n" o))
     cops*)

    (for-each
     (lambda (o)
       (format file "  for (int i=0; i<nums.length; i++) {\n")
       (format file "    System.out.println(Integer.toHexString(Float.floatToIntBits(~A(nums[i]))));\n" o)
       (format file "  }\n")
       (format file "  System.out.println(\"Testing ~A finished.\");\n" o))
     uops*)

    (format file "    System.err.println(\"All tests finished.\");\n")

    (format file "  }\n}\n")))