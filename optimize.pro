-injars       java/target/dist/lib/in.zip
-outjars      java/target/dist/lib/classes.zip

-verbose
#-dump

-allowaccessmodification
-dontobfuscate
# avoid too large methods
-optimizations "!method/inlining/unique"

-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}

-keep class com.jopdesign.sys.* { *; }
-keep class joprt.* { *; }

-keep class java.lang.String { *; }
-keep class java.lang.StringBuffer { *; }
-keep class java.lang.CharSequence { *; }
-keep class java.lang.Throwable { *; }
-keep class java.lang.Runnable { *; }
-keep class java.lang.Object { *; }
-keep class java.io.PrintStream { *; }
-keep class java.io.OutputStream { *; }
