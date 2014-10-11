package hirondelle.stocks.main;

import junit.framework.*;
import hirondelle.stocks.quotes.TESTExchange;
import hirondelle.stocks.quotes.TESTQuote;
import hirondelle.stocks.portfolio.TESTPortfolio;
import hirondelle.stocks.quotes.TESTStock;
import hirondelle.stocks.util.ui.TESTTheme;
import hirondelle.stocks.portfolio.TESTPortfolioDAO;
import hirondelle.stocks.quotes.TESTQuotesDAO;

/**
* Runs all JUnit tests in this application.
*
* <P>Not all classes have an associated JUnit test. Most tests are related to 
* Model Objects and Data Access Objects. 
*
* <P>These tests must be performed with live data, and the system must have 
* an open web connection.
*
*<P>These tests will overwrite the application's data store, so it is recommended that a 
* backup be performed using the application's <tt>File->Export</tt> feature. Restore 
* is performed through <tt>File->Import</tt>.
*
* <P>If run through an IDE on a Windows machine, this error might occur:
* <pre>
* java.net.SocketException: Unrecognized Windows Sockets error: 10106: create
* </pre>
* If so, simply run this class outside the IDE.
*/
public final class TESTAll {
  
  /*** Run the test cases.  */
   public static void main(String... aArgs) {
     String[] testCaseName = { TESTAll.class.getName()};
     junit.textui.TestRunner.main(testCaseName);
  }

  public static Test suite ( ) {
    TestSuite suite= new TestSuite("All JUnit Tests");

    suite.addTest(new TestSuite(TESTPortfolio.class));
    suite.addTest(new TestSuite(TESTQuote.class));
    suite.addTest(new TestSuite(TESTStock.class));
    suite.addTest(new TestSuite(TESTExchange.class));
    
    suite.addTest(new TestSuite(TESTPortfolioDAO.class));
    suite.addTest(new TestSuite(TESTQuotesDAO.class));
    
    suite.addTest(new TestSuite(TESTTheme.class));

    return suite;
  }
}
