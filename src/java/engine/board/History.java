package src.java.engine.board;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import src.java.engine.board.Move.MoveType;
import src.java.engine.board.piecelib.Piece.PieceType;

public class History
{

    private LinkedList<Move> ll_moves;
    private LinkedList<Integer> ll_fifty_move_rule;
    private int fifty_move_rule;


    public History()
    {
        ll_moves = new LinkedList<Move>();
        ll_fifty_move_rule = new LinkedList<Integer>();
        fifty_move_rule = 0;
    }

    public Move get_move(int i)
    {
        return ll_moves.get(i);
    }

    public int get_length()
    {
        return ll_moves.size();
    }

    public void m_register_move(Move move)
    {
        ll_moves.add(move);
        /// checks, if this move is an improvement for the fifty-move rule
        if (move.position_from().get_piece().get_piece_type() == PieceType.PAWN)
        {
            ll_fifty_move_rule.add(fifty_move_rule);
            fifty_move_rule = 0;
            return;
        }
        for (MoveType move_type : move.get_types())
        {
            if (move_type == MoveType.TAKES)
            {
                ll_fifty_move_rule.add(fifty_move_rule);
                fifty_move_rule = 0;
                return;
            }
        }
        fifty_move_rule++;
    }


    /**
     *  checks, if the current board is a draw, due to either:
     *  <p> - repetition (both players played the same move twice)
     *  <p> - {@link #fifty_move_rule} (both players had no improvement the past 50 moves)
     * 
     * @return {@code boolean} 
     */
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


    /**
     *  Used to reverse the previous move.
     *  The previous move will be removed from the {@link #ll_moves}
     * 
     *  <p> {@link #fifty_move_rule} will be updated
     * 
     */
    public void m_reverse()
    {
        Move move = ll_moves.removeLast();
        move.m_reverse();

        if (fifty_move_rule == 0)
        {
            fifty_move_rule = ll_fifty_move_rule.removeLast();
        }
        else
        {
            fifty_move_rule--;
        }
        
    }

    /**
     * REQUIRES REWORK
     * 
     * @param from
     * @return
     */
    public TreeMap<Integer, Integer> get_as_vectors(int from)
    {

        TreeMap<Integer, Integer> map_reduced = new TreeMap<Integer, Integer>();
        Iterator<Move> iterator = ll_moves.descendingIterator();
        
        int i = get_length();
        while (i != from)
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

    @Override
    public String toString() {
        String string = "";
        for (Move move : ll_moves) {
            string = string + move.toString() + ",\n";
        }
        return string;
    }

}
