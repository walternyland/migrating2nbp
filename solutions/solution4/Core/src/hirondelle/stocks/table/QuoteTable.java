package hirondelle.stocks.table;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import java.beans.*;

import hirondelle.stocks.quotes.Quote;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.preferences.QuoteTablePreferencesEditor;

/**
* Graphical component which displays the latest quote information for the 
* {@link hirondelle.stocks.portfolio.CurrentPortfolio}.
*
*<P> Listens to a {@link QuoteFilterFactory} and to a 
* {@link QuoteTablePreferencesEditor}, for updates on how to display 
* its information.
*
* <P> The sort of the table is altered by the user by clicking on column headers.
*/
public final class QuoteTable extends JScrollPane 
  implements PropertyChangeListener, Observer {

  /**
  * Constructor.
  *  
  * @param aTablePrefsEditor observed by this class to fetch and update 
  * user preferences for the display of this component.
  * @param aQuoteFilterFactory observed by this class to fetch the 
  * {@link QuoteFilter} to be applied to this component's data.
  */
  public QuoteTable(
    QuoteTablePreferencesEditor aTablePrefsEditor, 
    QuoteFilterFactory aQuoteFilterFactory
  ){
    fTablePrefsEditor = aTablePrefsEditor;
    fTablePrefsEditor.addObserver(this);
    fQuoteFilterFactory = aQuoteFilterFactory;
    fQuoteFilterFactory.addPropertyChangeListener(this);
    fModel = new QuoteTableModel();
    fTable = new JTable(fModel);
    fTableSortIndicator = new TableSortIndicator(fTable, UP_ICON, DOWN_ICON);
    fTableSortIndicator.addObserver(this);//bad practice; 'this' not yet constructed
    initGui();
  }
  
  /**
  * Update display of this table using new {@link Quote} 
  * objects.
  */
  public void setQuoteTable(List<Quote> aQuotes) {
    fModel.setQuoteTable(aQuotes);
  }
 
  /**
  * Update the display of this table, not using new underlying data, but using 
  * new preferences for its display.
  *
  *<P> Listens to the {@link QuoteTablePreferencesEditor}
  * passed to the constructor. 
  *
  * <P>(Sort is special since it may be updated both by clicking on the column
  * headers, or by setting a preference.)
  */
  @Override public void update(Observable aObservable, Object aData) {
    if ( aObservable == fTablePrefsEditor ) {
      synchWithPrefs();
    } 
    else if ( aObservable == fTableSortIndicator ) {
      fModel.filterAndSortQuotes();
    }
    else {
      throw new AssertionError("Unknown observable: " + aObservable);
    }
  }

  /**
  * Update the view in response to the selection by the end user of a new 
  * {@link QuoteFilter} for filtering displayed items.
  *
  * <P>Listens to the <tt>QuoteFilterFactory</tt> passed to the constructor.
  */
  @Override public void propertyChange(PropertyChangeEvent event) {
    fModel.filterAndSortQuotes();
  }
  
  // PRIVATE 
  
  private QuoteTablePreferencesEditor fTablePrefsEditor;
  private QuoteFilterFactory fQuoteFilterFactory;
  private JTable fTable;
  private QuoteTableModel fModel;
  
  /**
  * Allows user to select a different sort for the quotes, and provides a visual 
  * indicator of the current sort.
  */  
  private TableSortIndicator fTableSortIndicator;
  private static final ImageIcon UP_ICON =
    UiUtil.getImageIcon("/toolbarButtonGraphics/navigation/Up")
  ;
  private static final ImageIcon DOWN_ICON =
    UiUtil.getImageIcon("/toolbarButtonGraphics/navigation/Down")
  ;
  
  private void initGui(){
    fTable.getTableHeader().setReorderingAllowed(false);
    fTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    synchWithPrefs();
    setViewportView( fTable );
  }
  
  private void synchWithPrefs(){  
    fTable.setShowHorizontalLines( fTablePrefsEditor.hasHorizontalLines() );
    fTable.setShowVerticalLines ( fTablePrefsEditor.hasVerticalLines() );
    fTable.setRowHeight( fTablePrefsEditor.getRowHeight() );
    fModel.setColumnMapping( new ArrayList(fTablePrefsEditor.getColumnOrder()) );
    synchWithTableRendererPrefs();
    int sortIdx = fModel.fColumnMapping.indexOf( fTablePrefsEditor.getSortField() );
    fTableSortIndicator.setSortBy( new SortBy(SortOrder.DESCENDING, sortIdx) ) ;
  }
  
  private void synchWithTableRendererPrefs(){
    for (int idx = 0; idx < fTable.getColumnCount(); ++idx) {
      String colName = fTable.getColumnName(idx);
      QuoteField field = QuoteField.valueFrom(colName);
      if ( 
        field == QuoteField.Change || 
        field == QuoteField.PercentChange || 
        field == QuoteField.Profit ||
        field == QuoteField.PercentProfit
      ) {
        fTable.getColumnModel().getColumn(idx).setCellRenderer(new RenderRedGreen());
      }
      else if ( field == QuoteField.Price ) {
        fTable.getColumnModel().getColumn(idx).setCellRenderer(new RenderPrice());
      }
      else if ( field == QuoteField.Stock ){
        fTable.getColumnModel().getColumn(idx).setCellRenderer(new RenderStockName());
      }
    }
  }

  // TABLE MODEL 
  private final class QuoteTableModel extends AbstractTableModel {
    QuoteTableModel(){
      setColumnMapping(new ArrayList(fTablePrefsEditor.getColumnOrder()));
      //needed for startup: fQuoteTable can never be null, or the 
      //rendering of the table will throw NPE.
      fQuoteTable =  Collections.emptyList();
      fFilteredSortedQuoteTable = Collections.emptyList();
    }
    /**
    * @param aColumnMapping is a list of {@link QuoteField} 
    * objects, the order of which defines the presentation order of columns.
    */
    void setColumnMapping(List<QuoteField> aColumnMapping ){
      if (aColumnMapping == null || aColumnMapping.size() != QuoteField.values().length ){
        throw new IllegalArgumentException( "Column mapping null or of unexpected size.");
      }
      fColumnMapping = Collections.unmodifiableList(aColumnMapping);
      fireTableStructureChanged();
    }
    /**
    * Set the quotes which are to be displayed to the user. 
    */
    void setQuoteTable( List<Quote> aQuoteTable ){
      Args.checkForNull(aQuoteTable);
      fQuoteTable = Collections.unmodifiableList(aQuoteTable);
      filterAndSortQuotes();
    }
    @Override public String getColumnName(int aColumnIdx) { 
      return getField(aColumnIdx).toString();
    }
    @Override public Class<?> getColumnClass(int aColumnIdx) {
      Object value = getValueAt(0, aColumnIdx);
      return value.getClass();
    }
    @Override public int getColumnCount() {
      return fColumnMapping.size();
    }  
    @Override public int getRowCount() {
      return fFilteredSortedQuoteTable.size();
    }
    @Override public Object getValueAt(int aRowIdx, int aColumnIdx) {
      if (aRowIdx < 0 || aRowIdx > getRowCount()-1) {
        throw new IllegalArgumentException("Row index is out of range: " + aRowIdx);
      }
      if (aColumnIdx < 0 || aColumnIdx > getColumnCount()-1) {
        throw new IllegalArgumentException("Column index is out of range: " + aColumnIdx);
      }
      Quote quote = (Quote)fFilteredSortedQuoteTable.get(aRowIdx);
      //decide which field to grab based on configured mapping
      QuoteField field = getField(aColumnIdx);
      return getFieldValue( quote, field );
    }

    // PRIVATE

    /**
    * List containing {@link Quote} objects.
    *
    *<p>It is not compulsory that all items in <tt>fQuoteTable</tt> be displayed; 
    * if the the user has selected a particular filter, then only a portion of 
    * <tt>fQuoteTable</tt> will be displayed. 
    */
    private List<Quote> fQuoteTable;

    /**
    * The elements of <tt>fQuoteTable</tt> which the user wishes to display, and in 
    * the desired order.
    */
    private List<Quote> fFilteredSortedQuoteTable;

    /**
    * A list of {@link QuoteField} 
    * objects, the order of which defines the presentation order of columns.
    */
    private List<QuoteField> fColumnMapping;

    private void filterAndSortQuotes(){
      fFilteredSortedQuoteTable = fQuoteFilterFactory.getSelectedFilter().sift(fQuoteTable);
      Collections.sort(fFilteredSortedQuoteTable, getQuoteSorter());
      if (fTableSortIndicator.getSortBy().getOrder() == SortOrder.ASCENDING) {
        Collections.reverse(fFilteredSortedQuoteTable);
      }
      fireTableDataChanged();
    }
    
    private Comparator<Quote> getQuoteSorter(){
      SortBy sortBy = fTableSortIndicator.getSortBy();
      QuoteField quoteField = fModel.getField( sortBy.getColumn() );
      return QuoteSorterFactory.getSorter( quoteField );
    }
    
    private QuoteField getField( int aColumnIdx ){
      return (QuoteField)fColumnMapping.get(aColumnIdx);
    }

    private Object getFieldValue(Quote aQuote, QuoteField aField) {
      Object result = null;
      if ( aField == QuoteField.Stock ){
        //If default renderer used, then Object.toString generates the cell value.
        //If StockNameRenderer is used, then Stock.getName and Stock.getTicker are 
        //both used, in the cell value and tooltip.
        result = aQuote.getStock();
      }
      else if (aField == QuoteField.Price)  {
        result = aQuote.getPrice(); 
      }
      else if (aField == QuoteField.Change)  {
        result = aQuote.getChange(); 
      }
      else if (aField == QuoteField.PercentChange)  {
        result = aQuote.getPercentChange();
      }
      else if (aField == QuoteField.Profit) {
        result = aQuote.getProfit();
      }
      else if (aField == QuoteField.PercentProfit) {
        result = aQuote.getPercentProfit();
      }
      else {
        throw new AssertionError("Unknown field: " + aField);
      }
      return result;
    }
  }  
}
