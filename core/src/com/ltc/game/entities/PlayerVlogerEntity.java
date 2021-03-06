package com.ltc.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Timer;
import com.ltc.game.GameScreen;

import java.util.HashMap;
import java.util.Map;

import static com.ltc.game.Constants.PIXELS_IN_METER;

/**
 * @author Velkonost
 */
public class PlayerVlogerEntity extends Actor implements InputProcessor {

    //направление движения
    enum KeysVloger {
        LEFT, RIGHT, UP, DOWN
    }

    private Map<KeysVloger, Boolean> keys = new HashMap<KeysVloger, Boolean>();

    {
        keys.put(KeysVloger.LEFT, false);
        keys.put(KeysVloger.RIGHT, false);
        keys.put(KeysVloger.UP, false);
        keys.put(KeysVloger.DOWN, false);

    }

    private boolean hasEnemyPhone;

    private int countCalls = 3;
    private int countCameras = 5;

    private boolean withCamera = false;
    private int cameraTimer;

    private long time;
    Vector2 previousPosition;
    private Texture texture;
    private Texture textureCamera;

    private World world;

    private GameScreen game;

    private Body body;

    private Fixture fixture;

    private boolean isCircleDraw = false, circleGetCoords = false;

    private int xCircle, yCircle;

    public static final float SPEED_VLOGER = 2f;

    //позиция в мире
    Vector2 position = new Vector2();

    //используется для вычисления движения
    Vector2 velocity = new Vector2();

    public PlayerVlogerEntity(Texture texture, Texture textureCamera, GameScreen game, World world, float x, float y) {
        this.texture = texture;
        this.textureCamera = textureCamera;
        this.world = world;
        this.game = game;
        previousPosition = new Vector2(getX(), getY());
        setPosition(x, y);
        position = new Vector2(x, y);


        BodyDef def = new BodyDef();
        def.position.set(x, y);
        def.type = BodyDef.BodyType.DynamicBody;

        body = world.createBody(def);
        body.setFixedRotation(true);

        final PolygonShape box = new PolygonShape();
        box.setAsBox(0.25f, 0.5f);

  //      fixture = body.createFixture(box, 1000);
      //  fixture.setUserData("vloger");

        box.dispose();

        setSize(PIXELS_IN_METER, PIXELS_IN_METER);
    }


    public void boom(boolean boom){
        if(boom) {
            Gdx.input.setInputProcessor(this);
        }else{
            Gdx.input.setInputProcessor(null);
        }
    }

    public void detach() {
        body.destroyFixture(fixture);
        world.destroyBody(body);
    }

    public Body getBody() {
        return body;
    }

    @Override
    public void draw(final Batch batch, float parentAlpha) {
        setPosition((body.getPosition().x) * PIXELS_IN_METER,
                (body.getPosition().y) * PIXELS_IN_METER);



      //  if (withCamera) {

            batch.draw(textureCamera, getX(), getY(), getWidth(), getHeight());
            Timer.schedule(new Timer.Task() {

                @Override
                public void run() {
                    cameraTimer --;
                    if(cameraTimer == 0) withCamera = false;
                }
            }, 10).run();

        //} else batch.draw(texture, getX(), getY(), getWidth(), getHeight());

        if (isCircleDraw) {
            if (circleGetCoords) {
                xCircle = (int) game.getPlayerProger().getX();
                yCircle = (int) game.getPlayerProger().getY();

                circleGetCoords = false;
            }

            Timer.schedule(new Timer.Task() {

                @Override
                public void run() {
                    time --;
                    if(time == 0) {
                        isCircleDraw = false;
                    }
                }
            }, 10).run();

            Pixmap pixmap = new Pixmap(1680, 800, Pixmap.Format.RGBA8888);
            pixmap.setColor(new Color(1, 0, 0, 0.3f));
            pixmap.fillCircle((int) game.getPlayerProger().getX() - 10, (int) game.getPlayerProger().getY() - 20, 150);
            Texture textureCircle = new Texture(pixmap);

            batch.draw(textureCircle, 0, 0);

        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.D) {
            rightPressed();
        } else if (keycode == Input.Keys.A) {
            leftPressed();
        } else if (keycode == Input.Keys.W) {
            upPressed();
        } else if (keycode == Input.Keys.S) {
            downPressed();
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == Input.Keys.D) {
            rightReleased();
        } else if (keycode == Input.Keys.A) {
            leftReleased();
        } else if (keycode == Input.Keys.W) {
            upReleased();
        } else if (keycode == Input.Keys.S) {
            downReleased();
        }
        return true;
    }
    public boolean hasMoved(){
        if(previousPosition.x != getX() || previousPosition.y != getY()){
            previousPosition.x = getX();
            previousPosition.y = getY();
            return true;
        }
        return false;
    }
    @Override
    public boolean keyTyped(char character) {
        if (character == ' ' && countCameras > 0) {

            countCameras --;
            cameraTimer = 100;
            withCamera = true;

            if (game.collisionBtwPlayers) {
                System.out.print("WIN!");
                game.myachinWin = true;
                game.game.setScreen(game.win);
                game.socket.disconnect();
            }
        } else if ( (character == 'e' || character == 'е' || character == 'у') && countCalls > 0 && game.isTelephoneCollision) {
            isCircleDraw = true;
            circleGetCoords = true;
            countCalls --;
            time = 300;
        } else if (character == 'q'  || character == 'й') {
            hasEnemyPhone = game.isHasPhone();
        }
        return true;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    //флаг устанавливаем, что движемся влево
    private void leftPressed() {
        keys.get(keys.put(KeysVloger.LEFT, true));
    }

    //флаг устанавливаем, что движемся вправо
    private void rightPressed() {
        keys.get(keys.put(KeysVloger.RIGHT, true));
    }

    //флаг устанавливаем, что движемся вверх
    private void upPressed() {
        keys.get(keys.put(KeysVloger.UP, true));
    }

    //флаг устанавливаем, что движемся вниз
    private void downPressed() {
        keys.get(keys.put(KeysVloger.DOWN, true));
    }

    //освобождаем флаги
    private void leftReleased() {
        keys.get(keys.put(KeysVloger.LEFT, false));
    }

    private void rightReleased() {
        keys.get(keys.put(KeysVloger.RIGHT, false));
    }

    private void upReleased() {
        keys.get(keys.put(KeysVloger.UP, false));
    }

    private void downReleased() {
        keys.get(keys.put(KeysVloger.DOWN, false));
    }

    public void resetWay(){
        rightReleased();
        leftReleased();
        downReleased();
        upReleased();
    }

    //в зависимости от выбранного направления движения выставляем новое направление движения для персонажа
    public void processInput() {
        if (keys.get(KeysVloger.LEFT))
            body.setLinearVelocity(-SPEED_VLOGER, body.getLinearVelocity().y);
        if (keys.get(KeysVloger.RIGHT))
            body.setLinearVelocity(SPEED_VLOGER, body.getLinearVelocity().y);
        if (keys.get(KeysVloger.UP))
            body.setLinearVelocity(body.getLinearVelocity().x, SPEED_VLOGER);
        if (keys.get(KeysVloger.DOWN))
            body.setLinearVelocity(body.getLinearVelocity().x, -SPEED_VLOGER);
        //если не выбрано направление, то стоим на месте
        if ((keys.get(KeysVloger.LEFT) && keys.get(KeysVloger.RIGHT)) || (!keys.get(KeysVloger.LEFT) && (!keys.get(KeysVloger.RIGHT))))
            body.setLinearVelocity(0, body.getLinearVelocity().y);
        if ((keys.get(KeysVloger.UP) && keys.get(KeysVloger.DOWN)) || (!keys.get(KeysVloger.UP) && (!keys.get(KeysVloger.DOWN))))
            body.setLinearVelocity(body.getLinearVelocity().x, 0);
    }
}
