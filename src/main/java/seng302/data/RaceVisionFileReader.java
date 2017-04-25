package seng302.data;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import seng302.models.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * Created on 6/03/17.
 * Collection of methods for reading in data from files. Files must be located in the DEFAULT_FILE_PATH folder
 */

public class RaceVisionFileReader {

    private static final String DEFAULT_FILE_PATH = "/defaultFiles/";
    private static final String DEFAULT_STARTERS_FILE = "starters.txt";
    private static final String DEFAULT_COURSE_FILE = "AC35-course.xml";
    private static final String COURSE_FILE = "Race.xml";
    private static final String BOAT_FILE = "Boat.xml";

    private static Document dom;

    /**
     * Manages importing the course from the correct place
     * If a file path is specified, this will be used, otherwise a default is packaged with the jar.
     * Currently this an XML file at DEFAULT_FILE_PATH/COURSE_FILE
     * @param filePath String of the file path of the file to read in.
     * @return a Course object.
     */
    public static Course importCourse(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()) {
                parseXMLFile(filePath, false);
            } else {
                String resourcePath = DEFAULT_FILE_PATH + COURSE_FILE;
                parseXMLFile(resourcePath, true);
            }
            return importCourseFromXML();
        }  catch (IOException ioe) {
            System.err.printf("Unable to read %s as a course definition file. " +
                    "Ensure it is correctly formatted.\n", filePath);
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Overload of importCourse to simplify reading in the default course file.
     * @return a Course object
     */
    public static Course importCourse(){
        return importCourse(null);
    }


    /**
     * Attempts to read the desired XML file into the Document parser
     * @param filePath - the location of the file to be read, must be XML
     * @param isResource specifies whether the file is packaged in the resources folder
     * @throws IOException if the file is not found
     */
    public static void parseXMLFile(String filePath, boolean isResource) throws IOException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            if (isResource){
                dom = db.parse(RaceVisionFileReader.class.getResourceAsStream(filePath));
            } else {
                dom = db.parse(filePath);
            }
        } catch(ParserConfigurationException | SAXException pce) {
            pce.printStackTrace();
        }
    }

    /**
     * Decodes an XML file into a Course object
     * @return a Course object
     */
    public static Course importCourseFromXML() {
        Course course = new Course();

        try {
            Element root = dom.getDocumentElement();
            if (root.getTagName() != XMLTags.Course.RACE) {
                String message = String.format("The root tag must be <%s>.", XMLTags.Course.RACE);
                throw new XMLParseException(XMLTags.Course.RACE, message);
            }

            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    switch (element.getTagName()) {
                        case XMLTags.Course.COURSE:
                            NodeList compoundMarks = element.getElementsByTagName(XMLTags.Course.COMPOUND_MARK);
                            for (int j = 0; j < compoundMarks.getLength(); j++){
                                CompoundMark mark = parseCompoundMark((Element) compoundMarks.item(j));
                                course.addNewCompoundMark(mark);
                            }
                            break;
                        case XMLTags.Course.COMPOUND_MARK_SEQUENCE:
                            NodeList legs = element.getElementsByTagName(XMLTags.Course.CORNER);
                            Map<Integer, Integer> markOrder = new TreeMap<>();
                            for (int k = 0; k < legs.getLength(); k++) {
                                Element corner = (Element) legs.item(k);
                                Integer seqNumber = Integer.parseInt(corner.getAttribute("SeqID"));
                                Integer compoundMarkID = Integer.parseInt(corner.getAttribute("CompoundMarkID"));
                                markOrder.put(seqNumber, compoundMarkID);
                            }
                            for(Integer seqNumber : markOrder.keySet()){
                                course.addMarkInOrder(markOrder.get(seqNumber));
                            }
                            break;
                        case XMLTags.Course.WIND:
                            course.setWindDirection(Double.parseDouble(element.getTextContent()));
                            break;
                        case XMLTags.Course.COURSE_LIMIT:
                            NodeList courseLimits = element.getElementsByTagName(XMLTags.Course.LIMIT);
                            for (int k = 0; k < courseLimits.getLength(); k++) {
                                Coordinate coord = parseCourseLimitCoord((Element) courseLimits.item(k));
                                course.addToBoundary(coord);
                            }
                            break;
                        case XMLTags.Course.TIMEZONE:
                            course.setTimeZone(String.valueOf(element.getTextContent()));
                            break;
                    }
                }
            }
        } catch (XMLParseException e) {
            System.err.printf("Error reading course file around tag <%s>.\n", e.getTag());
            e.printStackTrace();
        }

        if(course.getCourseOrder().size() < 2){
            throw new InputMismatchException("There must be at least one leg in the course.");
        }

        if(course.getCourseOrder().get(0).isStartLine()){
            course.setStartLine((RaceLine) course.getCourseOrder().get(0));
        } else{
            throw new InputMismatchException("The first leg of the course must start at the start line.");
        }

        int lastMarkIndex = course.getCourseOrder().size() - 1;
        if(course.getCourseOrder().get(lastMarkIndex).isFinishLine()){
            course.setFinishLine((RaceLine) course.getCourseOrder().get(lastMarkIndex));
        } else{
            throw new InputMismatchException("The last leg of the course must end at the finish line.");
        }

        return course;
    }

    private static Mark parseMark(Element markElement){
        String markName = markElement.getAttribute(XMLTags.Course.NAME);
        Double lat1 = Double.parseDouble(markElement.getAttribute(XMLTags.Course.TARGETLAT));
        Double lon1 = Double.parseDouble(markElement.getAttribute(XMLTags.Course.TARGETLON));
        Integer sourceId = Integer.parseInt(markElement.getAttribute(XMLTags.Course.SOURCEID));
        Mark mark = new Mark(sourceId, markName, new Coordinate(lat1, lon1));
        return mark;
    }

    private static CompoundMark parseCompoundMark(Element compoundMarkElement) throws  XMLParseException{
        CompoundMark compoundMark;
        Integer compoundMarkID = Integer.parseInt(compoundMarkElement.getAttribute(XMLTags.Course.COMPOUNDMARKID));
        String compoundMarkName = compoundMarkElement.getAttribute(XMLTags.Course.NAME);
        NodeList markNodes = compoundMarkElement.getElementsByTagName(XMLTags.Course.MARK);
        if (markNodes.getLength() < 1) {
            throw new XMLParseException(XMLTags.Course.MARK, "Required tag was not defined.");
        }
        int numMarks = markNodes.getLength();
        if(numMarks == 2){
            Element mark1Element = (Element) markNodes.item(0);
            Element mark2Element = (Element) markNodes.item(1);

            Mark mark1 = parseMark(mark1Element);
            Mark mark2 = parseMark(mark2Element);

            if(mark1.getName().toLowerCase().contains(XMLTags.Course.START)){
                compoundMark = new RaceLine(compoundMarkID, compoundMarkName, mark1, mark2);
                compoundMark.setMarkAsStart();
            }else if(mark1.getName().toLowerCase().contains(XMLTags.Course.FINISH)){
                compoundMark = new RaceLine(compoundMarkID, compoundMarkName, mark1, mark2);
                compoundMark.setMarkAsFinish();
            } else{
                compoundMark = new CompoundMark(compoundMarkID, compoundMarkName, mark1, mark2);
            }
        }else{
            Element markElement = (Element) markNodes.item(0);
            Mark mark = parseMark(markElement);
            compoundMark = new CompoundMark(compoundMarkID, compoundMarkName, mark);
        }
        return compoundMark;
    }

//    /**
//     * Decodes a mark element into a CompoundMark object
//     *
//     * @param markElement - an XML <mark> element
//     * @return a CompoundMark (potentially Gate) object
//     * @throws XMLParseException when an expected tag is missing or unexpectedly formatted
//     */
//    private static CompoundMark parseMark(Element markElement) throws XMLParseException {
//        CompoundMark mark;
//        Integer markID = Integer.parseInt(markElement.getAttribute("CompoundMarkID"));
//        String name = markElement.getAttribute("Name");
//
//        NodeList markNodes = markElement.getElementsByTagName(XMLTags.Course.MARK);
//        if (markNodes.getLength() < 1) {
//            throw new XMLParseException(XMLTags.Course.MARK, "Required tag was not defined.");
//        }
//        Element currMark = (Element) markNodes;
//        currMark.getAttribute("SeqID");
//        currMark.getAttribute("Name");
//
//        NodeList latlons = markElement.getElementsByTagName(XMLTags.Course.LATLON);
//        if (latlons.getLength() < 1) {
//            throw new XMLParseException(XMLTags.Course.LATLON, "Required tag was not defined.");
//        }
//        double lat1 = extractLatitude((Element) latlons.item(0));
//        double lon1 = extractLongitude((Element) latlons.item(0));
//
//        if (latlons.getLength() > 1) { //it is a gate or start or finish
//            double lat2 = extractLatitude((Element) latlons.item(1));
//            double lon2 = extractLongitude((Element) latlons.item(1));
//
//            //Check whether the mark has a start or finish attribute
//            NamedNodeMap attr = markElement.getAttributes();
//            if (attr.getNamedItem(XMLTags.Course.START) != null) {
//                mark = new RaceLine(name, lat1, lon1, lat2, lon2);
//                mark.setMarkAsStart();
//            } else if (attr.getNamedItem(XMLTags.Course.FINISH) != null){
//                mark = new RaceLine(name, lat1, lon1, lat2, lon2);
//                mark.setMarkAsFinish();
//            } else{
//                mark = new Gate(name, lat1, lon1, lat2, lon2);
//            }
//        } else { //it is just a single-point mark
//            mark = new CompoundMark(name, lat1, lon1);
//        }
//
//        return mark;
//    }

    /**
     *
     * @param limit
     * @return
     * @throws XMLParseException
     */
    private static Coordinate parseCourseLimitCoord(Element limit) throws XMLParseException{
        Double lat = Double.parseDouble(limit.getAttribute("Lat"));
        Double lon = Double.parseDouble(limit.getAttribute("Lon"));
        Coordinate coord = new Coordinate(lat, lon);
        return coord;
    }

    /**
     * Pulls the latitude from an XML <lat> element and parses it as a double
     * @param latlon the element to pull the lat from
     * @return a double representing a latitude
     * @throws XMLParseException if no <lat> tag exists
     */
    private static double extractLatitude(Element latlon) throws XMLParseException {
        NodeList anyLats = latlon.getElementsByTagName(XMLTags.Course.LAT);
        if (anyLats.getLength() < 1) {
            throw new XMLParseException(XMLTags.Course.LAT, "Required tag was not defined.");
        }
        Node latElement = anyLats.item(0);
        return Double.parseDouble(latElement.getTextContent());
    }

    /**
     * Pulls the longitude from an XML <lon> element and parses it as a double
     * @param latlon the element to pull the lon from
     * @return a double representing a longitude
     * @throws XMLParseException if no <lon> tag exists
     */
    private static double extractLongitude(Element latlon) throws XMLParseException {
        NodeList anyLons = latlon.getElementsByTagName(XMLTags.Course.LON);
        if (anyLons.getLength() < 1) {
            throw new XMLParseException(XMLTags.Course.LON, "Required tag was not defined.");
        }
        Node lonElement = anyLons.item(0);
        return Double.parseDouble(lonElement.getTextContent());
    }





      //Old way of parsing boat file when it was in txt format
    /*
     * Imports file found at DEFAULT_STARTERS_FILE in DEFAULT_FILE_PATH in resources folder
     *
     * Boats defined as:
     *      BoatName, Speed
     *
     * Speed is expected in knots
     * @param filePath string of the file path were the starters are imported.
     * @return starters - ArrayList of Boat objects defined in file
    */

//    public static ArrayList<Boat> importStarters(String filePath){
//        ArrayList<Boat> starters = new ArrayList<>();
//
//        try {
//            BufferedReader br;
//            if (filePath != null && !filePath.isEmpty()) {
//                br = new BufferedReader(new FileReader(filePath));
//            } else {
//                filePath = DEFAULT_FILE_PATH + DEFAULT_STARTERS_FILE;
//                br = new BufferedReader(
//                        new InputStreamReader(RaceVisionFileReader.class.getResourceAsStream(filePath)));
//            }
//            ArrayList<Boat> allBoats = new ArrayList<>();
//
//            String line = br.readLine();
//            while (line != null){
//                StringTokenizer st = new StringTokenizer((line));
//                String name = st.nextToken(",");
//                String nickName = st.nextToken().trim();
//                double speed = Double.parseDouble(st.nextToken());
//                allBoats.add(new Boat(name, nickName, speed));
//                line = br.readLine();
//            }
//
//            Random ran = new Random();
//            for (int i = 0; i < Config.NUM_BOATS_IN_RACE; i++){
//                starters.add(allBoats.remove(ran.nextInt(allBoats.size())));
//            }
//            br.close();
//
//        } catch (FileNotFoundException e) {
//            System.err.printf("Starters file could not be found at %s\n", filePath);
//        } catch (IOException e) {
//            System.err.printf("Error reading starters file. Check it is in the correct format.");
//        }
//
//        return starters;
//    }

    /**
     * Manages importing the boats in the race from the correct place
     * If a file path is specified, this will be used, otherwise a default is packaged with the jar.
     * Currently this an XML file at DEFAULT_FILE_PATH/BOAT_FILE
     * @param filePath String of the file path of the file to read in.
     * @return an ArrayList of Boats.
     */
    public static List<Boat> importStarters(String filePath) {
        try {
            if (filePath != null && !filePath.isEmpty()) {
                parseXMLFile(filePath, false);
            } else {
                String resourcePath = DEFAULT_FILE_PATH + BOAT_FILE;
                parseXMLFile(resourcePath, true);
            }
            return importStartersFromXML();
        }  catch (IOException ioe) {
            System.err.printf("Unable to read %s as a boat definition file. " +
                    "Ensure it is correctly formatted.\n", filePath);
            ioe.printStackTrace();
            return null;
        }
    }


    /**
     * Imports file found at BOAT_FILE in DEFAULT_FILE_PATH in resources folder
     *
     * Boats defined as:
     *     BoatName, NickName and location
     *
     * @return starters - ArrayList of Boat objects defined in file
     */
    public static List<Boat> importStartersFromXML(){
        List<Boat> starters = new ArrayList<>();

        try {
            Element root = dom.getDocumentElement();
            if (root.getTagName() != XMLTags.Boats.BOATCONFIG) {
                String message = String.format("The root tag must be <%s>.", XMLTags.Boats.BOATCONFIG);
                throw new XMLParseException(XMLTags.Boats.BOATCONFIG, message);
            }

            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    switch (element.getTagName()) {
                        case XMLTags.Boats.BOATS:
                            NodeList boats = element.getElementsByTagName(XMLTags.Boats.BOAT);
                            for (int j = 0; j < boats.getLength(); j++){
                                Boat boat = parseBoat((Element) boats.item(j));
                                if(boat != null){
                                    starters.add(boat);
                                }
                            }
                            break;
                    }
                }
            }
        } catch (XMLParseException e) {
            System.err.printf("Error reading course file around tag <%s>.\n", e.getTag());
            e.printStackTrace();
        }

        if(starters.size() < 2){
            throw new InputMismatchException("There must be at least two boats in the race.");
        }

        return starters;
    }

    /**
     * Takes BoatXML tag and returns it as a Boat object.
     *
     *  @return boat
     */
    private static Boat parseBoat(Element boatXML) throws XMLParseException{
        Boat boat = null;
        String type = boatXML.getAttribute(XMLTags.Boats.TYPE);
        if(type.equals("Yacht")){
            String name = boatXML.getAttribute(XMLTags.Boats.BOATNAME);
            String nickname = boatXML.getAttribute(XMLTags.Boats.NICKNAME);
            Integer id = Integer.parseInt(boatXML.getAttribute(XMLTags.Boats.SOURCE_ID));
            //get lat long
            boat = new Boat(id, name, nickname, 0);
        }
        return boat;
    }

    /**
     * Takes a file from resources and puts it outside the jar to make it accessible to users
     * @param resourceName path to the resource
     * @param outputName path and name of where the file should be generated
     * @throws IOException when fails to read resource
     */
    static public void exportResource(String resourceName, String outputName) throws IOException {
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            inStream = RaceVisionFileReader.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(inStream == null) {
                throw new IOException("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            outStream = new FileOutputStream(outputName);
            while ((readBytes = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, readBytes);
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (inStream != null) {
                inStream.close();
            }
            if (outStream != null) {
                outStream.close();
            }
        }
    }
}
