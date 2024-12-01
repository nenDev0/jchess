package src.test.piecelibtest;


import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import src.java.engine.board.Board;
import src.java.engine.board.piecelib.*;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.piecelib.Piece.PieceType;


public class PawnMovesTest {

    private Board board;
    private Piece white_king;
    private Piece black_king;
    private Piece white_pawn;
    private Piece black_pawn;

    //private HashSet<Position> expected;
    //private HashSet<Position> actual;

    
    @Before
    public void setup() {
        board = new Board();
        white_king = board.get_collection(Type.WHITE).get_pieces_of_type(PieceType.KING).get(0);
        black_king = board.get_collection(Type.BLACK).get_pieces_of_type(PieceType.KING).get(0);
        white_pawn = board.get_collection(Type.WHITE).get_pieces_of_type(PieceType.PAWN).get(0);
        black_pawn = board.get_collection(Type.BLACK).get_pieces_of_type(PieceType.PAWN).get(0);

        //expected = new HashSet<Position>();
        //actual = new HashSet<Position>();
    }

    @Test
    public void edge_test_1() {
        
    }

    public void en_passant() {
        white_king.m_set_position(board.get_position(0, 0));
        black_king.m_set_position(board.get_position(3, 0));

        white_pawn.m_set_position(board.get_position(0, 4));
        black_pawn.m_set_position(board.get_position(1, 6));

        board.m_dump_update_notifications();

        black_pawn.m_set_position(board.get_position(1, 4));
        board.m_dump_update_notifications();

        assertTrue(white_pawn.is_legal_move(board.get_position(1, 5)));
        
    }


}