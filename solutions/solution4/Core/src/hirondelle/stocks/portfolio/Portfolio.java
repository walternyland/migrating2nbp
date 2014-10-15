package hirondelle.stocks.portfolio;

import java.util.*;
import java.math.BigDecimal;

import hirondelle.stocks.util.HashCodeUtil;
import hirondelle.stocks.util.EqualsUtil;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.quotes.QuotesDAO;
import hirondelle.stocks.util.DataAccessException;
import hirondelle.stocks.quotes.Quote;
import hirondelle.stocks.quotes.Stock;

/** 
* Represents a uniquely-named set of {@link Stock} objects,
* in which the user has an interest in monitoring.
*
*<P>Methods in this class which take a <tt>Collection</tt> of {@link Quote} 
* objects as a parameter 
*<ul>
* <li>treat the <tt>Collection</tt> as a filter :
* if a <tt>Stock</tt> in this <tt>Portfolio</tt> is not present 
* in the <tt>aQuotes</tt> param, then that <tt>Stock</tt> will 
* not contribute to the result of such a method. 
* <li> the <tt>Quote</tt> collection must only contain <tt>Quote</tt>s 
* which correspond to a <tt>Stock</tt> known to this portfolio.
*</ul>
*
* <P>Here is an example illustrating how to change the stocks contained in the 
* portfolio :
<pre>
fWorkingCopy = new TreeSet(fCurrentPortfolio.getStocks());
//..edit the contents of fWorkingCopy
fCurrentPortfolio.setStocks(fWorkingCopy);
</pre> 
* Note that edits to <tt>fWorkingCopy</tt> can 
* be easily abandoned, without affecting <tt>fCurrentPortfolio</tt>.
*/
public final class Portfolio  { 

  /**
  * Constructor.
  *  
  * @param aName is the unique identifier for this <tt>Portfolio</tt>, has 
  * visible content  
  * @param aStocks is possibly-empty, and represents a reference to an 
  * object which is shared with the caller; no defensive copy is made.
  */
  public Portfolio(String aName, Set<Stock> aStocks) {
    setName(aName);
    setStocks(aStocks);
  }

  /**
  * Return a <tt>Portfolio</tt> which contains no stocks, and whose title
  * is an empty <tt>String</tt>. 
  *
  * <P>An untitled <tt>Portfolio</tt> is a special object, which cannot be created either 
  * directly (by using the constructor), nor indirectly (by using the constructor and 
  * changing object state). These special objects are only obtained through this 
  * method. 
  */
  public static Portfolio getUntitledPortfolio(){
    return new Portfolio();
  }
  
  /**
  * Return <tt>true</tt> only if the name of this <tt>Portfolio</tt> is an 
  * empty <tt>String</tt>.
  */
  public boolean isUntitled(){
    return getName().equals(UNTITLED);
  }

  /**
  * Return the unique name of this <tt>Portfolio</tt>.
  *
  * @return possibly-empty <tt>String</tt>.
  */
  public String getName(){
    return fName;
  }

  /**
  * Set the unique name of this <tt>Portfolio</tt>.
  *
  * @param aName see {@link #Portfolio(String, Set)} for conditions on aName.
  */
  public void setName( String aName ){
    Args.checkForContent(aName);
    fName = aName;
  }

  /**
  * Return the {@link Stock} objects contained in this <tt>Portfolio</tt>.
  *
  * <P>The returned value is unmodifiable. See class description for example of 
  * of changing the <tt>Stock</tt>s in a <tt>Portfolio</tt>.
  */
  public Set<Stock> getStocks() {
    return fStocks;
  }

  /**
  * Replace the {@link Stock} objects contained in this <tt>Portfolio</tt>.
  *
  * @param aStocks see {@link #Portfolio(String, Set)} for conditions on aStocks.
  */
  public void setStocks( Set<Stock> aStocks ){
    Args.checkForNull(aStocks);
    fStocks = Collections.unmodifiableSet(aStocks);
  }

  /**
  * Return {@link Quote} objects, one for each <tt>Stock</tt> in this 
  * <tt>Portfolio</tt>.
  */
  public List<Quote> getQuotes() throws DataAccessException {
    QuotesDAO quotesDAO = new QuotesDAO(QuotesDAO.UseMonitor.TRUE, fStocks);
    return quotesDAO.getQuotes();
  }

  /**
  * Return the cost of acquisition of all items in this<tt>Portfolio</tt> which 
  * also appear in <tt>aQuotes</tt>.
  *  
  * Each {@link Stock} in <tt>aQuotes</tt> contributes the value
  * {@link Stock#getBookValue}.
  *
  * @param aQuotes is a possibly-filtered collection of {@link Quote} objects; see 
  * {@link hirondelle.stocks.table.QuoteFilter}; each <tt>Quote</tt> object 
  * corresponds to a <tt>Stock</tt> known to this <tt>Portfolio</tt>.
  * @return a value greater than or equal to <tt>0.00</tt>.
  */
  public BigDecimal getBookValue(Collection<Quote> aQuotes){
    BigDecimal result = new BigDecimal("0.00");
    for (Quote quote: aQuotes) {
      Stock stock = quote.getStock();
      if ( ! fStocks.contains(stock) ) {
        throw new IllegalArgumentException("Unknown stock: " + stock);
      }
      result = result.add( stock.getBookValue() );
    }
    return result.setScale(Consts.MONEY_DECIMAL_PLACES, Consts.MONEY_ROUNDING_STYLE);
  }
  
  /**
  * Return the current worth of all items in the <tt>Portfolio</tt> which 
  * also appear in <tt>aQuotes</tt>.
  * 
  * Each {@link Quote} contributes the value {@link Quote#getCurrentValue}.
  *
  * @param aQuotes is a possibly-filtered collection of {@link Quote} objects; see 
  * {@link hirondelle.stocks.table.QuoteFilter}.
  * @return a value greater than or equal to <tt>0.00</tt>.
  */
  public BigDecimal getCurrentValue(Collection<Quote> aQuotes){
    BigDecimal result = new BigDecimal("0.00");
    for(Quote quote : aQuotes) {
      Stock stock = quote.getStock();
      if ( ! fStocks.contains(stock) ) {
        throw new IllegalArgumentException("Unknown stock: " + stock);
      }
      result = result.add(quote.getCurrentValue());
    }
    return result.setScale(Consts.MONEY_DECIMAL_PLACES, Consts.MONEY_ROUNDING_STYLE);
  }
  
  /**
  * Return {@link #getCurrentValue} less {@link #getBookValue}.
  *
  * @param aQuotes is a possibly-filtered collection of {@link Quote} objects; see 
  * {@link hirondelle.stocks.table.QuoteFilter}.
  * @return value is positive for a profit, and negative for a loss.
  */
  public BigDecimal getProfit(Collection<Quote> aQuotes){
    return getCurrentValue(aQuotes).subtract( getBookValue(aQuotes) );
  }

  /**
  * Return {@link #getProfit} divided by {@link #getBookValue}; if the book value 
  * is zero, then return zero.
  *
  * @param aQuotes is a possibly-filtered collection of {@link Quote} objects; see 
  * {@link hirondelle.stocks.table.QuoteFilter}.
  */
  public BigDecimal getPercentageProfit(Collection<Quote> aQuotes){
    BigDecimal result = Consts.ZERO_MONEY;
    BigDecimal bookValue = getBookValue(aQuotes);
    if (bookValue.compareTo(Consts.ZERO_MONEY) != 0){
      BigDecimal profit = getProfit(aQuotes);
      result = profit.divide(bookValue, Consts.MONEY_ROUNDING_STYLE);
    }
    return result;
  }

  /**
  * Represent this object as a <tt>String</tt> - intended for debugging 
  * purposes only.
  */
  @Override public String toString() {
    StringBuilder result = new StringBuilder();
    String newLine = System.getProperty("line.separator");
    result.append( this.getClass().getName() );
    result.append(" {");
    result.append(newLine);

    result.append(" fName = ").append(fName).append(newLine);
    result.append(" fStocks = ").append(fStocks).append(newLine);
    
    result.append("}");
    result.append(newLine);
    return result.toString();
  }

  @Override public boolean equals( Object aThat ) {
    if ( this == aThat ) return true;
    if ( !(aThat instanceof Portfolio) ) return false;
    Portfolio that = (Portfolio)aThat;
    return 
      EqualsUtil.areEqual(this.fName, that.fName) &&
      EqualsUtil.areEqual(this.fStocks, that.fStocks)
    ;
  }

  @Override public int hashCode() {
    int result = HashCodeUtil.SEED;
    result = HashCodeUtil.hash(result, fName);
    result = HashCodeUtil.hash(result, fStocks);
    return result;
  }

  // PRIVATE 
  private String fName;
  private Set<Stock> fStocks;
  
  /**
  * This String will never conflict with a Portfolio name input by an end user, 
  * since those names must have a non-zero trimmed length, as enforced through the
  * public constructor.
  */
  private static final String UNTITLED = Consts.EMPTY_STRING;

  /**
  * Constructor allows for the creation of a special case, that of an 
  * untitled <tt>Portfolio</tt>, whose name is an empty String, and whose
  * Set of Stock objects is an empty Set.
  *
  * Note that this special case can only be constructed internally, and not 
  * through the public constructor; the public constructor's validations are
  * more restrictive. As well, the special case cannot be created in steps, through 
  * altering the state of an object returned by the public constructor.
  *
  * @see #getUntitledPortfolio
  */
  private Portfolio() {
    fName = UNTITLED; 
    setStocks( new TreeSet<Stock>() );
  }
}
