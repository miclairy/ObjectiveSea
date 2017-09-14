package seng302.data;

public enum AC35StreamField {
    HEADER_TIMESTAMP(3, 9), HEADER_SOURCE_ID(9,13),MESSAGE_LENGTH(13, 15), MESSAGE_TYPE(2, 3),
    XML_VERSION(0, 1), XML_ACK(1, 3), XML_TIMESTAMP(3, 9),XML_SUBTYPE(9, 10), XML_SEQUENCE(10, 12),XML_LENGTH(12, 14), XML_BODY(14, 14),
    //boat Location
    BOAT_TIMESTAMP(1, 7), BOAT_SOURCE_ID(7, 11), BOAT_SEQUENCE_NUM(11, 15), DEVICE_TYPE(15, 16), LATITUDE(16, 20), LONGITUDE(20, 24), HEADING(28, 30),
    SPEED_OVER_GROUND(38, 40), TRUE_WIND_DIRECTION(48, 50), TRUE_WIND_ANGLE(50, 52), SAIL_STATE(52, 54),

    RACE_STATUS(11, 12), WIND_DIRECTION(18, 20), WIND_SPEED(20, 22), BOAT_ID(25, 28), EST_TIME_TO_MARK(33, 38), CURRENT_TIME(1, 7), START_TIME(12, 18),
    ROUNDING_TIME(1, 7), ROUNDING_SOURCE_ID(13, 17), ROUNDING_MARK_ID(20, 21), ROUNDING_MARK_TYPE(19, 20),
    MARK_ACK(7, 9), MARK_RACE_ID(9, 13), MARK_SOURCE(13, 17), MARK_BOAT_STATUS(17, 18), ROUNDING_SIDE(18, 19), MARK_TYPE(19, 20), MARK_ID(20, 21),

    STATUS_MESSAGE_VERSION_NUMBER(0,1), STATUS_RACE_ID(7,11), EXPECTED_START_TIME(12,18), RACE_COURSE_WIND_DIRECTION(18,20), RACE_COURSE_WIND_SPEED(20,22),
    NUMBER_OF_BOATS_IN_RACE(22,23), RACE_TYPE(23,24), STATUS_SOURCE_ID(0,4), BOAT_STATUS(4,5), LEG_NUMBER(5,6), NUMBER_PENALTIES_AWARDED(6,7),

    BOAT_ACTION_BODY(4, 5), BOAT_ACTION_SOURCE_ID(0, 4),
    NUMBER_PENALTIES_SERVED(7,8), ESTIMATED_TIME_AT_NEXT_MARK(8,14), ESTIMATED_TIME_AT_FINISH(14,20),

    REGISTRATION_REQUEST_TYPE(0, 1), REGISTRATION_SOURCE_ID(0, 4), REGISTRATION_RESPONSE_STATUS(4, 5),

    EVENT_MESSAGE_VERSION_NUMBER(0,1), EVENT_TIME(1, 7), EVENT_ACK_NUM(7, 9), RACE_ID(9, 13), DESTINATION_SOURCE_ID(13, 17),
    INCIDENT_ID(17, 21), EVENT_ID(21, 22), BOAT_HEALTH(4, 5), BOAT_STATE_SOURCE_ID(0, 4),

    HOST_GAME_IP(0, 4), HOST_GAME_PORT(4, 8), HOST_GAME_MAP(8, 9), HOST_GAME_SPEED(9, 10), HOST_GAME_STATUS(10, 11),
    HOST_GAME_REQUIRED_PLAYERS(11, 12), HOST_GAME_CURRENT_PLAYERS(12, 13),

    REQUEST_RACES_MIN_PLAYERS(0, 1);



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
