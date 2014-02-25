package org.pets.viewer;
import java.beans.IntrospectionException;
import java.net.URL;
import java.util.List;
import javax.swing.ImageIcon;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.pets.api.Pet;
public class PetChildFactory extends ChildFactory<Pet> {
    @Override
    protected boolean createKeys(List<Pet> list) {
        FileObject petsFolder = FileUtil.getConfigFile("pets");
        FileObject[] petFiles = petsFolder.getChildren();
        for (FileObject petFile : petFiles) {
            URL url = URLMapper.findURL(petFile, URLMapper.INTERNAL);
            if (url != null) {
                String name = petFile.getName();
                ImageIcon image = new javax.swing.ImageIcon(url);
                list.add(new Pet(name, image));
            }
        }
//        list.add(new Pet("Bird","org/pets/viewer/images/Bird.gif"));
//        list.add(new Pet("Cat","org/pets/viewer/images/Cat.gif"));
//        list.add(new Pet("Dog","org/pets/viewer/images/Dog.gif"));
//        list.add(new Pet("Rabbit","org/pets/viewer/images/Rabbit.gif"));
//        list.add(new Pet("Pig","org/pets/viewer/images/Pig.gif"));
        return true;
    }
    @Override
    protected Node createNodeForKey(Pet key) {
        Node pn = null;
        try {
            pn = new PetNode(key);
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return pn;
    }
}
