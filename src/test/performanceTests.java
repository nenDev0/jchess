package src.test;



import org.junit.Test;
import static org.junit.Assert.*;

import src.java.engine.board.Board;
import src.java.engine.board.piecelib.Piece;



public class performanceTests
{

    @Test
    public void moving_pieces_backend()
    {
        int runs = 40000;
        long time = 0;
        Board board = new Board();
        board.m_initialise();


        time = System.nanoTime();
        for (int i = 0; i < runs; i++)
        {
            Openings.m_caro_kann(board);

            board.m_to_start();

            Openings.m_catalan(board);
            board.m_to_start();

            Openings.m_london(board);
            board.m_to_start();

            Openings.m_sicilian_najdorf(board);
            board.m_to_start();

            Openings.m_vienna(board);
            board.m_to_start();
        }
        time = System.nanoTime() - time;
        System.out.println("moving_pieces_backend - time taken: " + time/1000 +  "microseconds");
    }

    @Test
    public void bot_tree_algorithm()
    {   
        int runs = 40000;
        long time = 0;
        long start = 0;
        Board board = new Board();
        board.m_initialise();


        

    }


    @Test
    public void history_reduction_algorithm()
    {
        int runs = 40000;
        long time = 0;
        long start = 0;
        Board board = new Board();
        board.m_initialise();

        for (int i = 0; i < runs; i++)
        {
            Openings.m_caro_kann(board);

            start = System.nanoTime();
            board.get_history().get_as_vectors(0);
            time += System.nanoTime() - start;
            board.m_to_start();

            Openings.m_catalan(board);
            start = System.nanoTime();
            board.get_history().get_as_vectors(0);
            time += System.nanoTime() - start;
            board.m_to_start();

            Openings.m_london(board);
            start = System.nanoTime();
            board.get_history().get_as_vectors(0);
            time += System.nanoTime() - start;
            board.m_to_start();

            Openings.m_sicilian_najdorf(board);
            start = System.nanoTime();
            board.get_history().get_as_vectors(0);
            time += System.nanoTime() - start;
            board.m_to_start();

            Openings.m_vienna(board);
            start = System.nanoTime();
            board.get_history().get_as_vectors(0);
            time += System.nanoTime() - start;
            board.m_to_start();
        }
        System.out.println("history_reduction_algorithm - time taken: " + time/1000 +  "microseconds");
    }
    

}
