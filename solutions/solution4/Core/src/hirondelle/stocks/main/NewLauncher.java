package hirondelle.stocks.main;


import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.Util;

import java.util.logging.Logger;

/**
* Launch the application using a modern version of a splash screen.
*
*<P>Perform tasks in this order :
*<ul>
* <li>log basic system information 
* <li>show the main screen
* <li>remove the splash screen once the main screen is shown
*</ul>
*
* These tasks are performed in a thread-safe manner.
* The splash screen image is configured in the jar manifest. When launching without the jar 
* (for example in an IDE), you can specify the splash screen 
* image by passing the property '-splash:FILE_NAME' to the java command line, 
* where FILE_NAME is the absolute location of the StocksMonitor.gif file.   
*/
public final class NewLauncher { 

  /**
  * Launch the application and display the main window.
  *
  * @param aArgs are ignored by this application, and may take any value.
  */
  public static void main (String... aArgs) {
    
    //verifies that assertions are on:
    //  assert(false) : "Test";
    
    logBasicSystemInfo();
    showMainWindow();
    fLogger.info("Launch thread now exiting...");  
  }

  // PRIVATE 
  
  private static final Logger fLogger = Util.getLogger(NewLauncher.class);

  /**
  * Display the main window of the application to the user.
  */
  private static void showMainWindow(){
    fLogger.info("Showing the main window.");
    StocksMonitorMainWindow mainWindow = new StocksMonitorMainWindow();
  }

  private static void logBasicSystemInfo() {
    fLogger.info("Launching the application...");
    fLogger.config(
      "Operating System: " + System.getProperty("os.name") + " " + 
      System.getProperty("os.version")
    );
    fLogger.config("JRE: " + System.getProperty("java.version"));
    fLogger.info("Java Launched From: " + System.getProperty("java.home"));
    fLogger.config("Class Path: " + System.getProperty("java.class.path"));
    fLogger.config("Library Path: " + System.getProperty("java.library.path"));
    fLogger.config("Application Name: " + Consts.APP_NAME + "/" + Consts.APP_VERSION);
    fLogger.config("User Home Directory: " + System.getProperty("user.home"));
    fLogger.config("User Working Directory: " + System.getProperty("user.dir"));
    fLogger.info("Test INFO logging.");
    fLogger.fine("Test FINE logging.");
    fLogger.finest("Test FINEST logging.");
  }
}