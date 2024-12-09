package src.java.intelligence.evaluation;

import java.util.LinkedList;

import src.java.engine.board.Board;
import src.java.engine.board.Board.GameState;
import src.java.engine.board.Position;
import src.java.engine.board.piecelib.King;
import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.piecelib.Piece.PieceType;
import src.java.engine.board.updatesystem.ObserverStorage;


/**
 * 
 * 
 */
public class Calculator
{

    private final Configuration configuration;
    private int white_piececount;
    private int black_piececount;
    private float[] values;


    /**
     *  Constructor
     * 
     * @param configuration
     */
    public Calculator(Configuration configuration)
    {
        this.configuration = configuration;
        this.values = new float[2];
        values[0] = 0;
        values[1] = 0;
    }


    /**
     * 
     * 
     * @return {@link #configuration}
     */
    public Configuration get_configuration()
    {
        return configuration;
    }


    /**
     *  Used to calculate the whole evaluation of a board.
     *  
     * @param board // board to be evaluated
     * 
     * @return {@code float evaluated value} // return value is a delta, so a return value of 0 would be an even (board)position
     */
    public float evaluate(Board board)
    {
        white_piececount = board.get_collection(Type.WHITE).get_active_pieces().size();
        black_piececount = board.get_collection(Type.BLACK).get_active_pieces().size();
        values[0] = 0;
        values[1] = 0;

        if (board.is_final())
        {
            if (board.get_state() == GameState.CHECKMATE)
            {
                if (board.get_type() == Type.WHITE)
                {
                    return Integer.MIN_VALUE;
                }
                else
                {
                    return Integer.MAX_VALUE;
                }
            }
                return 0;
        }

        Type[] types = Type.values();
        for (int i = 0; i < 2; i++)
        {
            values[i] += get_piececount(types[i])                * configuration.coefficient(EvalType.PIECECOUNT,   get_piececount(types[i]))
                       + pieceweight(board, types[i])        * configuration.coefficient(EvalType.PIECEWEIGHT,      get_piececount(types[i]))
                       + king_safety(board, types[i])        * configuration.coefficient(EvalType.KING_SAFETY,      get_piececount(types[i]))
                       + game_state(board, types[i])
                       + castling_rights(board, types[i])    * configuration.coefficient(EvalType.CASTLING_RIGHTS,  get_piececount(types[i]))
                       + visible_from(board, types[i])       * configuration.coefficient(EvalType.VISIBLE_FROM,     get_piececount(types[i]));


            LinkedList<Piece> ll_piece = board.get_collection(types[i]).get_active_pieces();
            for (Piece piece : ll_piece)
            {
                values[i] += vision(piece)                      * configuration.coefficient(EvalType.VISION,                     piece.get_piece_type(), get_piececount(types[i]))
                           + legal_moves(board, piece)          * configuration.coefficient(EvalType.LEGAL_MOVES,                piece.get_piece_type(), get_piececount(types[i]))
                           + likes_center(piece)                * configuration.coefficient(EvalType.LIKES_CENTER,               piece.get_piece_type(), get_piececount(types[i]))
                           + likes_forward(piece)               * configuration.coefficient(EvalType.LIKES_FORWARD,              piece.get_piece_type(), get_piececount(types[i]))
                           + contested_positions(piece)         * configuration.coefficient(EvalType.CONTESTED_POSITIONS,        piece.get_piece_type(), get_piececount(types[i]))
                           + safe_moves(piece)                  * configuration.coefficient(EvalType.SAFE_MOVES,                 piece.get_piece_type(), get_piececount(types[i]))
                           + protection(piece)                  * configuration.coefficient(EvalType.PROTECTION,                 piece.get_piece_type(), get_piececount(types[i]))
                           + is_observed(piece)                 * configuration.coefficient(EvalType.IS_OBSERVED,                piece.get_piece_type(), get_piececount(types[i]));
            }
        }
        if (board.get_type() == Type.WHITE)
        {
            return delta(values[0], values[1]);
        }
        else
        {
            return -delta(values[1], values[0]);
        }
    }


    /**
     * Useful to handle it like an array, while having better readability.
     * Possibly unnecessary and could be replaced by an array.
     * 
     * 
     * @param type
     * 
     * @return  {@link #white_piececount}, {@link #black_piececount} // pre-grabbed from PieceCollections
     *          
     */
    private int get_piececount(Type type)
    {
         switch (type)
         {
            case WHITE:
                return white_piececount; 
            case BLACK:
                return black_piececount;
            default:
                throw new IllegalArgumentException("Why is u here?");
        }
    }


    /**
     * if {@code y == 0}, returns 9999
     *  
     *  <p> This in theory has a vanishingly small chance of happening, if the settings aren't handpicked,
     *      however maybe this should be analyzed at a later date.
     * 
     * 
     * @param x
     * @param y
     * 
     * @return {@code float delta} 
     */
    private float delta(float x, float y)
    {
        if (y == 0)
        {
            return 9999;
        }
        return x / y - 1;
    }


    /**
     * 
     * 
     * @param board
     * @param piece
     * 
     * @return {@code float value} 
     */
    private float legal_moves(Board board, Piece piece)
    {
        float value = 0;
        for (int i = piece.get_legal_moves().size(); i > 0 ; i--)
        {
            value += 1 / 1 + value * 1000 * configuration.coefficient(EvalType.LEGAL_MOVES_DEGRADATION, piece.get_piece_type(), get_piececount(piece.get_type()));
        }
        ///
        /// The idea is to prevent queens to simply yeet themselves at the king,
        /// as the enemy player would no longer have any legal moves.
        /// 
        if (board.get_state() == GameState.CHECK)
        {
            value = value * configuration.coefficient(EvalType.LEGAL_MOVES_WHILE_IN_CHECK, get_piececount(piece.get_type()));
        }
        return value;
    }


    /**
     *  Returns a value representative of how many of the positions, the King would be visible from, have opposing observers.
     * 
     * @param board
     * @param type
     * 
     * @return {@code float value} 
     */
    private float king_safety(Board board, Type type)
    {
        float value = 0;
        LinkedList<Position> positions = board.get_collection(type).get_active_pieces().get(0).get_observer().get_observed_positions();
        for (Position position : positions)
        {
            for (ObserverStorage o : position.get_observers())
            {
                if (o.get_piece().is_type(type))
                {
                    value++;
                }
                else
                {
                    value--;
                }
            }
        }
        return value;
    }


    /**
     *  Returns a value representative of how many positions the King is visible from.
     * 
     * @param board
     * @param type
     * 
     * @return {@code float value} 
     */
    private float visible_from(Board board, Type type)
    {
        return ((King)board.get_collection(type).get_active_pieces().get(0)).get_visible_from() * configuration.coefficient(EvalType.VISIBLE_FROM, get_piececount(type));
    }


    /**
     *  Checks are bad.
     *  Checkmates are significantly worse. 
     * 
     * @param board
     * @param type
     * 
     * @return {@code float value} 
     */
    private float game_state(Board board, Type type)
    {
        if (board.get_state() == GameState.CHECK && board.get_type() == type)
        {
            return 100 * configuration.coefficient(EvalType.IN_CHECK, get_piececount(type));
        }
        if (board.get_state() == GameState.CHECKMATE && board.get_type() == type)
        {
            return -Float.MAX_VALUE + 60;
        }
        return 0;
    }


    /**
     *  Returns a value for castling rights.
     *  Simply +1 for each.
     * 
     * @param board
     * @param type
     * 
     * @return {@code float value} 
     */
    private float castling_rights(Board board, Type type)
    {
        float value = 0;
        King king = (King) board.get_collection(type).get_pieces_of_type(PieceType.KING).getFirst();
        if (king.can_castle_kingside())
        {
            value++;
        }
        if (king.can_castle_queenside())
        {
            value++;
        }
        return value;
    }


    /**
     *  Returns a value representative of each position this piece can see.
     * 
     * @param piece
     * 
     * @return {@code float value} 
     */
    private float vision(Piece piece)
    {
        float value = 0;
        for (Position position : piece.get_observer().get_observed_positions())
        {
            for (ObserverStorage o : position.get_observers())
            {
                if (piece.is_type(o.get_piece().get_type()))
                {
                   value += 1 / (1 + value * 1000 * configuration.coefficient(EvalType.VISION_DEGRADATION, piece.get_piece_type(), get_piececount(piece.get_type()))); 
                }
            }
        }
        return value;
    }


    /**
     *  Returns a value representative of all protection it has, modified by it's weight,
     *  the value the piece sets on being protected and each extra protection it gains is
     *  degraded by the amount it already has.
     * 
     * @param piece
     * 
     * @return {@code float value}
     */
    private float protection(Piece piece)
    {
        float value = 0;
        for(ObserverStorage o: piece.get_position().get_observers())
        {
            if (piece.is_type(o.get_piece().get_type()))
            {
                value += piece.get_weight()
                      * configuration.coefficient(EvalType.WEIGHT_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()))
                      * configuration.coefficient(EvalType.PROTECTION_WEIGHT_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()))
                      /(1 + value
                          * 1000
                          * configuration.coefficient(EvalType.PROTECTION_DEGRADATION, piece.get_piece_type(), get_piececount(piece.get_type())));
            }
        }
        return value;
    }


    /**
     *  +1 for each square it is closer to the center.
     * 
     * @param piece
     * 
     * @return {@code float value}
     */
    private float likes_center(Piece piece)
    {
        float x = piece.get_position().get_x();
        float y = piece.get_position().get_y();
        x = (float)(-1/3.5 * Math.pow((3.5-x), 2) + 3.5);
        y = (float)(-1/3.5 * Math.pow((3.5-y), 2) + 3.5);
        return Float.min(x, y);
    }


    /**
     *  +1 for each row it is moved forward.
     * 
     * @param piece
     * 
     * @return {@code float value}
     */
    private int likes_forward(Piece piece)
    {
        switch (piece.get_type())
        {
            case WHITE:
                return piece.get_position().get_y();
            default:
            return 7 - piece.get_position().get_y();
        }
    }


    /**
     *  Returns a value representative of the collective pieceweight of the given type.
     * 
     * @param board 
     * @param type
     * 
     * @return {@code float value}
     */
    private float pieceweight(Board board, Type type)
    {
        LinkedList<Piece> ll_active_pieces; 
        int piececount = get_piececount(type);
        float degradation_coefficient = configuration.coefficient(EvalType.PIECEWEIGHT_DEGRADATION, piececount); 
        ///
        ll_active_pieces = board.get_collection(type).get_active_pieces();
        float weight = 0;
        for (Piece piece : ll_active_pieces)
        {
            weight += piece.get_weight() * configuration.coefficient(EvalType.WEIGHT_MODIFIER, piece.get_piece_type(), piececount)
                    / (1 + weight * 1000 * degradation_coefficient); 
        }
        return weight;
    }


    /**
     *  Returns a value representative of the amount of contested positions this piece has.
     *  Contested positions are defined as positions, which are observed by opposing pieces.
     * 
     * @param piece
     * 
     * @return {@code float value}
     */
    private float contested_positions(Piece piece)
    {
        float value = 0;
        for (Position position : piece.get_legal_moves().keySet())
        {
            if (position.get_piece() != null)
            {
                continue;
            }
            for (ObserverStorage o : position.get_observers())
            {
                if (!piece.is_type(o.get_piece().get_type()))
                {
                    value += 1 / (1 + value * 1000 * configuration.coefficient(EvalType.CONTESTED_POSITIONS_DEGRADATION, piece.get_piece_type(), get_piececount(piece.get_type())));
                }
            }
        }
        return value;
    }


    /**
     *  Calculates moves that are not being controlled by enemy pieces
     * 
     * 
     * @param piece
     * 
     * @return {@code float value}
     */
    private float safe_moves(Piece piece)
    {
        float value = 0;
        float safe_moves_degradation = configuration.coefficient(EvalType.SAFE_MOVES_DEGRADATION, piece.get_piece_type(), get_piececount(piece.get_type()));
        for (Position position : piece.get_legal_moves().keySet())
        {
            // take into account the weight of the pieces controlling?
            if (!position.has_opposing_pieces_observing(piece.get_type()))
            {
                value += 1
                      / (1 + value * 1000 * safe_moves_degradation);
            }
        }
        return value;
    }


    /**
     * 
     * 
     * @param piece
     * 
     * @return {@code float value}
     */
    private float is_observed(Piece piece)
    {
        float value = 0;
        float weight_modifier = configuration.coefficient(EvalType.WEIGHT_MODIFIER,
                                                          piece.get_piece_type(),
                                                          get_piececount(piece.get_type()));
        float is_observed_weight_modifier = configuration.coefficient(EvalType.IS_OBSERVED_WEIGHT_MODIFIER,
                                                                      piece.get_piece_type(),
                                                                      get_piececount(piece.get_type()));
        float is_observed_degradation = configuration.coefficient(EvalType.IS_OBSERVED_DEGRADATION,
                                                                  piece.get_piece_type(),
                                                                  get_piececount(piece.get_type()));
        for (ObserverStorage o : piece.get_position().get_observers())
        {
            if (!piece.is_type(o.get_piece().get_type()))
            {
                value += piece.get_weight() * weight_modifier * is_observed_weight_modifier
                      /  (1 + value * 1000 * is_observed_degradation);
            }
        }
        return value;
    }


}