package src.java.engine.board.updatesystem;

import src.java.engine.board.piecelib.Piece;

/**
 *  This Interface should only be used, if the handler does not
 *  require to interfere with the observation processes.
 * 
 * 
 */
public interface ObserverStorage
{
    public Piece get_piece();
}
