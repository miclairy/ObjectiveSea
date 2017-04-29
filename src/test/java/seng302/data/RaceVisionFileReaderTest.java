package seng302.data;


import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import seng302.models.*;

public class RaceVisionFileReaderTest {

    @Ignore //TODO Rewrite test for new xml format
    @Test
    public void readCourseFileTest(){
        Course course = RaceVisionFileReader.importCourse("data/testFiles/testRace.xml");
        Course expected = createExpectedCourse();

        Assert.assertNotNull(course);
        for (int i = 0; i < expected.getCourseOrder().size(); i++) {
            assertCompoundMarksAreEqual(expected.getCourseOrder().get(i), course.getCourseOrder().get(i));
        }
        for (Integer key : expected.getCompoundMarks().keySet()) {
            assertCompoundMarksAreEqual(expected.getCompoundMarks().get(key), course.getCompoundMarks().get(key));
        }
        Assert.assertEquals(expected.getWindDirection(), course.getWindDirection(), 0);
        Assert.assertTrue(course.getCompoundMarks().get("Start").isStartLine());
        Assert.assertTrue(course.getCompoundMarks().get("Finish").isFinishLine());
        Assert.assertTrue(course.getCompoundMarks().get("Start") instanceof RaceLine);
        Assert.assertTrue(course.getCompoundMarks().get("Finish") instanceof RaceLine);
        Assert.assertTrue(course.getCompoundMarks().get("Gate") != null);
        Assert.assertFalse(course.getCompoundMarks().get("Mark") != null);

        //Boundary
        Assert.assertEquals(course.getBoundary().size(), 3);
        Assert.assertEquals(course.getBoundary().get(0).getLat(), 32.5, 0);
        Assert.assertEquals(course.getBoundary().get(0).getLon(), -60.1, 0);

        Assert.assertEquals(course.getBoundary().get(1).getLat(), 32.0, 0);
        Assert.assertEquals(course.getBoundary().get(1).getLon(), -60.1, 0);

        Assert.assertEquals(course.getBoundary().get(2).getLat(), 32.0, 0);
        Assert.assertEquals(course.getBoundary().get(2).getLon(), -60.0, 0);

    }

    @Test
    public void nonExistentCourseFileTest(){
        //this code hides the expected printed stacktrace for the duration of the test, to prevent a successful
        //test appearing like an error. If this test fails, these lines should be taken out to see the real stacktrace.
        java.io.PrintStream realErrorStream = System.err;
        System.setErr(new java.io.PrintStream(new java.io.OutputStream(){public void write(int i){}}));

        Course course = RaceVisionFileReader.importCourse("I am a fake file");

        System.setErr(realErrorStream);
        Assert.assertNull(course);
    }

    /** This is a clone of the course that testRace.xml is expected to create */
    private Course createExpectedCourse() {
        Course expected = new Course();

        Mark startLine1 = new Mark(0, "Start Line 1", new Coordinate(0, 0));
        Mark startLine2 = new Mark(1, "Start Line 2", new Coordinate(0, 1));
        RaceLine start = new RaceLine(0, "Start", startLine1, startLine2);
        start.setMarkAsStart();

        Mark finishLine1 = new Mark(2, "Finish Line 1", new Coordinate(0, 5));
        Mark finishLine2 = new Mark(3, "Finish Line 2", new Coordinate(0, 6));
        RaceLine finish = new RaceLine(1, "Finish", finishLine1, finishLine2);
        finish.setMarkAsFinish();

        Mark mark1 = new Mark(4, "Mark 1", new Coordinate(2, 2));
        CompoundMark mark = new CompoundMark(2, "Mark", mark1);

        Mark gate1 = new Mark(3, "Gate 1", new Coordinate(3, 3));
        Mark gate2 = new Mark(4, "Gate 2", new Coordinate(4, 4));
        CompoundMark gate = new CompoundMark(3, "Gate", gate1, gate2);

        expected.addNewCompoundMark(start);
        expected.addNewCompoundMark(finish);
        expected.addNewCompoundMark(mark);
        expected.addNewCompoundMark(gate);
        expected.addMarkInOrder(1);
        expected.addMarkInOrder(2);
        expected.addMarkInOrder(3);
        expected.addMarkInOrder(4);
        expected.setWindDirection(200);
        return expected;
    }

    private void assertMarksAreEqual(Mark mark1, Mark mark2){
        Assert.assertEquals(mark1.getPosition().getLat(), mark2.getPosition().getLat(), 0);
        Assert.assertEquals(mark1.getPosition().getLon(), mark2.getPosition().getLon(), 0);
    }

    /** Compares two marks for equality */
    private void assertCompoundMarksAreEqual(CompoundMark compoundMark1, CompoundMark compoundMark2){
        Assert.assertEquals(compoundMark1.getCompoundMarkID(), compoundMark2.getCompoundMarkID());
        Assert.assertEquals(compoundMark1.getName(), compoundMark2.getName());
        assertMarksAreEqual(compoundMark1.getMark1(), compoundMark2.getMark1());
        Assert.assertEquals(compoundMark1.hasTwoMarks(), compoundMark2.hasTwoMarks());
        if (compoundMark1.hasTwoMarks()) {
            assertMarksAreEqual(compoundMark1.getMark2(), compoundMark2.getMark2());
            if (compoundMark1 instanceof RaceLine) {
                Assert.assertTrue(compoundMark2 instanceof RaceLine);
            }
        }
    }

}