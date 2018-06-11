Plot Digitizer Source Code README
27-APR-2014

Most of the source code for Plot Digitizer is protected either by the General Public License (GPL) or by the Lesser General Public License (LesserGPL).  Copies of both licenses are provided along with the source code and the header of each source file indicates it's legal status (GPL, LesserGPL, public domain, or not written by me).

Plot Digitizer uses the autotrace program for auto digitizing images.  Autotrace is not provided with this archive, but can be downloaded in either source or binary form from the autotrace web site:
http://sourceforge.net/projects/autotrace/

The Plot Digitizer source code, other than for auto-digitizing, is 100% pure Java.  It uses the apparently no longer supported but open source MRJ Adapter library (http://java.net/projects/mrjadapter/) to provide MacOS X specific behavior.  This library will do something appropriate on non-Mac platforms, but adds specific behavior that Mac users expect (Mac users are very particular).  I have included the latest development version of this library with this archive for your convenience.  When compiling, just make sure that MRJAdapter.jar is on your class path.

Plot Digitizer is set up to use the Apache ant build system (http://ant.apache.org/).  To build Plot Digitizer from the DOS or Unix command line, cd into the PlotDigitizerSrc directory, edit the "build.xml" file for your platform's needs (possibly to remove the MacOS X specific and/or Windows specific builds if you are not interested in those), and execute the "ant" command.  This will compile the program, place a double-clickable JAR file in the "jars" directory, create a MacOS X application bundle in the "dist" directory, and use Launch4J (http://launch4j.sourceforge.net/) to create a Windows executable file in the "dist" directory.

Plot Digitizer looks for the autotrace program on the system's search path (note that on MacOS X it does not look on the user's search path, but ONLY on the system search path).

That's all I can think of right now.  If you have any other problems with the source code, don't be afraid to contact me.

Ad astra,

Joseph A. Huwaldt
jhuwaldt@users.sourceforge.net

