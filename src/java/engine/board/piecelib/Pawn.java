package src.java.engine.board.piecelib;

import src.java.engine.board.PieceCollection;
import src.java.engine.board.Position;

public class Pawn extends Piece {

    private static final int weight = 1;
    public Piece en_passant;
    public Position en_passant_position;

    public Pawn(PieceCollection collection, int index) {
        super(collection, index);
    }

    public int get_weight()
    {
        return weight;
    }
    
    public PieceType get_piece_type()
    {
        return PieceType.PAWN;
    }

    public void m_legal_moves()
    {
        m_pawn_moves();
    }

    @Override
    public void m_set_position(Position position)
    {
        if (position == null) {
            super.m_set_position(position);
            return;
        }
        if (this.position() == null)
        {
            super.m_set_position(position);
            return;
        }
        //
        // promotion
        //
        if (position.get_y() == pawn_directional(7, 0))
        {
            super.m_set_position(null);
            collection().m_promote(this, position);
            return;
        }

        Piece taken_piece = position.get_piece();
        //
        //  actually sets position
        //
        super.m_set_position(position);
        //
        //  checks, if the move was an en passant move
        //
        if (position().get_y() != pawn_directional(5, 2))
        {
            return;
        }
        if (taken_piece != null)
        {
            return;
        }
        Piece opposing_pawn = collection().get_board_access().get_position(position().get_x(), position().get_y() - pawn_directional(1, -1)).get_piece();
        if (opposing_pawn == null)
        {
            return;
        }
        if (opposing_pawn.is_type(get_type()))
        {
            return;
        }
        if (opposing_pawn.get_piece_type() != PieceType.PAWN)
        {
            return;
        }
        en_passant = opposing_pawn;
        en_passant_position = opposing_pawn.position();
        opposing_pawn.collection().m_take(opposing_pawn);

    }


    @Override
    public String toString()
    {
        return super.toString() + "p";
    }


}
