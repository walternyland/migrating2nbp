package hirondelle.stocks.main;

import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.portfolio.EditSaver;
import hirondelle.stocks.portfolio.PortfolioDAO;
import hirondelle.stocks.preferences.GeneralLookPreferencesEditor;
import hirondelle.stocks.preferences.LoggingPreferencesEditor;
import hirondelle.stocks.preferences.OptionPaneExceptionHandler;
import hirondelle.stocks.util.Consts;

import hirondelle.stocks.file.FileDeleteAction;
import hirondelle.stocks.file.FileSaveAction;
import hirondelle.stocks.file.FileSaveAsAction;
import hirondelle.stocks.file.FileNewAction;
import hirondelle.stocks.file.FileCloseAction;
import hirondelle.stocks.file.FileOpenAction;
import hirondelle.stocks.file.FileExitAction;
import hirondelle.stocks.export.ImportAction;
import hirondelle.stocks.export.ExportAction;
import hirondelle.stocks.quotes.FetchQuotesAction;
import hirondelle.stocks.help.HelpAction;
import hirondelle.stocks.help.AboutAction;
import hirondelle.stocks.portfolio.EditPortfolioAction;
import hirondelle.stocks.preferences.EditUserPreferencesAction;

import hirondelle.stocks.table.QuoteTable;
import hirondelle.stocks.preferences.QuoteTablePreferencesEditor;
import hirondelle.stocks.quotes.SummaryView;
import hirondelle.stocks.table.QuoteFilterFactory;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import hirondelle.stocks.preferences.PreferencesEditor;

import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.ui.Theme;
import hirondelle.stocks.util.ui.UiConsts;
import hirondelle.stocks.util.ui.UiUtil;

/**
 * Main window for this application.
 * 
 * <P>All services of this application are obtained through the GUI presented 
 * by this class. All other windows are dialogs attached to this class. As such, 
 * this class uses almost all the other classes in this package, either directly 
 * or indirectly.
 * <P>
 * The layout of the window is typical for a simple GUI application :
 * <ul>
 * <li>menu
 * <li>toolbar
 * <li>tree on left side (filters the rows of the quotes table)
 * <li>table and summary on right side (displays current quote data)
 * </ul>
 */
final class StocksMonitorMainWindow implements Observer {

  /*
   * Implementation Notes: This class uses many other classes in the app, 
   * and performs a lot of wiring.
   *  
   * In particular, note how the bulk of the work is delegated to the various 
   * Action classes. Actions allow the same object to be used in both the 
   * menu and toolbar. Note that some items are created and used locally, 
   * while others are stored as fields. The preference is to use a local 
   * variable if possible, and upgrade to a field only if there's a good reason. 
   * (This reason is usually being able to reference an item in separate 
   * methods.) This follows the principle of using the smallest possible scope.
   */

  /** Construct this application's main window.  */
  public StocksMonitorMainWindow() {
    /*
    * Implementation Note: There are strong order dependencies in these method calls. 
    * For example, Action objects need to be built before they can be used to build 
    * the menu and toolbar.
    */
    initCurrentPortfolio();
    initGuiPieces();
    initFrame();
    initActions();
    initMainGui();
    broadcastCurrentPortfolioUpdate();
    showMainWindow();
    OptionPaneExceptionHandler.attachToRootLogger();
  }

  /**
  * Synchronize the state of this window with the state of the {@link CurrentPortfolio},
  * or with {@link GeneralLookPreferencesEditor} for the general look-and-feel of the
  * application.
  * 
  * <P>The name of the <tt>CurrentPortfolio</tt> is reflected in the title bar. As
  * well, a trailing asterisk will indicate that there are unsaved edits.
  */
  @Override public void update(Observable aObservable, Object aData) {
    fLogger.fine("Notify being broadcast...");
    if (aObservable == fCurrentPortfolio) {
      fLogger.fine("Notified by Current Portfolio...");
      synchTitleBarWithCurrentPortfolio();
    }
    else if (aObservable == fGeneralLookPrefs) {
      fLogger.fine("Notified by General Look...");
      synchGuiWithGeneralLookPrefs();
    }
  }

  // PRIVATE

  /** Principal window for this application.  */
  private JFrame fFrame;

  /**
  * Current user selection for the set of {@link hirondelle.stocks.quotes.Stock} objects 
  * for which quotes are to be displayed.
  */
  private CurrentPortfolio fCurrentPortfolio;

  /**
  * Allows end user to edit preferences for the general look and feel of the 
  * application, and allows programmatic, read-only access to these preferences as well.
  */
  private GeneralLookPreferencesEditor fGeneralLookPrefs;

  /** Table to display the latest quote information. */
  private QuoteTable fQuoteTable;

  /**
  * Allows end user to edit preferences for {@link #fQuoteTable}, and allows 
  * programmatic read-only access to these preferences as well.
  */
  private QuoteTablePreferencesEditor fQuoteTablePrefsEditor;

  /**
  * Returns implementations of {@link hirondelle.stocks.table.QuoteFilter} used to filter the
  * rows of {@link #fQuoteTable}.
  */
  private QuoteFilterFactory fQuoteFilterFactory;

  /**
  * Displays summary financial information for the items presented in {@link #fQuoteTable}.
  */
  private SummaryView fSummaryView;
  
  private Action fExitAction;
  private Action fEditUserPreferencesAction;
  private FetchQuotesAction fFetchQuotesAction;
  private Action fFileNewAction;
  private Action fFileOpenAction;
  private Action fFileCloseAction;
  private Action fFileSaveAction;
  private Action fFileSaveAsAction;
  private Action fFileDeleteAction;
  private JToolBar fToolbar;

  /**
  * The preferred size of the panel which contains both fQuoteTable and fSummaryView.
  * Determines the overall size of the main window. If this was not provided, then the main
  * window would appear a bit too large, and on Windows the bottom of the window would
  * be cut off by the task bar.
  */
  private static final Dimension MAIN_PANEL_SIZE = new Dimension(400, 300);
  private static final String ICON = "favicon.gif";
  private static final Logger fLogger = Util.getLogger(StocksMonitorMainWindow.class);

  private void initCurrentPortfolio() {
    fLogger.info("Initializing the current portfolio");
    PortfolioDAO dao = new PortfolioDAO();
    fCurrentPortfolio = new CurrentPortfolio(dao.fetchDefaultPortfolio(),
    CurrentPortfolio.NeedsSave.FALSE);
    fCurrentPortfolio.addObserver(this);
  }

  /** Init important pieces of the GUI, from which the main window will be built.*/
  private void initGuiPieces() {
    fLogger.info("Initializing main pieces of the GUI.");
    fQuoteFilterFactory = new QuoteFilterFactory(fCurrentPortfolio);
    fSummaryView = new SummaryView(fCurrentPortfolio, fQuoteFilterFactory);
    fQuoteTablePrefsEditor = new QuoteTablePreferencesEditor();
    fQuoteTable = new QuoteTable(fQuoteTablePrefsEditor, fQuoteFilterFactory);
    fGeneralLookPrefs = new GeneralLookPreferencesEditor();
    fGeneralLookPrefs.addObserver(this);
  }

  private void initFrame() {
    fFrame = new JFrame();
    // Note that this is *not* set ; see use of fExitAction below
    // fFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ImageIcon imageIcon = UiUtil.getImageIcon(ICON, this.getClass());
    fFrame.setIconImage(imageIcon.getImage());
    // use the default BorderLayout
  }

  private void initActions() {
    fLogger.info("Initializing Actions.");
    fFileDeleteAction = new FileDeleteAction(fCurrentPortfolio, fFrame);
    fFileSaveAction = new FileSaveAction(fCurrentPortfolio);
    fFileSaveAsAction = new FileSaveAsAction(fCurrentPortfolio, fFrame);
    EditSaver editSaver = new EditSaver(fFileSaveAction, fFileSaveAsAction, fFrame);
    fFileNewAction = new FileNewAction(fCurrentPortfolio, fFrame, editSaver);
    fFileOpenAction = new FileOpenAction(fCurrentPortfolio, fFrame, editSaver);
    fFileCloseAction = new FileCloseAction(fCurrentPortfolio, editSaver);

    java.util.List<PreferencesEditor> prefEditors = new ArrayList<>();
    prefEditors.add(fGeneralLookPrefs);
    prefEditors.add(new LoggingPreferencesEditor());
    prefEditors.add(fQuoteTablePrefsEditor);
    fEditUserPreferencesAction = new EditUserPreferencesAction(fFrame, prefEditors);

    fExitAction = new FileExitAction(fCurrentPortfolio, editSaver);

    fFetchQuotesAction = new FetchQuotesAction(
      fCurrentPortfolio, fQuoteTablePrefsEditor, fQuoteTable, fSummaryView
    );
    fFetchQuotesAction.startTimer();
  }

  private void initMainGui() {
    /*
    * Implementation Note:
    * Default tab order simply reflects the order in which items are
    * added to the container.
    */
    fLogger.info("Initializing larges pieces of the GUI.");
    JSplitPane splitPane = new JSplitPane(
      JSplitPane.HORIZONTAL_SPLIT, fQuoteFilterFactory, getStocksTableAndSummary()
    );
    splitPane.setOneTouchExpandable(true);
    fFrame.setJMenuBar(getMenuBar());
    fToolbar = getToolBar();
    fFrame.getContentPane().add(fToolbar, BorderLayout.NORTH);
    fFrame.getContentPane().add(splitPane, BorderLayout.CENTER);
    fFrame.addWindowListener(new WindowAdapter() {
      @Override public void windowClosing(WindowEvent e) {
        fExitAction.actionPerformed(null);
      }
    });
  }

  private void broadcastCurrentPortfolioUpdate() {
    fCurrentPortfolio.notifyObservers();
  }

  private JMenuBar getMenuBar() {
    JMenuBar menubar = new JMenuBar();

    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);
    fileMenu.add(fFileNewAction);
    fileMenu.add(fFileOpenAction);
    fileMenu.add(fFileCloseAction);
    fileMenu.addSeparator();
    fileMenu.add(fFileSaveAction);
    fileMenu.add(fFileSaveAsAction);
    fileMenu.add(fFileDeleteAction);
    fileMenu.addSeparator();
    fileMenu.add(fEditUserPreferencesAction);
    fileMenu.addSeparator();
    fileMenu.add(new ImportAction(fFrame));
    fileMenu.add(new ExportAction(fFrame));
    fileMenu.addSeparator();
    fileMenu.add(fExitAction);
    menubar.add(fileMenu);

    JMenu editMenu = new JMenu("Edit");
    editMenu.setMnemonic(KeyEvent.VK_E);
    editMenu.add(new EditPortfolioAction(fCurrentPortfolio, fFrame));
    editMenu.add(fFetchQuotesAction);
    menubar.add(editMenu);

    JMenu helpMenu = new JMenu("Help");
    helpMenu.setMnemonic(KeyEvent.VK_H);
    helpMenu.add(new HelpAction(
      fFrame, "Contents", KeyEvent.VK_C, null, HelpAction.View.CONTENTS)
    );
    helpMenu.add(new HelpAction(
      fFrame, "Index", KeyEvent.VK_I, null, HelpAction.View.INDEX)
    );
    helpMenu.add(
      new HelpAction(fFrame, "Search...", KeyEvent.VK_S, null, HelpAction.View.SEARCH)
    );
    helpMenu.add(new JSeparator());
    helpMenu.add(new AboutAction(fFrame));

    menubar.add(helpMenu);
    return menubar;
  }

  private JToolBar getToolBar() {
    JToolBar toolbar = new JToolBar();
    // toolbar.setFloatable(false);
    toolbar.setRollover(true);

    toolbar.add(fFileNewAction);
    toolbar.add(fFileOpenAction);
    toolbar.add(fFileSaveAction);
    toolbar.add(fFileSaveAsAction);
    toolbar.add(fFileDeleteAction);

    toolbar.addSeparator();
    toolbar.add(fFetchQuotesAction);

    // SWING BUG?: this toolbar button does not perform the same action
    // as the corresponding menu item (it fails with the cryptic message
    // "trouble in HelpActionListener").
    // toolbar.addSeparator();
    // toolbar.add( fHelpContentsAction );
    return toolbar;
  }

  private JComponent getStocksTableAndSummary() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.add(fQuoteTable);
    panel.add(Box.createVerticalStrut(UiConsts.ONE_SPACE));
    panel.add(fSummaryView);
    panel.setPreferredSize(MAIN_PANEL_SIZE);
    return panel;
  }

  private void showMainWindow() {
    fLogger.info("Showing the main window.");
    // For Themes with large fonts, the quote table column headers are not
    // being rendered properly upon startup: the color and font size are no different
    // from the defaults. This *may* be a Swing bug since all other items are
    // rendered correctly. In the case of Themes with large fonts, a repaint
    // is used to correct the problem.
    // (As a further data point, the column headers are correctly rendered
    // when the user changes Theme through the preferences dialog; thus,
    // the problem is only seen upon startup.)
    if (!Theme.hasLargeFont(fGeneralLookPrefs.getTheme())) {
      // no repaint occurs here
      synchGuiWithGeneralLookPrefs();
      UiUtil.centerAndShow(fFrame);
    }
    else {
      // forces a repaint to occur
      UiUtil.centerAndShow(fFrame);
      synchGuiWithGeneralLookPrefs();
    }
  }

  /**
   * Place text such as "StocksMonitor 1.0 - Portfolio Name: Blah *" in 
   * the title bar of the window. The trailing star denotes that the current 
   * portfolio has unsaved edits.
   */
  private void synchTitleBarWithCurrentPortfolio() {
    StringBuilder title = new StringBuilder(Consts.APP_NAME);
    title.append(Consts.SPACE);
    title.append(Consts.APP_VERSION);
    title.append(" - Portfolio : ");
    if (fCurrentPortfolio.isUntitled()) {
      title.append("(Untitled)");
    }
    else {
      title.append(fCurrentPortfolio.getName());
    }
    if (fCurrentPortfolio.getNeedsSave()) {
      title.append(" *");
    }
    fFrame.setTitle(title.toString());
  }

  private void synchGuiWithGeneralLookPrefs() {
    if (fGeneralLookPrefs.hasShowToolBar()) {
      fToolbar.setVisible(true);
    }
    else {
      /*
       * Implementation Note If the toolbar is made invisible while it still has 
       * the focus, then bad things happen : the user cannot immediately 
       * access the menu through the keyboard, nor does the TAB key 
       * change the focus. So, it is important that the toolbar give up 
       * the focus before it is made invisible. Here, this is done by having
       * the tree request focus.
       */
      fQuoteFilterFactory.requestFocusInWindow();
      fToolbar.setVisible(false);
    }

    // icon sizing: there is apparently no simple way to
    // change icon sizes. If the components of the toolbar are obtained, as in
    // java.util.List toolbarItems = Arrays.asList( fToolbar.getComponents() );
    // Iterator iter = toolbarItems.iterator();
    // while ( iter.hasNext() ) {
    //   AbstractButton button = (AbstractButton)iter.next();
    // }
    // then there is no "handle" for the image URL or file name.
    // Icon does not have it.
    // ImageIcon keeps URL and filename as fields, but there is
    // no corresponding get/set.

    MetalTheme preferredTheme = fGeneralLookPrefs.getTheme();
    MetalLookAndFeel.setCurrentTheme(preferredTheme);
    try {
      UIManager.setLookAndFeel(new MetalLookAndFeel());
    }
    catch (UnsupportedLookAndFeelException ex) {
      fLogger.severe("Cannot set new Theme for Java Look and Feel.");
    }

    SwingUtilities.updateComponentTreeUI(fFrame);
  }
}
