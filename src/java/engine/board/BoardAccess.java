package src.java.engine.board;

public interface BoardAccess
{
    
    public Position get_position(int column, int row); 
    public Position[] get_last_move();
}
