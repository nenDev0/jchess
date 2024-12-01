package src.java.engine.board;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

public class History {

    private LinkedList<Move> ll_moves;
    private LinkedList<Integer> ll_fifty_move_rule;
    private int fifty_move_rule;

    public History() {
        ll_moves = new LinkedList<Move>();
        ll_fifty_move_rule = new LinkedList<Integer>();
        fifty_move_rule = 0;
    }

    public Move get_move(int i) {
        return ll_moves.get(i);
    }

    public int get_length() {
        return ll_moves.size();
    }

    public void m_register_move(Position position1, Position position2) {
        m_register_move(new Move(position1, position2));
    }

    public void m_register_move(Move move) {
        ll_moves.add(move);
        if (move.is_improvement())
        {
            ll_fifty_move_rule.add(fifty_move_rule);
            fifty_move_rule = 0;
        }
        else
        {
            fifty_move_rule++;
        }
    }

    public boolean is_draw_by_repetition()
    {
        if (get_length() < 5)
        {
            return false;
        }
        // TODO: reminder to set fifty-move-rule back to 50
        if (fifty_move_rule == 20)
        {
            return true;
        }
        for (int breaking_point = 0, i = 0; breaking_point < 4 && i + 4 < get_length() ; i++, breaking_point++)
        {
            if (get_move(get_length() - i - 1).equals(get_move(get_length() - i - 5))) {
                continue;
            }
            return false;
        }
        return true;
    }


    public void m_reverse() {
        Move move = ll_moves.removeLast();
        move.m_reverse();

        if (move.is_improvement())
        {
            fifty_move_rule = ll_fifty_move_rule.removeLast();
        }
        else
        {
            fifty_move_rule--;
        }
        
    }

    public TreeMap<Integer, Integer> get_as_vectors(int from)
    {

        TreeMap<Integer, Integer> map_reduced = new TreeMap<Integer, Integer>();
        Iterator<Move> iterator = ll_moves.descendingIterator();
        
        int i = get_length() - 1;
        while (i != from - 1)
        {
            Move move = iterator.next();
            int hash_from = move.position_from().hashCode();
            int hash_to = move.position_to().hashCode();
            Integer removed = map_reduced.remove(hash_to);
            if (removed != null)
            {
                map_reduced.put(hash_from, removed);
            }
            else
            {
                map_reduced.put(hash_from, hash_to);
            }
            i--;
        }
        return map_reduced;
    }

    public TreeMap<Integer, Integer> red(int from)
    {
        TreeMap<Integer, Integer> ll_moves_red = new TreeMap<Integer, Integer>();

        for (Move move: ll_moves)
        {
            if (from > 0)
            {
                from--;
                continue;
            }
            int hash_from = move.position_from().hashCode();
            int hash_to = move.position_to().hashCode();
            boolean flag = false; 
            for (Entry<Integer, Integer> entry : ll_moves_red.entrySet())
            {
                if (entry.getValue() == hash_from)
                {
                    entry.setValue(hash_to);
                    flag = true;
                    break;
                }
            }
            if (!flag)
            {
                ll_moves_red.put(hash_from, hash_to);
            }
        }
        return ll_moves_red;
    }
}
