package src.java.engine.board.updatesystem;

import src.java.engine.board.Position;

/**
 *  Handles the ability to Observe a Position.
 *  This Interface should be used, if the owner is the one
 *  needing updates
 * 
 */
public interface ObserverReceiver
{
    public void m_observe(Position position);
    public void m_observe_silently(Position position);
    public void m_clear_observations(); 
}