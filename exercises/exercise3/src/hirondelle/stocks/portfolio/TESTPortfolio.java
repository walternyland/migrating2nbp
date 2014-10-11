package hirondelle.stocks.portfolio;

import junit.framework.*;
import java.util.*;
import java.math.BigDecimal;
import hirondelle.stocks.quotes.Stock;
import hirondelle.stocks.quotes.Exchange;
import hirondelle.stocks.quotes.Quote;

/** Junit tests for {@link Portfolio}. */
public final class TESTPortfolio extends TestCase {

  /** Run the test cases.  */
  public static void main(String... aArgs) {
    String[] testCaseName = {TESTPortfolio.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }
  
  /** Constructor.  */
  public TESTPortfolio( String aName) {
    super(aName);
  }

  // TEST CASES 

  public void testSuccessfulConstruction(){
    try {
      Portfolio portfolio = new Portfolio("Blah", fStocks);
      Set<Stock> empty = Collections.emptySet(); 
      portfolio = new Portfolio("A", empty);
    }
    catch (Throwable ex) {
      fail("Ctor failed unexpectedly.");
    }
  }
  
  public void testFailedConstruction(){
    testFailedCtor(null, fStocks);
    testFailedCtor(" ", fStocks);
    testFailedCtor("", fStocks);
    testFailedCtor("blah", null);
  }
  
  public void testUntitledPortfolio(){
    Portfolio untitled = Portfolio.getUntitledPortfolio();
    assertTrue( untitled.getName().equals("") );
    assertTrue( untitled.getStocks().isEmpty() );
  }
  
  public void testChangeTitledToUntitled(){
    try {
      fPortfolio.setName("");
    }
    catch (Throwable ex) {
      return;
    }
    fail();
  }
  
  public void testChangeUntitledToTitled(){
    Portfolio untitled = Portfolio.getUntitledPortfolio();
    untitled.setName("Blah");
  }
  
  public void testIsUntitled(){
    Portfolio untitled = Portfolio.getUntitledPortfolio();
    assertTrue( untitled.isUntitled() );
    assertTrue( !fPortfolio.isUntitled() );
  }
  
  public void testBookValue(){
    assertTrue( fPortfolio.getBookValue(fQuotes).doubleValue() == 1000 );
    assertTrue( fPortfolio.getBookValue(fFilteredQuotes).doubleValue() == 600 );
    try {
      fPortfolio.getBookValue(fInvalidQuotes);
    }
    catch(Throwable ex) {
      return;
    }
    fail();
  }
  
  
  public void testCurrentValue(){
    assertTrue( fPortfolio.getCurrentValue(fQuotes).doubleValue() == 2000 );
    assertTrue( fPortfolio.getCurrentValue(fFilteredQuotes).doubleValue() == 1200 );
    try {
      fPortfolio.getCurrentValue(fInvalidQuotes);
    }
    catch(Throwable ex) {
      return;
    }
    fail();
  }
  
  public void testProfit(){
    assertTrue( fPortfolio.getProfit(fQuotes).doubleValue() == 1000 );
    assertTrue( fPortfolio.getProfit(fFilteredQuotes).doubleValue() == 600 );
    try {
      fPortfolio.getProfit(fInvalidQuotes);
    }
    catch(Throwable ex) {
      return;
    }
    fail();
  }
  
  public void testPercentageProfit(){
    Stock ctr = new Stock("Blah", "CTR", fTSE, 0, new BigDecimal("0.00") );
    Set<Stock> stocks = new TreeSet<Stock>();
    stocks.add( ctr );
    Portfolio portfolio = new Portfolio("blah", stocks);
    Collection<Quote> quotes = new ArrayList<>();
    quotes.add( new Quote(ctr, new BigDecimal("12.00"), new BigDecimal("-0.25")) );
    assertTrue( portfolio.getPercentageProfit(quotes).longValueExact() == 0 );
    
    assertTrue( fPortfolio.getPercentageProfit(fQuotes).longValueExact() == 1 );
    assertTrue( fPortfolio.getPercentageProfit(fFilteredQuotes).longValue() == 1 );
    try {
      fPortfolio.getPercentageProfit(fInvalidQuotes);
    }
    catch(Throwable ex) {
      return;
    }
    fail();
  }
  
  public void testGetStocksUnmodifiable(){
    Collection<Stock> stocks = fPortfolio.getStocks();
    try {
      stocks.clear();
    }
    catch (UnsupportedOperationException ex) {
      return;
    }
    fail("Stocks should be unmodifiable.");
  }
  
  public void testModifyStocks(){
    assertTrue( ! fPortfolio.getStocks().isEmpty() );
    TreeSet<Stock> workingCopy = new TreeSet<Stock>( fPortfolio.getStocks() );
    workingCopy.clear();
    fPortfolio.setStocks(workingCopy);
    assertTrue( fPortfolio.getStocks().isEmpty() );
  }

  protected void setUp(){ }

  protected void tearDown() {  }

  // PRIVATE  //
  private static final Set<Stock> fStocks;
  private static final Collection<Quote> fQuotes;
  private static final Collection<Quote> fFilteredQuotes;
  private static final Collection<Quote> fInvalidQuotes;
  private static final Stock fSunw;
  private static final Stock fMsft;
  private static final Exchange fNasdaq = Exchange.valueFrom("Nasdaq Stock Exchange");
  private static final Exchange fTSE = Exchange.valueFrom("Toronto Stock Exchange");
  private static final Exchange fNYSE = Exchange.valueFrom("NYSE Stock Exchanges");
  static {
    fSunw = new Stock("Sun Microsystems", "SUNW", fNasdaq, 100, new BigDecimal("4"));
    fMsft = new Stock("Microsoft", "MSFT", fNasdaq, 100, new BigDecimal("6") );
               
    fStocks = new TreeSet<Stock>();
    fStocks.add(fSunw);
    fStocks.add(fMsft);
    
    fQuotes = new ArrayList<>();
    fQuotes.add( new Quote(fSunw, new BigDecimal("8"), new BigDecimal("1")) );
    fQuotes.add( new Quote(fMsft, new BigDecimal("12"), new BigDecimal("1")) );
    fFilteredQuotes = new ArrayList<>();
    fFilteredQuotes.add( new Quote(fMsft, new BigDecimal("12"), new BigDecimal("1")) );
    fInvalidQuotes = new ArrayList<>();
    fInvalidQuotes.add( new Quote(fSunw, new BigDecimal("8"), new BigDecimal("1")));
    fInvalidQuotes.add( new Quote(fMsft, new BigDecimal("12"), new BigDecimal("1")) );
    Stock ibm = new Stock("IntlBM", "IBM", fNYSE, 100, new BigDecimal("10.00") );
    fInvalidQuotes.add( new Quote(ibm, new BigDecimal("12"), new BigDecimal("1")) );
  }
  private Portfolio fPortfolio = new Portfolio("Test", fStocks);
  
  private void testFailedCtor(String aName, Set<Stock> aStocks){
    boolean hasSucceeded = true;
    try {
      Portfolio portfolio = new Portfolio(aName, aStocks);
    }
    catch(Throwable ex) {
      hasSucceeded = false;
    }
    if( hasSucceeded ) fail("Ctor unexpectedly succeeded.");
  }
}