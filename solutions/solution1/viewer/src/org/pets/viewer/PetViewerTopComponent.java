package org.pets.viewer;

import java.awt.BorderLayout;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@TopComponent.Description(
        preferredID = "PetViewerTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer",
        openAtStartup = true)
@NbBundle.Messages({
    "CTL_PetViewer=PetViewer Window",
    "HINT_PetViewer=This is a PetViewer window"
})
public class PetViewerTopComponent extends TopComponent implements ExplorerManager.Provider {

    private ExplorerManager em = new ExplorerManager();

    public PetViewerTopComponent() {
        setName(Bundle.CTL_PetViewer());
        setToolTipText(Bundle.HINT_PetViewer());
        setLayout(new BorderLayout());
        BeanTreeView btv = new BeanTreeView();
        btv.setRootVisible(false);
        add(btv, BorderLayout.CENTER);
        em.setRootContext(new AbstractNode(Children.create(new PetChildFactory(), true)));
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

}
