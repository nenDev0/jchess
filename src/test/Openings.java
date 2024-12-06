package src.test;

import src.java.engine.board.Board;

public class Openings {


    public static void m_london(Board board)
    {
        //System.out.println(">>>>>>> LONDON");
        board.m_commit(board.get_position(3, 1), board.get_position(3, 3));
        board.m_commit(board.get_position(3, 6), board.get_position(3, 4));
        board.m_commit(board.get_position(2, 0), board.get_position(5, 3));
        board.m_commit(board.get_position(6, 7), board.get_position(5, 5));
        board.m_commit(board.get_position(4, 1), board.get_position(4, 2));
        board.m_commit(board.get_position(4, 6), board.get_position(4, 5));
    }

    public static void m_sicilian_najdorf(Board board)
    {
        //System.out.println(">>>>>>> SICILIAN NAJDORF");
        // white pawn e2-e4
        board.m_commit(board.get_position(4, 1), board.get_position(4, 3));
        // black pawn c7-c5
        board.m_commit(board.get_position(2, 6), board.get_position(2, 4));
        // white knight g2-f3
        board.m_commit(board.get_position(6, 0), board.get_position(5, 2));
        // black pawn d7-d6
        board.m_commit(board.get_position(3, 6), board.get_position(3, 5));
        // white pawn d2-d4
        board.m_commit(board.get_position(3, 1), board.get_position(3, 3));
        // black pawn c5-d4
        board.m_commit(board.get_position(2, 4), board.get_position(3, 3));
        // white knight f3-d4
        board.m_commit(board.get_position(5, 2), board.get_position(3, 3));
        // black knight g8-f6
        board.m_commit(board.get_position(6, 7), board.get_position(5, 5));
        // white knight b1-c3
        board.m_commit(board.get_position(1, 0), board.get_position(2, 2));
        // black pawn a7-a6
        board.m_commit(board.get_position(0, 6), board.get_position(0, 5));
    }

    public static void m_vienna(Board board)
    {
        //System.out.println(">>>>>>> VIENNA");
        // white pawn e2-e4
        board.m_commit(board.get_position(4, 1), board.get_position(4, 3));
        // black pawn e7-e5
        board.m_commit(board.get_position(4, 6), board.get_position(4, 4));
        // white bishop f1-c4
        board.m_commit(board.get_position(5, 0), board.get_position(2, 3));
        // black knight g8-f6
        board.m_commit(board.get_position(6, 7), board.get_position(5, 5));
        // white pawn d2-d3
        board.m_commit(board.get_position(3, 1), board.get_position(3, 2));
        // black pawn c7-c
        board.m_commit(board.get_position(2, 6), board.get_position(2, 5));

    }

    public static void m_caro_kann(Board board)
    {
        board.m_commit(board.get_position(4, 1), board.get_position(4, 3));
        board.m_commit(board.get_position(2, 6), board.get_position(2, 5));
        board.m_commit(board.get_position(3, 1), board.get_position(3, 3));
        board.m_commit(board.get_position(3, 6), board.get_position(3, 4));
        board.m_commit(board.get_position(4, 3), board.get_position(4, 4));
        board.m_commit(board.get_position(2, 7), board.get_position(5, 4));
        board.m_commit(board.get_position(6, 0), board.get_position(5, 2));
        board.m_commit(board.get_position(4, 6), board.get_position(4, 5));
    }    

    public static void m_catalan(Board board)
    {
        //System.out.println(">>>>>>> CATALAN");
        board.m_commit(board.get_position(3, 1), board.get_position(3, 3));
        board.m_commit(board.get_position(3, 6), board.get_position(3, 4));
        board.m_commit(board.get_position(2, 1), board.get_position(2, 3));
        board.m_commit(board.get_position(4, 6), board.get_position(4, 5));
        board.m_commit(board.get_position(6, 0), board.get_position(5, 2));
        board.m_commit(board.get_position(6, 7), board.get_position(5, 5));

    }

    
}
