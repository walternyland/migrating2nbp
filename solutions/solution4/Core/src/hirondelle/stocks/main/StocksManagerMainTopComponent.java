package hirondelle.stocks.main;

import hirondelle.stocks.table.QuoteTable;
import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@TopComponent.Description(
        preferredID = "StocksMonitorMainTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "editor",
        openAtStartup = true,
        position = 10)
@ActionID(
        category = "Window",
        id = "hirondelle.stocks.main.StocksMonitorMainTopComponent")
@ActionReference(
        path = "Menu/Window",
        position = 10)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_StocksMonitorMainAction",
        preferredID = "StocksMonitorMainTopComponent"
)
@Messages({
    "CTL_StocksMonitorMainAction=Stocks Monitor",
    "CTL_StocksMonitorMainTopComponent=Stocks Monitor"
})
public class StocksManagerMainTopComponent extends TopComponent {
    public StocksManagerMainTopComponent() {
        setName(Bundle.CTL_StocksMonitorMainTopComponent());
        setLayout(new BorderLayout());
        add(getStocksTable(), BorderLayout.CENTER);
    }
    public final QuoteTable getStocksTable() {
        return CentralLookup.getDefault().lookup(QuoteTable.class);
    }
}
