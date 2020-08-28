package com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.brashmonkey.spriter.Entity;
import com.pizzaroof.sinfulrush.attacks.Armory;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.actors.SpriterAnimActor;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.missions.MissionDataCollector;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.BufferedReader;


/**bonus che può essere preso dall'utente*/
public class Bonus extends SpriterAnimActor {
    /**callback da eseguire per applicare l'effetto*/
    protected com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus.BonusCallback callback;

    /**contiene un riferimento al player: magari per bonus che gli aumentano la vita*/
    protected com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player;

    /**id per animazioni idle e taking... che devono esserci*/
    protected int IDLE_ANIM, TAKING_ANIM;

    /**questo bonus è già apparso sullo schermo?*/
    protected boolean hasAppeared;

    /**abbiamo già chiamato la callback?*/
    protected boolean takenCallbackCalled, disappearCallbackCalled;

    /**manteniamo un riferimento all'arsenale: magari il bonus lo vuole modificare*/
    protected Armory armory;

    /**manteniamo un riferimento alla lista di nemici: magari prendiamo bonus che li danneggiano*/
    protected Group enemiesGroup;

    /**dimensione hitbox (che in generale è diversa da dimensione sprite: sia drawing che non)*/
    protected Vector2 hitboxDims;

    /**direzione verso la quale sta fluttuando*/
    protected int floatingDir;

    /**durata floating*/
    protected float floatingDur;

    /**distanza per cui fluttuare*/
    protected float floatingDist;

    //prima volta che fluttua? dovrà fare metà distanza...
    protected boolean firstFloating;

    protected MissionDataCollector missionDataCollector;

    protected Mission.BonusType type;

    /**distanza per cui fluttuare*/
    protected static final float MIN_FLOATING_DIST = 25, MAX_FLOATING_DIST = 35;
    protected static final float MIN_FLOATING_T = 0.6f, MAX_FLOATING_T = 0.85f; //minimo e massimo tempo che deve impiegare per fluttuare

    protected SoundManager soundManager;

    public Bonus(SoundManager soundManager) {
        super();
        hitboxDims = new Vector2();
        this.soundManager = soundManager;
    }

    @Override
    public void actSkipTolerant(float delta) {
        updateSpriterPosition();
        super.actSkipTolerant(delta);

        if(!hasActions() && hasAppeared) { //ribalta floating direction
            floatingDir *= -1;
            float mul = firstFloating ? 0.5f : 1.f;
            addAction(Actions.moveBy(0, floatingDir * floatingDist * mul, floatingDur * mul));
            firstFloating = false;
        }

        if(isInCameraView())
            hasAppeared = true;
        if(hasAppeared && !isInCameraView()) //quando esce dalla camera liberalo
            removeAndFree();
    }

    /*ShapeRenderer sr = new ShapeRenderer(); //debug: mostra hitbox del bonus

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);
        batch.end();
        sr.setProjectionMatrix(batch.getProjectionMatrix());
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.rect(getX() + getDrawingWidth() / 2.f - hitboxDims.x / 2.f, getY() + getDrawingHeight()/2.f - hitboxDims.y/2.f, hitboxDims.x, hitboxDims.y);
        sr.end();
        batch.begin();
    }*/

    @Override
    public void reset() {
        super.reset();
        hasAppeared = false;
        takenCallbackCalled = false;
        disappearCallbackCalled = false;
        firstFloating = true;
        enemiesGroup = null;
        armory = null;
    }

    /**usato per inizializzare il bonus (probabilmente se lo prendi dalla pool)*/
    public void init(Vector2 centerPos, int charMapsId) {
        setSpriterAnimation(IDLE_ANIM);
        recomputeSpriterScale();
        hasAppeared = false;
        takenCallbackCalled = false;
        disappearCallbackCalled = false;
        setPositionFromCenter(centerPos);
        updateSpriterPosition();
        floatingDir = com.pizzaroof.sinfulrush.util.Utils.randChoice(-1, 1);
        floatingDur = com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_FLOATING_T, MAX_FLOATING_T);
        floatingDist = com.pizzaroof.sinfulrush.util.Utils.randFloat(MIN_FLOATING_DIST, MAX_FLOATING_DIST);
        firstFloating = true;
        spriterPlayer.characterMaps = charMapsId < 0 ? null : new Entity.CharacterMap[]{spriterPlayer.getEntity().getCharacterMap(charMapsId)};

        if(spriterPlayer != null) {
            spriterPlayer.speed = 0;
            spriterPlayer.update();
        }
    }

    @Override
    public void onSpriterAnimationEnded(int id) {
        if(id == TAKING_ANIM)
            removeAndFree();
    }

    /**metodo da chiamare quando il bonus viene preso dall'utente*/
    public void take() {
        if(getCurrentSpriterAnimation() != TAKING_ANIM) {
            setSpriterAnimation(TAKING_ANIM);
            if(missionDataCollector != null)
                missionDataCollector.updateBonusTaken(type, player);
            soundManager.bonusTaken();
        }
    }

    @Override
    public void onSpriterAnimationExecuting(int id, int act, int tot) {
        if(id == TAKING_ANIM && 3*act >= tot && !takenCallbackCalled) { // a un terzo dell'animazione di taking, prendiamo veramente l'effetto
            callback.onTaken(armory, enemiesGroup, player);
            takenCallbackCalled = true;
        }
    }

    public void setArmory(Armory armory) {
        this.armory = armory;
    }

    public void setEnemiesGroup(Group group) {
        this.enemiesGroup = group;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    /**setta la callback da eseguire quando si vuole applicare l'effetto*/
    public void setBonusCallback(BonusCallback callback) {
        this.callback = callback;
    }

    public void setHitBoxWidth(float w) {
        hitboxDims.x = w;
    }

    public void setHitBoxHeight(float h) {
        hitboxDims.y = h;
    }

    public void setMissionDataCollector(MissionDataCollector mdc) {
        missionDataCollector = mdc;
    }

    @Override
    public void removeAndFree() {
        super.removeAndFree();
        if(!disappearCallbackCalled) {
            disappearCallbackCalled = true;
            callback.onDisappear();
        }
    }

    @Override
    public Actor hit(float x, float y, boolean visible) {
        //facciamo in modo che l'hitbox funzioni...

        if(isTouchable() || !visible) { //facciamo come vuole super...
            //NB: (x, y) è relativo alle coordinate dell'attore
            //quindi il centro dello sprite è alle coordinate (drawingWidth/2, drawingHeight/2)
            if(Utils.pointInRect(x, y, //controlliamo se è nel rettangolo formato dall'hitbox... riuscendo a risalire alle coordinate del centro è facile
                    (drawingWidth - hitboxDims.x) * 0.5f,
                    (drawingHeight - hitboxDims.y) * 0.5f,
                    hitboxDims.x, hitboxDims.y))
                return this;
        }
        return null;
    }

    public void resetAnimationsId(int idle, int taking) {
        if(!spriterAnimations.containsKey(idle)) {
            addSpriterAnimation(idle, getSpriterAnimationDuration(IDLE_ANIM), Animation.PlayMode.LOOP);
            addSpriterAnimation(taking, getSpriterAnimationDuration(TAKING_ANIM), Animation.PlayMode.NORMAL);
        }
        IDLE_ANIM = idle;
        TAKING_ANIM = taking;
    }

    public void setBonusType(Mission.BonusType type) {
        this.type = type;
    }

    /**leggo bonus da una directory.
     * original_width drawing_width drawing_height width height
     * id_anim1 dur_anim1 | ... | id_animk dur_animk*/
    public void initFromFile(String directory, AssetManager asset, Stage stage) {
        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.bonusInfoPath(directory));

            String strs[] = reader.readLine().split(" "); //prima riga... varie dimensioni
            setOriginalWidth(Float.parseFloat(strs[0]));
            setDrawingWidth(Float.parseFloat(strs[1]));
            setDrawingHeight(Float.parseFloat(strs[2]));
            setWidth(Float.parseFloat(strs[3]));
            setHeight(Float.parseFloat(strs[4]));
            setHitBoxWidth(Float.parseFloat(strs[5]));
            setHitBoxHeight(Float.parseFloat(strs[6]));

            setSpriterData(asset.get(Utils.enemyScmlPath(directory)), stage.getBatch()); //setta data per spriter
            recomputeSpriterScale(); //ricalcola subito cose per spriter
            recomputeSpriterFlip();
            spriterPlayer.update();

            strs = reader.readLine().split(" "); //tutte animazioni
            for(int i=0; i<strs.length; i+=2) { //aggiungiamo animazioni
                addSpriterAnimation(Integer.parseInt(strs[i]), Float.parseFloat(strs[i + 1]), getPlayMode(i / 2));
                animationAdded(i / 2, Integer.parseInt(strs[i]));
            }

            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**aggiunta animazione numero @num con id @id*/
    public void animationAdded(int num, int id) {
        //assumiamo che la prima sia idle, la seconda taking... ma può essere ridefinito nelle sottoclassi
        if(num == 0) IDLE_ANIM = id;
        if(num == 1) TAKING_ANIM = id;
    }

    /**playmode per l'animazione @num*/
    public Animation.PlayMode getPlayMode(int num) {
        if(num == 0) return Animation.PlayMode.LOOP;
        return Animation.PlayMode.NORMAL;
    }
}
