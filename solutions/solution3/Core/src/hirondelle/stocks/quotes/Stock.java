package hirondelle.stocks.quotes;

import java.text.StringCharacterIterator;
import java.util.*;
import java.util.logging.*;
import java.math.BigDecimal;
import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.HashCodeUtil;
import hirondelle.stocks.util.EqualsUtil;;

/** 
* Data-centric, immutable value class which encapsulates items 
* related to a stock.
*
* <P>All permitted values are specified by the constructor.
*/
public final class Stock implements Comparable<Stock> { 

  /**
  * @param aName has visible content, and is the 
  * full name of the stock.
  * @param aTicker non-null, contains only letters, periods, underscores, and 
  * the ^ character, and must have a length in the range 1..20 inclusive ;
  * it is the ticker symbol used by <tt>aExchange</tt>, and does not include the 
  * suffix used by Yahoo to identify the exchange.
  * @param aExchange is not <tt>null</tt> 
  * @param aNumShares is not <tt>null</tt>, and represents a 
  * holding as input by the user (and usually reflects their actual holding).
  * @param aAveragePrice is not null, satisfies 
  * <tt>!aAveragePrice.doubleValue<0</tt>, 
  * and equals the average price paid by the user for <tt>aNumShares</tt>.
  */
  public Stock (
    String aName,
    String aTicker,
    Exchange aExchange,
    Integer aNumShares,
    BigDecimal aAveragePrice
  ) {
    fName = aName;
    fTicker = aTicker;
    fExchange = aExchange;
    fNumShares = aNumShares;
    fAveragePrice = aAveragePrice;
    validateState();
  }

  /**
  * Parse a result of {@link #toString} into a <tt>Stock</tt> object.
  *
  * @param aStockText is in the format defined by {@link #toString}.
  */
  public Stock valueOf(String aStockText) {
    StringTokenizer parser = new StringTokenizer(aStockText, FIELD_DELIMITER);
    try {
      String ticker = parser.nextToken();
      String name = parser.nextToken();
      Exchange exchange = Exchange.valueFrom(parser.nextToken());
      Integer numShares = Integer.valueOf(parser.nextToken());
      BigDecimal avgPrice = new BigDecimal(parser.nextToken());
      return new Stock(name, ticker, exchange, numShares, avgPrice);
    }
    catch (NoSuchElementException ex){
      fLogger.severe("Cannot parse into Stock object: \"" + aStockText + "\"");
      throw ex;
    }
  }

  /**
  * Format this object as text which can be used as input to {@link #valueOf}.
  *
  * The return value is used for persistence of <tt>Stock</tt> objects. 
  *
  * <P>The format is shown by this example:<br>
  * <tt>SUNW:Sun Microsystems:Nasdaq Stock Exchange:100:46.25</tt><br>
  * That is:<BR>
  * <tt>Ticker:Name:Exchange:NumShares:AveragePrice</tt>
  */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    result.append(fTicker).append(FIELD_DELIMITER);
    result.append(fName).append(FIELD_DELIMITER);
    result.append(fExchange).append(FIELD_DELIMITER);
    result.append(fNumShares).append(FIELD_DELIMITER);
    result.append(fAveragePrice);
    return result.toString();
  }

  /**
  * Validate user input to a fine-grained level. 
  * 
  * <P>All parameters other than <tt>aErrorMessages</tt> may take any value whatsoever. 
  * All parameters other than <tt>aErrorMessages</tt> are examined, and every 
  * invalid parameter will cause a corresponding error message to be added to 
  * <tt>aErrorMessages</tt>. Valid parameters have no corresponding message. 
  *
  * <P>All criteria for validity are identical with those of the constructor.
  *
  * <P>This method is intended to help validate textual user input into a dialog, before 
  * any attempt is made to call the constructor of this class. In the event of an 
  * error, the associated error messages can be displayed to the user. 
  *
  * @param aErrorMessages is an out parameter, in which an error message is 
  * placed for each problem found; it must be non-null and empty.
  */
  public static boolean isValidInput(
    List<String> aErrorMessages, 
    String aName, 
    String aTicker, 
    Exchange aExchange, 
    Integer aNumShares, 
    BigDecimal aAvgPrice
  ){
    if (aErrorMessages == null) {
      throw new IllegalArgumentException("List for error messages must be non-null.");
    }
    if ( !aErrorMessages.isEmpty() ) {
      throw new IllegalArgumentException("List for error messages must be initially empty.");
    }
    
    if ( ! isValidName(aName) ) {
      aErrorMessages.add("Name must have content.");
    }
    if ( ! isValidTicker(aTicker) ) {
      String message = 
        "Ticker symbols must have 1..20 characters, " + 
        "which are only letters, periods, underscores, and ^."
      ;
      aErrorMessages.add(message);
    }
    if ( ! isValidExchange(aExchange) ) {
      aErrorMessages.add("An exchange must be selected.");
    }
    if ( ! isValidNumShares(aNumShares) ) {
      aErrorMessages.add("Quantity must not be null.");
    }      
    if ( ! isValidAveragePrice(aAvgPrice) ) {
      aErrorMessages.add("Average price must be zero or positive.");
    }
    return aErrorMessages.isEmpty();
  }
  
  public String getName() {
    return fName;
  }

  public String getTicker() {
    return fTicker;
  }

  public Exchange getExchange() {
    return fExchange;
  }

  public Integer getNumShares() {
    return fNumShares;
  }

  public BigDecimal getAveragePrice() {
    return fAveragePrice;
  }

  /**
  * Return the product of {@link #getNumShares} and {@link #getAveragePrice}.
  *
  * <P>The book value of a security is simply the cost of its acquisition.
  */
  public BigDecimal getBookValue(){
    BigDecimal numShares = new BigDecimal(getNumShares().toString());
    return numShares.multiply(getAveragePrice()) ;
  }

  /**
  * Return <tt>true</tt> only if the ticker represents an index such as 
  * the Dow Jones or the Standard and Poor 500 , as opposed to a regular stock.
  *
  * <P>In the Yahoo system, an index always begins with the ^ character.
  */
  public boolean isIndex(){
    return fTicker.startsWith("^");
  }
  
  @Override public boolean equals(Object aThat) {
    if ( this == aThat ) return true;
    if ( !(aThat instanceof Stock) ) return false;
    Stock that = (Stock)aThat;
    return
      EqualsUtil.areEqual(this.fName, that.fName) &&
      EqualsUtil.areEqual(this.fTicker, that.fTicker) &&
      EqualsUtil.areEqual(this.fExchange, that.fExchange) &&
      EqualsUtil.areEqual(this.fNumShares, that.fNumShares) &&
      EqualsUtil.areEqual(this.fAveragePrice, that.fAveragePrice)
    ;
  }

  @Override public int hashCode() {
    int result = HashCodeUtil.SEED;
    result = HashCodeUtil.hash(result, fName);
    result = HashCodeUtil.hash(result, fTicker);
    result = HashCodeUtil.hash(result, fExchange);
    result = HashCodeUtil.hash(result, fNumShares);
    result = HashCodeUtil.hash(result, fAveragePrice);
    return result;
  }

  @Override public int compareTo (Stock aThat) {
    final int BEFORE = -1;
    final int EQUAL = 0;
    final int AFTER = 1;
    
    if ( this == aThat ) return EQUAL;

    if (!this.isIndex() && aThat.isIndex()) return AFTER;
    if (this.isIndex() && !aThat.isIndex()) return BEFORE;
    
    int comparison = this.fName.compareTo(aThat.fName);
    if ( comparison != EQUAL ) return comparison;    
    
    comparison = this.fTicker.compareTo(aThat.fTicker);
    if ( comparison != EQUAL ) return comparison;
    
    comparison = this.fExchange.compareTo(aThat.fExchange);
    if ( comparison != EQUAL ) return comparison;
    
    comparison = this.fNumShares.compareTo(aThat.fNumShares);
    if ( comparison != EQUAL ) return comparison;
    
    comparison = this.fAveragePrice.compareTo(aThat.fAveragePrice);
    if ( comparison != EQUAL ) return comparison;
 
    //all comparisons have yielded equality
    //verify that compareTo is consistent with equals (optional)
    assert this.equals(aThat) : "compareTo inconsistent with equals.";
    
    return EQUAL;
  }
  
  // PRIVATE
  
  private final String fName;
  private final String fTicker;
  private final Exchange fExchange;
  private final Integer fNumShares;
  private final BigDecimal fAveragePrice;
  private static final Logger fLogger = Util.getLogger(Stock.class);

  /**
  * Separates the various Stock fields as expressed in toString. 
  */
  private static final String FIELD_DELIMITER = ":";
  
  private void validateState(){
    boolean hasValidState = 
      isValidName(fName) &&
      isValidTicker(fTicker) &&
      isValidExchange(fExchange) &&
      isValidNumShares(fNumShares) &&
      isValidAveragePrice(fAveragePrice)
    ;
    if ( ! hasValidState ) {
      throw new IllegalArgumentException(this.toString());
    }
  }
  
  private static boolean isValidName(String aName) {
    return Util.textHasContent(aName);
  }
  
  private static boolean isValidTicker(String aName){
    if ( aName == null ) return false;
    String name = aName.trim();
    if ( name.length() == 0 || name.length()>20 ) return false;

    boolean result = true;
    StringCharacterIterator iterator = new StringCharacterIterator(aName);  
    char sCharacter =  iterator.current();
    while (sCharacter != StringCharacterIterator.DONE ){
      if ( 
        Character.isLetter(sCharacter) || 
        sCharacter == '.' ||
        sCharacter == '_' ||
        sCharacter == '^'
      ){
        //do nothing
      }
      else {
        result = false;
      }
      sCharacter = iterator.next();
    }
    return result;    
  }
  
  private static boolean isValidExchange(Exchange aExchange) {
    return aExchange != null;
  }
  
  private static boolean isValidNumShares(Integer aNumShares){
    return aNumShares != null;
  }
  
  private static boolean isValidAveragePrice( BigDecimal aAvgPrice) {
    return aAvgPrice != null && !(aAvgPrice.doubleValue()<0);
  }
}