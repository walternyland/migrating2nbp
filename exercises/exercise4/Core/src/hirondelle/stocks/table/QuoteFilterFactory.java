package hirondelle.stocks.table;

import java.util.logging.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;

import hirondelle.stocks.quotes.Stock;
import hirondelle.stocks.quotes.Quote;
import hirondelle.stocks.quotes.Exchange;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.util.Util;

/**
* Graphical component which allows the end user to select a 
* {@link QuoteFilter}, and informs its listeners of changes 
* to this selection.
* 
* <P>JDK less than 6.
*
* <P>When informed of selection changes, listeners call {@link #getSelectedFilter}
* and use the returned <tt>QuoteFilter</tt> to alter their display to reflect 
* only those items in the {@link CurrentPortfolio} which satisfy certain criteria.
*
* <P>The initial state of this class has no item selected, and 
* {@link #getSelectedFilter} returns a <tt>QuoteFilter</tt>
* which accepts all {@link Quote} objects. 
* When the <tt>CurrentPortfolio</tt> is changed, this initial state is recreated. 
*
* <p>Property Listeners are notified only when a selection is made actively by the
* user, and only if the selection is different from the previous selection.
*/
public final class QuoteFilterFactory extends JScrollPane implements Observer {

  /**
  * Constructor.
  *  
  * @param aCurrentPortfolio is observed by this class, since the list of 
  * possible filters displayed by this class depends on its content.
  */
  public QuoteFilterFactory (CurrentPortfolio aCurrentPortfolio){
    fCurrentPortfolio = aCurrentPortfolio;
    //bad practice - 'this' is not fully defined until the constructor has returned:
    fCurrentPortfolio.addObserver(this);
    fSelectedFilter = NO_SELECTION_FILTER;
    initGui();
  }
  
  /**
  * Return the <tt>QuoteFilter</tt> 
  * attached to the currently selected item. If no selection is present, 
  * then return a <tt>QuoteFilter</tt> which accepts all {@link Quote} objects.
  */
  public QuoteFilter getSelectedFilter(){
    assert fSelectedFilter != null : "Filter has null value.";
    return fSelectedFilter;
  }

  /**
  * Update this component's GUI in response to changes in the 
  * {@link CurrentPortfolio} passed to the constructor.
  */
  @Override public void update(Observable aObservable, Object aData) {
    fSelectedFilter = NO_SELECTION_FILTER;
    synchFilterTreeWithCurrentPortfolio();
  } 
  
  /**
  * Property name passed to listeners during <tt>PropertyChangeEvent</tt>.
  */
  public static final String SELECTED_FILTER = "SelectedFilter";
  
  // PRIVATE

  private CurrentPortfolio fCurrentPortfolio;
  private DefaultTreeModel fFilterTreeModel;
  
  /**
  * The GUI element which allows the user to select a particular filter.
  */
  private JTree fFilterSelector;
  
  /**
  * The {@link QuoteFilter} corresponding to the user's 
  * current non-null selection.
  */
  private QuoteFilter fSelectedFilter;
  
  /**
  * The {@link QuoteFilter} corresponding to the absence of 
  * any user selection.
  */
  private QuoteFilter NO_SELECTION_FILTER = new QuoteFilterAcceptAll(Consts.EMPTY_STRING);

  /*
  * These Strings appear as node names in fFilterSelector.
  */
  
  private static final String FILTER_BY = "Filter By";
  private static final String EXCHANGE = "Exchange";
  private static final String TODAYS_CHANGE = "Today's Change";
  private static final String GAINERS = "Gainers";
  private static final String LOSERS = "Losers";
  private static final String TICKER_TYPE = "Ticker Type";
  private static final String INDEX = "Index";
  private static final String NON_INDEX = "Non-Index";
  
  private static final Logger fLogger = Util.getLogger(QuoteFilterFactory.class);  
  
  /**
  * Build a tree corresponding to the contents of the {@link CurrentPortfolio}, 
  * and attach a corresponding {@link QuoteFilter} to 
  * every tree node.
  */
  private void initGui(){
    initFilterTreeModel();
    initFilterTree();
    synchFilterTreeWithCurrentPortfolio();
    setViewportView( fFilterSelector );
    setPreferredSize( new Dimension(160,240) );
  }
  
  private void initFilterTreeModel(){
    DefaultMutableTreeNode exchange =  new DefaultMutableTreeNode( 
      new QuoteFilterAcceptAll(EXCHANGE) 
    );
    //children of exchange depend on current portfolio, and are added a bit later
    
    //note how user objects are critical here, and must be attached to ALL nodes.
    DefaultMutableTreeNode todaysChange =  new DefaultMutableTreeNode(
      new QuoteFilterAcceptAll(TODAYS_CHANGE) 
    );
    todaysChange.add( new DefaultMutableTreeNode(new QuoteFilterGainers()) );
    todaysChange.add( new DefaultMutableTreeNode(new QuoteFilterLosers()) );

    DefaultMutableTreeNode tickerType =  new DefaultMutableTreeNode(
      new QuoteFilterAcceptAll(TICKER_TYPE) 
    );
    tickerType.add( new DefaultMutableTreeNode(new QuoteFilterIndex()) );
    tickerType.add( new DefaultMutableTreeNode(new QuoteFilterNonIndex()) );
    
    DefaultMutableTreeNode filterBy = new DefaultMutableTreeNode(
      new QuoteFilterAcceptAll(FILTER_BY) 
    );
    filterBy.add(exchange);
    filterBy.add(todaysChange);
    filterBy.add(tickerType);
    
    fFilterTreeModel = new DefaultTreeModel(filterBy);
  }
  
  private void initFilterTree(){
    fFilterSelector = new JTree(fFilterTreeModel);
    fFilterSelector.setRootVisible(true); //recommended "false" by L&F guidelines
    fFilterSelector.setShowsRootHandles(true); //recommended by L&F guidelines
    fFilterSelector.setEditable(false); 
    fFilterSelector.getSelectionModel().setSelectionMode(
      TreeSelectionModel.SINGLE_TREE_SELECTION
    );
    
    //Note that using a local class hides the fact that this class 
    //listens to a JTree; the public valueChanged event is not exposed in the 
    //public API of this class, since that method is of no interest to the caller; 
    //it is an implementation detail.
    fFilterSelector.addTreeSelectionListener(new TreeSelectionListener() {
      @Override public void valueChanged(TreeSelectionEvent e) {
        updateFilterAndBroadcast();      
      }
    });
  }
  
  /**
  * For each Exchange present in the {@link CurrentPortfolio}, add a single
  * node to the filter tree (under the Exchange branch), assign a 
  * {@link QuoteFilterExchange} as the user object, and ensure no item is selected.
  */
  private void synchFilterTreeWithCurrentPortfolio() {
    DefaultMutableTreeNode filterBy = 
      (DefaultMutableTreeNode)fFilterTreeModel.getRoot()
    ;
    //DEPENDENCY: the exchange node is the *first* item:
    DefaultMutableTreeNode exchangesNode = 
      (DefaultMutableTreeNode)filterBy.getFirstChild()
    ;
    exchangesNode.removeAllChildren();
    
    Set<Exchange> exchanges = new LinkedHashSet<Exchange>();
    for(Stock stock: fCurrentPortfolio.getStocks()){
      Exchange exchange = stock.getExchange();
      if ( ! exchanges.contains(exchange) ) {
        exchanges.add(exchange);
        exchangesNode.add(new DefaultMutableTreeNode(new QuoteFilterExchange(exchange))); 
      }
    }
    fFilterTreeModel.reload(); //fires a selection event, but it is suppressed
  }

  /**
  * Update listeners in response to a new filter selection in the GUI. 
  *
  * <P>If a new {@link CurrentPortfolio} is selected, then 
  * the tree will have no selection, and this method will do nothing.
  */
  private void updateFilterAndBroadcast() {
    if ( hasNoSelection() ) return;
    QuoteFilter newSelectedFilter = parseSelectedFilter();
    assert newSelectedFilter != null : "New filter is unexpectedly null.";
    assert newSelectedFilter != NO_SELECTION_FILTER : "New Filter is unexpected value.";
    if ( fSelectedFilter == newSelectedFilter ) {
      fLogger.fine("No broadcast: user selection does not have a new filter");
    }
    else {
      fLogger.fine("New filter being broadcast: " + newSelectedFilter);
      QuoteFilter oldSelectedFilter = fSelectedFilter;
      fSelectedFilter = newSelectedFilter;
      firePropertyChange(SELECTED_FILTER, oldSelectedFilter, fSelectedFilter);
    }
  }

  /**
  * Return true only if the user has made no filter selection.
  */
  private boolean hasNoSelection(){
    return (getSelectedNode() == null);
  }

  /**
  * Must call {@link #hasNoSelection} before invoking this method, to ensure that
  * a selection exists and is not null.
  */
  private QuoteFilter parseSelectedFilter(){
    assert getSelectedNode() != null : "Selected Filter unexpectedly null.";
    return (QuoteFilter)getSelectedNode().getUserObject();
  }
  
  /**
  * Return value is null if no selection is currently made.
  */
  private DefaultMutableTreeNode getSelectedNode(){
    return (DefaultMutableTreeNode) fFilterSelector.getLastSelectedPathComponent();
  }

  // All QuoteFilter implementations:
  
  /**
  * No {@link Quote} objects are rejected by this filter.
  *
  *<P> Since more than one tree node uses this filter, text is 
  * passed to the constructor to allows for customized display.
  */
  static private class QuoteFilterAcceptAll extends QuoteFilter {
    QuoteFilterAcceptAll(String aText) {
      fText = aText;
    }
    @Override public boolean isAcceptable( Quote aQuote ){
      return true;
    }
    @Override public String toString(){
      return fText;
    }
    private final String fText;
  }
  
  /**
  * {@link Quote} objects are accepted only if they are from a specific 
  * {@link Exchange}.
  */
  static private final class QuoteFilterExchange extends QuoteFilter {
    QuoteFilterExchange( Exchange aExchange ) {
      fTargetExchange = aExchange;
    }
    @Override public boolean isAcceptable( Quote aQuote ){
      return aQuote.getStock().getExchange() == fTargetExchange;
    }
    @Override public String toString(){
      return fTargetExchange.toString();
    }
    private Exchange fTargetExchange;
  }
  
  /**
  * {@link Quote} objects are accepted only if todays price change is 
  * non-negative.
  */
  static private final class QuoteFilterGainers extends QuoteFilter {
    @Override public boolean isAcceptable(Quote aQuote){
      return aQuote.getChange().doubleValue() >= 0.0;
    }
    @Override public String toString(){
      return GAINERS;
    }
  }

  /** 
  * {@link Quote} objects are accepted only if todays price change is negative.
  */
  static private final class QuoteFilterLosers extends QuoteFilter {
    @Override public boolean isAcceptable(Quote aQuote){
      return aQuote.getChange().doubleValue() < 0.0;
    }
    @Override public String toString(){
      return LOSERS;
    }
  }
  
  /** {@link Quote} objects are accepted only if it is an index.  */
  static private final class QuoteFilterIndex extends QuoteFilter {
    @Override public boolean isAcceptable(Quote aQuote){
      return aQuote.getStock().isIndex();
    }
    @Override public String toString(){
      return INDEX;
    }
  }

  /** {@link Quote} objects are accepted only if it is not an index.  */
  static private final class QuoteFilterNonIndex extends QuoteFilter {
    @Override public boolean isAcceptable(Quote aQuote){
      return !aQuote.getStock().isIndex();
    }
    @Override public String toString(){
      return NON_INDEX;
    }
  }
}
