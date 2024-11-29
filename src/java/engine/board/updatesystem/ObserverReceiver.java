package src.java.engine.board.updatesystem;

import src.java.engine.board.Position;

public interface ObserverReceiver {
    
    public void m_observe(Position position);
    public void m_observe_silently(Position position);
    public void m_clear_observations(); 
}
