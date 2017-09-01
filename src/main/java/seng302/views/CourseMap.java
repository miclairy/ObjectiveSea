package seng302.views;

import javafx.scene.image.Image;

public class CourseMap {

    private String mapName;
    private String imageLocation;
    private Image image;
    private Integer numberOfMarks;
    private String estTimeToRace;

    public CourseMap(String mapName, String imageLocation, Integer numberOfMarks, String estTimeToRace) {
        this.mapName = mapName;
        this.imageLocation = imageLocation;
        this.image = new Image(imageLocation);
        this.numberOfMarks = numberOfMarks;
        this.estTimeToRace = estTimeToRace;
    }

    public String getMapName() {
        return mapName;
    }

    public String getImageLocation() {
        return imageLocation;
    }

    public Image getImage() {
        return image;
    }

    public Integer getNumberOfMarks() {
        return numberOfMarks;
    }

    public String getEstTimeToRace() {
        return estTimeToRace;
    }
}
