package org.pets.api;
import javax.swing.ImageIcon;
public class Pet {
    String name;
    ImageIcon image;
    public Pet(String name, ImageIcon image) {
        this.name = name;
        this.image = image;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ImageIcon getImage() {
        return image;
    }
    public void setImage(ImageIcon image) {
        this.image = image;
    }
}
