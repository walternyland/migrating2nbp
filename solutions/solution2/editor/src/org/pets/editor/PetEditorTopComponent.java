package org.pets.editor;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.pets.api.Pet;
@TopComponent.Description(
        preferredID = "PetEditorTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "editor",
        openAtStartup = true)
@NbBundle.Messages({
    "CTL_PetEditor=PetEditor Window",
    "HINT_PetEditor=This is a PetEditor window"
})
public class PetEditorTopComponent extends TopComponent implements LookupListener {
    private Result<Pet> petResult;
    private JLabel petLabel;
    public PetEditorTopComponent() {
        setName(Bundle.CTL_PetEditor());
        setToolTipText(Bundle.HINT_PetEditor());
        setLayout(new BorderLayout());
        add(petLabel = new JLabel(), BorderLayout.CENTER);
    }
    @Override
    public void resultChanged(LookupEvent le) {
        if (petResult.allInstances().iterator().hasNext()) {
            Pet pet = petResult.allInstances().iterator().next();
            petLabel.setIcon(pet.getImage());
        }
    }
    @Override
    protected void componentOpened() {
        petResult = Utilities.actionsGlobalContext().lookupResult(Pet.class);
        petResult.addLookupListener(this);
    }
    @Override
    protected void componentClosed() {
        petResult.removeLookupListener(this);
    }
}
