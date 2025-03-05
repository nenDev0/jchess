package src.java.intelligence.datastructures;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.TreeSet;

import src.java.engine.board.Board;
import src.java.engine.board.Board.GameState;
import src.java.engine.board.Move;
import src.java.engine.board.Move.MoveType;
import src.java.engine.board.Position;
import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.evaluation.Calculator;

/**
 *  Used to generate a tree of {@link #Move},
 *  calculating the best possible move, based upon evaluations of future positions.
 *
 */
public class MoveNode implements Comparable<MoveNode>
{


    private int iteration;
    private TreeSet<MoveNode> set_stacked;
    private TreeSet<MoveNode> set_abandoned;
    private final TreeHeader header;
    private boolean is_final;
    private LinkedList<MoveNode> ll_parent_nodes;
    ///
    /// actual weight is the weight of the board this move generates specifically.
    /// weight is partially used to pass on the weight of this tree branch overall.
    private float actual_weight;
    private float weight;
    private Move move;
    ///
    /// If two parent nodes have this node as a child, this will prevent them from doing duplicate calculations
    private boolean best_move_calculated;


    /**
     * Constructor
     *
     * @param position1
     * @param position2
     * @param arr_types
     * @param header
     *
     */
    public MoveNode(Move move, MoveNode parent, TreeHeader header)
    {
        this.move = move;
        set_stacked = new TreeSet<MoveNode>();
        set_abandoned = new TreeSet<MoveNode>();
        ll_parent_nodes = new LinkedList<MoveNode>();
        if (parent != null)
        {
            ll_parent_nodes.add(parent);
        }
        this.header = header;
        this.is_final = false;
        this.best_move_calculated = false;
    }


    public int get_iteration()
    {
        return iteration;
    }


    /**
     *  adjust the local iteration and recursively adjust the iteration of this nodes childnodes accordingly.
     *
     * @param parent
     * @param iteration
     *
     */
    public void m_adjust_iteration(MoveNode parent, int iteration)
    {
        if (parent != null)
        {
            if (parent != ll_parent_nodes.getFirst())
            {
                return;
            }
        }
        this.iteration = iteration;
        for (MoveNode child : set_abandoned)
        {
            child.m_adjust_iteration(this, iteration + 1);
        }
        for (MoveNode child : set_stacked)
        {
            child.m_adjust_iteration(this, iteration + 1);
        }
    }


    /**
     *  removes the given parent from the parents ({@link #ll_parent_nodes}) of this node.
     *
     *
     */
    private void m_delete(MoveNode parent)
    {
        if (parent != null)
        {
            ll_parent_nodes.remove(parent);
            if (ll_parent_nodes.size() > 0)
            {
                return;
            }
        }
        for (MoveNode child : set_abandoned)
        {
            child.m_delete(this);
        }
        for (MoveNode child : set_stacked)
        {
            child.m_delete(this);
        }
    }


    /**
     *  This may be used to find an intersection.
     *
     * @param seeked_node
     * @param top_node
     *
     * @return
     */
    public LinkedList<MoveNode> get_branch()
    {
        LinkedList<MoveNode> ll_branch = new LinkedList<MoveNode>();
        if (ll_parent_nodes.size() == 0)
        {
            return ll_branch;
        }
        ll_branch.add(this);
        MoveNode parent = ll_parent_nodes.getFirst();
        while (parent.ll_parent_nodes.size() != 0)
        {
            ll_branch.add(parent);
            parent = parent.ll_parent_nodes.getFirst();
        }
        return ll_branch;
    }


    /**
     *
     * @return {@link #move}
     */
    public Move get_move()
    {
        return move;
    }


    /**
     *
     * @return {@code weight of this move}
     */
    public float get_weight()
    {
        return weight;
    }


    /**
     *
     *
     * @param iteration
     *
     * @return {@code int} // maximum amount of children, this node should have
     */
    private static int max_children(int depth, int iteration)
    {
        //return (depth - iteration)/5 + 2;
        return 8;
        //return Integer.MAX_VALUE;
    }


    /**
     *  Enables the ability to give this MoveNode a weight
     *
     */
    private void m_set_weight(float weight)
    {
        this.weight = weight;
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
    public boolean m_set_children(Move move)
    {
        TreeSet<MoveNode>[] sets = new TreeSet[2];
        sets[0] = set_stacked;
        sets[1] = set_abandoned;
        for (TreeSet<MoveNode> set : sets)
        {
            for (Iterator<MoveNode> iterator = set.iterator(); iterator.hasNext();)
            {
                MoveNode child = iterator.next();
                /// TODO moving this into the if-statement
                ///      has significant consequences
                ///      towards stack-size on next create-tree execution
                iterator.remove();
                if (move.equals(child.get_move()))
                {
                    m_delete(null);
                    this.set_abandoned = child.set_abandoned;
                    this.set_stacked = child.set_stacked;
                    // TODO might be irrelevant
                    child.ll_parent_nodes.clear();
                    child.set_abandoned = null;
                    child.set_stacked = null;
                    // TODO might be irrelevant
                    for (MoveNode node : set_abandoned)
                    {
                        node.ll_parent_nodes.clear();
                        node.ll_parent_nodes.add(this);
                    }
                    for (MoveNode node : set_stacked)
                    {
                        node.ll_parent_nodes.clear();
                        node.ll_parent_nodes.add(this);
                    }
                    return true;
                }
            }
        }
        m_delete(null);
        this.set_abandoned.clear();
        this.set_stacked.clear();
        return false;
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
     * @return
     */
    public Optional<MoveNode> create_node(Board board, Calculator calculator, int iteration)
    {
        header.m_add_total_executions();
        this.is_final = set_final_state(board, calculator);
        this.iteration = iteration;
        Optional<MoveNode> older_cousin = is_dead(board);
        if (older_cousin.isPresent())
        {
            ///
            ///
            /// This has to occur, as when a node is cut off from the main tree,
            /// the iteration value of itself and it's children
            /// are no longer updated.
            ///
            /// If it is subsequently added back into the main tree,
            /// it's iteration value has to be adjusted
            ///
            older_cousin.get().m_adjust_iteration(this, this.iteration);
            ///
            ///
            /// This needs to be done to remind the entire tree of it's structure.
            /// Child nodes will regain knowledge of it's parents all the way up
            /// until this node to ensure the tree is properly connected.
            ///
            older_cousin.get().m_undo_delete();
            return older_cousin;
        }
        m_set_weight(calculator.evaluate(board));
        this.actual_weight = weight;
        return Optional.empty();
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
    private Optional<MoveNode> is_dead(Board board)
    {
        if (iteration < 3 || iteration % 2 != 1)
        {
            return Optional.empty();
        }
        long time_start = System.nanoTime();
        CacheNode cache = header.get_cache((iteration - 3)/2);
        MoveNode older_cousin = cache.m_add_value(board.get_history().get_as_vectors(header.get_move_count()).entrySet().iterator(), this);
        if (older_cousin == null)
        {
            long time_stop = System.nanoTime();
            header.time += (time_stop - time_start)/1000;
            return Optional.empty();
        }
        long time_stop = System.nanoTime();
        header.time += (time_stop - time_start)/1000;
        header.m_add_total_nodes_ended();
        return Optional.of(older_cousin);
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
    public void m_stack_additional_childnodes(Type type, Calculator calculator, MoveNode parent)
    {
        if (parent != null)
        {
            if (ll_parent_nodes.getFirst() != parent)
            {
                return;
            }
        }
        header.m_add_total_executions_saved();
        ///
        if (!is_final && set_abandoned.size() == 0 && set_stacked.size() == 0)
        {
           header.get_stack().add(this);
           return;
        }
        /// allows get_best_move_recursive to calculate
        this.best_move_calculated = false;
        /// adds additional child nodes, which were previously abandoned
        TreeSet<MoveNode> set_children = m_reunite_children(type);
        m_set_weight(this.actual_weight);
        ///
        ///
        ///  can continue
        ///  -> follows down the tree
        ///
        for (MoveNode child : set_stacked)
        {
            child.m_stack_additional_childnodes(Type.get_opposite(type), calculator, this);
        }
        header.get_stack().addAll(set_children);
        set_stacked.addAll(set_children);
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
    public void m_create_childnodes(Board board, Calculator calculator)
    {
        /// termination conditions
        if (is_final || is_depth(iteration))
        {
            return;
        }
        /// each pieces legal moves get added as a possible future node
        LinkedList<MoveNode> ll_movetree = new LinkedList<MoveNode>();
        for     (Piece piece : board.get_collection(board.get_type()).get_active_pieces())
        {
            for (Entry<Position, MoveType[]> entry : piece.get_legal_moves().entrySet())
            {
                ll_movetree.add(new MoveNode(new Move(piece.get_position(), entry.getKey(), entry.getValue()), this, header));
            }
        }
        ///
        ///
        /// for each possible move, calculate their weight, final state
        /// and compare with cached nodes.
        ///
        /// If the childnode already exists, the childnode will return the cached node with the same board position.
        /// This node will then add it to the stacked nodes, as it is not necessary to calculate further
        ///
        Iterator<MoveNode> iterator = ll_movetree.iterator();
        while (iterator.hasNext())
        {
            MoveNode child = iterator.next();
            board.m_commit(child.move);
            Optional<MoveNode> optional_child = child.create_node(board, calculator, iteration + 1);
            ///
            ///
            /// adds the nodes, which don't require further calculations
            /// into the stacked set, so they can be used for the get_best_move
            /// calculation
            ///
            if (optional_child.isPresent())
            {
                optional_child.get().ll_parent_nodes.add(this);
                set_stacked.add(optional_child.get());
                iterator.remove();
            }
            board.m_revert();
        }
        TreeSet<MoveNode> set_children = new TreeSet<MoveNode>();
        set_children.addAll(ll_movetree);
        ll_movetree.clear();
        ///
        ///
        /// This may not be necessary, as it would require illegal moves
        /// or a preset empty board?... However good to have for now
        ///
        if (set_children.isEmpty() && set_stacked.isEmpty())
        {
            is_final = true;
            return;
        }
        ///
        ///
        /// abandones the nodes, which currently don't have a
        /// positive evaluation, massively reducing calculations,
        /// however an inaccurate first evaluation leads to
        /// worse results in the long run.
        ///
        m_abandon_children(board, set_children);
        header.get_stack().addAll(set_children);
        set_stacked.addAll(set_children);
    }


    /**
     *
     *
     * @param board
     * @param iteration
     *
     */
    private TreeSet<MoveNode> m_reunite_children(Type type)
    {
        TreeSet<MoveNode> set_children = new TreeSet<MoveNode>();
        if (type == Type.WHITE)
        {
            while (max_children(header.get_depth(), iteration) >= set_children.size() + set_stacked.size() && !set_abandoned.isEmpty())
            {
                set_children.add(set_abandoned.pollLast());
            }
        }
        else
        {
            while (max_children(header.get_depth(), iteration) >= set_children.size() + set_stacked.size() && !set_abandoned.isEmpty())
            {
                set_children.add(set_abandoned.pollFirst());
            }
        }
        return set_children;
    }


    /**
     *
     * @param board
     * @param iteration
     *
     */
    private void m_abandon_children(Board board, TreeSet<MoveNode> set_children)
    {
        if (board.get_type() == Type.WHITE)
        {
            while (max_children(header.get_depth(), iteration) < set_children.size() + set_stacked.size() && set_children.size() != 0)
            {
                set_abandoned.add(set_children.pollFirst());
            }
        }
        else
        {
            while (max_children(header.get_depth(), iteration) < set_children.size() + set_stacked.size() && set_children.size() != 0)
            {
                set_abandoned.add(set_children.pollLast());
            }
        }
    }



    /**
     *
     *
     * @param type
     * @return
     */
    private void m_undo_delete()
    {
        for (MoveNode child : set_stacked)
        {
            child.ll_parent_nodes.add(this);
            child.m_undo_delete();
        }
        for (MoveNode child : set_abandoned)
        {
            child.ll_parent_nodes.add(this);
            child.m_undo_delete();
        }
    }

    /**
     *
     *
     * @param type
     *
     * @return
     */
    public Move get_best_move(Type type)
    {
        return get_best_move_recursive(type).get_move();
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
    private MoveNode get_best_move_recursive(Type type)
    {
        /// This node is a leaf
        if (set_stacked.isEmpty())
        {
            return this;
        }
        /// prevents this recursion tree from being calculated multiple times
        if (!best_move_calculated)
        {
            best_move_calculated = true;
            ///
            ///
            /// Separate array required, to enable resorting of the set
            /// TODO: Do we need to reorder the set or can we simply find min, max here?
            /// TODO: any way to not use ugly Objects here?
            ///
            Object[] arr_children = set_stacked.toArray();
            set_stacked.clear();
            ///
            ///
            /// Recursively find the best move
            /// Every iteration will use a different type
            ///
            for (Object object : arr_children)
            {
                if (object instanceof MoveNode)
                {
                    MoveNode move = (MoveNode)object;
                    move.get_best_move_recursive(Type.get_opposite(type));
                    set_stacked.add(move);
            } // object instance of Movenode
                else
                {
                    throw new IllegalArgumentException("Object is not a MoveNode.");
                }
            }
        }
        ///
        ///
        /// Choose the best move for either side
        /// ->  calculating your next best move requires anticipating
        ///     your oponent's best move, therefore this is not specific
        ///     to the type of the player
        ///
        MoveNode child;
        if (type == Type.WHITE)
        {
            child = set_stacked.last();
        }
        else
        {
            child = set_stacked.first();
        }
        ///
        ///
        /// This move now has the weight of it's own 'best' child
        /// This change is reverted later
        ///
        m_set_weight(child.get_weight());
        //header.m_add_children_to_average(set_children.size());
        /// return value is only used to hand the move over to the TreeHeader by the top_node
        return child;
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
    private boolean set_final_state(Board board, Calculator calculator)
    {
        if (board.get_state().equals(GameState.CHECKMATE))
        {
            return true;
        }
        if (board.get_state().equals(GameState.DRAW))
        {
            if (board.get_type() != Type.BLACK)
            {
                m_set_weight((float)(-1));
            }
            else
            {
                m_set_weight((float)(1));
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
        if (move.get_weight() > weight)
        {
            return -1;
        }
        /// move.get_weight() <= get_weight()
        return 1;
    }


}