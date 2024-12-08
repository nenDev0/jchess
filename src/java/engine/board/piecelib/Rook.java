package src.java.engine.board.piecelib;

import src.java.engine.board.PieceCollection;


public class Rook extends Piece
{


    private static final int weight = 5;


    public Rook(PieceCollection collection, int index)
    {
        super(collection, index);
    }


    public int get_weight()
    {
        return weight;
    }


    public PieceType get_piece_type()
    {
        return PieceType.ROOK;
    }


    public void m_legal_moves()
    {
        m_horizontal_moves();
        m_vertical_moves();
    }


    public String toString()
    {
        return super.toString() + "r";
    }    


}
