package src.java.intelligence;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Map.Entry;

import src.java.engine.board.Board;
import src.java.engine.board.Move;
import src.java.engine.board.Type;
import src.java.intelligence.evaluation.Calculator;

import java.util.Set;

public class TreeHeader
{
    
    private MoveNode top_node;
    private Board board;
    private float weight;
    private LinkedList<MoveNode>[] history_cache;

    // telemetry
    private float total;
    private int values_added;
    private int total_executions;
    private int total_executions_saved;
    private Type type;
    private int depth;
    private int moves;
    public long time;
    private int nodes_ended;


    public TreeHeader(int depth, Type type, Board board)
    {
        moves = 0;
        
        int cache_size = (depth-4)/2 + 1;
        if (cache_size < 0)
        {
            cache_size = 0;
        }
        // TODO: figure this out
        history_cache = new LinkedList[cache_size];
        m_rm_cache();
        
        total_executions = 0;
        total_executions_saved = 0;
        time = 0;
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
            history_cache[i] = new LinkedList<MoveNode>();
        }
    }

    public MoveNode m_add_history_to_cache(MoveNode new_node, int iteration)
    {
        TreeMap<Integer, Integer> new_history = new_node.get_history_vectors();
        for (MoveNode cached_node : history_cache[iteration]/*.get(new_history.size())*/)
        {
            if (compare(cached_node.get_history_vectors(), new_history))
            {
                //m_add_total_executions_saved();
                return cached_node;
            }
        }
        history_cache[iteration].add(new_node);
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
    

    public void m_reset_average()
    {
        total = 0;
        values_added = 0;
    }

    public void m_clear()
    {
        total = 0;
        values_added = 0;
        top_node.m_delete();
        m_rm_cache();
    }


    public void create_Tree(Calculator calculator, int depth)
    {
        this.weight = calculator.evaluate(board);

        //System.out.println("eval before execution: " +this.weight);
        this.type = board.get_type();
        top_node.m_create_tree(board, calculator, 0);
    }

    
    public void m_add_total_executions_saved()
    {
        total_executions_saved++;
        int shown_executions = 100000;
        if (total_executions_saved % shown_executions == 0)
        {
            //System.out.println("current executions saved: "+ total_executions_saved / shown_executions + "  *  " + shown_executions);
        }
    }

    public void m_add_total_executions()
    {
        total_executions++;
        int shown_executions = 100000;
        if (total_executions % shown_executions == 0)
        {
            //System.out.println("current executions: "+ total_executions / shown_executions + "  *  " + shown_executions);
        }
    }

    public void m_add_total_nodes_ended()
    {
        nodes_ended++;
        int shown_executions = 10000;
        if (nodes_ended % shown_executions == 0)
        {
            //System.out.println("current nodes ended: "+ nodes_ended / shown_executions + "  *  " + shown_executions);
        }
    }

    public Type get_type()
    {
        return this.type;
    }

    public Move get_best_move()
    {
        //System.out.println("TOTAL EXECUTIONS: "+ total_executions);
        //System.out.println("TOTAL EXECUTIONS_SAVED: "+ total_executions_saved);
        //System.out.println("TOTAL NODES ENDED  "+ nodes_ended);
        //System.out.println("NODES >>> [" + total_executions + "|" + total_executions_saved + "|" + nodes_ended + "]");
        Move move = top_node.get_best_move_recursive(type);
        //System.out.println("Average time per node: " + (float)(time) / (float)(nodes_ended));
        //System.out.println("Move's weight: " + move.get_weight());
        /*System.out.println("<average: "+get_average()+" >");
        System.out.println("<final: " + move.get_weight() + " >");
        total_executions = 0;
        total_executions_saved = 0;
        nodes_ended = 0;
        time = 0;*/
        return move;
    }

    public float get_average()
    {
        return total / values_added;
    }

    public void m_add_value_to_average(float value)
    {
        total += value;
        values_added++;
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

        if (moves % 2 != 0)
        {
            return;
        }
        for (int i = 0; i < history_cache.length - 1 ; i++)
        {
            history_cache[i] = history_cache[i + 1];
        }
        history_cache[history_cache.length - 1] = new LinkedList<MoveNode>();
    }
}
