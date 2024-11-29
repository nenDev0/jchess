package src.java.engine.board.piecelib;

import src.java.engine.board.PieceCollection;

public class Queen extends Piece{

    private static final int weight = 8;

    public Queen(PieceCollection collection, int index)
    {
        super(collection, index);
    }

    //
    //
    /////// ####### getters ####### ///////
    //
    //

    public int get_weight()
    {
        return weight;
    }

    
    public PieceType get_piece_type()
    {
        return PieceType.QUEEN;
    }

    //
    //
    /////// ####### modifiers ####### ///////
    //
    //

    public void m_legal_moves()
    {
        m_diagonal_moves();
        m_horizontal_moves();
        m_vertical_moves();
    }


    @Override
    public String toString()
    {
        return super.toString() + "Q";
    }
}