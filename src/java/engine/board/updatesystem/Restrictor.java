package src.java.engine.board.updatesystem;

import java.util.LinkedList;

import src.java.engine.board.Position;
import src.java.engine.board.piecelib.Piece;

/**
 * 
 * 
 */
public interface Restrictor
{
    public void m_restrict(Piece piece, LinkedList<Position> positions); 
    public void m_restrict_all_to(LinkedList<Position> positions);
}
