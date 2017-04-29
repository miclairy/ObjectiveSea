package seng302.data;

/**
 * Created on 16/03/17.
 * Contains a set of constants that define XML tag strings for matching
 */
public class XMLTags {

    public class Regatta {
        public static final String REGATTA_CONFIG = "RegattaConfig";
        public static final String REGATTA_NAME = "RegattaName";
        public static final String UTC_OFFSET = "UtcOffset";
    }

    public class Boats {
        public static final String BOAT_CONFIG = "BoatConfig";
        public static final String BOATS = "Boats";

        //Boats
        public static final String BOAT = "Boat";
        public static final String TYPE = "Type";
        public static final String BOATNAME = "BoatName";
        public static final String NICKNAME = "ShorterName";
        public static final String SOURCE_ID = "SourceID";
    }

    public class Course {
        public static final String RACE = "Race";
        public static final String COURSE = "Course";

        //compound marks
        public static final String COMPOUND_MARK = "CompoundMark";
        public static final String MARK = "Mark";
        public static final String NAME = "Name";
        public static final String LAT = "lat";
        public static final String LON = "lon";
        public static final String TARGETLAT = "TargetLat";
        public static final String TARGETLON = "TargetLng";
        public static final String SOURCEID = "SourceID";
        public static final String COMPOUNDMARKID = "CompoundMarkID";

        //mark attributes
        public static final String START = "start";
        public static final String FINISH = "finish";

        //sequence
        public static final String COMPOUND_MARK_SEQUENCE = "CompoundMarkSequence";
        public static final String CORNER = "Corner";

        //wind
        public static final String WIND = "wind";

        //course limit
        public static final String COURSE_LIMIT = "CourseLimit";
        public static final String LIMIT = "Limit";

        //timezone
        public static final String TIMEZONE = "timezone";

    }
}
