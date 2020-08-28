package com.pizzaroof.sinfulrush.actors.physics.game_actors;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.backgrounds.StaticDecoration;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.PlatformEnemy;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;

/**piattaforma del gioco. Le piattaforme sono organizzate in directory: una directory identifica un insieme di piattaforme, nella directory deve essere presente
 * un file 'pad.pack' (una texture atlas con la grafica di ogni piattaforma) e due file: 'name.shape' e 'name.dim' per informazioni sulla piattafomra 'name'. Quindi in sostanza, una singola
 * piattaforma è due file all'interno di una cartella (che contiene più piattaforme). Concettualmente sarebbe stato meglio fare una texture per ogni piattaforma (QUESTE PIATTAFORME
 * NON HANNO ANIMAZIONI), però usando l'atlas risparmiamo spazio e carichiamo più efficientemente. Il nome del file della piattaforma deve essere lo stesso della regione nell'atlas.
 * il file .shape contiene lo shape della piattaforma e il .dim contiene le dimensioni (che per drawing e attuali sono uguali) NB: lo shape deve essere relativo alle dimensioni di dim*/
public class Platform extends PhysicSpriteActor {

    protected static final short PAD_ANIM = 0;

    /**booleano che indica se questa piattaforma è stata saltata*/
    protected boolean beenJumped;

    /**tutti i nemici che stanno su questa piattaforma*/
    protected HashSet<PlatformEnemy> myEnemies;

    /**layer in primo piano della piattaforma (serve per metterci eventuale cover)*/
    protected Group frontLayer;
    /**cover della piattaforma (opzionale: è un'immagine in primo piano sulla piattaforma)*/
    protected StaticDecoration platformCover;

    /**metodo privato: usa il factory @createPlatform per creare le piattaforme*/
    private Platform(com.pizzaroof.sinfulrush.actors.physics.World2D world, float friction, float restitution, Vector2 position, Shape...shapes) {
        super(world, BodyDef.BodyType.StaticBody, 0, friction, restitution, position, shapes);
        setJumped(false);
        myEnemies = new HashSet<>();
        platformCover = null;
        frontLayer = null;
    }

    @Override
    public void actSkipTolerant(float delta) {
        super.actSkipTolerant(delta);
        if(hasBeenJumped() && !isInCameraView()) { //è già stata saltata e non è più visibile? allora possiamo rimuoverla...
            if(myPool != null)
                removeAndFree();
            else
                remove();
        }
    }

    @Override
    public void onAnimationChanged(int old, int nw){}

    @Override
    protected void updateAnimation(float delta) {
        //non facciamo niente: è inutile fare il lavoro che fa super, tanto
        //questa piattaforma non ha animazioni
    }

    /**setta se la piattaforma è stata saltata o no*/
    public void setJumped(boolean jumped) {
        beenJumped = jumped;
    }

    /**è stata saltata?*/
    protected boolean hasBeenJumped() {
        return beenJumped;
    }

    /**aggiunge nemico su questa piattaforma*/
    public void addEnemy(PlatformEnemy enemy) {
        myEnemies.add(enemy);
    }

    /**rimuove nemico dalla piattaforma*/
    public void removeEnemy(PlatformEnemy enemy) {
        myEnemies.remove(enemy);
    }

    /**quanti mostri ci sono sulla piattaforma?*/
    public int getMonsterCount() {
        return myEnemies.size();
    }

    public HashSet<PlatformEnemy> getEnemies() {
        return myEnemies;
    }

    /**questa piattaforma è vuota sopra?*/
    public boolean isEmpty() {
        return getMonsterCount() == 0; //se ci sono 0 nemici si, altrimenti no
    }

    /**altezza dei mostri su questa piattaforma*/
    public float getHeightOfMonsters() {
        float mx = 0;
        for(Enemy e : getEnemies())
            mx = Math.max(mx, e.getHeight());
        return mx;
    }

    /**numero di nemici vivi sulla piattaforma*/
    public int numLivingEnemies() {
        int c = 0;
        for(Enemy e : getEnemies())
            if(e.getHp() > 0)
                c++;
        return c;
    }

    /**numero di nemici vivi sulla piattaforma con x >= @minx (minx è in pixel)*/
    public int numLivingEnemies(float minx) {
        int c = 0;
        for(Enemy e : getEnemies())
            if(e.getHp() > 0 && e.getBody() != null && e.getBody().getPosition().x * pixelPerMeter >= minx)
                c++;
        return c;
    }

    public void setFrontLayer(Group frontLayer) {
        this.frontLayer = frontLayer;
    }

    public void setCover(Group frontLayer, TextureRegion region) {
        setFrontLayer(frontLayer);
        platformCover = new StaticDecoration(region, false);
        platformCover.setWidth(getWidth());
        platformCover.setHeight(getHeight());
        platformCover.setX(getX());
        platformCover.setY(getY());
        platformCover.flip(!getHorDirection().equals(originalDirection));
    }

    @Override
    public void init(Vector2 position) {
        super.init(position);
        if(platformCover != null) {
            platformCover.setX(getX());
            platformCover.setY(getY());
            platformCover.flip(!getHorDirection().equals(originalDirection));
        }
    }

    @Override
    public boolean remove() {
        boolean r = super.remove();
        if(platformCover != null && platformCover.getStage() != null)
            platformCover.remove();
        return r;
    }

    @Override
    public void setParent(Group group) { //chiamata quando la piattaforma viene aggiunta allo stage
        super.setParent(group);
        if(platformCover != null)
            frontLayer.addActor(platformCover);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Platform))
            return false;
        return samePosition((Platform)obj);
    }

    @Override
    public void reset() {
        super.reset();
        setJumped(false);
        if(myEnemies == null)
            myEnemies = new HashSet<>();
        myEnemies.clear();
        setAnimation(PAD_ANIM);
    }

    /**usa questo factory method invece del costruttore
     * @param padName path del file piattaforma (deve essere all'interno della cartella)
     * @param position posizione in metri
     * @param flipped dobbiamo ribaltarla rispetto a y?*/
    public static Platform createPlatform(com.pizzaroof.sinfulrush.actors.physics.World2D world, AssetManager asset, Vector2 position, String padName, boolean flipped, String coverPad, Group topLayer) {
        String atlasFile = com.pizzaroof.sinfulrush.util.Utils.padAtlasPath(padName);

        TextureAtlas atlas = asset.get(atlasFile, TextureAtlas.class);

        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.padDimPath(padName));
            String strs[] = reader.readLine().split(" "); //(width, height) (drawing e attuali coincidono)
            int w = Integer.parseInt(strs[0]), h = Integer.parseInt(strs[1]);
            Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(com.pizzaroof.sinfulrush.util.Utils.padShapePath(padName), w, h, world.getPixelPerMeter(), flipped);  //crea shape
            Platform platform = new Platform(world, com.pizzaroof.sinfulrush.Constants.DEFAULT_PLATFORM_FRICTION, 0, position, shapes); //crea piattaforma e metti animazione
            platform.addAnimationFromAtlas(PAD_ANIM, atlas, com.pizzaroof.sinfulrush.util.Utils.padSimpleName(padName),1, Animation.PlayMode.NORMAL); //mettiamo un'animazione (che in realtà è un'immagine fissa)
            platform.setAnimation(PAD_ANIM);
            platform.setDrawingWidth(w); //mettiamo dimensioni (uguali per drawing e attuali)
            platform.setDrawingHeight(h);
            platform.setWidth(w);
            platform.setHeight(h);
            if(flipped) //ribalta immagine
                platform.setHorDirection(HorDirection.LEFT);
            platform.recomputePosition(); //ricalcola subito posizione, cosi getX() e getY() danno valori corretti

            if(coverPad != null && topLayer != null) { //ha un cover layer...
                platform.setCover(topLayer, atlas.findRegion(coverPad));
            }

            return platform;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; //null se c'è stato qualche problema con la lettura dei file
    }

    /**crea piattaforma invisibile: l'unico shape attualmente supportato è un box WxH
     * position, w e h sono in pixel*/
    public static Platform createInvisiblePlatform(World2D world, Vector2 position, int w, int h) {
        position.x /= world.getPixelPerMeter();
        position.y /= world.getPixelPerMeter();
        Platform platform = new Platform(world, Constants.DEFAULT_PLATFORM_FRICTION, 0, position, Utils.getBoxShape(w / world.getPixelPerMeter(), h / world.getPixelPerMeter()));
        platform.setWidth(w);
        platform.setHeight(h);
        platform.setDrawingWidth(w);
        platform.setDrawingHeight(h);
        platform.recomputePosition();
        return platform;
    }
}
