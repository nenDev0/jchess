package src.java.application;

import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.evaluation.JsonHandler;

public class Config {
    
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
    private static int losses;


    public Config()
    {
        cfg_handler = new JsonHandler("src/resources/configs/config.json");
        losses = 0;
    }

    public static float get_max_deviation()
    {
        return RANDOMIZER_MAX_DEVIATION + RANDOMIZER_MAX_DEVIATION * (float)losses/32;
    }

    public static float get_random_chance()
    {
        return RANDOMIZER_RANDOM_CHANCE + RANDOMIZER_RANDOM_CHANCE * (float)losses/64;
    }

    public static void m_reset_losses()
    {
        losses = 0;
    }

    public static void m_add_loss()
    {
        losses++;
        System.out.println("losses: " + losses);
    }
}