package hirondelle.stocks.portfolio;

import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.ui.UiUtil;

/**
* Verifies user input into a {@link javax.swing.text.JTextComponent} versus a 
* regular expression.
* 
*<P> This class is likely useful for a wide number of simple input needs.
* See the {@link java.util.regex.Pattern} class for details regarding regular 
* expressions.
*
* <P>The {@link #main} method is provided as a developer tool for  
* testing regular expressions versus user input, but the principal use of this 
* class is to be passed to {@link javax.swing.JComponent#setInputVerifier}.
*
*<P> Upon detection of invalid input, this class takes the following actions : 
*<ul>
* <li> emit a beep
* <li> overwrite the <tt>JTextComponent</tt> to display the following: 
* INVALID: &quot; (input data) &quot;
* <li> optionally, append the tooltip text to the content of the INVALID message ; this 
* is useful only if the tooltip contains helpful information regarding input.
* Warning : appending the tooltip text may cause the error 
* text to be too long for the corresponding text field. 
*</ul>
*
*<P> The user of this class is encouraged to always place conditions on data entry 
* in the tooltip for the corresponding field.
*/
final class RegexInputVerifier extends InputVerifier {
  
  /* 
  * Implementation Note:
  * Use of JOptionPane to display error messages in an 
  * InputVerifier seems buggy. There also seem to be issues 
  * regarding focus and events.
  */
  
  /**
  * Constructor.
  *  
  * @param aPattern regular expression against which all user input will
  * be verified; <tt>aPattern.pattern</tt> satisfies
  * {@link Util#textHasContent}.
  * @param aUseToolTip indicates if the tooltip text should be appended to 
  * error messages displayed to the user. 
  */
  RegexInputVerifier(Pattern aPattern, UseToolTip aUseToolTip){
    Args.checkForContent( aPattern.pattern() );
    fMatcher = aPattern.matcher(Consts.EMPTY_STRING);
    fUseToolTip = aUseToolTip.getValue();
  }
  
  /** Enumeration compels the caller to use a style which reads clearly. */
  enum UseToolTip {
    TRUE(true),
    FALSE(false);
    boolean getValue(){
      return fToggle;
    }
    private boolean fToggle;
    private UseToolTip(boolean aToggle){
      fToggle = aToggle;
    }
  }

  /**
  * Always returns <tt>true</tt>, in this implementation, such that focus can 
  * always transfer to another component whenever the validation fails.
  *
  * <P>If <tt>super.shouldYieldFocus</tt> returns <tt>false</tt>, then 
  * notify the user of an error.
  *
  * @param aComponent is a <tt>JTextComponent</tt>.
  */
  @Override public boolean shouldYieldFocus(JComponent aComponent){
    boolean isValid = super.shouldYieldFocus(aComponent);
    if (isValid){
      //do nothing
    }
    else {
      JTextComponent textComponent = (JTextComponent)aComponent;
      notifyUserOfError(textComponent);
    }
    return true;
  }
  
  /**
  * Return <tt>true</tt> only if the untrimmed user input matches the 
  * regular expression provided to the constructor.
  *
  * @param aComponent must be a <tt>JTextComponent</tt>.
  */
  @Override public boolean verify(JComponent aComponent) {
    boolean result = false;
    JTextComponent textComponent = (JTextComponent)aComponent;
    fMatcher.reset(textComponent.getText());
    if (fMatcher.matches()) {
      result =  true;
    }
    return result;
  }
  
  /**
  * The text which begins all error messages.
  *
  * The caller may examine their text fields for the presence of 
  * <tt>ERROR_MESSAGE_START</tt>, before processing input.
  */
  static final String ERROR_MESSAGE_START = "INVALID: ";

  /**
  * Matches user input against a regular expression.
  */
  private Matcher fMatcher;
  
  /**
  * Indicates if the JTextField's tooltip text is to be appended to 
  * error messages, as a second way of reminding the user.
  */
  private boolean fUseToolTip;

  /* 
  * Various regular expression patterns used to 
  * construct convenience objects of this class:
  */
  
  private static final String TEXT_FIELD =  "^(\\S)(.){1,75}(\\S)$";
  private static final String NON_NEGATIVE_INTEGER_FIELD = "(\\d){1,9}";
  private static final String INTEGER_FIELD = "(-)?" + NON_NEGATIVE_INTEGER_FIELD;
  private static final String NON_NEGATIVE_FLOATING_POINT_FIELD = 
    "(\\d){1,10}\\.(\\d){1,10}"
  ;
  private static final String FLOATING_POINT_FIELD =  
    "(-)?" + NON_NEGATIVE_FLOATING_POINT_FIELD
  ;
  private static final String NON_NEGATIVE_MONEY_FIELD =  "(\\d){1,15}(\\.(\\d){2})?";
  private static final String MONEY_FIELD =  "(-)?" + NON_NEGATIVE_MONEY_FIELD;
  
  /**  
  * Convenience object for input of integers: ...-2,-1,0,1,2...
  *
  * <P>From 1 to 9 digits, possibly preceded by a minus sign.
  * Corresponds approximately to the spec of <tt>Integer.parseInt</tt>.
  * The limit on the number of digits is related to size of <tt>Integer.MAX_VALUE</tt>
  * and <tt>Integer.MIN_VALUE</tt>.
  */
  static final RegexInputVerifier INTEGER = 
    new RegexInputVerifier(Pattern.compile(INTEGER_FIELD), UseToolTip.FALSE)
  ;

  /**
  * Convenience object for input of these integers: 0,1,2...
  *
  *<P> As in {@link #INTEGER}, but with no leading minus sign.
  */
  static final RegexInputVerifier NON_NEGATIVE_INTEGER = 
    new RegexInputVerifier(Pattern.compile(NON_NEGATIVE_INTEGER_FIELD), UseToolTip.FALSE)
  ;
         
  /**
  * Convenience object for input of short amounts of text.
  *
  * <P>Text contains from 1 to 75 non-whitespace characters.
  */
  static final RegexInputVerifier TEXT = 
    new RegexInputVerifier(Pattern.compile(TEXT_FIELD), UseToolTip.FALSE)
  ;
    
  /**
  * Convenience object for input of decimals numbers, eg -23.23321, 100.25.
  *
  * <P>Possible leading minus sign, 1 to 10 digits before the decimal, and 1 to 10 
  * digits after the decimal.
  */
  static final RegexInputVerifier FLOATING_POINT = 
    new RegexInputVerifier(Pattern.compile(FLOATING_POINT_FIELD), UseToolTip.FALSE)
  ;

  /**
  * Convenience object for input of non-negative decimals numbers, eg 23.23321, 100.25.
  *
  * <P>As in {@link #FLOATING_POINT}, but no leading minus sign.
  */
  static final RegexInputVerifier NON_NEGATIVE_FLOATING_POINT = 
    new RegexInputVerifier(
      Pattern.compile(NON_NEGATIVE_FLOATING_POINT_FIELD), UseToolTip.FALSE
    )
  ;

  /**
  * Convenience object for input of money values, eg -23, 100.25.
  *
  * <P>Possible leading minus sign, from 1 to 15 leading digits, and optionally  
  * a decimal place and two decimals.
  */
  static final RegexInputVerifier MONEY = 
    new RegexInputVerifier(Pattern.compile(MONEY_FIELD), UseToolTip.FALSE)
  ;
  
  /**
  * Convenience object for input of non-negative money values, eg 23, 100.25.
  *
  * <P>As in {@link #MONEY}, except no leading minus sign
  */
  static final RegexInputVerifier NON_NEGATIVE_MONEY = 
    new RegexInputVerifier(Pattern.compile(NON_NEGATIVE_MONEY_FIELD), UseToolTip.FALSE)
  ;

  /**
  * If an error message is currently displayed in aComponent, then 
  * do nothing; otherwise, display an error message to the user in a
  * aComponent (see class description for format of message).
  */
  private void notifyUserOfError(JTextComponent aTextComponent){
    if ( isShowingErrorMessage(aTextComponent) ){
      //do nothing, since user has not yet re-input.
    }
    else {
      UiUtil.beep();
      showErrorMessage(aTextComponent);
    }
  }

  private boolean isShowingErrorMessage(JTextComponent aTextComponent){
    return aTextComponent.getText().startsWith(ERROR_MESSAGE_START);
  }
  
  private void showErrorMessage(JTextComponent aTextComponent) {
    StringBuilder message = new StringBuilder(ERROR_MESSAGE_START);
    message.append("\"");
    message.append(aTextComponent.getText());
    message.append("\"");
    if ( fUseToolTip ) {
      message.append(aTextComponent.getToolTipText());
    }
    aTextComponent.setText(message.toString());
  }
  
  /*
  * All members appearing below are used only by the "main" developer test harness.
  */
  
  /**
  * Developer test harness for verifying a regular expression, using a simple 
  * graphical interface and a <tt>RegexInputVerifier</tt>.
  *
  *<p>Use of the GUI is straightforward :
  *<ul>
  *<li> Enter a regular expression (regex).
  *<li> Hit the <tt>Set Regex</tt> button to begin testing input versus the regex.
  *<li> Enter text into the <tt>Text Input</tt> field.
  *<li> When focus moves out of the <tt>Text Input</tt> field, then the input 
  * is verified versus the regex.
  *<li> To try out another regex, simply enter a new one and hit the 
  * <tt>Set Regex</tt> button.
  *<li> Tooltips offer simple reminders as well.
  *</ul>
  *
  * <p>(For running this test harness, <tt>RegexInputVerifier</tt> needs to 
  * be a public class; after testing is finished, it is probably a good idea to 
  * change the scope to package-private, since the services of this class are 
  * used only by the user interface layer.)
  */
  public static void main(String... aArgs){
    showGuiForExercisingVerifier();
  }
  
  private static JFrame fFrame;
  private static JTextField fRegexField;
  private static JTextField fInputField;
  
  private static void showGuiForExercisingVerifier(){
    //General layout of the gui:
    //  
    //  Regular Expression:     [----] 
    //  Test Input:             [----]
    //
    //  Set-Regex     Test       Close
    //
    fFrame = new JFrame("Test Regular Expressions - javapractices.com");
    fFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    JPanel content = new JPanel();
    content.setLayout( new BoxLayout(content, BoxLayout.Y_AXIS) );
    content.setBorder( UiUtil.getStandardBorder() );
    content.add( getEditor() );
    content.add( getCommandRow() );
    
    fFrame.getContentPane().add( content );
    UiUtil.centerAndShow( fFrame );
  }
  
  private static JComponent getEditor(){
    final JPanel content = new JPanel();
    content.setLayout( new GridBagLayout() );
    GridBagConstraints constraints = UiUtil.getConstraints(0,0);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    fRegexField = UiUtil.addSimpleEntryField(
      content, "Regular Expression:", null, 
      KeyEvent.VK_R, constraints, 
      "Double backslashes are not needed. Set Regex to init."
    );
    constraints = UiUtil.getConstraints(1,0);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    fInputField = UiUtil.addSimpleEntryField(
      content, "Test Input:", null, 
      KeyEvent.VK_I, constraints,
      "This text is verified versus the regular expression"
    );
    UiUtil.addVerticalGridGlue(content, 2);
    return content;
  }
  
  private static JComponent getCommandRow(){
    JButton setRegex = new JButton("Set Regex");
    setRegex.setMnemonic(KeyEvent.VK_S);
    setRegex.setToolTipText("Start testing input versus the above regular expression");
    setRegex.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setRegex();
      }
    });
    
    JButton test = new JButton("Test");
    test.setMnemonic(KeyEvent.VK_T);
    test.setToolTipText("Exercises verifier by simply receiving focus");
    test.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        //do nothing
        //exists only as a target for new focus, such that 
        //the focus tries to move out of the input field
      }
    });
    
    JButton close = new JButton("Close");
    close.setMnemonic(KeyEvent.VK_C);
    close.addActionListener( new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        fFrame.dispose();
      }
    });
    
    java.util.List<JComponent> buttons = new ArrayList<>();
    buttons.add(setRegex) ;
    buttons.add(test);
    buttons.add(close);
    
    return UiUtil.getCommandRow( buttons );
  }
  
  private static void setRegex(){
    Pattern regex = Pattern.compile(fRegexField.getText());
    RegexInputVerifier verifier = new RegexInputVerifier(regex, UseToolTip.FALSE);
    fInputField.setInputVerifier(verifier);
  }
}
