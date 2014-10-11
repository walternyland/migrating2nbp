package hirondelle.stocks.file;

import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.portfolio.EditSaver;
import hirondelle.stocks.portfolio.PortfolioDAO;
import hirondelle.stocks.quotes.Stock;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.ui.UiUtil;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

/**
* Create a new {@link hirondelle.stocks.portfolio.Portfolio} which has a unique name, 
* is placed in storage, and is displayed to the user as the (empty) 
* {@link CurrentPortfolio}, and which does not need a save.
*
* <P>If the <tt>CurrentPortfolio</tt> needs to be saved, 
* then the appropriate Save or Save As action is called.
*
* <P>This action is always enabled.
*/
public final class FileNewAction extends AbstractAction {

  /**
  * Constructor.
  *  
  * @param aCurrentPortfolio is updated by this action
  * @param aFrame parent window
  * @param aEditSaver allows the user to save any unsaved edits of the old 
  * <tt>CurrentPortfolio</tt>
  */
  public FileNewAction(
    CurrentPortfolio aCurrentPortfolio, JFrame aFrame, EditSaver aEditSaver
  ) {
    super("New", UiUtil.getImageIcon("/toolbarButtonGraphics/general/New")); 
    Args.checkForNull(aCurrentPortfolio);
    Args.checkForNull(aFrame);
    Args.checkForNull(aEditSaver);
    fCurrentPortfolio = aCurrentPortfolio;
    fFrame = aFrame;
    fEditSaver = aEditSaver;
    putValue(SHORT_DESCRIPTION, "Create a new portfolio");
    putValue(
      ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK) 
    );
    putValue(
      LONG_DESCRIPTION, "Create a new portfolio with given name and an empty set of stocks"
    );
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N) );    
  }

  /**
  * Present dialog for the input of a name for the new <tt>Portfolio</tt>. 
  *
  * <P> The supplied name must have a non-zero trimmed length, and must not be the 
  * the same name as any currently stored <tt>Portfolio</tt>.
  * If the name entered by the user is invalid, then when <tt>OK</tt> is selected
  * an info box informs the users of the problem, and they 
  * are asked to enter another name.
  *
  * <P> If the <tt>CurrentPortfolio</tt> being replaced by this action has 
  * any unsaved edits, then allow the user the option of saving such edits.
  */
  @Override public void actionPerformed(ActionEvent event) {
    fLogger.info("Start a new portfolio, with a unique name.");
    fPortfolioDAO = new PortfolioDAO();
    fEditSaver.save(fCurrentPortfolio, event);
    showDialog();
  }  
  
  // PRIVATE 
  private CurrentPortfolio fCurrentPortfolio;
  private JFrame fFrame;
  private PortfolioDAO fPortfolioDAO;
  private EditSaver fEditSaver;
  private static final Logger fLogger = Util.getLogger(FileNewAction.class);

  private void showDialog(){
    String newName = Consts.EMPTY_STRING; 
    while ( isInvalid(newName) ) {
      newName = askForNewName();
      if (newName == null) return;
      if ( isInvalid(newName) ) {
        String message = 
          "New name must have content, and must not duplicate a known Portfolio name."
        ;
        JOptionPane.showMessageDialog(
          fFrame, message, "Invalid Input", JOptionPane.INFORMATION_MESSAGE
        );
      }
    }
    initNewPortfolio(newName);
  }

  /**
  * Returns the user input for the new Portfolio name.
  * 
  * If user hits Cancel button or closes the frame, then null is returned. If 
  * the user hits OK without performing any input, then an empty String is returned.
  */
  private String askForNewName(){
    String title = UiUtil.getDialogTitle("New File");
    return JOptionPane.showInputDialog(
      fFrame, "File Name:", title, JOptionPane.QUESTION_MESSAGE
    );
  }
  
  private boolean isInvalid( String aNewName ){
    return ! fPortfolioDAO.isValidCandidateName(aNewName);
  }

 private void initNewPortfolio( String aNewName ){
    fCurrentPortfolio.setStocks(new TreeSet<Stock>());
    fCurrentPortfolio.setName(aNewName);
    fCurrentPortfolio.setNeedsSave(false);
    fPortfolioDAO.saveAs(fCurrentPortfolio.getPortfolio());
    fCurrentPortfolio.notifyObservers();
  }
}
