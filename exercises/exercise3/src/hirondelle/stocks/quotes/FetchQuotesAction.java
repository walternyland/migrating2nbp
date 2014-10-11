package hirondelle.stocks.quotes;

import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.preferences.QuoteTablePreferencesEditor;
import hirondelle.stocks.table.QuoteTable;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.DataAccessException;
import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.ui.UiConsts;
import hirondelle.stocks.util.ui.UiUtil;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

/**
* Fetch current quote data for the {@link CurrentPortfolio} from a data 
* source on the web.
*
* <P>This action is performed at many different times :
*<ul>
* <li>once upon startup
* <li>periodically, at an interval configured by the user
* <li>when the end user explicitly requests a refresh of quote info
* <li>when the user makes a change to the current portfolio
*</ul>
*
* <P>This class performs most of its work in a background thread, 
* using a javax.swing.Timer. The user interface remains responsive, 
* regardless of the time taken for its work to complete.
*/
public final class FetchQuotesAction extends AbstractAction implements Observer {

  /**
  * Constructor.
  *  
  * @param aCurrentPortfolio an <tt>Observable</tt> which notifies this 
  * object when the {@link CurrentPortfolio} is changed
  * @param aQuoteTablePrefEditor allows this class to read the user preference
  * for the frequency of periodic fetches
  * @param aQuoteTable a GUI element which is updated when a fetch is performed
  * @param aSummaryView a GUI element which is updated when a fetch is performed
  */
  public FetchQuotesAction (
    CurrentPortfolio aCurrentPortfolio, 
    QuoteTablePreferencesEditor aQuoteTablePrefEditor, 
    QuoteTable aQuoteTable, 
    SummaryView aSummaryView
  ) {
    super("Update", UiUtil.getImageIcon("/toolbarButtonGraphics/general/Refresh")); 
    Args.checkForNull(aQuoteTable);
    Args.checkForNull(aSummaryView);
    fCurrentPortfolio = aCurrentPortfolio;
    
    fQuoteTablePrefEditor = aQuoteTablePrefEditor;
    
    fQuoteTable = aQuoteTable;
    fSummaryView = aSummaryView;
    
    putValue(SHORT_DESCRIPTION, "Fetch updated stock quotes from web");
    putValue(
      ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, UiConsts.NO_KEYSTROKE_MASK)
    );      
    putValue(
      LONG_DESCRIPTION, 
      "Retrieves fresh stock quotes and displays it to the user in a table."
    );
    putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_U) );    

    fUpdateFreq = fQuoteTablePrefEditor.getUpdateFrequency();
  }

  /**
  * Start an internal Timer, which in turn calls {@link #actionPerformed(ActionEvent)}.
  * 
  * <P>This method must be called immediately after calling the constructor. 
  * (Since this operation uses a 'this' reference, it shouldn't be included in the 
  * constructor itself.)  
  */
  public void startTimer(){
    fQuoteTablePrefEditor.addObserver(this);
    fCurrentPortfolio.addObserver(this);
    fTimer = new javax.swing.Timer(fUpdateFreq * CONVERSION_FACTOR, this);
    fTimer.start();
  }

  /** 
   Fetch quotes from the web for the <tt>CurrentPortfolio</tt>.
   This is called either explicitly, or periodically, by a Timer.
  */
  @Override public void actionPerformed(ActionEvent e) {
    fLogger.info("Fetching quotes from web.");
    fSummaryView.showStatusMessage("Fetching quotes...");
    SwingWorker<List<Quote>, Void> hardWorker = new HardWorker();
    hardWorker.execute();
  }
  
  /**
  * Listens for changes to the <tt>CurrentPortfolio</tt> or the user
  * preference for update frequency. 
  *<P>Calls {@link #actionPerformed} if the current portfolio has changed.
  *<P>If the update frequency has changed, the underlying Timer 
  * is restarted.
  */
  @Override public void update(Observable aPublisher, Object aData) {
    fLogger.fine("Notified ...");
    if (aPublisher == fQuoteTablePrefEditor) {
      fLogger.fine("By StocksTablePrefEditor.");
      boolean hasChangedFreq = (fQuoteTablePrefEditor.getUpdateFrequency()!= fUpdateFreq);
      if (hasChangedFreq) {
        restartTimer();
      }
    }
    else {
      fLogger.fine("By Current Portfolio.");
      actionPerformed(null);
    }
  } 

  // PRIVATE 

  private static final Logger fLogger = Util.getLogger(FetchQuotesAction.class);
  
  /**
  * The set of {@link Stock} objects in which the user 
  * is currently interested.
  */
  private CurrentPortfolio fCurrentPortfolio;
  
  /**
  * Edits user preferences attached to a table which presents quote data, and 
  * allows read-only programmatic access to such preferences.
  */
  private QuoteTablePreferencesEditor fQuoteTablePrefEditor;
  
  /**
  * GUI element which is updated whenever a new set of quotes is obtained.
  */
  private QuoteTable fQuoteTable;

  /**
  * GUI element which is updated whenever a new set of quotes is obtained.
  */
  private SummaryView fSummaryView;
  
  /**
  * Periodically fetches quote data.
  *
  * <P>Use of a Swing Timer is acceptable here, in spite of the fact that the task
  * takes a long time to complete, since the task does <em>not</em> in fact get 
  * executed on the event-dispatch thread, but on a separate worker thread.
  */
  private javax.swing.Timer fTimer;
  
  /** The number of minutes to wait between fetches of quote information. */
  private int fUpdateFreq;
  
  private static final int CONVERSION_FACTOR = 
    Consts.MILLISECONDS_PER_SECOND * Consts.SECONDS_PER_MINUTE
  ;

  private final class HardWorker extends SwingWorker<java.util.List<Quote>, Void> {
    @Override  protected List<Quote> doInBackground() throws Exception {
      List<Quote> result = null;
      try {
        result = fCurrentPortfolio.getPortfolio().getQuotes();
      }
      catch(DataAccessException ex){
        ex.printStackTrace();
      }
      return result;
    }
    @Override protected void done() {
      try {
        List<Quote> quotes = get();
        if (quotes != null){
          showUpdated(quotes);
        }
        else {
          fSummaryView.showStatusMessage("Failed - Please connect to the web.");
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }

  private void showUpdated(List<Quote> aQuotes) {
    fQuoteTable.setQuoteTable(aQuotes);
    fSummaryView.setQuotes(aQuotes);
    StringBuilder warning = new StringBuilder();
    if (hasNoZeroPrices(aQuotes, warning)){
      fSummaryView.showStatusMessage("Done.");
    }
    else {
      fSummaryView.showStatusMessage(warning.toString());
    }
  }
  
  private MessageFormat fTickerWarningFormat = 
    new MessageFormat("Warning - no price for ticker {0} ({1})")
  ;
  
  private boolean hasNoZeroPrices(List<Quote> aQuotes, StringBuilder aMessage){
    for(Quote quote: aQuotes){
      if ( Util.isZeroMoney(quote.getPrice()) ) {
        Object[] params = {
          quote.getStock().getTicker(), 
          quote.getStock().getExchange()
        };
        aMessage.append(fTickerWarningFormat.format(params));
        return false;
      }
    }
    return true;
  }
  
  /** Use for testing purposes only.  */
  private void simulateLongDelay(){
    try {
      Thread.sleep(20000);
    }
    catch (InterruptedException ex) {
      System.out.println(ex);
    }
  }
  
  private void restartTimer(){
    fLogger.fine("Resetting initial delay and delay to: " + fUpdateFreq + " minutes.");
    fUpdateFreq = fQuoteTablePrefEditor.getUpdateFrequency();
    fTimer.setInitialDelay(fUpdateFreq * CONVERSION_FACTOR);
    fTimer.setDelay(fUpdateFreq * CONVERSION_FACTOR);
    fLogger.fine("Cancelling pending tasks, and restarting timer...");
    fTimer.restart();
  }
}
