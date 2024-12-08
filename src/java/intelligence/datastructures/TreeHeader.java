package src.java.intelligence.datastructures;


import java.util.TreeMap;

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
    private final int original_depth;
    private int depth;
    private int moves;
    private Type type;
    private CacheNode[] cache;

    // telemetry
    private int total_executions;
    private int total_executions_saved;
    public long total_request_time;
    public long total_requests;
    public long time;
    private long time_total;
    private long time_compare;
    private int nodes_ended;


    public TreeHeader(int depth, Board board, Type type)
    {
        moves = 0;
        
        int cache_size = depth/2; // ceil(depth/2) - 1 // ceil simplified to + 1
        if (cache_size < 0)
        {
            cache_size = 0;
        }
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
        top_node = new MoveNode(this);
        this.depth = depth;
        this.original_depth = depth;
        this.board = board;
        board.m_initialise();

    }


    public void m_rm_cache() {
        for (CacheNode cache_node : cache)
        {
            cache_node.m_delete(); 
        }
    }


    public int get_move_count()
    {
        return moves;
    }


    public MoveNode m_add_history_to_cache(MoveNode new_node, TreeMap<Integer, Integer> map_vectors,  int iteration)
    {

        long time = System.nanoTime();
        MoveNode move = cache[iteration].m_add_value(map_vectors.entrySet().iterator(), new_node);
        
        time_compare += System.nanoTime() - time;
        return move;
    }


    public int get_depth()
    {
        return this.depth;
    }


    public Board get_board()
    {
        return board;
    }


    public float get_weight()
    {
        return this.weight;
    }
    

    public void create_tree(Calculator calculator, int depth)
    {
        this.time_total = System.nanoTime();
        this.weight = calculator.evaluate(board);

        this.type = board.get_type();
        top_node.m_create_tree(board, calculator, 0);
        this.time_total = System.nanoTime() - time_total;
        this.total_request_time += time_total;
        this.total_requests++;
        m_rm_cache();
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
       Move move = top_node.get_best_move_recursive(type);
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
        top_node.m_set_children(move, board);
        int piececount = 0;
        for (Type type : Type.values())
        {
            piececount += board.get_collection(type).get_active_pieces().size();
        }
        ///
        /// increases depth, when the overall piececount decreased significantly enough
        /// This hopefully allows the bot to find easy queen-king checkmates
        /// This could be based off of possible moves per piece in future
        /// 
        for (MoveType move_type : move.get_types())
        {
            if (move_type.equals(MoveType.TAKES))
            {
                this.depth = original_depth + 2 *(int)(original_depth * ((float)32/piececount - 1)/32);
                int cache_size = depth/2;
                if (cache_size < 0)
                {
                    cache_size = 0;
                }
                cache = new CacheNode[cache_size];
                for (int i = 0; i < cache_size; i++)
                {
                    cache[i] = new CacheNode();
                }
                System.out.println("depth is: "+depth);
            }
        }
   }


}
