package src.java.engine.game;

import java.io.File;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import src.java.application.Config;

public class SoundEngine extends Thread{
    
    private String path = Config.audios_path;
    private File move;
    //private File take;
    private AudioInputStream stream_move;
    //private AudioInputStream stream_take;
    private Clip move_clip;
    private Clip take_clip;
    private boolean is_open;

    public SoundEngine() {
        is_open = false;
        move = new File(path + "move.wav");
        //take = new File("src/sounds/taking.wav");
        try {
        stream_move = AudioSystem.getAudioInputStream(move);
        //stream_take = AudioSystem.getAudioInputStream(take);
        } catch (Exception e) {
            throw new IllegalArgumentException("AUDIO AAA1");
        }
        AudioFormat format_m = stream_move.getFormat();
        //AudioFormat format_t = stream_take.getFormat();
        try {
            DataLine.Info info_m = new DataLine.Info(Clip.class, format_m);
            move_clip = (Clip) AudioSystem.getLine(info_m);
            //DataLine.Info info_t = new DataLine.Info(Clip.class, format_t);
            //take_clip = (Clip) AudioSystem.getLine(info_t);
            move_clip.open(stream_move);
        } catch (Exception e) {
            throw new IllegalArgumentException("AUDIO AAA2");
        }
    }

    public void play_move() {
        if (is_open)
        {
            return;
        }
        try {
            is_open = true;
            //move_clip.open(stream_move);
            move_clip.setMicrosecondPosition(0);
            move_clip.start();
            //sleep(1000);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Audio AAA4");
        }
        //move_clip.stop();
        is_open = false;
    }

    /*public void play_take()
    {
        if (is_open)
        {
            return;
        }
        try {
            is_open = true;
            take_clip.open(stream_take);
            take_clip.setMicrosecondPosition(0);
            take_clip.start(); 
            Thread.sleep(1000);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Audio AAA4");
        }
        take_clip.stop();
        is_open = false;
    }*/


    public void s_stop() {
        take_clip.close();
        move_clip.close();
    }
}
