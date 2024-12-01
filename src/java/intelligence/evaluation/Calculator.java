package src.java.intelligence.evaluation;

import java.util.LinkedList;

import src.java.engine.board.Board;
import src.java.engine.board.GameState;
import src.java.engine.board.Position;
import src.java.engine.board.piecelib.King;
import src.java.engine.board.piecelib.Piece;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.piecelib.Piece.PieceType;
import src.java.engine.board.updatesystem.ObserverStorage;

public class Calculator
{

    private Configuration configuration;
    private int white_piececount;
    private int black_piececount;
    private Type[] types;
    private float[] values;

    public Calculator(Configuration configuration)
    {
        this.configuration = configuration;
        this.types = Type.values();
        this.values = new float[2];
        values[0] = 0;
        values[1] = 0;
    }

    public Configuration get_configuration()
    {
        return configuration;
    }

    public float evaluate(Board board)
    {
        white_piececount = m_piececount(board, Type.WHITE);
        black_piececount = m_piececount(board, Type.BLACK);
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

        for (int i = 0; i < 2; i++)
        {
            values[i] += piececount(types[i])                * configuration.coefficient(EvalType.PIECECOUNT,      piececount(types[i]))
                       + pieceweight(board, types[i])        * configuration.coefficient(EvalType.PIECEWEIGHT,     piececount(types[i]))
                       + king_safety(board, types[i])        * configuration.coefficient(EvalType.KING_SAFETY,     piececount(types[i]))
                       + game_state(board, types[i])
                       + castling_rights(board, types[i])    * configuration.coefficient(EvalType.CASTLING_RIGHTS, piececount(types[i]))
                       + visible_from(board, types[i])       * configuration.coefficient(EvalType.VISIBLE_FROM,    piececount(types[i]));


            LinkedList<Piece> ll_piece = board.get_collection(types[i]).get_active_pieces();
            for (Piece piece : ll_piece)
            {
                values[i] += vision(board, piece)               * configuration.coefficient(EvalType.VISION,                     piece.get_piece_type(), piececount(types[i]))
                           + legal_moves(board, piece)          * configuration.coefficient(EvalType.LEGAL_MOVES,                piece.get_piece_type(), piececount(types[i]))
                           + likes_center(piece)                * configuration.coefficient(EvalType.LIKES_CENTER,               piece.get_piece_type(), piececount(types[i]))
                           + likes_forward(piece)               * configuration.coefficient(EvalType.LIKES_FORWARD,              piece.get_piece_type(), piececount(types[i]))
                           + contested_positions(board, piece)  * configuration.coefficient(EvalType.CONTESTED_POSITIONS,        piece.get_piece_type(), piececount(types[i]))
                           + safe_moves(board, piece)           * configuration.coefficient(EvalType.SAFE_MOVES,                 piece.get_piece_type(), piececount(types[i]))
                           + protection(board, piece)           * configuration.coefficient(EvalType.PROTECTION,                 piece.get_piece_type(), piececount(types[i]))
                           + is_observed(board, piece)          * configuration.coefficient(EvalType.IS_OBSERVED,                piece.get_piece_type(), piececount(types[i]));
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


    private int piececount(Type type)
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


    private int m_piececount(Board board, Type type)
    {
        return board.get_collection(type).get_active_pieces().size();
    }


    private float delta(float x, float y)
    {
        if (y == 0)
        {
            return 9999;
        }
        return x / y - 1;
    }


    private float legal_moves(Board board, Piece piece)
    {
        float value = 0;
        for (int i = piece.get_legal_moves().size(); i > 0 ; i--)
        {
            value += 1 / 1 + value * 1000 * configuration.coefficient(EvalType.LEGAL_MOVES_DEGRADATION, piece.get_piece_type(), piececount(piece.get_type()));
        }
        if (board.get_state() == GameState.CHECK)
        {
            value = value * configuration.coefficient(EvalType.LEGAL_MOVES_WHILE_IN_CHECK, piececount(piece.get_type()));
        }

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.LEGAL_MOVES_ENEMY_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()));
        }
        return value;
    }

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

    private float visible_from(Board board, Type type)
    {
        return ((King)board.get_collection(type).get_active_pieces().get(0)).get_visible_from() * configuration.coefficient(EvalType.VISIBLE_FROM, piececount(type));
    }

    private float game_state(Board board, Type type)
    {
        if (board.get_state() == GameState.CHECK && board.get_type() == type)
        {
            return 100 * configuration.coefficient(EvalType.IN_CHECK, piececount(type));
        }
        if (board.get_state() == GameState.CHECKMATE && board.get_type() == type)
        {
            return -Float.MAX_VALUE + 60;
        }
        return 0;
    }

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


    private float vision(Board board, Piece piece)
    {
        float value = 0;
        for (Position position : piece.observer().get_observed_positions())
        {
            for (ObserverStorage o : position.get_observers())
            {
                if (piece.is_type(o.get_piece().get_type()))
                {
                   value += 1 / (1 + value * 1000 * configuration.coefficient(EvalType.VISION_DEGRADATION, piece.get_piece_type(), piececount(piece.get_type()))); 
                }
            }
        }

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.VISION_ENEMY_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()));
        }

        return value;
    }

    private float protection(Board board, Piece piece)
    {
        float value = 0;
        for(ObserverStorage o: piece.position().get_observers())
        {
            if (piece.is_type(o.get_piece().get_type()))
            {
                value += piece.get_weight()
                      * configuration.coefficient(EvalType.WEIGHT_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()))
                      * configuration.coefficient(EvalType.PROTECTION_WEIGHT_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()))
                      /(1 + value
                          * 1000
                          * configuration.coefficient(EvalType.PROTECTION_DEGRADATION, piece.get_piece_type(), piececount(piece.get_type())));
            }
        }

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.PROTECTION_ENEMY_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()));
        }

        return value;
    }



    private float likes_center(Piece piece)
    {
        float x = piece.position().get_x();
        float y = piece.position().get_y();

        x = (float)(-1/3.5 * Math.pow((3.5-x), 2) + 3.5);
        y = (float)(-1/3.5 * Math.pow((3.5-y), 2) + 3.5);

        return Float.min(x, y);
    }

    private int likes_forward(Piece piece)
    {
        switch (piece.get_type())
        {
            case WHITE:
                return piece.position().get_y();
            default:
            return 7 - piece.position().get_y();
        }
    }

    private float pieceweight(Board board, Type type)
    {
        LinkedList<Piece> ll_active_pieces; 
        int piececount = piececount(type);
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

    private float contested_positions(Board board, Piece piece)
    {
        float value = 0;
        for (Position position : piece.get_legal_moves())
        {
            if (position.get_piece() != null)
            {
                continue;
            }
            for (ObserverStorage o : position.get_observers())
            {
                if (!piece.is_type(o.get_piece().get_type()))
                {
                    value += 1 / (1 + value * 1000 * configuration.coefficient(EvalType.CONTESTED_POSITIONS_DEGRADATION, piece.get_piece_type(), piececount(piece.get_type())));
                }
            }
        }

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.CONTESTED_POSITIONS_ENEMY_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()));
        }

        return value;
    }

    /*
     * safe_moves()
     *  calculates moves, that are not being controlled by enemy pieces
     *  
     */
    private float safe_moves(Board board, Piece piece)
    {
        float value = 0;
        for (Position position : piece.get_legal_moves())
        {
            // take into account the weight of the pieces controlling?
            if (!position.has_opposing_pieces_observing(piece.get_type()))
            {
                value += 1 / (1 + value * 1000 * configuration.coefficient(EvalType.SAFE_MOVES_DEGRADATION, piece.get_piece_type(), piececount(piece.get_type())));
            }
        }

        // modified by whether it's the enemy or not
        if (board.get_type() != piece.get_type())
        {
            value += value * 1000 * configuration.coefficient(EvalType.SAFE_MOVES_ENEMY_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()));
        }

        return value;
    }


    private float is_observed(Board board, Piece piece)
    {
        float value = 0;
        for (ObserverStorage o : piece.position().get_observers())
        {
            if (!piece.is_type(o.get_piece().get_type()))
            {
                value += piece.get_weight()
                      * configuration.coefficient(EvalType.WEIGHT_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()))
                      * configuration.coefficient(EvalType.IS_OBSERVED_WEIGHT_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()))
                      /(1 + value
                          * 1000
                          * configuration.coefficient(EvalType.IS_OBSERVED_DEGRADATION, piece.get_piece_type(), piececount(piece.get_type())));
            }
        }

        // modified by whether it's the enemy or not 
        if (board.get_type() != piece.get_type())
        {
            value += value
                  * 1000
                  * configuration.coefficient(EvalType.IS_OBSERVED_ENEMY_MODIFIER, piece.get_piece_type(), piececount(piece.get_type()));
        }
        return value;
    }
}