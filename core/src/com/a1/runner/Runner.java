package com.a1.runner;

import com.badlogic.gdx.graphics.Texture;

public class Runner extends Sprite {

    SoundManager soundManager;
    public float initSpeed = 7f;
    public float speed = initSpeed;
    public float gravyAcc = -0.7f;
    public float jumpAcc = 11.5f;
    public int gatheredCoins = 0;

    private Texture[] textures;
    private int currentImageIndex = 0;

    public Runner(Texture[] textures, SoundManager soundManager){
        texture = textures[0];
        this.soundManager = soundManager;
        this.textures = textures;
    }

    @Override
    public void tick(long ticks) {

        if (this.isInJumpOrInFall()) {
            this.currentImageIndex = 2;
        }
        else {
            if (ticks % 4 == 0) {
                this.currentImageIndex++;
                if (this.currentImageIndex >= this.textures.length)
                    this.currentImageIndex = 0;
            }

            if (ticks % 12 == 0){
                soundManager.playSound("step", 0.6f);
            }
        }

        this.texture = this.textures[this.currentImageIndex];
    }

    public void jump() {
        if (!this.isInJumpOrInFall()) {
            this.velocity.y = this.jumpAcc;
            soundManager.playSound("jump", 0.5f);
        }
    }

    public boolean isInJumpOrInFall() {
        return this.velocity.y != 0;
    }

    public boolean isOnWall(Wall wall) {
        return wall.boundingBox.contains(this.boundingBox.x + this.boundingBox.width / 2, this.boundingBox.y);
    }

    public boolean isNearCoin(Coin coin) {
        return this.boundingBox.overlaps(coin.boundingBox);
    }
}
