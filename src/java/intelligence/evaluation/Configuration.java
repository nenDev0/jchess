package src.java.intelligence.evaluation;

import java.util.HashMap;

import src.java.application.Config;
import src.java.engine.board.piecelib.Piece.Type;
import src.java.intelligence.datastructures.Setting;
import src.java.engine.board.piecelib.Piece.PieceType;

import java.util.Random;

/**
 *  Contains all Data used to add coefficients to the {@code EvalType}'s during evaluation.
 * 
 *  Global and piece-specific {@code EvalType}'s are saved separately within:
 * 
 *  {@link #map_overall_config},
 *  {@link #map_piece_config}
 * 
 */
public class Configuration
{

    /*
     * used to reduce calculations, when requesting EvalType coefficients
     */
    private Setting state_setting; // used to calculate the state_matrix
    private float[][] state_matrix; // used to create a 16x1 settings_vector for all settings

    private HashMap<EvalType, Setting> map_overall_config;
    private HashMap<PieceType, HashMap<EvalType, Setting>> map_piece_config;

    private JsonHandler cfg_handler; // handles read and write of the config file
    private boolean randomized; // enables this Configuration to have randomized values, based on the setting contained in the Config class


    /**
     *  Constructor
     *  initializes new JsonHandler with {@code path} and reads the config of the given path
     * 
     * @param path
     * @param randomized
     * 
     */
    public Configuration(String path, boolean randomized)
    {
        this.randomized = randomized;
        cfg_handler = new JsonHandler(path);
        map_overall_config = new HashMap<EvalType, Setting>();
        map_piece_config = new HashMap<PieceType, HashMap<EvalType, Setting>>();
        m_read_config();
    }


    /**
     * 
     * @return {@link #state_setting}
     */
    public Setting state_setting()
    {
        return state_setting;
    }


    /**
     *  Returns {@code true}, if this Configuration is set to randomize values.
     * 
     *  <p> Useful for bot-training-mode
     * 
     * 
     * @return {@code boolean}
     */
    public boolean is_randomized()
    {
        return randomized;
    }


    /**
     *  Passes call over to {@link #cfg_handler}. This will overwrite the config file with the settings of this Configuration.
     * 
     * @param file_path
     * @param type
     * 
     */
    public void write_config(String file_path, Type type)
    {
        cfg_handler.write_config(this, file_path, type);
    }


    /**
     *  Returns coefficient of the given {@code EvalType}, with regards to the current {@code piececount} and the handed {@code PieceType}
     * 
     * @param eval_type
     * @param piece_implementation
     * @param piececount
     * 
     * @return {@code float value}
     */
    public float coefficient(EvalType eval_type, PieceType piece_implementation, int piececount)
    {
        return setting(eval_type, piece_implementation).get_value(piececount);
    }


    /**
     *  Returns coefficient of the given {@code EvalType}, with regards to the current {@code piececount}
     * 
     * @param eval_type
     * @param piececount
     * 
     * @return {@code float value}
     */
    public float coefficient(EvalType eval_type, int piececount)
    {
        return setting(eval_type).get_value(piececount);
    }


    /**
     *  Returns {@code Setting} of the given {@code EvalType}
     * 
     *  <p> Used for piece specific {@code EvalType}'s
     * 
     * @param eval_type
     * @param piece_type
     * 
     * @return {@code Setting} // pulled from {@link #map_piece_config}
     */
    public Setting setting(EvalType eval_type, PieceType piece_type)
    {
        return map_piece_config.get(piece_type).get(eval_type);
    }


    /**
     *  Returns {@code Setting} of the given {@code EvalType}
     * 
     *  <p> Used for global {@code EvalType}'s
     * 
     * @param eval_type
     * 
     * @return
     */
    public Setting setting(EvalType eval_type)
    {
        return map_overall_config.get(eval_type);
    }


    /**
     *  executed during initialization.
     * 
     */
    private void m_read_config()
    {

        m_add_config(EvalType.TIMELINE, null);
        create_state_matrix();


        EvalType[] eval_types = EvalType.values();
        for (int i = JsonHandler.NUMBER_OF_PIECE_SPECIFIC_VALUES; i < JsonHandler.NUMBER_OF_PIECE_SPECIFIC_VALUES + JsonHandler.NUMBER_OF_GLOBAL_VALUES; i++)
        {
            m_add_config(eval_types[i], null);
            }

            for (PieceType impl : PieceType.values())
            {
                for (int i = 0; i < JsonHandler.NUMBER_OF_PIECE_SPECIFIC_VALUES; i++)
                {
                    m_add_config(eval_types[i], impl);
                }
            }


        //System.out.println(map_overall_config);
        //System.out.println(map_piece_config);
        m_normalize_values();
    }


    /**
     *  normalizes all values, so the bot configuration has a global maximum value equal to {@code 10}. 
     * 
     *  <p> ensures the values in the config file stay within calculatable margins.
     * 
     * 
     * @return void
     */
    private void m_normalize_values()
    {
        EvalType[] evals = EvalType.values();
        float max = 0;
        float x = 0;
        for (EvalType eval_type : map_overall_config.keySet())
            {
            x = get_max(setting(eval_type));
                if (x > max)
                {
                    max = x;
                }
            }
            for (PieceType impl : PieceType.values())
            {
            for(EvalType eval_type : map_piece_config.get(impl).keySet())
                {
                x = get_max(setting(eval_type, impl));
                    if (x > max)
                    {
                        max = x;
                }
            }
        }
        float modifier = (float)10/max;
        //System.out.println("maximum value found: "+max+", calculated modifier: "+modifier);
        
        for (int i = JsonHandler.NUMBER_OF_PIECE_SPECIFIC_VALUES; i < JsonHandler.NUMBER_OF_PIECE_SPECIFIC_VALUES + JsonHandler.NUMBER_OF_GLOBAL_VALUES; i++)
        {
            map_overall_config.get(evals[i]).m_modify_setting(modifier);
        }
        for (PieceType impl : PieceType.values())
        {
            for (int i = 0; i < JsonHandler.NUMBER_OF_PIECE_SPECIFIC_VALUES; i++)
                {
                map_piece_config.get(impl).get(evals[i]).m_modify_setting(modifier);
            }
        }
    }


    /**
     *  returns maximum value contained in this setting
     * 
     * @param setting
     * 
     * @return
     */
    private float get_max(Setting setting)
    {
        float max = 0;
        max = Float.max(Math.abs(setting.opening()), Math.abs(setting.mid_game()));
        max = Float.max(max, Math.abs(setting.end_game()));
        //System.out.println("max of setting " +setting + ", is :" + max);
        return max;
    }


    /**
     *  Creates the {@link #state_matrix}
     *  used to evaluate how each value within a setting is weighed, according to the piececount.
     * 
     * 
     */
    private void create_state_matrix()
    {
        state_matrix = new float[16][3];
        for (int piececount = 0; piececount < 16; piececount++)
        {
            double d_piececount = (double) (piececount + 1);
            state_matrix[piececount][0] = 
                    (float)Math.sqrt(((d_piececount - state_setting.opening() ) / (16 - state_setting.opening())));
            state_matrix[piececount][1] = 
                    (float)Math.sqrt(((d_piececount - state_setting.mid_game() ) / (16 - state_setting.mid_game())));
            state_matrix[piececount][2] = 1;
            for (int i = 0;i < 3; i++)
            {
                if (state_matrix[piececount][i] < 0 || Float.isNaN(state_matrix[piececount][i]))
                {
                    state_matrix[piececount][i] = 0;
                }
            }
            state_matrix[piececount][2] += - state_matrix[piececount][0] - state_matrix[piececount][1];
            state_matrix[piececount][1] += - state_matrix[piececount][0];
            for (int i = 0;i < 3; i++)
            {
                if (state_matrix[piececount][i] < 0)
                {
                    state_matrix[piececount][i] = 0;
                }
            }
        }
    }


    /**
     *  fills in {@link #map_overall_config} and {@link #map_piece_config}
     *  calls for {@link #cfg_handler} to read the config file and return specified values
     *  
     * @param eval_type
     * @param piece_implementation
     * 
     */
    private void m_add_config(EvalType eval_type, PieceType piece_implementation)
    {
        float[] values;
        if (piece_implementation == null)
        {
            values = cfg_handler.values(cfg_handler.info(eval_type.name()));
        }
        else
        {
            values = cfg_handler.values(cfg_handler.info(piece_implementation.toString(), eval_type.toString()));
        }

        if (randomized && eval_type != EvalType.TIMELINE)
        {
            values = m_randomize(values);
        }
        
        Setting setting;
        if (eval_type == EvalType.TIMELINE) {
            setting = new Setting(values[0],
                                  values[1], 
                                  values[2]);
            state_setting = new Setting(setting.opening(), setting.mid_game(), setting.end_game());
            //System.out.println(state_setting);
            return;
        }
            setting = new Setting(values[0],
                                  values[1], 
                                  values[2],
                                  state_matrix);

        if (piece_implementation == null)
        {
            map_overall_config.put(eval_type, setting);
            return;
        }
        else
        {
            map_piece_config.putIfAbsent(piece_implementation, new HashMap<EvalType, Setting>());
            map_piece_config.get(piece_implementation).put(eval_type, setting);
            return;
        }
    }


    /**
     *  when {@link #randomized}{@code = true},
     *  the handed values have a chance to have random deviations, based on values from the Config class.
     * 
     * @param values // values to randomize
     * 
     * @return handed values with potentially randomized deviations
     */
    private float[] m_randomize(float[] values)
    {
        Random rand = new Random();
        for (int i = 0; i < values.length; i++)
        {
            if (rand.nextFloat(0, 1) >= Config.get_random_chance())
            {
                continue;
            }
            values[i] = values[i] * rand.nextFloat(-Config.get_max_deviation() / Config.RANDOMIZER_POSITIVE_NEGATIVE_DIFF, Config.get_max_deviation());
            if (values[i] == 0)
            {
                values[i] = rand.nextFloat((float)-0.5,(float) 0.5);
            }
        }
        return values;
    }


}