package seng302.data;

public enum AC35StreamField {
    HEADER_TIMESTAMP(3, 9), HEADER_SOURCE_ID(9,13), MESSAGE_LENGTH(13, 15), MESSAGE_TYPE(2, 3),
    RACE_STATUS(11, 12),
    XML_VERSION(0, 1), XML_ACK(1, 3), XML_TIMESTAMP(3, 9), XML_SUBTYPE(9, 10), XML_SEQUENCE(10, 12), XML_LENGTH(12, 14), XML_BODY(14, 14),
    BOAT_TIMESTAMP(1, 7), BOAT_SOURCE_ID(7, 11), BOAT_SEQUENCE_NUM(11, 15), LATITUDE(16, 20), LONGITUDE(20, 24), HEADING(28, 30), BOAT_SPEED(34, 36),
    MARK_ACK(7, 9), MARK_RACE_ID(9, 13), MARK_SOURCE(13, 17), MARK_BOAT_STATUS(17, 18), ROUNDING_SIDE(18, 19), MARK_TYPE(19, 20), MARK_ID(20, 21),

    STATUS_MESSAGE_VERSION_NUMBER(0,1), STATUS_RACE_ID(7,11), EXPECTED_START_TIME(12,18), RACE_COURSE_WIND_DIRECTION(18,20), RACE_COURSE_WIND_SPEED(20,22),
    NUMBER_OF_BOATS_IN_RACE(22,23), RACE_TYPE(23,24), STATUS_SOURCE_ID(24,28), BOAT_STATUS(28,29), LEG_NUMBER(29,30), NUMBER_PENALTIES_AWARDED(30,31),
    NUMBER_PENALTIES_SERVED(31,32), ESTIMATED_TIME_AT_NEXT_MARK(32,38), ESTIMATED_TIME_AT_FINISH(38,44);

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

    public int getLength() {
        return endIndex - startIndex;
    }
}
