package src.java.engine.board;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import src.java.engine.board.Move.MoveType;
import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.piecelib.Piece.PieceType;
import src.java.engine.board.updatesystem.NotificationCollector;


/**
 *  Handles all the logic surrounding the relationships between
 *  positions ({@link #arr_positions}) and their pieces ({@link #white_pieces}, {@link #black_pieces}).
 *  Contains data, which dictate the state of the Game ({@link #state}), like CHECKMATE, CHECK, DRAW and the
 *  information about the player currently playing ({@link #type}).
 * 
 */
public class Board extends NotificationCollector implements BoardAccess
{


    /**
     *  
     * 
     */
    public enum GameState
    {
        NORMAL,
        DRAW,
        CHECK,
        CHECKMATE
    }


    private final Position[][] arr_positions;
    private final PieceCollection white_pieces;
    private final PieceCollection black_pieces;
    private final History history;
    private GameState state;
    private Type type;


    /**
     *  Constructor
     *  Initializes all necessary classes for the game logic to function.
     * 
     */
    public Board()
    {
        super();
        this.type = Type.WHITE;
        this.state = GameState.NORMAL;
        arr_positions = new Position[8][8];
        for (int row = 0; row < 8; row++)
        {
            for (int column = 0; column < 8; column++)
            {
                arr_positions[column][row] = new Position(column, row, this);
            }
        }
        this.white_pieces = new PieceCollection(Type.WHITE, this);
        this.black_pieces = new PieceCollection(Type.BLACK, this);
        this.white_pieces.m_standard_lineup();
        this.black_pieces.m_standard_lineup();
        this.history = new History();
        ///
        super.m_add_required_observers(this.white_pieces.get_pieces_of_type(PieceType.KING).get(0).get_observer());
        super.m_add_required_observers(this.black_pieces.get_pieces_of_type(PieceType.KING).get(0).get_observer());
    }


    /**
     *  Simply returns the Position instance with the coordinates x, y
     * 
     * @param column (x)
     * @param row (y)
     * 
     * @return Position
     */
    public Position get_position(int column, int row)
    {
        return arr_positions[column][row];
    }


    /**
     *  
     * 
     * @param type
     * 
     * @return
     */
    public PieceCollection get_collection(Type type)
    {
        if (type == Type.WHITE)
        return white_pieces;
        else if (type == Type.BLACK)
        return black_pieces;
        throw new IllegalArgumentException("Board.get_collection() : type not set correctly");
    }


    /**
     * 
     * 
     * @return
     */
    public History get_history()
    {
        return history;
    }


    /**
     *  Reverts everything back to the standard position.
     * 
     */
    public void m_to_start()
    {
        while (history.get_length() > 0)
        {
            m_revert();
        }
    }


    /**
     *  This method is required exclusively for en-passant.
     * 
     */
    public Position[] get_last_move()
    {
        if (history.get_length() == 0)
        {
            return null;
        }
        ///
        Position[] vec = new Position[2];
        ///
        Move move = history.get_move(history.get_length() - 1);
        vec[0] = move.position_from();
        vec[1] = move.position_to();
        return vec;
    }


    /**
     *  Returns true, if the Board currently has one of the following states:
     *  {@code DRAW} or {@code CHECKMATE}
     * 
     * @return {@code true: if (}{@link #state}{@code  == GameState.DRAW || GameState.CHECKMATE),}
     * <p>     {@code false: else}
     */
    public boolean is_final()
    {
        if (get_state() == GameState.CHECKMATE || get_state() == GameState.DRAW)
        {
            return true;
        }
        return false;
    }


    /**
     * 
     * 
     * @return {@link #state}
     */
    public GameState get_state()
    {
        return state;
    }


    /**
     * 
     * 
     * @return
     */
    public Type get_type()
    {
        return type;
    }


    /**
     * 
     * 
     */
    public void m_set_check()
    {
        this.state = GameState.CHECK;
    }


    /**
     * 
     * 
     */
    public void m_set_checkmate()
    {
        this.state = GameState.CHECKMATE;
    }


    /**
     * 
     * 
     */
    public void m_set_normal()
    {
        this.state = GameState.NORMAL;
    }


    /**
     * 
     * 
     */
    public void m_set_draw()
    {
        this.state = GameState.DRAW;
    }


    /**
     * 
     * 
     */
    public void m_type()
    {
        this.type = Type.get_opposite(this.type);
    }
    

    /**
     * 
     * 
     * @param move
     * 
     */
    public void m_commit(Move move)
    {
        get_history().m_register_move(move);
        move.m_commit();
        get_collection(get_type()).m_release_check();
        get_collection(get_type()).m_request_update(PieceType.PAWN);
        m_type();
        m_dump_update_notifications();
        get_collection(get_type()).m_state();
    }


    /**
     * 
     * 
     */
    public void m_revert()
    {
        if (get_history().get_length() <= 0)
        {
            return;
        }
        ///
        get_collection(get_type()).m_release_check();
        m_type();
        get_history().m_reverse();
        get_collection(get_type()).m_state();
        /// Currently, it seems without this, the code can throw a NPE
        /// Testing the code with it enabled for now, hoping there is no NPE,
        /// however that would not exactly aim towards this being the issue, as
        /// the bug occurred after a prolonged period of time 
        /// 
        /// Exception in thread "Thread-4" java.lang.NullPointerException:
        /// Cannot invoke "src.java.engine.board.Position.get_x()"
        /// because the return value of "src.java.engine.board.piecelib.Piece.get_position()" is null
        get_collection(get_type()).m_request_update(PieceType.PAWN);
        m_dump_update_notifications();
    }


    /**
     * 
     * 
     * @param board
     * 
     * @return
     */
    public Object continuity_check(Board board)
    {
        Type[] types = Type.values();
        /// types
        for (int t = 0; t < types.length; t++)
        {
            /// pieces
            if (get_collection(types[t]).get_active_pieces().size() != get_collection(types[t]).get_active_pieces().size())
            {   
                return get_collection(types[t]).get_active_pieces().size();
            }
            for (int i = 0; i < get_collection(types[t]).get_active_pieces().size(); i++)
            {
                Piece p1 = get_collection(types[t]).get_active_pieces().get(i);
                Piece p2 = board.get_collection(types[t]).get_active_pieces()
                            .get(board.get_collection(types[t]).get_active_pieces().indexOf(p1));
                ///
                ///
                if (!p1.ID().equals(p2.ID()))
                {
                    return p1.ID() + " >> " + p2.ID();
                }
                ///
                if (!p1.get_position().equals(p2.get_position().convert(this)))
                {
                    return p1.ID() + p1.get_position();
                }
                ///
                /// legal positions
                if (p1.get_legal_moves().size() != p2.get_legal_moves().size())
                {
                    return p1.ID() + p1.get_legal_moves() + "\n\\vvvvvv//\n" + p2.ID() + p2.get_legal_moves();
                }
                ///
                Iterator<Entry<Position, MoveType[]>> iterator1 = p1.get_legal_moves().entrySet().iterator();  
                for (Entry<Position, MoveType[]> entry : p2.get_legal_moves().entrySet()) {
                    if (!iterator1.next().getKey().equals(entry.getKey().convert(this))) {
                        LinkedList<Move> ll_moves = new LinkedList<Move>();
                        ll_moves.add(new Move(p1.get_position(), entry.getKey(), entry.getValue()));
                        ll_moves.add(new Move(p2.get_position(), entry.getKey(), entry.getValue()));
                        return ll_moves + ",\n" + p1.get_legal_moves() + "\n" + p2.get_legal_moves();
                    }
                }
            }
        }
        return null;
    }


    /**
     *  Board will be setup in a standard state
     * <p>
     *  - default positions
     * <p>
     *  - default number of pieces
     * <p>
     *  - White first
     * 
     * 
     * @return void
     */
    public void m_initialise()
    {
        ///
        /// [0 king, 1 queen, 2-3 rooks, 4-5 bishops, 6-7 knights, 8-16 pawns]
        ///
        /// King
        m_initialise_piece(Type.WHITE, PieceType.KING, 0, 4, 0);
        m_initialise_piece(Type.BLACK, PieceType.KING, 0, 4, 7);
        ///
        /// Queen
        m_initialise_piece(Type.WHITE, PieceType.QUEEN, 0, 3, 0);
        m_initialise_piece(Type.BLACK, PieceType.QUEEN, 0, 3, 7);
        ///
        /// Rooks
        m_initialise_piece(Type.WHITE, PieceType.ROOK, 0, 0, 0);
        m_initialise_piece(Type.BLACK, PieceType.ROOK, 0, 0, 7);
        m_initialise_piece(Type.WHITE, PieceType.ROOK, 1, 7, 0);
        m_initialise_piece(Type.BLACK, PieceType.ROOK, 1, 7, 7);
        ///
        /// Bishops
        m_initialise_piece(Type.WHITE, PieceType.BISHOP, 0, 2, 0);
        m_initialise_piece(Type.BLACK, PieceType.BISHOP, 0, 2, 7);
        m_initialise_piece(Type.WHITE, PieceType.BISHOP, 1, 5, 0);
        m_initialise_piece(Type.BLACK, PieceType.BISHOP, 1, 5, 7);
        ///
        /// Knights
        m_initialise_piece(Type.WHITE, PieceType.KNIGHT, 0, 1, 0);
        m_initialise_piece(Type.BLACK, PieceType.KNIGHT, 0, 1, 7);
        m_initialise_piece(Type.WHITE, PieceType.KNIGHT, 1, 6, 0);
        m_initialise_piece(Type.BLACK, PieceType.KNIGHT, 1, 6, 7);
        ///
        /// Pawns
        for (int i = 0; i < 8; i++) {
            m_initialise_piece(Type.WHITE, PieceType.PAWN, i, i, 1);
            m_initialise_piece(Type.BLACK, PieceType.PAWN, i, i, 6);
        }
        m_dump_update_notifications();
    }


    /**
     * 
     * 
     * @param type
     * @param impl
     * @param index
     * @param x
     * @param y
     * 
     */
    private void m_initialise_piece(Type type, PieceType impl, int index, int x, int y)
    {
        get_collection(type).get_pieces_of_type(impl).get(index).m_set_position(get_position(x, y));
    }


    /**
     * 
     * 
     */
    public Board clone()
    {
        Board clone = new Board();
        clone.m_initialise();
        for (int i = 0; i < get_history().get_length(); i++)
        {
            clone.m_commit(get_history().get_move(i).convert(clone));
        }
        ///
        if (get_type() != Type.WHITE)
        {
            clone.m_type();
        }
        clone.m_dump_update_notifications();
        return clone;
    }


    /**
     * 
     * 
     * @return
     */
    public TreeMap<String, Integer> get_reduced()
    {
        TreeMap<String, Integer> map_pieces = new TreeMap<String, Integer>();
        for (Type type : Type.values())
        {
            for (Piece piece : get_collection(type).get_active_pieces())
            {
                Position pos = piece.get_position();
                map_pieces.put(piece.ID(), Integer.rotateLeft(pos.get_x(), 3) + pos.get_y());
            }
        }
        return map_pieces;
    }


    @Override
    public String toString()
    {
        String s = "";
        for (int row = 7; row >= 0; row--)
        {
            s = s + (row+1) + " : ";
            for (int column = 0; column < 8; column++)
            {
                if (get_position(column, row).get_piece() == null)
                {
                    s = s + "   ";
                    continue;
                }
                s = s + get_position(column, row).get_piece() + " ";
            }
            s = s + "\n";
        }
        s = s + "    -- -- -- -- -- -- -- --\n";
        s = s + "     a  b  c  d  e  f  g  h";
        s = s + "\n->" + get_type();
        s = s + "\n<< " + get_state() + " >>";
        return s;
    }


}