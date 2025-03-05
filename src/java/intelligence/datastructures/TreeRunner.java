package src.java.intelligence.datastructures;

import java.util.TreeSet;

import src.java.engine.board.Board;
import src.java.intelligence.evaluation.Calculator;

public class TreeRunner implements Runnable
{

    private Board board;
    private TreeHeader header;
    private Calculator calculator;


    public TreeRunner(Board board, Calculator calculator, TreeHeader header)
    {
        this.header = header;
        this.board = board.clone();
        this.calculator = calculator;
    }





    @Override
    public void run()
    {
        TreeSet<MoveNode> set_stack = header.get_stack();
        while (set_stack.size() != 0)
        {
           MoveNode node = set_stack.pollFirst(); 

           node.m_create_childnodes(board, calculator);
        }
    }
    
}
