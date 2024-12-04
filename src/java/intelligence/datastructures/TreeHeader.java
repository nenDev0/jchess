package src.java.intelligence.datastructures;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;

import src.java.engine.board.Board;
import src.java.engine.board.Move;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.evaluation.Calculator;

import java.util.Set;

public class TreeHeader
{
    
    private MoveNode top_node;
    private Board board;
    private float weight;
    private LinkedList<MoveNode>[] history_cache;
    private int depth;
    private int moves;
    private Type type;

    // telemetry
    private int total_executions;
    private int total_executions_saved;
    public long time;
    private long time_total;
    private long time_compare;
    private int nodes_ended;


    // TODO: figure out the fix for this outcry 
    @SuppressWarnings("unchecked")
    public TreeHeader(int depth, Board board, Type type)
    {
        moves = 0;
        
        int cache_size = depth/2; // ceil(depth/2) - 1 // ceil simplified to + 1
        if (cache_size < 0)
        {
            cache_size = 0;
        }
        history_cache = new LinkedList[cache_size];
        for (int i = 0 ; i < cache_size; i++)
        {
            history_cache[i] = new LinkedList<MoveNode>();
        }
        
        
        total_executions = 0;
        total_executions_saved = 0;
        time = 0;
        time_compare = 0;
        time_total = 0;
        nodes_ended = 0;
        top_node = new MoveNode(this);
        this.depth = depth;
        this.board = board;
        board.m_initialise();

        m_clear();

    }


    public int get_move_count()
    {
        return moves;
    }


    public void m_rm_cache()
    {
        for (int i = 0; i < history_cache.length; i++)
        {
            history_cache[i].clear();
        }
    }


    public MoveNode m_add_history_to_cache(MoveNode new_node, int iteration)
    {

        long time = System.nanoTime();
        TreeMap<Integer, Integer> new_history = new_node.get_history_vectors();
        for (MoveNode cached_node : history_cache[iteration]/*.get(new_history.size())*/)
        {
            if (compare(cached_node.get_history_vectors(), new_history))
            {
                time_compare += System.nanoTime() - time;
                return cached_node;
            }
        }
        history_cache[iteration].add(new_node);
        time_compare += System.nanoTime() - time;
        return null;
    }


    private boolean compare(TreeMap<Integer, Integer> map_moves_1, TreeMap<Integer, Integer> map_moves_2)
    {
        Set<Entry<Integer, Integer>> map_set_2 = map_moves_2.entrySet();
        Iterator<Entry<Integer, Integer>> iterator = map_set_2.iterator();
        for (Entry<Integer, Integer> entry : map_moves_1.entrySet())
        {
            /*
             * iterator has ran out, before foreach loop has
             */
            if (!iterator.hasNext())
            {
                return false;
            }
            if (!entry.equals(iterator.next()))
            {
                return false;
            }
        }
        return true;
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
    

    public void m_clear()
    {
        top_node.m_delete();
        m_rm_cache();
    }


    public void create_tree(Calculator calculator, int depth)
    {
        this.time_total = System.nanoTime();
        this.weight = calculator.evaluate(board);

        //System.out.println("eval before execution: " +this.weight);
        this.type = board.get_type();
        top_node.m_create_tree(board, calculator, 0);
        this.time_total = System.nanoTime() - time_total;
        for (LinkedList<MoveNode> linkedList : history_cache) {
            linkedList.clear();
        }
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

    public Type get_opposite(Type type)
    {
        if (type == Type.WHITE)
        {
            return Type.BLACK;
        }
        else {
            return Type.WHITE;
        }
    }


    public void m_adjust(Move move)
    {
        moves++;
        move = move.convert(board);
        board.m_commit(move);
        top_node.m_set_children(move, board);
        /// 
        /// To reduce unnecessary move, this will only be done every 2 moves,
        /// which requires extra thought in the following code
        /// TODO: this can be replaced by a boolean
        if (type == Type.WHITE && moves % 2 != 0 || type == Type.BLACK && moves % 2 != 1)
        {
            return;
        }
        System.out.println("adjusting history cache for :"+type);
        for (LinkedList<MoveNode> linkedList : history_cache) {
            System.out.println(linkedList.size());
        }
        ///
        /// The size of the cache is different, depending on the depth of the calculations
        /// This deals with the edge cases
        /*switch (history_cache.length) {
            case 0:
                break;
            case 1:
                history_cache[0].clear();
                break;
            default:
                ///
                /// reduced GC usage by reusing the initialized lists
                LinkedList<MoveNode> ll_first  = history_cache[0];
                ll_first.clear();
                for (int i = 0; i < history_cache.length - 1 ; i++)
                {
                    history_cache[i] = history_cache[i + 1];
                }
                history_cache[history_cache.length - 1] = ll_first; 
                break;
        }*/
   }


}
