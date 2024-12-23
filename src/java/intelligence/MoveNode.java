package src.java.intelligence;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.TreeSet;

import src.java.engine.board.Board;
import src.java.engine.board.Board.GameState;
import src.java.engine.board.Move;
import src.java.engine.board.Position;
import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.evaluation.Calculator;

public class MoveNode extends Move implements Comparable<MoveNode>
{

    private TreeSet<MoveNode> set_children;
    private TreeSet<MoveNode> set_abandoned;
    private TreeHeader header;
    private boolean is_final;
    private TreeMap<Integer, Integer> map_history_vectors;
    private boolean children_requested;
    private float actual_weight;

    public MoveNode(Position position1, Position position2 , TreeHeader header)
    {
        super(position1, position2);
        set_children = new TreeSet<MoveNode>();
        set_abandoned = new TreeSet<MoveNode>();
        this.header = header;
        this.is_final = false;
        this.children_requested = false;
        //this.dad = dad;
    }

    //
    //  Alternate Constructor
    //  only used for the top node
    //
    public MoveNode(TreeHeader header)
    {
        super();
        set_children = new TreeSet<MoveNode>();
        set_abandoned = new TreeSet<MoveNode>();
        this.header = header;
        this.is_final = false;
        this.children_requested = false;
    }

    public int max_children(int iteration)
    {
        return (header.get_depth() - iteration)/2 + 2;
        //return Integer.MAX_VALUE;
    }


    public TreeSet<MoveNode> children()
    {
        this.children_requested = true;
        return set_children;
    }

    public TreeSet<MoveNode> abandoned()
    {
        return set_abandoned;
    }

    public TreeMap<Integer, Integer> get_history_vectors()
    {
        return map_history_vectors;
    }

    public Type opposite(Type type)
    {
        if (type == Type.WHITE) {
            return Type.BLACK;
        }
        else {
            return Type.WHITE;
        }
    }


    ///
    /// ###               ### ///
    /// ###   modifiers   ### ///
    /// ###               ### ///
    ///

    

    public void m_delete()
    {
        if (children_requested)
        {
            return;
        }
        for (MoveNode child : set_children)
        {
            child.m_delete();
        }
        set_children.clear();
        set_abandoned.clear();
    }

    public void m_set_children(Move move, Board board)
    {
        TreeSet<MoveNode> new_set = null;
        TreeSet<MoveNode> new_set_abandoned = null;
        while(true)
        {
            MoveNode child = set_children.pollLast();
            if (child == null)
            {
                if (new_set == null)
                {
                    header.m_rm_cache();
                    m_delete();
                }
                break;
            }
            if (move.equals(child))
            {
                new_set = child.children();
                new_set_abandoned = child.abandoned();
                //System.out.println("Found move!");
                continue;
            }
            child.m_delete();
        }
        if (new_set != null)
        {
            this.set_children = new_set;
            this.set_abandoned = new_set_abandoned;
        }
    }



    /*
     * create_node()
     *  Node is first created
     *   -> used to determine the state of the node
     *       -> @is_final: boolean
     *           (true: This node has no children)
     * 
     * 
     */
    public boolean create_node(Board board, Calculator calculator, int iteration)
    {
        header.m_add_total_executions();

        this.is_final = m_set_final_state(board, calculator, iteration);
        m_add_weight(calculator.evaluate(board));
        this.actual_weight = super.get_weight();

        // WHAT IS HAPPENING!=??=$")=?="§)$(§!=(?§"%(!"§$")))
        // TODO WHY DOES THIS NOT WORK YOU.. {*I dont get angry, when coding*}
        /*if (is_dead(board, iteration))
        {
            return false;
        }*/

        return true;
    }

    /*
     * is_dead()
     *  compares the vectors, which lead to this board state
     *  with vectors of all currently existing moveorders
     *  in this iteration
     * 
     * @param Board
     * @param iteration
     * 
     * 
     * @return boolean
     *  (
     *   -> true: current board position already calculated
     *   -> false: not calculated, continue calculation
     *  )
     */
    //TODO once ignored, can't be added back, even if this line wasn't followed
    public boolean is_dead(Board board, int iteration)
    {
        /*
         *  @int i
         *   -> allows for both parties to cut off nodes
         *   -> calculation is done 4 moves down the line
         *       black starts a move later, hence i_b = i_w + 1
         */
        if (iteration >= 4 && iteration % 2 == 0)
        {
            //long time_start = System.nanoTime();
            this.map_history_vectors = board.get_history().get_as_vectors(header.get_move_count());
            MoveNode older_cousin = header.m_add_history_to_cache(this, iteration/2 - 2);
            if (older_cousin != null)
            {
                this.set_children = older_cousin.children();
                this.set_abandoned = older_cousin.abandoned();
                //System.out.println("we are at iteration: "+ iteration +"\n and cutoff 2:\n"+ board.get_history().get_last_4_as_vec());
                //long time_stop = System.nanoTime();
                //header.time += (time_stop - time_start)/1000000;
                header.m_add_total_nodes_ended();
                return true;
            }
            //long time_stop = System.nanoTime();
            //header.time += (time_stop - time_start)/1000000;
            //System.out.println("we are at iteration: "+ iteration +"\n and did not cutoff 1:\n"+ board.get_history().get_last_4_as_vec());
        }
        return false;
    }




    /*
     * m_continue()
     *  continues down this node
     *   -> only executed, if node has already been created
     *       in a previous execution
     * 
     * @param board
     * @param calculator
     * @param iteration
     * 
     * @return void
     */
    public void m_continue(Board board, Calculator calculator, int iteration)
    {
        //header.m_add_total_executions_saved();

        m_reunite_children(board, iteration);
        m_add_weight(this.actual_weight);

        //  can't continue
        //  -> create tree from here
        //  stops if "is_final"
        if (set_children.isEmpty())
        {
            if (is_final)
            {
                return;
            }
            m_create_tree(board, calculator, iteration);
            return;
        }

        //  can continue
        //  -> follows down the tree
        System.out.println(set_children);
        for (MoveNode child : set_children)
        {
            // TODO remove temp & try_catch
            //Board temp = board.clone();
            //try
            //{
            System.out.println(child);
            board.m_commit(child);
            //} catch (Exception e)
            //{
            //    //m_print_hierarchy(child);
            //    System.out.println("We woulda printed the hierarchy here");
            //    System.out.println("we printing the clone at iteration: " + iteration + "\n" + temp);
            //    System.out.println("We printing all children:\n" +this.set_children);
            //    throw e;
            //}
            child.m_continue(board, calculator, iteration + 1);
            board.m_revert();
        }
    }

    //
    //  Create children recursively
    //  -> Depth First
    //
    public void m_create_tree(Board board, Calculator calculator, int iteration)
    {
        if (is_final)
        {
            return;
        }
        if (m_is_depth(iteration))
        {
            return;
        }

        set_children.clear();
        set_abandoned.clear();

        LinkedList<MoveNode> ll_movetree = new LinkedList<MoveNode>();
        for     (Piece piece : board.get_collection(board.get_type()).get_active_pieces())
        {
            for (Position position : piece.get_legal_moves())
            {
                ll_movetree.add(new MoveNode(piece.position(), position, header));
            }
        }

        Iterator<MoveNode> iterator = ll_movetree.iterator();
        while (iterator.hasNext())
        {
            MoveNode move = iterator.next();
            //Board temp = board.clone();
            board.m_commit(move);
            if (!move.create_node(board, calculator, iteration + 1))
            {
                set_abandoned.add(move);
                iterator.remove();
            }
            board.m_revert();
            /*
            Object o = temp.continuity_check(board);
            if (o != null)
            {
                throw new IllegalArgumentException("\nCONTINUITY ISSUE, board\n\n"+ board +"\n\n\n-> HOW IT SHOULD BE:\n\n"+temp+"\n\n\n---\n The problem child is:\n"+o);
            }
                */
        }
        set_children.addAll(ll_movetree);
        ll_movetree.clear();
        m_abandon_children(board, iteration);

        if (set_children.isEmpty())
        {
            is_final = true;
            return;
        }
        

        for (MoveNode move : set_children)
        {
            //Board temp = board.clone(); // REMOVE temp board & try-catch
            board.m_commit(move);
            move.m_create_tree(board, calculator, iteration + 1);
            board.m_revert();
            /*Object o = temp.continuity_check(board);
            if (o != null)
            {
                System.out.println("\n"+temp+"\n");
                System.out.println("\n"+board+"\n");
                throw new IllegalArgumentException("\nCONTINUITY ISSUE, board\n\n"+ board +"\n\n\n-> HOW IT SHOULD BE:\n\n"+temp+"\n\n\n---\n The problem child is:\n"+o);
            }*/
        }

    }
    

    private void m_reunite_children(Board board, int iteration)
    {
        if (board.get_type() == Type.WHITE)
        {
            while (max_children(iteration) > set_children.size() && !set_abandoned.isEmpty())
            {
                set_children.add(set_abandoned.pollLast());
            }
        }
        else
        {
            while (max_children(iteration) > set_children.size() && !set_abandoned.isEmpty())
            {
                set_children.add(set_abandoned.pollFirst());
            }
        }

    }

    private void m_abandon_children(Board board, int iteration)
    {
        /*
         * consider using averages with cutoff again?
         * 
         */
        if (board.get_type() == Type.WHITE)
        {
            while (max_children(iteration) < set_children.size())
            {
                set_abandoned.add(set_children.pollFirst());
            }
        }
        else
        {
            while (max_children(iteration) < set_children.size())
            {
                set_abandoned.add(set_children.pollLast());
            }
        }
    }

    //
    //  Find the best move
    //  -> Depth First
    //
    //
    public Move get_best_move_recursive(Type type)
    {

        if (set_children.isEmpty())
        {
            return this;
        }
        Iterator<MoveNode> iterator = set_children.iterator();
        while (iterator.hasNext())
        {
            MoveNode move = iterator.next();
            move.get_best_move_recursive(opposite(type));
        }
        Move move;
        if (type == Type.WHITE)
        {
            move = set_children.last();
        }
        else
        {
            move = set_children.first();
        }

        m_add_weight(move.get_weight());
        //header.m_add_children_to_average(set_children.size());
        return move;
    }


    private boolean m_is_depth(int iteration)
    {
        if (iteration == header.get_depth())
        {
            header.m_add_value_to_average(get_weight());
            return true;
        }
        return false;
    }

    /*
     * m_set_final_state()
     *  determines, whether there will be any more
     *   nodes coming after this
     * 
     * @param board
     * @param calculator
     * @param iteration
     * 
     * 
     * @return boolean (replaces is_final)
     */
    private boolean m_set_final_state(Board board, Calculator calculator, int iteration)
    {

        if (board.get_state().equals(GameState.CHECKMATE))
        {
            return true;
        }
        if (board.get_state().equals(GameState.DRAW))
        {
            if (board.get_type() != Type.BLACK)
            {
                m_add_weight((float)(-header.get_weight() - 2.225));
            }
            else
            {
                m_add_weight((float)(-header.get_weight() + 2.225));
            }
            return true;
        }
        return false;
    }

    
    /*
     * compareTo
     *  necessary to sort set_children
     *  
     *  @param MoveNode
     * 
     *  @return int
     */
    //move1.compareTo(move2)
    @Override
    public int compareTo(MoveNode move)
    {
        if (move.get_weight() > get_weight())
        {
            return -1;
        }
        if (move.get_weight() < get_weight())
        {
            return 1;
        }
        if (move.equals(move) && move.position_from() != null && move.position_to() != null)
        {
            return 0;
        }
        throw new IllegalArgumentException("AAAA");
    }
}