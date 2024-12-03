package src.java.application;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import src.java.engine.board.Board;
import src.java.engine.board.piecelib.Piece.PieceType;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.game.Game;
import src.java.engine.game.InteractionController;
import src.java.intelligence.BotController;


public class BoardGrid {
    
    private JPanel panel;
    private Game game;
    private PositionListener[][] listeners;
    private ImageFinder imagefinder;
    private Dimension dimension;
    private List<PositionListener> ll_highlighted_legal_moves;
    private PositionListener highlighted_piece;
    private InteractionController interaction_controller;

    public BoardGrid(Dimension dimension, Window window, BotController bot_controller)
    {
        m_update_dimension(dimension);
        this.imagefinder = new ImageFinder((int)get_dimension().getWidth(), (int)get_dimension().getHeight());
        this.panel = new JPanel();
        this.listeners = new PositionListener[8][8];
        this.ll_highlighted_legal_moves = new LinkedList<PositionListener>();
  
        for     (int y = 7; y >= 0; y--)
        {
            for (int x = 0; x < 8; x++)
            {
                listeners[x][y] = new PositionListener(x, y, this);
                panel.add(listeners[x][y].get_pane());
            }
        }


        this.game = new Game();
        this.game.get_board().m_initialise();
        this.interaction_controller = new InteractionController(this, game, window);
        bot_controller.set_interaction_controller(interaction_controller);
        game.m_set_interaction_controller(interaction_controller);
        panel.setLayout(new GridLayout(8, 8));
        panel.setVisible(true);
        panel.setPreferredSize(this.dimension);
    
    }

    /// ### getters ### ///

    public Dimension get_dimension()
    {
        return dimension;
    }
    public JPanel get_panel()
    {
        return panel;
    }

    public Board get_board()
    {
        return game.get_board();
    }

    public Game get_game()
    {
        return game;
    }

    public void m_send_input(int x, int y)
    {
        interaction_controller.m_click_on_position(x, y);
    }

    public void m_update_piece(int x, int y, Type type, PieceType piece_type)
    {
        listeners[x][y].m_update_piece(type, piece_type);
    }

    public void m_set_grid_position_eval(int x , int y, float eval)
    {
        listeners[x][y].m_set_eval(eval);
    }
    
    public void m_highlight_legal_move(int x, int y)
    {
        PositionListener position_listener = this.listeners[x][y];
        ll_highlighted_legal_moves.add(position_listener);
        position_listener.m_legal_move_activate();
    }

    public void m_highlight_piece(int x, int y)
    {
        this.highlighted_piece = listeners[x][y];
        highlighted_piece.m_select();
    }

    public void m_remove_all_highlights()
    {
        for (PositionListener position_listener : ll_highlighted_legal_moves) {
            position_listener.m_legal_move_deactivate();
        }
        if (highlighted_piece != null)
        {
            highlighted_piece.m_deselect();
        }
        ll_highlighted_legal_moves.clear();
        highlighted_piece = null;
    }

    public ImageFinder get_imagefinder()
    {
        return imagefinder;
    }

    public void m_update_dimension(Dimension dimension)
    {
        this.dimension = new Dimension((int)(dimension.getWidth()*0.95), (int)(dimension.getHeight()*0.95));
    }

}
