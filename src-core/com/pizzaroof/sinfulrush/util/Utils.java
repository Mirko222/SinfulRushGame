package com.pizzaroof.sinfulrush.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus.Bonus;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.*;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.util.pools.Pools;
import com.pizzaroof.sinfulrush.spawner.FillerType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

/**classe contenente metodi di utility varia*/
public class Utils {

    //---------utility per collision shapes----------------

    /**restituisce uno shape a cerchio per body fisici (raggio in metri!!!)*/
    public static CircleShape getCircleShape(float radius) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        return shape;
    }

    /**restituisce un box shape per body fisici (width e height in metri!!!)*/
    public static PolygonShape getBoxShape(float width, float height) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width/2.f, height/2.f);
        return shape;
    }

    /**legge una lista di shapes dal file
     * @param path path del file shape
     * @param bbw bounding box width (in pixel)
     * @param bbh bounding box height (in pixel)
     * @param ppm pixels per meter (quanti pixel in un metro)
     * @param flippedX dobbiamo ribaltare l'asse x?
     * FORMATO: la prima riga contiene il numero di shape.
     * Per ogni shape: tipo di shape {C=cerchio, P=poligono convesso). Se il tipo è un cerchio: (raggio, x, y) [le coordinate sono quelle del centro].
     * Se il tipo è un poligono convesso: N (cioè numero di punti nel poligono) e poi N coppie "x y" per indicare i punti del poligono (in senso orario/antiorario).
     * NB: TUTTE LE COORDINATE/DIMENSIONI DATE SONO IN PIXEL RELATIVE AD UN'IMMAGINE (CON BOUNDING BOX BBWxBBH)
     * NB2: mettiamo ogni shape su una sola riga*/
    public static Shape[] getShapesFromFile(String path, float bbw, float bbh, float ppm, boolean flippedX) throws IOException {
        float wm = bbw / ppm; //width in metri
        float hm = bbh / ppm; //height in metri

        BufferedReader reader = Utils.getInternalReader(path);
        int N = Integer.parseInt(reader.readLine()); //leggiamo numero di pezzi di cui è composto lo shape
        Shape shapes[] = new Shape[N];
        for(int t=0; t<N; t++) { //itera gli shape
            String strs[] = reader.readLine().split(" ");
            if(strs[0].equals("C")) { //circle shape
                shapes[t] = new CircleShape();
                shapes[t].setRadius(Float.parseFloat(strs[1]) / ppm); //leggiamo raggio (in metri)
                float x = Float.parseFloat(strs[2]), y = bbh/2 - Float.parseFloat(strs[3]); //leggiamo posizione cerchio (in metri) (capovolgiamo y, perchè nel body "sale verso l'alto", mentre nell'immagine "sale verso il basso")
                if(flippedX)//specchia x
                    x = bbw - x;
                x -= (bbw/2); //rendiamo relativo al centro (dopo aver specchiato)
                x /= ppm;
                y /= ppm;
                ((CircleShape) shapes[t]).setPosition(new Vector2(x, y));
            }
            else { //altrimenti è "P"
                int M = Integer.parseInt(strs[1]); //numero punti
                Vector2 vertices[] = new Vector2[M];
                for(int i=2; i<2*M+1; i+=2) { //scorri punti
                    float x = Float.parseFloat(strs[i]), y = bbh - Float.parseFloat(strs[i+1]); //leggiamo coordiante
                    if(flippedX) //specchia x
                        x = bbw - x;
                    //dobbiamo convertire da x,y relative all'immagine a x,y relative al body (il centro del body è 0,0) (capovolgiamo y, perchè nel body "sale verso l'alto", mentre nell'immagine "sale verso il basso")
                    float xm = x / ppm, ym = y / ppm; //x,y in metri
                    xm -= (wm / 2); //rendi le coordinate relative al centro
                    ym -= (hm / 2);
                    vertices[i/2-1] = new Vector2(xm, ym);
                }
                shapes[t] = new PolygonShape();
                ((PolygonShape)shapes[t]).set(vertices);
            }
        }
        reader.close();
        return shapes;
    }

    public static Shape[] getShapesFromFile(String path, float bbw, float bbh, float ppm) throws IOException {
        return getShapesFromFile(path, bbw, bbh, ppm, false);
    }

    /**restituisce mask bits per entrare in collisione con tutto tranne le @categories specificate*/
    public static short maskToNotCollideWith(short...categories) {
        short or = 0;
        for(short c : categories) //or di tutte le maschere
            or |= c;
        return (short)((short)(-1) ^ or); //tutti tranne l'or
    }

    //------------utility per calcoli fisici------------

    /**tempo per percorrere @dist spazio partendo con una velocità iniziale @v0 e avendo un'accelerazione uniforme di @a (tutto in metri)*/
    public static float timeToReachDistance(float v0, float dist, float a) {
        //sfruttiamo legge oraria moto rettilineo uniformemente accelerato
        //s = 1/2 a (t - ti)^2 + Vi (t - ti) + Si -> possiamo considerare tempo iniziale e spazio iniziale nulli
        //-> s = 1/2 a t^2 + Vi t -> da cui -> t = (-v0 +- sqrt(v0^2 +2 a s))/a (il segno dipende dal segno dell'accelerazione: il tempo deve essere positivo
        int sign = a < 0 ? -1 : 1;
        return (-v0 + sign * (float)Math.sqrt(Math.abs(v0*v0 + 2 * a * dist))) / a;
    }

    /**restituisce la velocità iniziale se si vuole raggiungere una velocità finale @vf e percorrere verso l'alto uno spazio di @space mentre si è
     * soggetti ad una forza di gravity @gravity (tutto in metri)*/
    public static float initialSpeedToReachHeight(float vf, float gravity, float space) {
        //legge di conservazione dell'energia: Kf + Uf = Ki + Ui
        //dove K = 1/2 m v^2 (energia cinetica) e U = m g h (energia potenziale, h è l'altitutidine dell'oggetto)
        //sostituendo si ottiene Vi = sqrt(Vf^2 + 2 g (hf - hi)) (e notiamo che hf-hi è lo spazio che si percorre)
        //la formula semplificata è: Vf^2 + 2 g hf = Vi^2 + 2 g hi
        return (float) Math.sqrt(Math.abs(vf*vf - 2 * gravity * space)); //mettiamo un meno perchè qui la gravità sarà negativa
    }

    //--------utility matematiche------------

    public static RandomXS128 rand = new RandomXS128();

    public static float randGaussian(float mean, float stdev) {
        return (float)(rand.nextGaussian() * stdev + mean);
    }

    public static Object randObject(Object ...obj) {
        return obj[Utils.randInt(0, obj.length-1)];
    }

    /**restituisce intero casuale in [min, max]*/
    public static int randInt(int min, int max) {
        if(min == max) return min;
        if(min > max) {
            int tmp = min;
            min = max;
            max = tmp;
        }
        return rand.nextInt(max - min + 1) + min;
    }

    /**float random in [0, 1]*/
    public static float randFloat() {
        return rand.nextFloat();
    }

    /**float random in [min, max]*/
    public static float randFloat(float min, float max) {
        if(min > max) {
            float tmp = min;
            min = max;
            max = tmp;
        }
        return randFloat() * (max - min) + min;
    }

    public static double randDouble(double min, double max) {
        if(min > max) {
            double tmp = min;
            min = max;
            max = tmp;
        }
        return rand.nextDouble() * (max - min) + min;
    }

    public static int randChoice(int ...vals) {
        return vals[Utils.randInt(0, vals.length-1)];
    }

    public static String randChoice(String ...vals) {
        return vals[Utils.randInt(0, vals.length-1)];
    }

    public static boolean randBool() {
        return rand.nextBoolean();
    }

    /**restituisce punto casuale in un cerchio*/
    public static Vector2 randomPointInCircle(Vector2 center, float radius) {
        float r = randFloat() * radius; //raggio a caso
        float rad = (float)(randFloat() * 2 * Math.PI); //[0, 2PI], angolo a caso
        return new Vector2((float)Math.cos(rad) * r + center.x, (float)Math.sin(rad) * r + center.y);
    }

    /**cosine interpolation.
     * @param a punto iniziale
     * @param b punto finale
     * @param x valore in [0,1] che indica "a che punto siamo" tra @a e @b*/
    public static float cosInterpolate(float a, float b, float x) {
        float f = (1 - (float)Math.cos(x * Math.PI)) * 0.5f;
        return a * (1 - f) + f * b;
    }

    /**punto (x, y) si trova nel rettangolo con punto in basso a sinistra (xmin, ymin)
     * e larghezza w e altezza h?*/
    public static boolean pointInRect(float x, float y, float xmin, float ymin, float w, float h) {
        return x >= xmin && x <= xmin + w && y >= ymin && y <= ymin + h;
    }

    /**punto nella telecamera?*/
    public static boolean pointInCamera(Camera camera, float x, float y) {
        return pointInRect(x, y, camera.position.x - camera.viewportWidth/2.f,
                camera.position.y - camera.viewportHeight/2.f, camera.viewportWidth, camera.viewportHeight);
    }

    /**@param upDownBoundings è vettore (limite inferiore, limite superiore) restituito da un gameplayScreen*/
    public static boolean actorWellVisible(Vector2 upDownBoundings, PhysicSpriteActor actor) {
        return actor.getY() + actor.getDrawingHeight() - upDownBoundings.x >= actor.getDrawingHeight()*0.5f
                &&
                upDownBoundings.y - actor.getY() >= actor.getDrawingHeight()*0.5f;
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    /**il bit @bitNumber di @bitmask è settato?*/
    public static boolean bitmaskSet(long bitmask, int bitNumber) {
        return (bitmask & (1L<<bitNumber)) > 0;
    }

    //---------utility per gestione cartelle assets-------------

    /**dal nome della pad, restituisce l'atlas associata*/
    public static String padAtlasPath(String padName) {
        String directory = padName.substring(0, padName.lastIndexOf("/")); //ricava directory dal nome (basta togliere quello che c'è prima dell'ultimo '/')
        return directory+"/"+Constants.PAD_ATLAS_SUFFIX;
    }

    /**restituisce path del file shape della pad dal nome*/
    public static String padShapePath(String padName) {
        return padName+"."+Constants.PAD_SHAPE_SUFFIX;
    }

    /**restituisce path del file dimensions della pad dal nome*/
    public static String padDimPath(String padName) {
        return padName+"."+Constants.PAD_DIM_SUFFIX;
    }

    /**restituisce nome "semplice" della piattaforma (cioè all'interno della directory)*/
    public static String padSimpleName(String padName) {
        return padName.substring(padName.lastIndexOf("/")+1);
    }

    /**width, height della piattaforma, dal file*/
    public static Vector2 padDimensions(String padName) throws IOException {
        BufferedReader reader = Utils.getInternalReader(Utils.padDimPath(padName));
        String strs[] = reader.readLine().split(" ");
        reader.close();
        return new Vector2(Integer.parseInt(strs[0]), Integer.parseInt(strs[1]));
    }

    //metodi per prendere path dei file di personaggio/nemici
    public static String playerInfoPath(String directory) {
        return directory+"/"+Constants.PLAYER_INFO_FILE_SUFFIX;
    }

    public static String playerShapePath(String directory) {
        return directory+"/"+Constants.PLAYER_SHAPE_FILE_SUFFIX;
    }

    public static String playerScmlPath(String directory) {
        return directory+"/"+Constants.PLAYER_SCML_SUFFIX;
    }

    public static String enemyInfoPath(String directory) {
        return directory+"/"+Constants.ENEMY_INFO_FILE_SUFFIX;
    }

    public static String enemyShapePath(String directory) {
        return directory+"/"+Constants.ENEMY_SHAPE_FILE_SUFFIX;
    }

    public static String enemyDeadShapePath(String directory) {
        return directory+"/"+Constants.DEAD_ENEMY_SHAPE_FILE_SUFFIX;
    }

    public static String enemyScmlPath(String directory) {
        //è possibile che l'scml sia in cartelle diverse (usando le charmaps possiamo riciclare...)

        if(directory.equals(Constants.DEATH_REAPER)) //molto particolare il caso della morte... è nella stessa cartella dell'elemental ma con un nome diverso
            return Constants.ELEMENTAL_DIRECTORY+"/animations2.scml";

        if(directory.equals(Constants.DEMON_DARKNESS_3_DIRECTORY) || directory.equals(Constants.ZOMBIE_CHIBI_DIRECTORY) ||
            directory.equals(Constants.SKELETON_CHIBI_DIRECTORY) || directory.equals(Constants.GHOUL_DIRECTORY))
            directory = Constants.DEMON_DARKNESS_1_DIRECTORY;
        else if(directory.equals(Constants.FALLEN_ANGEL_2_DIRECTORY))
            directory = Constants.FALLEN_ANGEL_1_DIRECTORY;
        else if(directory.equals(Constants.GARGOYLE_2_DIRECTORY) || directory.equals(Constants.GARGOYLE_1_FLY_DIRECTORY) ||
            directory.equals(Constants.GARGOYLE_2_FLY_DIRECTORY))
            directory = Constants.GARGOYLE_1_DIRECTORY;
        else if(directory.equals(Constants.ARMORED_SKULL_DIRECTORY))
            directory = Constants.MUMMY_DIRECTORY;
        else if(directory.equals(Constants.SUCCUBUS_CHIBI_DIRECTORY) || directory.equals(Constants.LAVA_GOLEM_2_DIRECTORY))
            directory = Constants.DEVIL_CHIBI_DIRECTORY;
        else if(directory.equals(Constants.GHOST2_DIRECTORY) || directory.equals(Constants.FLYING_GHOST1_DIRECTORY) ||
                directory.equals(Constants.FLYING_GHOST2_DIRECTORY))
            directory = Constants.GHOST1_DIRECTORY;
        else if(directory.equals(Constants.RED_FRIEND_DIRECTORY) || directory.equals(Constants.ORANGE_FRIEND_DIRECTORY))
            directory = Constants.YELLOW_FRIEND_DIRECTORY;
        else if(directory.equals(Constants.MINI_SKELETON2_DIRECTORY) || directory.equals(Constants.MINI_SKELETON3_DIRECTORY))
            directory = Constants.MINI_SKELETON1_DIRECTORY;

        return directory+"/"+Constants.ENEMY_SCML_SUFFIX;
    }

    public static String enemyAtlasPath(String directory) {
        return directory+"/"+Constants.ENEMY_ATLAS_SUFFIX;
    }

    public static String bonusScmlPath(String dir) {
        return dir+"/"+Constants.BONUS_SCML_SUFFIX;
    }

    public static String bonusInfoPath(String dir) {
        return dir+"/"+Constants.BONUS_INFO_SUFFIX;
    }

    /**restituisce dimensioni enemy leggendo il file*/
    public static Vector2 enemyDrawingDimensions(String directory) throws IOException {
        BufferedReader reader = Utils.getInternalReader(Utils.enemyInfoPath(directory));
        String str[] = reader.readLine().split(" ");
        reader.close();
        return new Vector2(Float.parseFloat(str[1]), Float.parseFloat(str[2]));
    }

    /**restituisce dimensioni enemy in pixel*/
    public static Vector2 enemyDimensions(String directory) throws IOException {
        BufferedReader reader = Utils.getInternalReader(Utils.enemyInfoPath(directory));
        String str[] = reader.readLine().split(" ");
        reader.close();
        return new Vector2(Float.parseFloat(str[3]), Float.parseFloat(str[4]));
    }

    /**restituisce dimensioni bonus in pixel*/
    public static Vector2 bonusDimensions(String directory) throws IOException {
        BufferedReader reader = Utils.getInternalReader(Utils.bonusInfoPath(directory));
        String str[] = reader.readLine().split(" ");
        reader.close();
        return new Vector2(Float.parseFloat(str[3]), Float.parseFloat(str[4]));
    }

    public static Vector2 getFillerObjectDimensions(String dir) throws IOException {
        FillerType type = Utils.getFillerType(dir);
        switch(type) {
            case ENEMY: return enemyDimensions(dir);
            case BONUS: return bonusDimensions(dir);
        }
        return null;
    }

    /**file scml per lo sheet effect dir*/
    public static String sheetEffectScmlPath(String dir) {
        return dir+"/"+Constants.SPRITER_GRAPHICS_EFFECT_SUFFIX;
    }

    /**file info per lo sheet effect dir*/
    public static String sheetEffectInfoPath(String dir) {
        return dir+"/"+Constants.SPRITER_EFFECT_INFO_SUFFIX;
    }


    /*private static LanguageManager.Text hellSuggs[][] = {{LanguageManager.Text.SUGGESTION1, LanguageManager.Text.SUGGESTION2,
                                                            LanguageManager.Text.SUGGESTION3, LanguageManager.Text.SUGGESTION4},
                                                            {LanguageManager.Text.SUGGESTION5, LanguageManager.Text.SUGGESTION6,
                                                            LanguageManager.Text.SUGGESTION7, LanguageManager.Text.SUGGESTION8}};
    private static int hellSuggsIndex[] = {0, 0};
    static {
        for(int r=0; r<hellSuggs.length; r++)
            for(int i=0; i<hellSuggs[r].length; i++) { //mischiamo suggerimenti
                int ind = Utils.randInt(i, hellSuggs[r].length-1);
                LanguageManager.Text tmp = hellSuggs[r][ind];
                hellSuggs[r][ind] = hellSuggs[r][i];
                hellSuggs[r][i] = tmp;
            }
    }

    public static LanguageManager.Text getHellSuggestion(Player player) {
        int r = player.getJumpedPlatforms() <= 3 ? 0 : Utils.randInt(0, 1); //35
        hellSuggsIndex[r] = (hellSuggsIndex[r] + 1) % hellSuggs[r].length;
        return hellSuggs[r][hellSuggsIndex[r]];
    }*/


    /**restituisce un buffered reader per leggere il file interno @path*/
    public static BufferedReader getInternalReader(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(Gdx.files.internal(path).read(), "UTF-8"));
    }

    /**set degli enemies corpo a corpo*/
    public static final HashSet<String> MELEE_ENEMIES = new HashSet<>();
    /**set degli sniper enemies*/
    public static final HashSet<String> SNIPER_ENEMIES = new HashSet<>();
    /**set degli sniper healer enemies*/
    public static final HashSet<String> SNIPER_HEALERS_ENEMIES = new HashSet<>();
    /**set degli sniper su piattaforma*/
    public static final HashSet<String> SNIPER_PLATFORM_ENEMIES = new HashSet<>();
    /**set dei nemici amici*/
    public static final HashSet<String> FRIEND_ENEMIES = new HashSet<>();
    static {
        MELEE_ENEMIES.add(Constants.DEMON_DARKNESS_1_DIRECTORY);
        MELEE_ENEMIES.add(Constants.DEMON_DARKNESS_2_DIRECTORY);
        MELEE_ENEMIES.add(Constants.DEMON_DARKNESS_3_DIRECTORY);
        MELEE_ENEMIES.add(Constants.DEVIL_CHIBI_DIRECTORY);
        MELEE_ENEMIES.add(Constants.HELL_KNIGHT_CHIBI_DIRECTORY);
        MELEE_ENEMIES.add(Constants.SUCCUBUS_CHIBI_DIRECTORY);
        MELEE_ENEMIES.add(Constants.CERBERUS_DIRECTORY);
        MELEE_ENEMIES.add(Constants.GARGOYLE_1_DIRECTORY);
        MELEE_ENEMIES.add(Constants.GARGOYLE_2_DIRECTORY);
        MELEE_ENEMIES.add(Constants.LAVA_GOLEM_2_DIRECTORY);
        MELEE_ENEMIES.add(Constants.GHOUL_DIRECTORY);
        MELEE_ENEMIES.add(Constants.MUMMY_DIRECTORY);
        MELEE_ENEMIES.add(Constants.LICH_DIRECTORY);
        MELEE_ENEMIES.add(Constants.SKELETON_CHIBI_DIRECTORY);
        MELEE_ENEMIES.add(Constants.ZOMBIE_CHIBI_DIRECTORY);
        MELEE_ENEMIES.add(Constants.ARMORED_SKULL_DIRECTORY);
        MELEE_ENEMIES.add(Constants.BLUE_SKELETON);
        MELEE_ENEMIES.add(Constants.GREEN_SKELETON);
        MELEE_ENEMIES.add(Constants.MINI_SKELETON1_DIRECTORY);
        MELEE_ENEMIES.add(Constants.MINI_SKELETON2_DIRECTORY);
        MELEE_ENEMIES.add(Constants.MINI_SKELETON3_DIRECTORY);
        MELEE_ENEMIES.add(Constants.OGRE_DIRECTORY);

        SNIPER_HEALERS_ENEMIES.add(Constants.FALLEN_ANGEL_1_DIRECTORY);
        SNIPER_HEALERS_ENEMIES.add(Constants.GHOST_HEALER_DIRECTORY);

        SNIPER_ENEMIES.add(Constants.FALLEN_ANGEL_2_DIRECTORY);
        SNIPER_ENEMIES.add(Constants.GARGOYLE_1_FLY_DIRECTORY);
        SNIPER_ENEMIES.add(Constants.GARGOYLE_2_FLY_DIRECTORY);
        SNIPER_ENEMIES.add(Constants.VAMPIRE_DIRECTORY);
        SNIPER_ENEMIES.add(Constants.FLYING_GHOST1_DIRECTORY);
        SNIPER_ENEMIES.add(Constants.FLYING_GHOST2_DIRECTORY);

        SNIPER_PLATFORM_ENEMIES.add(Constants.BLUE_DEMON_DIRECTORY);
        SNIPER_PLATFORM_ENEMIES.add(Constants.PURPLE_DEMON_DIRECTORY);
        SNIPER_PLATFORM_ENEMIES.add(Constants.GHOST1_DIRECTORY);
        SNIPER_PLATFORM_ENEMIES.add(Constants.GHOST2_DIRECTORY);
        SNIPER_PLATFORM_ENEMIES.add(Constants.RED_SKELETON);

        FRIEND_ENEMIES.add(Constants.YELLOW_FRIEND_DIRECTORY);
        FRIEND_ENEMIES.add(Constants.RED_FRIEND_DIRECTORY);
        FRIEND_ENEMIES.add(Constants.ORANGE_FRIEND_DIRECTORY);
    }

    /**genera nemico dalla directory
     * @param backgroundGroup gruppo di background per i nemici (probabilmente ce li spostiamo quando muoiono)*/
    public static Enemy getEnemyFromDirectory(String directory, AssetManager assetManager, World2D world, Vector2 initPosition, Group backgroundGroup, Group effectGroup, Group enemiesGroup, Stage stage, SoundManager soundManager, Player player) {
        if(directory == null)
            return null;

        if(MELEE_ENEMIES.contains(directory))
            return MeleeEnemy.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);

        if(SNIPER_ENEMIES.contains(directory))
            return FlyingSniperEnemy.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);

        if(SNIPER_HEALERS_ENEMIES.contains(directory))
            return HealerSniperEnemy.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup, enemiesGroup);

        if(SNIPER_PLATFORM_ENEMIES.contains(directory))
            return PlatformSniperEnemy.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);

        if(FRIEND_ENEMIES.contains(directory))
            return FriendEnemy.createEnemy(directory, soundManager, player, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);

        //casi speciali

        if(directory.equals(Constants.LAVA_GOLEM_DIRECTORY)) { //caso particolare lava golem
            HeadPopperMelee e = HeadPopperMelee.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);
            e.setHeadPoolId(Pools.PPPGolemHead);
            return e;
        }

        if(directory.equals(Constants.LAVA_GOLEM_3_DIRECTORY)) { //golem di lava che spawna altri golem piccoli
            HeadPopperMelee e = HeadPopperMelee.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);
            e.setHeadPoolId(Pools.PPPGolem3Head);
            e.setEnemiesGroup(enemiesGroup);
            ArrayList<PlatformEnemy> children = new ArrayList<>();
            children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.LAVA_GOLEM_2_DIRECTORY, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            if(Utils.randBool())
                children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.LAVA_GOLEM_2_DIRECTORY, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            e.setChildren(children);
            return e;
        }

        if(directory.equals(Constants.RED_DEMON_DIRECTORY)) {
            PlatformSniperEnemy e = PlatformSniperEnemy.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);
            e.setEnemiesGroup(enemiesGroup);
            if(Utils.randBool()) {
                ArrayList<PlatformEnemy> children = new ArrayList<>();
                children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.DEVIL_CHIBI_DIRECTORY, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
                if(Utils.randBool())
                    children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.DEVIL_CHIBI_DIRECTORY, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
                e.setChildren(children);
            }
            return e;
        }

        if(directory.equals(Constants.GIANT_SKELETON1) || directory.equals(Constants.GIANT_SKELETON2)) {
            MeleeEnemy e = MeleeEnemy.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);
            e.setEnemiesGroup(enemiesGroup);
            ArrayList<PlatformEnemy> children = new ArrayList<>();
            children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.MINI_SKELETON1_DIRECTORY, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            if(Utils.randBool()) {
                if(directory.equals(Constants.GIANT_SKELETON1))
                    children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.MINI_SKELETON2_DIRECTORY, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
                else
                    children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.MINI_SKELETON3_DIRECTORY, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            }
            e.setChildren(children);
            return e;
        }

        if(directory.equals(Constants.BLUE_NECROMANCER)) { //necromante blu: spawna scheletro blu
            MeleeEnemy e = MeleeEnemy.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);
            e.setEnemiesGroup(enemiesGroup);
            ArrayList<PlatformEnemy> children = new ArrayList<>();
            children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.BLUE_SKELETON, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            if(Utils.randBool())
                children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.BLUE_SKELETON, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            e.setChildren(children);
            return e;
        }

        if(directory.equals(Constants.GREEN_NECROMANCER)) {
            PlatformSniperEnemy e = PlatformSniperEnemy.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);
            e.setEnemiesGroup(enemiesGroup);
            ArrayList<PlatformEnemy> children = new ArrayList<>();
            children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.GREEN_SKELETON, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            if(Utils.randBool())
                children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.GREEN_SKELETON, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            e.setChildren(children);
            return e;
        }

        if(directory.equals(Constants.RED_NECROMANCER)) {
            PlatformSniperEnemy e = PlatformSniperEnemy.createEnemy(directory, soundManager, assetManager, world, stage, initPosition, backgroundGroup, effectGroup);
            e.setEnemiesGroup(enemiesGroup);
            ArrayList<PlatformEnemy> children = new ArrayList<>();
            children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.RED_SKELETON, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            if(Utils.randBool())
                children.add((PlatformEnemy)Utils.getEnemyFromDirectory(Constants.RED_SKELETON, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
            e.setChildren(children);
            return e;
        }

        if(directory.equals(Constants.GIANT_ZOMBIE3) || directory.equals(Constants.GIANT_ZOMBIE1) ||
                directory.equals(Constants.GIANT_ZOMBIE2)) { //caso particolare zombie
            HeadPopperMelee e = null;
            try {
                Vector2 dim = Utils.enemyDrawingDimensions(directory);
                Shape shapes[] = Utils.getShapesFromFile(Utils.enemyShapePath(directory), dim.x, dim.y, world.getPixelPerMeter());
                e = new HeadPopperMelee(world, soundManager, stage, 0, 0, 0, directory, assetManager, initPosition, backgroundGroup, effectGroup, shapes) {
                    @Override
                    public void onSpriterAnimationLooping(int id) {
                        if(id == RUNNING_ANIM)
                            spriterPlayer.setTime(300);
                    }
                };
            }catch(IOException ex) { //non dovrebbe succedere
                ex.printStackTrace();
            }
            e.setHeadPoolId(directory.equals(Constants.GIANT_ZOMBIE1) ? Pools.PPPZombie1Head :
                            directory.equals(Constants.GIANT_ZOMBIE3) ? Pools.PPPZombie3Head : Pools.PPPZombie2Head);
            String child = directory.equals(Constants.GIANT_ZOMBIE1) ? Constants.ZOMBIE_CHIBI_DIRECTORY :
                           directory.equals(Constants.GIANT_ZOMBIE3) ? Constants.GHOUL_DIRECTORY : null;
            if(child != null && Utils.randBool()) {
                e.setEnemiesGroup(enemiesGroup);
                ArrayList<PlatformEnemy> children = new ArrayList<>();
                children.add((PlatformEnemy)Utils.getEnemyFromDirectory(child, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
                if(Utils.randBool()) children.add((PlatformEnemy)Utils.getEnemyFromDirectory(child, assetManager, world, Vector2.Zero, backgroundGroup, effectGroup, enemiesGroup, stage, soundManager, player));
                e.setChildren(children);
            }
            return e;
        }

        return null;
    }

    /**prende bonus data solo la directory*/
    public static Bonus getBonusFromDirectory(String directory, AssetManager assetManager, Stage stage, SoundManager soundManager) {
        Bonus bonus = new Bonus(soundManager);
        bonus.initFromFile(directory, assetManager, stage);
        return bonus;
    }

    /**restituisce tipo di oggetto filler dalla directory*/
    public static FillerType getFillerType(String path) {
        if(path.charAt(0) == 'e') return FillerType.ENEMY; //le path iniziano tutte con enemies/...
        return FillerType.BONUS; //path dei bonus tutte con bonus/...
    }

    /**da un nome di bonus, restituisce la directory*/
    public static String getBonusDirectoryFromName(String name) {
        if(name.equals(Constants.SWORD_BONUS_NAME) || name.equals(Constants.RAGE_SWORD_BONUS_NAME) ||
            name.equals(Constants.SCEPTRE_BONUS_NAME) || name.equals(Constants.SCEPTRE_SPLIT_BONUS_NAME) ||
            name.equals(Constants.DOUBLE_RAGE_SWORD_BONUS_NAME) || name.equals(Constants.DOUBLE_SWORD_BONUS_NAME) ||
            name.equals(Constants.GLOVE_BONUS_NAME)) //spade e scettri
            return Constants.SWORD_BONUS_DIRECTORY;
        return Constants.BONUS_ICON_TYPE_1_DIR; //tutti gli altri bonus sono type 1
    }

    /**restituisce la character maps id di un oggetto dato il nome*/
    public static int charMapIdFromName(String name) {
        if(name.equals(Constants.WIND_BONUS_NAME)) return Constants.WIND_CHAR_MAPS;
        if(name.equals(Constants.ICE_BONUS_NAME)) return Constants.ICE_CHAR_MAPS;
        if(name.equals(Constants.RAGE_SWORD_BONUS_NAME) ||
            name.equals(Constants.DOUBLE_RAGE_SWORD_BONUS_NAME)) return Constants.RAGE_SWORD_CHAR_MAPS;
        if(name.equals(Constants.ELISIR_S_BONUS_NAME)) return Constants.ELISIR_S_CHAR_MAPS;
        if(name.equals(Constants.ELISIR_M_BONUS_NAME)) return Constants.ELISIR_M_CHAR_MAPS;
        if(name.equals(Constants.ELISIR_L_BONUS_NAME)) return Constants.ELISIR_L_CHAR_MAPS;
        if(name.equals(Constants.SLOWTIME_BONUS_NAME)) return Constants.SLOWTIME_CHAR_MAPS;
        if(name.equals(Constants.SCEPTRE_BONUS_NAME)) return Constants.SCEPTRE_CHAR_MAPS;
        if(name.equals(Constants.SCEPTRE_SPLIT_BONUS_NAME)) return Constants.SCEPTRE_SPLIT_CHAR_MAPS;
        if(name.equals(Constants.GLOVE_BONUS_NAME)) return Constants.GLOVE_CHAR_MAPS;
        return -1; //-1 == niente char maps
    }

    public static void resetAnimationsId(Bonus b, String name) {
        if(name.equals(Constants.SWORD_BONUS_NAME) || name.equals(Constants.RAGE_SWORD_BONUS_NAME) ||
            name.equals(Constants.SCEPTRE_BONUS_NAME) || name.equals(Constants.SCEPTRE_SPLIT_BONUS_NAME) ||
            name.equals(Constants.GLOVE_BONUS_NAME))
            b.resetAnimationsId(0, 1);
        else if(name.equals(Constants.DOUBLE_SWORD_BONUS_NAME))
            b.resetAnimationsId(2, 3);
        else if(name.equals(Constants.DOUBLE_RAGE_SWORD_BONUS_NAME))
            b.resetAnimationsId(4, 5);
    }

    //----------utility generiche---------------
    /**stampa un'immagine orizzontalmente, finché non copre tutto il necessario (se necessario sfora per coprire tutto).
     * @param minX x da cui iniziare a coprire
     * @param maxX x a cui arrivare a coprire
     * @param y y a cui stampare
     * @param img immagine con cui coprire
     * @param width larghezza immagine
     * @param height altezza immagine
     * @param flipY specchiamo verticalmente?*/
    public static void repeatImageHorizontally(Batch batch, float minX, float maxX, float y, Texture img, float width, float height, boolean flipY) {
        repeatImageHorizontally(batch, minX, maxX, y, img, width, height, flipY, 0);
    }

    public static void repeatImageHorizontally(Batch batch, float minX, float maxX, float y, TextureRegion img, float width, float height, boolean flipY) {
        repeatImageHorizontally(batch, minX, maxX, y, img, width, height, flipY, 0);
    }

    /**come repeat image horizontally, ma aggiungiamo un offset alla prima immagine da stampare (e le altre vengono stampate di conseguenza)*/
    public static void repeatImageHorizontally(Batch batch, float minX, float maxX, float y, TextureRegion img, float width, float height, boolean flipY, float offsetX) {
        float actX = minX + offsetX;
        while(actX <= maxX) { //copri tutta la zona orizzontale partendo da minX (se sforiamo maxX va bene comunque)
            batch.draw(img, actX, y + (flipY ? height : 0), width, height * (flipY ? -1 : 1));
            actX += width; //-5 (dovrebbe essere -1)
        }
        actX = minX + offsetX;
        while(actX >= minX) { //copre parte a sinistra
            batch.draw(img, actX - width,y + (flipY ? height : 0), width, height * (flipY ? -1 : 1));
            actX -= width;
        }
    }

    public static void repeatImageHorizontally(Batch batch, float minX, float maxX, float y, Texture img, float width, float height, boolean flipY, float offsetX) {
        float actX = minX + offsetX;
        while(actX <= maxX) { //copri tutta la zona orizzontale partendo da minX (se sforiamo maxX va bene comunque)
            batch.draw(img, actX, y + (flipY ? height : 0), width, height * (flipY ? -1 : 1));
            actX += width; //-5 (dovrebbe essere -1)
        }
        actX = minX + offsetX;
        while(actX >= minX) { //copre parte a sinistra
            batch.draw(img, actX - width,y + (flipY ? height : 0), width, height * (flipY ? -1 : 1));
            actX -= width;
        }
    }

    public static void repeatImageHorizontallySprite(Batch batch, float minX, float maxX, float y, Sprite img, float width, float height, boolean flipY, float offsetX) {
        float actX = minX + offsetX;
        img.setOrigin(0, 0);
        img.setScale(width / img.getWidth(), height / img.getHeight());
        img.setY(y + (flipY ? height : 0));
        while(actX <= maxX) { //copri tutta la zona orizzontale partendo da minX (se sforiamo maxX va bene comunque)
            img.setX(actX);
            img.draw(batch);
            actX += width; //-5 (dovrebbe essere -1)
        }
        actX = minX + offsetX;
        while(actX >= minX) { //copre parte a sinistra
            img.setX(actX - width);
            img.draw(batch);
            actX -= width;
        }
    }

    public static int getScore(Player player) {
        return player.getJumpedPlatforms() - player.getPlatformMalusCount();
    }

    public static Action disappearingAnimation(float dur) {
        return Actions.sequence(
                Actions.parallel(
                        Actions.scaleTo(1.5f, 1.5f, dur),
                        Actions.alpha(0, dur)
                ),
                Actions.visible(false)
        );
    }

    public static Action appearAnimation(float dur) {
        return Actions.sequence (
                Actions.delay(dur * 0.5f),
                Actions.visible(true),
                Actions.parallel (
                        Actions.scaleTo(1.f, 1.f, dur),
                        Actions.alpha(1, dur)
                )
        );
    }

    public static Action clickAction(Runnable runnable, float dur) {
        return Actions.sequence(disappearingAnimation(dur), Actions.run(runnable));
    }

    public static void getReadyForAnimations(Table table) {
        table.setTransform(true);
        table.setOrigin(table.getWidth()*0.5f, table.getHeight()*0.5f);
    }

    /**controlla che i colori siano gli stessi senza considerare alpha*/
    public static boolean sameColorRGB(Color a, Color b) {
        return Math.abs(a.r - b.r) <= Constants.EPS && Math.abs(a.g - b.g) <= Constants.EPS && Math.abs(a.b - b.b) <= Constants.EPS;
    }

    public static Vector2 randomDamagePosition(SpriteActor actor) {
        Vector2 center = actor.centerPosition();
        center.x += actor.getWidth() * Utils.randFloat(-0.6f, 0.6f);
        center.y += actor.getHeight() * Utils.randFloat(0.5f, 0.7f);
        return center;
    }

    public static boolean isCemeteryUnlocked(Preferences preferences) {
        return isCemeteryUnlocked(preferences.getInteger(Constants.HELL_HIGHSCORE_PREFS));
    }

    public static boolean isCemeteryUnlocked(int hellHighscore) {
        return hellHighscore >= Constants.SCORE_TO_UNLOCK_CEMETERY;
    }

    /**qual è la prima directory dei player che deve caricare?*/
    public static String getPlayerDirToLoad(Preferences prefs) {
        String str = prefs.getString(Constants.PLAYERS_SKIN_PREF, "");
        if(str.length() == 0) return Constants.THIEF_DIRECTORY;
        int pos = 0;
        for(int i=0; i<str.length(); i++)
            if(str.charAt(i) == 'S') {
                pos = i;
                break;
            }
        switch(pos) {
            case 2: return Constants.KNIGHT_DIRECTORY;
            case 4: return Constants.ELF_DIRECTORY;
            case 5: return Constants.WEREWOLF_DIRECTORY;
            default: return Constants.THIEF_DIRECTORY;
        }
    }

    /**stringa formattata con ore, minuti partendo dai secondi*/
    public static String getFormattedTime(long seconds) {
        long h = seconds / 3600L;
        seconds -= h * 3600L;
        long m = seconds / 60L;
        seconds -= m * 60L;
        String str = "";
        if(h != 0) str += h+"h ";
        if(m != 0) str += m+"m";
        //if(seconds != 0) str += seconds+"s";
        //return str.equals("") ? "0s" : str;
        return str.equals("") ? "0m" : str;
    }

    /**stringa in minuti/secondi dai secondi*/
    public static String getFormattedTimeSmall(long seconds) {
        long m = seconds / 60L;
        seconds -= m * 60L;
        String str = "";
        if(m != 0) str += m+"m ";
        if(seconds != 0) str += seconds+"s";
        return str.equals("") ? "0s" : str;
    }

    /***----------------------------------roba per colorare effetti particellari---------------*/
    private static final float[] DEFAULT_TIMELINE = {0.f, 1.f};
    private static final float[] ONE_COLOR_TIMELINE = {0f};
    private static final float[] DEFAULT_INT_TIMELINE = {0.f, 0.45080948f, 1.f};
    private static final float[] FIRE_COLORS = {1.f, 0.f, 0.f, 0.99215686f, 0.45882353f, 0.f};
    private static final float[] FIRE_INT_COLORS = {1.f,0.9137255f,0.f,1.f,0.654902f, 0.f, 1.f, 0.17254902f, 0.047058824f};
    private static final float[] BLUE_COLORS = {0.45882353f, 0.6039216f, 0.7490196f, 0.f, 0.07450981f, 0.99215686f};
    private static final float[] BLUE_INT_COLORS = {0.0f, 0.7529412f, 1.0f, 0.0f, 0.65882355f, 1.0f, 0.24313726f, 0.047058824f, 1.0f};
    private static final float[] DARK_COLORS = {0.29411766f, 0.f, 0.f, 0.f, 0.f, 0.f};
    private static final float[] DARK_INT_COLORS = {1f, 0.f, 0.f, 0.f, 0.f, 0.f};
    private static final float[] GREEN_COLORS = {0.f, 0.6431373f, 0.043137256f, 0.f, 0.3019608f, 0.05882353f};
    private static final float[] GREEN_INT_COLORS = {0.4745098f, 1.0f, 0.0f, 0.05490196f, 0.95686275f, 0.0f, 0.02745098f, 0.5568628f, 0.0627451f};
    private static final float[] VIOLET_COLORS = {0.42745098f, 0.023529412f, 0.74509805f, 0.42352942f, 0.023529412f, 0.74509805f, 0.88235295f, 0.f, 0.99215686f};
    private static final float[] VIOLET_INT_COLORS = {1f, 0f, 0.44313726f, 1f, 0.f, 0.70980394f, 0.6156863f, 0.047058824f,1.0f};
    private static final float[] VIOLET_TIMELINE = {0.0f, 0.13253012f, 1.0f};
    private static final float[] SKY_COLORS = {0f, 1f, 0.9764706f, 0.99215686f, 0.99215686f, 0.99215686f};
    private static final float[] SKY_INT_COLORS = {1f, 1f, 1f, 0.7411765f, 1f, 0.96862745f, 0.047058824f, 1f, 0.8980392f};
    private static final float[] WHITE_COLORS = {.6f, .6f, .6f, .6f, .6f, .6f};
    private static final float[] VIOLET_FRIEND_COLORS = {0.61960787f, 0.05490196f, 0.92156863f, 0.85882354f, 0.043137256f, 1.0f,0.99215686f,0.41960785f, 0.9411765f};
    private static final float[] VIOLET_FRIEND_INT_COLORS = {1.0f,0.0f, 0.9647059f, 1.0f,0.0f, 0.9647059f};
    private static final float[] COIN_COLORS = {1f, 0.7921569f, 0f};
    private static final float[] STAR_COLORS = {1f, 0.78039217f, 0f};
    private static final float[] GEM_COLORS = {0.9019608f, 0f, 1f};

    public static void colorEffect(ParticleEffectPool.PooledEffect e, Pools.PEffectColor color) {
        switch (color) {
            case FIRE:
                e.getEmitters().get(0).getTint().setColors(FIRE_COLORS);
                e.getEmitters().get(0).getTint().setTimeline(DEFAULT_TIMELINE);
                e.getEmitters().get(1).getTint().setColors(FIRE_INT_COLORS);
                e.getEmitters().get(1).getTint().setTimeline(DEFAULT_INT_TIMELINE);
                break;

            case BLUE:
                e.getEmitters().get(0).getTint().setColors(BLUE_COLORS);
                e.getEmitters().get(0).getTint().setTimeline(DEFAULT_TIMELINE);
                e.getEmitters().get(1).getTint().setColors(BLUE_INT_COLORS);
                e.getEmitters().get(1).getTint().setTimeline(DEFAULT_INT_TIMELINE);
                break;

            case DARK:
                e.getEmitters().get(0).getTint().setColors(DARK_COLORS);
                e.getEmitters().get(0).getTint().setTimeline(DEFAULT_TIMELINE);
                e.getEmitters().get(1).getTint().setColors(DARK_INT_COLORS);
                e.getEmitters().get(1).getTint().setTimeline(DEFAULT_TIMELINE);
                break;

            case GREEN:
                e.getEmitters().get(0).getTint().setColors(GREEN_COLORS);
                e.getEmitters().get(0).getTint().setTimeline(DEFAULT_TIMELINE);
                e.getEmitters().get(1).getTint().setColors(GREEN_INT_COLORS);
                e.getEmitters().get(1).getTint().setTimeline(DEFAULT_INT_TIMELINE);
                break;

            case VIOLET:
                e.getEmitters().get(0).getTint().setColors(VIOLET_COLORS);
                e.getEmitters().get(0).getTint().setTimeline(VIOLET_TIMELINE);
                e.getEmitters().get(1).getTint().setColors(VIOLET_INT_COLORS);
                e.getEmitters().get(1).getTint().setTimeline(DEFAULT_INT_TIMELINE);
                break;

            case SKY:
                e.getEmitters().get(0).getTint().setColors(SKY_COLORS);
                e.getEmitters().get(0).getTint().setTimeline(DEFAULT_TIMELINE);
                e.getEmitters().get(1).getTint().setColors(SKY_INT_COLORS);
                e.getEmitters().get(1).getTint().setTimeline(DEFAULT_INT_TIMELINE);
                break;

            case WHITE:
                for(ParticleEmitter em : e.getEmitters()) {
                    em.getTint().setColors(WHITE_COLORS);
                    em.getTint().setTimeline(DEFAULT_TIMELINE);
                }
                break;

            case MALUS_VIOLET:
                e.getEmitters().get(0).getTint().setColors(VIOLET_FRIEND_COLORS);
                e.getEmitters().get(0).getTint().setTimeline(DEFAULT_INT_TIMELINE);
                e.getEmitters().get(1).getTint().setColors(VIOLET_FRIEND_INT_COLORS);
                e.getEmitters().get(1).getTint().setTimeline(DEFAULT_TIMELINE);
                break;

            case TREASURE:
                e.getEmitters().get(0).getTint().setColors(STAR_COLORS);
                e.getEmitters().get(1).getTint().setColors(GEM_COLORS);
                e.getEmitters().get(2).getTint().setColors(COIN_COLORS);
                for(ParticleEmitter em : e.getEmitters())
                    em.getTint().setTimeline(ONE_COLOR_TIMELINE);
                break;
        }
    }
}
