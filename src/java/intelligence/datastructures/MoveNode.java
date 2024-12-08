package src.java.intelligence.datastructures;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeSet;

import src.java.engine.board.Board;
import src.java.engine.board.Board.GameState;
import src.java.engine.board.Move;
import src.java.engine.board.Position;
import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.evaluation.Calculator;

/**
 *  Used to generate a tree of {@link #Move},
 *  calculating the best possible move, based upon evaluations of future positions.
 * 
 */
public class MoveNode extends Move implements Comparable<MoveNode>
{
    private TreeSet<MoveNode> set_children;
    private TreeSet<MoveNode> set_abandoned;
    private TreeHeader header;
    private boolean is_final;
    private float actual_weight;
    ///
    /// If two parent nodes have this node as a child, this will prevent them from doing duplicate calculations
    private boolean best_move_calculated;


    public MoveNode(Position position1, Position position2, MoveType[] arr_types, TreeHeader header)
    {
        super(position1, position2, arr_types);
        set_children = new TreeSet<MoveNode>();
        set_abandoned = new TreeSet<MoveNode>();
        this.header = header;
        this.is_final = false;
        this.best_move_calculated = false;
        //this.dad = dad;
    }


    /**
     *  Alternative Constructor
     *  only used for the top node, as it requires no positions to be saved.
     * 
     * @param header
     * 
     */
    public MoveNode(TreeHeader header)
    {
        super();
        set_children = new TreeSet<MoveNode>();
        set_abandoned = new TreeSet<MoveNode>();
        this.header = header;
        this.is_final = false;
        this.best_move_calculated = false;
    }


    /**
     * 
     * 
     * @param iteration
     * 
     * @return {@code int} // maximum amount of children, this node should have
     */
    public int max_children(int iteration)
    {
        return (header.get_depth() - iteration)/5 + 2;
        //return 8;
        
        //return Integer.MAX_VALUE;
    }


    /**
     * 
     * 
     * @return {@link #set_children}
     */
    public TreeSet<MoveNode> get_children()
    {
        return set_children;
    }


    /**
     * 
     * 
     * @return {@link #set_abandoned}
     */
    public TreeSet<MoveNode> get_abandoned()
    {
        return set_abandoned;
    }

    
    /**
     *  Used to find the node in the tree, which followed the move played in the current game.
     * 
     *  <p> {@code if} found, sets children of this node to the children of that node,
     *      {@code else} sets children = null in order to restart calculation entirely.
     *  
     *  <p> Allows the algorithm to potentially reuse previously calculated moves.
     * 
     * 
     * @param move
     * @param board
     * 
     */
    public void m_set_children(Move move, Board board)
    {
        for (MoveNode child : set_children)
        {
            if (move.equals(child))
            {
                this.set_children = child.get_children();
                this.set_abandoned = child.get_abandoned();
                return;
            }
        }
        for (MoveNode child : set_abandoned)
        {
            if (move.equals(child))
            {
                this.set_children = child.get_children();
                this.set_abandoned = child.get_abandoned();
                return;
            }
        }
    }


    /**
     *  Only executed upon node creation. Determines, whether this node is final (a leaf).
     * 
     *  <p> ->  {@link #is_final} 
     *           (true: This node has no children)
     * 
     * @param board
     * @param calculator
     * @param iteration
     * 
     * @return boolean currently #unused
     */
    public boolean create_node(Board board, Calculator calculator, int iteration)
    {
        header.m_add_total_executions();
        this.is_final = set_final_state(board, calculator, iteration);

        if (is_dead(board, iteration))
        {
            return false;
        }

        m_add_weight(calculator.evaluate(board));
        this.actual_weight = super.get_weight();


        return true;
    }


    /**
     *  compares the vectors, which lead to this board state,
     *  with vectors of all currently existing nodes in this iteration
     * 
     *  <p> sets the children of this node to the children of the node,
     *      which has the same children to avoid duplicate, redundant calculations
     * 
     * @param board
     * @param iteration
     * 
     * @return boolean
     *  (
     *   {@code true}: current board position already calculated
     *   {@code false}: not calculated, continue calculation
     *  )
     */
    public boolean is_dead(Board board, int iteration)
    {
        /*
         *  @int i
         *   -> allows for both parties to cut off nodes
         *   -> calculation is done 4 moves down the line
         *       black starts a move later, hence i_b = i_w + 1
         */
        if (iteration >= 3 && iteration % 2 == 1)
        {
            long time_start = System.nanoTime();
            MoveNode older_cousin = header.m_add_history_to_cache(this, board.get_history().get_as_vectors(header.get_move_count()),(iteration - 3)/2);
            if (older_cousin != null)
            {
                this.set_children.addAll(older_cousin.get_children());
                this.set_abandoned.addAll(older_cousin.get_abandoned());

                m_add_weight(older_cousin.get_weight());
                this.actual_weight = get_weight();
                long time_stop = System.nanoTime();
                header.time += (time_stop - time_start)/1000;
                header.m_add_total_nodes_ended();
                return true;
            }
            long time_stop = System.nanoTime();
            header.time += (time_stop - time_start)/1000;
        }
        return false;
    }


    /**
     *  continues down this node
     *   -> only executed, if node has already been created in a previous execution
     * 
     * @param board
     * @param calculator
     * @param iteration
     * 
     */
    public void m_continue(Board board, Calculator calculator, int iteration)
    {
        header.m_add_total_executions_saved();

        ///
        /// allows get_best_move_recursive to calculate
        this.best_move_calculated = false;

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
        for (MoveNode child : set_children)
        {
            try
            {
                board.m_commit(child);
            
                child.m_continue(board, calculator, iteration + 1);
                board.m_revert();
            }
            catch (Exception e)
            {
                System.out.println(set_children);
                throw e;
            }
        }
    }


    /**
     *  Create children recursively
     *  -> Depth First
     * 
     * @param board
     * @param calculator
     * @param iteration
     * 
     */
    public void m_create_tree(Board board, Calculator calculator, int iteration)
    {
        ///
        /// termination conditions
        if (is_final)
        {
            return;
        }
        if (is_depth(iteration))
        {
            return;
        }
        ///
        /// This node already has calculated children
        /// This happens only to the top_node. Potentially could be pulled out?
        if (!set_children.isEmpty() || !set_abandoned.isEmpty())
        {
            m_continue(board, calculator, iteration);
            return;
        }
        ///
        /// each pieces legal moves get added as a possible future node
        LinkedList<MoveNode> ll_movetree = new LinkedList<MoveNode>();
        for     (Piece piece : board.get_collection(board.get_type()).get_active_pieces())
        {
            for (Entry<Position, MoveType[]> entry : piece.get_legal_moves().entrySet())
            {
                ll_movetree.add(new MoveNode(piece.get_position(), entry.getKey(), entry.getValue(), header));
            }
        }
        ///
        /// ended nodes are separated here and added back towards the end.
        /// This allows for more accurate get_best_move results, while
        /// reducing duplicated calculations
        // LinkedList<MoveNode> ll_ended_nodes = new LinkedList<MoveNode>();
        ///
        /// for each possible move, calculate their weight, final state
        /// and compare with cached nodes.
        /// 
        /// If the childnode already exists,
        /// the child node will clone the children of the cached node and return false.
        /// This node will then add it to the abandoned nodes, as it is not necessary to calculate further
        Iterator<MoveNode> iterator = ll_movetree.iterator();
        while (iterator.hasNext())
        {
            MoveNode move = iterator.next();
            try {
                board.m_commit(move);
                if (!move.create_node(board, calculator, iteration + 1))
                {
                    //ll_ended_nodes.add(move);
                    set_abandoned.add(move);
                    iterator.remove();
                }
                board.m_revert();
            } catch (Exception e) {
                System.out.println(board);
                System.out.println(set_children);
                throw e;
            }
        }
        set_children.addAll(ll_movetree);
        ll_movetree.clear();
        ///
        if (set_children.isEmpty() /*&& ll_ended_nodes.isEmpty()*/)
        {
            is_final = true;
            return;
        }
        /// 
        /// abandones the nodes, which currently don't have a
        /// positive evaluation, massively reducing calculations,
        /// however an inaccurate first evaluation leads to
        /// worse results in the long run.
        m_abandon_children(board, iteration);
        /// recursion
        for (MoveNode move : set_children)
        {
            try
            {
                board.m_commit(move);
                move.m_create_tree(board, calculator, iteration + 1);
                board.m_revert();   
            }
            catch (Exception e)
            {
                System.out.println(board);
                System.out.println(set_children);
                throw e;
            }
        }
        /// 
        /// adds the nodes, which didn't require further calculations
        /// back into the set, so they can be used for the get_best_move
        /// calculation
        //set_children.addAll(ll_ended_nodes);
    }
    

    /**
     * 
     * 
     * @param board
     * @param iteration
     * 
     */
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


    /**
     * 
     * @param board
     * @param iteration
     * 
     */
    private void m_abandon_children(Board board, int iteration)
    {
        /*
         * TODO: consider using averages with cutoff again?
         * would require searches to be breadth first to make sense
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


    /**
     *  Find the best move
     *  -> Depth First
     * 
     * 
     * @param type
     * 
     * @return best {@code move} to play, based on evaluations
     */
    public Move get_best_move_recursive(Type type)
    {
        /// This node is a leaf
        if (set_children.isEmpty())
        {
            return this;
        }
        ///
        /// prevents this recursion tree from being calculated multiple times
        if (!best_move_calculated)
        {
            best_move_calculated = true;
            ///
            /// Separate array required, to enable resorting of the set
            /// TODO: Do we need to reorder the set or can we simply find min, max here?
            /// TODO: any way to not use ugly Objects here?
            Object[] arr_children = set_children.toArray();
            set_children.clear();
            ///
            /// Recursively find the best move
            /// Every iteration will use a different type
            for (Object object : arr_children)
            {
                if (object instanceof MoveNode)
                {
                    MoveNode move = (MoveNode)object;
                    move.get_best_move_recursive(Type.get_opposite(type));
                    set_children.add(move);
            } // object instance of Movenode
                else {
                    throw new IllegalArgumentException("Object is not a MoveNode.");
                }
            }
        }
        /// 
        /// Choose the best move for either side
        /// ->  calculating your next best move requires anticipating
        ///     your oponent's best move, therefore this is not specific
        ///     to the type of the player
        Move move;
        if (type == Type.WHITE)
        {
            move = set_children.last();
        }
        else
        {
            move = set_children.first();
        }
        /// 
        /// This move now has the weight of it's own 'best' child
        /// This change is reverted later
        m_add_weight(move.get_weight());
        //header.m_add_children_to_average(set_children.size());
        ///
        /// return value is only used to hand the move over to the TreeHeader by the top_node
        return move;
    }


    /**
     * 
     * 
     * @param iteration
     * 
     * @return
     */
    private boolean is_depth(int iteration)
    {
        if (iteration == header.get_depth())
        {
            return true;
        }
        return false;
    }


    /**
     *  determines, whether there will be any more nodes coming after this
     * 
     * @param board
     * @param calculator
     * @param iteration
     * 
     * @return boolean (replaces is_final)
     */
    private boolean set_final_state(Board board, Calculator calculator, int iteration)
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


    /**
     *  necessary to sort set_children
     *  implements no opportunity for 0, as this would remove the move from the set,
     *  which is not intended
     *  
     * @param MoveNode
     * 
     * @return int
     */
    @Override
    public int compareTo(MoveNode move)
    {
        if (move.get_weight() > get_weight())
        {
            return -1;
        }
        ///
        /// move.get_weight() <= get_weight()
        return 1;
    }

}