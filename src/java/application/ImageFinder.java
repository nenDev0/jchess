package src.java.application;

import java.util.HashMap;

import javax.swing.ImageIcon;

import src.java.engine.board.piecelib.Piece.Type;
import src.java.engine.board.piecelib.Piece.PieceType;

public class ImageFinder {


    private static final String theme = Config.themes_path + Config.skooter_theme;


    private static final ImageIcon background_b = new ImageIcon(theme + "white_tile.png");
    private static final ImageIcon background_w = new ImageIcon(theme + "black_tile.png");
    private static final ImageIcon selection = new ImageIcon(theme + "selection.png");
    private static final ImageIcon legal_move = new ImageIcon(theme +"legal_move.png");
    private static final ImageIcon hover = new ImageIcon(theme + "hover.png");
    
    private static HashMap<PieceType, ImageIcon> map_white_pieces;
    private static HashMap<PieceType, ImageIcon> map_black_pieces;



    private int width;
    private int height;


    public ImageFinder(int width, int height)
    {
        this.width = width / 8;
        this.height = height / 8;


        map_white_pieces = new HashMap<PieceType, ImageIcon>();
        map_black_pieces = new HashMap<PieceType, ImageIcon>();

        map_white_pieces.put(PieceType.KING, new ImageIcon(theme + "white_pieces/king.png"));
        map_white_pieces.put(PieceType.QUEEN, new ImageIcon(theme + "white_pieces/queen.png"));
        map_white_pieces.put(PieceType.ROOK, new ImageIcon(theme + "white_pieces/rook.png"));
        map_white_pieces.put(PieceType.BISHOP, new ImageIcon(theme + "white_pieces/bishop.png"));
        map_white_pieces.put(PieceType.KNIGHT, new ImageIcon(theme + "white_pieces/knight.png"));
        map_white_pieces.put(PieceType.PAWN, new ImageIcon(theme + "white_pieces/pawn.png"));

        map_black_pieces.put(PieceType.KING, new ImageIcon(theme + "black_pieces/king.png"));
        map_black_pieces.put(PieceType.QUEEN, new ImageIcon(theme + "black_pieces/queen.png"));
        map_black_pieces.put(PieceType.ROOK, new ImageIcon(theme + "black_pieces/rook.png"));
        map_black_pieces.put(PieceType.BISHOP, new ImageIcon(theme + "black_pieces/bishop.png"));
        map_black_pieces.put(PieceType.KNIGHT, new ImageIcon(theme + "black_pieces/knight.png"));
        map_black_pieces.put(PieceType.PAWN, new ImageIcon(theme +"black_pieces/pawn.png"));

        m_scale_images();

    }

    public void m_scale_images()
    {
        selection.setImage(selection.getImage().getScaledInstance(width , height, 0));
        legal_move.setImage(legal_move.getImage().getScaledInstance(width, height, 0));
        background_w.setImage(background_w.getImage().getScaledInstance(width, height, 0));
        background_b.setImage(background_b.getImage().getScaledInstance(width, height, 0));
        hover.setImage(hover.getImage().getScaledInstance(width, height, 0));

        for (ImageIcon image : map_black_pieces.values())
        {
            image.setImage(image.getImage().getScaledInstance(width, height, 0));
        }

        for (ImageIcon image : map_white_pieces.values())
        {
            image.setImage(image.getImage().getScaledInstance(width, height, 0));
        }

    }


    public ImageIcon get_piece(Type type, PieceType impl)
    {
        if (type == Type.WHITE)
        {
            return map_white_pieces.get(impl);
        }
        if (type == Type.BLACK)
        {
            return map_black_pieces.get(impl);
        }
        else {
            return null;
        }
    }

    public ImageIcon get_hover()
    {
        return hover;
    }
    public ImageIcon get_selection()
    {
        return selection;
    }

    public ImageIcon get_legal_move()
    {
        return legal_move;
    }

    public ImageIcon get_background(int x, int y)
    {
        if ((x + y) % 2 == 0)
        {
            return background_w;
        }
        else
        {
            return background_b;
        }
    }
}
