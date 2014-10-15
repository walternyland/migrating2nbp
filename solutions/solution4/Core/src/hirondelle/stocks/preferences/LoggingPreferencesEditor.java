package hirondelle.stocks.preferences;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.ui.UiConsts;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.util.Util;

/**
* Refresh the logging config file used upon startup, through a call to
* {@link java.util.logging.LogManager#readConfiguration()}.
* 
* <P>Additionally, sets a temporary global logging level, 
* which acts as on override of the current settings.
* Setting this level affects all handlers 
* (except {@link OptionPaneExceptionHandler}) attached to the root 
* {@link java.util.logging.Logger}, and all known loggers, corresponding to 
* return value of {@link java.util.logging.LogManager#getLoggerNames}. 
*
* <P>This {@link PreferencesEditor} is unusual in that user actions are performed 
* immediately, without waiting for the user to hit the <tt>OK</tt> button.
*/
public final class LoggingPreferencesEditor implements PreferencesEditor {
  
  @Override public int getMnemonic() {
    return MNEMONIC;
  }
  
  @Override public String getTitle() {
    return TITLE;
  }
  
  @Override public JComponent getUI() {
    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.add(getLogFileUI()) ;
    content.add(getLogLevelUI());
    UiUtil.alignAllX(content, UiUtil.AlignX.LEFT);
    return content;
  }
 
  /**
  * No-operation.
  * Does not apply to this case, since there are no default preferences.
  */
  @Override public void matchGuiToDefaultPreferences() { }
  
  /**
  * No-operation.
  * Does not apply to this case, since there are no stored preferences
  * for logging.
  */
  @Override public void savePreferences() { }
  
  /**
  * Return the <tt>File</tt> which is currently being used by this application
  * to configure the Java Logging API.
  */
  public File getLogConfigFile(){
    File result = null;
    String locationFromProperty = System.getProperty("java.util.logging.config.file");
    if (locationFromProperty == null ) {
      //note that avoidance of file separator character ensures that this 
      //will operate on all platforms
      String rawJreHome= System.getProperty("java.home");
      File jreHome = new File(rawJreHome);
      File lib = new File(jreHome, "lib");
      result = new File(lib, "logging.properties");
    }
    else {
      result = new File(locationFromProperty);
    }
    assert result.exists();
    return result;
  }
  
  // PRIVATE
  
  private static final String TITLE = "Logging";
  private static final int MNEMONIC = KeyEvent.VK_L;
  private static final String EXPLANATION =    
    "Temporarily override the configured log level for all known loggers and handlers."+
    " (Setting the log level here will affect only the currently " + 
    "running program. To change settings more permanently, edit " +
    "the above-named log file.)"
  ;
  private JComboBox<Level> fLogLevel;
  private static final Logger fLogger = Util.getLogger(LoggingPreferencesEditor.class);  

  private static java.util.List<Level> LEVELS;
  static {
    //It is unfortunate that the Level class does not provide an enum of all values
    LEVELS = new ArrayList<>();
    LEVELS.add(Level.OFF);
    LEVELS.add(Level.FINEST);
    LEVELS.add(Level.FINER);
    LEVELS.add(Level.FINE);
    LEVELS.add(Level.CONFIG);
    LEVELS.add(Level.INFO);
    LEVELS.add(Level.WARNING);
    LEVELS.add(Level.SEVERE);
  }
  
  private void refreshLogFile(){
    LogManager logManager = LogManager.getLogManager();
    try {
      logManager.readConfiguration();
      OptionPaneExceptionHandler.attachToRootLogger();
    }
    catch (IOException ex){
      fLogger.log(
        Level.SEVERE, "Cannot re-read logging config file. Please verify file name.", ex
      );
    }
  }
  
  private JComponent getLogFileUI(){
    /* 
    * Implementation Note.
    * The end result is disatisfying. If the name of the logging 
    * config file is long, then JTextArea inserts line-breaks. (If the text is 
    * copied and pasted elsewhere, no line breaks appear.) If a JTextField is 
    * used instead of a JTextArea, then the justification is poor, and the user 
    * is given a visual cue that the file name may be edited, which is not the case.
    */
    JPanel content = new JPanel();
    content.setBorder( BorderFactory.createTitledBorder("Logging Config File") );
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    content.add(getLocation());
    content.add(Box.createVerticalStrut(UiConsts.ONE_SPACE));
    content.add(getRefresh()) ;
    content.add(Box.createVerticalStrut(UiConsts.ONE_SPACE));
    UiUtil.alignAllX(content, UiUtil.AlignX.CENTER);
    return content;
  }
  
  private JComponent getLocation(){
    return UiUtil.getStandardTextArea(getLogConfigFile().toString());
  }
  
  private JComponent getRefresh(){
    JButton refresh  = new JButton("Refresh Now");
    refresh.setMnemonic(KeyEvent.VK_R);
    refresh.setToolTipText("Refreshes the above logging config file");
    refresh.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent e){
        refreshLogFile();
      }
    });
    return refresh;
  }
  
  private JComponent getLogLevelUI(){
    JPanel content = new JPanel();
    content.setBorder(BorderFactory.createTitledBorder("Temporary Log Level"));
    content.setLayout( new BoxLayout(content, BoxLayout.Y_AXIS) );
    content.add( getExplanation() );
    content.add( Box.createVerticalStrut(UiConsts.ONE_SPACE) );
    content.add( getLogLevel() );
    content.add( Box.createVerticalStrut(UiConsts.ONE_SPACE) );
    UiUtil.alignAllX(content, UiUtil.AlignX.CENTER);
    return content;
  }
  
  private JComponent getExplanation(){
    return UiUtil.getStandardTextArea(EXPLANATION);
  }
  
  private JComponent getLogLevel(){
    DefaultComboBoxModel<Level> levelsModel = new DefaultComboBoxModel<Level>(
      LEVELS.toArray(new Level[0])
    );
    fLogLevel = new JComboBox<Level>(levelsModel); 
    fLogLevel.setToolTipText("Select an item to immediately update logging levels");
    fLogLevel.addActionListener( new ActionListener() {
      @Override public void actionPerformed(ActionEvent event) {
        changeGlobalLoggingLevels();
      }
    });
    fLogLevel.setMaximumSize(new Dimension(100,50));
    return fLogLevel;
  }
  
  private void changeGlobalLoggingLevels(){
    Level targetLevel = (Level)fLogLevel.getSelectedItem();
    Logger rootLogger = Logger.getLogger(Consts.EMPTY_STRING);

    for(Handler handler: rootLogger.getHandlers()){
      if ( ! (handler instanceof OptionPaneExceptionHandler) ) {
        //This rather distaseful use of instanceof seems to be unavoidable.
        //The intent is to use the graphical handler only for the most severe items, such 
        //that the user is not continually interrupted.
       handler.setLevel(targetLevel);
     }
    }
    
    rootLogger.setLevel(targetLevel);
    java.util.List<String> loggerNames = Collections.list(
      LogManager.getLogManager().getLoggerNames()
    );
    for(String loggerName : loggerNames){
      Logger logger = LogManager.getLogManager().getLogger(loggerName);
      logger.setLevel(targetLevel);
    }
  }
}
