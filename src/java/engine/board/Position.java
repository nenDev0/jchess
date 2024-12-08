package src.java.engine.board;

import java.util.LinkedList;

import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.updatesystem.NotificationCollector;
import src.java.engine.board.updatesystem.Observer;
import src.java.engine.board.updatesystem.ObserverSender;
import src.java.engine.board.updatesystem.ObserverStorage;


/**
 *  Handles {@link #ll_observers} from pieces, which would require updates based on
 *  changes committed onto this {@code Position}.
 * 
 *  <p> Contains a relationship with the {@link #piece} currently on the board's
 *      {@link #x}, {@link #y} values
 * 
 */
public class Position 
{


    private final int x;
    private final int y;
    private LinkedList<ObserverStorage> ll_observers;
    private LinkedList<ObserverStorage> ll_silent_observers;
    private Piece piece;
    private final NotificationCollector notification_collector;
   

    /**
     *  Constructor
     * 
     * @param x
     * @param y
     * @param notification_collector
     * 
     */
    public Position(int x, int y, NotificationCollector notification_collector)
    {
        ll_observers = new LinkedList<ObserverStorage>();
        ll_silent_observers = new LinkedList<ObserverStorage>();
        this.x = x;
        this.y = y;
        this.notification_collector = notification_collector;
    }


    /**
     * 
     * 
     * @return {@link #x}
     */
    public int get_x()
    {
        return x;
    }


    /**
     * 
     * 
     * @return {@link #y}
     */
    public int get_y()
    {
        return y;
    }


    /**
     * 
     * 
     * @return
     */
    public Piece get_piece()
    {
        return piece;
    }


    /**
     * 
     * 
     * @return
     */
    public LinkedList<ObserverStorage> get_observers()
    {
        return ll_observers;
    }


    /**
     * 
     * 
     * @param type
     * 
     * @return
     */
    public boolean has_opposing_pieces_observing(Type type)
    {
        for(ObserverStorage o : ll_observers) {
            Piece p = o.get_piece();
            if (!p.is_type(type))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * 
     * 
     * @param piece
     * 
     */
    public void m_set_piece(Piece piece)
    {
        this.piece = piece;
        m_pass_observers_over();
    }


    /**
     * 
     * 
     */
    public void m_rm_piece()
    {
        this.piece = null;
        m_pass_observers_over();
    }


    /**
     * 
     * 
     * @param o
     * 
     */
    public void m_subscribe(Observer o)
    {
        this.ll_observers.add(o);
    }


    /**
     * 
     * @param o
     * 
     */
    public void m_subscribe_silently(Observer o)
    {
        this.ll_silent_observers.add(o);
    }


    /**
     * 
     * 
     * @param o
     * 
     */
    public void m_unsubscribe(Observer o)
    {
        this.ll_observers.remove(o);
        this.ll_silent_observers.remove(o);
    }


    /**
     * 
     * 
     */
    private void m_pass_observers_over()
    {
        for (ObserverStorage observer : ll_observers)
        {
            notification_collector.m_receive_update_notification((ObserverSender) observer);
        }
        for (ObserverStorage observer : ll_silent_observers)
        {
            notification_collector.m_receive_update_notification((ObserverSender) observer);
        }
        notification_collector.m_receive_visual_update_notification(this);
    }
 

    /**
     * 
     * 
     * @throws NullPointerException {@code if (board == null)}
     * @throws ArrayIndexOutOfBoundsException {@code if (0 < x, y || x, y > 7)} 
     * 
     * @param board
     * 
     * @return
     */
    public Position convert(Board board)
    {
        return board.get_position(x, y);
    }


    @Override
    public String toString()
    {
        return "("+(get_x())+", "+(get_y())+")";
    }


    @Override
    public int hashCode()
    {
        return (x << 3) + y;
    }

}
