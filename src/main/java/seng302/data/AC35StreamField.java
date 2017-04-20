package seng302.data;

/**
 * Created by raych on 17/04/2017.
 */
enum AC35StreamField {
    MESSAGE_LENGTH(13, 15), MESSAGE_TYPE(2, 3), RACE_STATUS(11, 12),
    XML_SUBTYPE(9, 10), XML_LENGTH(12, 14), XML_BODY(14, 14),
    SOURCE_ID(7, 11), LATITUDE(16, 20), LONGITUDE(20, 24), HEADING(28, 30), SPEED_OVER_GROUND(38, 40), DEVICE_TYPE(15, 16);

    private final int startIndex, endIndex;

    AC35StreamField(int startIndex, int endIndex){
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }
}
