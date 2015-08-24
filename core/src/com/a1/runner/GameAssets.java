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
        textures.put("background.main", "background/main.png");
        textures.put("coin", "coin.png");
        textures.put("supercoin", "supercoin.png");
        textures.put("runner.step1", "runner/step1.png");
        textures.put("runner.step2", "runner/step2.png");
        textures.put("runner.step3", "runner/step3.png");

        textures.put("platform.1.top", "platform/1/top.png");
        textures.put("platform.1.topl0", "platform/1/topl0.png");
        textures.put("platform.1.topl1", "platform/1/topl1.png");
        textures.put("platform.1.topl2", "platform/1/topl2.png");
        textures.put("platform.1.topl3", "platform/1/topl3.png");

        textures.put("platform.1.bottoml0", "platform/1/bottoml0.png");
        textures.put("platform.1.bottoml1", "platform/1/bottoml1.png");
        textures.put("platform.1.bottoml2", "platform/1/bottoml2.png");
        textures.put("platform.1.bottoml3", "platform/1/bottoml3.png");

//        textures.put("tile", "tile.png");
//        textures.put("tile_bottom", "tile_bottom.png");
//        textures.put("tile2", "tile2.png");
//        textures.put("tile3", "tile3.png");
//        textures.put("tile4", "tile4.png");
//        textures.put("tile5", "tile5.png");

        textures.put("particle", "particle.png");
        textures.put("pause", "pause.png");
        textures.put("play", "play.png");
        textures.put("soundon", "soundon.png");
        textures.put("soundoff", "soundoff.png");

        textures.put("buttons.start", "buttons/start.png");
        textures.put("buttons.start_pressed", "buttons/start_pressed.png");
        textures.put("buttons.topscores", "buttons/topscores.png");
        textures.put("buttons.topscores_pressed", "buttons/topscores_pressed.png");

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
