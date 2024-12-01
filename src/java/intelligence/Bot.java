package src.java.intelligence;

import java.io.File;

import src.java.application.Config;
import src.java.engine.board.Board;
import src.java.engine.board.Move;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.evaluation.Calculator;
import src.java.intelligence.evaluation.Configuration;

public class Bot extends Thread
{

    private Type type;
    private Calculator calculator;
    private int depth;
    private TreeHeader tree;
    private String gen_path;

    // Telemetry
    //private long time_taken;
    //private long moves_total;

    public Bot(Type type, boolean randomized)
    {
        this.type = type;
        this.gen_path = Config.cfg_handler.info("PATH") + Config.cfg_handler.info("ID") + ".json";
        Configuration config = new Configuration(new File(gen_path), randomized);
        this.calculator = new Calculator(config);
        //System.out.println(info_handler.info("DEPTH"));
        this.depth = Integer.parseInt(Config.cfg_handler.info("DEPTH")) * 2 + 1;
        tree = new TreeHeader(this.depth, this.type, new Board());

        //this.time_taken = 0;
        //this.moves_total = 0;
    }

    public Calculator get_calculator()
    {
        return calculator;
    }


    public void m_reset_tree()
    {
        this.tree = new TreeHeader(depth, type, new Board());
    }


    public Type get_type()
    {
        return type;
    }


    public TreeHeader get_tree()
    {
        return tree;
    }

    public void write_config()
    {
        get_calculator().get_configuration().m_i_forgot();
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
        //moves_total++;
        //long start = System.nanoTime() / 1000000;
        tree.create_Tree(calculator, depth);
        Move move = tree.get_best_move();
        //long stop = System.nanoTime() / 1000000;
        //time_taken += stop - start;
        //System.out.println("Bot average calculation time: " + (time_taken / moves_total));
        return move;
    }
}
