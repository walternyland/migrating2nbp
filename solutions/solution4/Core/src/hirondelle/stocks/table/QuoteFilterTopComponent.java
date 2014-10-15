package hirondelle.stocks.table;

import hirondelle.stocks.main.*;
import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@TopComponent.Description(
        preferredID = "QuoteFilterFactoryTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = true,
        position = 10)
@ActionID(
        category = "Window",
        id = "hirondelle.stocks.main.QuoteFilterFactoryTopComponent")
@ActionReference(
        path = "Menu/Window",
        position = 30)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_QuoteFilterFactoryAction",
        preferredID = "QuoteFilterFactoryTopComponent"
)
@Messages({
    "CTL_QuoteFilterFactoryAction=Quote Filter",
    "CTL_QuoteFilterFactoryTopComponent=Quote Filter"
})
public class QuoteFilterTopComponent extends TopComponent {
    public QuoteFilterTopComponent() {
        setName(Bundle.CTL_QuoteFilterFactoryTopComponent());
        setLayout(new BorderLayout());
        add(getQuoteFilterFactory(), BorderLayout.CENTER);
    }
    public final QuoteFilterFactory getQuoteFilterFactory() {
        return CentralLookup.getDefault().lookup(QuoteFilterFactory.class);
    }
}