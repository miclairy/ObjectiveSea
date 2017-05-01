package seng302.data;

enum AC35StreamField {
    MESSAGE_LENGTH(13, 15), MESSAGE_TYPE(2, 3),
    XML_SUBTYPE(9, 10), XML_SEQUENCE(10, 12), XML_LENGTH(12, 14), XML_BODY(14, 14),
    SOURCE_ID(7, 11), LATITUDE(16, 20), LONGITUDE(20, 24), HEADING(28, 30), SPEED_OVER_GROUND(38, 40), DEVICE_TYPE(15, 16),
    RACE_STATUS(11, 12), WIND_DIRECTION(18, 20), WIND_SPEED(20, 22), CURRENT_TIME(1, 7), START_TIME(12, 18),
    ROUNDING_TIME(1, 7), ROUNDING_SOURCE_ID(13, 17), ROUNDING_MARK_ID(20, 21);

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
