package src.java.engine.game;



import java.util.Optional;
import java.util.Set;

import src.java.engine.board.Board;
import src.java.engine.board.Move;
import src.java.engine.board.Position;
import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;

/**
 *  Ensures, incoming interactions on the {@link #board} are valid.
 * 
 *      Currently handles the {@link #sound_engine}.
 * 
 *  <p> This is, where the information about the currently {@link #selected_piece} on the front-end GUI is saved.
 */
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


    /**
     *  initializes all visual updates to the front-end PositionListeners for each position at the beginning to make the pieces to be visible.
     * 
     *  <p> interaction_controller is not saved in the Game class, but simply passed over to the NotificationCollector.
     * 
     * @param interaction_controller
     */
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


    /**
     * 
     */
    public void m_reset()
    {
        board.m_to_start();
        board.m_set_normal();

       selected_piece = null;
        /*movecount = 0;
        black_total = 0;
        white_total = 0;*/
    }


    /**
     *  Sets the selected_piece, if the input is valid. Allows requests for the legal moves of the selected piece
     * 
     *  <p> Used by the user and bots through the InteractionController
     * 
     *  <p> A valid piece is a non-null piece, who's user's turn it currently is
     * 
     *  @param x
     *  @param y
     * 
     *  @return ({@code true}, if valid piece
     *           {@code false}, else )
     */
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
        return true;
    }


    /**
     * 
     */
    private void m_deselect_piece()
    {
        this.selected_piece = null;
    }


    /**
     *  Sets the selected_piece, if the input is valid. Allows requests for the legal moves of the selected piece
     * 
     *  <p> Used by the user and bots through the InteractionController
     * 
     *  <p> A valid position is one of the currently selected piece's legal moves.
     *  <p> alternatively it can be a position, which currently has a piece of the same type(WHITE/BLACK)
     * 
     *  @param x
     *  @param y
     * 
     *  @return ({@code true}, if a new piece was selected instead,
     *           {@code false}, if the move is either invalid or has been played out )
     * 
     */
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
        Optional<Move> move = selected_piece.get_legal_move(position);
        if(move.isEmpty())
        {
            m_deselect_piece();
            return false;
        }
        m_deselect_piece();
        board.m_commit(move.get());
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


    /**
     *  returns the legal moves of the selected piece
     * 
     *  @return ({@code null}, if no piece is selected,
     *           {@code LinkedList<Position>}, legal moves of the selected piece)
     */
    public Set<Position> get_legal_moves()
    {
        if (selected_piece == null)
        {
            return null;
        }
        return selected_piece.get_legal_moves().keySet();
    }


    /**
     * 
     * @return
     */
    public Board get_board()
    {
        return board;
    }


    /**
     *  Currently #unused 
     *  
     * @return
     */
    public Type get_turn() {
        return board.get_type();
    }

}