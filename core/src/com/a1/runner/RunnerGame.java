package com.a1.runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

public class RunnerGame extends ApplicationAdapter {

	static final int viewportWidth = 400;
	static final int viewportHeight = 240;

	static GameAssets gameAssets;
	SpriteBatch batch;
	OrthographicCamera camera;
	Renderer renderer;
	BitmapFont regularFont;
	GlyphLayout glyphLayout;
	SoundManager soundManager;
	Preferences prefs;

	static final float iconSize = 24;
	static final float padding = 10;

	static final int leftSceneEdgePosX = -Platform.blockWidth * Platform.maxBlocksCount * 2;
	static final int levelPadding = Platform.blockWidth;
	static final int platformsPerLevelCount = 8;
	static final int platformsLevelsCount = 4;
	ArrayList[] platforms;
	Runner runner;
	Background backgroundTop;
	Background backgroundBottom;
	Sprite blackmask;
	Sprite help;
	Sprite pause;
	Sprite resume;
	TextFigure levelMessage;
	static final String[] levelMessages = {"Level 1", "Level 2", "Level 3", "Level 4", "Level 5", "Amazing!", "Incredible!",
			"Masterpiece!", "God level!!", "No way!!!", "Are you kidding me?!"};

	QuitDialog quitDialog;

	static final int levelSwitchDelta = 25 * 4;
	int score;
	String scoreString;
	int bestScore;
	String bestScoreString;
	boolean soundOff;

	long ticks;

	static final int coinsCount = 5 * 4;
	ArrayList<Coin> coins;
	ArrayList<Coin> availableCoins;

	ArrayList<Figure> touchedFigures = new ArrayList<Figure>();

	Vector3 touchPos = new Vector3();

	boolean inMenu = true;
	boolean inGame;
	boolean inGameOver;
	boolean isPause;
	boolean isHelp;

	int currentLevel;

	ApplicationController appControler;
	final IAdsController adsController;
	static final boolean adsEnabled = true;
	static final int adsShowingIntervalInSec = 90;
	int lastAdsShowingTime;
	int gameSessionsSinceLastAdsShowing;
	int minGameSessionsToShowAds = 3;
	boolean adsLoading;
	EventHandler adsLoadedHandler;

	GameServices gameServices;
	static final boolean gameServicesEnabled = true;

	Scene menuScene;
	Scene gameScene;
	Scene gameOverScene;
	Scene currentScene;

	public RunnerGame(ApplicationController appControler, IAdsController adsController, GameServices actionResolver){
		this.appControler = appControler;
		this.adsController = adsController;
		this.gameServices = actionResolver;
		this.gameServices.setGameServicesEnabled(gameServicesEnabled);
	}

	@Override
	public void create () {

		this.adsLoadedHandler = new EventHandler() {
			@Override
			public void action(int value) {
				adsLoading = false;
				if (value == 0) {
					try {
						adsController.showInterstitialAd();
					}
					catch (Exception e){
						e.printStackTrace();
					}
				}
			}
		};

		// TODO: top scores
		// TODO: test on all android versions
		// TODO: set real ads
		// TODO: turn on image ads
		// TODO: figure out descirption
		// TODO: update screens
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
		float sx = regularFont.getScaleX();
		float sy = regularFont.getScaleX();
		regularFont.getData().setScale(sx * 0.95f, sy * 0.95f);
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

		quitDialog = new QuitDialog(gameAssets, viewportWidth, viewportHeight);
		quitDialog.setCloseHandler(new EventHandler() {
			@Override
			public void action(int value) {
				if (value == 1)
					appControler.killApp();
				else if (value == 0 && !inGame){
					resumeGame();
				}
			}
		});

		blackmask = new Sprite();
		blackmask.boundingBox.width = viewportWidth;
		blackmask.boundingBox.height = viewportHeight;
		blackmask.texture = gameAssets.textures.get("blackmask");
		blackmask.isVisible = false;

		levelMessage = new TextFigure(regularFont);
		levelMessage.boundingBox.y = 50;
		levelMessage.color = new Color(1.0f, 1.0f, 0.7f, 1.0f);

		runner = createRunner();

		platforms = createPlatforms();

		coins = createCoins();
		availableCoins = new ArrayList<Coin>();

		ArrayList<Figure> soundOnOffIcons = createSoundOnOffIcons();

		menuScene = new Scene();
		menuScene.figures.add(backgroundTop);
		menuScene.figures.add(backgroundBottom);
		menuScene.figures.add(title);
		menuScene.figures.addAll(createMenuButtons());
		menuScene.figures.add(blackmask);
		menuScene.figures.addAll(soundOnOffIcons);
		menuScene.figures.add(quitDialog);

		gameScene = new Scene();
		gameScene.figures.add(backgroundTop);
		gameScene.figures.add(backgroundBottom);

		for (ArrayList platformsLevel : platforms)
			gameScene.figures.addAll(platformsLevel);
		gameScene.figures.addAll(coins);
		gameScene.figures.add(runner);
		ArrayList<Figure> pausePlayButtons = createPauseResumeIcons();
		gameScene.figures.add(pausePlayButtons.get(0));
		gameScene.figures.add(levelMessage);
		gameScene.figures.add(blackmask);
		gameScene.figures.addAll(soundOnOffIcons);
		gameScene.figures.add(help);
		gameScene.figures.add(pausePlayButtons.get(1));
		gameScene.figures.add(quitDialog);

		Button okButton = new Button(gameAssets.textures.get("buttons.ok"), gameAssets.textures.get("buttons.ok_pressed"));
		okButton.boundingBox.width = 87 * 0.9f;
		okButton.boundingBox.height = 30 * 0.9f;
		okButton.boundingBox.x = (viewportWidth - okButton.boundingBox.width) / 2;
		okButton.boundingBox.y = 25;
		okButton.setClickHandler(new EventHandler() {
			@Override
			public void action(int value) {
				inMenu = true;
				inGameOver = false;
				soundManager.playSound("start", 0.3f);
				currentScene = menuScene;
				tryLoadAndShowAds();
			}
		});

		gameOverScene = new Scene();
		gameOverScene.figures.add(backgroundTop);
		gameOverScene.figures.add(backgroundBottom);
		gameOverScene.figures.add(okButton);
		gameOverScene.figures.add(blackmask);
		gameOverScene.figures.addAll(soundOnOffIcons);
		gameOverScene.figures.add(quitDialog);

		currentScene = menuScene;

		soundManager.playSound("start", 0.3f);

		Gdx.input.setCatchBackKey(true);
	}

	private void initPrefs(){
		try {
			prefs = Gdx.app.getPreferences("Runner");
			if (!prefs.contains("bestScore")) {
				prefs.putInteger("bestScore", 0);
			}
			bestScore = prefs.getInteger("bestScore");
			bestScoreString = String.valueOf(bestScore);

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

		Gdx.gl.glClearColor(0.25f, 0.75f, 0.75f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		if (adsLoading)
		{
			drawLoading();
			batch.end();
			return;
		}

		renderer.drawScene(currentScene);

		if (!quitDialog.isVisible) {
			if (inMenu) {
				drawCredits();
			}
			if (inGame) {
				drawScore();
			} else if (inGameOver) {
				drawGameOver();
			}
		}

		batch.end();

		doLogicStep();

		if (Gdx.input.isKeyPressed(Input.Keys.BACK)){
			if (!quitDialog.isVisible) {
				pauseGame();
				quitDialog.show();
			}
		}

		if (Gdx.input.justTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			onTouch(touchPos.x, touchPos.y);
		}

		ticks++;
	}

	private void drawLoading(){
		glyphLayout.setText(regularFont, "Loading...");
		regularFont.draw(batch, glyphLayout, (viewportWidth - glyphLayout.width) / 2, (viewportHeight + glyphLayout.height) / 2);
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
		startButton.setClickHandler(new EventHandler() {
			@Override
			public void action(int value) {
				inMenu = false;
				inGame = true;
				showHelp(true);
				help.isVisible = true;
				score = 0;
				scoreString = "0";
				currentLevel = -1;
				initPlatformsAndCoinsPositions();
				initRunner(runner);
				rearrangePlatforms(true);
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
			topScoresButton.setClickHandler(new EventHandler() {
				@Override
				public void action(int value) {
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
		rateButton.setClickHandler(new EventHandler() {
			@Override
			public void action(int value) {
				appControler.openAppMarketPage();
			}
		});
		buttons.add(rateButton);
		return buttons;
	}

	private ArrayList<Figure> createPauseResumeIcons(){
		ArrayList<Figure> icons = new ArrayList<Figure>();

		pause = new Sprite();
		resume = new Sprite();

		pause.texture = gameAssets.textures.get("pause");
		pause.boundingBox.width = pause.boundingBox.height = iconSize;
		pause.boundingBox.x = viewportWidth - iconSize;
		pause.boundingBox.y = viewportHeight - iconSize - padding;
		pause.setClickHandler(new EventHandler() {
			@Override
			public void action(int value) {
				pauseGame();
			}
		});
		icons.add(pause);

		float playSize = iconSize * 2;
		resume.texture = gameAssets.textures.get("play");
		resume.boundingBox.width = resume.boundingBox.height = playSize;
		resume.boundingBox.x = (viewportWidth - playSize) / 2;
		resume.boundingBox.y = (viewportHeight - playSize) / 2;
		resume.isVisible = false;
		resume.setClickHandler(new EventHandler() {
			@Override
			public void action(int value) {
				resumeGame(); }
		});
		icons.add(resume);

		return icons;
	}
	
	private void pauseGame(){
		blackmask.isVisible = true;
		isPause = true;
		resume.isVisible = true;
		pause.isVisible = false;
		soundManager.pauseMusic();
	}

	private void resumeGame(){
		blackmask.isVisible =  false;
		isPause = false;
		resume.isVisible = false;
		pause.isVisible = true;
		if (inGame)
			soundManager.playMusic();
	}

	private ArrayList<Figure> createSoundOnOffIcons(){
		ArrayList<Figure> icons = new ArrayList<Figure>();

		final Sprite on = new Sprite();
		final Sprite off = new Sprite();

		on.texture = gameAssets.textures.get("soundon");
		on.boundingBox.width = on.boundingBox.height = iconSize;
		on.boundingBox.x = viewportWidth - iconSize;
		on.isVisible = !soundOff;
		on.setClickHandler(new EventHandler() {
			@Override
			public void action(int value) {
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

		off.setClickHandler(new EventHandler() {
			@Override
			public void action(int value) {
				on.isVisible = true;
				off.isVisible = false;
				soundOn();
			}
		});
		icons.add(off);

		return icons;
	}

	private ArrayList[] createPlatforms(){
		ArrayList[] platforms = new ArrayList[platformsLevelsCount];
		for (int l = 0; l < platformsLevelsCount; l++) {
			platforms[l] = new ArrayList();
			for (int i = 0; i < platformsPerLevelCount; i++) {
				Platform p = new Platform(gameAssets, l);
				p.boundingBox.x = this.leftSceneEdgePosX;
				p.boundingBox.y = Platform.blockWidth * 3 * (platformsLevelsCount - l - 1) + levelPadding;
				platforms[l].add(p);
			}
		}
		return platforms;
	}

	private void initPlatformsAndCoinsPositions(){

		for (int i = 0; i < platformsLevelsCount; i++){
			for (int j = 0; j < platformsPerLevelCount; j++){
				Platform p = (Platform) platforms[i].get(j);
				p.setBlocksCount(Platform.minBlocksCount);
				p.boundingBox.x = leftSceneEdgePosX;
				p.setKind(0);
			}
		}

		for (int i = 0; i < coinsCount; i++){
			Coin c = coins.get(i);
			c.boundingBox.x = leftSceneEdgePosX;
		}
	}

	private void rearrangePlatforms(boolean forStart) {
		for (int i = 0; i < platformsLevelsCount; i++) {
			this.rearrangePlatformsLevel(platforms[i], forStart && i == 0);
		}
	}

	private void rearrangePlatformsLevel(ArrayList platforms, boolean forStart) {
		int i = 0;
		while (true) {
			Platform p = (Platform)platforms.get(0);
			Platform lastp = (Platform)platforms.get(platformsPerLevelCount - 1);

			if (p.boundingBox.x <= this.leftSceneEdgePosX) {
				int blocksCount = forStart
						? Platform.maxBlocksCount * 2
						: (int)(Platform.minBlocksCount + Math.random() * Platform.maxBlocksCount);
				p.setBlocksCount(blocksCount);
				double d = Platform.minDistance + Math.random() * Platform.maxDistance;
				p.boundingBox.x = forStart ? 0 : (int)(lastp.boundingBox.x + lastp.boundingBox.width + d);
				int k = ((runner.gatheredCoins / levelSwitchDelta)) % 9;
				if (k > 4) k = 9 - k;
				p.setKind(k);
				platforms.remove(0);
				platforms.add(p);
			}
			else {
				return;
			}
			i++;
			forStart = false;
		}
	}

	private Runner createRunner(){
		Runner r = new Runner(gameAssets, soundManager);
		r.boundingBox.x = this.leftSceneEdgePosX;
		return r;
	}

	private void initRunner(Runner r) {
		r.boundingBox.x = r.boundingBox.width * 3;
		Platform p = (Platform)platforms[0].get(0);
		r.boundingBox.y = p.boundingBox.y + p.boundingBox.height;
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
		superCoin.sound = gameAssets.sounds.get("supercoin");
		superCoin.isSuper = true;
		superCoin.texture = gameAssets.textures.get("supercoin");
		return coins;
	}

	private void rearrangeCoins() {

		availableCoins.clear();
		for (int i = 0; i < coinsCount; i++) {
			Coin c = this.coins.get(i);
			if (c.boundingBox.x <= this.leftSceneEdgePosX) {
				availableCoins.add(c);
			}
		}

		for (int i = 0; i < platformsLevelsCount; i++) {

			if (availableCoins.size() == 0)
				break;

			int ci = (int)(Math.random() * availableCoins.size());
			Coin coin = availableCoins.get(ci);
			double probability = coin.isSuper ? 0.002 : 0.5;
			double v = Math.random();
			if (v < probability) {
				Platform p = (Platform)this.platforms[i].get(platformsPerLevelCount - 1);
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
			for (int j = 0; j < coinsCount; j++){
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
		for (int i = 0; i < coinsCount; i++) {
			Coin c = this.coins.get(i);
			if (this.runner.isNearCoin(c)) {
				this.runner.gatheredCoins+= c.isSuper ? 10 : 1;
				c.boundingBox.x = leftSceneEdgePosX;
				soundManager.playSound(c.sound, 0.35f);
				break;
			}
		}
	}

	private void drawCredits(){
		float sx = regularFont.getScaleX();
		float sy = regularFont.getScaleX();
		regularFont.getData().setScale(sx * 0.35f, sy * 0.35f);
		glyphLayout.setText(regularFont, "Music by 8-bit Ninjas");
		regularFont.draw(batch, glyphLayout, 2, glyphLayout.height + 3);
		regularFont.getData().setScale(sx, sy);
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

		float sx = regularFont.getScaleX();
		float sy = regularFont.getScaleX();
		regularFont.getData().setScale(sx * 1.5f, sy * 1.5f);
		glyphLayout.setText(regularFont, "GAME OVER");
		regularFont.draw(batch, glyphLayout, (viewportWidth - glyphLayout.width) / 2,
				viewportHeight / 2 + (viewportHeight / 2 - glyphLayout.height) / 2 + glyphLayout.height);

		regularFont.getData().setScale(sx, sy);
		glyphLayout.setText(regularFont, "your best: ");
		float width = glyphLayout.width;
		glyphLayout.setText(regularFont, bestScoreString);
		width += glyphLayout.width;
		float xs = (viewportWidth - width) / 2;
		float xe = (viewportWidth + width) / 2;

		float y = viewportHeight / 2 + glyphLayout.height / 2;
		glyphLayout.setText(regularFont, "score:");
		regularFont.draw(batch, glyphLayout, xs, y);
		String score = String.valueOf(runner.gatheredCoins);
		glyphLayout.setText(regularFont, score);
		regularFont.draw(batch, glyphLayout, xe - glyphLayout.width, y);

		y -=  glyphLayout.height * 1.5;
		glyphLayout.setText(regularFont, "your best:");
		regularFont.draw(batch, glyphLayout, xs, y);
		glyphLayout.setText(regularFont, bestScoreString);
		regularFont.draw(batch, glyphLayout, xe - glyphLayout.width, y);
	}

	void doLogicStep() {

		if (quitDialog.isVisible) {
			quitDialog.tick(ticks);
			return;
		}

		if (isPause || isHelp)
			return;

		currentScene.tick(ticks);

		if (this.inGame) {
			for (int l = 0; l < platformsLevelsCount; l++) {
				for (int i = 0; i < platformsPerLevelCount; i++) {
					Platform w = (Platform)this.platforms[l].get(i);
					w.boundingBox.x -= this.runner.speed;
				}
			}
			this.rearrangePlatforms(false);

			for (int i = 0; i < coinsCount; i++) {
				this.coins.get(i).boundingBox.x -= this.runner.speed;
			}
			this.rearrangeCoins();
			this.takeCoin();

			this.runner.boundingBox.y += this.runner.velocity.y;

			Platform onPlatform = null;
			for (int l = 0; l < platformsLevelsCount; l++) {
				for (int i = 0; i < platformsPerLevelCount; i++) {
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

			float speedDelta = 0.25f;
			int l = runner.gatheredCoins / levelSwitchDelta;
			if (l != currentLevel){
				currentLevel = l;
				setLevelMessage(currentLevel);
			}

			if (l > 4) l = 4;
			runner.speed = runner.initSpeed + l * speedDelta;
		}
	}

	private void onGameOver(){

		this.inGameOver = true;
		this.inGame = false;
		soundManager.stopMusic();
		soundManager.playSound("death", 0.3f);
		currentScene = gameOverScene;

		try{
			if (bestScore < runner.gatheredCoins) {
				bestScore = runner.gatheredCoins;
				bestScoreString = String.valueOf(bestScore);
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

		if (isHelp && !quitDialog.isVisible){
			showHelp(false);
			soundManager.playMusic();
			return;
		}

		touchedFigures.clear();
		getTouchedFigure(currentScene.figures, x, y, touchedFigures);
		int s = touchedFigures.size();
		for(int i = 0; i < s; i++) {
			if (touchedFigures.get(i).click())
				return;
		}

		if (this.inGame && !isPause) {
			this.runner.jump();
		}
	}

	private void getTouchedFigure(ArrayList<Figure> figures, float x, float y,  ArrayList<Figure> touchedFigures) {
		for(int i = figures.size() - 1; i >= 0; i--){
			Figure f = figures.get(i);
			if (f.isVisible) {
				if (f instanceof ComposedFigure)
					getTouchedFigure(((ComposedFigure) f).figures, x - f.boundingBox.x, y - f.boundingBox.y, touchedFigures);
				else if (f.boundingBox.contains(x, y))
					touchedFigures.add(f);
			}
		}
	}

	private void tryLoadAndShowAds(){
		try {
			if (!adsEnabled)
				return;

			gameSessionsSinceLastAdsShowing++;
			if (gameSessionsSinceLastAdsShowing < minGameSessionsToShowAds)
				return;

			int now = (int)(System.currentTimeMillis() / 1000);
			if (now - lastAdsShowingTime < adsShowingIntervalInSec)
				return;

			adsLoading = true;
			adsController.requestInterstitialAdLoading(adsLoadedHandler);
			gameSessionsSinceLastAdsShowing = 0;
			lastAdsShowingTime = (int)(System.currentTimeMillis() / 1000);
		}
		catch(Exception e){
			adsLoading = false;
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

	private void setLevelMessage(int level){
		if (level >= levelMessages.length)
			return;
		levelMessage.setText(levelMessages[level], glyphLayout);
		levelMessage.centerAlign(viewportWidth, viewportHeight, true, false);
		levelMessage.setBlinking(60, 1);
	}
}
