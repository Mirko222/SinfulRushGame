package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

/**singolo "dito" di uno scettro*/
public class SceptreSwing {

    /**punto da cui spawnare la sfera*/
    private Vector2 spawnScreenPoint;
    /**punto di destinazione per la sfera (in combinazione con lo spawn si trova la direzione)*/
    private Vector2 destScreenPoint;

    private Group effectGroup;

    private String effectPath;

    /**minimo e massimo danno (aumenta all'aumentare della distanza percorsa)*/
    private int powerMin, powerMax;

    private Sceptre sceptre;

    private static final int speed = 7;

    /**minima distanza per considerare lo swipe valido*/
    private static final float MIN_SWIPE_DISTANCE = 5;

    protected AssetManager assetManager;

    public SceptreSwing(Group effectGroup, String effectPath, AssetManager assetManager, int powerMin, int powerMax, Sceptre sceptre, Vector2 init) {
        spawnScreenPoint = new Vector2();
        destScreenPoint = new Vector2();
        initParams(init);

        this.effectGroup = effectGroup;
        this.effectPath = effectPath;
        this.powerMin = powerMin;
        this.powerMax = powerMax;
        this.sceptre = sceptre;
        this.assetManager = assetManager;
    }

    public void onTouchDown(int screenx, int screeny) {
        spawnScreenPoint.set(screenx, screeny);
        destScreenPoint.set(screenx, screeny);
    }

    public void onTouchDragged(int screenx, int screeny) {
        if(Float.isNaN(spawnScreenPoint.x))
            spawnScreenPoint.set(screenx, screeny);

        destScreenPoint.set(screenx, screeny); //lo aggiorniamo sempre per poter dare un feedback grafico sulla direzione
    }

    public void onTouchUp(int screenx, int screeny) {
        if(Float.isNaN(spawnScreenPoint.x))
            return;

        destScreenPoint.set(screenx, screeny);
        if(destScreenPoint.dst2(spawnScreenPoint) < MIN_SWIPE_DISTANCE * MIN_SWIPE_DISTANCE) //quando ri rilascia facciamo partire: però non possono essere coincidenti, o non si è selezionata nessuna destinazione
            return;

        Vector2 dir = destScreenPoint.cpy().sub(spawnScreenPoint).nor();
        dir.y *= -1; //le coordinate y sono flippate su schermo
        Vector3 tmp = sceptre.toWorldPoint((int)spawnScreenPoint.x, (int)spawnScreenPoint.y);

        com.pizzaroof.sinfulrush.attacks.SplittingPowerball ball = (SplittingPowerball) sceptre.getBallPool().obtain();
        ball.init(effectPath, new Vector2(tmp.x, tmp.y), speed, powerMin, dir, powerMax, com.pizzaroof.sinfulrush.Constants.SCEPTRE_BALL_EFFECT, sceptre.getSplitRange(), effectGroup, sceptre.getEnemiesGroup(), false, Pools.PEffectColor.NONE);

        //usiamo animazioni invece di effetti particellari
        ball.useAnimations(
                assetManager.get(Utils.sheetEffectScmlPath(effectPath), SpriterData.class),
                sceptre.getStage().getBatch(),
                0,
                0.2f,
                1,
                0.5f,
                com.pizzaroof.sinfulrush.Constants.SCEPTRE_BALL_ORIGINAL_WIDTH,
                280,
                280
        );

        ball.useSmallBallAnimations(
                com.pizzaroof.sinfulrush.Constants.SCEPTRE_BALL_ORIGINAL_WIDTH,
                140, 140,
                0, 0.2f, 1, 0.5f
        );

        ball.setSoundManager(sceptre.getSoundManager());

        if(!sceptre.canSplit())
            ball.getSpriterPlayer().setCharacterMaps(Constants.SCEPTRE_B_CHARACTER_MAPS);
        else
            ball.getSpriterPlayer().characterMaps = null;

        ball.setEvil(false); //non è malvagia
        ball.setTimescale(false); //non facciamo timescale con queste powerball: le vogliamo sempre a stessa velocità
        effectGroup.addActor(ball);

        sceptre.getSoundManager().sceptreSpawn();

        resetParams(); //resettiamo dopo aver lanciato la sfera
    }

    /*ShapeRenderer renderer = new ShapeRenderer();

    public void draw(Batch batch) {
        if(!spawnScreenPoint.epsilonEquals(destScreenPoint, Constants.EPS)) {
            batch.end();
            //renderer.setProjectionMatrix(sceptre.stage.getCamera().combined);
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            renderer.setColor(Color.BLUE);
            Vector2 tmp = spawnScreenPoint.cpy();
            Vector2 tmp2 = destScreenPoint.cpy().sub(spawnScreenPoint).nor();
            Vector2 tmp3 = tmp.cpy();
            tmp3.x += (tmp2.x * (Gdx.graphics.getHeight()/40)); tmp3.y += (tmp2.y * (Gdx.graphics.getHeight()/40));
            tmp3.y = Gdx.graphics.getHeight() - tmp3.y;
            tmp.y = Gdx.graphics.getHeight() - tmp.y;
            renderer.rectLine(tmp, tmp3, Gdx.graphics.getHeight()/100);
            renderer.end();
            batch.begin();
        }
    }*/

    public void setPowers(int min, int max) {
        this.powerMin = min;
        this.powerMax = max;
    }

    private void resetParams() {
        spawnScreenPoint.set(Float.NaN, Float.NaN);
    }

    private void initParams(Vector2 init) {
        spawnScreenPoint.set(init);
        destScreenPoint.set(init);
    }

    public void setEffectPath(String effectPath) {
        this.effectPath = effectPath;
    }
}
