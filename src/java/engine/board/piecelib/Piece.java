package src.java.engine.board.piecelib;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;

import src.java.engine.board.PieceCollection;
import src.java.engine.board.Position;
import src.java.engine.board.Move;
import src.java.engine.board.Move.MoveType;
import src.java.engine.board.updatesystem.Observer;
import src.java.engine.board.updatesystem.ObserverReceiver;


/**
 *  Piece, handles the legal moves of this piece.
 * 
 */
public abstract class Piece implements Comparable<Piece>
{


    /**
     *  PieceType determines, what rules this Piece follows and allows comparing Pieces easily,
     *  without needing to compare classes.
     *  
     *  <p> KING,
     *  <p> QUEEN,
     *  <p> ROOK,
     *  <p> BISHOP,
     *  <p> KNIGHT,
     *  <p> PAWN
     * 
     */
    public enum PieceType
    {
        KING,
        QUEEN,
        ROOK,
        BISHOP,
        KNIGHT,
        PAWN;
    }


    //TODO move this to PieceCollection?
    public enum Type
    {
        WHITE,
        BLACK;


        /**
         * 
         * @param type
         * 
         * @return
         */
        public static Type get_opposite(Type type)
        {
            switch (type)
            {
                case WHITE:
                    return Type.BLACK;
            
                default:
                    return Type.WHITE; 
            }
        }

    }


    private final PieceCollection collection;
    private Position position;
    private LinkedHashMap<Position, MoveType[]> map_legal_moves;
    private final ObserverReceiver observer;
    protected final String ID;
    protected final int INDEX;
    private int moves;


    /**
     *  Constructor
     * 
     * @param collection
     * @param index
     * 
     */
    public Piece(PieceCollection collection, int index)
    {
        this.INDEX = index;
        this.collection = collection;
        this.map_legal_moves = new LinkedHashMap<Position, MoveType[]>();
        this.observer = new Observer(this);
        this.ID = toString() + index;
        this.moves = 0;
    }


    /**
     * 
     * 
     * @return {@link #ID}
     */
    public String ID()
    {
        return this.ID;
    }


    /**
     * 
     * 
     * @return {@link #INDEX}
     */
    public int INDEX()
    {
        return INDEX;
    }


    /**
     * 
     * 
     * @return {@link #moves}
     */
    public int moves()
    {
        return moves;
    }


    /**
     * 
     * 
     * @return {@link #map_legal_moves}
     */
    public LinkedHashMap<Position, MoveType[]> get_legal_moves()
    {
        return map_legal_moves;
    }


    /**
     * 
     * 
     * @return {@code collection.get_type()}
     */
    public Type get_type()
    {
        return collection.get_type();
    }


    /**
     * 
     * 
     * @return {@link #collection}
     */
    public PieceCollection get_collection()
    {
        return collection;
    }


    /**
     * 
     * 
     * @return {@link #position}
     */
    public Position get_position()
    {
        return position;
    }


    /**
     * 
     * 
     * @return {@link #observer} 
     */
    public Observer get_observer()
    {
        return (Observer) observer;
    }


    /**
     * 
     * 
     * @return weight specified by PieceType
     */
    public abstract int get_weight();


    /**
     * 
     * 
     * @return PieceType specified by class implementation
     */
    public abstract PieceType get_piece_type();


    /**
     *  returns a legal move, if the piece has a legal move to the specified {@code Position}.
     * 
     * @param position
     * 
     * @return {@code Optional<Move>}
     */
    public Optional<Move> get_legal_move(Position position)
    {
        MoveType[] move_types = map_legal_moves.get(position);
        ///
        if (move_types == null)
        {
            return Optional.empty();
        }
        return Optional.of(new Move(this.get_position(), position, move_types));
    }


    /**
     * 
     * 
     * @param type
     * 
     * @return
     */
    public boolean is_type(Type type)
    {
        return get_type() == type;
    }


    /**
     *  allows for modification of this Piece's move count
     * 
     *  <p> only strictly necessary for rook and king to allow
     *      castling after reversing the history
     *      (-> Possible to add them to all Piece Types?)
     *      (What does this mean exactly?)
     * 
     */
    public void m_increase_move()
    {
        moves++;
    }


    /**
     *  allows for modification of this Piece's move count
     * 
     *  <p> only strictly necessary for rook and king to allow
     *      castling after reversing the history
     *      (-> Possible to add them to all Piece Types?)
     *      (What does this mean exactly?)
     * 
     */
    public void m_decrease_move()
    {
        moves--;
    }


    /**
     * 
     * 
     * @param position
     * 
     */
    public void m_set_position(Position position)
    {
        observer.m_clear_observations();
        ///
        if(position == null)
        {
            map_legal_moves.clear();
            this.position.m_rm_piece();
            this.position = null;
            return;
        }
        ///
        if (this.position != null)
        {
            this.position.m_rm_piece();
        }
        ///
        if(position.get_piece() != null)
        {
            position.get_piece().get_collection().m_take(position.get_piece());
        }
        ///
        this.position = position;
        position.m_set_piece(this);
        m_update();
    }


    /**
     *  Restricts this piece to exclusively consist of moves, which are also contained
     *  in {@code ll_restrictions}
     * 
     * @param ll_restrictions
     *
     */
    public void m_restrict(LinkedList<Position> ll_restrictions)
    {
        LinkedHashMap<Position, MoveType[]> map_legal_moves_new = new LinkedHashMap<Position, MoveType[]>();
        for (Position position : ll_restrictions)
        {
            if (map_legal_moves.containsKey(position))
            {
                map_legal_moves_new.put(position, map_legal_moves.get(position));
            }
        }
        map_legal_moves = map_legal_moves_new;
    }


    /**
     *  Called by Observers to recalculate the legal moves.
     * 
     */
    public void m_update()
    {
        map_legal_moves.clear();
        observer.m_clear_observations();
        if (position == null)
        {
            return;
        }
        m_legal_moves();
    }


    /**
     *  Used to implement piecetype-specific.
     *  adds all possible moves to {@link #map_legal_moves}
     * 
     */
    public abstract void m_legal_moves();


    /**
     *  adds all possible vertical moves to {@link #map_legal_moves}
     * 
     */
    public void m_vertical_moves()
    {
        int x = this.position.get_x();
        int y = this.position.get_y();
        for (int i_y = y + 1; i_y < 8; i_y++)
        {
            if (!m_check_move(x, i_y))
                break;
        }
        for (int i_y = y - 1; i_y >= 0; i_y--)
        {
            if (!m_check_move(x, i_y))
                break;
        }
    }


    /**
     *  adds all possible horizontal moves to {@link #map_legal_moves}
     * 
     */
    public void m_horizontal_moves()
    {
        int x = this.position.get_x();
        int y = this.position.get_y();
        for (int i_x = x + 1; i_x < 8; i_x++)
        {
            if (!m_check_move(i_x, y))
                break;
        }
        for (int i_x = x - 1; i_x >= 0; i_x--)
        {
            if (!m_check_move(i_x, y))
                break;
        }
    }


    /**
     *  adds all possible diagonal moves to {@link #map_legal_moves}
     * 
     */
    public void m_diagonal_moves()
    {
        int x = this.position.get_x();
        int y = this.position.get_y();
        for (int i_x = x + 1, i_y = y + 1; i_x < 8 && i_y < 8; i_x++, i_y++)
        {
            if (!m_check_move(i_x, i_y))
                break;
        }
        for (int i_x = x - 1, i_y = y + 1; i_x >= 0 && i_y < 8; i_x--, i_y++)
        {
            if (!m_check_move(i_x, i_y))
                break;
        }
        for (int i_x = x + 1, i_y = y - 1; i_x < 8 && i_y >= 0; i_x++, i_y--)
        {
            if (!m_check_move(i_x, i_y))
                break;
        }
        for (int i_x = x - 1, i_y = y - 1; i_x >= 0 && i_y >= 0; i_x--, i_y--)
        {
            if (!m_check_move(i_x, i_y))
                break;
        }
    }


    /**
     *  adds all possible king moves to {@link #map_legal_moves}
     * 
     */
    public void m_king_moves()
    {
        int x = this.position.get_x();
        int y = this.position.get_y();
        ///
        if (x < 7)
        {
            m_check_move(x + 1, y);
            if (y < 7)
                m_check_move(x + 1, y + 1);
            if (y > 0)
                m_check_move(x + 1, y - 1);
        }
        if (y < 7)
            m_check_move(x, y + 1);
        if (y > 0)
            m_check_move(x, y - 1);
        if (x > 0)
        {
            m_check_move(x - 1, y);
            if (y < 7)
                m_check_move(x - 1, y + 1);
            if (y > 0)
                m_check_move(x - 1, y - 1);
        }
    }


    /**
     *  adds all possible pawn moves to {@link #map_legal_moves}
     * 
     */
    public void m_pawn_moves()
    {
        int x = get_position().get_x();
        int y = get_position().get_y();
        int directional_constant = directonal_parameter(1, -1);
        /// pawn forward
        if (m_pawn_check_move(x, y + directional_constant))
        {
            if (is_type(Type.WHITE) && y == 1)
            {
                m_pawn_check_move(x, y + 2 * directional_constant);
            }
            if (is_type(Type.BLACK) && y == 6)
            {
                m_pawn_check_move(x, y + 2 * directional_constant);
            }
        }
        /// pawn diagonal (takes)
        if (x < 7)
        {
            m_pawn_check_move_diagonal(x + 1, y + directional_constant);
        }
        if (x > 0)
        {
            m_pawn_check_move_diagonal(x - 1, y + directional_constant);
        }
        /// en passant
        if (y == 4 && is_type(Type.WHITE) || y == 3 && is_type(Type.BLACK))
        {
            if (x < 7)
            {
                observer.m_observe_silently(collection.get_board_access().get_position(x + 1, y));
                observer.m_observe_silently(collection.get_board_access().get_position(x + 1, y + 2 * directional_constant));
            }
            if (x > 0)
            {
                observer.m_observe_silently(collection.get_board_access().get_position(x - 1, y));
                observer.m_observe_silently(collection.get_board_access().get_position(x - 1, y + 2 * directional_constant));
            }
        }
    }


    /**
     *  adds all possible knight moves to {@link #map_legal_moves}
     * 
     */
    public void m_knight_moves()
    {
        int x = this.position.get_x();
        int y = this.position.get_y();
        ///
        if (x < 7)
        {
            if (y < 6)
            m_check_move(x + 1, y + 2);
            if (y > 1)
            m_check_move(x + 1, y - 2);
            if (x < 6)
            {
                if (y < 7)
                m_check_move(x + 2, y + 1);
                if (y > 0)
                m_check_move(x + 2, y - 1);
            }
        }
        if (x > 0)
        {
            if (y < 6)
            m_check_move(x - 1, y + 2);
            if (y > 1)
            m_check_move(x - 1, y - 2);
            if (x > 1)
            {
                if (y < 7)
                m_check_move(x - 2, y + 1);
                if (y > 0)
                m_check_move(x - 2, y - 1);
            }
        }
        
    }


    /**
     *  Used for specific purposes, where an if-statement relies upon
     *  the type of the piece. This method's sole purpose is code-readability.
     * 
     * @param w
     * @param b
     * 
     * @return
     */
    public int directonal_parameter(int w, int b)
    {
        switch (get_type())
        {
            case WHITE:
                return w;
            case BLACK:
                return b;
            default:
                throw new IllegalArgumentException("You shouldn't be here...");
        }
    }


    /**
     *  checks, if the given coordinates point towards are a legal move
     *  return value is used to determine, if this direction needs to be checked further. 
     * 
     * @param x
     * @param y
     * 
     * @return {@code true:  if (position.has_piece != null),
     *                false: else}
     */
    private boolean m_check_move(int x, int y)
    {
        Position future_position = collection.get_board_access().get_position(x, y);
        //System.out.println("observer : " + this + ", observing   : " + future_position);
        observer.m_observe(future_position);
        if (future_position.get_piece() == null)
        {
            map_legal_moves.put(future_position, new MoveType[0]);
            return true;
        }
        if (!is_type(future_position.get_piece().get_type()))
        {
            map_legal_moves.put(future_position, new MoveType[]{MoveType.TAKES});
        }
        return false;
    }


    /**
     *  pawns can only go forward, if there are no pieces.
     * 
     *  <p> This method returns a boolean, because that way the pawn knows,
     *      whether it should calculate, if it can also go 2 moves forwards.
     * 
     * @param x
     * @param y
     * 
     * @return {@code true:  if (pawn can go forwards),
     *                false: else}
     */
    private boolean m_pawn_check_move(int x, int y)
    {
        Position future_position = collection.get_board_access().get_position(x, y);
        observer.m_observe_silently(future_position);
        ///
        if (future_position.get_piece() == null)
        {
            if (future_position.get_y() == 7 && is_type(Type.WHITE) ||
                future_position.get_y() == 0 && is_type(Type.BLACK))
            {
                /// add other movetypes
                map_legal_moves.put(future_position, new MoveType[]{MoveType.PROMOTION_QUEEN});
            }
            else
            {
                map_legal_moves.put(future_position, new MoveType[0]);
            }
            return true;
        }
        return false;
    }


    /**
     *  pawns diagonal moves work very differently from normal moves or normal pawn moves.
     *  
     *  <p> If there is a piece, the pawn can take it
     *  <p> If there is a pawn next to it, which enables en-passant, the pawn can take it
     *  <p> else pawn can't go there
     * 
     * @param x
     * @param y
     * 
     * @return
     */
    private boolean m_pawn_check_move_diagonal(int x, int y)
    {
        Position future_position = collection.get_board_access().get_position(x, y);
        observer.m_observe(future_position);
        ///
        if (future_position.get_piece() != null)
        {
            if (future_position.get_piece().get_type() != get_type())
                if (future_position.get_y() == 7 && is_type(Type.WHITE) ||
                    future_position.get_y() == 0 && is_type(Type.BLACK))
                {
                    /// add other movetypes
                    map_legal_moves.put(future_position, new MoveType[]{MoveType.TAKES, MoveType.PROMOTION_QUEEN});
                }
                else
                {
                    map_legal_moves.put(future_position, new MoveType[]{MoveType.TAKES});
                }
                return true;
        }
        ///
        /// en passant
        Position[] last_move = collection.get_board_access().get_last_move();
        if (last_move == null)
        {
            return false;
        }
        ///
        if (last_move[1].get_piece() == null)
        {
            return false;
        }
        ///
        if (last_move[1].get_piece().get_piece_type() != PieceType.PAWN)
        {
            return false;
        }
        ///
        if (is_type(last_move[1].get_piece().get_type()))
        {
            return false;
        }
        ///
        int delta_y = last_move[0].get_y() - last_move[1].get_y();
        if (delta_y != directonal_parameter(2, -2))
        {
            return false;
        }
        ///
        if (last_move[1].get_x() != x)
        {
            return false;
        }
        ///
        if (last_move[1].get_y() == this.position.get_y())
        {
            if (last_move[0].get_x() < this.position.get_x())
            {
                map_legal_moves.put(future_position, new MoveType[]{MoveType.EN_PASSANT_LEFT});
            }
            else
            {
                map_legal_moves.put(future_position, new MoveType[]{MoveType.EN_PASSANT_RIGHT});
            }
            return true;
        }
        return false;
    }


    @Override
    public int compareTo(Piece o)
    {
        
        if (o.INDEX() > INDEX)
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }


    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Piece p)
        {
            if (p.ID().equals(ID()))
            {
                return true;
            }
        }
        return false;
    }


    public String toString()
    {
        String s = "";
        //s = ID() + ", ";
        if (get_type().equals(Type.WHITE))
            s = s +"w";
        else if (get_type().equals(Type.BLACK))
            s = s + "b";
        return s;
    }

}