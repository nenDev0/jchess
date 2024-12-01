package src.java.application;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.piecelib.Piece.PieceType;

public class PositionListener implements MouseListener
{

    private JLayeredPane pane;
    private BoardGrid grid;
    private int x;
    private int y;
    private JLabel background;
    private JLabel selection;
    private JLabel legal_move_layer;
    private JLabel piece;
    private JLabel hover;
    private Dimension dimension;
    private JLabel evaluation;

    public PositionListener(int x, int y, BoardGrid grid)
    {
        m_update_dimension(grid.get_dimension());
        this.x = x;
        this.y = y;
        hover = create_label(grid.get_imagefinder().get_hover());
        legal_move_layer = create_label(grid.get_imagefinder().get_legal_move());
        legal_move_layer.setVisible(false);
        selection = create_label(grid.get_imagefinder().get_selection());
        background = create_label(grid.get_imagefinder().get_background(x, y));
        background.setVisible(true);
        evaluation = new JLabel();
        evaluation.setForeground(new Color(0, 0, 0));
        evaluation.setBounds(0, 0, (int)this.dimension.getWidth(), (int)this.dimension.getHeight());

        piece = new JLabel();


        pane = new JLayeredPane();
        pane.setBounds(0, 0, (int)this.dimension.getWidth(), (int)this.dimension.getHeight());

        evaluation.setVisible(false);
        selection.setVisible(false);
        hover.setVisible(false);


        pane.addMouseListener(this);
        pane.setVisible(true);
        m_set_default_layering();
        this.grid = grid;
    }


    /// ### getters ### ///
    public JLayeredPane get_pane()
    {
        return pane;
    }


    /// ### modifiers ### ///

    public void m_set_default_layering()
    {
        pane.removeAll();
        //pane.add(evaluation, 0);
        pane.add(hover, 1);
        pane.add(selection, 2);
        pane.add(legal_move_layer, 3);
        pane.add(piece, 4);
        pane.add(background, 5); 
    }


    public void m_update_dimension(Dimension dimension)
    {
        this.dimension = new Dimension((int)(dimension.getWidth()/8), (int)(dimension.getHeight())/8);
    }

    public void m_select()
    {
            selection.setVisible(true);
    }

    public void m_deselect()
    {
            selection.setVisible(false);
    }

    public void m_legal_move_activate()
    {
        legal_move_layer.setVisible(true);
        evaluation.setVisible(true);
    }

    public void m_legal_move_deactivate()
    {
        legal_move_layer.setVisible(false);
        evaluation.setVisible(false);

    }

    public void m_update_piece(Type type, PieceType impl)
    {

        if (type == null)
        {
            piece = new JLabel();
        }
        else
        {
            piece = create_label(grid.get_imagefinder().get_piece(type, impl));
            piece.setVisible(true);
        }
        m_set_default_layering();
    }

    public void m_set_eval(float eval)
    {
        this.evaluation.setText(Float.toString(eval));
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
       grid.m_send_input(x, y); 
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {

    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        hover.setVisible(true);
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        hover.setVisible(false);

    }
    
    private JLabel create_label(ImageIcon image)
    {
        JLabel label = new JLabel(image);
        label.setBounds(0, 0, (int)this.dimension.getWidth(), (int)this.dimension.getHeight());
        return label;
    }
}
