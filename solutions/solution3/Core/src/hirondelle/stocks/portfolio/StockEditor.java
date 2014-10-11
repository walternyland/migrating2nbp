package hirondelle.stocks.portfolio;

import java.util.*;
import java.util.regex.Pattern;
import java.math.BigDecimal;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import hirondelle.stocks.quotes.Stock;
import hirondelle.stocks.quotes.Exchange;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.ui.StandardEditor;
import hirondelle.stocks.util.ui.UiUtil;

/**
* Dialog allows user to either add new a {@link Stock} to the 
* {@link CurrentPortfolio}, or to change the parameters of a <tt>Stock</tt>
* which is already in the <tt>CurrentPortfolio</tt>.
*/
final class StockEditor {
  
  /**
  * Constructor.
  *  
  * @param aFrame parent to which dialogs of this class are attached.
  */
  StockEditor(JFrame aFrame) {
    Args.checkForNull(aFrame);
    fFrame = aFrame;
  }
  
  /**
  * Return the {@link Stock} representing a new 
  * item which the user has input, or <tt>null</tt> if the user cancels the dialog.
  */
  Stock addStock(){
    showDialog("Add New Stock", null);
    return fNewStock;
  }

  /**
  * Return the possibly-edited version of <tt>aOldStock</tt>, representing 
  * the desired changes, or <tt>null</tt> if the user cancels the dialog.
  * 
  * @param aOldStock {@link Stock} which the end user wishes to change.
  */
  Stock changeStock(Stock aOldStock){
    Args.checkForNull(aOldStock);
    showDialog("Change Stock", aOldStock);
    return fNewStock;
  }
  
  // PRIVATE 

  /**
  * Always returned to the caller, and is null if the user cancels the 
  * dialog.
  */
  private Stock fNewStock;
  
  private JTextField fNameField;
  private JTextField fTickerField;
  private JComboBox<Exchange> fExchangeField;
  private JTextField fQuantityField;
  private JTextField fAveragePriceField;
  
  private JFrame fFrame;

  private void showDialog(String aTitle, Stock aInitialValue){
    Editor editor = new Editor(aTitle, fFrame, aInitialValue);
    editor.showDialog();
  }

  private final class Editor extends StandardEditor { 
    /**
    * @param aInitialValue is possibly-null; if null, then the editor represents 
    * and Add action, otherwise it represents a Change action, and all fields will
    * be pre-populated with values taken from aInitialValue.
    */
    Editor(String aTitle, JFrame aParent, Stock aInitialValue){
      super(aTitle, aParent, StandardEditor.CloseAction.HIDE);
      fInitialValue = aInitialValue;
    }
    @Override protected JComponent getEditorUI () {
      return getEditor(fInitialValue);
    }
    @Override protected void okAction() {
      initNewStock(this);
    }
    private Stock fInitialValue;
  }  
  
  private JComponent getEditor(Stock aInitialValue){
    JPanel content = new JPanel();
    content.setLayout(new GridBagLayout());
    addCompanyName(content, aInitialValue);
    addTickerField(content, aInitialValue);
    addExchange(content, aInitialValue);
    addQuantity(content, aInitialValue);
    addAveragePrice(content, aInitialValue);
    return content;
  }

  /**
  * Set fNewStock, if the input is valid.
  *
  * If there is at least one error message in the text input fields, then
  * inform the user that the error must be corrected before proceeding.
  *
  * <p>If no error messages, but input still detected as faulty as 
  * per {@link Stock#isValidInput(List, String, String, Exchange, Integer, BigDecimal)}, 
  * then inform user of the problem.
  *
  * @param aEditor is the dialog which will be closed if no errors occur.
  */
  private void initNewStock(StandardEditor aEditor){
    if (hasErrorMessage()) {
      String message = "Please correct the input error before proceeding.";
      JOptionPane.showMessageDialog(
        fFrame, message, "Invalid Input", JOptionPane.INFORMATION_MESSAGE
      );
      return;
    }
    
    String name = fNameField.getText();
    String ticker = fTickerField.getText();
    Exchange exchange = Exchange.valueFrom(fExchangeField.getSelectedItem().toString());
    Integer numShares = null;
    if (Util.textHasContent(fQuantityField.getText())) {
      numShares = Integer.valueOf(fQuantityField.getText());
    }
    else {
      numShares = Consts.ZERO;
    }
    BigDecimal avgPrice = null;
    if (Util.textHasContent(fAveragePriceField.getText())) {
      avgPrice = new BigDecimal(fAveragePriceField.getText());
    }
    else {
      avgPrice = new BigDecimal( "0.00" );
    }
    
    java.util.List<String> errorMessages = new ArrayList<>();
    if (Stock.isValidInput( errorMessages, name, ticker, exchange, numShares, avgPrice)) {
      fNewStock = new Stock(name, ticker, exchange, numShares, avgPrice); 
      aEditor.dispose();
    }
    else {
      Object[] message = errorMessages.toArray();
      JOptionPane.showMessageDialog(
        fFrame, message, "Invalid Input", JOptionPane.INFORMATION_MESSAGE
      );
    }
  }
  
  /**
  * Return true only if at least one of the text input fields contains 
  * an error message.
  */
  private boolean hasErrorMessage(){
    return 
      fNameField.getText().startsWith(RegexInputVerifier.ERROR_MESSAGE_START) ||
      fTickerField.getText().startsWith(RegexInputVerifier.ERROR_MESSAGE_START) ||
      fQuantityField.getText().startsWith(RegexInputVerifier.ERROR_MESSAGE_START) ||
      fAveragePriceField.getText().startsWith(RegexInputVerifier.ERROR_MESSAGE_START
    );
  }

  private void addCompanyName(JPanel aContent, Stock aInitialValue) {
    fNameField = UiUtil.addSimpleEntryField(
      aContent, 
      "Company Name:", 
      (aInitialValue == null ? null : aInitialValue.getName()), 
      KeyEvent.VK_C, 
      UiUtil.getConstraints(0,0),
      "No spaces on the ends, and at least one character"
    );
    fNameField.setInputVerifier(RegexInputVerifier.TEXT);
  }
  
  private void addTickerField(JPanel aContent, Stock aInitialValue) {
    fTickerField = UiUtil.addSimpleEntryField(
      aContent, 
      "Ticker:", 
      (aInitialValue == null ? null : aInitialValue.getTicker()), 
      KeyEvent.VK_T, 
      UiUtil.getConstraints(1,0),
      "No spaces on the ends, 1-20 characters: " + 
      "letters, periods, underscore and ^"
    );
    String regex = "(\\^)?([A-Za-z._]){1,20}";
    RegexInputVerifier tickerVerifier = 
      new RegexInputVerifier(Pattern.compile(regex), RegexInputVerifier.UseToolTip.FALSE
    );
    fTickerField.setInputVerifier( tickerVerifier );
  }

  private void addExchange(JPanel aContent, Stock aInitialValue) {
    JLabel exchange = new JLabel("Exchange:");
    exchange.setDisplayedMnemonic(KeyEvent.VK_X);
    aContent.add( exchange, UiUtil.getConstraints(2,0) );
    DefaultComboBoxModel<Exchange> exchangesModel = 
      new DefaultComboBoxModel<>(Exchange.VALUES.toArray(new Exchange[0]))
    ;
    fExchangeField = new JComboBox<Exchange>(exchangesModel);
    if (aInitialValue != null) {
      fExchangeField.setSelectedItem( aInitialValue.getExchange() );
    }
    exchange.setLabelFor(fExchangeField);
    GridBagConstraints constraints = UiUtil.getConstraints(2,1);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    aContent.add(fExchangeField, constraints);
  }
  
  private void addQuantity(JPanel aContent, Stock aInitialValue) {
    fQuantityField = UiUtil.addSimpleEntryField(
      aContent, 
      "Quantity:", 
      (aInitialValue == null ? null : 
      aInitialValue.getNumShares().toString()), 
      KeyEvent.VK_Q, 
      UiUtil.getConstraints(3,0),
      "Zero or Integer (possible leading minus sign)"
    );
    fQuantityField.setInputVerifier(RegexInputVerifier.INTEGER);
  }
  
  private void addAveragePrice(JPanel aContent, Stock aInitialValue) {
    fAveragePriceField = UiUtil.addSimpleEntryField(
      aContent, 
       "Average Price:", 
      (aInitialValue == null ? null : 
      aInitialValue.getAveragePrice().toString()), 
      KeyEvent.VK_A, 
      UiUtil.getConstraints(4,0),
      "Zero or Positive Number (two decimals optional)"
    );
    fAveragePriceField.setInputVerifier(RegexInputVerifier.NON_NEGATIVE_MONEY);
  }
}
