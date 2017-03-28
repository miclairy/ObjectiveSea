package seng302;

/**
 * Created by Michael Trotter on 3/25/2017.
 * A utility class for converting between various time units to aid in clean and readable code in other places.
 */
public class TimeUtils {

    private static final double NANOSECONDS_IN_SECOND = 1e9f;
    private static final double SECONDS_IN_MINUTE = 60;
    private static final double MINUTES_IN_HOUR = 60;

    public static double convertSecondsToHours(double seconds){
        return seconds / (SECONDS_IN_MINUTE * MINUTES_IN_HOUR);
    }

    public static double convertNanosecondsToSeconds(double nanoseconds){
        return nanoseconds / NANOSECONDS_IN_SECOND;
    }

    public static double convertHoursToSeconds(double hours){
        return hours * (SECONDS_IN_MINUTE * MINUTES_IN_HOUR);
    }

    public static double convertMinutesToSeconds(double seconds){
        return seconds * SECONDS_IN_MINUTE;
    }
}
