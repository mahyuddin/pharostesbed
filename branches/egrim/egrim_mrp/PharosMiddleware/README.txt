This is the Pharos Middleware.  The directory structure is as follows:

  - src/   Contains the source code.
  - jars/  Contains various libraries needed by the Pharos Middleware, 
           and PharosMiddleware.jar itself.
  - doc/   Contains the java docs.
  - bin/   Contains the compiled binaries of the Pharos Middleware.

==============================================================================
The program 'ant' is used to compile and jar the Pharos Middleware. See ant's
website for more details of this build system:

http://ant.apache.org/

To compile the PharosMiddeware and create a jar file, execute the command 
below.  The jar file should be in jar/PharosMiddleware.jar.

  $ ant

To clean up everything (i.e., remove compiled files):
  $ ant clean

To compile the code without creating a jar file:
  $ ant compile

To perform a clean build, execute:
  $ ant clean-build

To generate the javadocs in the doc/ directory:
  $ ant javadoc
