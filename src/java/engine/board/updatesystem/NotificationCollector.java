package src.java.engine.board.updatesystem;

import java.util.LinkedList;

import src.java.engine.board.Position;
import src.java.engine.game.InteractionController;

public abstract class NotificationCollector
{
    private InteractionController interaction_controller;


    // linked list -> no need for a bunch of list.get(i)
    private LinkedList<ObserverSender> ll_observers;
    private LinkedList<ObserverSender> ll_required_observers;
    private LinkedList<Position> ll_visually_altered_positions; // updates the UI

    public NotificationCollector ()
    {
        this.ll_observers = new LinkedList<ObserverSender>();
        this.ll_required_observers = new LinkedList<ObserverSender>();
    }

    //
    //
    /////// ####### modifiers ####### ///////
    //
    //

    
    public void m_set_interaction_controller(InteractionController interaction_controller)
    {
        this.interaction_controller = interaction_controller;
        this.ll_visually_altered_positions = new LinkedList<Position>();
    }


    public void m_add_required_observers(Observer observer)
    {
        ll_required_observers.add(observer);
    }

    public void m_receive_update_notification(ObserverSender observer)
    {
        ll_observers.add(observer);
    }

    public void m_receive_visual_update_notification(Position position)
    {
        if (interaction_controller == null)
        {
            return;
        }
        ll_visually_altered_positions.add(position);
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

        if (interaction_controller == null)
        {
            return;
        }
        for (Position position : ll_visually_altered_positions) {
            interaction_controller.m_update_grid_position(position);
        }
        ll_visually_altered_positions.clear();
    }

}
