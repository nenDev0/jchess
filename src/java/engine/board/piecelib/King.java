package src.java.engine.board.piecelib;

import java.util.Iterator;
import java.util.LinkedList;

import src.java.engine.board.PieceCollection;
import src.java.engine.board.Position;
import src.java.engine.board.Move.MoveType;
import src.java.engine.board.updatesystem.ObserverStorage;


public class King extends Piece
{

    private static final int weight = 0;
    /// used for evaluations
    private int visible_from;
    
    public King(PieceCollection collection, int index)
    {
        super(collection, index);
        visible_from = 0;
    }

    public int get_visible_from()
    {
        return visible_from;
    }
    
    /// ### getters ### ///

    public int get_weight()
    {
        return weight;
    }

    public PieceType get_piece_type()
    {
        return PieceType.KING;
    }

    public boolean can_castle_queenside()
    {
        if (moves() != 0)
        {
            return false;
        }
        int y = pawn_directional(0, 7);

        //System.out.println("Castling: queenside: " + queenside_castle + ", kingside: " + kingside_castle);
        Piece rook_queenside = get_collection().get_board_access().get_position(0, y).get_piece();

        if (rook_queenside != null)
        {
            if (is_type(rook_queenside.get_type()) && rook_queenside.moves() == 0)
            {
                return true;
            }
        }
        return false;
    }

    public boolean can_castle_kingside()
    {
        if (moves() != 0)
        {
            return false;
        }
        int y = pawn_directional(0, 7);

        Piece rook_kingside = get_collection().get_board_access().get_position(7, y).get_piece();
        if (rook_kingside != null) {
            if (is_type(rook_kingside.get_type()) && rook_kingside.moves() == 0)
            {
                return true;
            }
        }
        return false;
    }

    /// ### modifiers ### ///

    public void m_legal_moves()
    {
        m_king_moves();

        Iterator<Position> position_iterator = super.get_legal_moves().keySet().iterator();
        while (position_iterator.hasNext())
        {
            Position position = position_iterator.next();
            Iterator<ObserverStorage> iterator = position.get_observers().iterator();
            while (iterator.hasNext())
            {
                ObserverStorage o = iterator.next();
                if (is_type(o.get_piece().get_type()))
                {
                    continue;
                }
                position_iterator.remove();
                break;
            }
        }
        visible_from = 0;
        m_check_all_directions();
        m_check_castling();
    }

    @Override
    public void m_restrict(LinkedList<Position> ll_restrictions)
    {
        return;
    }
    


    public void m_set_position(Position position)
    {
        Position previous_position = get_position();
        boolean queenside_castle = can_castle_queenside();
        boolean kingside_castle = can_castle_kingside();
        super.m_set_position(position);
        get_collection().m_release_check();

        if (previous_position == null)
        {
            return;
        }

        int y = pawn_directional(0, 7);
        
        // CASTLING
        if (position.get_y() != y) 
        {
            return;
        }
        if (Math.abs(previous_position.get_x() - position.get_x()) != 2)
        {
            return;
        }

        if (queenside_castle)
        {
            if (position.get_x() == 2)
            {
                Piece rook_queenside = get_collection().get_board_access().get_position(0, y).get_piece();
                rook_queenside.m_set_position(get_collection().get_board_access().get_position(3, y));
                rook_queenside.m_increase_move();
                return;
            }
        }
        if (kingside_castle) 
        {
            if (position.get_x() == 6)
            {
                Piece rook_kingside = get_collection().get_board_access().get_position(7, y).get_piece();
                rook_kingside.m_set_position(get_collection().get_board_access().get_position(5, y));
                rook_kingside.m_increase_move();
                return;
            }
        }

        // CASTLING REVERSAL
        if (super.moves() != 0)
        {
            return;
        }
        if (previous_position.get_x() == 2)
        {
            Piece rook_queenside = get_collection().get_board_access().get_position(3, y).get_piece();
            rook_queenside.m_set_position(get_collection().get_board_access().get_position(0, y));
            rook_queenside.m_decrease_move();
            return;
        }
        else if (previous_position.get_x() == 6)
        {
            Piece rook_kingside = get_collection().get_board_access().get_position(5, y).get_piece();
            rook_kingside.m_set_position(get_collection().get_board_access().get_position(7, y));
            rook_kingside.m_decrease_move();
            return;
        }
    }

    // checks & pins
    private void m_check_all_directions() {
        LinkedList<PieceType> ll_implementations = new LinkedList<PieceType>();
        ll_implementations.add(PieceType.QUEEN);
        ll_implementations.add(PieceType.ROOK);
        check_direction(1, 0, ll_implementations);
        check_direction(-1, 0, ll_implementations);
        check_direction(0, 1, ll_implementations);
        check_direction(0, -1, ll_implementations);
        ll_implementations.remove(PieceType.ROOK);
        ll_implementations.add(PieceType.BISHOP);

        check_direction(1, 1, ll_implementations);
        check_direction(-1, 1, ll_implementations);
        check_direction(1, -1, ll_implementations);
        check_direction(-1, -1, ll_implementations);
        check_knight();
        check_pawn();
    }

    private void check_direction(int x_increment, int y_increment, LinkedList<PieceType> ll_implementations) {
        int x = super.get_position().get_x();
        int y = super.get_position().get_y();

        Piece previous_piece = null;
        Piece piece = null;
        int visible = 0;

        for (int i_x = x + x_increment, i_y = y + y_increment; i_x >= 0 && i_x < 8 && i_y >= 0 && i_y < 8; i_x += x_increment, i_y += y_increment) {
            piece = super.get_collection().get_board_access().get_position(i_x, i_y).get_piece();
            if (piece == null) {
                visible++;
                continue;
            }
            if (is_type(piece.get_type())) {
                if (previous_piece == null) {
                    previous_piece = piece;
                    continue;
                }
                return;
            }
            if (!ll_implementations.contains(piece.get_piece_type()))
            {
                return;
            }
            if (previous_piece != null) {
                visible_from += visible;
                m_send_restrictions(previous_piece, calculate_restrictions(x_increment, y_increment));
            }
            else
            {
                visible_from += visible + 4;
                m_send_restrictions(calculate_restrictions(x_increment, y_increment));
            }
            return;
        }
    }

    private LinkedList<Position> calculate_restrictions(int x_increment, int y_increment) {
        int x = super.get_position().get_x();
        int y = super.get_position().get_y();

        LinkedList<Position> ll_restrictions = new LinkedList<Position>();

        if (x - x_increment >= 0 && x - x_increment < 8 && y - y_increment >= 0 && y - y_increment < 8) {
            get_legal_moves().remove(super.get_collection().get_board_access().get_position(get_position().get_x() - x_increment, get_position().get_y() - y_increment));
        }

        for (int i_x = x + x_increment, i_y = y + y_increment; i_x >= 0 && i_x < 8 && i_y >= 0 && i_y < 8; i_x += x_increment, i_y += y_increment) {
            Position p = get_collection().get_board_access().get_position(i_x, i_y);
            ll_restrictions.add(p);
            if (p.get_piece() == null) {
                continue;
            }
            
            if (p.get_piece().get_type() == get_type()) {
                ll_restrictions.remove(p);
                continue;
            }
            else {
                //System.out.println("Problem child found!" + i_x + ", " + i_y);
                return ll_restrictions;
            }
        }
        throw new NullPointerException("King.calculate_restrictions() - Never found opposing piece");
    }

    private void check_knight() {
        int x = get_position().get_x();
        int y = get_position().get_y();
        PieceType knight = PieceType.KNIGHT;
        if (x < 7) {
            if (y < 6)
            m_check_move(x + 1, y + 2, knight);
            if (y > 1)
            m_check_move(x + 1, y - 2, knight);

            if (x < 6) {
                if (y < 7)
                m_check_move(x + 2, y + 1, knight);
                if (y > 0)
                m_check_move(x + 2, y - 1, knight);
            }
        }
        if (x > 0) {
            if (y < 6)
            m_check_move(x - 1, y + 2, knight);
            if (y > 1)
            m_check_move(x - 1, y - 2, knight);

            if (x > 1) {
                if (y < 7)
                m_check_move(x - 2, y + 1, knight);
                if (y > 0)
                m_check_move(x - 2, y - 1, knight);
            }
        }
    }

    private void check_pawn() {
        int x = get_position().get_x();
        int y = get_position().get_y();
        int directional_constant;
        if (is_type(Type.WHITE)) {
            directional_constant = 1;
        }
        else {
            directional_constant = -1;
        }
        if (x < 7 && y + directional_constant < 8 && y + directional_constant >= 0)
            m_check_move(x + 1, y + directional_constant, PieceType.PAWN);
        if (x > 0 && y + directional_constant < 8 && y + directional_constant >= 0)
            m_check_move(x - 1, y + directional_constant, PieceType.PAWN);
    }

    private void m_check_move(int x, int y, PieceType impl) {
        Position position = get_collection().get_board_access().get_position(x, y);
        if (position.get_piece() == null) {
            return;
        }
        if (!(is_type(position.get_piece().get_type())) && position.get_piece().get_piece_type() == impl) {
            LinkedList<Position> ll_restrictions = new LinkedList<Position>();
            ll_restrictions.add(position);
            m_send_restrictions(ll_restrictions);
        }

    }

    private void m_check_castling() {
        int y = pawn_directional(0, 7);
        if (can_castle_queenside()) {
            Piece rook_queenside = get_collection().get_board_access().get_position(0, y).get_piece();
            ///
            /// queenside castling
            /// checks, if there is any pieces inbetween rook and king
            /// checks, if any pieces of the opposing player can see the positions inbetween rook and king
            if (is_type(rook_queenside.get_type()) && rook_queenside.moves() == 0 )
            {
                if (get_collection().get_board_access().get_position(1, y).get_piece() == null &&
                    get_collection().get_board_access().get_position(2, y).get_piece() == null &&
                    get_collection().get_board_access().get_position(3, y).get_piece() == null) {
                    if (!get_collection().get_board_access().get_position(4, y).has_opposing_pieces_observing(get_type()) &&
                        !get_collection().get_board_access().get_position(1, y).has_opposing_pieces_observing(get_type()) &&
                        !get_collection().get_board_access().get_position(2, y).has_opposing_pieces_observing(get_type()) &&
                        !get_collection().get_board_access().get_position(3, y).has_opposing_pieces_observing(get_type())) {
                        get_legal_moves().put(get_collection().get_board_access().get_position(2, y), new MoveType[]{MoveType.CASTLING_QUEENSIDE});
                    }
                }
            }
        }
        if (can_castle_kingside()) {
            Piece rook_kingside = get_collection().get_board_access().get_position(7, y).get_piece();
            ///
            /// kingside castling
            /// checks, if there is any pieces inbetween rook and king
            /// checks, if any pieces of the opposing player can see the positions inbetween rook and king
            if (is_type(rook_kingside.get_type()) && rook_kingside.moves() < 1)
            {
                if (get_collection().get_board_access().get_position(5, y).get_piece() == null &&
                    get_collection().get_board_access().get_position(6, y).get_piece() == null) {
                    if (!get_collection().get_board_access().get_position(4, y).has_opposing_pieces_observing(get_type()) &&
                        !get_collection().get_board_access().get_position(6, y).has_opposing_pieces_observing(get_type()) &&
                        !get_collection().get_board_access().get_position(6, y).has_opposing_pieces_observing(get_type())) {
                        get_legal_moves().put(get_collection().get_board_access().get_position(6, y), new MoveType[]{MoveType.CASTLING_KINGSIDE});
                    }
                }
            }
        }
        
    }

    private void m_send_restrictions(LinkedList<Position> ll_restrictions) {
        get_collection().m_restrict_all_to(ll_restrictions);
        get_collection().m_acknowledge_check();
    }

    private void m_send_restrictions(Piece piece, LinkedList<Position> ll_restrictions) {
        super.get_collection().m_restrict(piece, ll_restrictions);
    }

    @Override
    public String toString() {
        return super.toString() + "K";
    }
}