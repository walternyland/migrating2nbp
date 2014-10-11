package hirondelle.stocks.file;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.logging.*;

import hirondelle.stocks.portfolio.Portfolio;
import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.portfolio.PortfolioDAO;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.util.Util;

/**
* Delete the {@link CurrentPortfolio} from storage, and display an untitled 
* portfolio to the user, which does not need a save.
* 
*<P>The user is always asked to first confirm the deletion.
*/
public final class FileDeleteAction extends AbstractAction implements Observer {
  
  /**
  * Constructor. 
  * @param aCurrentPortfolio will be deleted from storage by this action.
  * @param aParentFrame window to which this action is attached.
  */
  public FileDeleteAction(CurrentPortfolio aCurrentPortfolio, JFrame aParentFrame) {
    super("Delete", UiUtil.getImageIcon("/toolbarButtonGraphics/general/Delete")); 
    Args.checkForNull(aParentFrame);
    fCurrentPortfolio = aCurrentPortfolio;
    fCurrentPortfolio.addObserver( this );
    fFrame = aParentFrame;
    putValue(SHORT_DESCRIPTION, "Delete the current portfolio");
    putValue(
      LONG_DESCRIPTION, "Delete the current portfolio,both  from view and storage."
    );
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D) );    
  }

  @Override public void actionPerformed(ActionEvent e) {
    fLogger.info("Deleting the current portfolio from storage.");
    if ( isConfirmed() ) {
      deleteCurrentPortfolio();
    }
  }  
  
  /**
  * Synchronize the state of this object with the state of the 
  * {@link CurrentPortfolio} passed to the constructor.
  *
  * This action is enabled only when the <tt>CurrentPortfolio</tt> is titled.
  */
  public void update(Observable aPublisher, Object aData) {
    setEnabled( ! fCurrentPortfolio.isUntitled() );
  } 
  
  // PRIVATE 
  private CurrentPortfolio fCurrentPortfolio;
  private JFrame fFrame;
  private static final Logger fLogger = Util.getLogger(FileDeleteAction.class); 
  
  private boolean isConfirmed(){
    String title = UiUtil.getDialogTitle("Confirm Delete");
    String message = 
      "Are you sure you want to delete the Portfolio named '" + 
       fCurrentPortfolio.getName() + "' ?"
    ;
    /* 
    * Implementation Note
    * The implementation is complicated by the need to disable the default Yes 
    * button, which is not appropriate for a dialog which confirms a delete.
    * If this was not necessary, the implementation would be just two lines:
    * int result = JOptionPane.showConfirmDialog(
    *   fFrame, message, title, JOptionPane.YES_NO_OPTION
    * );
    * return (result == JOptionPane.YES_OPTION ? true : false);
    */
    JOptionPane optionPane = new JOptionPane(
      message, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION
    );
    JDialog dialog = optionPane.createDialog(fFrame, title);
    UiUtil.noDefaultButton(dialog.getRootPane());
    dialog.setVisible(true);
    return hasSelectedYes(optionPane);
  }
  
  private boolean hasSelectedYes(JOptionPane aOptionPane){
    boolean result = false;
    Object selection = aOptionPane.getValue();
    if (selection != null && selection instanceof Integer){
      Integer selectionVal = (Integer)selection; //cannot avoid cast
      if (selectionVal.intValue() == JOptionPane.YES_OPTION){
        result = true;
      }
    }
    fLogger.fine("Selected Yes?: " + result);
    return result;
  }
  
  private void deleteCurrentPortfolio(){
    PortfolioDAO portfolioDAO = new PortfolioDAO();
    portfolioDAO.delete(fCurrentPortfolio.getPortfolio());
    fCurrentPortfolio.setPortfolio(Portfolio.getUntitledPortfolio());
    fCurrentPortfolio.notifyObservers();
  }
}