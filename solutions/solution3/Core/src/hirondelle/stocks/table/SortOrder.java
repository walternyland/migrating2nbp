package hirondelle.stocks.table;

/** 
* Enumeration class for the two directions which a sort may take.
*/
public enum SortOrder  {  

  DESCENDING("Descending"),
  ASCENDING("Ascending");

  @Override public String toString() { 
    return fName;  
  } 

  /**
  * Return the opposite <tt>SortOrder</tt> from <tt>this</tt> one.
  */
  public SortOrder toggle(){
    return (this == ASCENDING ? DESCENDING : ASCENDING);
  }
  
  // PRIVATE
  private final String fName;
  private SortOrder(String aName){
    fName = aName;
  }
}