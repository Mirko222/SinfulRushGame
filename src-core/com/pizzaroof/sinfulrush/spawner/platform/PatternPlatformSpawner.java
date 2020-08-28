package com.pizzaroof.sinfulrush.spawner.platform;

import com.badlogic.gdx.assets.AssetManager;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.spawner.platform.pattern.Pattern;
import com.pizzaroof.sinfulrush.spawner.platform.pattern.Pattern1;
import com.pizzaroof.sinfulrush.spawner.platform.pattern.Pattern2;
import com.pizzaroof.sinfulrush.spawner.platform.pattern.PlatformInfo;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**è in sostanza un platform spawner uniforme, ma quando si va verso il basso si cerca di mettere
 * le piattaforme più vicine creando pattern pseudo-casuali, sfruttando la creazione di più piattaforme
 * insieme (batch) piuttosto che una piattaforma alla volta*/
public class PatternPlatformSpawner extends UniformPlatformSpawner {

    /**id del pattern attuale*/
    private int patternId;

    /**probabilità di usare un pattern*/
    private float patternProb;

    /**possibili piattaforme da usare nei pattern*/
    private ArrayList<String> possiblePatternPad;
    /**piattaforme possibili per i pattern, ma in un set, per fare ricerche veloci*/
    private HashSet<String> setPatternPad;

    /**possibili pattern che si concatenano con la situazione attuale*/
    private ArrayList<Integer> possiblePatternsId;

    /**tutti pattern che possono essere usati*/
    private ArrayList<Pattern> patterns;

    /** @param upDirection   deve generarli andando verso l'alto o verso il basso */
    public PatternPlatformSpawner(boolean upDirection, AssetManager assetManager, float viewportWidth, World2D world, float patternProb) {
        super(upDirection, assetManager, viewportWidth, world);
        possiblePatternPad = new ArrayList<>();
        this.patternProb = patternProb;
        possiblePatternsId = new ArrayList<>();
        patternId = -1;
        patterns = new ArrayList<>();
        setPatternPad = new HashSet<>();
        initPatterns(); //crea patterns disponibili
    }

    /**genera tipo di piattaforma per la nuova piattaforma*/
    @Override
    protected String generateNextPlatformType() {
        if(!patternInExec()) //non stiamo nel corso di un pattern... facciamo come sempre
            return super.generateNextPlatformType();
        //altrimenti prendiamone uno in maniera uniforme, ma da quelli che possiamo usare per i pattern
        return possiblePatternPad.get(rand.nextInt(possiblePatternPad.size()));
    }

    @Override
    public Platform generateNextPlatform() {
        if(upDirection) //se stiamo andando verso l'alto, usiamo uniform platform spawner
            return super.generateNextPlatform();

        if(patternInExec()) //abbiamo ancora qualcosa da creare che sta nel pattern
            return continuePattern();

        if(rand.nextFloat() < patternProb && getLastCreatedPlatform() != null && wantUsePattern()) { //lanciamo una moneta e decidiamo se fare un pattern o generazione solita (la prima piattaforma non è del pattern)
            chooseNewPattern(); //crea pattern
            return continuePattern(); //inizia ad estrarre cose dal pattern
        }

        //facciamo generazione normale
        return super.generateNextPlatform();
    }

    /**scegli un nuovo pattern*/
    private void chooseNewPattern() {
        possiblePatternsId.clear();
        //aggiungi i pattern possibili alla lista
        for(int i=0; i<patterns.size(); i++)
            if(patterns.get(i).isPossible(getLastCreatedPlatform(), viewportWidth, player))
                possiblePatternsId.add(i);

        //prendi un pattern
        patternId = possiblePatternsId.get(rand.nextInt(possiblePatternsId.size())); //si assume che nell'insieme di tutti i pattern, ce ne sia sempre almeno 1 applicabile

        //inizializziamo il pattern
        patterns.get(patternId).start();
    }

    /**restituisce nuova piattaforma per il pattern in corso*/
    private Platform continuePattern() {
        float spaceUp = Math.max(player.getHeight() * (1.5f + Player.MAGIC_PADDING_MUL), heightOfMonstersOnNextPlatform); //altezza sopra prossima piattaforma
        PlatformInfo info = patterns.get(patternId).generate(getLastCreatedPlatform(), nextType, viewportWidth, rand, spaceUp, player); //prendi info dal pattern

        info.position.x = (info.position.x + info.width/2) / world.getPixelPerMeter(); //trasforma in metri
        info.position.y = (info.position.y + info.height/2) / world.getPixelPerMeter();

        return Pools.obtainPlatform(info.name, rand.nextBoolean(), world, assetManager, info.position, getNextCover(), frontLayer);
        //return Platform.createPlatform(world, assetManager, info.position, info.name, rand.nextBoolean()); //e crea piattaforma
    }

    /**stiamo creando un pattern ancora?*/
    private boolean patternInExec() {
        return patternId > 0 && patternId < patterns.size() && patterns.get(patternId).getRemaining() > 0;
    }

    @Override
    public void addPadAvailable(String pad) { //aggiunge pad disponibili per lo spawer
        super.addPadAvailable(pad);
        //vedi quali di quelle che vengono aggiunte possono anche essere usate nei pattern
        try {
            float w = Utils.padDimensions(pad).x;
            if(w <= viewportWidth/3) { //devono essere <= 1/3 della viewport per poter essere usate nei pattern
                possiblePatternPad.add(pad);
                setPatternPad.add(pad); //aggiungiamo anche al set
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**inizializza tutti i pattern possibili*/
    private void initPatterns() {
        patterns.add(new Pattern1(false));
        patterns.add(new Pattern1(true));
        patterns.add(new Pattern2(false));
        patterns.add(new Pattern2(true));
    }

    /**ci troviamo in una situazione in cui vogliamo usare un pattern?*/
    private boolean wantUsePattern() {
        if(!setPatternPad.contains(nextType.v1)) //non possiamo concatenare, quindi sicuramente no
            return false;
        float lastX = getLastCreatedPlatform().getX();
        if(lastX < nextType.v2.x/2 || lastX > viewportWidth - nextType.v2.x/2) //se stiamo troppo esterni non vogliamo usarlo
            return false;
        return true;
    }

    /**aggiorna probabilità di usare pattern*/
    public void setPatternProb(float patternProb) {
        this.patternProb = patternProb;
    }
}
