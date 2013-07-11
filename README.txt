
Required software
-----------------

- Quartus 10 Web (Quartus 11 free Web edition does not support Cyclone FPGAs anymore)
  The old version can be found on the altera ftp server
  Note for linux users: 
    start the installer using 
    > bash 10.1sp1_quartus_free_linux.sh
    instead of running './10.1sp1_quartus_free_linux.sh' (the setup script contains bash-specific syntax).
    If the GUI installer aborts during installation, try to do a full installation (do not unselect components in the installer)

- Java SDK
  Note: Java SDK 1.7 should work, at least when using 'make' (I did not test it with ant yet). If you get errors about unsupported major.minor 
  version 51.0, make sure the the javac options for compiling the target code include '-target 1.5'.

- lpsolve55j
  Note: The repositories contains binaries for 32bit systems. On 64bit systems, you might need to recompile at least lpsolve55j from source.
  On linux, make sure LD_LIBRARY_PATH is set to the directory containing lpsolve55.so and lpsolve55j.so


Installing lpsolve55j
---------------------

If you get 'cannot find lpsolve55j in java.library.path' errors, set LD_LIBRARY_PATH to the repository root (export LD_LIBRARY_PATH=.) on
linux. On Windows, the lpsolve libraries must be in %PATH%.

To recompile liblpsolve55j and liblpsolve55 from source on linux (e.g. on 64bit systems):

- Get the source of lpsolve_5.5 and lpsolve_5.5_java from sourceforge
- in lpsolve_5.5/lpsolve55, run 'bash ccc'
- in lpsolve_5.5_java/lib, edit 'build', set LPSOLVE_DIR and JDK_DIR correctly, change '-L../../../' to '$LPSOLVE_DIR/' in last line, run
  'bash build'
- copy generated lpsolve_5.5/lpsolve55/bin/<platform>/liblpsolve55.so and lpsolve_5.5_java/lib/<platform>/liblpsolve55j.so into some dir <dir>
- export LD_LIBRARY_PATH=<dir>

