package seng302.data;

public enum CourseName {

    AC35(0, "AC35"), AC33(1, "AC33"), ATHENS(2, "Athens"), LAKE_TEKAPO(3, "Lake Tekapo"), LAKE_TAUPO(4, "Lake Taupo"), MALMO(5, "Malmo");

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
}
