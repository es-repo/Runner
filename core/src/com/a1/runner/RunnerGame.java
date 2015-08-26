package com.a1.runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class RunnerGame extends ApplicationAdapter {

	int viewportWidth = 400;
	int viewportHeight = 240;

	static GameAssets gameAssets;
	SpriteBatch batch;
	OrthographicCamera camera;
	Renderer renderer;
	BitmapFont regularFont;
	GlyphLayout glyphLayout;
    SoundManager soundManager;
    Preferences prefs;

    float iconSize = 24;
	float padding = 10;

	int leftSceneEdgePosX = -Platform.blockWidth * 15;
	int levelPadding = Platform.blockWidth;
	ArrayList[] platforms;
	Runner runner;
	Background backgroundTop;
	Background backgroundBottom;
	Sprite blackmask;
	Sprite help;

	int level2 = 25;
	int level3 = 50;
	int level4 = 75;
	int level5 = 100;

	int score;
	String scoreString;
    int bestScore;
	boolean soundOff;

	long ticks;

    int coinsCount = 6;	
    ArrayList<Coin> coins;
	ArrayList<Coin> availableCoins;

	ArrayList<Figure> touchedFigures = new ArrayList<Figure>();

	Vector3 touchPos = new Vector3();

	boolean inMenu = true;
	boolean inGame;
	boolean inGameOver;
	boolean isPause;
	boolean isHelp;

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
		// TODO: performance
		// TODO: music disappear after back button.
		// TODO: dont jump from the edge of the platform
		// TODO: runner falling from the top goes through bottom platform
		// TODO: sometimes the game slowdowns. looks like because of sounds or GC.
		// TODO: set ads
		// TODO: rating
		// TODO: pers 3rd frame shift 1 pixel head.
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
		regularFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);
		glyphLayout = new GlyphLayout();

		// LibGDX stuff.
		camera = new OrthographicCamera();
		camera.setToOrtho(false, viewportWidth, viewportHeight);
		batch = new SpriteBatch();

		renderer = new Renderer(batch, regularFont, viewportWidth, viewportHeight);

		// Create figures.

		Sprite title = new Sprite();
		title.texture = gameAssets.textures.get("title");
		title.boundingBox.width = 358;
		title.boundingBox.height = 42;
		title.boundingBox.x = (viewportWidth - title.boundingBox.width) / 2;
		title.boundingBox.y = viewportHeight / 2 + (viewportHeight / 2 - title.boundingBox.height) / 2;

		help = new Sprite();
		help.boundingBox.width = 150;
		help.boundingBox.height = 90;
		help.boundingBox.x = (viewportWidth - help.boundingBox.width) / 2;
		help.boundingBox.y = (viewportHeight - help.boundingBox.height) / 2;
		help.texture = gameAssets.textures.get("help");

		backgroundTop = new Background(gameAssets, viewportWidth, 80, "background.top");
		backgroundTop.scrollSpeed = 0.125f;
		backgroundTop.boundingBox.y = viewportHeight - backgroundTop.boundingBox.height;
		backgroundBottom = new Background(gameAssets, viewportWidth, 160, "background.bottom");

		blackmask = new Sprite();
		blackmask.boundingBox.width = viewportWidth;
		blackmask.boundingBox.height = viewportHeight;
		blackmask.texture = gameAssets.textures.get("blackmask");
		blackmask.isVisible =  true;

		runner = createRunner();
		initRunner(runner);

		platforms = createPlatforms();
		rearrangePlatforms();

		coins = createCoins();
		availableCoins = new ArrayList<Coin>();

		ArrayList<Figure> soundOnOffIcons = createSoundOnOffIcons();

		menuScene = new Scene();
		menuScene.figures.add(backgroundTop);
		menuScene.figures.add(backgroundBottom);
		menuScene.figures.add(title);
		menuScene.figures.addAll(createMenuButtons());
		menuScene.figures.addAll(soundOnOffIcons);

		gameScene = new Scene();
		gameScene.figures.add(backgroundTop);
		gameScene.figures.add(backgroundBottom);

		for (ArrayList platformsLevel : platforms)
			gameScene.figures.addAll(platformsLevel);
		gameScene.figures.addAll(coins);
		gameScene.figures.add(runner);
		ArrayList<Figure> pausePlayButtons = createPausePlayIcons();
		gameScene.figures.add(pausePlayButtons.get(0));
		gameScene.figures.add(blackmask);
		gameScene.figures.addAll(soundOnOffIcons);
		gameScene.figures.add(help);
		gameScene.figures.add(pausePlayButtons.get(1));

		gameOverScene = new Scene();
		gameOverScene.figures.add(backgroundTop);
		gameOverScene.figures.add(backgroundBottom);
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

		renderer.drawScene(currentScene);

		if (inGame) {
			drawScore();
		}
		else if (inMenu){
			//drawTitle();
		}
		else if (inGameOver){
			drawGameOver();
		}

		batch.end();

		ticks++;
	}

	private ArrayList<Button> createMenuButtons() {
		ArrayList<Button> buttons = new ArrayList<Button>();

		float buttonWidth = 180 * 0.9f;
		float buttonHeight = 30 * 0.9f;

		float y = viewportHeight / 2 - buttonHeight / 2;
		float dy = -buttonHeight * 1.25f;
		Button startButton = new Button(gameAssets.textures.get("buttons.start"), gameAssets.textures.get("buttons.start_pressed"));
		startButton.boundingBox.width = buttonWidth;
		startButton.boundingBox.height = buttonHeight;
		startButton.boundingBox.y = y;
		centerFigureHorizontally(startButton);
		startButton.setClickHandler(new ClickHandler() {
			@Override
			public void action() {
				inMenu = false;
				inGame = true;
				showHelp(true);
				help.isVisible = true;
				score = 0;
				scoreString = "0";
				initRunner(runner);
				initPlatformsAndCoinsPositions();
				rearrangePlatforms();
				rearrangeCoins();
				currentScene = gameScene;
			}
		});
		buttons.add(startButton);

		if (gameServicesEnabled) {
			y += dy;
			Button topScoresButton = new Button(gameAssets.textures.get("buttons.topscores"), gameAssets.textures.get("buttons.topscores_pressed"));
			topScoresButton.boundingBox.width = buttonWidth;
			topScoresButton.boundingBox.height = buttonHeight;
			topScoresButton.boundingBox.y = y;
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

		y += dy;
		Button rateButton = new Button(gameAssets.textures.get("buttons.rate"), gameAssets.textures.get("buttons.rate_pressed"));
		rateButton.boundingBox.width = buttonWidth;
		rateButton.boundingBox.height = buttonHeight;
		rateButton.boundingBox.y = y;
		centerFigureHorizontally(rateButton);
		rateButton.setClickHandler(new ClickHandler() {
			@Override
			public void action() {
//					if (gameServices.getSignedIn()) {
//						gameServices.showLeaderboard();
//					} else {
//						gameServices.login(true);
//					}
			}
		});
		buttons.add(rateButton);
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
				blackmask.isVisible = true;
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
				blackmask.isVisible =  false;
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

	private ArrayList[] createPlatforms(){
		int levels = 4;
		int count = 7;
		ArrayList[] platforms = new ArrayList[levels];
		for (int l = 0; l < levels; l++) {
			platforms[l] = new ArrayList();
			for (int i = 0; i < count; i++) {
				Platform p = new Platform(gameAssets, l);
				p.boundingBox.x = this.leftSceneEdgePosX;
				p.boundingBox.y = Platform.blockWidth * 3 * (levels - l - 1) + levelPadding;
				platforms[l].add(p);
			}
		}
		return platforms;
	}

	private void rearrangePlatforms() {
		for (int i = 0; i < platforms.length; i++) {
			this.rearrangePlatformsLevel(platforms[i]);
		}
	}

	private void rearrangePlatformsLevel(ArrayList platforms) {
		while (true) {
			Platform p = (Platform)platforms.get(0);
			Platform lastp = (Platform)platforms.get(platforms.size() - 1);

			if (p.boundingBox.x <= this.leftSceneEdgePosX) {
				int blocksCount = (int)(Platform.minBlocksCount + Math.random() * Platform.maxBlocksCount);
				p.setBlocksCount(blocksCount);
				p.boundingBox.x = (int)(lastp.boundingBox.x + lastp.boundingBox.width + Platform.minDistance + Math.random() * Platform.maxDistance);
				p.boundingBox.width = Platform.blockWidth * blocksCount;

				if (runner.gatheredCoins >= level2)
					p.setKind(1);
				if (runner.gatheredCoins >= level3)
					p.setKind(2);
				if (runner.gatheredCoins >= level4)
					p.setKind(3);
				if (runner.gatheredCoins >= level5)
					p.setKind(4);

				platforms.remove(0);
				platforms.add(p);
			}
			else {
				return;
			}
		}
	}

	private Runner createRunner(){
		Runner r = new Runner(gameAssets, soundManager);
		r.boundingBox.x = this.leftSceneEdgePosX;
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
			c.boundingBox.x = leftSceneEdgePosX;
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
			if (c.boundingBox.x <= this.leftSceneEdgePosX) {
				availableCoins.add(c);
			}
		}

        for (int i = 0; i < this.platforms.length; i++) {

            if (availableCoins.size() == 0)
                break;

            int ci = (int)(Math.random() * availableCoins.size());
			Coin coin = availableCoins.get(ci);
            double probability = coin.isSuper ? 0.1/*0.0002*/ : 0.5;
            double v = Math.random();
            if (v < probability) {
                Platform p = (Platform)this.platforms[i].get(this.platforms[i].size() - 1);
				coin.boundingBox.x = getCoinRandomPosition(p);
				coin.boundingBox.y = p.boundingBox.y + p.boundingBox.height;
                availableCoins.remove(coin);
            }
        }
    }

	private float getCoinRandomPosition(Platform platform){
		int s = (int)(platform.boundingBox.width / Coin.superSize);
		for (int i = 0; i < s; i++){
			int pos = (int)(Math.random() * s);
			float x = platform.boundingBox.x + pos * Coin.superSize + Coin.size / 2;
			float y = platform.boundingBox.y + platform.boundingBox.height + Coin.size / 2;
			boolean positionIsFree = true;
			for (int j = 0; j < coins.size(); j++){
				if (coins.get(j).boundingBox.contains(x, y)) {
					positionIsFree = false;
					break;
				}
			}
			if (positionIsFree)
				return platform.boundingBox.x + pos * Coin.superSize;

		}
		return leftSceneEdgePosX;
	}

    private void takeCoin(){
        for (int i = 0; i < this.coins.size(); i++) {
            Coin c = this.coins.get(i);
            if (this.runner.isNearCoin(c)) {
                this.runner.gatheredCoins+= c.isSuper ? 10 : 1;
				c.boundingBox.x = leftSceneEdgePosX;
                soundManager.playSound("coin", 0.4f);
				break;
            }
        }
    }

	private void initPlatformsAndCoinsPositions(){
		for (int i = 0; i < platforms.length; i++){
			for (int j = 0; j < platforms[i].size(); j++){
				Platform p = (Platform) platforms[i].get(j);
				p.boundingBox.x = leftSceneEdgePosX;
				p.setKind(0);
			}
		}

		for (int i = 0; i < coins.size(); i++){
			Coin c = coins.get(i);
			c.boundingBox.x = leftSceneEdgePosX;
		}
	}

	private void drawScore(){
		if (runner.gatheredCoins != score){
			score = runner.gatheredCoins;
			scoreString = String.valueOf(runner.gatheredCoins);
		}
		glyphLayout.setText(regularFont, scoreString);
		regularFont.draw(batch, glyphLayout, (viewportWidth - glyphLayout.width) / 2, viewportHeight - padding);
	}

	private void drawGameOver(){
		// TODO: fix objects allocation.
        float sx = regularFont.getScaleX();
        float sy = regularFont.getScaleX();
        regularFont.getData().setScale(sx * 1.5f, sy * 1.5f);
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

		if (isPause || isHelp)
			return;

		currentScene.tick(ticks);

		if (this.inGame) {
			for (int l = 0; l < this.platforms.length; l++) {
				for (int i = 0; i < this.platforms[l].size(); i++) {
					Platform w = (Platform)this.platforms[l].get(i);
					w.boundingBox.x -= this.runner.speed;
				}
				this.rearrangePlatforms();
			}

			for (int i = 0; i < this.coins.size(); i++) {
				this.coins.get(i).boundingBox.x -= this.runner.speed;
			}
			this.rearrangeCoins();
			this.takeCoin();

			this.runner.boundingBox.y += this.runner.velocity.y;

			Platform onPlatform = null;
			for (int l = 0; l < this.platforms.length; l++) {
				for (int i = 0; i < this.platforms[l].size(); i++) {
					Platform p = (Platform)this.platforms[l].get(i);
					if (this.runner.isOnPlatform(p)) {
						onPlatform = p;
						break;
					}
				}
				if (onPlatform != null)
					break;
			}

			if (onPlatform == null) {
				this.runner.velocity.y += this.runner.gravyAcc;
			} else {
				if (this.runner.velocity.y < 0) {
					this.runner.velocity.y = 0;
					this.runner.boundingBox.y = onPlatform.boundingBox.y + onPlatform.boundingBox.height;
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

		if (isHelp){
			showHelp(false);
			soundManager.playMusic();
			return;
		}

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

	void disposeNoSounds() {
		dispose(true);
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

	private void showHelp(boolean v){
		isHelp = v;
		help.isVisible = v;
		blackmask.isVisible = v;
	}
}
