package src.java.engine.board;

import java.util.LinkedList;

import src.java.engine.board.piecelib.*;
import src.java.engine.board.piecelib.Piece.PieceType;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.updatesystem.Restrictor;

import java.util.HashMap;

public class PieceCollection implements Restrictor {

    private HashMap<PieceType, LinkedList<Piece>> map_active_pieces;
    private LinkedList<Piece> ll_active_pieces;
    private LinkedList<Piece> ll_taken_pieces; 
    private LinkedList<Piece> ll_continuity;
    private Type type;
    private Board board;
    private boolean in_check;

    public PieceCollection(Type type, Board board) {
        this.type = type;
        this.board = board;
        this.in_check = false;
        map_active_pieces = new HashMap<PieceType, LinkedList<Piece>>();
        ll_active_pieces = new LinkedList<Piece>();
        ll_taken_pieces = new LinkedList<Piece>();
        ll_continuity = new LinkedList<Piece>();
    }

    /// ### getters ### ///

    public BoardAccess get_board_access() {
        return board;
    }

    /*
     * INDEX 0 RESERVED FOR KING!
     */
    public LinkedList<Piece> get_active_pieces() {
        return ll_active_pieces;
    }

    public LinkedList<Piece> get_pieces_of_type(PieceType impl) {
        return map_active_pieces.get(impl);
    }

    public Type get_type() {
        return type;
    }

    /// ### modifiers ### ///




    public void m_restrict(Piece piece, LinkedList<Position> ll_restrictions)
    {
        piece.m_restrict(ll_restrictions);
        m_request_update(piece);
    }


    public void m_restrict_all_to(LinkedList<Position> ll_restrictions)
    {
        for (Piece piece : get_active_pieces()) {
            m_restrict(piece, ll_restrictions);
        }
        m_acknowledge_check();
    }



    public void m_take(Piece piece)
    {
        m_rm_piece(piece);
        ll_taken_pieces.add(piece);
        piece.m_set_position(null);
    }


    public void m_untake(Piece piece) {
        m_add_piece(piece);
        ll_taken_pieces.remove(piece);
    }


    
    public void m_add_piece(Piece piece) {
        map_active_pieces.get(piece.get_piece_type()).add(piece);
        ll_active_pieces.add(piece);
        if (ll_active_pieces.size() > 16) {
            throw new IllegalArgumentException("We duplicated a piece: " + piece + "\n" + get_board_access());
        }
    }

    public void m_rm_piece(Piece piece) {
        if (piece.get_piece_type() == PieceType.KING) {
            throw new IllegalArgumentException("You were about to remove the King");
        }
        map_active_pieces.get(piece.get_piece_type()).remove(piece);
        boolean flag = ll_active_pieces.remove(piece);
        if (flag == false) {
            throw new IllegalArgumentException("Piece couldn't be removed: " + piece + "<size: "+ll_active_pieces.size()+" >\n" +ll_active_pieces);
        }
    }

    public void m_promote(Piece pawn, Position pos) {
        // TODO get_upgrade()?!?!
        PieceType impl = PieceType.QUEEN;
        Piece piece;

        int index = pawn.INDEX();
        m_rm_piece(pawn);
        ll_continuity.add(pawn);

        switch (impl) {
            case QUEEN:
                piece = new Queen(this, index);
                break;
            case ROOK:
                piece = new Rook(this, index);
                break;
            case BISHOP:
                piece = new Bishop(this, index);
                break;
            case KNIGHT:
                piece = new Knight(this, index);
                break;
        
            default:
                throw new IllegalArgumentException("tried upgrading Piece to non-viable Type");
        }
        //System.out.println("PROMOTION IN PROGRESS: ");
        piece.m_set_position(pos);
        m_add_piece(piece);
    }


    public void m_demote(Piece piece)
    {
        Position position = piece.position();
        piece.m_set_position(null);
        m_rm_piece(piece);

        Piece piece_continuity = ll_continuity.pollLast();
        m_add_piece(piece_continuity);
        piece_continuity.m_set_position(position);
    }


    public void m_acknowledge_check()
    {
        this.in_check = true;   
    }

    public void m_state()
    {
        if (m_has_moves())
        {
            if (board.get_history().is_draw_by_repetition())
            {
                board.m_set_draw();
                return;
            }
            if (!this.in_check)
            {
                board.m_set_normal();
                return; 
            }
            board.m_set_check();
            return;
        }
        if (get_pieces_of_type(PieceType.KING).get(0).position().has_opposing_pieces_observing(get_type()))
        {
            board.m_set_checkmate();
        }
        else
        {
            board.m_set_draw();
        }
    }
    
    public void m_release_check()
    {
        this.in_check = false;
    }

    public boolean m_has_moves()
    {
        for (Piece p : get_active_pieces())
        {
            if (p.get_legal_moves().isEmpty())
            {
                continue;
            }
            return true;
        }
        return false;
    }

    public void m_request_update()
    {
        for (Piece piece: ll_active_pieces)
        {
            m_request_update(piece);
        }
    }

    private void m_request_update(Piece piece)
    {
        board.m_receive_update_notification(piece.observer());
    }


    public void m_request_update(PieceType piece_type)
    {
        for (Piece piece : map_active_pieces.get(piece_type))
        if (piece.position().get_y() == piece.pawn_directional(4, 3))
        {
            board.m_receive_update_notification(piece.observer());
        }
    }


    public void m_standard_lineup() {

        ll_active_pieces.add(new King(this, 0));
        ll_active_pieces.add(new Queen(this, 1));
        ll_active_pieces.add(new Rook(this, 2));
        ll_active_pieces.add(new Rook(this, 3));
        ll_active_pieces.add(new Bishop(this, 4));
        ll_active_pieces.add(new Bishop(this, 5));
        ll_active_pieces.add(new Knight(this, 6));
        ll_active_pieces.add(new Knight(this, 7));

        for (int i = 0; i < 8; i++)
        {
            ll_active_pieces.add(new Pawn(this, 8 + i));
        }
        for (Piece piece : ll_active_pieces)
        {
            map_active_pieces.putIfAbsent(piece.get_piece_type(), new LinkedList<Piece>());
            map_active_pieces.get(piece.get_piece_type()).add(piece);
        }
    }

    public int[][] reduce()
    {
        int[][] reduced = new int[16][2];
        for (Piece piece : ll_active_pieces)
        {
            Position pos = piece.position();
            reduced[piece.INDEX()][0] = pos.get_x();
            reduced[piece.INDEX()][1] = pos.get_y();
        }
        return reduced;
    }
}