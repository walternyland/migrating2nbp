package hirondelle.stocks.table;

import hirondelle.stocks.quotes.Quote;
import java.util.*;

/**
* Allows collections of {@link Quote} objects 
* to be filtered according to a criterion defined by implementors.
* JDK less than 6.
*/
public abstract class QuoteFilter {
  
  /**
  * Defines the criteria by which <tt>aQuote</tt> is accepted or rejected 
  * by this filter.
  */
  abstract public boolean isAcceptable(Quote aQuote);
  
  /**
  * Return a <tt>List</tt> which has the same 
  * iteration order as <tt>aQuotes</tt>, but which includes only those elements 
  * which satisfy {@link #isAcceptable}.
  */
  public final List<Quote> sift(Collection<Quote> aQuotes ){
    /*
    * This is an example of a template method : the general outline is
    * defined here in this abstract base class, but the implementation of 
    * specific steps (in this case the method isAcceptable) is left to 
    * concrete subclasses.
    *
    * Note as well that this method is final, so that no subclass can override this 
    * implementation.
    */
    List<Quote> result = new ArrayList<>();
    for(Quote quote : aQuotes){
      if (isAcceptable(quote)) {
        result.add(quote);
      }
    }
    return result;
  }
}
