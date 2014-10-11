package hirondelle.stocks.file;

import java.awt.event.*;
import javax.swing.*;
import java.util.logging.*;

import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.portfolio.PortfolioDAO;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.portfolio.EditSaver;
import hirondelle.stocks.util.Util;

/**
* Close the application.
*
* <P>If the {@link CurrentPortfolio} has any unsaved edits, then allow the user 
* to decide if those edits should be saved.
* 
*<P>When the application is closed, the <tt>CurrentPortfolio</tt> is 
* saved as the default, and will be automatically loaded upon any re-launch.
*/
public final class FileExitAction extends AbstractAction {
  
  /**
  * Constructor.  
  * @param aCurrentPortfolio may have unsaved edits when this action is taken.
  * @param aEditSaver allows the user to save any unsaved edits.
  */
  public FileExitAction(CurrentPortfolio aCurrentPortfolio, EditSaver aEditSaver) {
    super("Exit", UiUtil.getEmptyIcon()); 
    Args.checkForNull(aCurrentPortfolio);
    Args.checkForNull(aEditSaver);
    fEditSaver = aEditSaver;
    fCurrentPortfolio = aCurrentPortfolio;
    putValue(SHORT_DESCRIPTION, "Close the application");
    //the windows ALT+F4 for File->Exit does not form part of the Java Look&Feel
    //putValue(
    //  ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK) 
    //);
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_X) );    
  }

  @Override public void actionPerformed(ActionEvent event) {
    fLogger.info("Exiting the application.");
    fEditSaver.save(fCurrentPortfolio, event);
    PortfolioDAO dao = new PortfolioDAO();
    dao.saveAsDefault( fCurrentPortfolio.getPortfolio() );
    System.exit(0);
  }
  
  // PRIVATE
  private CurrentPortfolio fCurrentPortfolio;
  private EditSaver fEditSaver;
  private static final Logger fLogger = Util.getLogger(FileExitAction.class); 
}