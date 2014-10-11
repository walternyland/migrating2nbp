package hirondelle.stocks.quotes;

import junit.framework.*;
import hirondelle.stocks.util.Consts;

/** JUnit tests for {@link Exchange}. */
public final class TESTExchange extends TestCase {

  /** Run the test cases. */
  public static void main(String... aArgs) {
    String[] testCaseName = {TESTExchange.class.getName()};
    junit.textui.TestRunner.main(testCaseName);
  }

  /** Canonical form of constructor. */
  public TESTExchange(String aName) {
    super(aName);
  }

  // TEST CASES //

  public void testValueOf() {
    Exchange nasdaq = Exchange.valueFrom("Nasdaq Stock Exchange");
    Exchange tse = Exchange.valueFrom("Toronto Stock Exchange");
    Exchange nyse = Exchange.valueFrom("NYSE Stock Exchanges");
  }

  public void testSuffix() {
    assertTrue(fNasdaq.getTickerSuffix().equals(Consts.EMPTY_STRING));
    assertTrue(fTSE.getTickerSuffix().equals("TO"));
  }

  public void testToString() {
    assertTrue(fNasdaq.toString().equals("Nasdaq Stock Exchange"));
    assertTrue(fTSE.toString().equals("Toronto Stock Exchange"));
  }

  public void testCompareTo() {
    assertTrue(fNasdaq.compareTo(fNasdaq) == 0);
    assertTrue(fNasdaq.compareTo(fTSE) < 0);
    assertTrue(fNasdaq.compareTo(fNYSE) > 0);
    assertTrue(fTSE.compareTo(fNYSE) > 0);
  }

  // FIXTURE //

  protected void setUp() { }

  protected void tearDown() { }

  // PRIVATE 
  private static final Exchange fNasdaq = Exchange.valueFrom("Nasdaq Stock Exchange");
  private static final Exchange fTSE = Exchange.valueFrom("Toronto Stock Exchange");
  private static final Exchange fNYSE = Exchange.valueFrom("NYSE Stock Exchanges");
}
