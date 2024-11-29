package src.java.engine.game;
import java.util.Scanner;

public class MoveReader {
   
    
    private Scanner scanner;
    
    public MoveReader() {
        scanner = new Scanner(System.in);
    }

    public int[] read_next() {
        String console_input;
        int[] vec = new int[2];
        while (true) {
            console_input = scanner.next();
            if (console_input.length() > 1)
                break;
            System.out.println("Your input is too short");
        }
        console_input = console_input.toLowerCase();

        char c_x = console_input.charAt(0);
        String s_y = console_input.substring(1, 2);

        switch (c_x)
        {
            case 'a':
                vec[0] = 0;
                break;
            case 'b':
                vec[0] = 1;
                break;
            case 'c':
                vec[0] = 2;
                break;
            case 'd':
                vec[0] = 3;
                break;
            case 'e':
                vec[0] = 4;
                break;
            case 'f':
                vec[0] = 5;
                break;
            case 'g':
                vec[0] = 6;
                break;
            case 'h':
                vec[0] = 7;
                break;
            default:
                System.out.println("Your input had a syntax error / was out of bounds");
                return read_next();
        }
        vec[1] =  Integer.parseInt(s_y) - 1;
            System.out.println(vec[0] + ", " + vec[1]);
            System.out.println(c_x + ", " + s_y);

        return vec;
    }
}
