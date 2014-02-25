package org.pets.viewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
@ActionID(
        category = "Edit",
        id = "org.pets.viewer.SomeAction"
)
@ActionRegistration(
        iconBase = "org/pets/viewer/Import.png",
        displayName = "#CTL_SomeAction"
)
@ActionReference(path = "Menu/File", position = 1300)
@Messages("CTL_SomeAction=Some")
public final class SomeAction implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO implement action body
    }
}
