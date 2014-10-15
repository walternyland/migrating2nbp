package hirondelle.stocks.util.ui;

import java.util.*;
import javax.swing.*;
import java.awt.event.*;

import hirondelle.stocks.util.Args;

/**
* Abstract Base Class for a dialog with standard layout, buttons, and behavior.
* (The name <tt>StandardEditor</tt> was chosen since almost all non-trivial 
* dialogs allow the user to edit data in some way.)
*
* <P>Use of this class will apply a standard appearance to 
* dialogs in the application.
*
* <P> Subclasses implement the body of the dialog (wherein business objects 
* are manipulated), and the action taken by the <tt>OK</tt> button. 
*
* <P>Services of a <tt>StandardEditor</tt> include: 
*<ul>
* <li>centering on the parent frame
* <li>reusing the parent's icon
* <li>standard layout and border spacing, based on Java Look and Feel guidelines.
* <li>uniform naming style for dialog title, with the application name appearing first
* <li><tt>OK</tt> and <tt>Cancel</tt> buttons at the bottom of the dialog - 
* <tt>OK</tt> is the default, and the <tt>Escape</tt> key activates 
* <tt>Cancel</tt> (the latter works only if the dialog receives the escape 
* keystroke, and not one of its components)
* <li>disabling of resizing
*</ul>
*
* <P>The <tt>Escape</tt> key does not always work (for example, when a 
* <tt>JTable</tt> row has the focus)
*/
public abstract class StandardEditor {
  
  /**
  * Constructor.
  * @param aTitle text which appears in the title bar after the name of 
  * the application; must have content.
  * @param aParent window to which this dialog is attached.
  * @param aCloseAction sets the behaviour of the dialog upon close.
  */
  protected StandardEditor (String aTitle, JFrame aParent, CloseAction aCloseAction) {
    Args.checkForContent(aTitle);
    Args.checkForNull(aParent);
    fTitle = aTitle;
    fParent = aParent;
    fCloseAction = aCloseAction.getValue();
  }
  
  /**
  * Forces calls to constructor to have greater clarity, by using an
  * enumeration instead of integers.
  */
  protected enum CloseAction {
    DISPOSE(JDialog.DISPOSE_ON_CLOSE),
    HIDE(JDialog.HIDE_ON_CLOSE);
    int getValue(){
      return fAction;
    }
    private final int fAction;
    private CloseAction(int aAction){
      fAction = aAction;
    }
  }
  
  /**
  * Display this <tt>StandardEditor</tt> to the user.
  *
  * <P>Follows the Java Look and Feel guidelines for spacing elements.
  */
  public final void showDialog(){
    boolean isModal = true;
    fDialog = new JDialog(fParent, UiUtil.getDialogTitle(fTitle), isModal);
    fDialog.setDefaultCloseOperation(fCloseAction);
    fDialog.setResizable(false);
    addCancelByEscapeKey();
    
    JPanel standardLayout = new JPanel();
    standardLayout.setLayout(new BoxLayout(standardLayout, BoxLayout.Y_AXIS));
    standardLayout.setBorder(UiUtil.getStandardBorder());
    standardLayout.add(getEditorUI());
    standardLayout.add(getCommandRow());
    
    fDialog.getContentPane().add(standardLayout);
    
    UiUtil.centerOnParentAndShow(fDialog);
  }

  /** Close the editor dialog.  */
  public final void dispose(){
    fDialog.dispose();
  }
  
  /**
  * Return the GUI which allows the user to manipulate the business 
  * objects related to this dialog; this GUI will be placed above the 
  * <tt>OK</tt> and <tt>Cancel</tt> buttons, in a standard manner.
  */
  protected abstract JComponent getEditorUI();
  
  /**
  * The action taken when the user hits the <tt>OK</tt> button.
  */
  protected abstract void okAction();
  
  // PRIVATE
  private final String fTitle;
  private final JFrame fParent;
  private JDialog fDialog;
  private final int fCloseAction;
  
  /**
  * Return a standardized row of command buttons, right-justified and 
  * all of the same size, with OK as the default button, and no mnemonics used, 
  * as per the Java Look and Feel guidelines. 
  */
  private JComponent getCommandRow() {
    JButton ok = new JButton("OK");
    ok.addActionListener( new ActionListener() {
      @Override public void actionPerformed(ActionEvent event) {
        okAction();
      }
    });
    fDialog.getRootPane().setDefaultButton( ok );
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        closeDialog();
      }
    });
    List<JComponent> buttons = new ArrayList<>();
    buttons.add(ok);
    buttons.add(cancel);
    return UiUtil.getCommandRow(buttons);
  }

  /**
  * Force the escape key to call the same action as pressing the Cancel button.
  *
  * <P>This does not always work. See class comment.
  */
  private void addCancelByEscapeKey(){
    String CANCEL_ACTION_KEY = "CANCEL_ACTION_KEY";
    int noModifiers = 0;
    KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, noModifiers, false);
    InputMap inputMap = 
      fDialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
    ;
    inputMap.put(escapeKey, CANCEL_ACTION_KEY);
    AbstractAction cancelAction = new AbstractAction(){
      @Override public void actionPerformed(ActionEvent e){
        closeDialog();
      }
    }; 
    fDialog.getRootPane().getActionMap().put(CANCEL_ACTION_KEY, cancelAction);
  }
  
  private void closeDialog(){
    fDialog.dispose();
  }
}
