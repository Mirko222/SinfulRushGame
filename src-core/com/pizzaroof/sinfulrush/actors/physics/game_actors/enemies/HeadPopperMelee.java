package com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticle;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.BufferedReader;
import java.io.IOException;

/**classe fatta apposta per il lava golem.
 * Aggiungiamo una riga con:
 * regione_testa, dimensioni_testa (in pixel), offset per far spawnare la testa (come gli offset di morte), heavy_level (indica quanto è pesante da 0 a 1)*/
public class HeadPopperMelee extends MeleeEnemy {

    public HeadPopperMelee(com.pizzaroof.sinfulrush.actors.physics.World2D world, SoundManager soundManager, Stage stage, float density, float friction, float restitution, String directory, AssetManager asset, Vector2 initPosition, Group behindGroup, Group effectGroup, Shape... shapes) {
        super(world, soundManager, stage, density, friction, restitution, directory, asset, initPosition, behindGroup, effectGroup, shapes);
    }

    private static final float HEAD_RESTITUTION = 0.1f;
    public static final float LOW_HEAD_MAX_VEL_X = 1.7f, LOW_HEAD_MIN_VEL_X = 1.f, LOW_HEAD_MIN_VEL_Y = 0.f, LOW_HEAD_MAX_VEL_Y = 2.f,
                                HIGH_HEAD_MAX_VEL_X = 1.7f, HIGH_HEAD_MIN_VEL_X = 1.f, HIGH_HEAD_MIN_VEL_Y = 1.5f, HIGH_HEAD_MAX_VEL_Y = 3.f;
    /**texture region per la testa*/
    private TextureRegion headRegion;
    /**dimensioni per la testa in pixel*/
    private Vector2 headDim;
    /**offset per la testa in pixel*/
    private Vector2 headOffset;

    private int headPoolId;

    private float minVelX, maxVelX, maxVelY, minVelY;

    public void setHeadPoolId(int id) {
        headPoolId = id;
    }

    @Override
    public void dying(Mission.BonusType deathType) {
        super.dying(deathType);
        //facciamo saltare anche la testa in aria...
        PhysicParticle pp = Pools.obtainPParticle(headPoolId, world, (int)(Math.max(headDim.x, headDim.y)/2.f), HEAD_RESTITUTION);
        pp.setColor(1, 1, 1, 1);
        float xoff = originalDirection.equals(getHorDirection()) ? headOffset.x : getDrawingWidth() - headOffset.x; //guarda se devi flippare la x
        Vector2 pos = new Vector2((getX() + xoff) / world.getPixelPerMeter(), (getY() + headOffset.y) / world.getPixelPerMeter());
        pp.setDrawingWidth(headDim.x);
        pp.setDrawingHeight(headDim.y);
        pp.init(pos, headRegion, -1, true);
        float vy = com.pizzaroof.sinfulrush.util.Utils.randFloat(minVelY, maxVelY);

        pp.getBody().setLinearVelocity(com.pizzaroof.sinfulrush.util.Utils.randFloat(minVelX, maxVelX) * com.pizzaroof.sinfulrush.util.Utils.randChoice(-1, 1), vy);
        pp.setHorDirection(getHorDirection());
        effectGroup.addActor(pp);
    }

    @Override
    protected void initFromDirectory(String directory, AssetManager asset, Stage stage) {
        super.initFromDirectory(directory, asset, stage);
        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.enemyInfoPath(directory));
            for (int i = 0; i < 5; i++) reader.readLine(); //leggo le prime 5 linee che non ci servono in questo caso
            String strs[] = reader.readLine().split(" ");

            headRegion = asset.get(com.pizzaroof.sinfulrush.util.Utils.enemyAtlasPath(directory), TextureAtlas.class).findRegion(strs[0]);
            headDim = new Vector2(Integer.parseInt(strs[1]), Integer.parseInt(strs[2]));
            headOffset = new Vector2(Integer.parseInt(strs[3]), Integer.parseInt(strs[4]));
            int heavyLevel = Integer.parseInt(strs[5]);
            switch (heavyLevel) {
                case 0:
                    minVelX = LOW_HEAD_MIN_VEL_X;
                    maxVelX = LOW_HEAD_MAX_VEL_X;
                    minVelY = LOW_HEAD_MIN_VEL_Y;
                    maxVelY = LOW_HEAD_MAX_VEL_Y;
                    break;
                case 1:
                    minVelX = HIGH_HEAD_MIN_VEL_X;
                    maxVelX = HIGH_HEAD_MAX_VEL_X;
                    minVelY = HIGH_HEAD_MIN_VEL_Y;
                    maxVelY = HIGH_HEAD_MAX_VEL_Y;
                    break;
            }

            reader.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**factory method per creare un nemico dalla directory (e altre cose ovvie)*/
    public static HeadPopperMelee createEnemy(String directory, SoundManager soundManager, AssetManager assetManager, World2D world, Stage stage, Vector2 initPosition, Group behindGroup, Group effectGroup) {
        try {
            Vector2 dim = com.pizzaroof.sinfulrush.util.Utils.enemyDrawingDimensions(directory);
            Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(Utils.enemyShapePath(directory), dim.x, dim.y, world.getPixelPerMeter());
            return new HeadPopperMelee(world, soundManager, stage, 0, 0, 0, directory, assetManager, initPosition, behindGroup, effectGroup, shapes);
        }catch(IOException e) { //non dovrebbe succedere
            e.printStackTrace();
        }
        return null;
    }
}
