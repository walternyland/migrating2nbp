package hirondelle.stocks.table;

import java.util.Comparator;
import hirondelle.stocks.quotes.Quote;

/**
* Translates a {@link QuoteField} into a 
* corresponding <tt>Comparator</tt> whose primary sort is on that field.
*
* <P>All returned <tt>Comparator</tt> objects are synchronized with 
* {@link Quote#equals}; thus, they may all be used with sorted collections.
*
*<P>There is only one use case for this class:
<pre>
 Comparator<Quote> quoteSorter = QuoteSorterFactory.getSorter(quoteField);
</pre>
*/
public final class QuoteSorterFactory {
  
  public static Comparator<Quote> getSorter(QuoteField aField) {
    Comparator<Quote> result = null;
    if ( aField == QuoteField.Stock ) {
      result = STOCK_SORTER;
    }
    else if ( aField == QuoteField.Price ) {
      result = PRICE_SORTER;
    }
    else if ( aField == QuoteField.Change) {
      result = CHANGE_SORTER;
    }
    else if ( aField == QuoteField.Profit ) {
      result = PROFIT_SORTER;
    }
    else if ( aField == QuoteField.PercentChange ) {
      result = PERCENT_CHANGE_SORTER;
    }
    else if ( aField == QuoteField.PercentProfit ) {
      result = PERCENT_PROFIT_SORTER;
    }
    else {
      throw new AssertionError("Unknown quote field: " + aField);
    }
    return result;
  }
  
  // PRIVATE 
  
  //disable all object construction 
  private QuoteSorterFactory(){
    throw new AssertionError();
  }
  
  private static final Comparator<Quote> STOCK_SORTER = new StockSorter();
  private static final Comparator<Quote> PRICE_SORTER = new PriceSorter();
  private static final Comparator<Quote> CHANGE_SORTER = new ChangeSorter();
  private static final Comparator<Quote> PROFIT_SORTER = new ProfitSorter();
  private static final Comparator<Quote> PERCENT_CHANGE_SORTER = 
    new PercentChangeSorter()
  ;
  private static final Comparator<Quote> PERCENT_PROFIT_SORTER = 
    new PercentProfitSorter()
  ;
  
  private static final int BEFORE = -1;
  private static final int EQUAL = 0;
  private static final int AFTER = 1;
  
  /**
  * Users expect items to be initially sorted in these ways, which one may define as
  * descending order:
  *<ul>
  * <li> Text: A, B, C.... (corresponding to natural ordering of <tt>String</tt>)
  * <li> Numbers 59, 23, 0, -2 (reverse of natural ordering of <tt>Number</tt>)
  *</ul>
  *
  * REVERSE is used to reverse the natural ordering of <tt>Number</tt>, to match 
  * these expectations.
  */
  private static final int REVERSE = -1;
  
  /**
  * Provides the template for <tt>compare</tt> methods, and 
  * eliminates code duplication.
  */
  private static abstract class QuoteSorter implements Comparator<Quote> {
    @Override public final int compare(Quote aThis, Quote aThat){
      if (aThis == aThat) return EQUAL;
      int result = compareFields(aThis, aThat);
      if (result == EQUAL) {
        assert aThis.equals(aThat) : "compareTo inconsistent with equals.";
      }
      return result;
    }
    abstract int compareFields(Quote aThis, Quote aThat);
  }

  /*
  * The following three classes perform all the same sorts, but in different
  * orders; they are named after the first sort performed.
  */
  
  private static final class StockSorter extends QuoteSorter {
    @Override public int compareFields(Quote aThis, Quote aThat){
      int comparison = compareStock(aThis, aThat);
      if ( comparison != EQUAL ) return comparison;

      comparison = comparePrice(aThis, aThat);
      if ( comparison != EQUAL ) return comparison;
      
      return compareChange(aThis, aThat);
    }
  }
  
  private static final class PriceSorter extends QuoteSorter {
    @Override public int compareFields(Quote aThis, Quote aThat){
      int comparison = comparePrice(aThis, aThat);
      if ( comparison != EQUAL ) return comparison;
      
      comparison = compareStock(aThis, aThat);
      if ( comparison != EQUAL ) return comparison;
      
      return compareChange(aThis, aThat);
    }
  }

  private static final class ChangeSorter extends QuoteSorter {
    @Override public int compareFields(Quote aThis, Quote aThat){
      int comparison = compareChange(aThis, aThat);
      if ( comparison != EQUAL ) return comparison;
      
      comparison = compareStock(aThis, aThat);
      if ( comparison != EQUAL ) return comparison;

      return comparePrice(aThis, aThat);
    }
  }

  private static int compareStock(Quote aThis, Quote aThat){
    return aThis.getStock().compareTo(aThat.getStock());
  }
  
  private static int comparePrice(Quote aThis, Quote aThat){
    return REVERSE * aThis.getPrice().compareTo(aThat.getPrice());
  }
  
  private static int compareChange(Quote aThis, Quote aThat){
    return REVERSE * aThis.getChange().compareTo(aThat.getChange());
  }

  /**
  * Sort first on a derived field, then sort as <tt>StockSorter</tt>.
  *
  * <P>Derived fields are special cases.
  * The guiding idea is to maintain synchronization with {@link Quote#equals}. 
  *
  * <P>{@link Quote#equals} uses Stock, Price, and 
  * Change, but does not use the derived fields PercentChange, Profit, 
  * and PercentProfit. Comparisons on derived fields, if performed at all, 
  * are always performed first, and are always followed by comparisons on 
  * Stock, Price, and Change. This style maintains synch with <tt>equals</tt>.
  */
  private static abstract class DerivedFieldSorter extends QuoteSorter {
    @Override public final int compareFields(Quote aThis, Quote aThat){
      int comparison = compareDerivedField(aThis, aThat);
      if (comparison != EQUAL) return comparison;
      
      return fStockSorter.compareFields(aThis, aThat);
    }
    abstract int compareDerivedField(Quote aThis, Quote aThat);
    private QuoteSorter fStockSorter = new StockSorter();
  }

  private static final class ProfitSorter extends DerivedFieldSorter {
    @Override public int compareDerivedField(Quote aThis, Quote aThat){
      return REVERSE * aThis.getProfit().compareTo(aThat.getProfit());
    }
  }

  private static final class PercentProfitSorter extends DerivedFieldSorter {
    @Override public int compareDerivedField(Quote aThis, Quote aThat){
      if ( 
        aThis.getPercentProfit().compareTo(aThat.getPercentProfit()) <= 0 
      ) return AFTER;
      if ( 
       aThis.getPercentProfit().compareTo(aThat.getPercentProfit()) > 0 
      ) return BEFORE;    
      return EQUAL;
    }
  }

  private static final class PercentChangeSorter extends DerivedFieldSorter {
    @Override public int compareDerivedField(Quote aThis, Quote aThat){
      if ( 
        aThis.getPercentChange().compareTo(aThat.getPercentChange()) <= 0 
      ) return AFTER;
      if ( 
        aThis.getPercentChange().compareTo(aThat.getPercentChange()) > 0 
      ) return BEFORE;    
      return EQUAL;
    }
  }
}
