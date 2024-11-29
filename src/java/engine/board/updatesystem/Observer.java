package src.java.engine.board.updatesystem;

import java.util.Iterator;
import java.util.LinkedList;

import src.java.engine.board.Position;
import src.java.engine.board.piecelib.Piece;

public class Observer implements ObserverReceiver, ObserverSender, ObserverStorage
{
    
    private Piece piece;
    // linked list -> no need for a bunch of list.get(i)
    private LinkedList<Position> ll_observed_positions;


    public Observer(Piece piece) {
        this.piece = piece;
        this.ll_observed_positions = new LinkedList<Position>();
    }

    //
    //
    /////// ####### getters ####### ///////
    //
    //


    public Piece get_piece()
    {
        return piece;
    }

    public LinkedList<Position> get_observed_positions()
    {
        return ll_observed_positions;
    }
    
    //
    //
    /////// ####### modifiers ####### ///////
    //
    //

    /*
     * Observer will observe specified Position
     *  -> Observer can be seen by other Pieces
     *
     *  
     * @param Position
     * @return void
     */
    public void m_observe(Position position)
    {
        position.m_subscribe(this);
        ll_observed_positions.add(position);
    }

    /*
     * Observer will observe specified Position
     *  -> Observer is hidden
     *
     *  
     * @param Position
     * @return void
     */
    public void m_observe_silently(Position position)
    {
        position.m_subscribe_silently(this);
        ll_observed_positions.add(position);
    }



    /*
     * Observer removes all Positions from storage
     *
     *  
     * @return void
     */
    public void m_clear_observations()
    {
        Iterator<Position> iterator = ll_observed_positions.iterator();
        while (iterator.hasNext()) {
            iterator.next().m_unsubscribe(this);
        }
        ll_observed_positions.clear();
    }



    /*
     * Observer will pass over specified restrictions to
     * it's designated Piece 
     * @param ll_restrictions: LinkedList<Position>
     *  
     * @return void
     */
    public void m_restrict(LinkedList<Position> ll_restrictions)
    {
        piece.m_restrict(ll_restrictions);

    }



    public void m_update()
    {
        piece.m_update();
    }
}
