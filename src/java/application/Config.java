package src.java.application;

import java.io.File;
import java.util.HashMap;

import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.evaluation.JsonHandler;

public class Config {
    
    //TODO implement this to include all configurable settings
    
    public static JsonHandler cfg_handler;

    public static final String themes_path = "src/resources/icons/";
    public static final String audios_path = "src/resources/sounds/";
    public static final String default_theme = "default_theme/";
    public static final String sacred_theme = "sacred_theme/";
    public static final String skooter_theme = "skooter_theme/";



    public static boolean white_randomized;
    public static boolean black_randomized;

    private static final float RANDOMIZER_MAX_DEVIATION = (float)4.0;
    public static final float RANDOMIZER_POSITIVE_NEGATIVE_DIFF = (float) 1.0;
    private static final float RANDOMIZER_RANDOM_CHANCE = (float)0.03;
    public static HashMap<Type, Integer> losses;
    private static Type losing;


    public Config()
    {
        losses = new HashMap<Type, Integer>();
        cfg_handler = new JsonHandler(new File("src/resources/configs/config.json"));
        for (Type type : Type.values())
        {
            losses.putIfAbsent(type, 0);
            System.out.println(losses.get(type));
        }
        losing = Type.BLACK;
    }


    public static Type get_loser()
    {
        return losing;
    }

    public static float get_max_deviation()
    {
        return RANDOMIZER_MAX_DEVIATION + RANDOMIZER_MAX_DEVIATION * losses.get(losing)/32;
    }

    public static float get_random_chance()
    {
        return RANDOMIZER_RANDOM_CHANCE + RANDOMIZER_RANDOM_CHANCE * losses.get(losing)/64;
    }

    public static void m_add_loss()
    {
        m_add_loss(losing);
    }

    public static void m_add_loss(Type type)
    {
        if (type != losing && losing != null)
        {
            losses.put(losing, 0);
            losing = type;
        }
        losses.putIfAbsent(type, 0);
        losses.put(type, losses.get(type) + 1);
        System.out.println("losses: " + losses.get(type) + ", by: " + losing);
    }
}