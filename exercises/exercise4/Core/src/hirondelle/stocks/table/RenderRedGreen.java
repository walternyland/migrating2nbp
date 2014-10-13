package hirondelle.stocks.table;

import javax.swing.table.*;
import javax.swing.*;
import java.awt.*;

/**
* Display a <tt>Number</tt> in a table cell as either red (for negative values)
* or green (for non-negative values), and aligned on the right.
*
* <P>Note that this class will work with any <tt>Number</tt> -  
* <tt>Double</tt>, <tt>BigDecimal</tt>, etc.
*/
final class RenderRedGreen extends DefaultTableCellRenderer {
  
  RenderRedGreen () {
    setHorizontalAlignment(SwingConstants.RIGHT);   
  }
  
  @Override public Component getTableCellRendererComponent(
    JTable aTable, Object aNumberValue, boolean aIsSelected, 
    boolean aHasFocus, int aRow, int aColumn
  ) {  
    /* 
    * Implementation Note :
    * It is important that no 'new' objects be present in this 
    * implementation (excluding exceptions):
    * if the table is large, then a large number of objects would be 
    * created during rendering.
    */
    if (aNumberValue == null) return this;
    Component renderer = super.getTableCellRendererComponent(
      aTable, aNumberValue, aIsSelected, aHasFocus, aRow, aColumn
    );
    Number value = (Number)aNumberValue;
    if (value.doubleValue() < 0) {
      renderer.setForeground(Color.red);
    }
    else {
      renderer.setForeground(fDarkGreen);
    }
    return this;
  }
  
  // PRIVATE 
  
  //the default green is too bright and illegible
  private Color fDarkGreen = Color.green.darker();
}