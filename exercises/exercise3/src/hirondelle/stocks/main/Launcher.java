package hirondelle.stocks.main;

import java.util.logging.*;
import java.awt.EventQueue;
import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.Consts;

/**
* Launch the application using an older version of a splash screen.
*
*<P>Perform tasks in this order :
*<ul>
* <li>log basic system information 
* <li>promptly show a splash screen upon startup
* <li>show the main screen
* <li>remove the splash screen once the main screen is shown
*</ul>
*
* These tasks are performed in a thread-safe manner.
*/
public final class Launcher { 

  /**
  * Launch the application and display the main window.
  *
  * @param aArgs are ignored by this application, and may take any value.
  */
  public static void main (String... aArgs) {
    
    /*
    * Implementation Note:
    *
    * Note that the launch thread of any GUI application is in effect an initial 
    * worker thread - it is not the event dispatch thread, where the bulk of processing
    * takes place. Thus, once the launch thread realizes a window, then the launch 
    * thread should almost always manipulate such a window through 
    * EventQueue.invokeLater. (This is done for closing the splash 
    * screen, for example.)
    */
    
    //verifies that assertions are on:
    //  assert(false) : "Test";
    
    logBasicSystemInfo();
    showSplashScreen();
    showMainWindow();
    EventQueue.invokeLater(new SplashScreenCloser());
    fLogger.info("Launch thread now exiting...");
  }

  // PRIVATE 
  
  private static SplashScreen fSplashScreen;
  private static final Logger fLogger = Util.getLogger(Launcher.class);
  private static final String SPLASH_IMAGE = "StocksMonitor.gif";

  /**
  * Show a simple graphical splash screen, as a quick preliminary to the main screen.
  */
  private static void showSplashScreen(){
    fLogger.info("Showing the splash screen.");
    fSplashScreen = new SplashScreen(SPLASH_IMAGE);
    fSplashScreen.splash();
  }
  
  /**
  * Display the main window of the application to the user.
  */
  private static void showMainWindow(){
    fLogger.info("Showing the main window.");
    StocksMonitorMainWindow mainWindow = new StocksMonitorMainWindow();
  }

  /** 
  * Removes the splash screen. 
  *
  * Invoke this <tt>Runnable</tt> using 
  * <tt>EventQueue.invokeLater</tt>, in order to remove the splash screen
  * in a thread-safe manner.
  */
  private static final class SplashScreenCloser implements Runnable {
    @Override public void run(){
      fLogger.fine("Closing the splash screen.'");
      fSplashScreen.dispose();
    }
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