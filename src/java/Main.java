package src.java;

import src.java.application.Window;

import src.java.application.Config;

public class Main {


    @SuppressWarnings("unused")
    public static void main(String[] args)
    {
        Config config = new Config();
        Window window = new Window();
        window.run();
    }
    
}
