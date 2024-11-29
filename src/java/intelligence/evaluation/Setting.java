package src.java.intelligence.evaluation;

public class Setting
{

    private float opening;
    private float mid_game;
    private float end_game;
    private float[] setting_vector;

    public Setting(float opening, float mid_game, float end_game)
    {
        this.opening = opening;
        this.mid_game = mid_game;
        this.end_game = end_game;
    }

    public Setting(float opening, float mid_game, float end_game, float[][] matrix_state)
    {
        this.opening = opening;
        this.mid_game = mid_game;
        this.end_game = end_game;
        this.setting_vector = m_calculate_coefficient_vector(matrix_state);
    }

    public float opening()
    {
        return opening;
    }

    public float mid_game()
    {
        return mid_game;
    }
    
    public float end_game()
    {
        return end_game;
    }

    public float get(int i)
    {
        return setting_vector[i - 1];
    }

    public void m_modify_setting(float modifier)
    {
        opening = opening * modifier;
        mid_game = mid_game * modifier;
        end_game = end_game * modifier;
    }


    private float[] m_calculate_coefficient_vector(float[][] matrix_state)
    {
        float[] setting_vector = new float[16];
        // piececount (-1)
        for (int piececount = 0; piececount < 16; piececount++)
        {
            setting_vector[piececount] =
                opening  * matrix_state[piececount][0] +
                mid_game * matrix_state[piececount][1] +
                end_game * matrix_state[piececount][2];
        }
        return setting_vector;
    }

    @Override
    public String toString()
    {
        int length = String.valueOf(opening).length();

        String s = "[ " + opening + ",";
        while (length < 15)
        {
            s = s + " ";
            length++;
        }

        s = s    + mid_game + ",";
        length = String.valueOf(mid_game).length();
        while (length < 15)
        {
            s = s + " ";
            length++;
        }

        s = s    + end_game;
        length = String.valueOf(end_game).length();
        while (length < 15)
        {
            s = s + " ";
            length++;
        }
        s = s + " ]";
        return s;
    }
    
}
