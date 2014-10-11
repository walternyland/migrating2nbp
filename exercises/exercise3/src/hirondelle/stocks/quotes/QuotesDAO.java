package hirondelle.stocks.quotes;

import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.net.*;
import java.math.BigDecimal;
import javax.swing.ProgressMonitorInputStream;

import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.DataAccessException;

/**
* Given a set of stocks in a {@link hirondelle.stocks.portfolio.Portfolio}, will retrieve 
* current price information from the web, and return corresponding 
* {@link Quote} objects.
*/
public final class QuotesDAO { 

  /**
  * Constructor. 
  *  
  * @param aUseMonitor indicates if a <tt>ProgressMonitor</tt> should be used 
  * during fetch operations.
  * @param aStocks is a possibly-empty collection of {@link Stock} objects, 
  * with a definite iteration order.
  */
  public QuotesDAO(UseMonitor aUseMonitor, Collection<Stock> aStocks){
    Args.checkForNull(aStocks);
    fStocks = aStocks;
    fUseMonitor = aUseMonitor.getValue();
  }

  /** 
  * Enumeration for the two states of aUseMonitor passed to the  
  * constructor. 
  */
  public enum UseMonitor { 
    TRUE(true),
    FALSE(false);
    boolean getValue() { 
      return fToggle;  
    } 
    private final  boolean fToggle;
    private UseMonitor (boolean aToggle) { 
      fToggle = aToggle;
    }
  }  

  /** 
  * Fetch current stock price data from the web.
  *
  * @return List of {@link Quote} objects, whose size and iteration 
  * order matches that of the collection of <tt>Stock</tt> objects passed to the 
  * constructor; if that collection is empty, then return an empty <tt>List</tt>; if
  * a ticker is invalid, then it is still included in the result, but its price
  * will be zero.
  */
  public List<Quote> getQuotes() throws DataAccessException {
    /*
    * Implementation Notes:
    * Uses a Yahoo service, which outputs a simple textual (non-html) String
    * in response to a query regarding stock prices.
    * Example URL for a single stock (NT.TO symbol in this case): <br>
    *  http://quote.yahoo.com/d/quotes.csv?s=NT.TO&f=sl1d1t1c1ohgv&e=.csv 
    *
    * Multiple selections are simply comma-separated as in: <br>
    *  http://quote.yahoo.com/d/quotes.csv?s=NT.TO,SUNW&f=sl1d1t1c1ohgv&e=.csv <br>
    *
    * which outputs the following:<br>
    * "NT.TO",3.59,"12/2/2002","4:53pm",0.00,N/A,N/A,N/A,0<br>
    * "SUNW",4.14,"12/3/2002","4:00pm",-0.15,4.56,4.58,4.12,46700<br>
    *
    * The fields appear in this order:<br>
    * ticker, last trade, date-last-trade, time-last-trade, change, open,hi, lo,volume<br>
    * 
    * The Yahoo service limits request to a maximum of 200 quotations.
    * Indexes begin with a special character, eg "^DJI" is the Dow Jones.
    * Example currency conversion: "THBMGF=X", which gives a quote between 
    * THB - Thai baht - and MGF  - Malagasy franc. The result of this query is: <br>
    * "THBMGF=X",149.4387,"1/22/2003","10:19am",N/A,N/A,N/A,N/A,N/A
    */
    if (OFF_LINE) {
      System.out.println("HARD - fixed quote prices");
      return getStaticQuotes(); //debugging only
    }
    if (fStocks.size() == 0) return Collections.emptyList();
    
    URL yahooUrl = null;
    try {
      yahooUrl = new URL(getYahooUrlText());
    }
    catch (MalformedURLException ex){
      fLogger.severe("Cannot create Yahoo Url using: " + getYahooUrlText());
    }
    List<Quote> queryResult = getQueryResult(yahooUrl);
    //fLogger.fine("Query result: " + queryResult);
    return queryResult;
  }
  
  // PRIVATE 

  /**
  * Developers may set this environment variable to true in order to operate 
  * offline, such that hard-coded quote information is used instead of being 
  * fetched from the web.
  *
  * In the development environemnt, add a custom property to the command line, if 
  * desired, as in : 
  * in "java -Doffline=true -jar StocksMonitor.jar"
  */
  private static final boolean OFF_LINE = Boolean.getBoolean("offline");
  
  /**
  * The collection of {@link Stock} objects for which quotes will be retrieved.
  */
  private Collection<Stock> fStocks;

  /**
  * If true, then a graphical progress indicator is to be displayed 
  * to the user while long-running operations take place.
  *
  * The GUI is only displayed if the underlying operation takes more than 
  * a few seconds. 
  *
  * (In practice, since the amount of data being 
  * retrieved is usually small, the gui element will 
  * almost never be displayed. Tests with 200 quotes, which 
  * is the max allowable by Yahoo, showed no Progress Bar.)
  */
  private boolean fUseMonitor;
  
  private static final Logger fLogger = 
    Logger.getLogger(QuotesDAO.class.getPackage().getName())
  ;  

  private static final String fYAHOO_URL_START = "http://quote.yahoo.com/d/quotes.csv?s=";
  private static final String fYAHOO_URL_END = "&f=sl1d1t1c1ohgv&e=.csv";
  
  private BigDecimal ZERO = Consts.ZERO_MONEY_WITH_DECIMAL;
  private int ROUND_MODE = Consts.MONEY_ROUNDING_STYLE;
  private int DECIMALS = Consts.MONEY_DECIMAL_PLACES;
  
  /**
  * Return the HTTP URL to be used for fetching stock data from Yahoo,
  * represented as a String.
  */
  private String getYahooUrlText(){
    StringBuilder result = new StringBuilder(fYAHOO_URL_START);
    Iterator<Stock> stocksIter = fStocks.iterator();
    while (stocksIter.hasNext()){
      Stock stock = stocksIter.next();
      result.append(getTickerForYahooUrl(stock));
      if ( stocksIter.hasNext() ) {
        result.append(Consts.COMMA);
      }
    }
    result.append(fYAHOO_URL_END);
    return result.toString();

    //Exercises a large number of quotes (the max allowed by Yahoo is 200)
    //    StringBuilder result = new StringBuilder(fYAHOO_URL_START);
    //    for ( int idx = 0; idx<200 ; ++idx) {
    //      result.append("SUNW");
    //      if ( idx < 199 ) {
    //       result.append(",");
    //      }
    //    }
    //    result.append(fYAHOO_URL_END);
    //    return result.toString();
  }
  
  /**
  * Return the custom stock ticker symbol used by Yahoo, which may contain 
  * a suffix representing the Exchange. (The Exchange suffix is not used for some 
  * common US exchanges.)
  */
  private String getTickerForYahooUrl( Stock aStock ) {
    StringBuilder result = new StringBuilder(aStock.getTicker ());
    String exchangeSuffix = aStock.getExchange().getTickerSuffix();
    if (Util.textHasContent(exchangeSuffix)){
      result.append(".");
      result.append(exchangeSuffix);
    }
    return result.toString();
  }

  /**
  * Return List of {@link Quote} objects.
  */
  private List<Quote> getQueryResult(URL aHttpRequest) throws DataAccessException {
    /* 
     * This implementation depends on determinate iteration orders.
     * That is, the iteration
     * order used here is identical to that used to generate the query.
     * As well, the order of items in the response must match the order 
     * in the request. 
     * This is necessary only because the exchange mapping is not one-to-one.
     */
    List<Quote> result = new ArrayList<>();
    List<String> lines = getLines(aHttpRequest);
    int rowIdx = 0;
    for(Stock stock : fStocks){
      result.add(getQuote(stock, lines.get(rowIdx)));
      ++rowIdx;
    }
    return result;
  }

  /**
  * Translate the response from Yahoo into a List of lines of text.
  * This is needed only because the Yahoo response is not readily parsed
  * back into exchange and stock, since the mapping is not quite one-to-one.
  */
  private List<String> getLines(URL aHttpRequest) throws DataAccessException {
    List<String> result = new ArrayList<>();
    try {
      InputStream input = null;
      LineNumberReader htmlReader = null;
      try {
        if ( fUseMonitor ){
          //This error may occur inside an IDE:
          //java.net.SocketException: Unrecognized Windows Sockets error: 10106: create
          input = new ProgressMonitorInputStream(
            null, "Fetching.", aHttpRequest.openStream()
          );
        }
        else {
          input = aHttpRequest.openStream(); 
        }
        htmlReader = new LineNumberReader(new InputStreamReader(input));
        String line = null;
        while ( (line = htmlReader.readLine())!= null ) {
          result.add(line);
        }
      }
      finally {
        if (htmlReader != null) htmlReader.close();
        if (input != null) input.close();
      }
    }
    catch(IOException ex) {
      throw new DataAccessException("Please connect to the Web. Cannot access Yahoo.", ex);
    }
    return result;
  }
  
  /**
  * Return Quote object corresponding to a single line of a Yahoo 
  * query response.
  */ 
  private Quote getQuote(Stock aStock, String aQueryResultLine) {
    //The fields appear in this order:
    //ticker, last-trade, date-last-trade, time-last-trade, change, open, hi, lo, volume
    StringTokenizer parser = new StringTokenizer(aQueryResultLine, Consts.COMMA);
    
    //confirm that this line matches aStock's ticker
    String ticker = parseTicker(parser.nextToken());
    if ( !ticker.startsWith(aStock.getTicker()) ) {
      fLogger.severe(
        "Invalid ticker-exchange? Expected line for " + aStock.getTicker() + 
        ", but received: "+ aQueryResultLine
      );
    }
    
    BigDecimal roundedPrice = parsePrice(parser.nextToken());
    //BigDecimal methods return new objects:
    roundedPrice = roundedPrice.setScale(
      Consts.MONEY_DECIMAL_PLACES, ROUND_MODE
    );
    
    //discard two tokens
    parser.nextToken(); 
    parser.nextToken(); 

    BigDecimal roundedChange =  parsePriceChange(parser.nextToken());
    roundedChange = roundedChange.setScale(
      Consts.MONEY_DECIMAL_PLACES, Consts.MONEY_ROUNDING_STYLE
    );
    
    return new Quote(aStock, roundedPrice, roundedChange);
  }
  
    
   /**
   * Convert a price from text to a numeric value.
   * If the price contains a fraction, change it to a decimal.
   */
   private BigDecimal parsePrice(String aPrice) {
     //Prices from Yahoo are in four forms
     //   78 5/8   (characterized by space and slash)
     //   78.625
     //      5/8
     //     .01 
     BigDecimal result = ZERO;
     BigDecimal dollars = ZERO;
     BigDecimal numerator = ZERO;
     BigDecimal denominator = ZERO;
     aPrice = aPrice.trim();
     if (aPrice.indexOf('/') != -1) {
        //the price contains a fraction somewhere
        StringTokenizer parser = new StringTokenizer(aPrice);
        while (parser.hasMoreElements()) {
           String token = parser.nextToken();
           if (token.indexOf('/') == -1) {
             //not the fractional part, so it must be the dollar part
             dollars = new BigDecimal(token);
           }
           else { 
             //the fractional part; gets its 2 parts
             StringTokenizer fractionParser = new StringTokenizer(token,"/");
             numerator = new BigDecimal(fractionParser.nextToken());
             denominator = new BigDecimal(fractionParser.nextToken()); 
           }
        }
        if (!denominator.equals(ZERO)){
          BigDecimal cents = 
            numerator.divide(denominator, ROUND_MODE).setScale(DECIMALS, ROUND_MODE)
          ;
          result = dollars.add(cents);
        }
        else {
          result = dollars;
        }
     }
     else {
        //price doesn't contain any fraction
        result = new BigDecimal(aPrice);
     }
     return result;
   }
  
   /**
   * Convert a price change from text to a numeric value.
   * If the price change contains a fraction, convert it to a decimal.
   * Textual price changes are simply an algebraic sign plus a price.
   */
   private BigDecimal parsePriceChange(String aPriceChange) {
    //Price changes from Yahoo come in five forms:
    //   +5.25
    //   -5 1/4
    //   -1/4
    //     0.00    (seen on holiday closures)
    //     0       (seen on holiday closures)
    BigDecimal result = ZERO;
    BigDecimal sign = ZERO;
    aPriceChange = aPriceChange.trim();
    //take the leading character as the sign. 
    if ( aPriceChange.startsWith(Consts.PLUS_SIGN) ) {
      sign = new BigDecimal("1");
    }
    else if ( aPriceChange.startsWith(Consts.NEGATIVE_SIGN) ) {
      sign = new BigDecimal("-1");
    }
    else {
      //the price change is zero, and has no leading sign
      return result;
    }
    //pass the string without the leading sign to parsePrice
    String magnitudeOfPriceChange = aPriceChange.substring(1); 
    result = sign.multiply(parsePrice(magnitudeOfPriceChange));
    return result;
  }
  
  /**
  * Remove all quotation marks.
  * 
  * @param aTicker is of the form "JAVA", with leading and trailing quotation marks.
  */
  private String parseTicker(String aTicker){
    return aTicker.replaceAll(Consts.DOUBLE_QUOTE, Consts.EMPTY_STRING);
  }
  
  /**
  * Used for developing purposes only, to provide simple stub data.
  * Uses the stocks in fStocks, but returns simple, fixed numbers for the quote.
  */
  private List<Quote> getStaticQuotes(){
    java.util.List<Quote> result = new ArrayList<>();
    for(Stock stock : fStocks){
      Quote quote = new Quote(stock, new  BigDecimal("10.00"), new BigDecimal("-0.75"));
      result.add(quote );
    }
    return result;
  }
}