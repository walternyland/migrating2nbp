package hirondelle.stocks.quotes;

import junit.framework.*;
import java.math.BigDecimal;

/** JUnit tests for {@link Quote}.*/
public final class TESTQuote extends TestCase {

  /** Run the test cases.  */
   public static void main(String... aArgs) {
     String[] testCaseName = { TESTQuote.class.getName()};
     junit.textui.TestRunner.main(testCaseName);
  }

  public TESTQuote( String aName) {
   super( aName );
  }

  // TEST CASES //
  
  public void testSuccessfulConstruction(){
    try{
      Quote quote = new Quote(fStock, fOneDollar, fTenCentsDown);
      quote = new Quote(fStock, new BigDecimal("0.01"), fTenCentsUp );
      quote = new Quote(fStock, fOneDollar, new BigDecimal("0") );
      quote = new Quote(fStock, new BigDecimal("0"), new BigDecimal("0") );
      quote = new Quote(fStock, new BigDecimal("0.00"), new BigDecimal("0") );
    }
    catch(Throwable ex) {
      fail("Ctor failed unexpectedly.");
    }
  }
  
  public void testFailedConstruction(){
    testFailedCtor(null, fOneDollar, fTenCentsDown);
    testFailedCtor(fStock, null, fTenCentsDown);
    testFailedCtor(fStock, new BigDecimal("-0.01"), fTenCentsDown);
    testFailedCtor(fStock, fOneDollar, null);
  }
  
  public void testPercentChange(){
    Quote quote = new Quote(fStock, fOneDollar, new BigDecimal("0.20"));
    assertTrue( quote.getPercentChange().equals(new BigDecimal("25.00")));
    
    BigDecimal ZERO = new BigDecimal("0.00");
    quote = new Quote(fStock, fOneDollar, new BigDecimal("0.0"));
    assertTrue( quote.getPercentChange().equals(ZERO));
    
    quote = new Quote(fStock, new BigDecimal("0.80"), new BigDecimal("-0.20"));
    assertTrue( quote.getPercentChange().equals(new BigDecimal("-20.00")));
    
    quote = new Quote(fStock, new BigDecimal("0.00"), new BigDecimal("-0.20"));
    assertTrue( quote.getPercentChange().equals(ZERO));
    
    quote = new Quote(fStock, new BigDecimal("0"), new BigDecimal("-0.20"));
    assertTrue( quote.getPercentChange().equals(ZERO));
    
    quote = new Quote(fStock, new BigDecimal("118.53"), new BigDecimal("-0.17"));
    assertTrue( quote.getPercentChange().equals(new BigDecimal("-0.14")));
    
  }
  
  public void testCurrentValue(){
    assertTrue( fQuote.getCurrentValue().doubleValue() == 100 );
    
    Stock stock = new Stock("Blah", "BLA", fNYSEStockExchanges, 0, new BigDecimal("0"));
    Quote quote = new Quote(stock, fOneDollar, fTenCentsDown);
    assertTrue( quote.getCurrentValue().doubleValue() == 0 );
    
    stock = new Stock("Blah", "BLA", fNYSEStockExchanges, 122, new BigDecimal("88.00"));
    quote = new Quote(stock, new BigDecimal("118.53"), new BigDecimal("-0.17"));
    assertTrue( quote.getCurrentValue().equals(new BigDecimal("14460.66")) );
    
  }
  
  public void testProfit(){
    assertTrue( fQuote.getProfit().doubleValue() == -425 );
    Quote quote = new Quote(fStock, new BigDecimal("5.25"), fTenCentsDown);
    assertTrue( quote.getProfit().doubleValue() == 0 );
    
    Stock stock = new Stock(
      "Blah", "BLA", fNYSEStockExchanges, 122, new BigDecimal("88.00")
    );
    quote = new Quote(stock, new BigDecimal("118.53"), new BigDecimal("-0.17"));
    assertTrue( quote.getProfit().equals(new BigDecimal("3724.66")));
    
  }
  
  public void testPercentProfit(){
    BigDecimal ZERO = new BigDecimal("0.00");
    
    Quote quote = new Quote(fStock, new BigDecimal("5.25"), fTenCentsDown);
    assertTrue( quote.getPercentProfit().equals(ZERO));
    
    quote = new Quote(fStock, new BigDecimal("6.30"), fTenCentsDown);
    assertTrue( quote.getPercentProfit().equals(new BigDecimal("20.00")) );
    
    Stock stock = new Stock(
      "Blah", "BLA", fTorontoStockExchange, 0, new BigDecimal("0") 
    );
    quote = new Quote(stock, new BigDecimal("6.30"), fTenCentsDown);
    assertTrue( quote.getPercentProfit().equals(ZERO) );
    
    stock = new Stock(
      "Blah", "BLA", fTorontoStockExchange, 0, new BigDecimal("0.00") 
    );
    quote = new Quote(stock, new BigDecimal("6.30"), fTenCentsDown);
    assertTrue( quote.getPercentProfit().equals(ZERO));
  }
  
   // PRIVATE 
   private final BigDecimal fOneDollar = new BigDecimal("1.00");
   private final BigDecimal fTenCentsUp = new BigDecimal(".10");
   private final BigDecimal fTenCentsDown = new BigDecimal("-0.10");
   private final Exchange fNYSEStockExchanges = Exchange.valueFrom(
     "NYSE Stock Exchanges"
   );
   private final Exchange fTorontoStockExchange = Exchange.valueFrom(
     "Toronto Stock Exchange"
   );
   private final Stock fStock = new Stock(
     "Pepsi", "PEP", fNYSEStockExchanges, 100, new BigDecimal("5.25")
   );
   private final Quote fQuote = new Quote(fStock, fOneDollar, fTenCentsDown);
   
  /**
  * If a Quote can be successfully constructed from the args, then call <tt>fail</tt>.
  */
  private void testFailedCtor(Stock aStock, BigDecimal aPrice, BigDecimal aChange){
    boolean hasSucceeded = true;
    try {
      Quote quote = new Quote(aStock, aPrice, aChange);
    }
    catch (Throwable ex){
      hasSucceeded = false;
    }
    if (  hasSucceeded ) fail();
  }
}
