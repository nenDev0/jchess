package src.java.intelligence;


import src.java.application.Config;
import src.java.engine.board.Board;
import src.java.engine.board.Move;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.datastructures.TreeHeader;
import src.java.intelligence.evaluation.Calculator;
import src.java.intelligence.evaluation.Configuration;

public class Bot extends Thread
{

    private Calculator calculator;
    private int depth;
    private TreeHeader tree;
    private String gen_path;
    private Type type;


    //TODO: enable better method for resetting the bot (includes setting new Config, new Calculator etc...)
    public Bot(Type type, boolean randomized)
    {
        this.type = type;
        this.gen_path = Config.cfg_handler.info("PATH") + Config.cfg_handler.info("ID") + ".json";
        Configuration config = new Configuration(gen_path, randomized);
        this.calculator = new Calculator(config);
        //System.out.println(info_handler.info("DEPTH"));
        this.depth = Integer.parseInt(Config.cfg_handler.info("DEPTH")) * 2 + 1;
        tree = new TreeHeader(this.depth, new Board(), type);

        //this.time_taken = 0;
        //this.moves_total = 0;
    }

    public Calculator get_calculator()
    {
        return calculator;
    }

    public void m_reset_tree()
    {
        this.tree = new TreeHeader(depth, new Board(), type);
    }

    public void write_config()
    {
        get_calculator().get_configuration().write_config(gen_path, type);
    }

    public void clear_tree()
    {
        tree.m_clear();
    }

    public void m_adjust_tree(Move move)
    {
        tree.m_adjust(move);
    }

    public Move calculate_best_move()
    {
        tree.create_tree(calculator, depth);
        Move move = tree.get_best_move();
        return move;
    }
}
