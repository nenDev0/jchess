package src.java.intelligence.datastructures;


import java.util.Iterator;
import java.util.TreeSet;

import src.java.engine.board.Board;
import src.java.engine.board.Move;
import src.java.engine.board.Move.MoveType;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.evaluation.Calculator;


public class TreeHeader
{

    private MoveNode top_node;
    private Board board;
    private float weight;
    private final int final_depth;
    private int depth;
    private int moves;
    private Type type;
    private CacheNode[] cache;
    private TreeSet<MoveNode> set_stack;
    private Thread[] arr_threads;
    private Calculator calculator;

    // telemetry
    private int total_executions;
    private int total_executions_saved;
    public long total_request_time;
    public long total_requests;
    public long time;
    private long time_total;
    private long time_compare;
    private int nodes_ended;


    public TreeHeader(int depth, Board board, Calculator calculator, Type type)
    {
        moves = 0;
        this.board = board;
        this.calculator = calculator;
        board.m_initialise();

        /// ceil(depth/2) - 1 // ceil simplified to + 1
        int cache_size = depth/2;
        cache = new CacheNode[cache_size];
        for (int i = 0; i < cache_size; i++)
        {
            cache[i] = new CacheNode();
        }
        total_executions = 0;
        total_executions_saved = 0;
        time = 0;
        total_request_time = 0;
        total_requests = 0;
        time_compare = 0;
        time_total = 0;
        nodes_ended = 0;
        top_node = new MoveNode(null, null, this);
        top_node.create_node(board, calculator, cache_size);
        this.depth = depth;
        this.final_depth = depth;
        this.set_stack = new TreeSet<MoveNode>();
        this.type = type;
    }


    public int get_move_count()
    {
        return moves;
    }


    public TreeSet<MoveNode> get_stack()
    {
        return set_stack;
    }


    public CacheNode get_cache(int iteration)
    {
        return cache[iteration];
    }


    public int get_depth()
    {
        return this.depth;
    }


    public float get_weight()
    {
        return this.weight;
    }


    public void create_tree(Calculator calculator, int depth)
    {
        this.time_total = System.nanoTime();
        this.weight = calculator.evaluate(board);
        top_node.m_adjust_iteration(null, 0);
        top_node.m_stack_additional_childnodes(type, calculator, null);
        System.out.println("stacksize:" + set_stack.size());
        Board clone = board.clone();
        while (set_stack.size() != 0)
        {
            MoveNode node = set_stack.pollFirst();
            int i = 0;
            Iterator<MoveNode> parent_nodes = node.get_branch().descendingIterator();
            ///
            /// node.get_iteration() == node.get_branch().size()
            if (node.get_iteration() != node.get_branch().size())
            {
                throw new IllegalStateException("ah");
            }
            try
            {
                while(parent_nodes.hasNext())
                {
                    board.m_commit(parent_nodes.next().get_move());
                    i++;
                }
            }
            catch (Exception e)
            {
                for (MoveNode move : node.get_branch())
                {
                    System.out.println(move.get_move());
                }
                System.out.println("actual board:");
                System.out.println(board);
                System.out.println("index: "+ i);
                throw e;
            }
            node.m_create_childnodes(board, calculator);
            for(int j = i; j > 0; j--)
            {
                board.m_revert();
            }
            Object o = clone.continuity_check(board);
            if (o != null)
            {
                System.out.println(clone);
                System.out.println(board);
                System.out.println(o);
                throw new IllegalArgumentException("continuity check failure");
            }
        }
        /*this.arr_threads = new Thread[32];
        for (int i = 0; i < arr_threads.length; i++)
        {
            arr_threads[i] = new Thread(new TreeRunner(board, calculator, this));
            arr_threads[i].run();
        }*/
        this.time_total = System.nanoTime() - time_total;
        this.total_request_time += time_total;
        this.total_requests++;
    }


    public void m_add_total_executions_saved()
    {
        total_executions_saved++;
    }


    public void m_add_total_executions()
    {
        total_executions++;
    }


    public void m_add_total_nodes_ended()
    {
        nodes_ended++;
    }


    public Move get_best_move()
    {
        System.out.println("NODES >>> [total|saved|ended]");
        System.out.println("NODES >>> [" + total_executions + "|" + total_executions_saved + "|" + nodes_ended + "]");
        Move move = top_node.get_best_move(type);
        System.out.println("move_weight: "+top_node.get_weight() + ", type: " + type);
        System.out.println("Total time: " + time_total/1000);
        System.out.println("Average time per node: " + (float)(time_total/1000) / (float)(total_executions));
        System.out.println("Average time per node ended: " + (float)(time) / (float)(nodes_ended));
        System.out.println("Average time per comparison: " + (float)(time_compare/1000) / (float)(total_executions));
        System.out.println("Average time per request: " + (float)total_request_time/1000000/total_requests + "ms");
        //System.out.println("Move's weight: " + move.get_weight());
        //System.out.println("<average: "+get_average()+" >");
        //System.out.println("<final: " + move.get_weight() + " >");
        total_executions = 0;
        total_executions_saved = 0;
        nodes_ended = 0;
        time = 0;
        time_total = 0;
        return move;
    }


    /**
     *  {@code if (the tree has this move as a child)}
     *      sets this tree to the subtree of that child,
     *  {@code else}
     *      clears the tree
     *
     *
     *  <p> Note: this method also dictates the future depth of the tree
     *      If the piececount has fallen low enough, the depth of the tree
     *      will increase
     *
     * @param move
     *
     */
    public void m_adjust(Move move)
    {
        moves++;
        move = move.convert(board);
        board.m_commit(move);
        // TODO is this necessary?!?!
        set_stack.clear();
        // TODO This seems to be the issue -> running contents of if-statement every time runs fine
        top_node.m_set_children(move);
        /*if (!top_node.m_set_children(move))
        {*/
            //set_stack.clear();
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
            top_node = new MoveNode(null, null, this);
            if (top_node.create_node(board, calculator, 0).isPresent())
            {
                throw new IllegalStateException("We got a problem... Top Node not initialized correctly");
            }
        /*}*/
        ///
        /// increases depth, when the overall piececount decreased significantly enough
        /// This hopefully allows the bot to find easy queen-king checkmates
        /// This could be based off of total possible moves in future
        ///
        for (MoveType move_type : move.get_types())
        {
            if (move_type.equals(MoveType.TAKES) ||
                move_type.equals(MoveType.EN_PASSANT_LEFT) ||
                move_type.equals(MoveType.EN_PASSANT_RIGHT))
            {
                int piececount = 0;
                for (Type type : Type.values())
                {
                    piececount += board.get_collection(type).get_active_pieces().size();
                }
                this.depth = final_depth + 2 * (int)(final_depth * ((float)32/piececount - 1)/24);
                //System.out.println("depth is: "+depth);
            }
        }
        ///
        /// The cache only needs to be adjusted every 2 moves, as this bot will only
        /// have a move requested every second move.
        ///
        if (type == Type.WHITE && moves % 2 != 0 ||
            type == Type.BLACK && moves % 2 != 1)
        {
            return;
        }
        int new_cache_size = depth/2;
        System.out.println("Cache carryover - Type:" + type);
        ///
        /// Carry cached nodes over to still be usable after tree adjustion
        /// This is necessary if and only if the tree grows every iteration.
        ///
        /// only needs to be adjusted every 2 moves, as this bot will only
        /// have a move requested every second move.
        ///
        /// Note, this is even useful, when the tree does not contain the move mentioned,
        /// as future child nodes could still already exist in the cache.
        ///
        CacheNode[] carried_nodes = cache;
        this.cache = new CacheNode[new_cache_size];
        int filled = 0;
        for (int i = 0; i < carried_nodes.length - 2 && i < new_cache_size; i++)
        {
            filled++;
            cache[i] = carried_nodes[i + 2];
        }
        ///
        /// cache array size needs to be adjustable, as the depth is variable.
        /// Any additionally opened cache nodes initialized.
        ///
        for (int i = filled; i < new_cache_size; i++)
        {
            cache[i] = new CacheNode();
        }
   }


}
