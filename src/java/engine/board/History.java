package src.java.engine.board;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import src.java.engine.board.Move.MoveType;
import src.java.engine.board.piecelib.Piece.PieceType;


/**
 *  Handles the LinkedList {@link #ll_moves}, which holds all {@code Move's} played on
 *  the {@code Board}, which contains this {@code History}
 *  
 *  <p> In addition it handles the {@link #fifty_move_rule}, which results in a draw, unrelated to the actual
 *      state of the board
 * 
 */
public class History
{


    private LinkedList<Move> ll_moves;
    /// necessary to keep consistency on reversion calls
    private LinkedList<Integer> ll_fifty_move_rule; 
    private int fifty_move_rule;


    /**
     *  Constructor
     *  
     */
    public History()
    {
        ll_moves = new LinkedList<Move>();
        ll_fifty_move_rule = new LinkedList<Integer>();
        fifty_move_rule = 0;
    }


    /**
     *  returns the move at index {@code i}
     * 
     * @throws NullPointerException {@code if (i >= History.get_length())}
     * 
     * @param i
     * 
     * @return
     */
    public Move get_move(int i)
    {
        return ll_moves.get(i);
    }


    /**
     *  Returns amount of moves played.
     * 
     * 
     * @return int {@link #ll_moves}.size()
     */
    public int get_length()
    {
        return ll_moves.size();
    }


    /**
     * 
     * 
     * @throws NullPointerException {@code if (move.position_from().get_piece() == null)}
     * 
     * @param move
     * 
     */
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
        /// 
        /// backwards consistency fifty move rule reversal
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
     * <p> {@code <K: position_from(int), V: position_to(int)>}
     * 
     *  <p> Important Note: Moves which have a movetype will modify the outcome.
     *      A piece which has been taken should not have a vector
     *      representing it's location on the board.
     *      For this case, we use additional bits leading the position_to value. 
     * 
     *  <p> The following values are always rotated by 6 bits left:
     *      (In the code, trailing zeros are partially reduced for readability)
     *   
     *  <p> {@code (0b00001 (1))} -> no longer on the board (position 64)
     *  <p> {@code (0b00010 (2))} -> promotion to queen
     *  <p> {@code (0b00100 (3))} -> promotion to rook 
     *  <p> {@code (0b00110 (4))} -> promotion to bishop 
     *  <p> {@code (0b01000 (5))} -> promotion to knight 
     *  <p> {@code (0b10000 (6))} -> last move is a pawn double forward
     *      
     *  <p> As some of these can happen at the same time, or leak into
     *      surrounding bits, this seems to be the minimal amount of bits.
     * 
     *      There for sure are some steps one could take to reduce it by a single bit,
     *      however it will most likely not get much better than that
     * 
     * 
     * @param from // at which move this algorithm will start reducing
     * 
     * @return TreeMap<Integer, Integer> // each entry represents {@code<K:pos_from, V:pos_to>};
     *                                      pos_from, pos_to as Integers
     *                                      note: pos_to has additional leading bits for the
     *                                      described usecases 
     */
    public TreeMap<Integer, Integer> get_as_vectors(int from)
    {
        TreeMap<Integer, Integer> map_reduced = new TreeMap<Integer, Integer>();
        /// 
        /// Note: the iterator is descending for two reasons:
        /// 
        ///     1. Descending is slightly faster, as it does not need to iterate
        ///     over moves, which don't need to be considered.
        /// 
        ///     2. It allows us to work with the keys of the TreeMap, when we write
        ///     the vectors as {key: from => value: to}
        /// 
        /// However this method comes with one massive Con:
        /// 
        ///     The maps can currently not be cached, unless the algorithm receives
        ///     multiple changes.
        /// 
        Iterator<Move> iterator = ll_moves.descendingIterator();
        int i = get_length();
        while (i > from)
        {
            Move move = iterator.next();
            int hash_from = move.position_from().hashCode();
            int hash_to;
            /// 
            /// When a pawn enables en-passant on the last move
            /// on one board, another board may be recognized as being the same,
            /// although en-passant is not possible.
            /// Adding an additional bit, in case en-passant is possible,
            /// prevents this.
            /// 
            if (i == get_length() &&
                move.position_to().get_piece().get_piece_type() == PieceType.PAWN &&
                Math.abs(move.position_from().get_y() - move.position_to().get_y()) == 2)
            {
                int last_move_bit_en_passant = (1 << 10);
                hash_to = move.position_to().hashCode() + last_move_bit_en_passant;
                m_combine_vectors(map_reduced, hash_from, hash_to);
                hash_to += -last_move_bit_en_passant;
            }
            else
            {
                hash_to = move.position_to().hashCode();
                m_combine_vectors(map_reduced, hash_from, hash_to);
            }
            i--;
            /// required for case CASTLING.
            int y;
            for (MoveType type : move.get_types())
            {
                switch (type)
                {
                    case TAKES:
                        /// 
                        /// taken piece vector will end in 64,
                        /// representing it has been removed from the game
                        /// 
                        map_reduced.put(hash_to, 1 << 6);
                        break;
                    /// 
                    /// vectors will gain additional bits on the position_to value
                    /// in order to represent the promotion-type of the piece
                    /// 
                    /// This ensures, boards with the same vectors are also
                    /// compared by the correct promotions
                    /// 
                    /// example: if the pieces were to be on the same position
                    /// at the end, they could have differing promotions (i.e. knight, bishop)
                    /// and be identified as the same board positions, even if they are not.
                    /// 
                    case PROMOTION_QUEEN:
                        map_reduced.put(hash_from, map_reduced.get(hash_from) + (1 << 7));
                        break;
                    /// 
                    case PROMOTION_ROOK:
                        map_reduced.put(hash_from, map_reduced.get(hash_from) + (2 << 7));
                        break;
                    /// 
                    case PROMOTION_BISHOP:
                        map_reduced.put(hash_from, map_reduced.get(hash_from) + (3 << 7));
                        break;
                    /// 
                    case PROMOTION_KNIGHT:
                        map_reduced.put(hash_from, map_reduced.get(hash_from) + (4 << 7));
                        break;
                    /// 
                    /// add the vector for the rook.
                    /// 
                    /// These vectors are easily calculated and
                    /// don't require any additional information
                    case CASTLING_KINGSIDE:
                        y = move.position_from().get_y();
                        m_combine_vectors(map_reduced,(7 << 3) + y, (5 << 3) + y);
                        break;
                    /// 
                    case CASTLING_QUEENSIDE:
                        y = move.position_from().get_y();
                        m_combine_vectors(map_reduced, y, (3 << 3) + y);
                        break;
                    /// 
                    /// sets the vector for the piece taken
                    /// 
                    /// These vectors are easily calculated and
                    /// don't require any additional information
                    case EN_PASSANT_LEFT: case EN_PASSANT_RIGHT:
                        m_combine_vectors(map_reduced,
                                         (move.position_to().get_x() << 3) + move.position_from().get_y(),
                                         (1 << 6));
                        break;
                    /// 
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
     * @throws NullPointerException {@code if (map_reduced == null)}
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
                ///
                /// Note that if a value points to itself one cannot remove it,
                /// as in the case of a king or a rook, it provides the information,
                /// castling is no longer allowed.
                /// 
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
