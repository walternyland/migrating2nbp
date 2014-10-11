package hirondelle.stocks.quotes;

import junit.framework.*;
import java.util.*;
import java.math.BigDecimal;

/** JUnit test cases for {@link Stock}.*/
public final class TESTStock extends TestCase {

  /** Run the test cases.  */
   public static void main(String args[]) {
     String[] testCaseName = { TESTStock.class.getName()};
     junit.textui.TestRunner.main(testCaseName);
   }

  public TESTStock( String aName) {
   super( aName );
  }

  // TEST CASES //
  
  public void testBookValue(){
    Stock sunw = new Stock(
      "Sun", "SUNW", fExchange, 100, new BigDecimal("35.25") 
    );
    assertTrue( sunw.getBookValue().doubleValue()>0 );
    assertTrue( sunw.getBookValue().intValue() == 3525 );
    Stock msft = new Stock(
      "Microsoft", "MSFT", fExchange, 0, new BigDecimal("0.00") 
    );
    assertTrue( msft.getBookValue().doubleValue()<0.00001 );
    assertTrue( msft.getBookValue().intValue() == 0 );
  }
  
  public void testIsValidInput(){
    List<String> errors = new ArrayList<>();
    String name = "Sun";
    String ticker = "SUNW";
    Exchange exchange = fExchange;
    Integer numShares = new Integer(100);
    BigDecimal avgPrice = new BigDecimal("35.25");
    
    assertTrue(Stock.isValidInput(errors,  name, ticker, exchange, numShares, avgPrice));
    errors.clear();
    assertTrue(!Stock.isValidInput(errors,  null, ticker, exchange, numShares, avgPrice));
    errors.clear();
    assertTrue(!Stock.isValidInput(errors,  "", ticker, exchange, numShares, avgPrice));
    errors.clear();
    assertTrue(!Stock.isValidInput(errors,  " ", ticker, exchange, numShares, avgPrice));
    errors.clear();
    assertTrue(!Stock.isValidInput(errors,  name, null, exchange, numShares, avgPrice));
    errors.clear();
    assertTrue(!Stock.isValidInput(errors,  name, "B2B", exchange, numShares, avgPrice));
    errors.clear();
    assertTrue(Stock.isValidInput(errors,  name, "B._^", exchange, numShares, avgPrice));
    errors.clear();
    assertTrue(
      Stock.isValidInput(
        errors,  name, "aaaaaaaaaabbbbbbbbbb", exchange, numShares, avgPrice
      ) 
    );
    errors.clear();
    assertTrue( 
      !Stock.isValidInput(
        errors,  name, "aaaaaaaaaabbbbbbbbbbb", exchange, numShares, avgPrice
      ) 
    );
    errors.clear();
    assertTrue( !Stock.isValidInput(errors,  name, ticker, null, numShares, avgPrice) );
    errors.clear();
    assertTrue( !Stock.isValidInput(errors,  name, ticker, exchange, null, avgPrice) );
    errors.clear();
    assertTrue( 
      Stock.isValidInput(errors,  name, ticker, exchange, 0, avgPrice) 
    );
    errors.clear();
    assertTrue( 
      Stock.isValidInput(errors,  name, ticker, exchange, -1, avgPrice) 
    );
    errors.clear();
    assertTrue( !Stock.isValidInput(errors,  name, ticker, exchange, numShares, null) );
    errors.clear();
    assertTrue( 
      Stock.isValidInput(
        errors,  name, ticker, exchange, numShares, new BigDecimal("0.00")
      ) 
    );
    errors.clear();
    assertTrue( 
      Stock.isValidInput(
        errors,  name, ticker, exchange, numShares, new BigDecimal("0.01")
      ) 
    );
    errors.clear();
    assertTrue( 
      !Stock.isValidInput(
        errors,  name, ticker, exchange, numShares, new BigDecimal("-0.01")
      ) 
    );
    errors.clear();
  }
  
  public void testIsIndex(){
    Stock sunw = new Stock("Sun", "SUNW", fExchange, 100, new BigDecimal("35.25") );
    assertTrue( !sunw.isIndex() );
    Stock nasdaq = new Stock("Nasdaq", "^IXIG", fExchange, 100, new BigDecimal("35.25"));
    assertTrue( nasdaq.isIndex() );
  }
  
  public void testToString(){
    //SUNW:Sun Microsystems:Nasdaq Stock Exchange:100:46.25 
    Stock sunw = new Stock(
      "Sun Microsystems", "SUNW", fExchange, 100, new BigDecimal("46.25") 
    );
    assertTrue( 
      sunw.toString().equals("SUNW:Sun Microsystems:Nasdaq Stock Exchange:100:46.25") 
    );
  }
  
  public void testSuccessfulCtor(){
    String name = "Sun";
    String ticker = "SUNW";
    Exchange exchange = fExchange;
    Integer numShares = new Integer(100);
    BigDecimal avgPrice = new BigDecimal("35.25");
    
    Stock stock = null;
    try {
      stock = new Stock(name, ticker, exchange, numShares, avgPrice);
      stock = new Stock(name, "B.B", exchange, numShares, avgPrice);
      stock = new Stock(name, "B", exchange, numShares, avgPrice);
      stock = new Stock(name, "^B", exchange, numShares, avgPrice);
      stock = new Stock(name, "B_B", exchange, numShares, avgPrice);
      stock = new Stock(name, "aaaaaaaaaabbbbbbbbbb", exchange, numShares, avgPrice);
      stock = new Stock(name, ticker, exchange, new Integer(-1), avgPrice);
      stock = new Stock(name, ticker, exchange, new Integer(0), avgPrice);
      stock = new Stock(name, ticker, exchange, numShares, new BigDecimal("0"));
      stock = new Stock(name, ticker, exchange, numShares, new BigDecimal("0.00"));
      stock = new Stock(name, ticker, exchange, numShares, new BigDecimal("0.01"));
    }
    catch(Throwable ex) {
      fail("Ctor failed unexpectedly");
    }
  }
  
  public void testFailedConstruction(){
    testFailedCtor(null, fTicker, fExchange, fNumShares, fAvgPrice);
    testFailedCtor(" ", fTicker, fExchange, fNumShares, fAvgPrice);
    testFailedCtor("", fTicker, fExchange, fNumShares, fAvgPrice);
    testFailedCtor(fName, null, fExchange, fNumShares, fAvgPrice);
    testFailedCtor(fName, "2", fExchange, fNumShares, fAvgPrice);
    testFailedCtor(fName, "aaaaaaaaaabbbbbbbbbbc", fExchange, fNumShares, fAvgPrice);
    testFailedCtor(fName, fTicker, null, fNumShares, fAvgPrice);
    testFailedCtor(fName, fTicker, fExchange, null, fAvgPrice);
    testFailedCtor(fName, fTicker, fExchange, fNumShares, null);
    testFailedCtor(fName, fTicker, fExchange, fNumShares, new BigDecimal("-0.01"));
  }
  
  // PRIVATE  //
  private String fName = "Sun";
  private String fTicker = "SUNW";
  private Exchange fExchange = Exchange.valueFrom("Nasdaq Stock Exchange");
  private Integer fNumShares = new Integer(100);
  private BigDecimal fAvgPrice = new BigDecimal("35.25");

  /**
  * Call fail method only if a Stock can be successfully constructed from the args.
  */
  private void testFailedCtor(
    String aName, String aTicker, Exchange aExchange, 
    Integer aNumShares, BigDecimal aAvgPrice
  ){
    boolean hasSucceeded = true;
    try {
      Stock stock = new Stock(aName, aTicker, aExchange, aNumShares, aAvgPrice);
    }
    catch (Throwable ex){
      hasSucceeded = false;
    }
    if (  hasSucceeded ) fail();
  }
}
