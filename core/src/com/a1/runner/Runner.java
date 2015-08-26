package com.a1.runner;

import com.badlogic.gdx.graphics.Texture;

public class Runner extends Sprite {

    public static int size = 24;

    SoundManager soundManager;
    public float initSpeed = 4f;
    public float speed = initSpeed;
    public float gravyAcc = -0.35f;
    public float jumpAcc = 5.525f;
    public int gatheredCoins = 0;

    private Texture[] textures;
    private Texture fallTexture;
    private Texture jumpTexture;
    private int currentImageIndex = 0;

    public Runner(GameAssets assets, SoundManager soundManager){

        Texture[] textures = {
                assets.textures.get("runner.step1"),
                assets.textures.get("runner.step2"),
                assets.textures.get("runner.step3"),
                assets.textures.get("runner.step2")};
        fallTexture = assets.textures.get("runner.fall");
        jumpTexture = assets.textures.get("runner.jump");
        this.textures = textures;
        boundingBox.width = Runner.size;
        boundingBox.height = Runner.size;
        texture = textures[0];
        this.soundManager = soundManager;
    }

    @Override
    public void tick(long ticks) {

        if (this.isInJump()) {
            this.currentImageIndex = 2;
            texture = jumpTexture;
        }
        else if (this.isInFall()){
            texture = fallTexture;
        }
        else {
            if (ticks % 4 == 0) {
                this.currentImageIndex++;
                if (this.currentImageIndex >= this.textures.length)
                    this.currentImageIndex = 0;
            }
            this.texture = this.textures[this.currentImageIndex];

            if (ticks % 12 == 0){
                soundManager.playSound("step", 0.6f);
            }
        }


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

    public boolean isInJump() {
        return this.velocity.y > 0.001;
    }

    public boolean isInFall() {
        return this.velocity.y < 0;
    }

    public boolean isOnPlatform(Platform platform) {
        return platform.boundingBox.contains(this.boundingBox.x + 1, this.boundingBox.y) ||
                platform.boundingBox.contains(this.boundingBox.x + boundingBox.width - 1, this.boundingBox.y);
    }

    public boolean isNearCoin(Coin coin) {
        return this.boundingBox.overlaps(coin.boundingBox);
    }
}
