package src.test;

import static org.junit.Assert.*;

import java.util.LinkedList;

import org.junit.Test;

import src.java.engine.board.Board;
import src.java.engine.board.PieceCollection;
import src.java.engine.board.Position;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.piecelib.Piece.PieceType;
import src.java.engine.game.Game;


public class level0Test {
    public void set_up() {

    }


    @Test
    public void board_initialisation_correct() {
        Game game = new Game();
        Board board = game.get_board();
        board.m_initialise();

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                assertEquals(board.get_position(x, y).get_x(), x);
                assertEquals(board.get_position(x, y).get_y(), y);
            }
        }

        PieceCollection white_collection = board.get_collection(Type.WHITE);
        PieceCollection black_collection = board.get_collection(Type.BLACK);


        assertEquals(white_collection.get_type(), Type.WHITE);
        assertEquals(black_collection.get_type(), Type.BLACK);
        LinkedList<Position> correct_legal_positions = new LinkedList<Position>();

        //king
        assertEquals(
            correct_legal_positions,
            board.get_collection(Type.WHITE).get_pieces_of_type(PieceType.KING).get(0).get_legal_moves());
        //queen
        assertEquals(correct_legal_positions, white_collection.get_pieces_of_type(PieceType.QUEEN).get(0).get_legal_moves());
        assertEquals(correct_legal_positions, black_collection.get_pieces_of_type(PieceType.QUEEN).get(0).get_legal_moves());

        //rooks
        assertEquals(correct_legal_positions, white_collection.get_pieces_of_type(PieceType.ROOK).get(0).get_legal_moves());
        assertEquals(correct_legal_positions, black_collection.get_pieces_of_type(PieceType.ROOK).get(0).get_legal_moves());
        assertEquals(correct_legal_positions, white_collection.get_pieces_of_type(PieceType.ROOK).get(1).get_legal_moves());
        assertEquals(correct_legal_positions, black_collection.get_pieces_of_type(PieceType.ROOK).get(1).get_legal_moves());

        //bishops
        assertEquals(correct_legal_positions, white_collection.get_pieces_of_type(PieceType.BISHOP).get(0).get_legal_moves());
        assertEquals(correct_legal_positions, black_collection.get_pieces_of_type(PieceType.BISHOP).get(0).get_legal_moves());
        assertEquals(correct_legal_positions, white_collection.get_pieces_of_type(PieceType.BISHOP).get(1).get_legal_moves());
        assertEquals(correct_legal_positions, black_collection.get_pieces_of_type(PieceType.BISHOP).get(1).get_legal_moves());

        //knights
        correct_legal_positions.add(game.get_board().get_position(2, 2));
        correct_legal_positions.add(game.get_board().get_position(0, 2));
        assertEquals(correct_legal_positions, white_collection.get_pieces_of_type(PieceType.KNIGHT).get(0).get_legal_moves());
        correct_legal_positions.clear();

        correct_legal_positions.add(game.get_board().get_position(7, 2));
        correct_legal_positions.add(game.get_board().get_position(5, 2));
        assertEquals(correct_legal_positions, white_collection.get_pieces_of_type(PieceType.KNIGHT).get(1).get_legal_moves());
        correct_legal_positions.clear();

        correct_legal_positions.add(game.get_board().get_position(2, 5));
        correct_legal_positions.add(game.get_board().get_position(0, 5));
        assertEquals(correct_legal_positions, black_collection.get_pieces_of_type(PieceType.KNIGHT).get(0).get_legal_moves());
        correct_legal_positions.clear();

        correct_legal_positions.add(game.get_board().get_position(7, 5));
        correct_legal_positions.add(game.get_board().get_position(5, 5));
        assertEquals(correct_legal_positions, black_collection.get_pieces_of_type(PieceType.KNIGHT).get(1).get_legal_moves());
        correct_legal_positions.clear();
        
        //pawns
        for (int i = 0 ; i < 8 ; i++) {
            correct_legal_positions.add(game.get_board().get_position(i, 2));
            correct_legal_positions.add(game.get_board().get_position(i, 3));
            

            assertEquals(correct_legal_positions, white_collection.get_pieces_of_type(PieceType.PAWN).get(i).get_legal_moves());

            correct_legal_positions.clear();
        }


    }

}
