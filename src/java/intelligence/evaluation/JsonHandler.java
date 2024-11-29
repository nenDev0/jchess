package src.java.intelligence.evaluation;

import java.io.FileWriter;
import java.io.File;
import java.util.Scanner;

import src.java.engine.board.Type;
import src.java.engine.board.piecelib.PieceType;

public class JsonHandler
{
    
    private String file_data;
    public static final int NUMBER_OF_PIECE_SPECIFIC_VALUES = 24; 
    public static final int NUMBER_OF_GLOBAL_VALUES = 8;
    public static final int NUMBER_OF_TOPICS = 2;
    public JsonHandler(File file)
    {
        try
        {
            read_file(file);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("FILE FAILED TO READ");
        }
    }

    private void read_file(File file) throws Exception
    {
        Scanner scanner = new Scanner(file);
        file_data = "";
        while (scanner.hasNext())
        {
            file_data = file_data + scanner.nextLine();
        }
        file_data = file_data.replaceAll(" ", "");
        //System.out.println(file_data);
        scanner.close();
    }

    public float[] values(String segment)
    {
        String[] s_values = segment.split(",");
        float[] values = new float[s_values.length];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = Float.parseFloat(s_values[i]);
        }
        return values;

    }

    public String info(String identification)
    {
        return extract(file_data, identification);
    }

    public String info(String topic, String identification)
    {
        return extract(extract(file_data, topic), identification);
    }

    public void write_config(Configuration config, String file_path, Type type)
    {
        System.out.println("WIRTING CONFIG");
        EvalType[] evals = EvalType.values();
        PieceType[] piece_types = PieceType.values();
        String json = "{\n"
                    + "    \"TYPE\": \"" + type.name() + "\",\n";

        json = json + "    \"" + EvalType.TIMELINE + "\":                                     " + config.state_setting() + ",\n";

        json = json + "    \""+ EvalType.GLOBAL +"\":\n    {\n";
        for (int i = NUMBER_OF_PIECE_SPECIFIC_VALUES; i < NUMBER_OF_PIECE_SPECIFIC_VALUES + NUMBER_OF_GLOBAL_VALUES; i++)
        {
            Setting setting = config.setting(evals[i]);
            json = json + "        \"" + evals[i].name() + "\": ";
            int j = 0;
            while (evals[i].name().length() + j < 40)
            {
                json = json + " ";
                j++;
            }
            json = json + setting;
            if (i < NUMBER_OF_PIECE_SPECIFIC_VALUES + NUMBER_OF_GLOBAL_VALUES - 1)
            {
                json = json + ",";
            }
            json = json + "\n";
        }
        json = json + "    },\n";
        for (int t = 0; t < piece_types.length; t++)
        {
            json = json + "    \"" + piece_types[t].name() + "\":\n    {\n";
            for (int i = 0 ; i < NUMBER_OF_PIECE_SPECIFIC_VALUES ; i++)
            {
                json = json + write_setting(evals[i], config.setting(evals[i], piece_types[t]), i);
            }
            json = json + "    }";
            if (t < piece_types.length - 1)
            {
                json = json + ",";
            }
            json = json + "\n";
        }
        json = json + "}";

        // TODO implement Changelog -> use in further iterations

        try
        {
        FileWriter newgen = new FileWriter(file_path);
        newgen.write(json);
        newgen.close();
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("couldn't write!");
        }
    }

    private String write_setting(EvalType eval, Setting setting, int i)
    {
        String s = "        \"" + eval.name() + "\": ";
        int j = 0;
        while (eval.name().length() + j < 40)
        {
            s = s + " ";
            j++;
        }
        s = s + setting;
        if (i < NUMBER_OF_PIECE_SPECIFIC_VALUES - 1)
        {
            s = s + ",";
        }
        s = s + "\n";
        return s;
    }

    private String extract(String data, String identification)
    {
        int content_begin = data.indexOf("\"" +identification + "\"") + identification.length() + 3;
        String segment = data.substring(content_begin);
            switch (segment.charAt(0))
            {
                case '[':
                    segment = m_segment_finder(segment, '[', ']');
                    break;
                case '{':
                    segment = m_segment_finder(segment, '{', '}');
                    break;
                case '\"':
                    segment = segment.substring(1, segment.indexOf('\"', 1));
                    break;
                default:
                    segment = segment.substring(0, m_next_ender(segment));
                    break;
            }

        //System.out.println("extracted segment: " + segment + ", called: " + identification);
        return segment;
    }


    private int m_next_ender(String data)
    {
        int position = -1;
        char[] enders = {']', '}', ','};
        for (int i = 0; i < enders.length; i++)
        {
            int iterate_position = data.indexOf(enders[i]);
            if (position == -1 || position > iterate_position)
            {
                position = iterate_position;
            }
        }
        return position;

    }

    private String m_segment_finder(String data, char bracket, char bracket_end)
    {
        String partition = "";
        int bracket_position = 0;
        int bracket_end_position = data.indexOf(bracket_end, 1);

        partition = data.substring(bracket_position + 1, bracket_end_position);

        while (partition.indexOf(bracket) != -1)
        {
        bracket_end_position = data.indexOf(bracket_end ,bracket_end_position + 1);
        bracket_position = partition.indexOf(bracket);
        partition = data.substring(bracket_position + 1, bracket_end_position);
        }
        partition = data.substring( 1, bracket_end_position);
        return partition;
    }

}
