package hirondelle.stocks.preferences;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.logging.*;

import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.StandardEditor;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.preferences.PreferencesEditor;
import hirondelle.stocks.util.Util;

/**
* Present dialog to allow update of user preferences.
*
* <P>Related preferences are grouped together and placed in 
* a single pane of a <tt>JTabbedPane</tt>, which corresponds to an 
* implementation of {@link PreferencesEditor}. Values are pre-populated with 
* current values for preferences.
*
*<P>Most preferences have default values. If so, a  
* <tt>Restore Defaults</tt> button is provided for that set of related 
* preferences.
*
*<P>Preferences are not changed until the <tt>OK</tt> button is pressed. 
* Exception: the logging preferences take effect immediately, without the need 
* for hitting <tt>OK</tt>.
*/
public final class EditUserPreferencesAction extends AbstractAction {

  /**
  * Constructor.
  *  
  * @param aFrame parent window to which this dialog is attached.
  * @param aPrefEditors contains implementations of {@link PreferencesEditor}, 
  * each of which is placed in a pane of a <tt>JTabbedPane</tt>.
  */
  public EditUserPreferencesAction (JFrame aFrame, List<PreferencesEditor> aPrefEditors) {
    super("Preferences...", UiUtil.getEmptyIcon()); 
    Args.checkForNull(aFrame);
    Args.checkForNull(aPrefEditors);
    fFrame = aFrame;
    putValue(SHORT_DESCRIPTION, "Update user preferences");
    putValue(LONG_DESCRIPTION, "Allows user input of preferences.");
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P));    
    fPrefEditors = aPrefEditors;
  }

  /** Display the user preferences dialog.  */
  @Override public void actionPerformed(ActionEvent event) {
    fLogger.info("Showing user preferences dialog.");
    //lazy construction: fEditor is created only once, when this action 
    //is explicitly invoked
    if (fEditor == null) {
      fEditor = new Editor("Edit Preferences", fFrame);
    }
    fEditor.showDialog();
  }
  
  // PRIVATE 
  private JFrame fFrame;
  private java.util.List<PreferencesEditor> fPrefEditors;
  private static final Logger fLogger = Util.getLogger(EditUserPreferencesAction.class);  
  
  /**
  * Specifying this as a field allows for "lazy" creation and use of the GUI, which is 
  * of particular importance for a preferences dialog, since they are usually heavyweight, 
  * and have a large number of components.
  */
  private Editor fEditor;
  
  /**
  * Return GUI for editing all preferences, pre-populated with current 
  * values.
  */
  private JComponent getPrefEditors(){
    JTabbedPane content = new JTabbedPane();
    content.setTabPlacement(JTabbedPane.LEFT);
    int idx = 0;
    for(PreferencesEditor prefEditor: fPrefEditors) {
      JComponent editorGui = prefEditor.getUI();
      editorGui.setBorder(UiUtil.getStandardBorder());
      content.addTab(prefEditor.getTitle() , editorGui);
      content.setMnemonicAt(idx, prefEditor.getMnemonic());
      ++idx;
    }
    return content;
  }
  
  /** Called only when the user hits the OK button.  */
  private void saveSettings(){
    fLogger.fine("User selected OK. Updating table preferences.");
    for(PreferencesEditor prefEditor: fPrefEditors) {
      prefEditor.savePreferences();
    }
  }
  
  /**
  * An example of a nested class which is nested because it is attached only 
  * to the enclosing class, and it cannot act as superclass since multiple 
  * inheritance of implementation is not possible. 
  * 
  * The implementation of this nested class is kept short by calling methods 
  * of the enclosing class.
  */
  private final class Editor extends StandardEditor { 
    Editor(String aTitle, JFrame aParent){
      super(aTitle, aParent, StandardEditor.CloseAction.HIDE);
    }
    @Override public JComponent getEditorUI () {
      JPanel content = new JPanel();
      content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
      content.add(getPrefEditors());
      //content.setMinimumSize(new Dimension(300,300));
      return content;
    }
    @Override public void okAction() {
      saveSettings();
      dispose();
    }
  }  
}
