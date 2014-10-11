package hirondelle.stocks.file;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import hirondelle.stocks.portfolio.PortfolioDAO;
import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.util.ui.UiUtil;
import java.util.logging.Logger;
import hirondelle.stocks.util.Util;

/**
* Save the edits performed on the {@link CurrentPortfolio}, and update the display 
* to show that the <tt>CurrentPortfolio</tt> no longer needs a save.
*/
public final class FileSaveAction extends AbstractAction implements Observer {

  /**
  * Constructor.
  *  
  * @param aCurrentPortfolio is to be saved by this action.
  */
  public FileSaveAction(CurrentPortfolio aCurrentPortfolio) {
    super("Save", UiUtil.getImageIcon("/toolbarButtonGraphics/general/Save")); 
    fCurrentPortfolio = aCurrentPortfolio;
    fCurrentPortfolio.addObserver(this);
    putValue(SHORT_DESCRIPTION, "Save edits to the current portfolio");
    putValue(
      ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK)
    );
    putValue(LONG_DESCRIPTION, "Save edits to the current portfolio");
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));    
  }

  @Override public void actionPerformed(ActionEvent e) {
    fLogger.info("Saving edits to the current portfolio.");
    PortfolioDAO portfolioDAO = new PortfolioDAO();
    portfolioDAO.save(fCurrentPortfolio.getPortfolio());
    fCurrentPortfolio.setNeedsSave(false);
    fCurrentPortfolio.notifyObservers();
  }  
  
  /**
  * Synchronize the state of this object with the state of the 
  * <tt>CurrentPortfolio</tt> passed to the constructor.
  *
  * This action is enabled only when the <tt>CurrentPortfolio</tt> is titled and 
  * needs a save.
  */
  @Override public void update(Observable aPublisher, Object aData) {
    setEnabled(fCurrentPortfolio.getNeedsSave() && !fCurrentPortfolio.isUntitled());
  } 
  
  // PRIVATE 
  private CurrentPortfolio fCurrentPortfolio;
  private static final Logger fLogger = Util.getLogger(FileSaveAction.class);
}
