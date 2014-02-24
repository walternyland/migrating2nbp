package org.pets.viewer;

import java.beans.IntrospectionException;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.pets.api.Pet;

public class PetChildFactory extends ChildFactory<Pet>{

    @Override
    protected boolean createKeys(List<Pet> list) {
        list.add(new Pet("Bird","org/pets/viewer/images/Bird.gif"));
        list.add(new Pet("Cat","org/pets/viewer/Bird.gif"));
        list.add(new Pet("Dog","org/pets/viewer/Dog.gif"));
        list.add(new Pet("Rabbit","org/pets/viewer/Rabbit.gif"));
        list.add(new Pet("Pig","org/pets/viewer/Pig.gif"));
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
