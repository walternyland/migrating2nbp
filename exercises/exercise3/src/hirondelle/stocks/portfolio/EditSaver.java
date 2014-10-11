package hirondelle.stocks.portfolio;

import javax.swing.*;
import java.awt.event.*;
import hirondelle.stocks.util.Args;

/**
* Allow the user the choice of saving edits to the {@link CurrentPortfolio}.
*/
public final class EditSaver {

  /**
  * Constructor.
  *  
  * @param aSaveAction performed if the user decides to save edits and the
  * <tt>CurrentPortfolio</tt> has a title.
  * @param aSaveAsAction performed if the user decides to save edits and the
  * <tt>CurrentPortfolio</tt> does not have a title.
  * @param aFrame parent window to which this dialog belongs.
  */
  public EditSaver(Action aSaveAction, Action aSaveAsAction, JFrame aFrame) {
    Args.checkForNull(aSaveAction);
    Args.checkForNull(aSaveAsAction);
    Args.checkForNull(aFrame);
    fSaveAction = aSaveAction;
    fSaveAsAction = aSaveAsAction;
    fFrame = aFrame;
  }

  /**
   * Save <tt>aCurrentPortfolio</tt>, if necessary.
   *  
   * <P>If <tt>aCurrentPortfolio</tt>
   * has no unsaved edits, or if the user does not wish to save edits, then do nothing.
   * Otherwise, perform either <tt>SaveAction</tt> or <tt>SaveAsAction</tt>,
   * according to whether <tt>aCurrentPortfoilo</tt> is titled or untitled,
   * respectively.
   * 
   * @param aEvent is passed to either <tt>SaveAction</tt> or
   * <tt>SaveAsAction</tt>.
   */
  public void save(CurrentPortfolio aCurrentPortfolio, ActionEvent aEvent) {
    if (!aCurrentPortfolio.getNeedsSave()) return;
    if (!userWantsToSaveEdits(aCurrentPortfolio)) return;

    if (aCurrentPortfolio.isUntitled()) {
      fSaveAsAction.actionPerformed(aEvent);
    }
    else {
      fSaveAction.actionPerformed(aEvent);
    }
  }

  // PRIVATE 
  private Action fSaveAction;
  private Action fSaveAsAction;
  private JFrame fFrame;

  private boolean userWantsToSaveEdits(CurrentPortfolio aCurrentPortfolio) {
    StringBuilder message = new StringBuilder("Do you want to save the edits for the \"");
    message.append(aCurrentPortfolio.getName());
    message.append("\" portfolio?");
    int result = JOptionPane.showConfirmDialog(
      fFrame, message.toString(), "Confirm Save Edits", JOptionPane.YES_NO_OPTION
    );
    return (result == JOptionPane.YES_OPTION ? true : false);
  }
}
