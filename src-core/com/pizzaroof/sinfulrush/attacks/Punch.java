package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.PerlinNoise;
import com.pizzaroof.sinfulrush.util.Utils;

/** pugno:
 *  quando l'utente preme il dito, si colpisce il nemico.
 *  Il datto fatto su un nemico aumenta all'aumentare dei colpi consecutivi andati a segno (e in rapida successione). (e il danno sarà sempre compreso tra [min, max]).
 *  La grafica del pugno è data da una sola atlas, che contiene @numPhases animazioni, una per ogni fase (se ne ha di meno, l'ultima verrà usata per tutte le successive). Le regioni dell'atlas
 *  saranno nominate "1" "2" etc..
 *  Ha anche un file info.txt cosi organizzato:
 *  minDamae maxDamage numPhases resetTime
 *  drawingWidth drawingHeight durataHit
 * */
public class Punch extends Attack {

    /**danni del pugno*/
    private int minDamage, maxDamage;

    /**danno attuale*/
    private int actualDamage;

    /**numero di fasi per raggiungere danno massimo*/
    private int numPhases;

    /**quanto tempo può passare al massimo prima di resettare il danno?*/
    private float resetTime;
    /**tempo attuale passato*/
    private float actualTime;

    /**fase attuale*/
    private int actualPhase;

    /**numero animazioni*/
    private int numAnimations;

    /**lato del quadrato che contiene il touch*/
    private static final float TOUCH_LEN = Constants.EPS * 1000;

    //ci salviamo il punto dove colpisce, cosi non dobbiamo ricrearlo ogni volta
    private Vector3 hitPoint;

    /**perlin noise, usato per ruotare "a caso" gli effetti grafici*/
    private com.pizzaroof.sinfulrush.util.PerlinNoise perlin;

    /**ha fatto danni?*/
    private boolean damageDone;

    /**@param minDamage minimo danno del pugno
     * @param maxDamage massimo danno del pugno
     * @param nPhases numero di fasi (cioè colpi consecutivi andati a segno in poco tempo) per raggiungere maxDamage
     * @param resetTime tempo dopo il quale il danno viene resettato se non si colpisce
     * @param atlas texture atlas con la grafica per i vari colpi (le regioni devono chiamarsi "1", "2", etc.)
     * @param duration durata di un singolo hit grafico
     * NB: le dimensioni grafiche del pungo possono essere settate con setDrawingWidth() e setDrawingHeight()*/
    public Punch(World2D world, Stage stage, SoundManager soundManager, int minDamage, int maxDamage, int nPhases, float resetTime, TextureAtlas atlas, float duration, String ... regionsName) {
        super(stage, world, soundManager);
        this.minDamage = minDamage;
        this.maxDamage = maxDamage;
        actualDamage = minDamage;
        this.numPhases = nPhases;
        this.resetTime = resetTime;
        actualPhase = 0;
        actualTime = 0;
        hitPoint = new Vector3();

        numAnimations = Math.min(nPhases, regionsName.length);
        for(int i=0; i<numAnimations; i++) //aggiungi animazioni
            addAnimationFromAtlas(i+1, atlas, regionsName[i], duration, Animation.PlayMode.NORMAL);

        perlin = new PerlinNoise();
    }

    public int getMinDamage() {
        return minDamage;
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);

        if(actualPhase > 0) { //ha fatto dei colpi di fila
            actualTime += delta; //salviamoci che deve continuare a colpire
            if(actualTime > resetTime) {  //se è passato troppo tempo,
                setPunchPhase(actualPhase-1);  //diminuiamo la fase
                //resetPunch();// resettiamo tutto
            }
        }
        //System.out.println(actualPhase+" "+actualDamage+" "+actualTime);

        if(isAnimationEnded()) //animazione finita... mettine una che non esiste per non stampare nientec
            setAnimation(-1);
    }


    /**callback per quando si colpisce una fixture*/
    private QueryCallback callback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if(fixture.getBody().getUserData() == null) //corpo senza user data... non ci serve
                return true;

            if(!(fixture.getBody().getUserData() instanceof Enemy)) //non ho colpito un nemico: allora non devo fare niente
                return true;
            //fixture.testPoint per vedere se è veramente dentro
            //nemico colpito
            Enemy e = (Enemy) fixture.getBody().getUserData(); //prendo il nemico
            if(e.getHp() <= 0) //nemico già morto... non vale, vediamo se c'è qualcuno vivo che ho colpito
                return true;

            Vector2 hitPoint = e.centerPosition();
            double angle = com.pizzaroof.sinfulrush.util.Utils.randDouble(0, Math.PI); //angolo a caso, parte superiore: vogliamo far spawnare i numeri solo in alto (o è difficile vederli su uno schermo... vengono coperti dal dito)
            Vector2 dir = new Vector2((float)Math.cos(angle), (float)Math.sin(angle));
            float mag = e.getWidth() * com.pizzaroof.sinfulrush.util.Utils.randFloat(0.6f, 0.7f) * Math.abs(dir.x) + e.getHeight() * Utils.randFloat(0.6f, 0.7f) * Math.abs(dir.y);  //Utils.randFloat(Math.min(e.getWidth(), e.getHeight()) * 0.5f, Math.min(e.getWidth(), e.getHeight()) * 0.8f);
            hitPoint.x += mag * dir.x;
            hitPoint.y += mag * dir.y;
            if(hitPoint.x < 0) hitPoint.x = 0;
            if(hitPoint.x > getStage().getCamera().viewportWidth) hitPoint.x = getStage().getCamera().viewportWidth - 50;
            float topY = getStage().getCamera().viewportHeight * 0.5f + getStage().getCamera().position.y;
            float bottomY = -getStage().getCamera().viewportHeight * 0.5f + getStage().getCamera().position.y;
            if(hitPoint.y < bottomY) hitPoint.y = bottomY;
            if(hitPoint.y > topY) hitPoint.y = topY - 50;
            e.takeDamage(actualDamage, hitPoint, Color.WHITE, Mission.BonusType.PUNCH); //faccio il danno

            setPunchPhase(actualPhase + 1);
            damageDone = true;

            return false;
        }
    };

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        //facciamo una query al mondo di box2d per vedere se abbiamo colpito qualcosa

        hitPoint.set(screenX, screenY, 0);
        hitPoint = stage.getCamera().unproject(hitPoint); //passo a coordinate della camera

        setRotation(perlin.noise() * 360);
        setPositionFromCenter(hitPoint.x, hitPoint.y); //posizione giusta
        setAnimation(Math.min(actualPhase+1, numAnimations)); //setta animazione corretta

        hitPoint.x /= world.getPixelPerMeter(); //passo a coordinate del mondo
        hitPoint.y /= world.getPixelPerMeter();

        //query al mondo2d
        damageDone = false;
        world.getBox2DWorld().QueryAABB(callback, hitPoint.x - TOUCH_LEN/2.f, hitPoint.y - TOUCH_LEN/2.f, hitPoint.x + TOUCH_LEN/2.f, hitPoint.y + TOUCH_LEN/2.f);

        if(damageDone)
            soundManager.punchDamage();
        else
            soundManager.punchHit();

        return false;
    }

    /**ricalcola danni del pugno in base alla fase*/
    private void computePunchDamage() {
        if(numPhases <= 1) {
            actualDamage = minDamage;
            return;
        }

        float increment = Interpolation.linear.apply((float) (actualPhase) / (float) (numPhases - 1)); //calcola fattore in [0, 1] dove 0=danno minimo 1=danno massimo, in base alla fase in cui stiamo
        actualDamage = Math.min((int) Math.ceil(minDamage + increment * (maxDamage - minDamage)), maxDamage);
    }

    private void setPunchPhase(int phase) {
        actualTime = 0;
        actualPhase = Math.max(0, Math.min(phase, numPhases-1));
        computePunchDamage();
    }

    /**fa tornare il pugno al danno minimo (o l'utente ha lisciato, oppure ha aspettato troppo)*/
    private void resetPunch() {
        actualPhase = 0;
        actualTime = 0;
        actualDamage = minDamage;
    }

    @Override
    public void onAnimationChanged(int id1, int id2) {
        //evita che cambiano dimensioni animazione
    }

    @Override
    public void resetY(float maxy) {
        setY(getY() - maxy);
    }
}
