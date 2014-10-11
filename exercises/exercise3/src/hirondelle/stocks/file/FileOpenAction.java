package hirondelle.stocks.file;

import java.awt.event.*;
import javax.swing.*;
import hirondelle.stocks.portfolio.Portfolio;
import hirondelle.stocks.portfolio.PortfolioDAO;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.portfolio.EditSaver;
import java.util.logging.Logger;
import hirondelle.stocks.util.Util;

/**
* Present an alphabetical list of <tt>Portfolio</tt> names, from which the user may 
* choose one, which is then presented to the user as the <tt>CurrentPortfolio</tt>.
*
* <P>If the <tt>CurrentPortfolio</tt> being replaced has unsaved edits, 
* then the appropriate <tt>Save</tt> or <tt>SaveAs</tt> action is called 
* before this action executes.
*
*<P>This action is always enabled.
*/
public final class FileOpenAction extends AbstractAction {

  /**
  * Constructor.
  *  
  * @param aCurrentPortfolio is updated by this action.
  * @param aFrame the parent window
  * @param aEditSaver allows the user to save any unsaved edits of the old
  * <tt>CurrentPortfolio</tt> being replaced by this action.
  */
  public FileOpenAction(
    CurrentPortfolio aCurrentPortfolio, JFrame aFrame, EditSaver aEditSaver
  ) {
    super("Open", UiUtil.getImageIcon("/toolbarButtonGraphics/general/Open")); 
    Args.checkForNull(aCurrentPortfolio);
    Args.checkForNull(aFrame);
    Args.checkForNull(aEditSaver);
    fCurrentPortfolio = aCurrentPortfolio;
    fFrame = aFrame;
    fEditSaver = aEditSaver;
    putValue(SHORT_DESCRIPTION, "Open an existing portfolio");
    putValue(
      ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK)
    );
    putValue(LONG_DESCRIPTION, "Open an existing portfolio with a given name");
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O) );    
  }

  @Override public void actionPerformed(ActionEvent event) {
    fLogger.info("Open an existing portfolio.");
    fPortfolioDAO = new PortfolioDAO();
    fEditSaver.save(fCurrentPortfolio, event);
    showDialog();
  }  

  // PRIVATE 
  private CurrentPortfolio fCurrentPortfolio;
  private PortfolioDAO fPortfolioDAO;
  private JFrame fFrame;
  private EditSaver fEditSaver;
  private static final Logger fLogger = Util.getLogger(FileOpenAction.class);

  /** Allow user to select the desired portfolio from a list.  */
  private void showDialog(){
    String selectedName = askForSelectedPortfolio();
    if ( selectedName != null ) {
      openPortfolio(selectedName);
    }
  }
  
  /**
  * Returns the user selection for an existing Portfolio name, as selected from an 
  * alphabetical list.
  * 
  * If user hits Cancel button or closes the frame, then null is returned. If 
  * the user hits OK without making a selection, then the first item in the 
  * list is returned.
  */
  private String askForSelectedPortfolio(){
    Object[] portfolioNames = fPortfolioDAO.fetchAllPortfolioNames().toArray();
    String title = UiUtil.getDialogTitle("Open Portfolio");
    Object result = JOptionPane.showInputDialog(
      fFrame, "Please select a Portfolio:", title, 
      JOptionPane.QUESTION_MESSAGE, null, portfolioNames, null
    );
    return ( result == null ? null : result.toString() );
  }

  private void openPortfolio(String aSelectedName ){
    Portfolio selectedPortfolio = fPortfolioDAO.fetch(aSelectedName);
    fCurrentPortfolio.setPortfolio(selectedPortfolio);
    fCurrentPortfolio.setNeedsSave(false);
    fCurrentPortfolio.notifyObservers();
  }
}
