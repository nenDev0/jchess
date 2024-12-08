package src.java.engine.board;

import src.java.engine.board.piecelib.Pawn;
import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.piecelib.Piece.PieceType;


/**
 *  Contains all data needed to execute a Move on the board.
 *  This class should be used to handle all the specific rules connected to the
 *  {@code MoveType's} ({@link #arr_types})
 *  
 */
public class Move
{


    /**
     *      Note that some of these can happen at the same time.
     * 
     *  <p> All of theses states must be saved to ensure correct history reduction
     *      for the bot algorithm.
     * 
     *  <p> In case the Chess Standard Notation were to be implemented,
     *      they would all have to be saved correctly as well
     */
    public enum MoveType
    {
        TAKES,
        CASTLING_QUEENSIDE,
        CASTLING_KINGSIDE,
        /// Note that, as it stands right now. A move, which is EN_PASSANT, is not considered TAKES
        /// This simply allows for more linear logic implementations.
        EN_PASSANT_LEFT,
        EN_PASSANT_RIGHT,
        PROMOTION_QUEEN,
        PROMOTION_ROOK,
        PROMOTION_BISHOP,
        PROMOTION_KNIGHT,
    }


    /// The fields should never change, as all information needed can be
    /// initialized on initialization
    private final Position position_from;
    private final Position position_to;
    private final PieceType implementation;
    private final Piece taken_piece;
    private final MoveType[] arr_types;
    ///
    private float weight;


    /**
     *  Alternative Constructor
     *  This is only used in a single instance:
     *  Allows MoveNodes, which extend Nodes, to have no unnecessary random information.
     *  Ensures none of the internal logic is processed.
     * 
     *  <p> Note: When this Constuctor is called,
     *      any methods within this instance, besides weight related calls, throw Exceptions
     */
    public Move()
    {
        arr_types = null;
        position_from = null;
        position_to = null;
        implementation = null;
        taken_piece = null;
    }


    /**
     *  Constructor.
     *  Pulls all data necessary to correctly execute the move.
     * 
     * @throws NullPointerException, if {@link #position_to} is null
     *  and {@link #arr_types} contains {@code MoveType.TAKES}
     * 
     * @param position_from
     * @param position_to
     * @param arr_types
     * 
     */
    public Move(Position position_from, Position position_to, MoveType[] arr_types)
    {
        this.position_from = position_from;
        this.position_to = position_to;
        this.arr_types = arr_types;
        /// 
        /// The move must receive this information.
        /// new class to dictate, which piece is allowed to move where?
        /// -> reduce class hierarchy to only include "piece" as an overall class?
        Piece taken_piece = null;
        for (MoveType move_type : arr_types) {
            switch (move_type) {
                case TAKES:
                    taken_piece = position_to().get_piece();
                    break;
                ///
                /// There currently is no better connection to enable this.
                /*case EN_PASSANT_LEFT: 
                    taken_piece = position_from.get_piece().
                                                get_collection().
                                                get_board_access().
                                                get_position(position_from.get_x() + 1, position_from.get_y()).
                                                get_piece();
                    break;
                case EN_PASSANT_RIGHT:
                    taken_piece = position_from.get_piece().
                                                get_collection().
                                                get_board_access().
                                                get_position(position_from.get_x() - 1, position_from.get_y()).
                                                get_piece();
                ///
                */
                default:
                    break;
            }
        }
        this.taken_piece = taken_piece;
        this.implementation = position_from.get_piece().get_piece_type();
    }


    /**
     *  Sets the weight of this move to the given value.
     * 
     * @param weight
     * 
     */
    public void m_add_weight(float weight)
    {
        this.weight = weight;
    }


    /**
     * 
     * 
     * @return {@link #weight}
     */
    public float get_weight()
    {
        return weight;
    }
 

    /**
     * 
     * 
     * @return {@link #arr_types}
     */
    public MoveType[] get_types()
    {
        return arr_types;
    }


    /**
     * 
     * 
     * @return {@link #position_from}
     */
    public Position position_from()
    {
        return position_from;
    }


    /**
     * 
     * 
     * @return {@link #position_to}
     */
    public Position position_to()
    {
        return position_to;
    }


    /**
     *  Commits this move on the board.
     *  (Currently not true:) Handles all special rules.
     * 
     * @throws NullPointerException {@code if (position_from == null ||
     *                                         position_to == null   ||
     *                                         position_from.get_piece() == null)}
     */
    public void m_commit()
    {
        Piece piece = position_from.get_piece();
        piece.m_set_position(position_to);
        piece.m_increase_move();
    }


    /**
     *  Reverses this move.
     *  (Currently not true:) Ensures consistency and takes care of all special rules.
     * 
     * @throws NullPointerException {@code if (position_from == null ||
     *                                         position_to == null   ||
     *                                         position_to.get_piece() == null)}
     */
    public void m_reverse()
    {

        /// implement movetype reversion here.
        /*for (MoveType move_type : arr_types)
        {
            switch (move_type)
            {
                case TAKES:
                case EN_PASSANT_LEFT: 
                    taken_piece = position_from.get_piece().
                                                get_collection().
                                                get_board_access().
                                                get_position(position_from.get_x() + 1, position_from.get_y()).
                                                get_piece();
                    break;
                case EN_PASSANT_RIGHT:
                    taken_piece = position_from.get_piece().
                                                get_collection().
                                                get_board_access().
                                                get_position(position_from.get_x() - 1, position_from.get_y()).
                                                get_piece();
        }*/
        Piece piece = position_to.get_piece();
        piece.m_decrease_move();
        if (piece.get_piece_type() != PieceType.KING)
        {
            piece.m_set_position(null);
        }
        else
        {
            piece.m_set_position(position_from);
        }
        ///
        if (taken_piece != null)
        {
            taken_piece.get_collection().m_untake(taken_piece);
            taken_piece.m_set_position(position_to);
        }
        if (piece.get_piece_type() != PieceType.KING)
        {
            piece.m_set_position(position_from);
        }
        ///
        /// promotion reversal -> demotion
        if (implementation == PieceType.PAWN)
        {
            if (position_to.get_y() == 7 && piece.is_type(Type.WHITE) ||
                position_to.get_y() == 0 && piece.is_type(Type.BLACK))
            {
                piece.get_collection().m_demote(piece);
                return;
            }
            /// en passant reversal
                Pawn pawn = (Pawn) piece;
            if (pawn.en_passant != null)
            {
                if (pawn.en_passant_position.get_y() != position_from.get_y())
                {
                    return;
                }
                pawn.en_passant.get_collection().m_untake(pawn.en_passant);
                pawn.en_passant.m_set_position(pawn.en_passant_position);
                pawn.en_passant_position = null;
                pawn.en_passant = null;
            }
        }
    }


    public boolean equals(Move move)
    {
        if (!this.position_from().equals(move.position_from()))
        {
            return false;
        }
        if (!this.position_to().equals(move.position_to()))
        {
            return false;
        }
        return true;
    }


    /**
     *  Clones this {@code move}, with pointers adjusted to the board given
     *  
     * @throws NullPointerException {@code if (position_from == null ||
     *                                         position_to == null   ||
     *                                         board == null         ||
     *                                         movetypes are wrong)}
     * 
     * @param board
     * 
     * @return
     */
    public Move convert(Board board)
    {
        return new Move(position_from.convert(board), position_to.convert(board), arr_types);
    }


    @Override
    public String toString()
    {
        String s = "";
        s = "\nMove :(" + position_from + ",  -> " + position_to + ")\n";
        return s;
    }


    @Override
    public int hashCode()
    {
       return (position_from.hashCode() << 6) + position_to.hashCode();
    }

    
}