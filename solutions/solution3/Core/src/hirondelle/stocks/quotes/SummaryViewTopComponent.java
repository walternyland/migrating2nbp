package hirondelle.stocks.quotes;

import hirondelle.stocks.main.*;
import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@TopComponent.Description(
        preferredID = "SummaryViewTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = true,
        position = 10)
@ActionID(
        category = "Window",
        id = "hirondelle.stocks.main.SummaryViewTopComponent")
@ActionReference(
        path = "Menu/Window",
        position = 20)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SummaryViewAction",
        preferredID = "SummaryViewTopComponent"
)
@Messages({
    "CTL_SummaryViewAction=Summary View",
    "CTL_SummaryViewTopComponent=Summary View"
})
public class SummaryViewTopComponent extends TopComponent {
    public SummaryViewTopComponent() {
        setName(Bundle.CTL_SummaryViewTopComponent());
        setLayout(new BorderLayout());
        add(getSummaryView(), BorderLayout.CENTER);
    }
    public final SummaryView getSummaryView() {
        return CentralLookup.getDefault().lookup(SummaryView.class);
    }
}
