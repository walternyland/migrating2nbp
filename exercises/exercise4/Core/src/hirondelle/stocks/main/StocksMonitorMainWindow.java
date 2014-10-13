package hirondelle.stocks.main;

import hirondelle.stocks.portfolio.CurrentPortfolio;
import hirondelle.stocks.portfolio.PortfolioDAO;
import hirondelle.stocks.preferences.GeneralLookPreferencesEditor;
import hirondelle.stocks.preferences.LoggingPreferencesEditor;
import hirondelle.stocks.preferences.OptionPaneExceptionHandler;
import hirondelle.stocks.quotes.FetchQuotesAction;
import hirondelle.stocks.table.QuoteTable;
import hirondelle.stocks.preferences.QuoteTablePreferencesEditor;
import hirondelle.stocks.quotes.SummaryView;
import hirondelle.stocks.table.QuoteFilterFactory;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import hirondelle.stocks.preferences.PreferencesEditor;

import hirondelle.stocks.util.Util;
import hirondelle.stocks.util.ui.UiConsts;
import org.openide.modules.OnStart;
import org.openide.util.lookup.ServiceProvider;

/**
 * Main window for this application.
 *
 * <P>
 * All services of this application are obtained through the GUI presented by
 * this class. All other windows are dialogs attached to this class. As such,
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
@OnStart
public final class StocksMonitorMainWindow implements Runnable{

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
    /**
     * Construct this application's main window.
     */
    @Override
    public void run() {
        /*
         * Implementation Note: There are strong order dependencies in these method calls. 
         * For example, Action objects need to be built before they can be used to build 
         * the menu and toolbar.
         */
        initCurrentPortfolio();
        initGuiPieces();
        initActions();
        OptionPaneExceptionHandler.attachToRootLogger();
    }
    
    
    /**
     * Current user selection for the set of
     * {@link hirondelle.stocks.quotes.Stock} objects for which quotes are to be
     * displayed.
     */
    private CurrentPortfolio fCurrentPortfolio;

    /**
     * Allows end user to edit preferences for the general look and feel of the
     * application, and allows programmatic, read-only access to these
     * preferences as well.
     */
    private GeneralLookPreferencesEditor fGeneralLookPrefs;

    /**
     * Table to display the latest quote information.
     */
    private QuoteTable fQuoteTable;

    /**
     * Allows end user to edit preferences for {@link #fQuoteTable}, and allows
     * programmatic read-only access to these preferences as well.
     */
    private QuoteTablePreferencesEditor fQuoteTablePrefsEditor;

    /**
     * Returns implementations of {@link hirondelle.stocks.table.QuoteFilter}
     * used to filter the rows of {@link #fQuoteTable}.
     */
    private QuoteFilterFactory fQuoteFilterFactory;

    /**
     * Displays summary financial information for the items presented in
     * {@link #fQuoteTable}.
     */
    private SummaryView fSummaryView;

    private FetchQuotesAction fFetchQuotesAction;

    /**
     * The preferred size of the panel which contains both fQuoteTable and
     * fSummaryView. Determines the overall size of the main window. If this was
     * not provided, then the main window would appear a bit too large, and on
     * Windows the bottom of the window would be cut off by the task bar.
     */
    private static final Logger fLogger = Util.getLogger(StocksMonitorMainWindow.class);

    private void initCurrentPortfolio() {
        fLogger.info("Initializing the current portfolio");
        PortfolioDAO dao = new PortfolioDAO();
        fCurrentPortfolio = new CurrentPortfolio(dao.fetchDefaultPortfolio(),
                CurrentPortfolio.NeedsSave.FALSE);
    }

    /**
     * Init important pieces of the GUI, from which the main window will be built.
     */
    private void initGuiPieces() {
        fLogger.info("Initializing main pieces of the GUI.");
        fQuoteFilterFactory = new QuoteFilterFactory(fCurrentPortfolio);
        fSummaryView = new SummaryView(fCurrentPortfolio, fQuoteFilterFactory);
        fQuoteTablePrefsEditor = new QuoteTablePreferencesEditor();
        fQuoteTable = new QuoteTable(fQuoteTablePrefsEditor, fQuoteFilterFactory);
        fGeneralLookPrefs = new GeneralLookPreferencesEditor();
    }

    private void initActions() {
        fLogger.info("Initializing Actions.");
        java.util.List<PreferencesEditor> prefEditors = new ArrayList<>();
        prefEditors.add(fGeneralLookPrefs);
        prefEditors.add(new LoggingPreferencesEditor());
        prefEditors.add(fQuoteTablePrefsEditor);
        CentralLookup.getDefault().add(prefEditors);
        CentralLookup.getDefault().add(fCurrentPortfolio);
        CentralLookup.getDefault().add(fQuoteTablePrefsEditor);
        CentralLookup.getDefault().add(fQuoteTable);
        CentralLookup.getDefault().add(fSummaryView);
        CentralLookup.getDefault().add(fQuoteFilterFactory);
        fFetchQuotesAction = new FetchQuotesAction();
        fFetchQuotesAction.startTimer();
    }

}
