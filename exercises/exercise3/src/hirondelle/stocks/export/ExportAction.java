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
 Present a dialog to allow user to save all {@link hirondelle.stocks.portfolio.Portfolio} 
 data as a single text file.

 <P>The text file is not intended to be edited by the end user, but rather 
 as the input to the corresponding {@link ImportAction}. The format of the 
 file is defined by {@link java.util.prefs.Preferences#exportSubtree}. 
 The file name and extension are arbitrary, and defined entirely by the user.
*/
public final class ExportAction extends AbstractAction {

  /**
  * Constructor. 
  * @param aFrame parent frame to which this dialog is attached.
  */
  public ExportAction(JFrame aFrame) {
    super("Export...", UiUtil.getEmptyIcon()); 
    Args.checkForNull(aFrame);
    fFrame = aFrame;
    putValue(SHORT_DESCRIPTION, "Save all stored Portfolios as a single text file");
    putValue(
      LONG_DESCRIPTION, 
      "Save all stored Portfolios as an XML file, suitable as input to the Import command."
    );
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_E) );    
  }

  /**
  * Allow the user to choose a destination file, then write the content of 
  * all saved <tt>Portfolio</tt> objects to this file as XML.
  *
  *<P><ul>
  * <li>If a file is not selected, then take no action.
  * <li>If a nonexisting file is selected, the write proceeds immediately.
  * <li>If an existing file is selected but it has no write access, then the user is 
  * informed and asked to select a different file.
  * <li>If an existing file is selected and it has write access, then the user is informed
  * that the file will be overwritten, and is asked to confirm this operation.
  *</ul>
  *
  * <P>User navigation is minimized by storing the apparent directory of current interest. 
  * If the user selects this action a <em>second</em> time, then the file 
  * selection dialog is pre-set to this directory of interest, so that repeated 
  * navigation of the file system is avoided.
  */
  @Override public void actionPerformed (ActionEvent event) {
    fLogger.info("Exporting Portfolios to a single text file.");
    JFileChooser chooser = new JFileChooser(fDirOfInterest);
    chooser.setDialogTitle("Export All Portfolios");
    chooser.setApproveButtonToolTipText("Save all Portfolios to single text file");
    
    int choice = chooser.showDialog(fFrame, "Export");
    if (choice != JFileChooser.APPROVE_OPTION) {
      fLogger.fine("No file selection made");
      return;
    }
    
    File file = chooser.getSelectedFile();
    fDirOfInterest = file.getParentFile();
    if ( file.exists() && !file.canWrite() ){
      fLogger.fine("file does not have write access: " + file);
      JOptionPane.showMessageDialog(
        fFrame, fNO_WRITE_MESSAGE, 
        UiUtil.getDialogTitle("Invalid Export File"), JOptionPane.ERROR_MESSAGE
      );
      return;
    }
    
    if ( file.exists() && file.canWrite() ) {
      fLogger.fine( "File exists and may be overwritten: " + file);
      if ( ! isOverwriteConfirmed(file) ) return;
    }
    
    fLogger.fine("Attempting to write to export file");
    PortfolioDAO dao = new PortfolioDAO();
    dao.exportXML(file);
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
  
  private static final Object[] fNO_WRITE_MESSAGE = {
    "Read-only file.",
    "The file you have selected is read-only, and cannot be changed.",
    "Please select a new file."
  };

  private static final Logger fLogger = Util.getLogger(ExportAction.class); 
  
  private boolean isOverwriteConfirmed(File aFile){
    String title = UiUtil.getDialogTitle("Confirm Overwrite");
    Object[] message = {
      "This file already exists.",
      "Do you wish to overwrite " + aFile.getName() + " ?"
    };
    int result = JOptionPane.showConfirmDialog(
      fFrame, message, title, JOptionPane.YES_NO_OPTION
    );
    return (result == JOptionPane.YES_OPTION ? true : false);
  }
}
