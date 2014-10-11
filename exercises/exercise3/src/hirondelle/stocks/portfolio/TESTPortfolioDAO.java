package hirondelle.stocks.portfolio;

import junit.framework.*;
import java.util.*;
import java.math.BigDecimal;

import hirondelle.stocks.quotes.Exchange;
import hirondelle.stocks.quotes.Stock;

/**
* JUnit tests for {@link PortfolioDAO}.
*
* <P>These tests are unusual in that they affect the data store. 
* It is thus always useful to take advantage of any Backup/Restore 
* facility when executing such tests. 
* This can be done by launching the application and selecting <tt>File->Export</tt>.
* It is highly recommended that this be done before running these tests.
*/
public final class TESTPortfolioDAO extends TestCase {

  /** Run the test cases.  */
   public static void main(String... aArgs) {
     String[] testCaseName = {TESTPortfolioDAO.class.getName()};
     junit.textui.TestRunner.main(testCaseName);
  }

   public TESTPortfolioDAO (String aName) {
    super( aName );
  }

  // TEST CASES
   
  public void testSaveAs(){
    Set<Stock> stocks = new LinkedHashSet<Stock>();
    Stock ibm = new Stock("Big Blue", "IBM", fNYSE, 100, new BigDecimal("3.25")); 
    stocks.add(ibm);
    Portfolio borneo = new Portfolio("Borneo", stocks);
    fPortfolioDAO.saveAs(borneo);
    
    Collection<String> names = fPortfolioDAO.fetchAllPortfolioNames();
    assertTrue( names.contains("Borneo") );
    
    //second saveAs should fail
    try {
      fPortfolioDAO.saveAs( borneo );
    }
    catch(Throwable ex){
      return;
    }
    fail();
  }
  
  public void testFetchAllPortfolioNames(){
    populateFromScratch();
    Collection<String> names = fPortfolioDAO.fetchAllPortfolioNames();
    assertTrue( names.size()==2 );
    assertTrue( names.contains("Alaska") );
    assertTrue( names.contains("Le Havre") );
    
    Iterator<String> namesIter = names.iterator();
    String name = namesIter.next();
    assertTrue( name.equals("Alaska") );
    name = namesIter.next();
    assertTrue( name.equals("Le Havre") );
  }
 
  public void testIsValidCandidateName(){
    populateFromScratch();
    assertTrue( !fPortfolioDAO.isValidCandidateName("") );
    assertTrue( !fPortfolioDAO.isValidCandidateName(null) );
    assertTrue( !fPortfolioDAO.isValidCandidateName(" ") );
    assertTrue( !fPortfolioDAO.isValidCandidateName("Alaska") );
    assertTrue( !fPortfolioDAO.isValidCandidateName("Le Havre") );
    assertTrue( fPortfolioDAO.isValidCandidateName("ALASKA") );
    assertTrue( fPortfolioDAO.isValidCandidateName("Bobby") );
  }

  public void testFetch(){
    populateFromScratch();
    Portfolio alaska = fPortfolioDAO.fetch("Alaska");
    assertTrue( alaska.getName().equals("Alaska") );
    assertTrue( alaska.getStocks().contains(fIbm) ) ;
    assertTrue( alaska.getStocks().size() == 4 );
    try {
      fPortfolioDAO.fetch("Borneo");
    }
    catch (Throwable ex) {
      return;
    }
    fail();
  }
  
  public void testSaveAsDefaultAndFetchDefault(){
    populateFromScratch();
    Portfolio leHavre = fPortfolioDAO.fetch("Le Havre");
    fPortfolioDAO.saveAsDefault(leHavre);
    Portfolio defaultPortfolio = fPortfolioDAO.fetchDefaultPortfolio();
    assertTrue( defaultPortfolio.getName().equals("Le Havre") );
    assertTrue( defaultPortfolio.getStocks().size() == 5 );
  }

  public void testDelete(){
    populateFromScratch();
    Portfolio leHavre = fPortfolioDAO.fetch("Le Havre");
    fPortfolioDAO.delete(leHavre);
    Collection<String> names = fPortfolioDAO.fetchAllPortfolioNames();
    assertTrue( ! names.contains("Le Havre") );
    assertTrue( names.contains("Alaska") );
    assertTrue( names.size() == 1 );
    
    Portfolio defaultPort = fPortfolioDAO.fetchDefaultPortfolio();
    assertTrue( defaultPort.getName().equals("Alaska") );
    //now delete the default Portfolio
    fPortfolioDAO.delete( defaultPort ); 
    defaultPort = fPortfolioDAO.fetchDefaultPortfolio();
    assertTrue( !defaultPort.getName().equals("Alaska") );
    assertTrue( defaultPort.isUntitled() );
  }
  
  public void testDeleteAll(){
    populateFromScratch();
    Collection<String> names = fPortfolioDAO.fetchAllPortfolioNames();
    assertTrue( names.size() == 2 );
    fPortfolioDAO.deleteAll();
    names = fPortfolioDAO.fetchAllPortfolioNames();
    assertTrue( names.size() == 0 );
  }
  
  public void testAddStock(){
    populateFromScratch();
    
    Portfolio alaska = fPortfolioDAO.fetch("Alaska");
    Set<Stock> workingCopy = new TreeSet<Stock>( alaska.getStocks() );
    assertTrue( workingCopy.size() == 4 );
    Stock ctr = new Stock(
      "Canadian Tire", "CTR", fTSE, 100, new BigDecimal("10.00") 
    ); 
    assertTrue( !workingCopy.contains(ctr) );
    workingCopy.add( ctr );
    alaska.setStocks( workingCopy );
    
    fPortfolioDAO.save(alaska);
    alaska = fPortfolioDAO.fetch("Alaska");
    Collection<Stock> stocks = alaska.getStocks();
    assertTrue( stocks.size() == 5 );
    assertTrue( stocks.contains(ctr) );
  }
  
  public void testDeleteStock(){
    populateFromScratch();
    
    Portfolio alaska = fPortfolioDAO.fetch("Alaska");
    Set<Stock> workingCopy = new TreeSet<Stock>( alaska.getStocks() );
    assertTrue( workingCopy.size() == 4 );
    assertTrue( workingCopy.contains(fIbm) );
    workingCopy.remove(fIbm);
    assertTrue( workingCopy.size() == 3 );
    assertTrue( !workingCopy.contains(fIbm) );
    alaska.setStocks( workingCopy );
    fPortfolioDAO.save(alaska);
    
    alaska = fPortfolioDAO.fetch("Alaska");
    Collection<Stock> stocks = alaska.getStocks();
    assertTrue( stocks.size() == 3 );
    assertTrue( !stocks.contains(fIbm) );
  }
  
  // FIXTURE //

  /**
  * Build a fixture of test objects. This method is called anew for
  * each test, such that the tests will always start with the same
  * set of test objects, and the execution of one test will not interfere
  * with the execution of another.
  */
  protected void setUp(){
    fPortfolioDAO.deleteAll();
  }

  /**
  * Re-set test objects.
  */
  protected void tearDown(){
    fPortfolioDAO.deleteAll();
  }

  // PRIVATE  
  private PortfolioDAO fPortfolioDAO = new PortfolioDAO();
  private static final Exchange fNasdaq = Exchange.valueFrom("Nasdaq Stock Exchange");
  private static final Exchange fTSE = Exchange.valueFrom("Toronto Stock Exchange");
  private static final Exchange fNYSE = Exchange.valueFrom("NYSE Stock Exchanges");
  private Stock fIbm = new Stock("Big Blue", "IBM", fNYSE, 100, new BigDecimal("3.25") ); 

  /**
  * Delete all existing portfolios, and add two Portfolios called Alaska and Le Havre, 
  * each with several stocks.
  */
  private void populateFromScratch(){
    Stock csco = new Stock(
      "Cisco Systems", "CSCO", fNasdaq, 200, new BigDecimal("3.25") 
    ); 
    Stock msft = new Stock(
      "Microsoft", "MSFT", fNasdaq, 200, new BigDecimal("3.25") 
    ); 
    Stock mo = new Stock(
      "Philip Morris", "MO", fNYSE, 200, new BigDecimal("3.25") 
    ); 
    Set<Stock> stocks = new LinkedHashSet<Stock>();
    stocks.add( fIbm );
    stocks.add( csco ) ;
    stocks.add( msft );
    stocks.add( mo ) ;
    Portfolio alaska = new Portfolio("Alaska", stocks);
    fPortfolioDAO.saveAs( alaska );
    
    Stock pepsi = new Stock(
      "Pepsi", "PEP", fNYSE, 800, new BigDecimal("5.65") 
    );
    stocks.add(pepsi);
    fPortfolioDAO.saveAs( new Portfolio("Le Havre", stocks) );
    
    fPortfolioDAO.saveAsDefault(alaska);
  }
}
