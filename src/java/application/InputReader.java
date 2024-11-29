package src.java.application;

import java.util.Scanner;

public class InputReader
{

    private Scanner scanner;

    public InputReader()
    {
        this.scanner = new Scanner(System.in);
    }
    
    public void listen()
    {
        switch (get_input())
        {
            case "":
                break;
        
            default:
                break;
        }
    }

    public String get_input()
    {
        return scanner.next();
    }

}
