package src.main.java.controllers;

import com.sun.javafx.application.LauncherImpl;

/**
 * Created by Devin on 16/05/17.
 */
public class Launcher {
    public static void main( String[] args )
    {
        LauncherImpl.launchApplication(Main.class, SplashScreenLoader.class, args);
    }
}
