package hirondelle.stocks.table;

import javax.swing.table.*;
import javax.swing.*;
import java.text.NumberFormat;

/**
* Display a <tt>Number</tt> in a table cell in the format defined by  
* {@link NumberFormat#getCurrencyInstance()}, and aligned to the right.
*/
final class RenderPrice extends DefaultTableCellRenderer {
  
  RenderPrice() { 
    setHorizontalAlignment(SwingConstants.RIGHT);  
  }
  
  @Override public void setValue(Object aValue) {
    Object result = aValue;
    if ((aValue != null) && (aValue instanceof Number)) {
      Number numberValue = (Number)aValue;
      NumberFormat formatter = NumberFormat.getCurrencyInstance();
      result = formatter.format(numberValue.doubleValue());
    } 
    super.setValue(result);
  }   
}