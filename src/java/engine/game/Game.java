package src.java.engine.game;



import java.util.HashMap;

import src.java.application.Config;
import src.java.application.Window;
import src.java.engine.board.Board;
import src.java.engine.board.GameState;
import src.java.engine.board.Move;
import src.java.engine.board.Position;
import src.java.engine.board.Type;
import src.java.engine.board.piecelib.Piece;
import src.java.intelligence.Bot;

public class Game extends Thread {
    
    private Board board;
    private SoundEngine sound_engine;
    private Piece selected_piece;
    private VisualState visual_state;
    private Bot bot1;
    private Bot bot2;
    //private int movecount;
    //private float black_total;
    //private float white_total;
    private HashMap<Type, Integer> piececount;
    private Window window;
    private boolean player_interaction;
    //private boolean player;

    public Game()
    {
        this.board = new Board();
        this.sound_engine = new SoundEngine();
        this.player_interaction = true;
        //this.player = true;
        m_reset();
    }

    public void m_set_window(Window w)
    {
        this.window = w;
    }

    public void m_reset()
    {

        this.piececount = new HashMap<Type, Integer>();
        for (Type type : Type.values())
        {
            piececount.put(type, board.get_collection(type).get_active_pieces().size());
        }
        if (bot1 != null)
        {
            if (board.get_state() == GameState.CHECKMATE
             && get_opposite_bot(board.get_type()).get_calculator().get_configuration().is_randomized())
            {
                rounds_won++;
                System.out.println(Config.get_loser() + ", is currently on round: " + rounds_won);
                if (rounds_won == 1)
                {
                    let_bot_write_config(get_opposite_bot(get_turn()));
                    Config.m_add_loss(get_turn());
                    System.out.println("WRITING TO CONFIG!");
                    this.bot1 = new Bot(Type.WHITE, !bot1.get_calculator().get_configuration().is_randomized());
                    this.bot2 = new Bot(Type.BLACK, !bot2.get_calculator().get_configuration().is_randomized());
                    rounds_won = 0;
                }
                else
                {
                    this.bot1.m_reset_tree();
                    this.bot2.m_reset_tree();
                }
            }
            else
            {
                Config.m_add_loss();
                rounds_won = 0;
                System.out.println(Config.get_loser() + ", did not win, back to: " + rounds_won);
                this.bot1 = new Bot(Type.WHITE, bot1.get_calculator().get_configuration().is_randomized());
                this.bot2 = new Bot(Type.BLACK, bot2.get_calculator().get_configuration().is_randomized());
            }
        }
        if (bot1 == null)
        {
            bot1 = new Bot(Type.WHITE, false);
            bot2 = new Bot(Type.BLACK, true);
        }

        board.m_to_start();
        board.m_set_normal();

        System.out.println("bot1: "+ Type.WHITE + ", is randomized, if : " + bot1.get_calculator().get_configuration().is_randomized());
        System.out.println("bot2: "+ Type.BLACK + ", is randomized, if : " + bot2.get_calculator().get_configuration().is_randomized());
        this.visual_state = VisualState.NOMINAL;
        selected_piece = null;
        /*movecount = 0;
        black_total = 0;
        white_total = 0;*/

    }



    public Bot get_opposite_bot(Type type)
    {
        if (type != Type.WHITE) {
            return bot1;
        }
        return bot2;
    }

    public Bot get_bot(Type type)
    {
        if (type == Type.WHITE) {
            return bot1;
        }
        return bot2;
    }

    public void let_bot_write_config(Bot bot)
    {
            bot.write_config();

    }

    public void bot_turn()
    {
        bot_turn(get_bot(get_turn()));
    }

    public boolean bot_turn(Bot bot)
    {
        Thread.yield();
        //float previous_eval = bot.get_calculator().evaluate(board);
        Move move;
        if (board.is_final())
        {
            if (board.get_state() == GameState.CHECKMATE)
            {
                System.out.println(board.get_state());
                System.out.println("\n//////////////////////////\n//////////////////////////\n//////////////////////////\n");
                return false;
            }
            if (board.get_state() == GameState.DRAW)
            {
                System.out.println(board.get_state());
                System.out.println("\n//////////////////////////\n//////////////////////////\n//////////////////////////\n");
                return false;
            }
        }
        move = bot.calculate_best_move();
        //System.out.println(move);
        move = move.convert(board);
        try
        {
            m_select_piece(move.position_from().get_piece());
            Thread.yield();
            //Thread.sleep(150);
            m_select_position(move.position_to());
            Thread.yield();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Something went vewwy wong");
        }

        float eval = bot.get_calculator().evaluate(board);
        if (bot.get_type() == Type.WHITE) {
            /*movecount++;
            System.out.println("///// ##### MOVECOUNT : " + movecount);
            white_total += eval - previous_eval;
            System.out.println("average improvement for:" + bot.get_type() + ", is :" + (white_total/movecount) );
            */
        }
        if (bot.get_type()== Type.BLACK) {
            /*System.out.println("///// ##### MOVECOUNT : " + movecount);
            black_total += previous_eval - eval;
            System.out.println("average improvement for:" + bot.get_type() + ", is :" + (black_total/movecount) );
            */
        }
        //previous_eval = eval;

        return true;
    }


    public VisualState get_visual_state()
    {
        return visual_state;
    }

    public void m_select_piece(Piece piece) {
        if (!piece.is_type(get_turn())) {
            return;
        }
        this.selected_piece = piece;
        this.selected_piece.position().get_listener().m_select();
        if (player_interaction)
        {
            Board clone = board.clone();
            for (Position position : selected_piece.get_legal_moves()) {
                position.get_listener().m_legal_move_activate();
                clone.m_commit(new Move(selected_piece.position(), position).convert(clone));
                position.get_listener().m_set_eval(get_bot(get_turn()).get_calculator().evaluate(clone));
                clone.m_revert();
            }
        }
        else
        {
            for (Position position : selected_piece.get_legal_moves()) {
                position.get_listener().m_legal_move_activate();
            }
        }
        visual_state = VisualState.SELECTED;
    }

    public void m_deselect_piece()
    {
            this.selected_piece.position().get_listener().m_deselect();
            for (Position position : selected_piece.get_legal_moves()) {
                position.get_listener().m_legal_move_deactivate();
        }
        this.selected_piece = null;
        visual_state = VisualState.NOMINAL;
    }

    public void m_select_position(Position position)
    {
        if (position.get_piece() != null) {
            if (position.get_piece().is_type(get_turn())) {
                m_deselect_piece();
                m_select_piece(position.get_piece());
                return;
            }
        }
        if(!selected_piece.is_legal_move(position))
        {
            m_deselect_piece();
            return;
        }
        Piece piece = selected_piece;
        m_deselect_piece();
        board.m_commit(piece, position);
        bot1.m_adjust_tree(board.get_history().get_move(board.get_history().get_length() - 1));
        bot2.m_adjust_tree(board.get_history().get_move(board.get_history().get_length() - 1));
        //System.out.println(bot1.get_calculator().evaluate(board));
        /*if (board.get_collection(get_turn()).get_active_pieces().size() < piececount.get(get_turn()))
        {

            sound_engine.play_take();
        }
        else
        {*/
        sound_engine.play_move();
        //}
        piececount.put(get_turn(), board.get_collection(get_turn()).get_active_pieces().size());

        this.window.m_set_eval(get_bot(get_turn()).get_calculator().evaluate(board));
        //print_state();
        Thread.yield();
    }


    public Board get_board()
    {
        return board;
    }

    public void print_state() {
        System.out.println(board);
    }


    public void m_set_board(Board board) {
        this.board = board;
    }

    public Type get_turn() {
        return board.get_type();
    }

    private void m_bots_catchup()
    {
        for (int i = 0; i < board.get_history().get_length(); i++)
        {
            bot1.m_adjust_tree(board.get_history().get_move(i));
            bot2.m_adjust_tree(board.get_history().get_move(i));
        }
    }


    public void m_london()
    {
        System.out.println(">>>>>>> LONDON");
        board.m_commit(board.get_position(3, 1).get_piece(), board.get_position(3, 3));
        board.m_commit(board.get_position(3, 6).get_piece(), board.get_position(3, 4));
        board.m_commit(board.get_position(2, 0).get_piece(), board.get_position(5, 3));
        board.m_commit(board.get_position(6, 7).get_piece(), board.get_position(5, 5));
        board.m_commit(board.get_position(4, 1).get_piece(), board.get_position(4, 2));
        board.m_commit(board.get_position(4, 6).get_piece(), board.get_position(4, 5));
    }

    public void m_sicilian_najdorf()
    {
        System.out.println(">>>>>>> SICILIAN NAJDORF");
        // white pawn e2-e4
        board.m_commit(board.get_position(4, 1).get_piece(), board.get_position(4, 3));
        // black pawn c7-c5
        board.m_commit(board.get_position(2, 6).get_piece(), board.get_position(2, 4));
        // white knight g2-f3
        board.m_commit(board.get_position(6, 0).get_piece(), board.get_position(5, 2));
        // black pawn d7-d6
        board.m_commit(board.get_position(3, 6).get_piece(), board.get_position(3, 5));
        // white pawn d2-d4
        board.m_commit(board.get_position(3, 1).get_piece(), board.get_position(3, 3));
        // black pawn c5-d4
        board.m_commit(board.get_position(2, 4).get_piece(), board.get_position(3, 3));
        // white knight f3-d4
        board.m_commit(board.get_position(5, 2).get_piece(), board.get_position(3, 3));
        // black knight g8-f6
        board.m_commit(board.get_position(6, 7).get_piece(), board.get_position(5, 5));
        // white knight b1-c3
        board.m_commit(board.get_position(1, 0).get_piece(), board.get_position(2, 2));
        // black pawn a7-a6
        board.m_commit(board.get_position(0, 6).get_piece(), board.get_position(0, 5));
    }

    public void m_vienna()
    {
        System.out.println(">>>>>>> VIENNA");
        // white pawn e2-e4
        board.m_commit(board.get_position(4, 1).get_piece(), board.get_position(4, 3));
        // black pawn e7-e5
        board.m_commit(board.get_position(4, 6).get_piece(), board.get_position(4, 4));
        // white bishop f1-c4
        board.m_commit(board.get_position(5, 0).get_piece(), board.get_position(2, 3));
        // black knight g8-f6
        board.m_commit(board.get_position(6, 7).get_piece(), board.get_position(5, 5));
        // white pawn d2-d3
        board.m_commit(board.get_position(3, 1).get_piece(), board.get_position(3, 2));
        // black pawn c7-c6
        board.m_commit(board.get_position(2, 6).get_piece(), board.get_position(2, 5));

    }

    public void m_caro_kann()
    {
        System.out.println(">>>>>>> CARO KANN");
        board.m_commit(board.get_position(4, 1).get_piece(), board.get_position(4, 3));
        board.m_commit(board.get_position(2, 6).get_piece(), board.get_position(2, 5));
        board.m_commit(board.get_position(3, 1).get_piece(), board.get_position(3, 3));
        board.m_commit(board.get_position(3, 6).get_piece(), board.get_position(3, 4));
        board.m_commit(board.get_position(4, 3).get_piece(), board.get_position(4, 4));
        board.m_commit(board.get_position(2, 7).get_piece(), board.get_position(5, 4));
        board.m_commit(board.get_position(6, 0).get_piece(), board.get_position(5, 2));
        board.m_commit(board.get_position(4, 6).get_piece(), board.get_position(4, 5));
    }    

    public void m_catalan()
    {
        System.out.println(">>>>>>> CATALAN");
        board.m_commit(board.get_position(3, 1).get_piece(), board.get_position(3, 3));
        board.m_commit(board.get_position(3, 6).get_piece(), board.get_position(3, 4));
        board.m_commit(board.get_position(2, 1).get_piece(), board.get_position(2, 3));
        board.m_commit(board.get_position(4, 6).get_piece(), board.get_position(4, 5));
        board.m_commit(board.get_position(6, 0).get_piece(), board.get_position(5, 2));
        board.m_commit(board.get_position(6, 7).get_piece(), board.get_position(5, 5));

    }

   private int rounds_won;
    
    @Override
    public void run()
    {
        player_interaction = false;
        rounds_won = 0;
        //this.player = false;
        System.out.println("Bot play enabled!");
        for (int i = 0; i < 4000; i++)
        {
            switch (rounds_won) {
                case 0:
                    m_caro_kann();
                    break;
                case 1:
                    m_sicilian_najdorf();
                    break;
                case 2:
                    m_london();
                    break;
                case 3:
                    m_vienna();
                    break;
                case 4:
                    m_catalan();
                    break;
                default:
                    break;
            }
            m_bots_catchup();
            try {
            while (bot_turn(get_bot(get_turn())))
            {}
            } catch (Exception e)
            {
                e.printStackTrace();   
                System.out.println("ERROR");
                System.out.println("ERROR");
                System.out.println("ERROR");
                System.out.println("ERROR");
                System.out.println("ERROR");
                try {
                    //Thread.sleep(4000);
                } catch (Exception y)
                {
                }
            }

            m_reset();
        }
        //while (bot_turn(get_bot(get_turn())))
        //{}
        System.out.println("Bot play disabled!");
    }
}