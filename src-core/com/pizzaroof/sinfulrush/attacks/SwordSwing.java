package com.pizzaroof.sinfulrush.attacks;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.SpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.LinkedList;

/**singolo taglio della spada (che può averne più di uno)*/
public class SwordSwing {

    /**punti dove è stato strisciato il dito in sequenze*/
    private LinkedList<Vector2> points;

    /**lista smoothed dei punti*/
    private LinkedList<Vector2> smoothed;

    /**danno dello swing*/
    private int damage;

    /**mondo fisico per query*/
    private World2D world;

    /**colore swing*/
    private Color color;

    private AssetManager assetManager;

    /**gruppo di cui fa parte la spada (lo swing è un elemento della spada)*/
    private Group group;

    /**mesh per stampare la grafica*/
    private Mesh mesh;

    /**vertici per mesh*/
    private float verts[];

    /**indice attuale per riempire l'array verts*/
    private int vertInd;

    /**shader per disegnare lo swing*/
    private ShaderProgram shader;

    /**vettori per texture coordinate interne e esterne (relative ai vertici sullo swing)*/
    private Vector2 extCoord = new Vector2(0f, 0f), intCoord = new Vector2(1.f, 0f);

    /**quanti punti vogliamo mettere al massimo per uno swing?*/
    private static final int MAX_POINTS_PER_SWING = 10;

    /**quante iterazioni di smooth facciamo?*/
    private static final int SMOOTH_ITERATIONS = 1;

    private static final int VERTEX_COMPONENTS = 8;

    /**massimo numero di vertici nello swing*/
    private static final int MAX_VERTICES = 3 * 4 * (1<<SMOOTH_ITERATIONS) * MAX_POINTS_PER_SWING; //dobbiamo stampare 4 triangoli (massimo) per ogni punto nello swing (NB: i punti crescono esponenzialmente con lo smooth)

    /**camera usata*/
    private Camera camera;

    /**sword a cui appartiene*/
    private Sword mySword;

    /**puntatore che indica il punto a cui abbiamo fatto un suono (-1 significa che dobbiamo vedere il primo punto... il vero punto
     * dell'ultimo suono ormai è sparito)*/
    private int lastPointBeforeSound;

    /**distanza da percorrere per fare un suono?*/
    private static final float DISTANCE_TO_SOUND = 400.f / com.pizzaroof.sinfulrush.Constants.PIXELS_PER_METER;

    public SwordSwing(World2D world, AssetManager assetManager, Group group, int damage, Sword mySword) {
        this.world = world;
        this.assetManager = assetManager;
        this.group = group;
        this.damage = damage;
        this.points = new LinkedList<>();
        this.mySword = mySword;
        smoothed = new LinkedList<>();

        mesh = new Mesh(false, MAX_VERTICES, 0, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"),
                        new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color"),
                        new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0")); //mettiamo texture coordinates, in modo da sapere quanto siamo lontani dal centro dello swing

        verts = new float[MAX_VERTICES * VERTEX_COMPONENTS];

        vertInd = 0;

        lastPointBeforeSound = -1;

        shader = assetManager.get(com.pizzaroof.sinfulrush.Constants.SWORD_SHADER, ShaderProgram.class);
    }

    public void setDamage(int dmg) {
        damage = dmg;
    }

    public void addNewPoint(Vector2 worldPoint) {
        if(points.size() > 0 && worldPoint.dst2(points.getLast()) < com.pizzaroof.sinfulrush.Constants.EPS)
            return;

        if(points.size() > 0) {
            //NB: raycast ignora gli shape che contengono @last
            world.getBox2DWorld().rayCast(raycastCallback, points.getLast(), worldPoint); //fai raycast callback
        }
        points.add(worldPoint);

        if(points.size() > MAX_POINTS_PER_SWING) {
            points.removeFirst(); //solo per questioni grafiche... potremmo rimuoverli già da 2
            if(lastPointBeforeSound > 0)
                lastPointBeforeSound--;
        }

        //controlla se devi eseguire un suono...
        Vector2 soundPoint = points.get(Math.max(0, lastPointBeforeSound));
        if(soundPoint.dst2(worldPoint) >= DISTANCE_TO_SOUND * DISTANCE_TO_SOUND) {
            mySword.getSoundManager().swordSwing();
            lastPointBeforeSound = points.size()-1;
        }
    }

    public void clearPoints() {
        points.clear();
        lastPointBeforeSound = -1;
    }

    /**smoothed points, utile per stamparli più smooth, non chiamarla spesso*/
    public LinkedList<Vector2> smoothedPoints() {
        return smoothedPoints(SMOOTH_ITERATIONS);
    }

    /**restituisce una lista dei punti, smoothed (in sostanza è utile per stamparli*/
    public LinkedList<Vector2> smoothedPoints(int iterations) {
        smoothed.clear(); //facciamo sempre tutto con la stessa lista, per motivi di efficienza
        smoothed.addAll(points);

        if(points.size() < 2) return smoothed; //servono almeno 2 punti

        //Chaikin's algorithm... L'idea è tagliare gli spigoli e farli più smooth iterativamente
        for(int iter=0; iter<iterations; iter++) { //ripeti per un certo numero di iterazioni, NB: il numero di punti aumenta esponenzialmente
            int len = smoothed.size();
            Vector2 first = null;
            for(int i=0; i<len-1; i++) { //O(len)
                Vector2 tmp = smoothed.getFirst();
                if(i == 0) first = tmp.cpy();
                smoothed.removeFirst();

                Vector2 p1 = tmp.cpy().scl(0.75f).add(smoothed.getFirst().cpy().scl(0.25f)); // 3/4 p_i + 1/4 p_{i+1}
                Vector2 p2 = tmp.scl(0.25f).add(smoothed.getFirst().cpy().scl(0.75f)); // 1/4 p_i + 3/4 p_{i+1}
                smoothed.addLast(p1);
                smoothed.addLast(p2);
            }
            smoothed.addLast(smoothed.pollFirst());//ultimo punto non genera nuovi punti: spostiamolo alla fine
            smoothed.addFirst(first);//rimettiamo punto iniziale (finale e iniziale non vanno tolti, o accorciano la curva
        }

        return smoothed;
    }

    public void setColor(Color c) {
        this.color = c;
    }

    /**stampa lo swing*/
    public void draw(Batch batch, Camera camera) {

        this.camera = camera;

        float maxWidth = 25, minWidth = 0; //minima e massima larghezza del taglio
        float depth = 60; //profondità della punta
        int numPoint = 0;
        Vector2 last = null, lastPerp = null, lastPerp2 = null;
        LinkedList<Vector2> points = smoothedPoints();
        if(points.size() > 2) {
            batch.end();

            Vector2 p2 = points.getLast().cpy(); //aggiungi un punto extra per fare la punta più profonda
            Vector2 p1 = points.get(points.size()-2).cpy();
            p2.sub(p1).nor().setLength(depth / world.getPixelPerMeter()).add(points.getLast()); //prendi direzione p1->p2 per sapere dove allungare
            points.addLast(p2);

            for (Vector2 point : points) {
                Vector2 tmp = new Vector2(point.x * world.getPixelPerMeter(), point.y * world.getPixelPerMeter());
                numPoint++;

                if (last != null) {
                    Vector2 dir = tmp.cpy().sub(last).nor(); //direzione last->tmp
                    Vector2 perp = new Vector2(-dir.y, dir.x); //vettore perpendicolare a tmp
                    Vector2 perp2 = perp.cpy().scl(-1); //altra direzione (sempre vettore perpendicolare) (tmp è l'asse di simmetria)

                    float fac = Interpolation.linear.apply((float) numPoint / points.size()); //interpolazione per vedere quanto fare cicciotto il taglio in questo punto

                    perp.setLength(fac * (maxWidth - minWidth) + minWidth).add(tmp); //trasla sulla "punta" di tmp (e metti anche la larghezza giusta)
                    perp2.setLength(fac * (maxWidth - minWidth) + minWidth).add(tmp);

                    if (lastPerp != null) {
                        if (numPoint != points.size()) {
                            //per le texture coordinates mettiamo (1, 0) sui punti al centro, e (0, 0) sui punti esterni (i perpendicolari), poi opengl fa interpolazione
                            //e quello che otteniamo è che più il valore della texutre è vicino a 1, più è vicino al centro

                            //punto centrale: dobbiamo attaccare i vari estremi
                            addTriangle(last, color, intCoord, perp, color, extCoord, lastPerp, color, extCoord);
                            addTriangle(last, color, intCoord, perp, color, extCoord, tmp, color, intCoord);

                            addTriangle(last, color, intCoord, perp2, color, extCoord, lastPerp2, color, extCoord);
                            addTriangle(last, color, intCoord, perp2, color, extCoord, tmp, color, intCoord);
                        } else {
                            //ultimo punto: dobbiamo creare la punta
                            addTriangle(last, color, intCoord, lastPerp, color, extCoord, tmp, color, extCoord);
                            addTriangle(last, color, intCoord, lastPerp2, color, extCoord, tmp, color, extCoord);
                        }
                    } else { //punta iniziale (dal primo punto agli estremi del secondo)
                        addTriangle(last, color, extCoord, tmp, color, intCoord, perp, color, extCoord);
                        addTriangle(last, color, extCoord, tmp, color, intCoord, perp2, color, extCoord);
                    }

                    lastPerp = perp;
                    lastPerp2 = perp2;
                }
                last = tmp;
            }

            flushMesh();

            batch.begin();
        }

    }

    protected void flushMesh() {
        mesh.setVertices(verts, 0, vertInd);

        Gdx.gl.glDepthMask(false);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);


        shader.begin();

        shader.setUniformMatrix("u_projTrans", camera.combined);

        mesh.render(shader, GL20.GL_TRIANGLES);

        shader.end();

        Gdx.gl.glDepthMask(true);

        vertInd = 0;
    }

    protected void addVertex(Vector2 pos, Color color, Vector2 texCoord) {
        if(vertInd + VERTEX_COMPONENTS >= verts.length) //non riusciamo a inserirli tutti... fai flush
            flushMesh();

        verts[vertInd++] = pos.x;
        verts[vertInd++] = pos.y;
        verts[vertInd++] = color.r;
        verts[vertInd++] = color.g;
        verts[vertInd++] = color.b;
        verts[vertInd++] = color.a;
        verts[vertInd++] = texCoord.x;
        verts[vertInd++] = texCoord.y;
    }

    protected void addTriangle(Vector2 pos1, Color color1, Vector2 texCoord1, Vector2 pos2, Color color2, Vector2 texCoord2, Vector2 pos3, Color color3, Vector2 texCoord3) {
        addVertex(pos1, color1, texCoord1);
        addVertex(pos2, color2, texCoord2);
        addVertex(pos3, color3, texCoord3);
    }

    /**callback per il raycast*/
    private RayCastCallback raycastCallback = new RayCastCallback() {
        @Override
        public float reportRayFixture(Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            if(fraction > 1 || fraction < 0) return fraction; //punto fuori dai 2 per cui si è fatta la query (restituisco fraction per diminuire lunghezza raycast)

            if(fixture.getBody().getUserData() instanceof Enemy) { //enemy... faccio danni
                Enemy e = (Enemy)fixture.getBody().getUserData();
                if(e.getHp() > 0) {
                    e.takeDamage(damage, new Vector2(world.getPixelPerMeter() * point.x, world.getPixelPerMeter() * point.y), Color.WHITE, Mission.BonusType.SWORD);
                    damageToEnemy(e);
                }

                //sparkle quando si colpisce un nemico
                SpriteActor sparkle = new SpriteActor() {
                    @Override
                    public void actSkipTolerant(float delta) {
                        super.actSkipTolerant(delta);
                        if(isAnimationEnded())
                            remove();
                    }
                };


                sparkle.addAnimationFromAtlas(0, assetManager.get(com.pizzaroof.sinfulrush.Constants.SPARKLE_ATLAS, TextureAtlas.class), Constants.SPARKLE_REG_NAME,.5f, Animation.PlayMode.NORMAL);
                sparkle.setAnimation(0);
                sparkle.setDrawingWidth(69);
                sparkle.setDrawingHeight(69);
                sparkle.setPositionFromCenter(point.x * world.getPixelPerMeter(), point.y * world.getPixelPerMeter());
                sparkle.setRotation(Utils.randFloat() * 360);
                if(color != null)
                    sparkle.setColor(color);
                group.addActor(sparkle);
            }
            return 1; //non diminuire lunghezza
        }
    };

    /**callback per quando si fa danno a un nemico @e*/
    protected void damageToEnemy(Enemy e) {
        mySword.getSoundManager().swordDamage();
    }

    /**usata per resettare y quando diventa troppo grande*/
    public void resetY(float maxy) {
        for(Vector2 p : points)
            p.y -= maxy;
    }
}
