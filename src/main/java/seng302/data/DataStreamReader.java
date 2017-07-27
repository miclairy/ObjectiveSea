package seng302.data;

import seng302.models.Boat;
import seng302.models.Race;
import seng302.utilities.TimeUtils;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static seng302.data.AC35StreamField.*;
import static seng302.data.AC35StreamXMLMessage.*;

/**
 * Created on 13/04/17.
 */
public class DataStreamReader extends Receiver implements Runnable{

    private Socket clientSocket;
    private InputStream dataStream;
    private String sourceAddress;
    private int sourcePort;
    private Race race;
    private Map<AC35StreamXMLMessage, Integer> xmlSequenceNumbers = new HashMap<>();


    public DataStreamReader(String sourceAddress, int sourcePort){
        this.sourceAddress = sourceAddress;
        this.sourcePort = sourcePort;

        //initialize "current" xml sequence numbers to -1 to say we have not yet received any
        xmlSequenceNumbers.put(REGATTA_XML_MESSAGE, -1);
        xmlSequenceNumbers.put(RACE_XML_MESSAGE, -1);
        xmlSequenceNumbers.put(BOAT_XML_MESSAGE, -1);
    }

    /**
     * Runs the reader by setting up the connection and start reading in data
     */
    @Override
    public void run(){
        setUpConnection();
        readData();
    }

    /**
     * Sets up the connection to the data source by creating a socket and creates a InputStream from the socket
     */
    void setUpConnection() {
        try {
            clientSocket = new Socket(sourceAddress, sourcePort);
            dataStream = clientSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Converts an integer to a latitude/longitude angle as per specification.
     * (-2^31 = -180 deg, 2^31 = 180 deg)
     * @param value the latitude/longitude as a scaled integer
     * @return the actual latitude/longitude angle
     */
    static double intToLatLon(int value){
        return (double)value * 180 / Math.pow(2, 31);
    }


    /**
     * Converts an integer to a true wind angle as per specification.
     * @param value the angle as a scaled integer
     * @return the actual angle of the wind
     */
    static double intToTrueWindAngle(int value){
        return (double)value * 180 / Math.pow(2, 15);
    }

    /**
     * Converts an integer to a heading angle as per specification.
     * @param value the heading as a scaled integer
     * @return the actual angle of the heading
     */
    static double intToHeading(int value){
        return (double)value * 360 / Math.pow(2, 16);
    }

    /**
     * Reads in a XML Message, parses the header and saves the XML payload to the corresponding file
     * @param body The byte array containing the XML Message (header + payload)
     */
    private void convertXMLMessage(byte[] body) throws IOException {
        int xmlSubtypeValue = byteArrayRangeToInt(body, XML_SUBTYPE.getStartIndex(), XML_SUBTYPE.getEndIndex());
        AC35StreamXMLMessage xmlSubtype = AC35StreamXMLMessage.fromInteger(xmlSubtypeValue);
        int xmlSequenceNumber = byteArrayRangeToInt(body, XML_SEQUENCE.getStartIndex(), XML_SEQUENCE.getEndIndex());
        int xmlLength = byteArrayRangeToInt(body, XML_LENGTH.getStartIndex(), XML_LENGTH.getEndIndex());

        String xmlBody = new String(Arrays.copyOfRange(body, XML_BODY.getStartIndex(), XML_BODY.getStartIndex()+xmlLength));
        xmlBody = xmlBody.trim();
        InputStream xmlInputStream = new ByteArrayInputStream(xmlBody.getBytes());
        //Taken out since the new stream sends xmls not in order
//        if (xmlSequenceNumbers.get(xmlSubtype) < xmlSequenceNumber) {
            xmlSequenceNumbers.put(xmlSubtype, xmlSequenceNumber);
            if (xmlSubtype == REGATTA_XML_MESSAGE) {
                System.out.printf("Client: New Regatta XML Received, Sequence No: %d\n", xmlSequenceNumber);
                RaceVisionXMLParser.importRegatta(xmlInputStream, race);
            } else if (xmlSubtype == RACE_XML_MESSAGE) {
                System.out.printf("Client: New Race XML Received, Sequence No: %d\n", xmlSequenceNumber);
                if (race != null) {
                    setChanged();
                    Race newRace = RaceVisionXMLParser.importRace(xmlInputStream);
                    notifyObservers(newRace);
                } else {
                    setRace(RaceVisionXMLParser.importRace(xmlInputStream));
                }
            } else if (xmlSubtype == BOAT_XML_MESSAGE) {
                System.out.printf("Client: New Boat XML Received, Sequence No: %d\n", xmlSequenceNumber);
                if(race.getCompetitors().size() == 0){
                    List<Boat> competitors = RaceVisionXMLParser.importStarters(xmlInputStream);
                    race.setCompetitors(competitors);
                    setChanged();
                    notifyObservers(competitors);
                }
            }
//        }
    }

    /**
     * Parses portions of the boat location message byte array to their corresponding values.
     * @param body the byte array containing the boat location message
     */
    private void parseBoatLocationMessage(byte[] body) {
        int sourceID = byteArrayRangeToInt(body, BOAT_SOURCE_ID.getStartIndex(), BOAT_SOURCE_ID.getEndIndex());
        int latScaled = byteArrayRangeToInt(body, LATITUDE.getStartIndex(), LATITUDE.getEndIndex());
        int lonScaled = byteArrayRangeToInt(body, LONGITUDE.getStartIndex(), LONGITUDE.getEndIndex());
        int headingScaled = byteArrayRangeToInt(body, HEADING.getStartIndex(), HEADING.getEndIndex());
        int boatSpeed = byteArrayRangeToInt(body, SPEED_OVER_GROUND.getStartIndex(), SPEED_OVER_GROUND.getEndIndex());

        int deviceType = byteArrayRangeToInt(body, DEVICE_TYPE.getStartIndex(), DEVICE_TYPE.getEndIndex());
        int trueWindDirectionScaled = byteArrayRangeToInt(body, TRUE_WIND_DIRECTION.getStartIndex(), TRUE_WIND_DIRECTION.getEndIndex());
        int trueWindAngleScaled = byteArrayRangeToInt(body, TRUE_WIND_ANGLE.getStartIndex(), TRUE_WIND_ANGLE.getEndIndex());
        double trueWindAngle = intToTrueWindAngle(trueWindAngleScaled);
        //unused as we believe this is always sent as 0 from the AC35 feed
        double trueWindDirection = intToHeading(trueWindDirectionScaled);
        double lat = intToLatLon(latScaled);
        double lon = intToLatLon(lonScaled);
        double heading = intToHeading(headingScaled);
        double speedInKnots = TimeUtils.convertMmPerSecondToKnots(boatSpeed);

        if(deviceType == BOAT_DEVICE_TYPE){
            race.updateBoat(sourceID, lat, lon, heading, speedInKnots, trueWindAngle);
        } else if(deviceType == MARK_DEVICE_TYPE){
            race.getCourse().updateMark(sourceID, lat, lon);
        }
    }


    /**
     * Keeps reading in from the data stream and parses each message header and hands off the payload to the
     * corresponding method. Ignores the message if the message type is not needed.
     */
    private void readData(){
        DataInput dataInput = new DataInputStream(dataStream);
        while(race == null || !race.getRaceStatus().isRaceEndedStatus()) {
            try {
                byte[] header = new byte[HEADER_LENGTH];
                dataInput.readFully(header);

                int messageLength = byteArrayRangeToInt(header, MESSAGE_LENGTH.getStartIndex(), MESSAGE_LENGTH.getEndIndex());
                int messageTypeValue = byteArrayRangeToInt(header, MESSAGE_TYPE.getStartIndex(), MESSAGE_TYPE.getEndIndex());
                AC35StreamMessage messageType = AC35StreamMessage.fromInteger(messageTypeValue);
                int sourceID = byteArrayRangeToInt(header, HEADER_SOURCE_ID.getStartIndex(), HEADER_SOURCE_ID.getEndIndex());
                byte[] body = new byte[messageLength];
                dataInput.readFully(body);
                byte[] crc = new byte[CRC_LENGTH];
                dataInput.readFully(crc);
                if (checkCRC(header, body, crc)) {
                    switch (messageType) {
                        case XML_MESSAGE:
                            convertXMLMessage(body);
                            break;
                        default:
                            if (race != null && race.isInitialized()) {
                                switch (messageType) {
                                    case BOAT_LOCATION_MESSAGE:
                                        parseBoatLocationMessage(body);
                                        break;
                                    case RACE_STATUS_MESSAGE:
                                        parseRaceStatusMessage(body);
                                        break;
                                    case MARK_ROUNDING_MESSAGE:
                                        parseMarkRoundingMessage(body);
                                        break;
                                    case YACHT_EVENT_CODE:
                                        parseYachtEventMessage(body);
                                        break;
                                    case REGISTRATION_ACCEPT:
                                        parseRegistrationAcceptMessage(body);
                                }
                            }
                    }
                } else {
                    System.err.println("Incorrect CRC. Message Ignored.");
                }

            } catch (IOException e) {
                System.err.println("Error occurred when reading data from stream:");
                System.err.println(e);
            }
        }
    }

    private void parseRegistrationAcceptMessage(byte[] body) {
        Integer id = byteArrayRangeToInt(body, REGISTRATION_SOURCE_ID.getStartIndex(), REGISTRATION_SOURCE_ID.getEndIndex());
        System.out.println("Client: Received ID of " + id);
        setChanged();
        notifyObservers(id);
    }


    /**
     * Parses the body of Race Status message, and updates race status, race times and wind direction
     * based on values received
     * @param body the body of the race status message
     */
    private void parseRaceStatusMessage(byte[] body) {
        int raceStatus = byteArrayRangeToInt(body, RACE_STATUS.getStartIndex(), RACE_STATUS.getEndIndex());
        int raceCourseWindDirection = byteArrayRangeToInt(body, WIND_DIRECTION.getStartIndex(), WIND_DIRECTION.getEndIndex());
        long currentTime = byteArrayRangeToLong(body, CURRENT_TIME.getStartIndex(), CURRENT_TIME.getEndIndex());
        long expectedStartTime = byteArrayRangeToLong(body, START_TIME.getStartIndex(), START_TIME.getEndIndex());
        long windSpeed = byteArrayRangeToInt(body, WIND_SPEED.getStartIndex(), WIND_SPEED.getEndIndex());

        double windDirectionInDegrees = intToHeading(raceCourseWindDirection);

        byte[] boatStatuses = new byte[body.length - 24];

        for(int i = 24; i < body.length; i++){
            boatStatuses[i - 24] = body[i];
        }

        for  (int k = 0; k < boatStatuses.length; k += 20) {
            int boatID = byteArrayRangeToInt(boatStatuses, 0 + k, 4 + k);
            int boatStatus = byteArrayRangeToInt(boatStatuses, 4 + k, 5 + k);
            long estimatedTimeAtMark = byteArrayRangeToLong(boatStatuses, 8 + k, 14 + k);
            int legNumber = byteArrayRangeToInt(boatStatuses, 5 + k, 6 + k);
            int legOffset = 0;
            if(race.getCourse().hasEntryMark()) legOffset += 1;
            Boat boat = race.getBoatById(boatID);
            if(boat != null){
                boat.setTimeTillMark(estimatedTimeAtMark);
                boat.setLeg(legNumber + legOffset);
                boat.setStatus(BoatStatus.values()[boatStatus]);
            }
        }
        if(race.isFirstMessage()){
            race.updateRaceOrder();
            race.setFirstMessage(false);
        }
        race.getCourse().setWindDirection(windDirectionInDegrees);
        race.getCourse().setTrueWindSpeed(TimeUtils.convertMmPerSecondToKnots(windSpeed));
        race.updateRaceStatus(RaceStatus.fromInteger(raceStatus));
        race.setStartTimeInEpochMs(expectedStartTime);
        race.setCurrentTimeInEpochMs(currentTime);
    }

    private void parseYachtEventMessage(byte[] body){
        int eventID = byteArrayRangeToInt(body, EVENT_ID.getStartIndex(), EVENT_ID.getEndIndex());
        int boatID = byteArrayRangeToInt(body, DESTINATION_SOURCE_ID.getStartIndex(), DESTINATION_SOURCE_ID.getEndIndex());
        if (eventID == YachtEventCode.COLLISION.code()) {
            Boat boat = race.getBoatById(boatID);
            boat.setBoatColliding(true);
            boat.setMarkColliding(true);
        }
        if (eventID == YachtEventCode.COLLISION_PENALTY.code()) {

        }
    }

    /**
     * Parses the body of Mark Rounding message, and updates the race based on values received
     * @param body the body of the mark rounding message
     */
    private void parseMarkRoundingMessage(byte[] body) {
        int passedEntryLimitLine = 100;
        int passedEntryLine = 101;
        int passedStartLineId = 102;
        int passedFinishLineId = 103;
        long time = byteArrayRangeToLong(body, ROUNDING_TIME.getStartIndex(), ROUNDING_TIME.getEndIndex());
        int sourceID = byteArrayRangeToInt(body, ROUNDING_SOURCE_ID.getStartIndex(), ROUNDING_SOURCE_ID.getEndIndex());
        int markIndex = byteArrayRangeToInt(body, ROUNDING_MARK_ID.getStartIndex(), ROUNDING_MARK_ID.getEndIndex());

        if(markIndex == passedStartLineId || markIndex == passedEntryLimitLine ||markIndex == passedEntryLine){
            markIndex = 0;
        } else if(markIndex == passedFinishLineId){
            markIndex = race.getCourse().getCourseOrder().size()-1;
        }
        race.updateMarkRounded(sourceID, markIndex, time);
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void setRace(Race race) {
        this.race = race;
    }

    public Race getRace() {
        return race;
    }
}
