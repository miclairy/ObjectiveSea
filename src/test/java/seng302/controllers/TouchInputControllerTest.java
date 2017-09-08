package seng302.controllers;

import org.junit.Test;

/**
 * Created by gemma on 8/09/2017.
 */
public class TouchInputControllerTest {

    @Test
    public void checkRotationTest() {
        boolean upwind = TouchInputController.checkRotation(200.0, 270.0, 0.0);
        boolean downwind = TouchInputController.checkRotation(45.0, 90.0, 0.0);
        assert(upwind);
        assert(!downwind);

        boolean upwind1 = TouchInputController.checkRotation(350.0, 10.0, 45.0);
        boolean downwind1 = TouchInputController.checkRotation(10.0, 350.0, 45.0);
        assert(upwind1);
        assert(downwind1);

        boolean upwind2 = TouchInputController.checkRotation(350.0, 1.0, 45.0);
        assert(upwind2);
    }
}
