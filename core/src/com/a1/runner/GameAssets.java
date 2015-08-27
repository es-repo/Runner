package com.a1.runner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

public class GameAssets {
    public Map<String, Texture> textures = new HashMap<String, Texture>();
    public Map<String, Sound > sounds = new HashMap<String, Sound>();
    public Map<String, Music> musics = new HashMap<String, Music>();

    public void dispose(boolean noSounds){
        for (Map.Entry<String, Texture> entry : textures.entrySet())	{
            entry.getValue().dispose();
        }

        if (noSounds)
            return;

        for (Map.Entry<String, Sound> entry : sounds.entrySet())	{
            entry.getValue().dispose();
        }

        for (Map.Entry<String, Music> entry : musics.entrySet())	{
            entry.getValue().dispose();
        }
    }

    public void dispose(){
        dispose(true);
    }

    public void loadAssets(boolean noSounds){
        HashMap<String, String> textures = new HashMap<String, String>();
        textures.put("blackmask", "blackmask.png");
        textures.put("title", "title.png");
        textures.put("help", "help.png");
        textures.put("background.top", "background/top.png");
        textures.put("background.bottom", "background/bottom.png");
        textures.put("coin", "coin.png");
        textures.put("supercoin", "supercoin.png");
        textures.put("runner.step1", "runner/step1.png");
        textures.put("runner.step2", "runner/step2.png");
        textures.put("runner.step3", "runner/step3.png");
        textures.put("runner.fall", "runner/fall.png");
        textures.put("runner.jump", "runner/jump.png");

        for (int k = 0; k < 5; k++) {
            String sk = String.valueOf(k);
            String path = "platform/" + sk + "/";
            String id = "platform."  + sk + ".";
            textures.put(id + "top", path + "top.png");
            textures.put(id + "top_left", path + "top_left.png");
            textures.put(id + "top_right", path + "top_right.png");
            textures.put(id + "bottom", path + "bottom.png");
            textures.put(id + "bottom_left", path + "bottom_left.png");
            textures.put(id + "bottom_right", path + "bottom_right.png");
        }

        textures.put("pause", "pause.png");
        textures.put("play", "play.png");
        textures.put("soundon", "soundon.png");
        textures.put("soundoff", "soundoff.png");

        textures.put("buttons.start", "buttons/start.png");
        textures.put("buttons.start_pressed", "buttons/start_pressed.png");
        textures.put("buttons.topscores", "buttons/topscores.png");
        textures.put("buttons.topscores_pressed", "buttons/topscores_pressed.png");
        textures.put("buttons.rate", "buttons/rate.png");
        textures.put("buttons.rate_pressed", "buttons/rate_pressed.png");

        textures.put("quit_dialog.frame", "quit_dialog/frame.png");
        textures.put("quit_dialog.buttons.yes", "quit_dialog/buttons/yes.png");
        textures.put("quit_dialog.buttons.yes_pressed", "quit_dialog/buttons/yes_pressed.png");
        textures.put("quit_dialog.buttons.no", "quit_dialog/buttons/no.png");
        textures.put("quit_dialog.buttons.no_pressed", "quit_dialog/buttons/no_pressed.png");

        for (Map.Entry<String, String> entry : textures.entrySet())	{
            this.textures.put(entry.getKey(), new Texture(Gdx.files.internal("textures/" + entry.getValue())));
        }

        if (noSounds)
            return;

        HashMap<String, String> sounds = new HashMap<String, String>();
        sounds.put("coin", "coin.mp3");
        sounds.put("death", "death.wav");
        sounds.put("jump", "jump.wav");
        sounds.put("start", "start.wav");
        sounds.put("step", "step.wav");

        for (Map.Entry<String, String> entry : sounds.entrySet())	{
            this.sounds.put(entry.getKey(), Gdx.audio.newSound(Gdx.files.internal("sounds/" + entry.getValue())));
        }

        HashMap<String, String> musics = new HashMap<String, String>();
        musics.put("song", "song.mp3");

        for (Map.Entry<String, String> entry : musics.entrySet())	{
            Music music = Gdx.audio.newMusic(Gdx.files.internal("sounds/" + entry.getValue()));
            this.musics.put(entry.getKey(), music);
            music.setLooping(true);
        }
    }

}
