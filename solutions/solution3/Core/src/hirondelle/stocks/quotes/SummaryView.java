package hirondelle.stocks.quotes;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import java.awt.*;
import java.beans.*;
import java.math.BigDecimal;

import hirondelle.stocks.table.QuoteFilter;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.UiConsts;
import hirondelle.stocks.util.ui.UiUtil;
import hirondelle.stocks.table.QuoteFilterFactory;
import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.util.Util;

/**
 * Component placed on the main screen to present summary information
 * regarding the {@link CurrentPortfolio} to the user.
 * 
 * <P> Monetary values are presented with zero decimal places, and percentages 
 * are presented with 2 decimal places.
 */
public final class SummaryView extends JPanel implements PropertyChangeListener {

  /**
   * Constructor. 
   * 
   * @param aCurrentPortfolio summarized by this class
   * @param aQuoteFilterFactory source of a {@link QuoteFilter} used to
   * limit this summary to only certain items.
   */
  public SummaryView(
    CurrentPortfolio aCurrentPortfolio, QuoteFilterFactory aQuoteFilterFactory
  ) {
    Args.checkForNull(aCurrentPortfolio);
    fCurrentPortfolio = aCurrentPortfolio;
    fQuoteFilterFactory = aQuoteFilterFactory;
    fQuoteFilterFactory.addPropertyChangeListener(this);
    LayoutManager layout = new BoxLayout(this, BoxLayout.Y_AXIS);
    setLayout(layout);
    add(getSummaryFields());
    add(Box.createVerticalStrut(UiConsts.ONE_SPACE));
    add(getTimeLastUpdateField());
    add(Box.createVerticalGlue());
    add(getStatusField());
  }

  /**
   * Update this <tt>SummaryView</tt> in response to selection by user of a new
   * criterion for filtering items.
   * 
   * <P> Listens to the {@link QuoteFilterFactory} passed to the constructor.
   * @param event processed only if its <tt>getPropertyName</tt> is equal to
   * <tt>QuoteFilterFactory.SELECTED_FILTER</tt>.
   */
  @Override public void propertyChange(PropertyChangeEvent event) {
    boolean isUndesiredEvent = !event.getPropertyName().equals(
      QuoteFilterFactory.SELECTED_FILTER
    );
    if (isUndesiredEvent) {
      fLogger.finer("SummaryView DISCARDING event...");
    }
    else {
      fLogger.finer("SummaryView processing event...");
      updateView();
    }
  }

  /**
   * Display summary information for the {@link CurrentPortfolio}, adding any filtering
   * according to the {@link QuoteFilterFactory} passed to the constructor.
   * 
   * <P>Use a {@link ColorTip} to draw the user's attention to the fresh 
   * quote information, by highlighting the time of last update for a few seconds.
   * @param aQuotes contains a {@link Quote} for every
   * {@link Stock} in the {@link CurrentPortfolio}.
   */
  void setQuotes(Collection<Quote> aQuotes) {
    fQuotes = aQuotes;
    ColorTip colorTip = new ColorTip(0, 2, fTimeLastUpdate, Color.yellow);
    colorTip.start();
    updateView();
  }

  /**
   * Present short text to the user indicating success or failure of the most recent
   * {@link FetchQuotesAction}.
   * 
   * <P>In the case of failure, <tt>aMessage</tt> should indicate remedial measures.
   * @param aMessage has visible content
   */
  void showStatusMessage(String aMessage) {
    Args.checkForContent(aMessage);
    fStatusMessage.setText(aMessage);
  }

  // PRIVATE
  private CurrentPortfolio fCurrentPortfolio;
  private QuoteFilterFactory fQuoteFilterFactory;
  private Collection<Quote> fQuotes;

  private JLabel fBookValue;
  private JLabel fCurrentValue;
  private JLabel fProfit;
  private JLabel fPercentageProfit;
  private JLabel fTimeLastUpdate;
  private JLabel fStatusMessage;

  private static final Logger fLogger = Util.getLogger(SummaryView.class);

  private JComponent getSummaryFields() {
    JPanel content = new JPanel();
    LayoutManager layout = new BoxLayout(content, BoxLayout.X_AXIS);
    content.setLayout(layout);
    content.add(getValueFields());
    content.add(Box.createHorizontalGlue());
    content.add(getProfitFields());
    return content;
  }

  private JComponent getValueFields() {
    JPanel content = new JPanel();
    content.setLayout(new GridBagLayout());
    fBookValue = UiUtil.addSimpleDisplayField(
      content, "Book Value", null, UiUtil.getConstraints(0,0), false
    );
    fBookValue.setToolTipText("Acquisition cost of the portfolio");
    fCurrentValue = UiUtil.addSimpleDisplayField(
      content, "Current Value", null, UiUtil.getConstraints(1, 0), false
    );
    fCurrentValue.setToolTipText("Current value of the portfolio");
    return content;
  }

  private JComponent getProfitFields() {
    JPanel content = new JPanel();
    content.setLayout(new GridBagLayout());
    fProfit = UiUtil.addSimpleDisplayField(
      content, "Profit", null, UiUtil.getConstraints(0, 0), false
    );
    fProfit.setToolTipText("Current value minus book value");
    fPercentageProfit = UiUtil.addSimpleDisplayField(
      content, "% Profit", null, UiUtil.getConstraints(1, 0), false
    );
    fPercentageProfit.setToolTipText("Profit divided by book value, as percent");
    return content;
  }

  private JComponent getTimeLastUpdateField() {
    JPanel content = new JPanel();
    content.setLayout(new GridBagLayout());
    fTimeLastUpdate = UiUtil.addSimpleDisplayField(
      content, "Last Update", null, UiUtil.getConstraints(0, 0), false
    );
    return content;
  }

  private JComponent getStatusField() {
    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.X_AXIS));
    fStatusMessage = UiUtil.addSimpleDisplayField(
      content, "Status", null, UiUtil.getConstraints(0,0), false
    );
    content.add(Box.createHorizontalGlue());
    return content;
  }

  private void updateView() {
    QuoteFilter filter = fQuoteFilterFactory.getSelectedFilter();
    Collection<Quote> filteredQuotes = filter.sift(fQuotes);

    fBookValue.setText(getBookValue(filteredQuotes));
    fCurrentValue.setText(getCurrentValue(filteredQuotes));
    fProfit.setText(getProfit(filteredQuotes));
    fPercentageProfit.setText(getPercentageProfit(filteredQuotes));
    fTimeLastUpdate.setText(UiUtil.getLocalizedTime(new Date()));
  }

  private String getBookValue(Collection<Quote> aQuotes) {
    Number value = fCurrentPortfolio.getPortfolio().getBookValue(aQuotes);
    return UiUtil.getLocalizedInteger(value);
  }

  private String getCurrentValue(Collection<Quote> aQuotes) {
    Number value = fCurrentPortfolio.getPortfolio().getCurrentValue(aQuotes);
    return UiUtil.getLocalizedInteger(value);
  }

  private String getProfit(Collection<Quote> aQuotes) {
    Number value = fCurrentPortfolio.getPortfolio().getProfit(aQuotes);
    return UiUtil.getLocalizedInteger(value);
  }

  private String getPercentageProfit(Collection<Quote> aQuotes) {
    BigDecimal value = fCurrentPortfolio.getPortfolio().getPercentageProfit(aQuotes);
    return UiUtil.getLocalizedPercent(value);
  }
}
