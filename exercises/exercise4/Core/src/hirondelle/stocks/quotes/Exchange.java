package hirondelle.stocks.quotes;

import java.util.*;
import java.io.*;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.FileUtil;
 
/** 
* Enumeration for all exchanges used by the Yahoo quote system.
* 
* <P>Warning: this is an example of an older-style enumeration. A more modern style 
* would use an 'enum', instead of a class.
* 
* <P>The full name of the <tt>Exchange</tt> is used as an identifier. All other items
* are attached to the full name.
*
* <P> This type-safe enumeration is unusual in that each element is not 
* declared or exported individually; rather, the {@link #VALUES} field includes 
* all elements in one <tt>Collection</tt>. Thus, the caller cannot refer to 
* specific <tt>Exchange</tt> objects. 
*/
public final class Exchange implements Comparable<Exchange> { 
  
  /*
  * Implementation Note:
  * This class is unusual in that its data is read in from an associated text file, 
  * which is placed in the same directory as this class. This text file must be available 
  * at runtime. The text file is read when this class is loaded.
  *
  * The format of the text file is shown by these example lines:
  * # Blah comment
  * NYSE Stock Exchanges  (NYS) N/A
  * Amsterdam Stock Exchange  (AEX) .AS
  * ..etc
  * 
  * This class is not implemented as an enum class, since it relies on an 
  * underlying file for its data, and cannot declare enumeration members in the 
  * usual way.
  */
  
  /**
  * Return the suffix representing the <tt>Exchange</tt> (excluding dot). 
  * 
  * <P>These suffixes are defined by Yahoo, and are appended to tickers in order 
  * to provide an exact identification of a traded entity. For example, the Toronto 
  * Stock Exchange is assigned the TO suffix. Yahoo will append .TO to all 
  * tickers defined by the Toronto Stock Exchange.
  *
  * <P>In this system, some exchanges (for example, the NYSE) do not have 
  * a suffix. In this case, an empty <tt>String</tt> is returned.
  *
  * <P>The suffix is not intended for display to the end user; rather, these 
  * suffixes should be an internal detail hidden from them.
  */  
  public String getTickerSuffix(){
    return fTickerSuffix;
  }

  /**
  * Return the full name of this <tt>Exchange</tt>, suitable for presentation to the 
  * end user.
  *
  * <P>The suffix is not included in the return value.
  */
  @Override public String toString() { 
    return fName;  
  } 

  /** 
  * Convert <tt>aText</tt> into its corresponding <tt>Exchange</tt> object, 
  * if possible.
  *
  * @param aText possibly-null text which may map to an Exchange.
  * @return null if <tt>aText</tt> is null, else try to
  * match to the <tt>Exchange</tt> whose {@link #toString} equals <tt>aText</tt>.
  * @throws IllegalArgumentException if a non-null <tt>aText</tt>
  * cannot be matched to a known enumeration element.
  */
  public static Exchange valueFrom(String aText) { 
    if (aText == null) return null; 
    
    for(Exchange exchange : VALUES){
      if ( aText.equals( exchange.toString() ) ) { 
        return exchange; 
      } 
    }
    throw new IllegalArgumentException("Cannot parse into Exchange object:" + aText); 
  } 

  @Override public int compareTo(Exchange that) {
    return fOrdinal - that.fOrdinal;
  }  
  
  private final String fName;
  private final String fTickerSuffix;

  private static int fNextOrdinal = 0;
  private final int fOrdinal = fNextOrdinal++;
  
  /** Text file name */
  private static final String TEXT_FILE_NAME = "exchanges.txt";

  /**
  * The text file resource uses this char to denote comment lines.
  */
  private static final String COMMENT_CHAR = "#";
  
  /**
  * The text file resource uses this String to denote absent data.
  * For example, the NYSE has no associated suffix, whose absence is 
  * denoted by this String.
  */
  private static final String NOT_AVAILABLE = "N/A";
  
  /**
  * Populated when this class is loaded, this field contains the 
  * parsed result of reading in the text resource file.
  */
  private static List<Exchange> fValues;
  
  /**
  * Private constructor is needed to disallow the caller from constructing 
  * these objects.
  */
  private Exchange (String aName, String aTickerSuffix) { 
    fName = aName;
    fTickerSuffix = aTickerSuffix;
  }

  /**
  * Parse the text resource file and store the result in a field.
  */
  static {
    fValues = new ArrayList<>();
    parseExchangesFromTextFile();
  }

  /**
  * Parse each line not starting with a fCOMMENT_CHAR into an Exchange object.
  * Add each Exchange object to fValues, in the same order as in the text file.
  */
  private static void parseExchangesFromTextFile() {
    List<String> exchangesText = FileUtil.asLines(TEXT_FILE_NAME, Exchange.class);
    for(String line: exchangesText){
      if ( ! line.startsWith(COMMENT_CHAR) ){
        parseExchange(line);
      }
    }
  }

  static private void parseExchange(String aLine){
    StringTokenizer parser = new StringTokenizer(aLine, Consts.TAB);
    String fullName = parser.nextToken();
    //ignore the abbreviation:
    parser.nextToken();
    String suffix = getSuffix( parser.nextToken() );
    Exchange exchange = new Exchange(fullName, suffix);
    fValues.add(exchange);
  }
  
  static private String getSuffix(String aRawSuffix){
    if ( aRawSuffix.equals(NOT_AVAILABLE) ) {
      return Consts.EMPTY_STRING;
    }
    else {
      return removeLeadingDot(aRawSuffix);
    }
  }
  
  static private String removeLeadingDot(String aRawSuffix){
    return aRawSuffix.substring(1);
  }

  /**
  * Allows caller to iterate over all elements of the enumeration.
  */
  public static final List<Exchange> VALUES = Collections.unmodifiableList(fValues);
}
