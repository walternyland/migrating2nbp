package hirondelle.stocks.help;

import hirondelle.stocks.preferences.LoggingPreferencesEditor;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.FileUtil;
import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.ui.UiConsts;
import hirondelle.stocks.util.ui.UiUtil;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
* Display a modal dialog, centered on the main window, which
* contains general information about both this application and 
* the system on which it's running.
*
*<P> The system information includes a running snapshot of the 
* object heap size. A button is provided to coax the JVM to 
* perform garbage collection.
*/
public final class AboutAction extends AbstractAction {

  /**
  * Constructor. 
  * 
  * @param aFrame parent window to which this dialog is attached.
  */
  public AboutAction(JFrame aFrame) {
    super("About " + Consts.APP_NAME);
    Args.checkForNull(aFrame);
    fFrame = aFrame;
    putValue(SHORT_DESCRIPTION, "About this application");
    putValue(LONG_DESCRIPTION, "Displays details regarding the StocksMonitor application.");
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A) );    
  }

  @Override public void actionPerformed(ActionEvent e) {
    fLogger.info("Showing the about box.");    
    showAboutBox();
  }
  
  // PRIVATE
  private JFrame fFrame;
  
  /**
  * Displays the size of the object heap.
  *
  * In a typical GUI application, the breakdown for memory consumption is roughly
  * <ul>
  * <li> classes - 70%
  * <li> objects - 15%
  * <li> other  - 15%
  *</ul>
  *
  * There is no API for displaying total memory use. The memory consumed by objects,
  * however, is available, and is displayed by this label; the display is updated 
  * periodically.
  */
  private JLabel fObjectHeapSize;
  
  /** Periodically updates the display of <tt>fObjectHeapSize</tt>.  */
  private javax.swing.Timer fTimer;
  private ActionListener fHeapSizeUpdater;
  private static final int UPDATE_FREQ = 2 * Consts.MILLISECONDS_PER_SECOND;
  private static final long SLEEP_INTERVAL = 100;
  
  private static final String ABOUT_TEXT_FILE = "About.txt";
  private static final Dimension ABOUT_TEXT_SIZE = new Dimension(100,250);
  
  private static final Logger fLogger = Util.getLogger(AboutAction.class);

  private void showAboutBox(){
    JTabbedPane aboutPane = new JTabbedPane();
    aboutPane.addTab( "About" , getAboutPanel() );
    aboutPane.setMnemonicAt(0, KeyEvent.VK_A);
    aboutPane.addTab( "System Info" , getSystemInfoPanel() );
    aboutPane.setMnemonicAt(1, KeyEvent.VK_S);
    
    startHeapSizeTimer();
    Icon image =  UiUtil.getImageIcon("xray-small.jpg", this.getClass()) ;
    String title = UiUtil.getDialogTitle("About");
    JOptionPane.showMessageDialog(fFrame, aboutPane, title, JOptionPane.OK_OPTION, image);
    stopHeapSizeTimer();
  }
  
  private JComponent getAboutPanel(){
    JPanel aboutPanel = new JPanel();
    aboutPanel.setLayout( new BoxLayout(aboutPanel, BoxLayout.Y_AXIS) );
    
    String appNameAndVersion = Consts.APP_NAME + Consts.SPACE + Consts.APP_VERSION;
    String text = appNameAndVersion + Consts.NEW_LINE + getAboutFileContents();
    JTextArea aboutText = UiUtil.getStandardTextAreaHardNewLines( text );
    JScrollPane scrollPane = new JScrollPane(aboutText);
    scrollPane.setPreferredSize(ABOUT_TEXT_SIZE);
    
    aboutPanel.add(scrollPane);
    return aboutPanel;
  }
  
  private String getAboutFileContents(){
    return FileUtil.asString(ABOUT_TEXT_FILE, this.getClass());
  }
  
  private JComponent getSystemInfoPanel(){
    JPanel infoPanel = getStandardPanel();

    Map<String, String> info = new HashMap<String, String>();
    addSysProperty(info, "Java Version","java.version");
    addSysProperty(info, "Java VM", "java.vm.info");
    addSysProperty(info, "Java Home", "java.home");
    addSysProperty(info, "Java Vendor", "java.vendor");
    addSysProperty(info, "User Current Directory", "user.dir" );
    addSysProperty(info, "User Home Directory", "user.home" );
    
    StringBuilder osInfo = new StringBuilder();
    osInfo.append( getProperty("os.arch") );
    osInfo.append( Consts.SPACE);
    osInfo.append( getProperty("os.name") );
    osInfo.append( Consts.SPACE);
    osInfo.append( getProperty("os.version") );
    info.put("Operating System", osInfo.toString());
    
    LoggingPreferencesEditor loggingPrefs = new LoggingPreferencesEditor();
    info.put("Logging Config File", loggingPrefs.getLogConfigFile().toString() );

    //Note that a HashMap is used to populate the tree (bit faster), but a TreeMap is the 
    //final version presented to the user (bit slower, but has desired iteration order)
    UiUtil.addSimpleDisplayFields( infoPanel, new TreeMap<String, String>(info) );    
    
    fObjectHeapSize = UiUtil.addSimpleDisplayField(
      infoPanel, 
      "Object Heap Size", 
      getHeapSize(), 
      UiUtil.getConstraints(8,0),
      true
    );
    fObjectHeapSize.setToolTipText("Total memory consumption is much larger");
    
    addGarbageCollectionButton(infoPanel);
    return infoPanel;
  }
  
  private void addGarbageCollectionButton(JPanel aInfoPanel){
    JButton collectGarbage = new JButton("Collect Garbage");   
    collectGarbage.setToolTipText("Request garbage collection by JVM");
    collectGarbage.setMnemonic(KeyEvent.VK_C);
    collectGarbage.addActionListener( new ActionListener() {
      @Override public void actionPerformed(ActionEvent event) {
        putOutTheGarbage(); //slight pause while this happens
        updateHeapSizeDisplay();
      }
    });
    GridBagConstraints constraints = UiUtil.getConstraints(9,1);
    constraints.insets = new Insets(UiConsts.ONE_SPACE, 0,0,0);
    aInfoPanel.add(collectGarbage, constraints );
  }
  
  /**
  * Return a <tt>JPanel</tt> which has a <tt>GridBagLayout</tt>, and
  * a border as specified in {@link UiUtil#getStandardBorder}.
  */
  private JPanel getStandardPanel(){
    JPanel result  = new JPanel();
    result.setLayout(new GridBagLayout());
    result.setBorder(UiUtil.getStandardBorder());
    return result;
  }
  
  private void updateHeapSizeDisplay(){
    fLogger.fine("Updating heap size...");
    fObjectHeapSize.setText(getHeapSize());
  }

  /** Return a measure of the current heap size in kilobytes.  */
  private String getHeapSize(){
    long totalMemory = Runtime.getRuntime().totalMemory();
    long freeMemory = Runtime.getRuntime().freeMemory();
    Long memoryUseKB = new Long( (totalMemory - freeMemory)/Consts.ONE_KILOBYTE );
    
    StringBuilder result = new StringBuilder();
    result.append(UiUtil.getLocalizedInteger(memoryUseKB));
    result.append(" KB");
    return result.toString();
  }

  /** Periodically update the display of object heap size.  */
  private void startHeapSizeTimer(){
    //SwingWorker isn't used here, since the action happens more than once,
    //and the task doesn't take very long
    fHeapSizeUpdater = new ActionListener() {
      @Override public void actionPerformed(ActionEvent evt) {
        //this returns quickly; it won't lock up the GUI
        updateHeapSizeDisplay();
      }
    };    
    fTimer = new javax.swing.Timer(UPDATE_FREQ, fHeapSizeUpdater);
    fTimer.start();    
    fLogger.fine("Starting timer...");
  }

  /**
  * Must be called when the About Box is closed - otherwise the timer will continue 
  * to operate.
  */
  private void stopHeapSizeTimer(){
    fLogger.fine("Stopping timer...");
    fTimer.stop(); //stops notifying registered listeners
    fTimer.removeActionListener(fHeapSizeUpdater); //removes the one registered listener
    fHeapSizeUpdater = null;
    fTimer = null;
  }
  
  private static void putOutTheGarbage() {
    collectGarbage();
    collectGarbage();
  }

  private static void collectGarbage() {
    try {
      System.gc();
      Thread.currentThread().sleep(SLEEP_INTERVAL);
      System.runFinalization();
      Thread.currentThread().sleep(SLEEP_INTERVAL);
    }
    catch (InterruptedException ex){
      ex.printStackTrace();
    }
  }
  
  private void addSysProperty(Map<String, String> aMap, String aKey, String aPropertyName){
    aMap.put(aKey, getProperty(aPropertyName));
  }
  
  private String getProperty(String aName){
    return System.getProperty(aName);
  }
}