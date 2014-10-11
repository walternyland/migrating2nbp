package hirondelle.stocks.help;

import java.awt.event.*;
import javax.swing.*;
import java.util.logging.*;
import javax.help.*;
import java.net.URL;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.Util;

/**
* Display the help system for the application.
*
* <P>Display one of table of contents, index, or search tab, according 
* to argument passed to the constructor. This implementation uses 
* Sun's <a href=http://java.sun.com/products/javahelp/>JavaHelp</a> tool.
*
* <P>This action activates the Help key (often <tt>F1</tt>) for this application. 
* When the help key is pressed, the help system's table of contents is displayed.
*
* <P>This action is unusual in that it corresponds to more than one menu item 
* (Contents, Index, and Search).
*
* <P>Note: the displayed JavaHelp screen is not centered; it's left as is, 
* since the JavaHelp GUI is often cut off at the bottom anyway, and centering would 
* make this problem worse.
*/
public final class HelpAction extends AbstractAction {

  /**
  * Constructor.
  *  
  * @param aFrame parent window to which the help window is attached
  * @param aText name of the menu item for this help action
  * @param aMnemonicKeyEvent mnemonic for <tt>aText</tt>
  * @param aIcon possibly-null graphic to be displayed alongside the text, or 
  * in a toolbar
  * @param aView determines which help window is to be displayed: Contents, Index, 
  * or Search
  */
  public HelpAction(
    JFrame aFrame, String aText, int aMnemonicKeyEvent, Icon aIcon, View aView
  ) {
    super(aText, aIcon);
    Args.checkForNull(aFrame);
    Args.checkForNull(aText);
    Args.checkForNull(aView);
    fFrame = aFrame;
    fView = aView;
    putValue(SHORT_DESCRIPTION, "StocksMonitor Help");
    putValue(LONG_DESCRIPTION, "Displays JavaHelp for StocksMonitor.");
    putValue(MNEMONIC_KEY, new Integer(aMnemonicKeyEvent) );    
    initHelpSystem(); 
  }

  @Override public void actionPerformed(ActionEvent event) {
    fLogger.info("Showing help system.");
    fHelpBroker.setCurrentView( fView.toString() );
    fDisplayHelp.actionPerformed( event );
  }
  
  /** Enumeration for the style of presentation of the the Help system. */
  public enum View {
    SEARCH("Search"), 
    CONTENTS("TOC"), 
    INDEX("Index");
    @Override public String toString(){
      return fName;
    }
    private View(String aName){
      fName = aName;
    }
    private String fName;
  } 

  // PRIVATE 
  private JFrame fFrame;
  private View fView;
  /** Path used by a classloader to find the JavaHelp files. */
  private static final String PATH_TO_JAVA_HELP =
    "hirondelle/stocks/help/JavaHelp/HelpSet.hs"
  ;
  private ClassLoader DEFAULT_CLASS_LOADER = null;
  private static final Logger fLogger = Util.getLogger(HelpAction.class); 
  
  private HelpBroker fHelpBroker;
  private CSH.DisplayHelpFromSource fDisplayHelp;
  
  /** Initialize the JavaHelp system. */
  private void initHelpSystem(){
    //optimization to avoid repeated init
    if ( fHelpBroker != null && fDisplayHelp != null) return;
    
    //(uses the classloader mechanism)
    ClassLoader loader = this.getClass().getClassLoader();
    URL helpSetURL = HelpSet.findHelpSet(loader, PATH_TO_JAVA_HELP);
    assert helpSetURL != null : "Cannot find help system.";
    try {
      HelpSet helpSet = new HelpSet(DEFAULT_CLASS_LOADER, helpSetURL);
      fHelpBroker = helpSet.createHelpBroker();
      fHelpBroker.enableHelpKey( fFrame.getRootPane(), "overview", helpSet );
      fDisplayHelp = new CSH.DisplayHelpFromSource(fHelpBroker);
    }
    catch (HelpSetException ex) {
      fLogger.severe("Cannot create help system with: " + helpSetURL);
    }
    assert fHelpBroker != null : "HelpBroker is null.";
  }
}
