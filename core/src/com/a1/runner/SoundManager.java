package com.a1.runner;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

import java.util.Map;

public class SoundManager {

    float commonVolume = 1f;
    boolean musicWasPlaying;
    boolean off;

    Map<String, Sound> sounds;
    Music music;

    public SoundManager(Map<String, Sound> sounds, Music music){
        this.sounds = sounds;
        this.music = music;
        music.setLooping(true);
    }

    public void set_commonVolume(float volume){
        commonVolume = volume;
    }

    public void off(){
        set_commonVolume(0);
        if (music.isPlaying()) {
            music.pause();
            musicWasPlaying = true;
        }
        music.setVolume(0);
        off = true;
    }

    public void on(){
        set_commonVolume(1);
        if (musicWasPlaying) {
            music.play();
            musicWasPlaying = false;
        }
        music.setVolume(1);
        off = false;
    }

    public void playSound(String sn){
        playSound(sn, 1);
    }

    public void playSound(String sn, float volume){
        if (off)
            return;
        float v = commonVolume * volume;
        sounds.get(sn).play(v);
    }

    public void playMusic(){
        musicWasPlaying = true;
        if (off)
            return;
        music.setVolume(commonVolume);
        music.play();
    }

    public void stopMusic(){
        music.stop();
        musicWasPlaying = false;
    }

    public void pauseMusic(){

        music.pause();
    }
}
