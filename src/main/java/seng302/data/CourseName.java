package seng302.data;

import java.util.Objects;

public enum CourseName {

    AC35(0, "AC35"), AC33(1, "AC33"), LAKE_TEKAPO(2, "Lake Tekapo"), LAKE_TAUPO(3, "Lake Taupo"), MALMO(4, "Malmo"), ATHENS(5, "Athens");

    private final int value;
    private final String name;

    CourseName(int value, String text){
        this.value = value;
        this.name = text;
    }

    public int getValue() {return value;}

    public String getText(){return name;}

    public static CourseName getCourseNameFromInt(int value){
        CourseName returnCourse = null;
        for (CourseName course : CourseName.values()){
            if (course.getValue() == value){
                returnCourse = course;
            }
        }
        return returnCourse;
    }

    public static int getCourseIntFromName(String name){
        int courseIndex = -1;
        for (CourseName courseName : CourseName.values()){
            if (Objects.equals(name, courseName.getText())){
                System.out.println(name);
                courseIndex = courseName.getValue();
            }
        }
        return courseIndex;
    }
}
