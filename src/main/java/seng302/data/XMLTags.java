package seng302.data;

/**
 * Created on 16/03/17.
 * Contains a set of constants that define XML tag strings for matching
 */
public class XMLTags {

    public class Boats {
        public static final String BOATCONFIG = "BoatConfig";
        public static final String BOATS = "Boats";

        //Boats
        public static final String BOAT = "Boat";
        public static final String TYPE = "Type";
        public static final String BOATNAME = "BoatName";
        public static final String NICKNAME = "ShorterName";
    }

    public class Course {
        public static final String RACE = "Race";
        public static final String COURSE = "Course";

        //marks
        public static final String COMPOUNDMARK = "CompoundMark";
        public static final String MARKS = "marks";
        public static final String MARK = "Mark";
        public static final String NAME = "name";
        public static final String LATLON = "latlon";
        public static final String LAT = "lat";
        public static final String LON = "lon";
        //mark attributes
        public static final String START = "start";
        public static final String FINISH = "finish";

        //sequence
        public static final String COMPOUNDMARKSEQUENCE = "CompoundMarkSequence";
        public static final String CORNER = "Corner";

        //wind
        public static final String WIND = "wind";

        //course limit
        public static final String COURSELIMIT = "CourseLimit";
        public static final String LIMIT = "limit";

        //timezone
        public static final String TIMEZONE = "timezone";

    }
}
