package src.java.engine.board;

import java.util.LinkedList;

import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.updatesystem.NotificationCollector;
import src.java.engine.board.updatesystem.Observer;
import src.java.engine.board.updatesystem.ObserverSender;
import src.java.engine.board.updatesystem.ObserverStorage;

public class Position 
{
    private int x;
    private int y;
    private LinkedList<ObserverStorage> ll_observers;
    private LinkedList<ObserverStorage> ll_silent_observers;
    private Piece piece;
    private NotificationCollector notification_collector;
   
    public Position(int x, int y, NotificationCollector notification_collector) {
        ll_observers = new LinkedList<ObserverStorage>();
        ll_silent_observers = new LinkedList<ObserverStorage>();
        this.x = x;
        this.y = y;
        this.notification_collector = notification_collector;
    }

    public int get_x() {
        return x;
    }

    public int get_y() {
        return y;
    }

    public Piece get_piece() {
        return piece;
    }

    public LinkedList<ObserverStorage> get_observers() {
        return ll_observers;
    }

    public boolean has_opposing_pieces_observing(Type type) {
        for(ObserverStorage o : ll_observers) {
            Piece p = o.get_piece();
            if (!p.is_type(type)) {
                return true;
            }
        }
        return false;
    }

    // PIECE ONLY
    public void m_set_piece(Piece piece) {
        this.piece = piece;
        m_pass_observers_over();
    }

    public void m_rm_piece() {
        this.piece = null;
        m_pass_observers_over();
    }

    public void m_subscribe(Observer o) {
        this.ll_observers.add(o);
    }

    public void m_subscribe_silently(Observer o) {
        this.ll_silent_observers.add(o);
    }

    public void m_unsubscribe(Observer o) {
        this.ll_observers.remove(o);
        this.ll_silent_observers.remove(o);
    }

    private void m_pass_observers_over() {
        for (ObserverStorage observer : ll_observers) {
            notification_collector.m_receive_update_notification((ObserverSender) observer);
        }
        for (ObserverStorage observer : ll_silent_observers) {
            notification_collector.m_receive_update_notification((ObserverSender) observer);
        }
        notification_collector.m_receive_visual_update_notification(this);
    }

    public Position convert(Board board)
    {
        return board.get_position(x, y);
    }

    @Override
    public String toString() {
        return "("+(get_x())+", "+(get_y())+")";
    }

    @Override
    public int hashCode() {
        return (x << 3) + y;
    }

}
