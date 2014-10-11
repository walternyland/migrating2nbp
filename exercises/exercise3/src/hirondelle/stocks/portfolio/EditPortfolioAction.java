package hirondelle.stocks.portfolio;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.logging.*;
import java.util.*;

import hirondelle.stocks.quotes.Stock;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.StandardEditor;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.util.Util;

/**
* Present dialog to edit the list of {@link hirondelle.stocks.quotes.Stock} 
* objects in the {@link CurrentPortfolio}. (See {@link StockEditor} as well.)
*/
public final class EditPortfolioAction extends AbstractAction {
  
  /**
  * Constructor.
  *  
  * @param aCurrentPortfolio contains the stocks in which the user has an interest.
  * @param aFrame parent frame to which this editor is attached (the main window).
  */
  public EditPortfolioAction (CurrentPortfolio aCurrentPortfolio, JFrame aFrame) {
    super("Portfolio...", UiUtil.getEmptyIcon()); 
    Args.checkForNull(aCurrentPortfolio);
    Args.checkForNull(aFrame);
    fCurrentPortfolio = aCurrentPortfolio;
    fModel = new Model();
    fFrame = aFrame;
    putValue(SHORT_DESCRIPTION, "Edit the stocks in this portfolio");
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK));
    putValue(LONG_DESCRIPTION, "Add, change, delete operations on stocks in portfolio.");
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_P) );    
  }

  /**
  * Display a modal dialog, centered on the main window, to allow the user to 
  * edit their portfolio of stocks.
  *
  * <P>If the user selects <tt>OK</tt>, save any edits and update the display of 
  * the main window - otherwise, discard all edits.
  *
  *<P> The current stocks are presented in a table, along with 
  * <tt>Add-Change-Delete</tt> buttons. 
  * The <tt>Add</tt> button is always enabled, while the <tt>Change</tt> and 
  * <tt>Delete</tt> buttons are enabled only if a table row is selected.
  *
  *<P> <tt>Add</tt> and <tt>Change</tt> operations will verify input of 
  * each field : when focus leaves a field, any invalid input will cause a 
  * short message to be placed in the field. Selecting <tt>OK</tt> while 
  * any item is still invalid will cause a message to be displayed, 
  * asking for correction before proceeding.
  * The only difference between <tt>Add</tt> and <tt>Change</tt> is 
  * that the <tt>Change</tt> operation will pre-populate data entry areas.
  */
  @Override public void actionPerformed(ActionEvent aEvent) {
    fLogger.info("Edit the list of stocks in the current portfolio.");
    initWorkingCopy();
    showDialog();
  }

  // PRIVATE

  /**
  * The current portfolio, whose contents are to be edited. It is important to
  * note that this field is updated only if the user selects OK and if 
  * fWorkingCopy does not equal fCurrentPortfolio. All individual 
  * edits made by the user are performed on fWorkingCopy. This allows the user 
  * to easily back out of any edits they have performed.
  */
  private CurrentPortfolio fCurrentPortfolio;
  
  /**
  * Contains the {@link Stock} objects being edited.
  *
  * <P>Initially contains all items in fCurrentPortfolio; all edits 
  * performed by the user will be initially applied only to fWorkingCopy. If 
  * the user ultimately chooses OK, then the objects in 
  * fCurrentPortfolio are made to match the items in fWorkingCopy. 
  * 
  * <P>Created once upon construction, and emptied and reused each time 
  * actionPerformed is called.
  */
  private Set<Stock> fWorkingCopy;

  /**
  * Allows the user to select one of the stocks in their portfolio.
  */
  private JList<Model> fStockSelector;
  
  /** Underlies fStockSelector.  */
  private Model fModel;

  /** Parent window to which the dialog is attached.  */
  private JFrame fFrame;
  
  private static final Logger fLogger = Util.getLogger(EditPortfolioAction.class); 
  
  /**
  * Create a working copy of the Stock objects in fCurrentPortfolio.
  * It is important to note that since Stock objects are immutable, 
  * then creating a copy with independent storage is unnecessary. 
  * The <tt>Collection</tt> of items is edited by adding and
  * removing objects themselves, and not by changing the state of objects.
  */
  private void initWorkingCopy(){
    /* 
    * Implementation Note
    * TreeSort preserves the sort order, even when the set changes
    */
    fWorkingCopy = new TreeSet<Stock>( fCurrentPortfolio.getStocks() );
    fLogger.fine("Working copy inited to: " + fWorkingCopy);
  }
  
  private void synchDisplayWithWorkingCopy(){
    fStockSelector.setSelectedIndex(0);
    fStockSelector.updateUI();
  }
  
  private void showDialog(){
    Editor editor = new Editor("Edit Portfolio", fFrame);
    editor.showDialog();
  }
  
  private JComponent getStockSelector(){
    fStockSelector = new JList<Model>(fModel);
    fStockSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    //initially, no item is selected :
    //fStockSelector.setSelectedIndex(0); 
    fStockSelector.setToolTipText ("Choose item, then change or delete using buttons");
    JScrollPane scrollPane = new JScrollPane(fStockSelector);
    return scrollPane;
  }

  private JComponent getCommandColumn(){
    JButton add = new JButton("Add...");
    add.setMnemonic(KeyEvent.VK_A); //consumed erroneously by JList in JDK 1.4.0
    add.addActionListener( new ActionListener() {
      @Override public void actionPerformed(ActionEvent e) {
        addStock();
      }
    });
    
    class ChangeAction extends EditAction {
      ChangeAction(){
        super("Change...", KeyEvent.VK_C);
      }
      @Override public void actionPerformed(ActionEvent event){
        changeSelectedStock();
      }
    }
    JButton change = new JButton( new ChangeAction() );
    
    class DeleteAction extends EditAction {
      DeleteAction() {
        super("Delete", KeyEvent.VK_D);
      }
      @Override public void actionPerformed(ActionEvent e){
        deleteSelectedStock();
      }
    }
    
    JButton delete = new JButton( new DeleteAction() );
    java.util.List<JComponent> buttons = new ArrayList<>();
    buttons.add(add);
    buttons.add(change);
    buttons.add(delete);
    return UiUtil.getCommandColumn(buttons);
  }
  
  private void addStock(){
    StockEditor stockEditor = new StockEditor(fFrame);
    Stock newStock = stockEditor.addStock();
    fLogger.fine("Adding stock to working copy: " + newStock);
    if ( newStock != null ) {
      fWorkingCopy.add(newStock);
      synchDisplayWithWorkingCopy();
    }
  }
  
  private void changeSelectedStock(){
    fLogger.fine("Changing stock...");
    Stock selectedStock = getSelectedStock();
    StockEditor stockEditor = new StockEditor(fFrame);
    Stock changedStock = stockEditor.changeStock(selectedStock);
    if ( changedStock != null ) {
      fWorkingCopy.remove(selectedStock);
      fWorkingCopy.add(changedStock);
      synchDisplayWithWorkingCopy();
    }
  }
  
  private void deleteSelectedStock(){
    Stock selectedStock = getSelectedStock();
    fWorkingCopy.remove(selectedStock);
    synchDisplayWithWorkingCopy();
  }

  private Stock getSelectedStock(){
    int idx = fStockSelector.getSelectionModel().getLeadSelectionIndex();
    Collection<Stock> collection = fWorkingCopy; //need as 'generic temp variable'
    java.util.List<Stock> stocks = new ArrayList<>(collection);
    return stocks.get(idx);
  }

  private boolean isSelectionPresent() {
    return ! fStockSelector.getSelectionModel().isSelectionEmpty();
  }

  /**
  * Both the change and delete actions should be enabled only if there 
  * is a selection made in the table. This private class encapsulates 
  * that behavior.
  */
  private abstract class EditAction extends AbstractAction implements ListSelectionListener{
    EditAction(String aText, int aMnemonic){
      super(aText);
      putValue(MNEMONIC_KEY, new Integer(aMnemonic) );    
      setEnabled(false);
      fStockSelector.getSelectionModel().addListSelectionListener(this);
    }
    @Override public void valueChanged(ListSelectionEvent event) {
      fLogger.fine("Firing EditAction.valueChanged");
      setEnabled(isSelectionPresent());
    }
  }
  
  /**
  * The implementation of this nested class is kept short by calling methods 
  * of the enclosing class.
  */
  private final class Editor extends StandardEditor { 
    Editor(String aTitle, JFrame aParent){
      super(aTitle, aParent, StandardEditor.CloseAction.HIDE);
    }
    @Override protected JComponent getEditorUI () {
      JPanel content = new JPanel();
      content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
      content.add( getStockSelector());
      content.add( getCommandColumn() );
      return content;
    }
    @Override protected void okAction() {
      if (fWorkingCopy.equals(fCurrentPortfolio.getStocks())) {
        fLogger.fine("No detected change in underlying stocks.");
      }
      else {
        fLogger.fine("Detected change in underlying stocks.");
        fCurrentPortfolio.setStocks(fWorkingCopy);
        fCurrentPortfolio.setNeedsSave(true);
        fCurrentPortfolio.notifyObservers();
      }
      dispose();
    }
  }  

  /**
  * Adapts fWorkingCopy to a ListModel, and controls how each Stock is 
  * represented as text in the JList.
  */
  private final class Model extends AbstractListModel {
    @Override public Object getElementAt(int aRowIndex) {
      Collection<Stock> collection = fWorkingCopy;
      java.util.List<Stock> list = new ArrayList<>(collection);
      Stock stock = list.get(aRowIndex);
      return stock.getName();
    }
    @Override public int getSize() {
      return fWorkingCopy.size();
    }
  }
}

