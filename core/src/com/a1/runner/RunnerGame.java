package com.a1.runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class RunnerGame extends ApplicationAdapter {

	int viewportWidth = 400;
	int viewportHeight = 240;

	static GameAssets gameAssets;
	SpriteBatch batch;
	OrthographicCamera camera;
	ShapeRenderer shapeRenderer;
	BitmapFont regularFont;
	GlyphLayout glyphLayout;
    SoundManager soundManager;
    Preferences prefs;

    float iconSize = 24;
	float padding = 10;

	int wallUpdatePosX = -Wall.tileSize * 15;
	int levelPadding = Wall.tileSize;
	ArrayList[] walls;
	Runner runner;

	int level2 = 10;//25;
	int level3 = 20;//50;
	int level4 = 30;//75;
	int level5 = 40;//100;

    int bestScore;
	boolean soundOff;

	long ticks;

    int coinsCount = 6;	
    ArrayList<Coin> coins;
	ArrayList<Coin> availableCoins;

	int particlesCount = 0;
    ArrayList<Particle> particles;

	ArrayList<Figure> touchedFigures = new ArrayList<Figure>();

	Vector3 touchPos = new Vector3();

	boolean inMenu = true;
	boolean inGame;
	boolean inGameOver;
	boolean isPause;

	IAdsController adsController;
	boolean adsEnabled = false;
	int adsShowingIntervalInSec = 90;
	int lastAdsShowingTime;

	GameServices gameServices;
	boolean gameServicesEnabled = true;

	Scene menuScene;
	Scene gameScene;
	Scene gameOverScene;
	Scene currentScene;

	public RunnerGame(IAdsController adsController, GameServices actionResolver){

        this.adsController = adsController;
		this.gameServices = actionResolver;
		this.gameServices.setGameServicesEnabled(gameServicesEnabled);
	}
	
	@Override
	public void create () {
		// TODO: set ads
		// TODO: rating
		// TODO: credits
		lastAdsShowingTime = (int)(System.currentTimeMillis() / 1000);

		initPrefs();

		// Don't load sounds if activity was already created once.
		// Otherwise it leads to errors with SoundPool after several
		// the Activity recreations.
        boolean dontLoadSoundsAgain = gameAssets != null;
		if (gameAssets == null){
            gameAssets = new GameAssets();
        }
		gameAssets.loadAssets(dontLoadSoundsAgain);
		soundManager = new SoundManager(gameAssets.sounds, gameAssets.musics.get("song"));
		if (soundOff)
			soundManager.off();

		regularFont = new BitmapFont(Gdx.files.internal("fonts/vermin_vibes_1989.fnt"));
		//regularFont.getData().scale(0.25f);
		regularFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);
		glyphLayout = new GlyphLayout();

		// LibGDX stuff.
		camera = new OrthographicCamera();
		camera.setToOrtho(false, viewportWidth, viewportHeight);
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

		// Create figures.
		particles = createParticles();

		runner = createRunner();
		initRunner(runner);

		walls = createWalls();
		rearrangeWalls();

		coins = createCoins();
		availableCoins = new ArrayList<Coin>();

		ArrayList<Figure> soundOnOffIcons = createSoundOnOffIcons();

		menuScene = new Scene();
		menuScene.figures.addAll(particles);
		menuScene.figures.addAll(createMenuButtons());
		menuScene.figures.addAll(soundOnOffIcons);

		gameScene = new Scene();
		gameScene.figures.addAll(particles);
		for (ArrayList wallsLevel : walls)
			gameScene.figures.addAll(wallsLevel);
		gameScene.figures.addAll(coins);
		gameScene.figures.add(runner);
		gameScene.figures.addAll(createPausePlayIcons());
		gameScene.figures.addAll(soundOnOffIcons);

		gameOverScene = new Scene();
		gameOverScene.figures.addAll(particles);
		gameOverScene.figures.addAll(soundOnOffIcons);

		currentScene = menuScene;
	}

	private void initPrefs(){
		try {
			prefs = Gdx.app.getPreferences("Runner");
			if (!prefs.contains("bestScore")) {
				prefs.putInteger("bestScore", 0);
			}
			bestScore = prefs.getInteger("bestScore");

			if (!prefs.contains("sondoff")) {
				prefs.putBoolean("soundoff", false);
			}
			soundOff = prefs.getBoolean("soundoff");
		}
		catch(Exception e){
			// don't fail the game.
			e.printStackTrace();
		}
	}

	@Override
	public void render() {

		if (Gdx.input.justTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			onTouch(touchPos.x, touchPos.y);
		}

		doLogicStep();

		Gdx.gl.glClearColor(0, 0.81f, 0.82f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		batch.draw(gameAssets.textures.get("background"), 0, 0, viewportWidth, viewportHeight);

		drawScene(currentScene);

		if (inGame) {
			drawScore();
		}
		else if (inMenu){
			drawTitle();
		}
		else if (inGameOver){
			drawGameOver();
		}

		batch.end();

		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.end();

		ticks++;
	}

	private ArrayList<Button> createMenuButtons() {
		ArrayList<Button> buttons = new ArrayList<Button>();

		float buttonWidth = viewportWidth / 2.2f;
		float buttonHeight = viewportHeight / 8f;

		Button startButton = new Button(gameAssets.textures.get("buttons.start"), gameAssets.textures.get("buttons.start_pressed"));
		startButton.boundingBox.width = buttonWidth;
		startButton.boundingBox.height = buttonHeight;
		startButton.boundingBox.y = viewportHeight / 2 - startButton.boundingBox.height;
		centerFigureHorizontally(startButton);
		startButton.setClickHandler(new ClickHandler() {
			@Override
			public void action() {
				inMenu = false;
				inGame = true;
				initRunner(runner);
				initWallsAndCoinsPositions();
				rearrangeWalls();
				rearrangeCoins();
				currentScene = gameScene;
				soundManager.playMusic();
			}
		});
		buttons.add(startButton);

		if (gameServicesEnabled) {
			Button topScoresButton = new Button(gameAssets.textures.get("buttons.topscores"), gameAssets.textures.get("buttons.topscores_pressed"));
			topScoresButton.boundingBox.width = buttonWidth;
			topScoresButton.boundingBox.height = buttonHeight;
			topScoresButton.boundingBox.y = startButton.boundingBox.y - startButton.boundingBox.height * 1.2f;
			centerFigureHorizontally(topScoresButton);
			topScoresButton.setClickHandler(new ClickHandler() {
				@Override
				public void action() {
//					if (gameServices.getSignedIn()) {
//						gameServices.showLeaderboard();
//					} else {
//						gameServices.login(true);
//					}
				}
			});
			buttons.add(topScoresButton);
		}
		return buttons;
	}

	private ArrayList<Figure> createPausePlayIcons(){
		ArrayList<Figure> icons = new ArrayList<Figure>();

		final Sprite pause = new Sprite();
		final Sprite play = new Sprite();

		pause.texture = gameAssets.textures.get("pause");
		pause.boundingBox.width = pause.boundingBox.height = iconSize;
		pause.boundingBox.x = viewportWidth - iconSize;
		pause.boundingBox.y = viewportHeight - iconSize - padding;
		pause.setClickHandler(new ClickHandler() {
			@Override
			public void action() {
				isPause = true;
				play.isVisible = true;
				pause.isVisible = false;
				soundManager.pauseMusic();
			}
		});
		icons.add(pause);

		float playSize = iconSize * 2;
		play.texture = gameAssets.textures.get("play");
		play.boundingBox.width = play.boundingBox.height = playSize;
		play.boundingBox.x = (viewportWidth - playSize) / 2;
		play.boundingBox.y = (viewportHeight - playSize) / 2;
		play.isVisible = false;
		play.setClickHandler(new ClickHandler() {
			@Override
			public void action() {
				isPause = false;
				play.isVisible = false;
				pause.isVisible = true;
				soundManager.playMusic();
			}
		});
		icons.add(play);

		return icons;
	}

	private ArrayList<Figure> createSoundOnOffIcons(){
		ArrayList<Figure> icons = new ArrayList<Figure>();

		final Sprite on = new Sprite();
		final Sprite off = new Sprite();

		on.texture = gameAssets.textures.get("soundon");
		on.boundingBox.width = on.boundingBox.height = iconSize;
		on.boundingBox.x = viewportWidth - iconSize;
		on.isVisible = !soundOff;
		on.setClickHandler(new ClickHandler() {
			@Override
			public void action() {
				on.isVisible = false;
				off.isVisible = true;
				soundOff();
			}
		});
		icons.add(on);

		off.texture = gameAssets.textures.get("soundoff");
		off.boundingBox.width = off.boundingBox.height = iconSize;
		off.boundingBox.x = viewportWidth - iconSize;
		off.isVisible = soundOff;

		off.setClickHandler(new ClickHandler() {
			@Override
			public void action() {
				on.isVisible = true;
				off.isVisible = false;
				soundOn();
			}
		});
		icons.add(off);

		return icons;
	}

	private ArrayList[] createWalls(){
		int levels = 4;
		int count = 7;
		ArrayList[] walls = new ArrayList[levels];
		for (int l = 0; l < levels; l++) {
			walls[l] = new ArrayList();
			for (int i = 0; i < count; i++) {
				Wall w = new Wall();
				w.texture = gameAssets.textures.get("tile");
				w.countH = Wall.minTilesCount;
				w.boundingBox.width = Wall.tileSize * w.countH;
				w.boundingBox.height = Wall.tileSize;
				w.boundingBox.x = this.wallUpdatePosX;
				w.boundingBox.y = Wall.tileSize * 3 * l + levelPadding;
				walls[l].add(w);
			}
		}
		return walls;
	}

	private void rearrangeWalls() {
		for (int i = 0; i < walls.length; i++) {
			this.rearrangeWallsLevel(walls[i]);
		}
	}

	private void rearrangeWallsLevel(ArrayList walls) {
		while (true) {
			Wall w = (Wall)walls.get(0);
			Wall lw = (Wall)walls.get(walls.size() - 1);

			if (w.boundingBox.x <= this.wallUpdatePosX) {
				w.countH = (int)(Wall.minTilesCount + Math.random() * Wall.maxTilesCount);
				w.boundingBox.x = (int)(lw.boundingBox.x + lw.boundingBox.width + Wall.minDistance + Math.random() * Wall.maxDistance);
				w.boundingBox.width = Wall.tileSize * w.countH;
				String tn = "tile";
				if (runner.gatheredCoins >= level2)
					tn = "tile2";
				if (runner.gatheredCoins >= level3)
					tn = "tile3";
				if (runner.gatheredCoins >= level4)
					tn = "tile4";
				if (runner.gatheredCoins >= level5)
					tn = "tile5";
                w.texture = gameAssets.textures.get(tn);
				walls.remove(0);
				walls.add(w);
			}
			else {
				return;
			}
		}
	}

	private Runner createRunner(){
		Texture[] textures = {
				gameAssets.textures.get("runner1"),
				gameAssets.textures.get("runner2"),
				gameAssets.textures.get("runner3"),
				gameAssets.textures.get("runner2")};
		Runner r = new Runner(textures, soundManager);
		r.boundingBox.width = Runner.size;
		r.boundingBox.height = Runner.size;
		r.boundingBox.x = this.wallUpdatePosX;
        return r;
	}

	private void initRunner(Runner r) {
		r.boundingBox.x = r.boundingBox.width * 3;
		r.boundingBox.y = viewportHeight - r.boundingBox.height * 2;
		this.runner.velocity.y = 0;
		this.runner.speed = this.runner.initSpeed;
		r.gatheredCoins = 0;
	}

    private ArrayList<Coin> createCoins(){
        ArrayList<Coin> coins = new ArrayList<Coin>();
        for (int i = 0; i < coinsCount; i++) {
            Coin c = new Coin();
			c.texture = gameAssets.textures.get("coin");
            c.boundingBox.width = Coin.size;
            c.boundingBox.height = Coin.size;
			c.boundingBox.x = wallUpdatePosX;
            c.sound = gameAssets.sounds.get("coin");
            coins.add(c);
        }
        Coin superCoin = coins.get(coinsCount - 1);
        superCoin.boundingBox.height = superCoin.boundingBox.width = Coin.superSize;
        superCoin.isSuper = true;
        superCoin.texture = gameAssets.textures.get("supercoin");
        return coins;
    }

    private void rearrangeCoins() {

		availableCoins.clear();
		for (int i = 0; i < this.coins.size(); i++) {
			Coin c = this.coins.get(i);
			if (c.boundingBox.x <= this.wallUpdatePosX) {
				availableCoins.add(c);
			}
		}

        for (int i = 0; i < this.walls.length; i++) {

            if (availableCoins.size() == 0)
                break;

            int ci = (int)(Math.random() * availableCoins.size());
			Coin coin = availableCoins.get(ci);
            double probability = 0.5;//coin.isSuper ? 0.0002 : 0.5;
            double v = Math.random();
            if (v < probability) {
                Wall w = (Wall)this.walls[i].get(this.walls[i].size() - 1);
				coin.boundingBox.x = getCoinRandomPosition(w); //(float)(w.boundingBox.x + Math.random() * (w.boundingBox.width - coin.boundingBox.width));
				coin.boundingBox.y = w.boundingBox.y + w.boundingBox.height;
                availableCoins.remove(coin);
            }
        }
    }

	private float getCoinRandomPosition(Wall w){
		int s = (int)(w.boundingBox.width / Coin.superSize);
		for (int i = 0; i < s; i++){
			int p = (int)(Math.random() * s);
			float x = w.boundingBox.x + p * Coin.superSize + Coin.size / 2;
			float y = w.boundingBox.y + w.boundingBox.height + Coin.size / 2;
			boolean positionIsFree = true;
			for (Coin c : coins){
				if (c.boundingBox.contains(x, y)) {
					positionIsFree = false;
					break;
				}
			}
			if (positionIsFree)
				return w.boundingBox.x + p * Coin.superSize;

		}
		return wallUpdatePosX;
	}

    private void takeCoin(){
        for (int i = 0; i < this.coins.size(); i++) {
            Coin c = this.coins.get(i);
            if (this.runner.isNearCoin(c)) {
                this.runner.gatheredCoins+= c.isSuper ? 10 : 1;
				c.boundingBox.x = wallUpdatePosX;
                soundManager.playSound("coin", 0.4f);
				break;
            }
        }
    }

	private void initWallsAndCoinsPositions(){
		for (int i = 0; i < walls.length; i++){
			for (int j = 0; j < walls[i].size(); j++){
				Wall w = (Wall)walls[i].get(j);
				w.boundingBox.x = wallUpdatePosX;
			}
		}

		for (int i = 0; i < coins.size(); i++){
			Coin c = coins.get(i);
			c.boundingBox.x = wallUpdatePosX;
		}
	}

    private ArrayList<Particle> createParticles(){
        ArrayList<Particle> particles = new ArrayList<Particle>();
        for (int i = 0; i < particlesCount; i++) {
            Particle p = new Particle();
            p.texture = gameAssets.textures.get("particle");
            p.boundingBox.height = p.boundingBox.width = 1 + (float)(Math.random() * 5);
            p.boundingBox.x = (float)(wallUpdatePosX + Math.random() * (-wallUpdatePosX + viewportWidth));
            p.boundingBox.y = (float)(Math.random() * viewportHeight);
            p.z = 9 - p.boundingBox.height;
            particles.add(p);
        }
        return particles;
    }

	private void drawScore(){
		String score = String.valueOf(runner.gatheredCoins);
		glyphLayout.setText(regularFont, score);
		regularFont.draw(batch, glyphLayout, (viewportWidth - glyphLayout.width) / 2, viewportHeight - padding);
	}

	private void drawTitle(){
        float sx = regularFont.getScaleX();
        float sy = regularFont.getScaleX();
        regularFont.getData().setScale(sx * 3.5f, sy * 3.5f);
        glyphLayout.setText(regularFont, "RUNNER");
        regularFont.draw(batch, glyphLayout, (viewportWidth - glyphLayout.width) / 2,
				viewportHeight / 2 + (viewportHeight / 2 - glyphLayout.height) / 2 + glyphLayout.height);
        regularFont.getData().setScale(sx, sy);
	}

	private void drawGameOver(){
        float sx = regularFont.getScaleX();
        float sy = regularFont.getScaleX();
        regularFont.getData().setScale(sx * 2, sy * 2);
        glyphLayout.setText(regularFont, "GAME OVER");
        regularFont.draw(batch, glyphLayout, (viewportWidth - glyphLayout.width) / 2,
                viewportHeight / 2 + (viewportHeight / 2 - glyphLayout.height) / 2 + glyphLayout.height);
        regularFont.getData().setScale(sx, sy);

		String best = String.valueOf(bestScore);

        glyphLayout.setText(regularFont, "your best: " + best);
        float xs = (viewportWidth - glyphLayout.width) / 2;
        float xe = (viewportWidth + glyphLayout.width) / 2;

        float y = viewportHeight / 2;
        glyphLayout.setText(regularFont, "score:");
        regularFont.draw(batch, glyphLayout, xs, y);
        String score = String.valueOf(runner.gatheredCoins);
        glyphLayout.setText(regularFont, score);
        regularFont.draw(batch, glyphLayout, xe - glyphLayout.width, y);

        y -=  glyphLayout.height * 1.5;
        glyphLayout.setText(regularFont, "your best:");
        regularFont.draw(batch, glyphLayout, xs, y);
        glyphLayout.setText(regularFont, best);
        regularFont.draw(batch, glyphLayout, xe - glyphLayout.width, y);
    }

	void doLogicStep() {

		if (isPause)
			return;

		currentScene.tick(ticks);

		for (int i = 0; i < this.particles.size(); i++) {
			Particle p = this.particles.get(i);
			p.boundingBox.x -= this.runner.speed / p.z;
			if (p.boundingBox.x < wallUpdatePosX) {
				p.boundingBox.x = -wallUpdatePosX + viewportWidth;
			}
		}

		if (this.inGame) {
			for (int l = 0; l < this.walls.length; l++) {
				for (int i = 0; i < this.walls[l].size(); i++) {
					Wall w = (Wall)this.walls[l].get(i);
					w.boundingBox.x -= this.runner.speed;
				}
				this.rearrangeWalls();
			}

			for (int i = 0; i < this.coins.size(); i++) {
				this.coins.get(i).boundingBox.x -= this.runner.speed;
			}
			this.rearrangeCoins();
			this.takeCoin();

			this.runner.boundingBox.y += this.runner.velocity.y;

			Wall onWall = null;
			for (int l = 0; l < this.walls.length; l++) {
				for (int i = 0; i < this.walls[l].size(); i++) {
					Wall w = (Wall)this.walls[l].get(i);
					if (this.runner.isOnWall(w)) {
						onWall = w;
						break;
					}
				}
				if (onWall != null)
					break;
			}

			if (onWall == null) {
				this.runner.velocity.y += this.runner.gravyAcc;
			} else {
				if (this.runner.velocity.y < 0) {
					this.runner.velocity.y = 0;
					this.runner.boundingBox.y = onWall.boundingBox.y + onWall.boundingBox.height;
				}
			}

			float bottom = -this.runner.boundingBox.height * 3;
			if (this.runner.boundingBox.y < bottom) {
				//this.runner.boundingBox.y = viewportHeight;
                onGameOver();
			}

			runner.speed = runner.initSpeed;
			float speedDelta = 0.25f;
			if (runner.gatheredCoins >= level2)
				runner.speed = runner.initSpeed + speedDelta;
			if (runner.gatheredCoins >= level3)
				runner.speed = runner.initSpeed + speedDelta * 2;
			if (runner.gatheredCoins >= level4)
				runner.speed = runner.initSpeed + speedDelta * 3;
			if (runner.gatheredCoins >= level5)
				runner.speed = runner.initSpeed + speedDelta * 4;
		}
	}

	private void onGameOver(){

		this.inGameOver = true;
		this.inGame = false;
		soundManager.stopMusic();
		soundManager.playSound("death", 0.5f);
		currentScene = gameOverScene;

		try{
			if (bestScore < runner.gatheredCoins) {
				bestScore = runner.gatheredCoins;
				prefs.putInteger("bestScore", bestScore);
				prefs.flush();
			}

			if (gameServices.getSignedIn()){
				gameServices.submitScore(bestScore);
			}
		}
		catch (Exception e)	{
			// dont fail the game.
			e.printStackTrace();
		}
	}

	private void onTouch(float x, float y) {

		ArrayList<Figure> touchedFigures = getTouchedFigure(currentScene.figures, x, y);
		for(Figure f : touchedFigures) {
			if (f.click())
				return;
		}

		if (this.inGame) {
			this.runner.jump();
		}
		else if (this.inGameOver) {
			this.inMenu = true;
			this.inGameOver = false;
            soundManager.playSound("start", 0.5f);
			currentScene = menuScene;
			tryShowAds();
		}
	}

	private ArrayList<Figure> getTouchedFigure(ArrayList<Figure> figures, float x, float y) {
		touchedFigures.clear();
		for(Figure f : figures){
			if (f.isVisible && f.boundingBox.contains(x, y))
				touchedFigures.add(f);
		}
		return touchedFigures;
	}

	private void tryShowAds(){
		try {
			if (!adsEnabled)
				return;

			int now = (int)(System.currentTimeMillis() / 1000);
			if (now - lastAdsShowingTime < adsShowingIntervalInSec)
				return;

			adsController.showInterstitialAd(new Runnable() {
				@Override
				public void run() {
					//Gdx.app.exit();
					lastAdsShowingTime = (int)(System.currentTimeMillis() / 1000);
                    // Game  activity will be restarted after the ads closing so force to call dispose.
					//disposeNoSounds();
				}
			});
		}
		catch(Exception e){
			// don't fail the game.
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {
		dispose(false);
	}

	void dispose(boolean noSounds) {
		gameAssets.dispose(noSounds);
		batch.dispose();
	}

	void disposeNoSounds(){
		dispose(true);
	}

	private void drawScene(Scene scene){
		for (Figure f : scene.figures)
			drawFigure(f);
	}

	private void drawFigure(Figure f){

		if (!f.isVisible)
			return;

		if (f instanceof Tile)
			drawTile((Tile) f);
		else if (f instanceof Sprite)
			drawSprite((Sprite)f);
        else if (f instanceof TextFigure)
            drawTextFigure((TextFigure)f);
	}

	private void drawSprite(Sprite s){
		batch.draw(s.texture, s.boundingBox.x, s.boundingBox.y, s.boundingBox.width, s.boundingBox.height);
	}

	private void drawTile(Tile t){
		float tileWidth = t.get_tileWidth();
		for (double k = 0, x = t.boundingBox.x; k <  t.countH; k++, x += tileWidth) {
			batch.draw(t.texture, (int) x, t.boundingBox.y, tileWidth, t.boundingBox.height);
		}
	}

    private void drawTextFigure(TextFigure f){
        regularFont.draw(batch, f.text, f.boundingBox.x, f.boundingBox.y + f.boundingBox.height);
    }

	private void soundOn(){
		soundManager.on();
		try {
			prefs.putBoolean("soundoff", false);
			prefs.flush();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private void soundOff(){
		soundManager.off();
		try {
			prefs.putBoolean("soundoff", true);
			prefs.flush();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}

	private void centerFigureHorizontally(Figure f){
		f.boundingBox.x = (viewportWidth - f.boundingBox.width) / 2;
	}
}
