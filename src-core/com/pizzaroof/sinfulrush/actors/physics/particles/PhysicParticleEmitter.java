package com.pizzaroof.sinfulrush.actors.physics.particles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**emettitore di particelle fisiche. è rappresentato in un file (NB: nella cartella del file deve essere presente un'atlas chiamata particles.atlas) :
 * N restitution //un intero che indica quanti diversi sprite particelle possono esserci, restitution di tutte le particelle
 * particle_region1 minVx maxVx minVy maxVy minTimeToLive maxTimeToLive rotate(può ruotare?) probability1
 * ... //per ogni tipo di particella salviamo: nome della regione, velocità iniziale minima/massima su x/y (m/s), min/max time to live, rotate, probabilità che esce (le probabilità non devono sommare a un numero particolare: si sommano e si va in proporzione)
 * ...*/
public class PhysicParticleEmitter {

    /**parametri per colore*/
    private Color minColor, maxColor;

    /**parametri per dimensione particelle (in pixel)*/
    private int minRadius, maxRadius;

    /**parametri per numero particelle*/
    private int minCount, maxCount;

    /**punto da dove vengono spawnare le particelle (pixel)*/
    private Vector2 spawnPoint;

    /**raggio entro il quale vengono spawnate le particelle (pixel)*/
    private float spawnRadius;

    /**a quanto sommano le probabilità (qualsiasi intero positivo va bene)*/
    private int totProb;

    private float particleRestitution;

    /**informazioni sulle particelle che si possono generare*/
    private class ParticleInfo {
        String region;
        int prob;
        float minVx, maxVx; //minima e massima velocità iniziale su x
        float minVy, maxVy; //minima e massima velocità iniziale su y
        float minTTL, maxTTL; //minimo e massime time to live della particella
        boolean rotate; //true se la particella deve ruotare, false altrimenti
    }

    /**lista delle possibili particelle che si possono creare*/
    private ArrayList<ParticleInfo> particles;

    /**map per cachare i risultati delle regioni*/
    private HashMap<Integer, TextureRegion> regionsMap;

    /**mondo 2d per creare le particelle fisiche*/
    private com.pizzaroof.sinfulrush.actors.physics.World2D world;

    /**atlas dove prendere le particelle*/
    private TextureAtlas atlas;

    /**gruppo dove aggiungere le particelle*/
    private Group particleGroup;

    /**pool per le particelle (gestita internamente all'emitter)*/
    private PhysicParticlePool particlePool;

    /**particle callback*/
    private PPECallback particleCallback;

    /**inizializza emettitore dal file.
     * @param path percorso per il file che identifica l'emettitore
     * @param atlas atlas per le particelle
     * @param minParticleRadius minimo raggio particella
     * @param maxParticleRadius massimo raggio particella*/
    public PhysicParticleEmitter(String path, TextureAtlas atlas, int minParticleRadius, int maxParticleRadius) {
        particles = new ArrayList<>();
        spawnPoint = new Vector2();
        regionsMap = new HashMap<>();
        this.minRadius = minParticleRadius;
        this.maxRadius = maxParticleRadius;
        this.atlas = atlas;

        particleCallback = null;

        load(path);
    }

    /**metodo usato per generare effettivamente le particelle*/
    public void generateParticles() {
        int num = com.pizzaroof.sinfulrush.util.Utils.randInt(minCount, maxCount); //numero di particelle da generare a caso
        for(int i=0; i<num; i++) { //genera num particelle
            Color color = minColor.cpy();
            color.lerp(maxColor, com.pizzaroof.sinfulrush.util.Utils.randFloat()); //colore a caso tra min e max (fai linear interpolation con coefficiente casuale)

            int rp = com.pizzaroof.sinfulrush.util.Utils.randInt(1, totProb); //particella random, seguendo la distribuzione data in input
            int particleIndex = 0;
            for(int j=0; j<particles.size(); j++)
                if(particles.get(j).prob >= rp) {
                    particleIndex = j;
                    break;
                }
                else
                    rp -= particles.get(j).prob;

            int radius = com.pizzaroof.sinfulrush.util.Utils.randInt(minRadius, maxRadius); //raggio a caso
            particlePool.setNextRadius(radius); //setta il raggio per la nuova particella (NB: in realtà potrebbe non essere usata se si ricicla una particella vecchia)

            Vector2 initPoint = com.pizzaroof.sinfulrush.util.Utils.randomPointInCircle(spawnPoint, spawnRadius); //punto a caso nel cerchio di spawn
            initPoint.x /= world.getPixelPerMeter();
            initPoint.y /= world.getPixelPerMeter();

            if(!regionsMap.containsKey(particleIndex)) //cacha la regione corrispondente
                regionsMap.put(particleIndex, atlas.findRegion(particles.get(particleIndex).region));
            PhysicParticle particle = particlePool.obtain();
            float ttl = com.pizzaroof.sinfulrush.util.Utils.randFloat(particles.get(particleIndex).minTTL, particles.get(particleIndex).maxTTL);
            particle.init(initPoint, regionsMap.get(particleIndex), ttl, particles.get(particleIndex).rotate); //inizializza partiella
            particle.setCallback(particleCallback);

            particle.setColor(color);

            //prendi le velocità dalla particella corrispondente
            float vx = com.pizzaroof.sinfulrush.util.Utils.randFloat(particles.get(particleIndex).minVx, particles.get(particleIndex).maxVx);
            float vy = com.pizzaroof.sinfulrush.util.Utils.randFloat(particles.get(particleIndex).minVy, particles.get(particleIndex).maxVy);
            particle.getBody().setLinearVelocity(vx, vy);
            particleGroup.addActor(particle);
        }
    }

    //tutti questi parametri devono essere settati a mano prima di poter usare l'emettitore

    public void setColors(Color minColor, Color maxColor) {
        this.minColor = minColor;
        this.maxColor = maxColor;
    }

    public void setParticleCount(int minCount, int maxCount) {
        this.minCount = minCount;
        this.maxCount = maxCount;
    }

    public void setParticleCallback(PPECallback callback) {
        particleCallback = callback;
    }

    public void setSpawnPoint(float x, float y) {
        this.spawnPoint.set(x, y);
    }

    public void setSpawnPoint(Vector2 spawnPoint) {
        setSpawnPoint(spawnPoint.x, spawnPoint.y);
    }

    public void setSpawnRadius(float spawnRadius) {
        this.spawnRadius = spawnRadius;
    }

    public void setParticleGroup(Group particleGroup) {
        this.particleGroup = particleGroup;
    }

    /**@param selfCollision le particelle di quest'emitter entrano in collisione tra loro?*/
    public void setWorld(World2D world, boolean selfCollision, boolean environmentCollision) {
        this.world = world;
        particlePool = new PhysicParticlePool(world, selfCollision, environmentCollision);
        particlePool.setNextRestitution(particleRestitution);
    }

    private void load(String path) {
        try {
            BufferedReader reader = Utils.getInternalReader(path);
            String strs[] = reader.readLine().split(" ");
            int N = Integer.parseInt(strs[0]);
            particleRestitution = Float.parseFloat(strs[1]);
            totProb = 0;
            for(int i=0; i<N; i++) {
                ParticleInfo info = new ParticleInfo();
                strs = reader.readLine().split(" "); //leggi parametri come specificato sopra
                info.region = strs[0];
                info.minVx = Float.parseFloat(strs[1]);
                info.maxVx = Float.parseFloat(strs[2]);
                info.minVy = Float.parseFloat(strs[3]);
                info.maxVy = Float.parseFloat(strs[4]);
                info.minTTL = Float.parseFloat(strs[5]);
                info.maxTTL = Float.parseFloat(strs[6]);
                info.rotate = Boolean.parseBoolean(strs[7]);
                info.prob = Integer.parseInt(strs[8]);
                totProb += info.prob;
                particles.add(info);
            }
            reader.close();
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
}
