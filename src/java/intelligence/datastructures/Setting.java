package src.java.intelligence.datastructures;


/**
 *  Used to save setting tuples coming from config files written in json.
 * 
 */
public class Setting
{

    private float opening;
    private float mid_game;
    private float end_game;
    private float[] setting_vector; // pre-calculated vector: 16x1, one value per piececount


    /**
     *  Constructor.
     * 
     * @param opening
     * @param mid_game
     * @param end_game
     * @param matrix_state required to precalculate the {@link #setting_vector}
     * 
     */
    public Setting(float opening, float mid_game, float end_game, float[][] matrix_state)
    {
        this.opening = opening;
        this.mid_game = mid_game;
        this.end_game = end_game;
        this.setting_vector = m_calculate_coefficient_vector(matrix_state);
    }


    /**
     *  Alternative Constructor
     *  only used for Timeline Setting.
     *  No need for a {@link #setting_vector}.
     * 
     * @param opening
     * @param mid_game
     * @param end_game
     * 
     */
    public Setting(float opening, float mid_game, float end_game)
    {
        this.opening = opening;
        this.mid_game = mid_game;
        this.end_game = end_game;
    }


    /**
     * 
     * @return {@link #opening}
     */
    public float opening()
    {
        return opening;
    }


    /**
     * 
     * @return {@link #mid_game}
     */
    public float mid_game()
    {
        return mid_game;
    }
    

    /**
     * 
     * @return {@link #end_game}
     */
    public float end_game()
    {
        return end_game;
    }


    /**
     *  returns a precalculated value for the handed piececount, defined by this setting's original values.
     * 
     * 
     * @param piececount
     * 
     * @return value of {@link #setting_vector} at piececount - 1
     */
    public float get_value(int piececount)
    {
        return setting_vector[piececount - 1];
    }


    /**
     * 
     * @param modifier
     */
    public void m_modify_setting(float modifier)
    {
        opening = opening * modifier;
        mid_game = mid_game * modifier;
        end_game = end_game * modifier;
    }


    /**
     *  calculates the setting_vector using the state matrix handed over by the Configuration class.
     * 
     * 
     * @param matrix_state
     * 
     * @return {@link #setting_vector}
     */
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


    /**
     * Used, when writing config files.
     * 
     * 
     * @return String in format {@code [0.0, 0.0, 0.0]}
     * with added whitespaces to increase readability in config files
     */
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
