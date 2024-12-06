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

    private Configuration configuration;
    private int white_piececount;
    private int black_piececount;
    private float[] values;


    /**
     * 
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
     *  <p> currently most methods get the evaluated piece passed over. This might often not be necessary and could be replaced.
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
                values[i] += vision(board, piece)               * configuration.coefficient(EvalType.VISION,                     piece.get_piece_type(), get_piececount(types[i]))
                           + legal_moves(board, piece)          * configuration.coefficient(EvalType.LEGAL_MOVES,                piece.get_piece_type(), get_piececount(types[i]))
                           + likes_center(piece)                * configuration.coefficient(EvalType.LIKES_CENTER,               piece.get_piece_type(), get_piececount(types[i]))
                           + likes_forward(piece)               * configuration.coefficient(EvalType.LIKES_FORWARD,              piece.get_piece_type(), get_piececount(types[i]))
                           + contested_positions(board, piece)  * configuration.coefficient(EvalType.CONTESTED_POSITIONS,        piece.get_piece_type(), get_piececount(types[i]))
                           + safe_moves(board, piece)           * configuration.coefficient(EvalType.SAFE_MOVES,                 piece.get_piece_type(), get_piececount(types[i]))
                           + protection(board, piece)           * configuration.coefficient(EvalType.PROTECTION,                 piece.get_piece_type(), get_piececount(types[i]))
                           + is_observed(board, piece)          * configuration.coefficient(EvalType.IS_OBSERVED,                piece.get_piece_type(), get_piececount(types[i]));
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
        if (board.get_state() == GameState.CHECK)
        {
            value = value * configuration.coefficient(EvalType.LEGAL_MOVES_WHILE_IN_CHECK, get_piececount(piece.get_type()));
        }

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.LEGAL_MOVES_ENEMY_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()));
        }
        return value;
    }


    /**
     * 
     * 
     * @param board
     * @param type
     * 
     * @return {@code float value} 
     */
    private float king_safety(Board board, Type type)
    {
        float value = 0;

        LinkedList<Position> positions = board.get_collection(type).get_active_pieces().get(0).observer().get_observed_positions();
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
     * 
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
     * 
     * 
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
     * 
     * 
     * @param board
     * @param piece
     * 
     * @return {@code float value} 
     */
    private float vision(Board board, Piece piece)
    {
        float value = 0;
        for (Position position : piece.observer().get_observed_positions())
        {
            for (ObserverStorage o : position.get_observers())
            {
                if (piece.is_type(o.get_piece().get_type()))
                {
                   value += 1 / (1 + value * 1000 * configuration.coefficient(EvalType.VISION_DEGRADATION, piece.get_piece_type(), get_piececount(piece.get_type()))); 
                }
            }
        }

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.VISION_ENEMY_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()));
        }

        return value;
    }


    /**
     * 
     * 
     * @param board
     * @param piece
     * 
     * @return {@code float value} 
     */
    private float protection(Board board, Piece piece)
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

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.PROTECTION_ENEMY_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()));
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
    private float likes_center(Piece piece)
    {
        float x = piece.get_position().get_x();
        float y = piece.get_position().get_y();

        x = (float)(-1/3.5 * Math.pow((3.5-x), 2) + 3.5);
        y = (float)(-1/3.5 * Math.pow((3.5-y), 2) + 3.5);

        return Float.min(x, y);
    }


    /**
     * 
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
     * 
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
     * 
     * 
     * @param board
     * @param piece
     * 
     * @return {@code float value} 
     */
    private float contested_positions(Board board, Piece piece)
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

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.CONTESTED_POSITIONS_ENEMY_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()));
        }

        return value;
    }


    /**
     *  calculates moves, that are not being controlled by enemy pieces
     * 
     * 
     * @param board
     * @param piece
     * 
     * @return {@code float value} 
     */
    private float safe_moves(Board board, Piece piece)
    {
        float value = 0;
        for (Position position : piece.get_legal_moves().keySet())
        {
            // take into account the weight of the pieces controlling?
            if (!position.has_opposing_pieces_observing(piece.get_type()))
            {
                value += 1 / (1 + value * 1000 * configuration.coefficient(EvalType.SAFE_MOVES_DEGRADATION, piece.get_piece_type(), get_piececount(piece.get_type())));
            }
        }

        // modified by whether it's the enemy or not
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.SAFE_MOVES_ENEMY_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()));
        }

        return value;
    }


    /**
     * 
     * 
     * @param board
     * @param piece
     * 
     * @return {@code float value} 
     */
    private float is_observed(Board board, Piece piece)
    {
        float value = 0;
        for (ObserverStorage o : piece.get_position().get_observers())
        {
            if (!piece.is_type(o.get_piece().get_type()))
            {
                value += piece.get_weight()
                      * configuration.coefficient(EvalType.WEIGHT_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()))
                      * configuration.coefficient(EvalType.IS_OBSERVED_WEIGHT_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()))
                      /(1 + value
                          * 1000
                          * configuration.coefficient(EvalType.IS_OBSERVED_DEGRADATION, piece.get_piece_type(), get_piececount(piece.get_type())));
            }
        }

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value
                  * 1000
                  * configuration.coefficient(EvalType.IS_OBSERVED_ENEMY_MODIFIER, piece.get_piece_type(), get_piececount(piece.get_type()));
        }
        return value;
    }


}