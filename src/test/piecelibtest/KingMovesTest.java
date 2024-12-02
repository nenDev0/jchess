package src.test.piecelibtest;

import java.util.HashSet;


import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import src.java.engine.board.Board;
import src.java.engine.board.Position;
import src.java.engine.board.piecelib.*;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.piecelib.Piece.PieceType;


public class KingMovesTest {

    private Board board;
    private Piece white_king;
    private Piece black_king;
    private HashSet<Position> expected;
    private HashSet<Position> actual;

    
    @Before
    public void setup() {
        board = new Board();
        white_king = board.get_collection(Type.WHITE).get_pieces_of_type(PieceType.KING).get(0);
        black_king = board.get_collection(Type.BLACK).get_pieces_of_type(PieceType.KING).get(0);

        expected = new HashSet<Position>();
        actual = new HashSet<Position>();
    }
    
    @Test
    public void edge_test_1() {

    ////// ####### ####### //////
        white_king.m_set_position(board.get_position(0, 0));
        black_king.m_set_position(board.get_position(7, 7));
        board.m_dump_update_notifications();

        expected.add(board.get_position(1, 0));
        expected.add(board.get_position(1, 1));
        expected.add(board.get_position(0, 1));

        actual.addAll(white_king.get_legal_moves());
        // TODO: solve this -> Castling? maybe a reversing issue
        assertEquals(expected, actual );

        actual = new HashSet<Position>();
        actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);
    }

        @Test
        public void edge_test_2() {

    ////// ####### ####### //////
        white_king.m_set_position(board.get_position(7, 7));
        black_king.m_set_position(board.get_position(0, 0));

        board.m_dump_update_notifications();

        expected.add(board.get_position(7, 6));
        expected.add(board.get_position(6, 7));
        expected.add(board.get_position(6, 6));

        actual.addAll(white_king.get_legal_moves());
        assertEquals(expected, actual);

        actual = new HashSet<Position>();
        actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);
    }

    @Test
    public void taking_pieces() {
        ////// ####### ####### //////
        //<y>
        // 7:                      wK
        // 6:                      wp
        // 5:                        
        // 4:                        
        // 3:                        
        // 2:                        
        // 1:                        
        // 0: bK                     
        //    -- -- -- -- -- -- -- --
        //     a  b  c  d  e  f  g  h
        //     0  1  2  3  4  5  6  7 <x>

        Piece white_pawn = board.get_collection(Type.WHITE).get_pieces_of_type(PieceType.PAWN).get(0);

    ////// ####### ####### //////
        white_pawn.m_set_position(board.get_position(7, 6));
        white_king.m_set_position(board.get_position(7, 7));
        black_king.m_set_position(board.get_position(0, 0));

        board.m_dump_update_notifications();

        expected.add(board.get_position(7, 6));
        expected.add(board.get_position(6, 7));
        expected.add(board.get_position(6, 6));

        actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);

        expected.remove(board.get_position(7, 6));
        actual = new HashSet<Position>(); actual.addAll(white_king.get_legal_moves());
        assertEquals(expected, actual);

        expected.clear();

    ////// ####### change ####### //////
        //<y>
        // 7: bK xo                  
        // 6: wp oo                wK
        // 5:                        
        // 4:                        
        // 3:                        
        // 2:                        
        // 1:                        
        // 0:                        
        //    -- -- -- -- -- -- -- --
        //     a  b  c  d  e  f  g  h
        //     0  1  2  3  4  5  6  7 <x>
        black_king.m_set_position(board.get_position(0, 7));
        white_king.m_set_position(board.get_position(7, 6));

        white_pawn.m_set_position(board.get_position(0, 6));
        white_pawn.get_collection().m_untake(white_pawn);
        board.m_dump_update_notifications();

        expected.add(board.get_position(0, 6));
        expected.add(board.get_position(1, 6));
        expected.add(board.get_position(1, 7));


        actual = new HashSet<Position>(); actual.addAll(black_king.observer().get_observed_positions());
        assertEquals(expected, actual);

        expected.remove(board.get_position(1, 7));

        actual = new HashSet<Position>(); actual.addAll(black_king.get_legal_moves());
        assertEquals(expected, actual);


        black_king.m_set_position(board.get_position(0, 6));
        assertEquals(7, white_king.get_collection().get_pieces_of_type(PieceType.PAWN).size());    
    }


    @Test
    public void avoiding_enemy_control() {
        ////// ####### ####### //////
        // 7:                   xo oo
        // 6:                   xo wK
        // 5:                   xo oo
        // 4:                   :     
        // 3:                   :    
        // 2:                   :    
        // 1: xo xo .. .. .. .. br   
        // 0: bK oo                  
        //    -- -- -- -- -- -- -- --
        //     a  b  c  d  e  f  g  h

    ////// ####### ####### //////
        Piece black_rook = board.get_collection(Type.BLACK).get_pieces_of_type(PieceType.ROOK).get(0);
        white_king.m_set_position(board.get_position(7, 6));
        black_king.m_set_position(board.get_position(0, 0));

        board.m_dump_update_notifications();

        expected.add(board.get_position(7, 7));
        expected.add(board.get_position(7, 5));
        expected.add(board.get_position(6, 7));
        expected.add(board.get_position(6, 6));
        expected.add(board.get_position(6, 5));

        actual = new HashSet<Position>(); actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);

        actual = new HashSet<Position>(); actual.addAll(white_king.get_legal_moves());
        assertEquals(expected, actual);

    ////// ####### change ####### //////
        black_rook.m_set_position(board.get_position(6, 1));
        board.m_dump_update_notifications();

        actual = new HashSet<Position>(); actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);


        expected.remove(board.get_position(6, 7));
        expected.remove(board.get_position(6, 5));
        expected.remove(board.get_position(6, 6));


        actual = new HashSet<Position>(); actual.addAll(white_king.get_legal_moves());
        assertEquals(expected, actual);

        expected.clear();

    // black king
        expected.add(board.get_position(0, 1));
        expected.add(board.get_position(1, 0));
        expected.add(board.get_position(1, 1));

        actual = new HashSet<Position>(); actual.addAll(black_king.get_legal_moves());
        assertEquals(expected, actual);

        actual = new HashSet<Position>(); actual.addAll(black_king.observer().get_observed_positions());
        assertEquals(expected, actual);
    }

    @Test
    public void king_and_king() {
        // ///// ######  ###### ///// //
        //<y>                         //
        // 7:                         // 
        // 6:                         // 
        // 5:                         //
        // 4:                         //
        // 3:                         //
        // 2: oo xo oo oo             //
        // 1: wK xo bK oo             //
        // 0: oo xo oo oo             //
        //    -- -- -- -- -- -- -- -- //
        //     a  b  c  d  e  f  g  h
        //     0  1  2  3  4  5  6  7 <x>


        white_king.m_set_position(board.get_position(0, 1));
        black_king.m_set_position(board.get_position(2, 1));

        board.m_dump_update_notifications();

        expected.add(board.get_position(0, 0));
        expected.add(board.get_position(1, 0));
        expected.add(board.get_position(1, 1));
        expected.add(board.get_position(1, 2));
        expected.add(board.get_position(0, 2));

        // white king
        actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);

        expected.remove(board.get_position(1, 0));
        expected.remove(board.get_position(1, 1));
        expected.remove(board.get_position(1, 2));

        actual = new HashSet<Position>(); actual.addAll(white_king.get_legal_moves());
        assertEquals(expected, actual);

        expected.clear();

        // black king
        expected.add(board.get_position(1, 0));
        expected.add(board.get_position(1, 1));
        expected.add(board.get_position(1, 2));
        expected.add(board.get_position(2, 0));
        expected.add(board.get_position(2, 2));
        expected.add(board.get_position(3, 0));
        expected.add(board.get_position(3, 1));
        expected.add(board.get_position(3, 2));

        actual = new HashSet<Position>(); actual.addAll(black_king.observer().get_observed_positions());
        assertEquals(expected, actual);
        
        expected.remove(board.get_position(1, 0));
        expected.remove(board.get_position(1, 1));
        expected.remove(board.get_position(1, 2));
        
        actual = new HashSet<Position>(); actual.addAll(black_king.get_legal_moves());
        assertEquals(expected, actual);
    }

    @Test
    public void restricting_1() {
        ////// ####### ####### //////
        //<y>
        // 7:          br          bK
        // 6:           :            
        // 5:           :            
        // 4:           :            
        // 3:           :            
        // 2:           :            
        // 1: wr       .:            
        // 0:          wK            
        //    -- -- -- -- -- -- -- --
        //     a  b  c  d  e  f  g  h 
        //     0  1  2  3  4  5  6  7 <x>


        Piece white_rook = board.get_collection(Type.WHITE).get_pieces_of_type(PieceType.ROOK).get(0);
        Piece black_rook = board.get_collection(Type.BLACK).get_pieces_of_type(PieceType.ROOK).get(0);

        white_king.m_set_position(board.get_position(3, 0));
        white_rook.m_set_position(board.get_position(0, 1));
        black_king.m_set_position(board.get_position(7, 7));
        black_rook.m_set_position(board.get_position(3, 7));

        board.m_dump_update_notifications();


        // white king
        expected.add(board.get_position(2, 0));
        expected.add(board.get_position(2, 1));
        expected.add(board.get_position(3, 1));
        expected.add(board.get_position(4, 1));
        expected.add(board.get_position(4, 0));

        actual.clear() ; actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);

        expected.remove(board.get_position(3, 1));

        actual.clear(); actual.addAll(white_king.get_legal_moves());
        assertEquals(expected, actual);

        expected.clear();


        // white rook
        expected.add(board.get_position(3, 1));
        
        actual.clear(); actual.addAll(white_rook.get_legal_moves());
        assertEquals(expected, actual);

        ////// ####### change ####### //////
        //<y>
        // 7:          br          bK
        // 6:          ::            
        // 5:          ::            
        // 4:          ::            
        // 3:          wr            
        // 2:          :             
        // 1:       oo oo oo         
        // 0:       oo<wK>oo         
        //    -- -- -- -- -- -- -- --
        //     a  b  c  d  e  f  g  h
        //     0  1  2  3  4  5  6  7 <x>

        white_rook.m_set_position(board.get_position(3, 3));

        board.m_dump_update_notifications();


        // white king
        expected.add(board.get_position(2, 0));
        expected.add(board.get_position(2, 1));
        expected.add(board.get_position(3, 1));
        expected.add(board.get_position(4, 1));
        expected.add(board.get_position(4, 0));

        actual.clear() ; actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);

        expected.clear();


        // white rook
        expected.add(board.get_position(3, 1));
        expected.add(board.get_position(3, 2));
        expected.add(board.get_position(3, 4));
        expected.add(board.get_position(3, 5));
        expected.add(board.get_position(3, 6));
        expected.add(board.get_position(3, 7));

        actual.clear(); actual.addAll(white_rook.get_legal_moves());
        assertEquals(expected, actual);


    }
    @Test
    public void restricting_2() {
        white_king.m_set_position(board.get_position(3, 0));
        black_king.m_set_position(board.get_position(7, 7));

        Piece white_rook = board.get_collection(Type.WHITE).get_pieces_of_type(PieceType.ROOK).get(0);
        Piece black_rook = board.get_collection(Type.BLACK).get_pieces_of_type(PieceType.ROOK).get(0);

        ////// ####### ####### //////
        //<y>
        // 7:          br          bK
        // 6:           :            
        // 5:           :            
        // 4:           :            
        // 3:           :            
        // 2:        bk :            
        // 1: wr       .:            
        // 0:         <wK>           
        //    -- -- -- -- -- -- -- --
        //     a  b  c  d  e  f  g  h
        //     0  1  2  3  4  5  6  7 <x>

        white_rook.m_set_position(board.get_position(0, 1));
        black_rook.m_set_position(board.get_position(3, 7));

        Piece black_knight = board.get_collection(Type.BLACK).get_pieces_of_type(PieceType.KNIGHT).get(0);
        black_knight.m_set_position(board.get_position(2, 2));

        board.m_dump_update_notifications();
    
        // white king
        expected.add(board.get_position(2, 0));
        expected.add(board.get_position(2, 1));
        expected.add(board.get_position(3, 1));
        expected.add(board.get_position(4, 1));
        expected.add(board.get_position(4, 0));

        actual.clear() ; actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);

        expected.remove(board.get_position(3, 1));
        expected.remove(board.get_position(4, 1));

        actual.clear(); actual.addAll(white_king.get_legal_moves());
        assertEquals(expected, actual);

        expected.clear();

        // white rook

        actual.clear(); actual.addAll(white_rook.get_legal_moves());
        assertEquals(expected, actual);


        ////// ####### change ####### //////
        //<y>
        // 7:          br          bK
        // 6:          ::            
        // 5:          ::            
        // 4:          ::            
        // 3:          wr            
        // 2:       bk :             
        // 1:       oo oo xo         
        // 0:       oo<wK>oo         
        //    -- -- -- -- -- -- -- --
        //     a  b  c  d  e  f  g  h
        //     0  1  2  3  4  5  6  7 <x>

        white_rook.m_set_position(board.get_position(3, 3));

        board.m_dump_update_notifications();
    
        // white king
        expected.add(board.get_position(2, 0));
        expected.add(board.get_position(2, 1));
        expected.add(board.get_position(3, 1));
        expected.add(board.get_position(4, 1));
        expected.add(board.get_position(4, 0));

        actual.clear() ; actual.addAll(white_king.observer().get_observed_positions());
        assertEquals(expected, actual);

        expected.remove(board.get_position(4, 1));

        actual.clear(); actual.addAll(white_king.get_legal_moves());
        assertEquals(expected, actual);
        
        expected.clear();
        

        // white rook
        actual.clear(); actual.addAll(white_rook.get_legal_moves());
        assertEquals(expected, actual);
    }
}
