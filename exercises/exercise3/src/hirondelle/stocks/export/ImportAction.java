package hirondelle.stocks.export;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.logging.*;

import hirondelle.stocks.portfolio.PortfolioDAO;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.util.Util;

/**
* Allows user to replace the all {@link hirondelle.stocks.portfolio.Portfolio}s 
* used by this application with those defined in a single text file.
*
* <P>The text file must be the unaltered result of a previous {@link ExportAction}.
*/
public final class ImportAction extends AbstractAction {
  
  /**
  * Constructor. 
  * @param aFrame parent window to which this action is attached.
  */
  public ImportAction(JFrame aFrame) {
    super("Import...", UiUtil.getEmptyIcon()); 
    Args.checkForNull(aFrame);
    fFrame = aFrame;
    putValue(SHORT_DESCRIPTION, "Import the result of a previous Export operation.");
    putValue(
      LONG_DESCRIPTION, "Replace all Portfolios with those defined in a text file."
    );
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I) );    
  }

  /**
  * Allow the user to select a single file, and attempt to use its contents to replace 
  * all currently stored {@link hirondelle.stocks.portfolio.Portfolio} objects.
  *
  *<P>
  * <ul>
  * <li>If a file is not selected, then take no action.
  * <li>If a non-existing file is selected, inform the user and ask them to select 
  * something else.
  * <li>If an existing but non-readable file is selected, inform the user and ask them to 
  * select something else.
  *</ul>
  *
  *<P> If the user attempts to import a second time, present the file selection GUI using 
  * the directory in which they showed interest during the first import. (This will 
  * often decrease the navigation burden on the user.)
  */
  @Override public void actionPerformed (ActionEvent event) {
    fLogger.info("Importing portfolios from a text file.");
    JFileChooser chooser = new JFileChooser(fDirOfInterest);
    chooser.setDialogTitle("Import-Replace All Portfolios");
    chooser.setApproveButtonToolTipText("Replace all Portfolios");
    
    int choice = chooser.showDialog(fFrame, "Import-And-Replace");
    if (choice != JFileChooser.APPROVE_OPTION) {
      fLogger.fine("No file selection made");
      return;
    }
    
    File file = chooser.getSelectedFile();
    fDirOfInterest = file.getParentFile();
    
    if ( !file.exists() ){
      fLogger.fine("Selected file does not exist " + file);
      showErrorMessage(fNOT_EXIST_MESSAGE);
      return;
    }
    
    if ( file.exists() && !file.canRead() ){
      fLogger.fine("File does not have read access: " + file);
      showErrorMessage(fNO_READ_MESSAGE);
      return;
    }
    
    fLogger.fine("Attempting to import file");
    PortfolioDAO dao = new PortfolioDAO();
    dao.importXML(file);
  }
  
  // PRIVATE 
  private JFrame fFrame;
  
  /**
  * The directory which the user last expressed an interest.
  *
  * This value is set each time the <tt>FileChooser</tt> is closed, using the 
  * directory corresponding to the file selection. It is then later used upon any second 
  * invocation, to free the user from probably repeating the same navigation operations.
  */
  private static File fDirOfInterest;
  
  private static final String fINVALID_IMPORT_FILE = "Invalid Import File";
  
  private static final Object[] fNO_READ_MESSAGE = {
    "File cannot be read.",
    "A readable file must be selected for an Import.",
    "Please select a different file."
  };
  
  private static final Object[] fNOT_EXIST_MESSAGE = {
    "File does not currently exist.",
    "An existing file must be selected for an Import.",
    "Please select a different file."
  };

  private static final Logger fLogger = Util.getLogger(ImportAction.class); 
  
  private void showErrorMessage(Object[] aMessage){
    JOptionPane.showMessageDialog(
      fFrame, aMessage,
      UiUtil.getDialogTitle(fINVALID_IMPORT_FILE),
      JOptionPane.ERROR_MESSAGE
    );
  }
}
