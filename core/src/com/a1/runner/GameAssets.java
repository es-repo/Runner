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
}
