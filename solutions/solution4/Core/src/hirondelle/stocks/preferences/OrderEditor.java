package hirondelle.stocks.preferences;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.util.Consts;
import hirondelle.stocks.util.ui.UiConsts;
import hirondelle.stocks.util.ui.UiUtil;

/**
* Graphical component for changing the order of several items. 
* 
* <P>A number of unique items are presented in a list. The user changes the order of 
* items in the list by first selecting an item, and then clicking an up or 
* down button. Items are never added to, or removed from, the list. 
*
* <P>Button behavior: 
* <ul>
* <li>The up button is placed above the down button.
* <li>Both buttons are enabled only if there is an item selected in the list.
* <li>When a button is selected, the highlighted item is moved, and
* retains the highlight; no action is taken if trying to move the first item up 
* or the last item down.
*</ul> 
*
* <p>This class is unusual in that it does not return what it was originally given. 
* The caller passes in a <tt>Set</tt>, which may or may not have a definite 
* order as specified by its <tt>Iterator</tt>.
* However, when the result of user interaction is passed back to the 
* caller, a <tt>Set</tt> with a definite iteration order is returned, corresponding to 
* the current order of appearance of items in the graphical interface.
* This is indeed the whole point of this 
* class: to impose an order on a <tt>Set</tt> of N unique items whose original 
* order is either undefined, or is indeed defined, but is to be changed by the end user.
*
* <P>This class is an example of defining a custom compound graphical component out of 
* standard Swing components.
*/
final class OrderEditor extends JPanel {

  /**
  * Construct with text buttons. 
  *  
  * @param aUpButtonText placed in the <tt>Up</tt> button, has 
  * visible content. 
  * @param aDownButtonText placed in the <tt>Down</tt> button, 
  * has visible content.
  */
  OrderEditor(String aUpButtonText, String aDownButtonText){
    Args.checkForContent(aUpButtonText);
    Args.checkForContent(aDownButtonText);
    fUpButtonText = aUpButtonText;
    fDownButtonText = aDownButtonText;
    buildGui();
  }

  /**
  * Construct with graphical icons.
  *  
  * @param aUpButtonIcon placed on the <tt>Up</tt> button.
  * @param aDownButtonIcon placed on the <tt>Down</tt> button.
  */
  OrderEditor(Icon aUpButtonIcon, Icon aDownButtonIcon){
    Args.checkForNull(aUpButtonIcon);
    Args.checkForNull(aDownButtonIcon);
    fUpButtonIcon = aUpButtonIcon;
    fDownButtonIcon = aDownButtonIcon;
    buildGui();
  }
  
  /**
  * Display <tt>aItems</tt> in this component.
  *
  * @param aItems is not empty and contains the items to be ordered; each item has a 
  * <tt>toString</tt> method whose result is suitable for display to the user.
  */
  void setItems(Set<Object> aItems) {
    /* 
    * Implementation Note :
    * <tt>aItems</tt> is not passed in the constructor, since callers often prefer
    * constructing a GUI in its entirety first, before populating the GUI with problem 
    * domain data. 
    */
    Args.checkForEmpty(aItems);
    fModel = new DefaultListModel();
    for(Object item : aItems){
      fModel.addElement(item);
    }
    fList.setModel(fModel);
  }

  /**
  * Return the <tt>aItems</tt> passed to {@link #setItems}
  * (having the same size and content as the original), but in a 
  * <tt>Set</tt> whose iteration order matches the order of items as 
  * currently presented to the user.
  */
  Set<Object> getItems(){
    /* Implementation Note :
     * The return value is Set, not LinkedHashSet. 
     * The guarantee for the iteration order of the return value is made in 
     * the javadoc, not by the return type. 
     * The callers of this method do not use any methods particular 
     * to LinkedHashSet, so it is not necessary to return that type.
     */
    Set<Object> result = new LinkedHashSet<Object>();
    List<?> elements = Collections.list(fModel.elements());
    for ( Object element : elements) {
      result.add(element);
    }
    assert result.size() == fModel.size();
    return result;
  }

  /**
  * Overridden in order to implement <tt>Label.setLabelFor</tt>. 
  * <P>When any label attached to this item is selected through its mnemonic, then the
  * list of items is selected, and the up and down arrow keys will alter the selection.
  */
  public void requestFocus(){
    /* 
    * Implementation Note :
    * Sun recommends that requestFocusInWindow be used instead of 
    * requestFocus, but this does not appear to be possible in this case.
    */
    fList.requestFocus();
  }

  // PRIVATE 
  private String fUpButtonText;
  private String fDownButtonText;
  private Icon fUpButtonIcon;
  private Icon fDownButtonIcon;

  /** Displays the items to be ordered.  */
  private JList fList;
  
  /**
  * An internal version of the items to be sorted. The user's original <tt>Set</tt>
  * is not touched by this class, such that the two may vary independently.
  * 
  * The <tt>Set</tt> of items passed in by the user is manipulated 
  * in a <tt>JList</tt>. Uniqueness constraints are maintained simply by never 
  * adding or removing from this <tt>JList</tt>.
  */
  private DefaultListModel fModel;
  
  private void buildGui(){
    setLayout( new BoxLayout(this, BoxLayout.X_AXIS) );

    fList = new JList();
    fList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    fList.setSelectedIndex(0);
    fList.setToolTipText ("Choose item, then move using buttons");
    add( Box.createHorizontalGlue() );
    add( new JScrollPane(fList) );
    add( Box.createHorizontalStrut(UiConsts.ONE_SPACE) ); 
    add( getButtons() );
    add( Box.createHorizontalGlue() );

    UiUtil.alignAllY(this, UiUtil.AlignY.CENTER );
  }
  
  /**
  * Return two buttons which allow the user to move a selected item up and down
  * the list. 
  */
  private JComponent getButtons() {
    Action upAction = (
      hasIcons() ? 
      new UpAction(fUpButtonIcon) : 
      new UpAction(fUpButtonText)
    );
    JButton up = new JButton(upAction);
    
    Action downAction = (
      hasIcons() ? 
      new DownAction(fDownButtonIcon) : 
      new DownAction(fDownButtonText)
    );
    JButton down = new JButton(downAction);

    java.util.List<JComponent> buttons = new ArrayList<>();
    buttons.add(up);
    buttons.add(down);
    JComponent result = UiUtil.getCommandColumn(buttons);
    result.setBorder(  BorderFactory.createEmptyBorder(0,0,0,0) );
    return result;
  }

  /**
  * Swap the selected element its predecessor; if there
  * is no predecessor, then do nothing.
  */
  private void swapWithPredecessor(){
    int selectedIdx = fList.getSelectedIndex();
    if ( selectedIdx == 0 ) return;
    
    int predecessorIdx = selectedIdx - 1;
    Object predecessor = fModel.getElementAt( predecessorIdx );
    Object mover = fModel.getElementAt( selectedIdx );
    fModel.setElementAt( mover, predecessorIdx );
    fModel.setElementAt( predecessor, selectedIdx );
    
    //keep the original element highlighted
    fList.setSelectedIndex(predecessorIdx);
  }

  /**
  * Swap the selected element with its successor; if there
  * is no successor, then do nothing.
  */
  private void swapWithSuccessor(){
    int selectedIdx = fList.getSelectedIndex();
    if ( selectedIdx== fModel.getSize() - 1 ) return;

    int successorIdx = selectedIdx + 1;
    Object successor = fModel.getElementAt( successorIdx );
    Object mover = fModel.getElementAt( selectedIdx );
    fModel.setElementAt( mover, successorIdx );
    fModel.setElementAt( successor, selectedIdx);
    
    //ensure the original element remains highlighted
    fList.setSelectedIndex( successorIdx );
  }
 
  /**
  * Return true only if the caller has used the icon version of the constructor.
  */
  private boolean hasIcons(){
    return (fUpButtonIcon != null);
  }
  
  private boolean isSelectionPresent() {
    return ! fList.getSelectionModel().isSelectionEmpty();
  }
  
  /**
  * Enforces the constraint that both the Up and Down buttons are enabled 
  * only if there is a selection made in the list. 
  */
  private abstract class EditAction extends AbstractAction implements ListSelectionListener{
    EditAction(String aText){
      super(aText);
      init();
    }
    EditAction(Icon aIcon){
      super(Consts.EMPTY_STRING, aIcon);
      init();
    }
    @Override public final void valueChanged(ListSelectionEvent event) {
      setEnabled( isSelectionPresent() );
    }
    private final void init(){
      setEnabled(false);
      fList.getSelectionModel().addListSelectionListener(this);
    }
  }
  
  private final class UpAction extends EditAction {
    UpAction(String aText){
      super(aText);
    }
    UpAction(Icon aIcon){
      super(aIcon);
    }
    @Override public void actionPerformed(ActionEvent event) {
      swapWithPredecessor();
    }
  }
  
  private final class DownAction extends EditAction {
    DownAction(String aText){
      super(aText);
    }
    DownAction(Icon aIcon){
      super(aIcon);
    }
    @Override public void actionPerformed(ActionEvent event) {
      swapWithSuccessor();
    }
  }
}
