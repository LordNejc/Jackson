package com.ok;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

/**
 * Artwork from https://goodstuffnononsense.com/about/
 * https://goodstuffnononsense.com/hand-drawn-icons/space-icons/
 */
public class nejc extends ApplicationAdapter {
	private Texture ballImg;
	private Texture playerImg;
	private Texture cocoImg;
	private Sound wof;

	private SpriteBatch batch;
	private OrthographicCamera camera;

	private Rectangle player;
	private Array<Rectangle> balls; //special LibGDX Array
	private Array<Rectangle> cocos;
	private long lastBallTime;
	private long lastCocoTime;
	private int ballsRescuedScore;
	private int playerHealth; //Starts with 100

	private BitmapFont font;

	//Values are set experimental
	private static int SPEED = 600; // pixels per second
	private static int SPEED_BALL = 200; // pixels per second
	private static int SPEED_COCO = 300; // pixels per second
	private static long CREATE_BALL_TIME = 1000000000; //ns
	private static long CREATE_COCO_TIME = 1000000000; //ns

	private void commandMoveLeft() {
		player.x -= SPEED * Gdx.graphics.getDeltaTime();
		if(player.x < 0) player.x = 0;
	}

	private void commandMoveReght() {
		player.x += SPEED * Gdx.graphics.getDeltaTime();
		if(player.x > Gdx.graphics.getWidth() - playerImg.getWidth())
			player.x = Gdx.graphics.getWidth() - playerImg.getWidth();
	}

	private void commandMoveLeftCorner() {
		player.x = 0;
	}
	private void commandMoveRightCorner() {
		player.x = Gdx.graphics.getWidth() - playerImg.getWidth();
	}

	private void commandTouched() {
		Vector3 touchPos = new Vector3();
		touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(touchPos);
		player.x = touchPos.x - playerImg.getWidth() / 2;
	}

	private void commandExitGame() {
		Gdx.app.exit();
	}

	@Override
	public void create() {

		font = new BitmapFont();
		font.getData().setScale(2);
		ballsRescuedScore = 0;
		playerHealth = 100;

		// default way to load texture
		playerImg = new Texture(Gdx.files.internal("doge64.png"));
		ballImg = new Texture(Gdx.files.internal("Ball64.png"));
		cocoImg = new Texture(Gdx.files.internal("coco128.png"));
		wof = Gdx.audio.newSound(Gdx.files.internal("wof.mp3"));

		// create the camera and the SpriteBatch
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();

		// create a Rectangle to logically represents the player
		player = new Rectangle();
		player.x = Gdx.graphics.getWidth() / 2 - playerImg.getWidth() / 2; // center the player horizontally
		player.y = 20; // bottom left corner of the player is 20 pixels above the bottom screen edge
		player.width = playerImg.getWidth();
		player.height = playerImg.getHeight();

		balls = new Array<Rectangle>();
		cocos = new Array<Rectangle>();
		//add first astronoutn and asteroid
		spawnBall();
		spawnCoco();

	}

	private void spawnBall() {
		Rectangle astronaut = new Rectangle();
		astronaut.x = MathUtils.random(0, Gdx.graphics.getWidth() - ballImg.getWidth());
		astronaut.y = Gdx.graphics.getHeight();
		astronaut.width  = ballImg.getWidth();
		astronaut.height = ballImg.getHeight();
		balls.add(astronaut);
		lastBallTime = TimeUtils.nanoTime();
	}

	private void spawnCoco() {
		Rectangle asteroid = new Rectangle();
		asteroid.x = MathUtils.random(0, Gdx.graphics.getWidth()- ballImg.getWidth());
		asteroid.y = Gdx.graphics.getHeight();
		asteroid.width = cocoImg.getWidth();
		asteroid.height = cocoImg.getHeight();
		cocos.add(asteroid);
		lastCocoTime = TimeUtils.nanoTime();
	}


	@Override
	public void render() { //runs every frame
		//clear screen
		Gdx.gl.glClearColor(0, 0, 0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		batch.setProjectionMatrix(camera.combined);

		// begin a new batch and draw the player, balls, cocos
		batch.begin();
		{ //add brackets just for intent
			batch.draw(playerImg, player.x, player.y);
			for (Rectangle asteroid : cocos) {
				batch.draw(cocoImg, asteroid.x, asteroid.y);

			}
			for (Rectangle astronaut : balls) {
				batch.draw(ballImg, astronaut.x, astronaut.y);
			}
			font.setColor(Color.YELLOW);
			font.draw(batch, "" + ballsRescuedScore, Gdx.graphics.getWidth() - 50, Gdx.graphics.getHeight() - 20);
			font.setColor(Color.GREEN);
			font.draw(batch, "" + playerHealth, 20, Gdx.graphics.getHeight() - 20);
		}
		batch.end();

		// process user input
		if(Gdx.input.isTouched()) commandTouched(); //mouse or touch screen
		if(Gdx.input.isKeyPressed(Keys.LEFT)) commandMoveLeft();
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) commandMoveReght();
		if(Gdx.input.isKeyPressed(Keys.A)) commandMoveLeftCorner();
		if(Gdx.input.isKeyPressed(Keys.S)) commandMoveRightCorner();
		if(Gdx.input.isKeyPressed(Keys.ESCAPE)) commandExitGame();

		// check if we need to create a new
		if(TimeUtils.nanoTime() - lastBallTime > CREATE_BALL_TIME) spawnBall();
		if(TimeUtils.nanoTime() - lastCocoTime > CREATE_COCO_TIME) spawnCoco();

		if (playerHealth > 0) { //is game end?
			// move and remove any that are beneath the bottom edge of
			// the screen or that hit the player.
			for (Iterator<Rectangle> iter = cocos.iterator(); iter.hasNext(); ) {
				Rectangle asteroid = iter.next();
				asteroid.y -= SPEED_COCO * Gdx.graphics.getDeltaTime();
				if (asteroid.y + cocoImg.getHeight() < 0) iter.remove();
				if (asteroid.overlaps(player)) {
					wof.play();
					playerHealth--;
				}
			}

			for (Iterator<Rectangle> iter = balls.iterator(); iter.hasNext(); ) {
				Rectangle astronaut = iter.next();
				astronaut.y -= SPEED_BALL * Gdx.graphics.getDeltaTime();
				if (astronaut.y + ballImg.getHeight() < 0) iter.remove(); //From screen
				if (astronaut.overlaps(player)) {
					wof.play();
					ballsRescuedScore++;
					if (ballsRescuedScore%10==0) SPEED_COCO+=66; //speeds up
					iter.remove(); //smart Array enables remove from Array
				}
			}
		} else { //health of player is 0 or less
			batch.begin();
			{
				font.setColor(Color.RED);
				font.draw(batch, "The END", Gdx.graphics.getHeight() / 2, Gdx.graphics.getHeight() / 2);
			}
			batch.end();
		}
	}

	@Override
	public void dispose() {
		// dispose of all the native resources
		ballImg.dispose();
		playerImg.dispose();
		wof.dispose();
		batch.dispose();
		font.dispose();
	}
}
