package hirondelle.stocks.table;

import hirondelle.stocks.util.HashCodeUtil;
import hirondelle.stocks.util.EqualsUtil;

/** 
* Data-centric, immutable value class representing both the direction 
* of a sort and the index of its column.
*
* <P><tt>SortBy</tt> does not have any knowledge of table contents, so it may be 
* used with any sorted table.
*/
public final class SortBy  { 

  /**
  * Constructor. 
  *  
  * @param aSortOrder satisfies <tt>aSortOrder!=null</tt> and denotes ascending 
  * or descending order.
  * @param aColumn satisfies <tt>aColumn >= 0</tt> and is the index of a 
  * table column.
  */
  public SortBy(SortOrder aSortOrder, int aColumn) {
    fSortOrder = aSortOrder;
    fColumn = aColumn;
    validateState();
  }

  /**
  * Return the sense of the sort, either ascending or descending.
  */
  public SortOrder getOrder() {
    return fSortOrder;
  }
  
  /**
  * Return the column index identifying the sort, or {@link #NO_SELECTED_COLUMN}
  * in the case of {@link #NONE}. 
  */
  public int getColumn() {
    return fColumn;
  }
  
  /**
  * A special <tt>SortBy</tt> which represents the absence of any sort.
  */
  public static final SortBy NONE = new SortBy();
  
  /**
  * The special value of the column index used by {@link #NONE}. 
  */
  public static final int NO_SELECTED_COLUMN = -1;

  /**
  * Represent this object as a <tt>String</tt> - intended 
  * for logging purposes only.
  */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    String newLine = System.getProperty("line.separator");
    result.append( this.getClass().getName() );
    result.append(" {");
    result.append(newLine);

    result.append(" fSortOrder = ").append(fSortOrder).append(newLine);
    result.append(" fColumn = ").append(fColumn).append(newLine);
    
    result.append("}");
    result.append(newLine);
    return result.toString();
  }

  @Override public boolean equals( Object aThat ) {
    if ( this == aThat ) return true;
    if ( !(aThat instanceof SortBy) ) return false;
    SortBy that = (SortBy)aThat;
    return 
      EqualsUtil.areEqual(this.fSortOrder, that.fSortOrder) &&
      EqualsUtil.areEqual(this.fColumn, that.fColumn)
    ;
  }

  @Override public int hashCode() {
    int result = HashCodeUtil.SEED;
    result = HashCodeUtil.hash( result, fSortOrder );
    result = HashCodeUtil.hash( result, fColumn );
    return result;
  }

  // PRIVATE
  private final SortOrder fSortOrder;
  private final int fColumn;
  
  private void validateState() {
    boolean hasValidState = (fSortOrder!=null) && (fColumn >= 0);
    if ( !hasValidState ) throw new IllegalArgumentException(this.toString());
  }
  
  /**
  * Constructor used only for the special case of no sort at all.
  *
  * <P>Note that this constructor does not perform the validations done by the public 
  * constructor, and is thus not subject to the same restrictions.
  */
  private SortBy(){
    fSortOrder = SortOrder.DESCENDING;
    fColumn = NO_SELECTED_COLUMN;
  }
}