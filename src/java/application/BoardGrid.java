package src.java.application;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;

import src.java.engine.board.Board;
import src.java.engine.game.Game;


public class BoardGrid {
    
    private JPanel panel;
    private Game game;
    private PositionListener[][] listeners;
    private ImageFinder imagefinder;
    private Dimension dimension;

    public BoardGrid(Dimension dimension, Window window)
    {
        m_update_dimension(dimension);
        this.game = new Game();
        this.game.m_set_window(window);
        this.game.get_board().m_initialise();
        imagefinder = new ImageFinder((int)get_dimension().getWidth(), (int)get_dimension().getHeight());
        this.listeners = new PositionListener[8][8];
        this.panel = new JPanel();
        panel.setLayout(new GridLayout(8, 8));

        for     (int y = 7; y >= 0; y--)
        {
            for (int x = 0; x < 8; x++)
            {
                listeners[x][y] = new PositionListener(x, y, this);
                panel.add(listeners[x][y].get_pane());
                game.get_board().get_position(x, y).m_set_listener(listeners[x][y]);
            }
        }
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

    public ImageFinder get_imagefinder()
    {
        return imagefinder;
    }

    public void m_update_dimension(Dimension dimension)
    {
        this.dimension = new Dimension((int)(dimension.getWidth()*0.95), (int)(dimension.getHeight()*0.95));
    }

}
