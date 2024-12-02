package src.java.engine.game;



import java.util.LinkedList;

import src.java.engine.board.Board;
import src.java.engine.board.Move;
import src.java.engine.board.Position;
import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;

public class Game {
    
    private Board board;
    private SoundEngine sound_engine;
    private Piece selected_piece;

    public Game()
    {
        this.board = new Board();
        this.sound_engine = new SoundEngine();
        m_reset();
    }

    public void m_set_interaction_controller(InteractionController interaction_controller)
    {
        board.m_set_interaction_controller(interaction_controller);
        for (Piece piece : board.get_collection(Type.WHITE).get_active_pieces())
        {
            interaction_controller.m_update_grid_position(piece.get_position());
        }
        for (Piece piece : board.get_collection(Type.BLACK).get_active_pieces())
        {
            interaction_controller.m_update_grid_position(piece.get_position());
        }
    }

    public void m_reset()
    {
       board.m_to_start();
        board.m_set_normal();

       selected_piece = null;
        /*movecount = 0;
        black_total = 0;
        white_total = 0;*/
    }

    public boolean m_select_piece(int x, int y)
    {
        Piece piece = board.get_position(x, y).get_piece();
        if (piece == null)
        {
            return false;
        }
        if (!piece.is_type(get_turn())) 
        {
            return false;
        }
        this.selected_piece = piece;
        Board clone = board.clone();
        for (Position position : selected_piece.get_legal_moves())
        {
            clone.m_commit(new Move(selected_piece.get_position(), position).convert(clone));
            clone.m_revert();
        }
        return true;
    }

    private void m_deselect_piece()
    {
        this.selected_piece = null;
    }

    public boolean m_select_position(int x, int y)
    {
        Position position = board.get_position(x, y);
        if (position.get_piece() != null)
        {
            if (position.get_piece().is_type(get_turn()))
            {
                m_deselect_piece();
                m_select_piece(position.get_x(), position.get_y());
                return true;
            }
        }
        if(!selected_piece.is_legal_move(position))
        {
            m_deselect_piece();
            return false;
        }
        Piece piece = selected_piece;
        m_deselect_piece();
        board.m_commit(piece.get_position(), position);
        // TODO: adjust tree crashes after reverse, while botplay is disabled.
       //System.out.println(bot1.get_calculator().evaluate(board));
        /*if (board.get_collection(get_turn()).get_active_pieces().size() < piececount.get(get_turn()))
        {

            sound_engine.play_take();
        }
        else
        {*/
        sound_engine.play_move();
        //}
        return false;
    }

    public LinkedList<Position> get_legal_moves()
    {
        return selected_piece.get_legal_moves();
    }

    public Board get_board()
    {
        return board;
    }

    public Type get_turn() {
        return board.get_type();
    }

}