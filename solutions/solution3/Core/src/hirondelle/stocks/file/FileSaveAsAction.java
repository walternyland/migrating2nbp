package hirondelle.stocks.file;

import hirondelle.stocks.main.CentralLookup;
import java.awt.event.*;
import javax.swing.*;
import hirondelle.stocks.portfolio.PortfolioDAO;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.portfolio.CurrentPortfolio;
import java.util.logging.Logger;
import hirondelle.stocks.util.Util;
import java.awt.Frame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
* Store the {@link CurrentPortfolio} under a new unique name, and 
* display it to the user as the <tt>CurrentPortfolio</tt>, which does not 
* need a save, and which reflects the new name just entered.
*
* <P>This action is always enabled.
*/
@ActionID(
        category = "File",
        id = "hirondelle.stocks.quotes.SaveAsAction"
)
@ActionRegistration(
        displayName = "#CTL_SaveAsAction"
)
@ActionReference(path = "Menu/File", position = 50)
@NbBundle.Messages("CTL_SaveAsAction=Save As...")
public final class FileSaveAsAction extends AbstractAction {

  /**
  * Constructor.
  *  
  * @param aCurrentPortfolio is to be saved by this action.
  * @param aFrame the parent window
  */
  public FileSaveAsAction() {
    super("Save As...", UiUtil.getImageIcon("/toolbarButtonGraphics/general/SaveAs")); 
//    Args.checkForNull(aCurrentPortfolio);
//    Args.checkForNull(aFrame);
    fCurrentPortfolio = CentralLookup.getDefault().lookup(CurrentPortfolio.class);
//    fFrame = aFrame;
    putValue(SHORT_DESCRIPTION, "Save the current portfolio under a new name");
    putValue(LONG_DESCRIPTION, "Save the current portfolio under a new given name");
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A) );    
  }

  /**
  * Allow user to input a new name for the current portfolio. 
  *
  * <P>The name must have a non-zero trimmed length, and must be unique.
  * If the name entered by the user does not comply, then they are 
  * informed and asked to re-input.
  */
  @Override public void actionPerformed(ActionEvent e) {
    fLogger.info("Saving the current portfolio under a new name.");    
    fPortfolioDAO = new PortfolioDAO();
    showDialog();
  }  

  // PRIVATE 
  
  private CurrentPortfolio fCurrentPortfolio;
  private PortfolioDAO fPortfolioDAO;
  private JFrame fFrame = (JFrame)WindowManager.getDefault().getMainWindow();
  private static final Logger fLogger = Util.getLogger(FileSaveAsAction.class);
  
  private void showDialog(){
    String newName = Consts.EMPTY_STRING; 
    while ( isInvalid(newName) ) {
      newName = askForNewName();
      if (newName == null) return;
      if (isInvalid(newName)) {
        String title = UiUtil.getDialogTitle("Invalid Input");
        String message = 
          "New name must have content, and must not " + 
          "duplicate a known Portfolio name."
        ;
        JOptionPane.showMessageDialog(
          fFrame, message, title, JOptionPane.INFORMATION_MESSAGE
        );
      }
    }
    savePortfolio(newName);
  }

  /**
  * Returns the user input for the new Portfolio name.
  * 
  * If user hits Cancel button or closes the frame, then null is returned. If 
  * the user hits OK without performing any input, then an empty String is returned.
  */
  private String askForNewName(){
    String title = UiUtil.getDialogTitle("Save As");
    return JOptionPane.showInputDialog(
      fFrame, "File Name:", title, JOptionPane.QUESTION_MESSAGE
    );
  }

  private boolean isInvalid( String aNewName ){
    return ! fPortfolioDAO.isValidCandidateName(aNewName);
  }

  private void savePortfolio(String aNewName ){
    fCurrentPortfolio.setName( aNewName );
    fCurrentPortfolio.setNeedsSave(false);
    fPortfolioDAO.saveAs(fCurrentPortfolio.getPortfolio());
    fCurrentPortfolio.notifyObservers();
  }
}