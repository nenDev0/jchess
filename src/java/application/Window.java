package src.java.application;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Container;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import src.java.engine.board.History;


public class Window extends JFrame
{
    
    private Dimension dimension;
    private BoardGrid grid;
    private static JLabel keybindings = new JLabel();
    private Container pane;
    private JLabel evaluation;

    public Window()
    {
        dimension = new Dimension(920, 920);
        
        setTitle("Chess");
        
        setSize(dimension);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        this.pane = getContentPane();
        pane.setLayout(new FlowLayout());
        pane.setBackground(new Color(20, 22 , 25));

        this.grid = new BoardGrid(dimension, this);
        pane.add(grid.get_panel());
        //JButton bot_1 = new JButton("activate bot_1");
        //JButton bot_2 = new JButton("activate bot_2");
        this.evaluation = new JLabel("I AM EVALUATION");
        this.evaluation.setForeground(new Color(255, 255, 255));
        //this.evaluation.setVerticalAlignment(SwingConstants.BOTTOM);

        JButton bots = new JButton("activate bot_play");
        JButton revert = new JButton("revert move");

        revert.addActionListener(new ActionListener()
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                grid.get_game().get_board().m_revert();
            }
            
        });

        bots.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e)
            {
                bot_play();
            }
        });
        pane.add(bots);
        pane.add(revert);
        pane.add(evaluation);
        this.evaluation.setBounds(0, 0, evaluation.getWidth(), evaluation.getHeight());

        keybindings.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Left"), "reverse");
        keybindings.getActionMap().put("reverse", new History_Reverse(grid.get_board().get_history()));
    }
    
    public void m_set_eval(float eval)
    {
        this.evaluation.setText("evaluation: " + eval);
    }

    public void bot_play()
    {
        Thread t = grid.get_game();
        t.setPriority(1);
        t.start();
        //grid.get_game().bot_turn();

    }

    public void run()
    {
        EventQueue.invokeLater(new Runnable()
        {

                @Override
                public void run()
                {
                    Window m = new Window();
                    m.setVisible(true);
                }
            }
        );

    }
    private class History_Reverse extends AbstractAction
    {
        private History history;
        public History_Reverse(History history)
        {
            this.history = history;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            System.out.println("AAAAAAAAAAAAAA");
            history.m_reverse();
        }
    }
}