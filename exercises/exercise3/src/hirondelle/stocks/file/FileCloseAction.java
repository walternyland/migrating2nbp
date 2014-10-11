package hirondelle.stocks.file;

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.portfolio.EditSaver;
import java.util.logging.Logger;
import hirondelle.stocks.util.Util;

/**
 * Close the {@link CurrentPortfolio}, and display to the user an empty untitled portfolio,
 * which does not need a save.
 * <P>
 * If the <tt>CurrentPortfolio</tt> needs to be saved, then the user is offered the
 * option of saving its edits.
 */
public final class FileCloseAction extends AbstractAction implements Observer {

  /**
   * Constructor.
   * 
   * @param aCurrentPortfolio will be updated by this action to be untitled and empty of
   * stocks.
   * @param aEditSaver if <tt>aCurrentPortfolio</tt> has any unsaved edits, then
   * <tt>aEditSaver</tt> will offer the user the option of saving the edits.
   */
  public FileCloseAction(CurrentPortfolio aCurrentPortfolio, EditSaver aEditSaver) {
    super("Close", UiUtil.getEmptyIcon());
    Args.checkForNull(aEditSaver);
    fCurrentPortfolio = aCurrentPortfolio;
    fCurrentPortfolio.addObserver(this);
    fEditSaver = aEditSaver;
    putValue(SHORT_DESCRIPTION, "Close the current portfolio");
    putValue(
      ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK)
    );
    putValue(
      LONG_DESCRIPTION, "Close the current portfolio and display an empty set of stocks"
    );
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
  }

  @Override public void actionPerformed(ActionEvent event) {
    fLogger.info("Closing the current portfolio, and starting empty.");
    fEditSaver.save(fCurrentPortfolio, event);
    fCurrentPortfolio.clear();
    fCurrentPortfolio.setNeedsSave(false);
    fCurrentPortfolio.notifyObservers();
  }

  /**
   * Synchronize the state of this object with the state of the {@link CurrentPortfolio}
   * passed to the constructor. This action is disabled only when the
   * <tt>CurrentPortfolio</tt> is untitled and does not need a save.
   */
  public void update(Observable aPublisher, Object aData) {
    boolean noClose = fCurrentPortfolio.isUntitled() && !fCurrentPortfolio.getNeedsSave();
    setEnabled(!noClose);
  }

  // PRIVATE
  private CurrentPortfolio fCurrentPortfolio;
  private EditSaver fEditSaver;
  private static final Logger fLogger = Util.getLogger(FileCloseAction.class);
}
