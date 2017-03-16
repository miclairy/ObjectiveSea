package seng302;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Created on 6/03/17.
 * Collection of methods for reading in data from files. Files must be located in the DATA_PATH folder
 * TODO: exit program or use some default values on failed read, rather than catching exceptions and failing later
 */

public class RaceVisionFileReader {

    private static final String DATA_PATH = "data/";
    private static final String STARTERS_FILE = "starters.txt";
    private static final String COURSE_FILE = "course.txt";
    private static final String COURSE_FILE_XML = "course.xml";

    private static Document dom;

    /**
     * Manages importing the course from the correct place
     * Currently this an XML file at DATA_PATH/COURSE_FILE_XML
     * @return a Course object
     */
    public static Course importCourse() {
        String filePath = DATA_PATH + COURSE_FILE_XML;
        try {
            parseXMLFile(filePath);
            return importCourseFromXML();
        }  catch (IOException ioe) {
            System.err.printf("Unable to read %s as a course definition file. " +
                    "Ensure it is correctly formatted.\n", COURSE_FILE_XML);
            ioe.printStackTrace();
            return null;
        }
    }

    /**
     * Attempts to read the desired XML file into the Document parser
     * @param filePath - the location of the file to be read, must be XML
     * @throws IOException if the file is not found
     */
    public static void parseXMLFile(String filePath) throws IOException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(filePath);
        } catch(ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch(SAXException se) {
            se.printStackTrace();
        }
    }

    /**
     * Decodes an XML file into a Course object
     *
     * Expected file structure:
     * <course>
     *     <marks>
     *         //definitions of each mark (see parseMark)
     *     </marks>
     *     <legs>
     *          <leg>[Start Mark]</leg>
     *          <leg>[A Mark]</leg>
     *          ...
     *          <leg>[Finish Mark]</leg>
     *     </legs>
     * </course>
     *
     * @return a Course object
     */
    public static Course importCourseFromXML() {
        Course course = new Course();

        try {
            Element root = dom.getDocumentElement();
            if (root.getTagName() != XMLTags.Course.COURSE) {
                String message = String.format("The root tag must be <%s>.", XMLTags.Course.COURSE);
                throw new XMLParseException(XMLTags.Course.COURSE, message);
            }

            NodeList nodes = root.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    switch (element.getTagName()) {
                        case XMLTags.Course.MARKS:
                            NodeList marks = element.getElementsByTagName(XMLTags.Course.MARK);
                            for (int j = 0; j < marks.getLength(); j++) {
                                course.addNewMark(parseMark((Element) marks.item(j)));
                            }
                            break;
                        case XMLTags.Course.LEGS:
                            NodeList legs = element.getElementsByTagName(XMLTags.Course.LEG);
                            for (int k = 0; k < legs.getLength(); k++) {
                                course.addMarkInOrder(legs.item(k).getTextContent());
                            }
                    }
                }
            }
        } catch (XMLParseException e) {
            System.err.printf("Error reading course file around tag <%s>.\n", e.getTag());
            e.printStackTrace();
        }

        return course;
    }

    /**
     * Decodes a mark element into a CompoundMark object
     *
     * Expected structure of a mark:
     * <mark>
     *     <name>Name is required</name>
     *     <latlon>
     *         <lat>...</lat>
     *         <lon>...</lon>
     *     </latlon>
     * </mark>
     *
     * If multiple <latlon> tags exist, the Mark will be interpreted as a Gate object
     *
     * To define a mark as the start mark use the start attribute, e.g.
     * <mark start="start">
     *
     * To define a mark as the finish mark use the finish attribute, e.g.
     * <mark start="finish">
     *
     * @param markElement - an XML <mark> element
     * @return a CompoundMark (potentially Gate) object
     * @throws XMLParseException when an expected tag is missing or unexpectedly formatted
     */
    private static CompoundMark parseMark(Element markElement) throws XMLParseException {
        CompoundMark mark;

        NodeList nameNodes = markElement.getElementsByTagName(XMLTags.Course.NAME);
        if (nameNodes.getLength() < 1) {
            throw new XMLParseException(XMLTags.Course.NAME, "Required tag was not defined.");
        }
        String name = nameNodes.item(0).getTextContent();
        NodeList latlons = markElement.getElementsByTagName(XMLTags.Course.LATLON);
        if (latlons.getLength() < 1) {
            throw new XMLParseException(XMLTags.Course.LATLON, "Required tag was not defined.");
        }
        double lat1 = extractLatitude((Element) latlons.item(0));
        double lon1 = extractLongitude((Element) latlons.item(0));

        if (latlons.getLength() > 1) { //it is a gate (or start or finish)
            double lat2 = extractLatitude((Element) latlons.item(1));
            double lon2 = extractLongitude((Element) latlons.item(1));
            mark = new Gate(name, lat1, lon1, lat2, lon2);
        } else { //it is just a single-point mark
            mark = new CompoundMark(name, lat1, lon1);
        }

        //Check whether the mark has a start or finish attribute
        NamedNodeMap attr = markElement.getAttributes();
        if (attr.getNamedItem(XMLTags.Course.START) != null){
            mark.setMarkAsStart();
        }
        if (attr.getNamedItem(XMLTags.Course.FINISH) != null){
            mark.setMarkAsFinish();
        }

        return mark;
    }

    /**
     * Pulls the latitude from an XML <lat> element and parses it as a double
     * @param latlon
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
     * @param latlon
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

    /**
     * Imports file found at STARTERS_FILE in DATA_PATH
     *
     * Boats defined as:
     *      BoatName, Speed
     *
     * Speed is expected in knots
     * @return starters - ArrayList of Boat objects defined in file
     */
    public static ArrayList<Boat> importStarters(){
        ArrayList<Boat> starters = new ArrayList<>();
        String filePath = DATA_PATH + STARTERS_FILE;

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));
            ArrayList<Boat> allBoats = new ArrayList<>();

            String line = br.readLine();
            while (line != null){
                StringTokenizer st = new StringTokenizer((line));
                String name = st.nextToken(",");
                double speed = Double.parseDouble(st.nextToken());
                allBoats.add(new Boat(name, speed));
                line = br.readLine();
            }

            Random ran = new Random();
            for (int i = 0; i < Config.NUM_BOATS_IN_RACE; i++){
                starters.add(allBoats.remove(ran.nextInt(allBoats.size())));
            }

        } catch (FileNotFoundException e) {
            System.err.printf("Starters file could not be found at %s\n", filePath);
        } catch (IOException e) {
            System.err.printf("Error reading starters file. Check it is in the correct format.");
        }

        return starters;
    }

    /**
     * @deprecated
     * Imports text file found at COURSE_FILE in DATA_PATH
     *
     * Marks defined as:
     *      MarkName
     *      Latitude Longitude
     *
     * Example file format:
     *      Mark 1
     *      123.4 56.8
     *      Mark 2
     *      345.6 -34.5
     *      Mark Order:
     *      Mark 1
     *      Mark 2
     *      Mark 1
     *
     * @return course - a Course object as specified in the file
     */
    public static Course legacyImportCourse() {
        String filePath = DATA_PATH + COURSE_FILE;
        Course course = new Course();

        try {
            BufferedReader br = new BufferedReader(new FileReader(filePath));

            String markName = br.readLine();
            while (markName != null){
                /** define each mark */
                String line = br.readLine();
                StringTokenizer st = new StringTokenizer(line);
                double lat = Double.parseDouble(st.nextToken());
                double lon = Double.parseDouble(st.nextToken());
                course.addNewMark(new CompoundMark(markName, lat, lon));
                markName = br.readLine();

                if (markName.equals("Mark Order:")){
                    /** once 'Mark Order' token found, read in order of marks throughout course */
                    markName = null;
                    String mark = br.readLine();
                    while (mark != null) {
                        course.addMarkInOrder(mark);
                        mark = br.readLine();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.printf("Course file could not be found at %s\n", filePath);
        } catch (IOException e) {
            System.err.printf("Error reading course file. Check it is in the correct format.");
        }
        return course;
    }
}
