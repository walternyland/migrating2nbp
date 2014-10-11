package hirondelle.stocks.portfolio;

import hirondelle.stocks.quotes.Exchange;
import hirondelle.stocks.quotes.Stock;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.Util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
* Add, change, delete, and fetch {@link Portfolio} objects 
* from the datastore.
*/
public final class PortfolioDAO { 
  
  /*
  * Implementation Note:
  * This implementation uses the Preferences mechanism for storage. All preferences 
  * are attached to the system, not the user. This allows for easy data transfer, while 
  * allowing for rapid selection of a portfolio from a simple list of names, as opposed 
  * to using a JFileChooser.
  */
  
  /**
  * Save a <tt>Portfolio</tt> under a name which is not already known to the datastore. 
  * The <tt>Portfolio</tt> may be new, or may be an old <tt>Portfolio</tt>
  * being saved under a new name. Should be used with {@link #isValidCandidateName} to
  * verify that a candidate name does not already exist.
  *
  * @param aPortfolio has a name different from any known <tt>Portfolio</tt>.
  */
  public void saveAs(Portfolio aPortfolio){
    if( isStored(aPortfolio.getName()) ){
      String message = "Cannot save; name already exists: " + aPortfolio.getName();
      IllegalArgumentException ex = new IllegalArgumentException(message);
      throw ex;
    }
    getPortfoliosRootPref().node( aPortfolio.getName() );
    save(aPortfolio);
  }
  
  /**
  * Save a <tt>Portfolio</tt> which already exists in storage.
  *
  * {@link #saveAs} must be used if <tt>aPortfolio</tt> has not yet been saved.
  *
  * @param aPortfolio already exists in storage.
  */
  public void save(Portfolio aPortfolio){
    Preferences portfolioPref = getExistingPortfolioPref(aPortfolio.getName());
    portfolioPref.put(STOCKS_KEY, aPortfolio.getStocks().toString() );
  }

  /**
  * Save <tt>aPortfolio</tt> as the one to be launched upon startup.
  */
  public void saveAsDefault( Portfolio aPortfolio ){
    Preferences rootPref = getPortfoliosRootPref();
    rootPref.put(DEFAULT_PORTFOLIO_NAME_KEY, aPortfolio.getName());
  }
  
  /**
  * Used at startup, and returns the <tt>Portfolio</tt> which was current 
  * when the application was last closed. If the application was not launched
  * before, or if an untitled <tt>Portfolio</tt> was current when it was last closed,
  * then return an untitled <tt>Portfolio</tt>.
  */
  public Portfolio fetchDefaultPortfolio(){
    Preferences rootPref = getPortfoliosRootPref();
    String defaultPortfolioName = rootPref.get(
      DEFAULT_PORTFOLIO_NAME_KEY, Consts.EMPTY_STRING
    );
    if ( Util.textHasContent(defaultPortfolioName) ) {
      return fetch ( defaultPortfolioName );
    }
    else {
      return Portfolio.getUntitledPortfolio();
    }
  }
  
  /**
  * Return <tt>String</tt> objects, one for each stored <tt>Portfolio</tt>. 
  * 
  * <P> The iteration order of the return value is alphabetical. 
  */
  public Collection<String> fetchAllPortfolioNames(){
    /* 
    * Implementation Note:
    * The returned object needs a specific iteration order, so a TreeSet is used.
    * But, the declared return type is the generic Collection interface. 
    * This is done because the callers of this method only use Collection
    * methods - they never use methods specific to TreeSet (or even Set). 
    *
    * The general idea is that return type should only be as specific as
    * callers demand. If, in the future, some new caller demands a more specific
    * return type (say Set), then the the return type of this method can 
    * be changed without producing ripple effects in existing callers.
    */
    String[] names = null;
    try {
      names = getPortfoliosRootPref().childrenNames();
    }
    catch (BackingStoreException ex) {
      fLogger.log(Level.SEVERE, "Cannot access backing store.", ex);
    }
    return new TreeSet<String>( Arrays.asList(names) );
  }
  
  /**
  * Return <tt>true</tt> only if <tt>aNewName</tt> has visible content 
  * and is not an element of {@link #fetchAllPortfolioNames}.
  *
  * <P> A candidate name is a name input by the end user during a <tt>File->New</tt>
  * or <tt>File->SaveAs</tt> operation as a candidate <tt>Portfolio</tt> name.
  */
  public boolean isValidCandidateName(String aNewName){
    Collection<String> allNames = fetchAllPortfolioNames();
    return ( Util.textHasContent(aNewName) && !allNames.contains(aNewName) );
  }
  
  /**
  * Return the <tt>Portfolio</tt> whose unique id is <tt>aPortfolioName</tt>.
  *
  * @param aPortfolioName must be known to the datastore.
  */
  public Portfolio fetch(String aPortfolioName){
    Preferences portfolioPref = getExistingPortfolioPref(aPortfolioName);
    return new Portfolio(aPortfolioName, getStocks(portfolioPref) );
  }
  
  /**
  * Remove a stored <tt>Portfolio</tt>, and leave all others intact.
  *
  * <P>If <tt>aPortfolio</tt> is the default <tt>Portfolio</tt>, then the 
  * preference for the default <tt>Portfolio</tt> is removed as well, 
  * and an untitled <tt>Portfolio</tt> will be returned from 
  * {@link #fetchDefaultPortfolio}.
  */
  public void delete( Portfolio aPortfolio ){
    String defaultPortfolioName = fetchDefaultPortfolio().getName();
    if ( aPortfolio.getName().equals(defaultPortfolioName) ) {
      getPortfoliosRootPref().remove(DEFAULT_PORTFOLIO_NAME_KEY);
    }
    
    Preferences portfolioPref = getExistingPortfolioPref(aPortfolio.getName());
    try {
      portfolioPref.removeNode();
    }
    catch (BackingStoreException ex) {
      fLogger.log(Level.SEVERE, "Cannot access backing store", ex);
    }
  }

  /**
  * Remove all <tt>Portfolio</tt>s, and start from scratch 
  * (for development purposes only).
  * 
  * <P>End users can only delete <tt>Portfolio</tt>s one at at time, using 
  * {@link #delete}.
  */
  public void deleteAll(){
    try {
      //removes both the root node and all its descendants
      getPortfoliosRootPref().removeNode();
    }
    catch ( BackingStoreException ex ) {
      fLogger.log(Level.SEVERE, "Cannot access backing store.", ex);
    }
  }

  /**
  * Place the content of all <tt>Portfolio</tt>s in a single text file, 
  * in the format defined by {@link Preferences#exportSubtree}.
  *
  * @param aFile may or may not currently exist; if it does not yet exist, it is created; 
  * if it does exist, it must have write access, and will be overwritten by this method.
  */
  public void exportXML(File aFile) {
    try {
      OutputStream output = null;
      try { 
        output = new BufferedOutputStream( new FileOutputStream( aFile ) );
        getPortfoliosRootPref().exportSubtree(output);
      }
      finally {
        output.close();
      }
    }
    catch (IOException ex) {
      fLogger.severe("Cannot save to file " + aFile);
    }
    catch(BackingStoreException ex){
      fLogger.log(Level.SEVERE, "Cannot access backing store.", ex);
    }
  }
  
  /**
  * Replace all current <tt>Portfolio</tt>s with the contents of a 
  * single XML file produced by {@link #exportXML}.
  *
  * @param aFile must already exist, must have read access, and must contain an 
  * unmodified result of {@link #exportXML}.
  */
  public void importXML(File aFile) {
    try {
      InputStream input = null;
      try {
        input = new BufferedInputStream( new FileInputStream( aFile ) );
        getPortfoliosRootPref().importPreferences(input);
      }
      finally {
       input.close();        
      }
    }
    catch (IOException ex) {
      fLogger.severe("Cannot read file " + aFile);
    }
    catch(InvalidPreferencesFormatException ex){
      fLogger.log(Level.SEVERE, "Format of Preferences file is invalid.", ex);
    }
  }
  
  // PRIVATE //

  // Example Preferences tree structure :
  //
  // /stocksmonitor/data (system node attached to this package)
  //   key="DefaultPortfolioName"
  //   value=JillsPortfolio
  //   BobsPortfolio (node name provided by end user, acts as unique id)
  //     key="stocks" (fixed key name)
  //     value=[PEP:Pepsi:NYSE Stock Exchanges:100:8.25, 
  //            SUNW:Sun Microsystems:Nasdaq Exchange:500:3.25]
  //   JillsPortfolio (another node)
  //     key="stocks"
  //     value=[IBM:Big Blue:NYSE Stock Exchanges:300:4.50, 
  //           SUNW:Sun Microsystems:Nasdaq Exchange:500:3.25]
  //     ...
  // The value is a Set of formatted Stock objects.
 
  private static final Logger fLogger = Util.getLogger(PortfolioDAO.class);  

  /**
  * The Preferences key used to look up 
  * a String containing a Set of formatted Stock objects.
  */
  private static final String STOCKS_KEY = "stocks"; 
  
  private static final String DEFAULT_PORTFOLIO_NAME_KEY = "DefaultPortfolioName";
  
  /**
  * Return the Preferences node which contains all Portfolios. If the node,
  * does not exist, then it is created.
  */
  private static Preferences getPortfoliosRootPref(){
    return Preferences.userNodeForPackage(PortfolioDAO.class);    
  }
  
  /**
  * Return an existing Portfolio Preferences node. 
  * @param aPortfolioName must already exist; if not, an IllegalArgumentException 
  * is thrown.
  */
  private Preferences getExistingPortfolioPref( String aPortfolioName ){
    if ( isStored(aPortfolioName) ) {
      return getPortfoliosRootPref().node( aPortfolioName );
    }
    else {
      throw new IllegalArgumentException("Unknown Portfolio Name:" + aPortfolioName);
    }
  }

  /**
  * Return true only if aPortfolioName corresponds to a stored Portfolio.
  */
  private boolean isStored(String aPortfolioName) {
    boolean result = false;
    try {
      result = getPortfoliosRootPref().nodeExists(aPortfolioName);
    }
    catch ( BackingStoreException ex){
      fLogger.severe(
        "Cannot access backing store for Portfolio Name: " + aPortfolioName + 
        " Exception: " + ex
       );
    }
    return result;
  }

  /**
  * Return the stored Stock objects attached to a particular Preference.
  * If there are no stocks, return an empty Set.
  */
  private Set<Stock> getStocks( Preferences aPortfolioPref ){
    Collection<Stock> result = new HashSet<Stock>();
    String rawStocks = aPortfolioPref.get(STOCKS_KEY, Consts.EMPTY_STRING);
    //fLogger.fine("Raw stocks: " + rawStocks);
    String delimiters = "[],";
    StringTokenizer parser = new StringTokenizer( rawStocks, delimiters );
    while ( parser.hasMoreTokens() ) {
      String rawStock = parser.nextToken().trim();
      if ( rawStockHasContent(rawStock) ) {
        Stock stock = getStock(rawStock);
        result.add(stock);
      }
    }
    return new TreeSet<Stock>(result);
  }
  
  private boolean rawStockHasContent( String aRawStock ){
    return 
     aRawStock!=null && 
     aRawStock.trim().length()>0 && 
     !aRawStock.equalsIgnoreCase("null") ;
  }
  
  /**
  * Parse a raw String of the form  
  * "PEP:Pepsi:NYSE Stock Exchanges:100:8.25"
  * into a Stock object.
  */
  private Stock getStock(String aRawStock){
    Stock result = null;
    String delimiter = ":";
    StringTokenizer parser = new StringTokenizer(aRawStock, delimiter);
    try {
      String ticker = parser.nextToken();
      String name = parser.nextToken();
      Exchange exchange = Exchange.valueFrom( parser.nextToken() );
      Integer quantity = Integer.valueOf( parser.nextToken() );
      BigDecimal avgPrice = new BigDecimal( parser.nextToken() );
      result = new Stock(name, ticker, exchange, quantity, avgPrice);
    }
    catch ( NoSuchElementException ex){
      fLogger.severe("Cannot parse into Stock object: \"" + aRawStock + "\"");
    }
    return result;
  }
  
  /** Exercise some toy data.  */
  private static void main(String... args) {
    Exchange nYSEStockExchanges = Exchange.valueFrom("NYSE Stock Exchanges");
    Exchange nasdaqStockExchange = Exchange.valueFrom("Nasdaq Stock Exchange"); 
    Exchange torontoStockExchange = Exchange.valueFrom("Toronto Stock Exchange");
    
    Stock ibm = new Stock(
      "Big Blue", "IBM", nYSEStockExchanges, new Integer(100), new BigDecimal("6.00") 
    ); 
    Stock csco = new Stock(
      "Cisco Systems", "CSCO", nasdaqStockExchange, 
      new Integer(200), new BigDecimal("10.25") 
    ); 
    Stock ctr = new Stock(
      "Canadian Tire", "CTR", torontoStockExchange, 
      new Integer(100), new BigDecimal("5.00") 
    ); 
    Stock mo = new Stock(
      "Philip Morris", "MO", nYSEStockExchanges, 
      new Integer(200), new BigDecimal("3.25") 
    );
    
    Set<Stock> stocks = new TreeSet<Stock>();
    stocks.add(ibm);
    stocks.add(csco) ;
    stocks.add(ctr);
    stocks.add(mo) ;
    Portfolio alaska = new Portfolio("Alaska", stocks);
    PortfolioDAO dao = new PortfolioDAO();
    dao.saveAs( alaska );
    
    Stock pepsi = new Stock(
      "Pepsi", "PEP", nYSEStockExchanges, 
      new Integer(800), new BigDecimal("5.65") 
    );
    stocks.add(pepsi);
    dao.saveAs(new Portfolio("Le Havre", stocks));
    
    dao.saveAsDefault(alaska);
  }
}
