package seng302.data;

public enum AC35StreamField {
    HEADER_TIMESTAMP(3, 9), HEADER_SOURCE_ID(9,13), MESSAGE_LENGTH(13, 15), MESSAGE_TYPE(2, 3),
    RACE_STATUS(11, 12),
    XML_VERSION(0, 1), XML_ACK(1, 3), XML_TIMESTAMP(3, 9), XML_SUBTYPE(9, 10), XML_SEQUENCE(10, 12), XML_LENGTH(12, 14), XML_BODY(14, 14),
    BOAT_TIMESTAMP(1, 7), BOAT_SOURCE_ID(7, 11), BOAT_SEQUENCE_NUM(11, 15), LATITUDE(16, 20), LONGITUDE(20, 24), HEADING(28, 30), BOAT_SPEED(34, 36);

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
