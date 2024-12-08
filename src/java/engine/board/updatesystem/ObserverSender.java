package src.java.engine.board.updatesystem;

import java.util.LinkedList;

import src.java.engine.board.Position;

/**
 *  Handles the ability to send updates to Pieces observing this Position.
 * 
 * 
 */
public interface ObserverSender
{
    public void m_update();
    public void m_restrict(LinkedList<Position> ll_restrictions);
}
