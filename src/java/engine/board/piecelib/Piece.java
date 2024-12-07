package src.java.engine.board.piecelib;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

import src.java.engine.board.PieceCollection;
import src.java.engine.board.Position;
import src.java.engine.board.Move;
import src.java.engine.board.Move.MoveType;
import src.java.engine.board.updatesystem.Observer;
import src.java.engine.board.updatesystem.ObserverReceiver;

public abstract class Piece implements Comparable<Piece>{
    

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


    private PieceCollection collection;
    private Position position;
    private HashMap<Position, MoveType[]> map_legal_moves;
    private ObserverReceiver observer;
    protected String ID;
    protected int INDEX;
    private int moves;


    public Piece(PieceCollection collection, int index)
    {
        this.INDEX = index;
        this.collection = collection;
        this.map_legal_moves = new HashMap<Position, MoveType[]>();
        this.observer = new Observer(this);
        this.ID = toString() + index;
        this.moves = 0;
    }
    

    public String ID()
    {
        return this.ID;
    }


    public int INDEX()
    {
        return INDEX;
    }


    public int moves()
    {
        return moves;
    }


    public HashMap<Position, MoveType[]> get_legal_moves()
    {
        return map_legal_moves;
    }


    public Type get_type() {
        return collection.get_type();
    }


    public PieceCollection get_collection()
    {
        return collection;
    }


    public Position get_position()
    {
        return position;
    }


    public Observer get_observer()
    {
        return (Observer) observer;
    }


    public abstract int get_weight();


    public abstract PieceType get_piece_type();


    public Optional<Move> get_legal_move(Position position)
    {
        MoveType[] move_types = map_legal_moves.get(position);

        if (move_types == null)
        {
            return Optional.empty();

        }
        return Optional.of(new Move(this.get_position(), position, move_types));
    }


    public boolean is_type(Type type)
    {
        return get_type() == type;
    }


    /**
     * allows for modification of this (Piece)'s move count
     * 
     *  ->  only strictly necessary for:
     *       rook and king to allow
     *       castling after reversing the history
     *       (-> Possible to add them to all Piece Types?)
     * 
     */
    public void m_decrease_move()
    {
        moves--;
    }


    public void m_increase_move()
    {
        moves++;
    }


    public void m_set_position(Position position)
    {
        observer.m_clear_observations();
        
        if(position == null)
        {
            map_legal_moves.clear();
            this.position.m_rm_piece();
            this.position = null;
            return;
        }

        if (this.position != null)
        {
            this.position.m_rm_piece();
        }
        
        if(position.get_piece() != null)
        {
            position.get_piece().get_collection().m_take(position.get_piece());
        }

        this.position = position;
        position.m_set_piece(this);
        m_update();
    }


    public void m_restrict(LinkedList<Position> ll_restrictions)
    {
        HashMap<Position, MoveType[]> map_legal_moves_new = new HashMap<Position, MoveType[]>();
        for (Position position : ll_restrictions)
        {
            if (map_legal_moves.containsKey(position))
            {
                map_legal_moves_new.put(position, map_legal_moves.get(position));
            }
        }
        map_legal_moves = map_legal_moves_new;
    }


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
    

    public abstract void m_legal_moves();


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


    public void m_horizontal_moves() {
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


    public void m_king_moves()
    {
        int x = this.position.get_x();
        int y = this.position.get_y();

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
        if (x > 0) {
            m_check_move(x - 1, y);
            if (y < 7)
                m_check_move(x - 1, y + 1);
            if (y > 0)
                m_check_move(x - 1, y - 1);
        }
    }


    public void m_pawn_moves()
    {
        int x = get_position().get_x();
        int y = get_position().get_y();
        int directional_constant = pawn_directional(1, -1);

        // pawn forward
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
        // pawn diagonal (takes)
        if (x < 7)
        {
            m_pawn_check_move_diagonal(x + 1, y + directional_constant);
        }
        if (x > 0)
        {
            m_pawn_check_move_diagonal(x - 1, y + directional_constant);
        }
        // en passant
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


    public void m_knight_moves()
    {
        int x = this.position.get_x();
        int y = this.position.get_y();

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


    public int pawn_directional(int w, int b)
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


    // true -> continue checking, false -> end checking
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


    private boolean m_pawn_check_move(int x, int y)
    {
        Position future_position = collection.get_board_access().get_position(x, y);
        observer.m_observe_silently(future_position);

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


    private boolean m_pawn_check_move_diagonal(int x, int y)
    {
        Position future_position = collection.get_board_access().get_position(x, y);
        observer.m_observe(future_position);

        if (future_position.get_piece() != null)
        {
            if (future_position.get_piece().get_type() != get_type())
                map_legal_moves.put(future_position, new MoveType[]{MoveType.TAKES});
                return true;
        }

        // en passant
        Position[] last_move = collection.get_board_access().get_last_move();
        if (last_move == null)
        {
            return false;
        }

        if (last_move[1].get_piece() == null)
        {
            return false;
        }

        if (last_move[1].get_piece().get_piece_type() != PieceType.PAWN)
        {
            return false;
        }
        
        if (is_type(last_move[1].get_piece().get_type()))
        {
            return false;
        }

        int delta_y = last_move[0].get_y() - last_move[1].get_y();
        if (delta_y != pawn_directional(2, -2))
        {
            return false;
        }

        if (last_move[1].get_x() != x)
        {
            return false;
        }

        if (last_move[1].get_y() == this.position.get_y())
        {
            map_legal_moves.put(future_position, new MoveType[]{MoveType.EN_PASSANT});
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