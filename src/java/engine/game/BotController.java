package src.java.engine.game;

import src.java.application.Config;
import src.java.engine.board.Board;
import src.java.engine.board.Move;
import src.java.engine.board.Board.GameState;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.Bot;


public class BotController extends Thread
{
    
    private InteractionController interaction_controller;
    private Bot bot1;
    private static boolean bot1_active;
    private Bot bot2;
    private static boolean bot2_active;
    private boolean lost;


    public BotController()
    {
        lost = false;
        if (bot1 == null)
        {
            bot1 = new Bot(Type.WHITE, false);
            bot2 = new Bot(Type.BLACK, true);
        }
        System.out.println("bot1: "+ Type.WHITE + ", is randomized, if : " + bot1.get_calculator().get_configuration().is_randomized());
        System.out.println("bot2: "+ Type.BLACK + ", is randomized, if : " + bot2.get_calculator().get_configuration().is_randomized());
    }

    public void set_interaction_controller(InteractionController interaction_controller)
    {
        this.interaction_controller = interaction_controller;
    }
    public float get_eval(Type type, Board board)
    {
        return get_bot(type).get_calculator().evaluate(board);
    }

    private void m_reset_bots()
    {
        bot1 = new Bot(Type.WHITE, false);
        bot2 = new Bot(Type.BLACK, true);
    }

    private void m_bots_to_start()
    {
        bot1.m_reset_tree();
        bot2.m_reset_tree();
    }

    public void m_adjust_bots(Move move)
    {
        bot1.m_adjust_tree(move);
        bot2.m_adjust_tree(move);
    }

    public Bot get_bot(Type type)
    {
        if (type == Type.WHITE)
        {
            return bot1;
        }
        return bot2;
    }

    public void let_bot_write_config(Bot bot)
    {
            bot.write_config();
    }

    public static void activate_bot(int i)
    {
        switch (i) {
            case 1:
                bot1_active = true;
                break;
            default:
                bot2_active = true;
                break;
        }
    }

    public boolean bot_turn(Type type)
    {
        switch (type) {
            case WHITE:
                if (!bot1_active)
                {
                    return false;
                }
                break;
            default:
                if (!bot2_active)
                {
                    return false;
                }
                break;
        }
        bot_turn_loop(type);
        return true;
    }

   private boolean bot_turn_loop(Type type)
    {
        Bot bot = get_bot(type);
        Move move = bot.calculate_best_move();
        boolean is_final;
        //System.out.println(move);
        try
        {
            GameState state = interaction_controller.m_bot_input(move.position_from(), move.position_to());
            switch (state)
            {
                case CHECKMATE:
                    is_final = true;
                    break;
                case DRAW:
                    is_final = true;
                    break;
                default:
                    is_final = false;
                    break;
            }
            Thread.yield();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Something went vewwy wong");
        }

        m_adjust_bots(move);

        return is_final;
    }

    private void m_reverse_bot_color()
    {
        Bot bot_temp = bot1;
        bot1 = bot2;
        bot2 = bot_temp;
    }

    @Override
    public void run() {
        Type type = Type.WHITE;
        activate_bot(0);
        activate_bot(1);
        System.out.println("Bot play enabled!");
        for (int i = 0; i < 4000; i++)
        {
            for(int j = 0; j < 2; j++)
            {
                while (!bot_turn_loop(type))
                {
                   Type.get_opposite(type);
                }
                interaction_controller.m_reset();
                m_bots_to_start();
                if (get_bot(type).get_calculator().get_configuration().is_randomized())
                {
                    m_reverse_bot_color();
                    if (j == 1)
                    {
                        get_bot(Type.get_opposite(type)).write_config();
                        Config.m_reset_losses();
                    }
                }
                else
                {
                    Config.m_add_loss();
                    j = 2;
                }
                type = Type.WHITE;
            }
            m_reset_bots();
        }
    }

}