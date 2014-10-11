package hirondelle.stocks.quotes;

import java.math.BigDecimal;

import hirondelle.stocks.util.HashCodeUtil;
import hirondelle.stocks.util.EqualsUtil;
import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.Consts;

/** 
* Data-centric, immutable value class which represents the 
* dynamic price data attached to a {@link Stock}.
*/
public final class Quote  { 
  
  /**
  * Constructor.
  *  
  * @param aStock satisfies <tt>aStock!=null</tt> 
  * @param aCurrentPrice current price of <tt>aStock</tt>,and 
  * satisfies <tt>aPrice!=null && aPrice>=0</tt> 
  * @param aChange current price less the opening price of <tt>aStock</tt>, 
  * and satisfies <tt>aChange!=null</tt> 
  */
  public Quote(Stock aStock, BigDecimal aCurrentPrice, BigDecimal aChange) {
    fStock = aStock;
    fCurrentPrice = aCurrentPrice;
    fChange = aChange;
    validateState();
  }

  /**
  * Return the percentage change between the opening price and the 
  * current price; if {@link #getPrice} returns 0, then this method returns 0.
  *
  * <P>Example: current price=1.00 and change=0.20, returns a value of 25.00.
  */
  public BigDecimal getPercentChange(){
    BigDecimal result = new BigDecimal("0.00");
    if( ! Util.isZeroMoney(getPrice()) ){
      result = getChange().divide(getOpeningPrice(), EXTRA_DECIMALS, ROUNDING_MODE);
      result = rounded(result.multiply(HUNDRED));
    }
    return result;
  }
  
  /**
  * Return the product of {@link Stock#getNumShares} and {@link #getPrice}.
  * 
  * Represents the current value of the holding, using the latest available 
  * price quote.
  */
  public BigDecimal getCurrentValue(){
    BigDecimal numShares = new BigDecimal( getStock().getNumShares().toString() );
    return  numShares.multiply( getPrice() );
  }
  
  /**
  * Return {@link #getCurrentValue} less {@link Stock#getBookValue}.
  */
  public BigDecimal getProfit(){
    return getCurrentValue().subtract( getStock().getBookValue() );
  }
  
  /**
  * Return {@link #getProfit} divided by {@link Stock#getBookValue}, multiplied 
  * by 100; if {@link Stock#getBookValue} or {@link #getPrice} returns 0,
  * then this method returns 0.
  */
  public BigDecimal getPercentProfit(){
    BigDecimal result = new BigDecimal("0.00");
    BigDecimal bookValue = getStock().getBookValue();
    if ( ! Util.isZeroMoney(bookValue) && ! Util.isZeroMoney(getPrice()) )  {
      result = getProfit().divide(bookValue, EXTRA_DECIMALS, ROUNDING_MODE);
      result = rounded(result.multiply(HUNDRED));
    }
    return result;
  }

  /**
  * Return the <tt>aStock</tt> passed to the constructor.
  */
  public Stock getStock() {
    return fStock;
  }

  /**
  * Return the <tt>aPrice</tt> passed to the constructor.
  */
  public BigDecimal getPrice() {
    return fCurrentPrice;
  }

  /**
  * Return the <tt>aChange</tt> passed to the constructor.
  */
  public BigDecimal getChange() {
    return fChange;
  }

  /**
  * Represent this object as a String - intended for logging purposes only.
  */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    String newLine = System.getProperty("line.separator");
    result.append( this.getClass().getName() );
    result.append(" {");
    result.append(newLine);

    result.append("Stock: ").append(fStock).append(newLine);
    result.append("Opening Price: ").append(getOpeningPrice()).append(newLine);
    result.append("Current Price: ").append(fCurrentPrice).append(newLine);
    result.append("Change: ").append(fChange).append(newLine);
    result.append("%Change: ").append(getPercentChange()).append(newLine);
    result.append("%Profit: ").append(getPercentProfit()).append(newLine);
    
    result.append("}");
    result.append(newLine);
    return result.toString();
  }

  @Override public boolean equals(Object aThat) {
    if ( this == aThat ) return true;
    if ( !(aThat instanceof Quote) ) return false;
    Quote that = (Quote)aThat;
    return 
      EqualsUtil.areEqual(this.fStock, that.fStock) &&
      EqualsUtil.areEqual(this.fCurrentPrice, that.fCurrentPrice) &&
      EqualsUtil.areEqual(this.fChange, that.fChange)
    ;
  }

  @Override public int hashCode() {
    int result = HashCodeUtil.SEED;
    result = HashCodeUtil.hash(result, fStock);
    result = HashCodeUtil.hash(result, fCurrentPrice);
    result = HashCodeUtil.hash(result, fChange);
    return result;
  }

  // PRIVATE  
  private final Stock fStock;
  private final BigDecimal fCurrentPrice;
  private final BigDecimal fChange;
  private static final int DECIMALS = 2;
  private static int EXTRA_DECIMALS = 4;
  private static final int ROUNDING_MODE = BigDecimal.ROUND_HALF_EVEN;
  private static BigDecimal HUNDRED = new BigDecimal("100");
  private static final BigDecimal ZERO = Consts.ZERO_MONEY_WITH_DECIMAL;
  
  private void validateState() {
    boolean hasValidState = 
      (fStock!=null) &&
      (fCurrentPrice!=null &&  fCurrentPrice.compareTo(ZERO) >= 0) &&
      (fChange!=null)
    ;
    if ( !hasValidState ) throw new IllegalArgumentException(this.toString());
  }
  
  private BigDecimal getOpeningPrice(){
    return getPrice().subtract( getChange() );  
  }
  
  private BigDecimal rounded(BigDecimal aNumber){
    return aNumber.setScale(DECIMALS, ROUNDING_MODE);
  }  
  
  private static void main(String... aArgs){
    Exchange NYSE = Exchange.valueFrom("NYSE Stock Exchanges");
    Stock stock = new Stock("Blah", "BLA", NYSE, 122, new BigDecimal("88"));
    Quote quote = new Quote(stock, new BigDecimal("118.53"), new BigDecimal("-0.17")); 
    System.out.println(quote);
  }
}