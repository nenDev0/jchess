package src.java.engine.board.piecelib;


import src.java.engine.board.PieceCollection;


public class Bishop extends Piece
{


    private static final int weight = 3;


    public Bishop(PieceCollection collection, int index)
    {
        super(collection, index);
    }


    public int get_weight()
    {
        return weight;
    }


    public PieceType get_piece_type()
    {
        return PieceType.BISHOP;
    }


    public void m_legal_moves()
    {
        m_diagonal_moves();
    }
    

    @Override
    public String toString()
    {
        return super.toString() + "b";
    }

    
}
