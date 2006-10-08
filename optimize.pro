-injars       java/target/dist/lib/in.zip
-outjars      java/target/dist/lib/classes.zip

-verbose
#-dump

-allowaccessmodification
-dontobfuscate

-keepclasseswithmembers public class * {
    public static void main(java.lang.String[]);
}
-keep class com.jopdesign.sys.* {
	*;
}
