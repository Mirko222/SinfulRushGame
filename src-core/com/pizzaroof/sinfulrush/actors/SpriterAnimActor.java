package com.pizzaroof.sinfulrush.actors;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.brashmonkey.spriter.Animation;
import com.brashmonkey.spriter.Mainline;
import com.brashmonkey.spriter.Player;
import com.brashmonkey.spriter.gdx.Drawer;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.util.Pair;

import java.util.HashMap;

/**attore che usa le animazioni di spriter. (può, volendo, usare anche le frame by frame).
 * Si assume che la l'origine di Spriter sia al centro dello sprite rispetto alle x e sul lato inferiore dello sprite rispetto alle y*/
public class SpriterAnimActor extends SpriteActor {
    protected Player spriterPlayer; //player per gestire uno sprite .scml
    private Drawer spriterDrawer; //drawer per il player

    /**mappa delle animazioni, con relativa durata in secondi e modalità (key=id)*/
    protected HashMap<Integer, com.pizzaroof.sinfulrush.util.Pair<Float, PlayMode>> spriterAnimations;

    /**animazione corrente di spriter*/
    private int currentSpriterAnimation;

    private boolean spAnimationEnded; //dobbiamo smettere di mandare avanti l'animazione?
    private float originalWidth; //larghezza originale dello sprite (in spriter)

    /**altezza originale in spriter.
     * NB: se negativa, viene guardata semplicemente la larghezza e si prende l'altezza in modo da mantenere le propozioni*/
    private float originalHeight;

    /**rotazione spriter in gradi*/
    private float spriterRotationDeg;

    public SpriterAnimActor(HorDirection direction) {
        super(direction);
        spriterAnimations = new HashMap<>();
        originalWidth = 1;
        currentSpriterAnimation = -1;
        spAnimationEnded = true;
        this.originalHeight = -1;
        spriterRotationDeg = 0;
    }

    public SpriterAnimActor() {
        this(HorDirection.RIGHT);
    }

    /**@param id id dell'animazione in spriter (sono tipicamente in ordine crescente da 0)
     * @param duration durata in secondi dell'animazione
     * @param playMode come eseguire animazione (supporta solo: normal e loop)*/
    public void addSpriterAnimation(int id, float duration, PlayMode playMode) {
        spriterAnimations.put(id, new Pair<>(duration, playMode));
    }

    public void setSpriterAnimation(int anim) {
        if(spriterAnimations.containsKey(anim)) { //settiamo solo quelle che abbiamo inserito
            spriterPlayer.setAnimation(anim);
            currentSpriterAnimation = anim;
            spAnimationEnded = false;
        }
        else
            currentSpriterAnimation = -1; //gli altri valori servono a invalidare
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        updateSpriterAnimation(delta);
    }

    protected void updateSpriterAnimation(float delta) {
        if(spriterPlayer != null && spriterAnimations.containsKey(currentSpriterAnimation)) {
            if(spAnimationEnded) //se l'animazione è finita, non va avanti
                spriterPlayer.speed = 0;
            else {
                spriterPlayer.speed = computeSpriterAnimationSpeed(delta);
            }

            recomputeSpriterFlip();
            recomputeSpriterScale(); //ricalcola scala se è cambiato qualcosa

            spriterPlayer.update(); //aggiorna spriter player
        }
    }


    protected int computeSpriterAnimationSpeed(float delta) {
        //sistema velocità per animazione
        float len = spriterPlayer.getAnimation().length;
        //sappiamo che l'animazione è lunga len e vogliamo eseguirla in ...v1 secondi
        //quindi in delta secondi ci muoviamo solo di delta*len/...v1
        float speed = delta * len / spriterAnimations.get(currentSpriterAnimation).v1;
        return (int) Math.ceil(speed);
    }

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);
        if(spriterPlayer != null && spriterAnimations.containsKey(currentSpriterAnimation)) {
            spriterDrawer.setColor(getColor());
            spriterDrawer.draw(spriterPlayer);
        }
    }

    /**animazione corrente per spriter player*/
    public int getCurrentSpriterAnimation() {
        return currentSpriterAnimation;
    }

    /**setta dati per l'animazione spriter*/
    public void setSpriterData(SpriterData data, Batch batch) {
        spriterPlayer = new Player(data.getSCMLData().getEntity(0));
        spriterDrawer = new Drawer(data.getLoader(), batch, null);

        //listener per il player (quando ha finito le animazioni, principalmente)
        Player.PlayerListener spriterListener = new Player.PlayerListener() {
            @Override
            public void animationFinished(Animation animation) {
                if(spriterAnimations.containsKey(animation.id)) {
                    if (spriterAnimations.get(animation.id).v2.equals(PlayMode.NORMAL)) { //animazione "normale", deve fermarsi
                        spAnimationEnded = true;
                        spriterPlayer.setTime(animation.length - 1); //l'animazione non deve ricominciare, si blocca sull'ultimo frame
                        spriterPlayer.speed = 0;
                        spriterPlayer.update();
                        onSpriterAnimationEnded(animation.id);
                    }
                    //finito un ciclo di loop... avvertiamo la callback
                    if (spriterAnimations.get(animation.id).v2.equals(PlayMode.LOOP)) {
                        onSpriterAnimationLooping(animation.id);
                    }
                }
            }

            @Override
            public void animationChanged(Animation oldAnim, Animation newAnim) {

            }

            @Override
            public void preProcess(Player player) {

            }

            @Override
            public void postProcess(Player player) {
                onSpriterAnimationExecuting(currentSpriterAnimation, player.getTime(), player.getAnimation().length);
            }

            @Override
            public void mainlineKeyChanged(Mainline.Key prevKey, Mainline.Key newKey) {

            }
        };
        spriterPlayer.addListener(spriterListener);
    }

    /**callback per quando termina un'animazione spriter*/
    protected void onSpriterAnimationEnded(int id) {
    }

    /**callback per quando un'animazione in loop sta per ricominciare il ciclo*/
    protected void onSpriterAnimationLooping(int id) {
    }

    /**callback chiamata ad ogni aggiornamento di una animazione spriter. @id = id animazione in esecuzione
     * , @actualFrame = frame attuale dell'animazione, @totFrames = numero frame totali nell'animazione*/
    protected void onSpriterAnimationExecuting(int id, int actualFrame, int totFrames) {
    }

    /**setta la larghezza originale dello sprite*/
    public void setOriginalWidth(float ow) {
        originalWidth = ow;
    }

    public void setOriginalHeight(float oh) {
        originalHeight = oh;
    }

    /**ricalcola fattore di scala, sapendo con che larghezza vogliamo stampare*/
    public void recomputeSpriterScale() {
        if(spriterPlayer != null) {
            //in base alla larghezza in cui vogliamo stampare, ridimensioniamo lo sprite
            float scaleFactor = getDrawingWidth() / originalWidth; //l'altezza verrà messa mantenendo l'aspect ratio
            float scaleFactor2 = originalHeight > 0 ? getDrawingHeight() / originalHeight : scaleFactor;
            if (Math.abs(spriterPlayer.getScale() - scaleFactor) > Constants.EPS) //in sostanza non ha la scala voluta (confronti con i float... meglio non usare ==)
                spriterPlayer.setScale(scaleFactor, scaleFactor2);
        }
    }

    public void recomputeSpriterFlip() {
        if(spriterPlayer == null) return;

        //flippiamo se sta guardando la direzione sbagliata
        //NB: flip funziona come vogliamo perchè stiamo assumendo che l'origine delle x sta al centro dello sprite
        if((!originalDirection.equals(getHorDirection()) && spriterPlayer.flippedX()>0) ||
                (originalDirection.equals(getHorDirection()) && spriterPlayer.flippedX()<0))
            spriterPlayer.flipX();
    }

    protected void updateSpriterPosition() {
        if(spriterPlayer != null)
            spriterPlayer.setPosition(getX() + getWidth()/2.f, getY()); //aumentiamo x perché in Spriter stiamo centrati su x
    }

    //aggiorniamo la x dello spriter player, quando qualcuno vuole modificare la x
    @Override
    public void setX(float x) {
        super.setX(x);
        if(spriterPlayer != null)
            spriterPlayer.setPosition(x + getWidth()/2.f, spriterPlayer.getY()); //aumentiamo x perché in Spriter stiamo centrati su x
    }

    //aggiorniamo y dello spriter player, quando qualcuno vuole modificare la y
    @Override
    public void setY(float y) {
        super.setY(y);
        if(spriterPlayer != null)
            spriterPlayer.setPosition(spriterPlayer.getX(), y);
    }

    //aggiorna tutta posizione
    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        if(spriterPlayer != null) {
            spriterPlayer.setPosition(x + getWidth()/2, y);
        }
    }

    //aggiorna posizione avendo coordinate del centro
    @Override
    public void setPositionFromCenter(float cx, float cy) {
        super.setPositionFromCenter(cx, cy);
        if(spriterPlayer != null) {
            spriterPlayer.setPosition(cx, cy - getHeight()/2.f); //x già centrata, y è troppo sopra (non consideriamo
        }
    }

    /**restituisce durata dell'animazione spriter (in secondi)*/
    protected float getSpriterAnimationDuration(int id) {
        if(spriterAnimations.containsKey(id))
            return spriterAnimations.get(id).v1;
        return -1;
    }

    /**cambia la modalità di esecuzione per l'animazione @id (di spriter)*/
    protected void setSpriterAnimationMode(int id, PlayMode mode) {
        if(spriterAnimations.containsKey(id))
            spriterAnimations.get(id).v2 = mode;
    }

    public float getOriginalWidth() {
        return originalWidth;
    }

    public float getOriginalHeight() {
        return originalHeight;
    }

    public void setSpriterRotation(float degRot) {
        if(spriterPlayer != null) {
            spriterPlayer.rotate(degRot - spriterRotationDeg);
            spriterRotationDeg = degRot;
        }
    }

    public Player getSpriterPlayer() {
        return spriterPlayer;
    }

    public float getSpriterRotationDeg() {
        return spriterRotationDeg;
    }

    /**aggiorna durata di animazione @id con dur*/
    public void setSpriterAnimationDuration(int id, float dur) {
        if(spriterAnimations.containsKey(id))
            spriterAnimations.get(id).v1 = dur;
    }

    @Override
    public void reset() {
        super.reset();
        currentSpriterAnimation = -1;
        spAnimationEnded = true;
        setSpriterRotation(0);
    }
}
