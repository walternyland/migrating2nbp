package org.pets.viewer;

import java.beans.IntrospectionException;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;
import org.pets.api.Pet;

public class PetNode extends BeanNode {

    public PetNode(Pet bean) throws IntrospectionException {
        super(bean, Children.LEAF, Lookups.singleton(bean));
    }
    
}
