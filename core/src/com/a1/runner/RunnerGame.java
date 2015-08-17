package com.a1.runner;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RunnerGame extends ApplicationAdapter {

	boolean adsEnabled = false;
	int adsShowingIntervalInSec = 90;
	int lastAdsShowingTime;

	int viewportWidth = 800;
	int viewportHeight = 480;

	static GameAssets gameAssets;
	SpriteBatch batch;
	OrthographicCamera camera;
	ShapeRenderer shapeRenderer;
	BitmapFont regularFont;
	GlyphLayout glyphLayout;
    SoundManager soundManager;
    Preferences prefs;

    float iconSize = 48;
	float padding = 20;

	int wallTileSize = 32;
	int wallsMaxDistance = this.wallTileSize * 8;
	float wallsMinDistance = this.wallTileSize * 1.5f;
	int wallsMaxTilesCount = 12;
	int wallsMinTilesCount = 3;
	int wallUpdatePosX = -this.wallTileSize * 15;
	int levelPadding = wallTileSize;
	ArrayList[] walls;
	Runner runner;

	int level2 = 25;
	int level3 = 50;
	int level4 = 75;
	int level5 = 100;

    int bestScore;
	boolean soundOff;

    private int coinsCount = 6;
    ArrayList<Coin> coins;
    ArrayList<Coin> availableCoins;

    int particlesCount = 100;
    ArrayList<Particle> particles;

    HashMap<String, ControlElement> menuControlElements;
	HashMap<String, ControlElement> inGameControlElements;
    HashMap<String, ControlElement> commonControlElements;

	Vector3 touchPos = new Vector3();

	boolean inMenu = true;
	boolean inGame;
	boolean inGameOver;
    boolean playSoundInMenu = true;
	boolean isPause;
	boolean showingAds;

	private IAdsController adsController;

	public RunnerGame(IAdsController adsController){

        this.adsController = adsController;
	}
	
	@Override
	public void create () {
		// TODO: rating
		// TODO: set ads
        // TODO: google records
        // TODO: coin sound
        // TODO: song
		lastAdsShowingTime = (int)(System.currentTimeMillis() / 1000);

		// Don't load sounds if activity was already created once.
		// Otherwise it leads to errors with SoundPool after several
		// the Activity recreations.
        boolean dontLoadSoundsAgain = gameAssets != null;
		if (gameAssets == null){
            gameAssets = new GameAssets();
        }
		loadAssets(dontLoadSoundsAgain);

		initPrefs();

        soundManager = new SoundManager(gameAssets.sounds, gameAssets.musics.get("song"));
		if (soundOff)
			soundManager.off();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, viewportWidth, viewportHeight);
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

        regularFont = new BitmapFont(Gdx.files.internal("fonts/presstart.fnt"));
        regularFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        glyphLayout = new GlyphLayout();

        runner = createRunner();
        initRunner(runner);

        walls = createWalls();
		rearrangeWalls();

        coins = new ArrayList<Coin>();
        availableCoins = createCoins();

        particles = createParticles();

        menuControlElements = createMenuControlElements();
		inGameControlElements = createInGameControlElements();
        commonControlElements = createCommonControlElements();
	}

	private void initPrefs(){
		prefs = Gdx.app.getPreferences("Runner");
		if (!prefs.contains("bestScore")) {
			prefs.putInteger("bestScore", 0);
		}
		bestScore = prefs.getInteger("bestScore");

		if (!prefs.contains("sondoff")){
			prefs.putBoolean("soundoff", false);
		}
		soundOff = prefs.getBoolean("soundoff");
	}

	@Override
	public void render() {

		if (Gdx.input.justTouched()) {
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			onAction(touchPos.x, touchPos.y);
		}

		doLogicStep();

		Gdx.gl.glClearColor(0, 0.81f, 0.82f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		batch.begin();

        drawParticles();

		if (inGame) {
			drawWalls();
            drawCoins();
			drawRunner(runner);
			drawScore();
			drawControlElements(inGameControlElements);
            drawControlElements(commonControlElements);
		}
		else if (inMenu){
			drawMenu();
            drawControlElements(commonControlElements);
            if (playSoundInMenu){
                soundManager.playSound("start", 0.5f);
                playSoundInMenu = false;
            }
		}
		else if (inGameOver){
			drawGameOver();
            drawControlElements(commonControlElements);
		}

		batch.end();

		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.end();
	}

	private void loadAssets(boolean noSounds){
		HashMap<String, String> textures = new HashMap<String, String>();
		textures.put("coin", "coin.png");
        textures.put("supercoin", "supercoin.png");
		textures.put("runner1", "runner1.png");
		textures.put("runner2", "runner2.png");
		textures.put("runner3", "runner3.png");
		textures.put("tile", "tile.png");
        textures.put("tile2", "tile2.png");
        textures.put("tile3", "tile3.png");
		textures.put("tile4", "tile4.png");
		textures.put("tile5", "tile5.png");
        textures.put("particle", "particle.png");
		textures.put("pause", "pause.png");
		textures.put("play", "play.png");
        textures.put("soundon", "soundon.png");
        textures.put("soundoff", "soundoff.png");

		for (Map.Entry<String, String> entry : textures.entrySet())	{
			gameAssets.textures.put(entry.getKey(), new Texture(Gdx.files.internal("textures/" + entry.getValue())));
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
			gameAssets.sounds.put(entry.getKey(), Gdx.audio.newSound(Gdx.files.internal("sounds/" + entry.getValue())));
		}

		HashMap<String, String> musics = new HashMap<String, String>();
		sounds.put("song", "song.mp3");

		for (Map.Entry<String, String> entry : sounds.entrySet())	{
			Music music = Gdx.audio.newMusic(Gdx.files.internal("sounds/" + entry.getValue()));
			gameAssets.musics.put(entry.getKey(), music);
			music.setLooping(true);
		}
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
				w.countH = 3;
				w.boundingBox.width = this.wallTileSize * w.countH;
				w.boundingBox.height = this.wallTileSize;
				w.boundingBox.x = this.wallUpdatePosX;
				w.boundingBox.y = this.wallTileSize * 3 * l + levelPadding;
				walls[l].add(w);
			}
		}
		return walls;
	}

	private void drawWalls(){
		for (int i = 0; i < walls.length; i++) {
			for (int j = 0; j < walls[i].size(); j++){
				Wall w = (Wall)walls[i].get(j);
				drawWall(w);
			}
		}
	}

	private void drawWall(Wall w) {
		drawFigure(w);
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
				w.countH = (int)(this.wallsMinTilesCount + Math.random() * this.wallsMaxTilesCount);
				w.boundingBox.x = (int)(lw.boundingBox.x + lw.boundingBox.width + this.wallsMinDistance + Math.random() * this.wallsMaxDistance);
				w.boundingBox.width = this.wallTileSize * w.countH;
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
		r.boundingBox.width = this.wallTileSize * 1.5f;
		r.boundingBox.height = this.wallTileSize * 1.5f;
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

	private void drawRunner(Runner r){
		drawFigure(r);
	}

    private ArrayList<Coin> createCoins(){
        ArrayList<Coin> coins = new ArrayList<Coin>();
        for (int i = 0; i < coinsCount; i++) {
            Coin c = new Coin();
			c.texture = gameAssets.textures.get("coin");
            c.boundingBox.width = this.wallTileSize;
            c.boundingBox.height = this.wallTileSize;
            c.sound = gameAssets.sounds.get("coin");
            coins.add(c);
        }
        Coin superCoin = coins.get(coinsCount - 1);
        superCoin.boundingBox.height = superCoin.boundingBox.width = 40;
        superCoin.isSuper = true;
        superCoin.texture = gameAssets.textures.get("supercoin");
        return coins;
    }

    private void rearrangeCoins() {

        for (int i = 0, j = 0; i < this.walls.length; i++, j++) {
            if (this.availableCoins.size() == 0)
                break;

            j = j % availableCoins.size();
            Coin c = this.availableCoins.get(j);
            double probability = c.isSuper ? 0.0002 : 0.5;
            double v = Math.random();
            if (v < probability) {
                Wall w = (Wall)this.walls[i].get(this.walls[i].size() - 1);
                c.boundingBox.x = (float)(w.boundingBox.x + Math.random() * (w.boundingBox.width - c.boundingBox.width));
                c.boundingBox.y = w.boundingBox.y + w.boundingBox.height;
                this.availableCoins.remove(c);
                this.coins.add(c);
            }
        }

        for (int i = 0; i < this.coins.size(); i++) {
            Coin c = this.coins.get(i);
            if (c.boundingBox.x <= this.wallUpdatePosX) {
                this.coins.remove(i);
                this.availableCoins.add(c);
            }
        }
    }

    private void takeCoin(){
        for (int i = 0; i < this.coins.size(); i++) {
            Coin c = this.coins.get(i);
            if (this.runner.isNearCoin(c)) {
                this.runner.gatheredCoins+= c.isSuper ? 10 : 1;
				this.coins.remove(i);
                this.availableCoins.add(c);
                soundManager.playSound("coin", 0.4f);
				break;
            }
        }
    }

    private void drawCoins(){
        for (int i = 0; i < coins.size(); i++) {
            drawFigure(coins.get(i));
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

    private void drawParticles(){
        for (int i = 0; i < particles.size(); i++)
			drawFigure(particles.get(i));
    }

	private void drawScore(){
		String score = String.valueOf(runner.gatheredCoins);
		glyphLayout.setText(regularFont, score);
		regularFont.draw(batch, glyphLayout, (viewportWidth - glyphLayout.width) / 2, viewportHeight - padding);
	}

    private HashMap<String, ControlElement> createMenuControlElements(){

        HashMap<String, ControlElement> elems = new HashMap<String, ControlElement>();

        ControlElement start = new ControlElement();
        TextFigure t = new TextFigure();
        start.figure = t;
        t.id = "start";
        t.text = "start!";
        glyphLayout.setText(regularFont, t.text);
        t.boundingBox.x = (viewportWidth - glyphLayout.width) / 2;
        t.boundingBox.y = viewportHeight / 2 - glyphLayout.height;
        t.boundingBox.width = glyphLayout.width;
        t.boundingBox.height = glyphLayout.height;
        elems.put(t.id, start);

        return elems;
    }

	private HashMap<String, ControlElement> createInGameControlElements(){

		HashMap<String, ControlElement> elems = new HashMap<String, ControlElement>();

		ControlElement pause = new ControlElement();
        Sprite s = new Sprite();
        pause.figure = s;
		s.id = "pause";
		s.texture = gameAssets.textures.get("pause");
		s.boundingBox.width = s.boundingBox.height = iconSize;
		s.boundingBox.x = viewportWidth - iconSize;
		s.boundingBox.y = viewportHeight - iconSize - padding;
		elems.put(s.id, pause);

		float playSize = 96;
		ControlElement play = new ControlElement();
        s = new Sprite();
        play.figure = s;
		s.id = "play";
		s.texture = gameAssets.textures.get("play");
		s.boundingBox.width = s.boundingBox.height = playSize;
		s.boundingBox.x = (viewportWidth - playSize) / 2;
		s.boundingBox.y = (viewportHeight - playSize) / 2;
		s.isVisisble = false;
		elems.put(s.id, play);

		return elems;
	}

    private HashMap<String, ControlElement> createCommonControlElements(){

        HashMap<String, ControlElement> elems = new HashMap<String, ControlElement>();

        ControlElement soundon = new ControlElement();
        Sprite s = new Sprite();
        soundon.figure = s;
        s.id = "soundon";
        s.texture = gameAssets.textures.get("soundon");
        s.boundingBox.width = s.boundingBox.height = iconSize;
        s.boundingBox.x = viewportWidth - iconSize;
		s.isVisisble = !soundOff;
        elems.put(s.id, soundon);

        ControlElement soundoff = new ControlElement();
        s = new Sprite();
        soundoff.figure = s;
        s.id = "soundoff";
        s.texture = gameAssets.textures.get("soundoff");
        s.boundingBox.width = s.boundingBox.height = iconSize;
        s.boundingBox.x = viewportWidth - iconSize;
        s.isVisisble = soundOff;
        elems.put(s.id, soundoff);

        return elems;
    }

    private void drawControlElements(Map<String, ControlElement> elems){
        for (Map.Entry<String, ControlElement> entry : elems.entrySet()){
            drawFigure(entry.getValue().figure);
        }
    }

	private void drawMenu(){
        float sx = regularFont.getScaleX();
        float sy = regularFont.getScaleX();
        regularFont.getData().setScale(sx * 3.5f, sy * 3.5f);
        glyphLayout.setText(regularFont, "RUNNER");
        regularFont.draw(batch, glyphLayout, (viewportWidth - glyphLayout.width) / 2,
                viewportHeight / 2 + (viewportHeight / 2 - glyphLayout.height) / 2 + glyphLayout.height);
        regularFont.getData().setScale(sx, sy);

        drawControlElements(menuControlElements);
	}

	private void drawGameOver(){
        float sx = regularFont.getScaleX();
        float sy = regularFont.getScaleX();
        regularFont.getData().setScale(sx * 2, sy * 2);
        glyphLayout.setText(regularFont, "GAME OVER");
        regularFont.draw(batch, glyphLayout, (viewportWidth - glyphLayout.width) / 2,
                viewportHeight / 2 + (viewportHeight / 2 - glyphLayout.height) / 2 + glyphLayout.height);
        regularFont.getData().setScale(sx, sy);

        glyphLayout.setText(regularFont, "score: 9999");
        float xs = (viewportWidth - glyphLayout.width) / 2;
        float xe = (viewportWidth + glyphLayout.width) / 2;

        float y = viewportHeight / 2;
        glyphLayout.setText(regularFont, "score:");
        regularFont.draw(batch, glyphLayout, xs, y);
        String score = String.valueOf(runner.gatheredCoins);
        glyphLayout.setText(regularFont, score);
        regularFont.draw(batch, glyphLayout, xe - glyphLayout.width, y);

        y -=  glyphLayout.height * 1.5;
        glyphLayout.setText(regularFont, "best:");
        regularFont.draw(batch, glyphLayout, xs, y);
        String best = String.valueOf(bestScore);
        glyphLayout.setText(regularFont, best);
        regularFont.draw(batch, glyphLayout, xe - glyphLayout.width, y);
    }

	protected void doLogicStep() {

		if (isPause)
			return;

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
                if (bestScore < runner.gatheredCoins) {
                    bestScore = runner.gatheredCoins;
                    prefs.putInteger("bestScore", bestScore);
                    prefs.flush();
                }

				this.inGameOver = true;
				this.inGame = false;
				soundManager.stopMusic();
                soundManager.playSound("death", 0.5f);
			}

			this.runner.tick();

			runner.speed = runner.initSpeed;
			float speedDelta = 0.5f;
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

	private void onAction(float x, float y) {

		if (showingAds)
			return;

        ControlElement selectedControlElement = null;
        if (inGame) {
            selectedControlElement = getSelectedControlElement(x, y, inGameControlElements);
        }
        else if (inMenu){
            selectedControlElement = getSelectedControlElement(x, y, menuControlElements);
        }

        if (selectedControlElement == null) {
            selectedControlElement = getSelectedControlElement(x, y, commonControlElements);
        }

        if (selectedControlElement != null) {
            if (selectedControlElement.figure.id == "play") {
                isPause = false;
                inGameControlElements.get("play").figure.isVisisble = false;
                inGameControlElements.get("pause").figure.isVisisble = true;
                soundManager.playMusic();
            }
            else if (selectedControlElement.figure.id == "pause") {
                isPause = true;
                inGameControlElements.get("play").figure.isVisisble = true;
                inGameControlElements.get("pause").figure.isVisisble = false;
                soundManager.pauseMusic();
            }
            else if (selectedControlElement.figure.id == "soundoff"){
                commonControlElements.get("soundon").figure.isVisisble = true;
                commonControlElements.get("soundoff").figure.isVisisble = false;
				soundOn();
            }
            else if (selectedControlElement.figure.id == "soundon"){
                commonControlElements.get("soundon").figure.isVisisble = false;
                commonControlElements.get("soundoff").figure.isVisisble = true;
				soundOff();
            }
            else if (selectedControlElement.figure.id == "start"){
                this.inMenu = false;
                this.inGame = true;
                this.initRunner(runner);
                this.initWallsAndCoinsPositions();
                this.rearrangeWalls();
                this.rearrangeCoins();
                soundManager.playMusic();
            }
            return;
        }

		if (this.inGame) {
			this.runner.jump();
		}
		else if (this.inGameOver) {
			this.inMenu = true;
            playSoundInMenu = true;
			this.inGameOver = false;
            soundManager.playSound("start", 0.5f);
			tryShowAds();
		}
	}

	private ControlElement getSelectedControlElement(float x, float y, HashMap<String, ControlElement> elements){
		for (Map.Entry<String, ControlElement> entry : elements.entrySet()){
			ControlElement e = entry.getValue();
			if (e.figure.isVisisble && e.figure.boundingBox.contains(x, y))
				return e;
		}
		return null;
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
					showingAds = false;
					lastAdsShowingTime = (int)(System.currentTimeMillis() / 1000);
                    // Game  activity will be restarted after the ads closing so force to call dispose.
					//disposeNoSounds();
				}
			});
		}
		catch(Exception e){
			// don't fail the game.
			showingAds = false;
		}
	}

	@Override
	public void dispose() {
		dispose(false);
	}

	void dispose(boolean noSounds) {
		gameAssets.disposeNoSounds();
		batch.dispose();
	}

	void disposeNoSounds(){
		dispose(true);
	}

	private void drawFigure(Figure f){

		if (!f.isVisisble)
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

    private void  drawTextFigure(TextFigure f){
        regularFont.draw(batch, f.text, f.boundingBox.x, f.boundingBox.y + f.boundingBox.height);
    }

	private void soundOn(){
		soundManager.on();
		prefs.putBoolean("soundoff", false);
		prefs.flush();
	}

	private void soundOff(){
		soundManager.off();
		prefs.putBoolean("soundoff", true);
		prefs.flush();
	}
}
