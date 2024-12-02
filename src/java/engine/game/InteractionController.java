package src.java.engine.game;

import src.java.application.BoardGrid;
import src.java.application.Window;
import src.java.engine.board.Position;
import src.java.engine.board.Board.GameState;
import src.java.engine.board.piecelib.Piece;

/**
 * This class deals with all user-inputs and relays their respective responses.
 * 
 */
public class InteractionController
{
    
    /**
     * Determines, whether the user has selected a piece ({@code VisualState.SELECTED}) or not ({@code VisualState.NOMINAL})
     */
    public enum VisualState
    {
        NOMINAL,
        SELECTED

    }

    // TODO: How will this class know, what position_updates to push to the positionlisteners on moves?

    private Game game;
    private BoardGrid grid;
    private Window window;
    private VisualState visual_state;

    public InteractionController(BoardGrid grid, Game game, Window window)
    {
        this.grid = grid;
        this.game = game;
        this.window = window;
        this.visual_state = VisualState.NOMINAL;
    }

    /**
     *  This function handles all calls from the BoardGrid's PositionListeners to the Game.
     * 
     * <p>  - If the position has a piece, the piece and it's legal moves get highlighted.
     *  
     * <p>  - If there is a highlighted piece and this position is a legal move,
     *           the game plays the move out and subsequently removes the highlights.
     * 
     * <p>  - If there is no piece highlighted and the position does not contain a piece,
     *          which is also of the type, who's turn it currently is, nothing happens.
     * 
     * <p>  - If there is a piece selected, however the user clicks on their own pieces,
     *          the new piece becomes the selected one and highlights change accordingly.
     * 
     * 
     * <p>  - If a move is successfully played, the evaluation will be updated in the Window
     * 
     * @param x
     * @param y
     * 
     */
    public void m_click_on_position(int x, int y)
    {
        if (visual_state == VisualState.NOMINAL)
        {
            if (game.m_select_piece(x, y))
            {
                grid.m_highlight_piece(x, y);
                for (Position position : game.get_legal_moves())
                {
                   grid.m_highlight_legal_move(position.get_x(), position.get_y()); 
                }
                visual_state = VisualState.SELECTED;
            }
            return;
        }
        boolean new_selection = game.m_select_position(x, y);
        grid.m_remove_all_highlights();
        if (new_selection)
        {
            grid.m_highlight_piece(x, y);
            for (Position position : game.get_legal_moves())
            {
                grid.m_highlight_legal_move(position.get_x(), position.get_y()); 
            }
        }
        else
        {
            visual_state = VisualState.NOMINAL;
            grid.m_remove_all_highlights();
            //window.m_set_eval(game.get_eval());
        }
    }

    public GameState m_bot_input(Position position_from, Position position_to)
    {
        game.m_select_piece(position_from.get_x(), position_from.get_y());
        game.m_select_position(position_to.get_x(), position_to.get_y());
        //window.m_set_eval(game.get_eval());
        return game.get_board().get_state();
    }


    public void m_set_move_eval(Position position, float eval)
    {
        grid.m_set_grid_position_eval(position.get_x(), position.get_y(), eval);
    }

    /**
     * @param position
     */
    public void m_update_grid_position(Position position)
    {
        Piece piece = position.get_piece();
        if (piece == null)
        {
            grid.m_update_piece(position.get_x(), position.get_y(), null, null);
            return;
        }

        grid.m_update_piece(position.get_x(), position.get_y(), piece.get_type(), piece.get_piece_type());
    }

    public void m_reset()
    {
        game.m_reset();
    }


}