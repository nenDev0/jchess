package src.java.engine.board.updatesystem;

import java.util.LinkedList;

public abstract class NotificationCollector
{
    // linked list -> no need for a bunch of list.get(i)
    private LinkedList<ObserverSender> ll_observers;
    private LinkedList<ObserverSender> ll_required_observers;

    public NotificationCollector ()
    {
        this.ll_observers = new LinkedList<ObserverSender>();
        this.ll_required_observers = new LinkedList<>();
    }

    //
    //
    /////// ####### modifiers ####### ///////
    //
    //
    
    public void m_add_required_observers(Observer observer)
    {
        ll_required_observers.add(observer);
    }

    public void m_receive_update_notification(ObserverSender observer)
    {
        ll_observers.add(observer);
    }

    public void m_dump_update_notifications()
    {
        LinkedList<ObserverSender> ll_clone = new LinkedList<ObserverSender>();
        ll_clone.addAll(ll_observers);

        ll_observers.clear();

        for (ObserverSender observer_sender : ll_clone) {
                observer_sender.m_update();
        }
        ll_clone.clear();

        for (ObserverSender o : ll_required_observers) {
            o.m_update();
        }
    }

}
