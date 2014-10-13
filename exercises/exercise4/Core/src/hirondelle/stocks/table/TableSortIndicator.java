package hirondelle.stocks.table;

import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.Args;

/**
* Places an up or down icon in a table column header, as an indicator of the 
* primary sort.
*
*<P>This class does not do any sorting of the underlying rows - it merely indicates
* the identity and direction of the sorted column.
*
* <P>The user changes the indicated sort by simply clicking on a column header.
* The initial click always indicates a descending sort. 
* A re-click on the same column will toggle the indicated direction. 
*
* <P>Listeners to this class are notified when the sort has changed, and 
* use {@link #getSortBy} to retrieve the new sort, and then perform the actual
* sorting. Example :
<pre>
  TableSortIndicator fSortIndicator = new TableSortIndicator(table, upIcon, downIcon);
  fSortIndicator.addObserver(this);
  //when the user clicks a column header, fSortIndicator will notify 
  //registered observers, who will call getSortBy to fetch the new sort.
  //..
  public void update(Observable aObservable, Object aData) {
    //extract column and (asc|desc) from fSortIndicator
    SortBy sortBy = fSortIndicator.getSortBy();
    //...perform the actual sorting
  }
</pre> 
* Instead of using a mouse click, the sort can be set programmatically as well; this 
* is useful for reflecting a sort selected through a preferences dialog. Example :
 <pre>
  TableSortIndicator fSortIndicator = new TableSortIndicator(table, upIcon, downIcon);
  fSortIndicator.addObserver(this);
  fSortIndicator.setSortBy(sortByPreference) ; //setSortBy calls the update method
</pre> 
*/
final class TableSortIndicator extends Observable {
  
  /**
  * Constructor.
  *  
  * @param aTable receives indication of table sort ; if it has any custom 
  * header renderers, they will be overwritten by this class.
  * @param aUpIcon placed in column header to indicate ascending sort.
  * @param aDownIcon placed in column header to indicate descending sort.
  */
  TableSortIndicator(JTable aTable, Icon aUpIcon, Icon aDownIcon) {
    Args.checkForNull(aUpIcon);
    Args.checkForNull(aDownIcon);
    
    fTable = aTable;
    fUpIcon = aUpIcon;
    fDownIcon = aDownIcon;
    fCurrentSort = SortBy.NONE;
    
    initHeaderClickListener();    
    initHeaderRenderers();
    assert getRenderer(0) != null : "Ctor - renderer 0 is null.";
  }
  
  /**
  * Return the identity of column having the primary sort, and the direction
  * of its sort.
  */
  SortBy getSortBy(){
    return fCurrentSort;
  }
  
  /**
  * Change the sort programmatically, instead of through a user click.
  *
  * <P>If there is a user preference for sort, it is passed to this method.
  * Notifies listeners of the change to the sort.
  */
  void setSortBy( SortBy aTargetSort ){
    validateIdx( aTargetSort.getColumn() );
    initHeaderRenderers();
    assert getRenderer(0) != null : "setSortBy - renderer 0 is null.";
    fTargetSort = aTargetSort;
    
    if ( fCurrentSort == SortBy.NONE ){
      setInitialHeader();
    }
    else if ( 
      fCurrentSort.getColumn() == fTargetSort.getColumn() && 
      fCurrentSort.getOrder() != fTargetSort.getOrder() 
    ) {
      toggleIcon();
    }
    else {
      updateTwoHeaders();
    }
    synchCurrentSortWithSelectedSort();
    notifyAndPaint();
  }
  
  // PRIVATE
  private final JTable fTable;
  private final Icon fUpIcon;
  private final Icon fDownIcon;
  
  /**
  * The sort as currently displayed to the user, representing the end result of a 
  * previous user request.
  */
  private SortBy fCurrentSort;
  
  /**
  * A new sort to be processed, whose origin is either a user preference or a 
  * a mouse click.
  *
  * Once fTargetSort is processed, fCurrentSort is assigned to fTargetSort.
  */
  private SortBy fTargetSort;
  
  private static final SortOrder fDEFAULT_SORT_ORDER = SortOrder.DESCENDING;
  
  /**
  * Return true only if the index is in the range 0..N-1, where N is the 
  * number of columns in fTable.
  */
  private boolean isValidColumnIdx(int aColumnIdx) {
    return 0 <= aColumnIdx && aColumnIdx <= fTable.getColumnCount()-1 ;
  }

  private void validateIdx(int aSelectedIdx) {
    if ( ! isValidColumnIdx(aSelectedIdx) ) {
      throw new IllegalArgumentException("Column index is out of range: " + aSelectedIdx);
    }
  }
  
  /**
  * Called both upon construction and by {@link #setSortBy}.
  *  
  * If fireTableStructureChanged is called, then the original headers are lost, and 
  * this method must be called in order to restore them.
  */
  private void initHeaderRenderers(){
    /*
    * Attach a default renderer explicitly to all columns. This is a 
    * workaround for the unusual fact that TableColumn.getHeaderRenderer returns 
    * null in the default case; Sun did this as an optimization for tables with 
    * very large numbers of columns. As well, there is only one default renderer
    * instance which is reused by each column.
    * See http://developer.java.sun.com/developer/bugParade/bugs/4276838.html for 
    * further information.
    */
    for (int idx=0; idx < fTable.getColumnCount(); ++idx) {
      TableColumn column = fTable.getColumnModel().getColumn(idx);
      column.setHeaderRenderer(new Renderer(fTable.getTableHeader()));
      assert column.getHeaderRenderer() != null : "Header Renderer is null";
    }
  }
  
  private Renderer getRenderer(int aColumnIdx) {
    TableColumn column = fTable.getColumnModel().getColumn(aColumnIdx);
    return (Renderer)column.getHeaderRenderer();
  }
  
  private void initHeaderClickListener() {
    fTable.getTableHeader().addMouseListener( new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent event) {
        int selectedIdx = fTable.getColumnModel().getColumnIndexAtX(event.getX());
        processClick( selectedIdx );
      }
    });
  }

  /**
  * Update the display of table headers to reflect a new sort, as indicated by a 
  * mouse click performed by the user on a column header.
  *
  * If <tt>aSelectedIdx</tt> is the column which already has the sort indicator, 
  * then toggle the indicator to its opposite state (up -> down, down -> up). 
  * If <tt>aSelectedIdx</tt> does not already display a sort indicator, then 
  * add a down indicator to it, and remove the indicator from the fCurrentSort 
  * column, if present.
  */
  private void processClick(int aSelectedIdx){
    validateIdx(aSelectedIdx);
    
    if (fCurrentSort.getColumn() == aSelectedIdx) {
      fTargetSort = new SortBy( fCurrentSort.getOrder().toggle(), aSelectedIdx);
    }
    else {
      fTargetSort = new SortBy(fDEFAULT_SORT_ORDER , aSelectedIdx);
    }
    
    if (fCurrentSort == SortBy.NONE){
      setInitialHeader();
    }
    if (fCurrentSort.getColumn() == fTargetSort.getColumn()) {
      toggleIcon();
    }
    else {
      updateTwoHeaders();
    }
    
    synchCurrentSortWithSelectedSort();
    notifyAndPaint();
  }

  private void notifyAndPaint(){
    setChanged();
    notifyObservers();
    fTable.getTableHeader().resizeAndRepaint();
  }
  
  private void setInitialHeader(){
    if (fTargetSort.getOrder() == SortOrder.DESCENDING){
      getRenderer(fTargetSort.getColumn()).setIcon(fDownIcon);
    }
    else {
      getRenderer(fTargetSort.getColumn()).setIcon(fUpIcon);
    }
  }

  /**
  * Flip the direction of the icon (up->down or down->up).
  */
  private void toggleIcon(){
    Renderer renderer = getRenderer(fCurrentSort.getColumn());
    if (fCurrentSort.getOrder() == SortOrder.ASCENDING) {
      renderer.setIcon(fDownIcon);
    }
    else {
      renderer.setIcon(fUpIcon);
    }
  }
  
  /**
  * Change the fCurrentSort column to having no icon, and change the fTargetSort 
  * column to having a down icon.
  */
  private void updateTwoHeaders() {
    getRenderer(fCurrentSort.getColumn()).setIcon(null);
    getRenderer(fTargetSort.getColumn()).setIcon(fDownIcon);
  }
  
  private void synchCurrentSortWithSelectedSort(){
    fCurrentSort = fTargetSort;
  }

  /**
  * Renders a column header with an icon.
  *
  * This class duplicates the default header behavior, but there does 
  * not seem to be any other option, since such an object does not seem
  * to be available from JTableHeader.
  */
  private final class Renderer extends DefaultTableCellRenderer {
    Renderer(JTableHeader aTableHeader){
      setHorizontalAlignment(JLabel.CENTER);
      setForeground(aTableHeader.getForeground());
      setBackground(aTableHeader.getBackground());
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      fTableHeader = aTableHeader;
    }
    @Override public Component getTableCellRendererComponent(
      JTable aTable, Object aValue, boolean aIsSelected,
      boolean aHasFocus, int aRowIdx, int aColumnIdx
    ) {    
      setText((aValue == null) ? Consts.EMPTY_STRING : aValue.toString());
      setFont(fTableHeader.getFont());
      return this;
    }
    private JTableHeader fTableHeader;
  }
}
