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
            if (get_move(get_length() - i - 1).equals(get_move(get_length() - i - 5)))
            {
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
     *  This method returns a reduced version of the #{@link History}.
     *  All moves, from move {@code from} until the end will be
     *  represented using global vectors. This means, an entry of the map
     *  will define exactly, how a piece from position 1 moved to position 2
     * 
     *  <p> Important Note: Moves which have a movetype will modify the outcome.
     *      A piece which has been taken should not have a vector
     *      representing it's location on the board.
     *      For this case, we use negative values.
     * 
     * @param from
     * 
     * @return TreeMap<Integer, Integer> // each entry represents {@code<K:pos_from, V:pos_to>};
     *                                      pos_from, pos_to as Integers
     */
    public TreeMap<Integer, Integer> get_as_vectors(int from)
    {

        TreeMap<Integer, Integer> map_reduced = new TreeMap<Integer, Integer>();
        Iterator<Move> iterator = ll_moves.descendingIterator();
        
        int i = get_length();
        while (i != from)
        {
            i--;
            Move move = iterator.next();
            int hash_from = move.position_from().hashCode();
            int hash_to = move.position_to().hashCode();
            m_combine_vectors(map_reduced, hash_from, hash_to);
            
            for (MoveType type : move.get_types())
            {
                switch (type)
                {
                    case TAKES:
                        /// 
                        /// taken piece vector will end in -1,
                        /// representing it has been removed from the game
                        map_reduced.put(hash_to, 64);
                        break;

                    case PROMOTION:
                        /// 
                        /// pawn vector will end in -2
                        /// representing it has promoted
                        map_reduced.put(hash_from, 65);
                        break;
                    ///
                    /// current piece vector at it's from value will have a value
                    /// representing the promotion-type of the piece (inverted)
                    /// 
                    /// This method separates the vector of the pawn from the vector
                    /// of itself as a promoted piece
                    /// 
                    /// This ensures, boards with the same vectors are also
                    /// compared by the correct promotions
                    /// 
                    /// example: if the pieces were to be on the same position
                    /// at the end, they could have differing promotions (i.e. knight, bishop)
                    /// and be identified as the same board positions, even if they are not.
                    /// 
                    /// TODO: Problem: pieces, which originate from the same value, will be overwritten.
                    /// Here we calculate the x_value * 6 + ..., in order to make this possibility vanishingly
                    /// small. However, it will happen, so hopefully there is a better way.

                    case PROMOTION_QUEEN:
                        m_combine_vectors(map_reduced, (move.position_from().get_x() + 1) * 6 + 66, hash_to);
                        break;

                    case PROMOTION_ROOK:
                        m_combine_vectors(map_reduced, (move.position_from().get_x() + 1) * 6 + 67, hash_to);
                        break;

                    case PROMOTION_BISHOP:
                        m_combine_vectors(map_reduced, (move.position_from().get_x() + 1) * 6 + 68, hash_to);
                        break;

                    case PROMOTION_KNIGHT:
                        m_combine_vectors(map_reduced, (move.position_from().get_x() + 1) * 6 + 69, hash_to);
                        break;
                                       
                    /// This encapsulates a normal move
                    /// No further information is required in this vector
                    default:
                        break;
                }
            }
        }
        return map_reduced;
    }

    /**
     *  Combines entries about multiple vectors together.
     *  This enables the reduction of specific move vectors
     *  to their global move-vectors
     * 
     *  <p> This method is only used for the get_as_vectors method
     * 
     *  <p> Important Note: This method only works, if the vectors are added
     *      in reverse order.
     * 
     * @param map_reduced // map with the entries, representing vectors
     * @param hash_from
     * @param hash_to
     * 
     */
    private void m_combine_vectors(TreeMap<Integer, Integer> map_reduced, int hash_from, int hash_to)
    {
        Integer removed = map_reduced.remove(hash_to);
            if (removed != null)
            {
                map_reduced.put(hash_from, removed);
            }
            else
            {
                map_reduced.put(hash_from, hash_to);
            }
        }

    @Override
    public String toString()
    {
        String string = "";
        for (Move move : ll_moves)
        {
            string = string + move.toString() + ",\n";
        }
        return string;
    }

}
