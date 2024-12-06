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
    public MoveNode m_add_value(Iterator<Entry<String, Integer>> values, MoveNode move)
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
        Entry<String, Integer> entry = values.next();
        CacheNode new_node = new CacheNode();
        CacheNode node = map_nodes.putIfAbsent((entry.getKey() + entry.getValue()).hashCode(), new_node);
        if (node == null)
        {
            return new_node.m_add_value(values, move);
        }
        return node.m_add_value(values, move);
    }


    /**
     *  Calls all childnodes to recursively delete the tree.
     *  This nodes' {@link #map_nodes} will still be initialized
     * 
     * 
     */
    public void m_delete()
    {
        for (CacheNode node : map_nodes.values())
        {
            node.m_recursive_delete();
        }
        map_nodes.clear();
    }


    /**
     *  recursively delete all childnodes, followed by itself.
     * 
     * 
     */
    private void m_recursive_delete()
    {
        for (CacheNode node : map_nodes.values())
        {
            node.m_delete();
        }
        map_nodes.clear();
        move = null;
        map_nodes = null;
 
    }


}
