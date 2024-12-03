package src.java.intelligence;

import src.java.application.Config;
import src.java.engine.board.Board;
import src.java.engine.board.Move;
import src.java.engine.board.Board.GameState;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.game.InteractionController;


/**
 *  This class manages the Bots.
 *  This includes their: construction, activation, updates, inputs, learning and writing configs.
 * 
 */
public class BotController extends Thread
{
    
    private InteractionController interaction_controller;
    private Bot bot1;
    private Bot bot2;
    private static boolean bot1_active;
    private static boolean bot2_active;


    /**
     * 
     * 
     */
    public BotController()
    {
        bot1 = new Bot(Type.WHITE, false);
        bot2 = new Bot(Type.BLACK, true);
        bot1_active = false;
        bot2_active = false;
    }


    /**
     *  The interaction Controller is needed for bots to input moves.
     * 
     * @param interaction_controller
     */
    public void set_interaction_controller(InteractionController interaction_controller)
    {
        this.interaction_controller = interaction_controller;
    }


    /**
     * 
     * 
     * @param type
     * @param board
     * 
     * @return {@code float}: evaluation
     */
    // TODO: frontend currently has no evals
    public float get_eval(Type type, Board board)
    {
        return get_bot(type).get_calculator().evaluate(board);
    }


    /**
     * 
     */
    private void m_reset_bots()
    {
        bot1 = new Bot(Type.WHITE, false);
        bot2 = new Bot(Type.BLACK, true);
    }


    /**
     * 
     */
    private void m_bots_to_start()
    {
        bot1.m_reset_tree();
        bot2.m_reset_tree();
    }


    /**
     *  Adjusts the trees of the bots by the handed position. 
     * 
     * @param move
     * 
     */
    private void m_adjust_bots(Move move)
    {
        bot1.m_adjust_tree(move);
        bot2.m_adjust_tree(move);
    }


    /**
     *  returns bot with specified type.
     * 
     * @param type
     * 
     * @return {@code WHITE:} {@link #bot1}, {@code BLACK:} {@link #bot2}
     */
    public Bot get_bot(Type type)
    {
        if (type == Type.WHITE)
        {
            return bot1;
        }
        return bot2;
    }


    /**
     *  This is currently irreversible
     * 
     *  <p> Currently static to allow the Frontend to call this method, without needing access to the class itself.
     * 
     * @param i ({@code 1 =}{@link #bot1} , {@code 2 =}{@link #bot2})
     */
    public static void activate_bot(int i)
    {
        switch (i)
        {
            case 1:
                bot1_active = true;
                break;
            default:
                bot2_active = true;
                break;
        }
    }


    /**
     * This would be the method to call for a bot's turn, if the player had only one active.
     * 
     *  <p> not called, while bot-training mode is active.
     *  <p> currently #unused
     *  
     * @param type
     * 
     * @return
     */
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


    /**
     * Only used by the BotController, if the program is currently running in Bot-Training mode.
     * 
     * @param type (of the bot playing)
     * 
     * @return ({@code true}, if the board is final
     *          {@code false}, else)
     */
    private boolean bot_turn_loop(Type type)
    {
        Bot bot = get_bot(type);
        Move move = bot.calculate_best_move();
        System.out.println("move_weight: "+move.get_weight() + ", type: " + type);
        boolean is_final;
        //System.out.println(move);
        try
        {
            GameState state = interaction_controller.m_bot_input(move.position_from(), move.position_to());
            switch (state)
            {
                case CHECKMATE: case DRAW:
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


    /**
     * 
     * 
     */
    private void m_reverse_bot_color()
    {
        Bot bot_temp = bot1;
        bot1 = bot2;
        bot2 = bot_temp;
    }


    /**
     *  This is currently the place to train the bots on themselves. Runs as a thread.
     * 
     */
    @Override
    public void run()
    {
        Type type = Type.WHITE;
        activate_bot(0);
        activate_bot(1);
        System.out.println("Bot play enabled!");
        for (int i = 0; i < 4000; i++)
        {
            for(int j = 0; j < 2; j++)
            {
                System.out.println("bot1: "+ Type.WHITE + ", is randomized, if : " + bot1.get_calculator().get_configuration().is_randomized());
                System.out.println("bot2: "+ Type.BLACK + ", is randomized, if : " + bot2.get_calculator().get_configuration().is_randomized());
                while (!bot_turn_loop(type))
                {
                   type = Type.get_opposite(type);
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