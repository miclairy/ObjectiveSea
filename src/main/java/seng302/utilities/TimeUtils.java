package seng302.utilities;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;

/**
 * A utility class for converting between various time units to aid in clean and readable code in other places.
 */
public class TimeUtils {

    private static final double NANOSECONDS_IN_SECOND = 1e9f;
    private static final double SECONDS_IN_MINUTE = 60;
    private static final double MINUTES_IN_HOUR = 60;

    private static boolean incorrectTimeZone = true;
    private static String foundId = new String();

    public static double convertSecondsToHours(double seconds){
        return seconds / (SECONDS_IN_MINUTE * MINUTES_IN_HOUR);
    }


    /**
     *
     * @param timeZone The local time zone
     * @return String contaning the correct time for the given time zone
     */
    public static String setTimeZone(String timeZone) {
        String defaultTimeZone = TimeZone.getDefault().getID();

        try {
            for (String id : TimeZone.getAvailableIDs()) {

                if (id.matches("(?i).*?" + timeZone + ".*")) {
                    foundId = id;
                    incorrectTimeZone = false;
                    break;
                }
            }
            if (incorrectTimeZone) {
                throw new Exception("Incorrect TimeZone in XML file. TimeZone reset to default.");
            }
        } catch (Exception e) {
            foundId = defaultTimeZone;
            incorrectTimeZone = false;
            System.out.println(e.getMessage());
        } finally {
            Instant instant = Instant.now();
            ZoneId zone = ZoneId.of(foundId);
            ZonedDateTime zonedDateTime = instant.atZone(zone);
            int hours = zonedDateTime.getHour();
            int minutes = zonedDateTime.getMinute();
            int seconds = zonedDateTime.getSecond();
            int utc = zonedDateTime.getOffset().getTotalSeconds()/3600;
            String isPositive = "";
            if(utc >= 0){
                isPositive = "+";
            }
            return String.format("%02d:%02d:%02d UTC%s%d", hours, minutes, seconds, isPositive, utc);
        }
    }


    /**
     * taken from sprint 2 work, however the original code is http://www.geodatasource.com/developers/java
     * @param lat1 latitude of the boat
     * @param lat2 latitude of the mark
     * @param lon1 longitude of the boat
     * @param lon2 longitude of the mark
     * @return a distance double in nautical
     */
    public static double calcDistance(double lat1, double lat2, double lon1, double lon2){
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist * 0.8684);
    }

    private static double deg2rad(double deg) {return (deg * Math.PI / 180.0); }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
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
