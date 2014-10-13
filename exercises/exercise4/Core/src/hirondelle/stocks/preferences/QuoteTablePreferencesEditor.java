package hirondelle.stocks.preferences;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;

import hirondelle.stocks.table.QuoteField;
import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.ui.UiConsts;
import hirondelle.stocks.util.ui.UiUtil;

/**
* Graphical component allows editing of user preferences related to the 
* {@link hirondelle.stocks.table.QuoteTable},
* and programmatic read-only access to these preferences.
*/
public final class QuoteTablePreferencesEditor extends Observable 
  implements PreferencesEditor {

  @Override public JComponent getUI() {
    JPanel content = new JPanel();
    GridBagLayout gridbag = new GridBagLayout();
    content.setLayout(gridbag);
    addSortField(content);
    addHorizontalVerticalLines(content);
    addRowHeight(content);
    addUpdateFrequency(content);
    addColumnOrder(content);
    addRestore(content);
    matchGuiToStoredPrefs();
    return content;
  }

  @Override public String getTitle() {
    return TITLE;
  }

  @Override public int getMnemonic() {
    return MNEMONIC;
  }

  @Override public void savePreferences() {
    fLogger.fine("Updating table preferences.");
    fPrefs.putBoolean(HORIZONTAL_LINES_KEY, fHorizontalLines.isSelected());
    fPrefs.putBoolean(VERTICAL_LINES_KEY, fVerticalLines.isSelected());
    fPrefs.putInt(ROW_HEIGHT_KEY, fRowSizeModel.getNumber().intValue());
    fPrefs.put(SORT_FIELD_KEY, fSortField.getSelectedItem().toString());
    fPrefs.put(COLUMN_ORDER_KEY, fColumnOrderEditor.getItems().toString());
    fPrefs.putInt(UPDATE_FREQ_KEY, fUpdateFreqModel.getNumber().intValue());
    setChanged();
    notifyObservers();
  }

  @Override public void matchGuiToDefaultPreferences() {
    fHorizontalLines.setSelected(HORIZONTAL_LINES_DEFAULT);
    fVerticalLines.setSelected(VERTICAL_LINES_DEFAULT);
    fRowSizeModel.setValue(new Integer(ROW_HEIGHT_DEFAULT));
    fSortField.setSelectedItem(QuoteField.valueFrom(SORT_FIELD_DEFAULT));
    fColumnOrderEditor.setItems(parseRawColumnOrder(COLUMN_ORDER_DEFAULT));
    fUpdateFreqModel.setValue(new Integer(UPDATE_FREQ_DEFAULT));
  }

  /**
  * Return preference for the display of horizontal lines for each row.
  */
  public boolean hasHorizontalLines() {
    return fPrefs.getBoolean(HORIZONTAL_LINES_KEY, HORIZONTAL_LINES_DEFAULT);
  }

  /**
  * Return preference for the display of vertical lines for each column.
  */
  public boolean hasVerticalLines() {
    return fPrefs.getBoolean(VERTICAL_LINES_KEY, VERTICAL_LINES_DEFAULT);
  }

  /**
   * Return the height of each row in pixels, in the range <tt>16..32</tt>
   * (inclusive).
   */
  public int getRowHeight() {
    return fPrefs.getInt(ROW_HEIGHT_KEY, ROW_HEIGHT_DEFAULT);
  }

  /**
   * Return a field identifier, but no ascending-descending indicator.
   */
  public QuoteField getSortField() {
    String fieldName = fPrefs.get(SORT_FIELD_KEY, SORT_FIELD_DEFAULT);
    return QuoteField.valueFrom(fieldName);
  }

  /**
   * Return <tt>Set</tt> of {@link QuoteField} objects, whose
   * iteration order reflects the user's preferred column order.
   */
  public Set<Object> getColumnOrder() {
    return parseRawColumnOrder(fPrefs.get(COLUMN_ORDER_KEY, COLUMN_ORDER_DEFAULT));
  }

  /**
   * Return the number of minutes to wait between periodic updates, in the range
   * <tt>1..60</tt>.
   */
  public int getUpdateFrequency() {
    return fPrefs.getInt(UPDATE_FREQ_KEY, UPDATE_FREQ_DEFAULT);
  }

  // PRIVATE
  private static final String TITLE = "Quote Table";
  private static final int MNEMONIC = KeyEvent.VK_Q;
  private static final String STOCKS_TABLE_NODE_NAME = 
    "stocksmonitor/ui/prefs/StocksTable"
  ;
  private static final boolean HORIZONTAL_LINES_DEFAULT = true;
  private static final String HORIZONTAL_LINES_KEY = "HorizontalLines";
  private static final boolean VERTICAL_LINES_DEFAULT = true;
  private static final String VERTICAL_LINES_KEY = "VerticalLines";
  private static final int MAX_ROW_HEIGHT = 32;
  private static final int MIN_ROW_HEIGHT = 16;
  private static final int INITIAL_ROW_HEIGHT = MIN_ROW_HEIGHT;
  private static final int STEP_SIZE = 1;
  private static final int ROW_HEIGHT_DEFAULT = MIN_ROW_HEIGHT;
  private static final String ROW_HEIGHT_KEY = "RowHeight";
  private static final String SORT_FIELD_DEFAULT = "Stock";
  private static final String SORT_FIELD_KEY = "SortBy";
  // This preference is unusual in that in needs a bit of parsing
  private static final String COLUMN_ORDER_DEFAULT = 
    "[Stock, Price, Change, %Change, Profit, %Profit]"
  ;
  private static final String COLUMN_ORDER_KEY = "ColumnOrder";
  private static final int MAX_UPDATE_FREQ = 60;
  private static final int MIN_UPDATE_FREQ = 1;
  private static final int UPDATE_FREQ_DEFAULT = 1; // seconds
  private static final String UPDATE_FREQ_KEY = "UpdateFrequency";

  private Preferences fPrefs = Preferences.userRoot().node(STOCKS_TABLE_NODE_NAME);
  private JCheckBox fHorizontalLines;
  private JCheckBox fVerticalLines;
  private SpinnerNumberModel fRowSizeModel;
  private JComboBox<QuoteField> fSortField;
  private OrderEditor fColumnOrderEditor;
  private SpinnerNumberModel fUpdateFreqModel;

  private static final Logger fLogger = Util.getLogger(QuoteTablePreferencesEditor.class);

  private void addSortField(JPanel aContent) {
    JLabel sortBy = new JLabel("Sort By:");
    sortBy.setDisplayedMnemonic(KeyEvent.VK_S);
    sortBy.setToolTipText("Always descending order");
    aContent.add(sortBy, getConstraints(0, 0));
    DefaultComboBoxModel<QuoteField>sortByModel = new DefaultComboBoxModel<QuoteField>(
      QuoteField.values()
    );
    fSortField = new JComboBox<QuoteField>(sortByModel);
    sortBy.setLabelFor(fSortField);
    aContent.add(fSortField, getConstraints(0, 1));
  }

  private void addHorizontalVerticalLines(JPanel aContent) {
    JLabel show = new JLabel("Show:");
    aContent.add(show, getConstraints(1, 0));

    fHorizontalLines = new JCheckBox("Horizontal Lines");
    fHorizontalLines.setMnemonic(KeyEvent.VK_H);
    aContent.add(fHorizontalLines, getConstraints(1, 1));

    fVerticalLines = new JCheckBox("Vertical Lines");
    fVerticalLines.setMnemonic(KeyEvent.VK_V);
    aContent.add(fVerticalLines, getConstraints(1, 2, 2, 1));
  }

  private void addRowHeight(JPanel aContent) {
    JLabel rowHeight = new JLabel("Row Height:");
    rowHeight.setDisplayedMnemonic(KeyEvent.VK_R);
    rowHeight.setToolTipText("Height in pixels of table rows");
    aContent.add(rowHeight, getConstraints(2, 0));
    fRowSizeModel = new SpinnerNumberModel(
      INITIAL_ROW_HEIGHT, MIN_ROW_HEIGHT, MAX_ROW_HEIGHT, STEP_SIZE
    );
    JSpinner rowHeightSelector = new JSpinner(fRowSizeModel);
    rowHeight.setLabelFor(rowHeightSelector);
    aContent.add(rowHeightSelector, getConstraints(2, 1));
  }

  private void addUpdateFrequency(JPanel aContent) {
    JLabel updateFreq = new JLabel("Update Freq:");
    updateFreq.setDisplayedMnemonic(KeyEvent.VK_U);
    updateFreq.setToolTipText("Refresh interval in minutes");
    aContent.add(updateFreq, getConstraints(2, 2));

    int initialFreqValue = MIN_UPDATE_FREQ;
    fUpdateFreqModel = new SpinnerNumberModel(
      initialFreqValue, MIN_UPDATE_FREQ, MAX_UPDATE_FREQ, 1
    );
    JSpinner updateFreqSelector = new JSpinner(fUpdateFreqModel);
    updateFreq.setLabelFor(updateFreqSelector);
    aContent.add(updateFreqSelector, getConstraints(2, 3));
  }

  private void addColumnOrder(JPanel aContent) {
    JLabel columnOrder = new JLabel("Column Order:");
    columnOrder.setDisplayedMnemonic(KeyEvent.VK_C);
    GridBagConstraints columnOrderConstraints = getConstraints(3, 0);
    columnOrderConstraints.anchor = GridBagConstraints.NORTH;
    aContent.add(columnOrder, columnOrderConstraints);

    fColumnOrderEditor = new OrderEditor("Up", "Down");
    // Demonstrates alternate ctor using icons:
    // fColumnOrderEditor = new OrderEditor(
    // UiUtil.getImageIcon("/toolbarButtonGraphics/navigation/Up"),
    // UiUtil.getImageIcon("/toolbarButtonGraphics/navigation/Down"),
    // UiConsts.ONE_SPACE * 15 );
    columnOrder.setLabelFor(fColumnOrderEditor);
    GridBagConstraints orderEditorConstraints = UiUtil.getConstraints(3, 1);
    orderEditorConstraints.insets = new Insets(UiConsts.ONE_SPACE, 0, 0, 0);
    aContent.add(fColumnOrderEditor, orderEditorConstraints);
  }

  private void addRestore(JPanel aContent) {
    JButton restore = new JButton("Restore Defaults");
    restore.setMnemonic(KeyEvent.VK_D);
    restore.addActionListener(new ActionListener() {
      @Override public void actionPerformed(ActionEvent event) {
        matchGuiToDefaultPreferences();
      }
    });
    GridBagConstraints constraints = UiUtil.getConstraints(3, 2, 2, 1);
    constraints.anchor = GridBagConstraints.SOUTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    aContent.add(restore, constraints);
  }

  /**
   * Return a <tt>Set</tt> of {@link QuoteField} objects, whose
   * iteration order corresponds to the preferred column order.
   */
  private Set<Object> parseRawColumnOrder(String aRawColumnOrderPref) {
    java.util.List<String> columnNames = Util.getListFromString(aRawColumnOrderPref);
    Set<Object> result = new LinkedHashSet<Object>();
    for(String fieldName: columnNames){
      result.add(QuoteField.valueFrom(fieldName));
    }
    assert (result.size() == QuoteField.values().length);
    return result;
  }

  private void matchGuiToStoredPrefs() {
    fHorizontalLines.setSelected(hasHorizontalLines());
    fVerticalLines.setSelected(hasVerticalLines());
    fRowSizeModel.setValue(new Integer(getRowHeight()));
    fSortField.setSelectedItem(getSortField());
    fColumnOrderEditor.setItems(getColumnOrder());
    fUpdateFreqModel.setValue(new Integer(getUpdateFrequency()));
  }

  private GridBagConstraints getConstraints(int aY, int aX) {
    GridBagConstraints result = UiUtil.getConstraints(aY, aX);
    addBottom(result);
    return result;
  }

  private GridBagConstraints getConstraints(int aY, int aX, int aWidth, int aHeight) {
    GridBagConstraints result = UiUtil.getConstraints(aY, aX, aWidth, aHeight);
    addBottom(result);
    return result;
  }

  private void addBottom(GridBagConstraints aConstraints) {
    aConstraints.insets = new Insets(0, 0, UiConsts.ONE_SPACE, UiConsts.ONE_SPACE);
  }

  /**
   * Developer tool for removing old preferences and restarting from the beginning.
   */
  private static void main(String... aArgs) {
    QuoteTablePreferencesEditor thing = new QuoteTablePreferencesEditor();
    try {
      thing.fPrefs.removeNode();
    }
    catch (BackingStoreException ex) {
      fLogger.severe("Cannot access preferences.");
    }
    fLogger.severe("Done.");
  }
}
