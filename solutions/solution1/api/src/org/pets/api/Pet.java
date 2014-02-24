package org.pets.api;

public class Pet {

    String name;
    String pathToImage;

    public Pet(String name, String pathToImage) {
        this.name = name;
        this.pathToImage = pathToImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPathToImage() {
        return pathToImage;
    }

    public void setPathToImage(String pathToImage) {
        this.pathToImage = pathToImage;
    }
    
}
