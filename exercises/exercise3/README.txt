StocksMonitor 
Version : 1.7.0
Published : September 20, 2013

StocksMonitor is an example Swing application, implemented in Java.
It is created by Hirondelle Systems and is available through javapractices.com.

For general info, see :
http://www.javapractices.com/topic/TopicAction.do?Id=170

Javadoc :
http://www.javapractices.com/apps/stocksmonitor/javadoc/index.html

Java Web Start launch point :
http://www.javapractices.com/apps/stocksmonitor/StocksMonitor.jnlp
(This launches the original version of the app. It doesn't launch your edited 
version.)


RUNNING THE APPLICATION FROM THE COMMAND LINE
---------------------------------------------------
A convenience jar is supplied, so that you can run the app locally, without 
needing to do a complete build, if desired. You may run the app using :

[STOCKSMONITOR_HOME]/jnlp>java -jar StocksMonitor.jar

This convenience jar is located in the jnlp directory.
The jnlp directory as a whole contains all items needed to launch the app using 
Java Web Start *from a location under javapractices.com*. It's merely
provided as an example of the files needed to launch an app with Java Web Start.
*When running the above command line*, Java Web Start is not invoked in any way.


REQUIREMENTS
---------------------------------------------------
JDK 1.7.0 (or better) is required to compile StocksMonitor.
All other required libraries are already supplied with StocksMonitor. You 
do not need to download them. As background information, StocksMonitor 
uses these jars :
 - lib\jh.jar : Java Help 1.1.3 help system for graphical apps - http://java.sun.com/products/javahelp/
 - lib\jlfgr-1_0.jar : Java Look and Feel Graphics Repository 1.0 -  http://java.sun.com/developer/techDocs/hi/repository/
 - lib\junit.jar : unit testing tool - http://www.junit.org/


OPTIONAL BUT HIGHLY RECOMMENDED
---------------------------------------------------
The 'Ant' build tool may be used for various tasks such as compiling, 
building, and generating javadoc. Ant is both free and open source. 
It is widely used by Java developers. For more information, see :
http://ant.apache.org/

Ant uses a build.xml file for defining various build tasks. An example build.xml 
is supplied with StocksMonitor, and is located in the project's base directory. 
It should be treated as a starting point for your own version of build.xml. 
You will need to review its content, and edit its settings, before running any 
ANT targets.
 

LOGGING
---------------------------------------------------
StocksMonitor uses the logging services of the JDK.

Logging for your machine is usually configured using this file :
[JDK_HOME]/jre/lib/logging.properties

For information on using and configuring the JDK logger, see :
http://www.javapractices.com/topic/TopicAction.do?Id=143

Example Logging output for a successful launch of the application :

Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
INFO: Launching the application...
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
CONFIG: Operating System: Windows XP 5.1
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
CONFIG: JRE: 1.5.0_07
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
INFO: Java Launched From: C:\Program Files\Java\jre1.5.0_07
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
CONFIG: Class Path: C:\myname\Projects\stocks\bin;C:\myname\Projects\Libraries\junit3.8.1\junit.jar;C:\myname\Projects\stocks\lib\jh.jar;C:\myname\Projects\stocks\lib\jlfgr-1_0.jar
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
CONFIG: Library Path: C:\Program Files\Java\jre1.5.0_07\bin;.;C:\WINDOWS\system32;C:\WINDOWS;C:\jdk1.5.0\bin;C:\WINDOWS\system32;C:\WINDOWS;C:\WINDOWS\system32\WBEM;C:\ant\bin;
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
CONFIG: Application Name: StocksMonitor/1.5.0
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
CONFIG: User Home Directory: C:\Documents and Settings\myname
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
CONFIG: User Working Directory: C:\myname\Projects\stocks
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
INFO: Test INFO logging.
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher logBasicSystemInfo
FINE: Test FINE logging.
Jul 11, 2008 9:13:48 AM hirondelle.stocks.main.Launcher showSplashScreen
INFO: Showing the splash screen.
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.Launcher showMainWindow
INFO: Showing the main window.
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.StocksMonitorMainWindow initCurrentPortfolio
INFO: Initializing the current portfolio
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.StocksMonitorMainWindow initGuiPieces
INFO: Initializing main pieces of the GUI.
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.StocksMonitorMainWindow initActions
INFO: Initializing Actions.
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.StocksMonitorMainWindow initMainGui
INFO: Initializing larges pieces of the GUI.
Jul 11, 2008 9:13:49 AM hirondelle.stocks.quotes.FetchQuotesAction update
FINE: Notified ...
Jul 11, 2008 9:13:49 AM hirondelle.stocks.quotes.FetchQuotesAction update
FINE: By Current Portfolio...
Jul 11, 2008 9:13:49 AM hirondelle.stocks.quotes.FetchQuotesAction actionPerformed
INFO: Fetching quotes from web.
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.StocksMonitorMainWindow update
FINE: Notify being broadcast...
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.StocksMonitorMainWindow update
FINE: Notified by Current Portfolio...
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.StocksMonitorMainWindow showMainWindow
INFO: Showing the main window.
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.Launcher main
INFO: Launch thread now exiting...
Jul 11, 2008 9:13:49 AM hirondelle.stocks.main.Launcher$SplashScreenCloser run
FINE: Closing the splash screen.'
Jul 11, 2008 9:13:50 AM hirondelle.stocks.quotes.ColorTip$Worker run
FINE: Initial Sleeping...
Jul 11, 2008 9:13:50 AM hirondelle.stocks.quotes.ColorTip$Worker run
FINE: Activation Sleeping...
Jul 11, 2008 9:13:52 AM hirondelle.stocks.quotes.ColorTip$Worker run
FINE: Color worker done.



BUGS 
---------------------------------------------------
See the application's Help->About dialog for a list of known bugs.
