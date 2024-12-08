package src.java.intelligence.datastructures;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;


/**
 *  This class represents a tree.
 *  It is used to cache {@link #move}_nodes and access them efficiently.
 * 
 *  <p> each node represents a vector(move), which is crushed down into an int, used as a Key
 *  <p> nodes are not required to contain MoveNodes,
 *      as they are only allocated, if they were handed into that node specifically
 * 
 *  <p> GC will have a field-day here, however the datastructure is kept as minimalistic as possible.
 * 
 */
public class CacheNode {
    
    private MoveNode move;
    private TreeMap<Integer, CacheNode> map_nodes; /// connects a new cache_node with a move-vector


    /**
     *  Constructor
     *  
     */
    public CacheNode()
    {
        this.map_nodes = new TreeMap<Integer, CacheNode>();
        this.move = null;
    }


    /**
     *  Used to find already existing MoveNodes existent within the tree.
     *  In case there is none, this MoveNode will be saved.
     * 
     * @param values // Iterator of Entries (K: int, V: int) representing vectors
     * @param move // MoveNode to set {@link #move} to
     * 
     * @return  // {@code MoveNode}, if there was already a MoveNode at the specified location
     *          // {@code null}, else
     */
    public MoveNode m_add_value(Iterator<Entry<Integer, Integer>> values, MoveNode move)
    {
        if (!values.hasNext())
        {
            if (this.move != null)
            {
                return this.move;
            }
            this.move = move;
            return null;
        }
        Entry<Integer, Integer> entry = values.next();
        CacheNode new_node = new CacheNode();
        ///
        /// Position_from values (Keys) need to be rotated by 10 bits. The final format is the following:
        /// ###.    ###.    #.      ###.        ###.    ###
        /// x_from. y_from. passant.movetypes.  x_to.   y_to
        /// 0-7     0-7     0-1     0-5         0-7     0-7
        CacheNode node = map_nodes.putIfAbsent((entry.getKey() << 10) + entry.getValue(), new_node);
        if (node == null)
        {
            return new_node.m_add_value(values, move);
        }
        return node.m_add_value(values, move);
    }


    /**
     *  Relying on GC to clear all initialized CacheNodes
     * 
     * 
     */
    public void m_delete()
    {
        map_nodes.clear();
    }

}
