package hirondelle.stocks.table;

/**
* Enumeration for the fields of the 
* {@link hirondelle.stocks.quotes.Quote} class.
* 
* Advantages to using this class as part of a table model :
* <ul>
* <li> can parse text which maps table columns to fields
* <li> can be used for column names
* <li> length of <tt>QuoteField.values()</tt> gives the column count
* </ul>
*/
public enum QuoteField { 

  Stock("Stock"),
  Price("Price"),
  Change("Change"),
  PercentChange("%Change"),
  Profit("Profit"),
  PercentProfit("%Profit");

  /**
  * Return a text representation of the <tt>QuoteField</tt>.
  *
  * Return values : <tt>Stock, Price, Change, %Change, Profit, %Profit</tt>.
  * @return value contains only letters, and possibly a percent sign.
  */
  @Override public String toString() { 
    return fName;  
  }

  /** 
  * Parse text into a <tt>QuoteField</tt>.
  * 
  * <P>The text is matched according to the value of {@link #toString()}, 
  * not from the symbolic name of the enumerated item.
  */
  public static QuoteField valueFrom(String aText){
    for (QuoteField quoteField: values()){
      if(quoteField.toString().equals(aText)) {
        return quoteField;
      }
    }
    throw new IllegalArgumentException("Cannot parse into a QuoteField: " + aText);
  }
  
  private final String fName;

  /**
  * @param aName only letters and percent sign are valid characters.
  */
  private QuoteField(String aName) { 
    fName = aName;
  }
}