package com.pizzaroof.sinfulrush.util;

import com.badlogic.gdx.math.RandomXS128;

/**classe per usare perlin noise 1D*/
public class PerlinNoise {

    /**massimo tempo standard*/
    private static final int DEF_MAX_TIME = 100;

    /**incremento standard per il tempo*/
    public static final float DEF_TIME_INC = 0.1f;

    //tempo attuale
    private float actualTime;
    //incremento del tempo
    private float timeInc;

    /**permutazione casuale*/
    private int permutation[];

    public PerlinNoise() {
        this(DEF_MAX_TIME);
    }


    /**maxTime=lunghezza periodo*/
    public PerlinNoise(int maxTime) {
        buildPermutation(new RandomXS128(), maxTime);
    }

    /**costruiamo perlin noise con un certo seed. maxTime=lunghezza periodo*/
    public PerlinNoise(long seed, int maxTime) {
        //generiamo permutazione casuale
        RandomXS128 rand = new RandomXS128();
        rand.setSeed(seed);
        buildPermutation(rand, maxTime);
    }

    private void buildPermutation(RandomXS128 rand, int maxTime) {
        actualTime = 0;
        timeInc = DEF_TIME_INC;

        permutation = new int[maxTime+1]; //crea una permutazione di maxTime elementi (dopo maxTime volte, i valori si ripetono)
        int tmp[] = new int[maxTime+1];
        for(int i=0; i<=maxTime; i++)
            tmp[i] = i;

        for(int i=maxTime; i>=0; i--) { //mischia (in maniera dipendente dal seed)
            int ind = rand.nextInt(i+1);
            permutation[i] = tmp[ind];
            tmp[ind] = tmp[i];
        }
    }

    /**restituisce il "rumore" ad un certo tempo @time (è un valore in [-0.5, 0.5])*/
    public float noise(float time) {
        int x = (int)Math.floor(time) & 255;
        time -= Math.floor(time);
        float u = fade(time);
        float a = grad(permutation[x], time);
        float b = grad(permutation[x+1], time-1);
        return Utils.cosInterpolate(a, b, u);
    }

    /**perlin noise in [0, 1]*/
    public float noise01(float time) {
        return noise(time) + 0.5f;
    }

    /**perlin noise in [0, 1] usando il tempo interno della classe*/
    public float noise() {
        float ret = noise01(actualTime);
        actualTime = incrementTime(actualTime);
        return ret;
    }

    /**perlin noise in [-1, 1]*/
    public float noise11() {
        return noise() * 2.f - 1.f;
    }

    /**restituisce noise in [0, 1] aumentando il tempo della quantità specificata*/
    public float incNoise(float inc) {
        setTimeInc(inc);
        return noise();
    }

    /**restituisce noise in [-1, 1] aumentando il tempo della quantità specificata*/
    public float incNoise11(float inc) {
        return incNoise(inc) * 2.f - 1.f;
    }

    //gradiente pseudo-casuale (l'hash è psuedocasuale)
    private float grad(int hash, float x) {
        return hash%2 == 0 ? x : -x;
    }

    //fade function, per dare smoothness
    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    public void setTimeInc(float timeInc) {
        this.timeInc = timeInc;
    }

    public int getMaxTime() {
        return permutation.length;
    }

    //restituisce tempo usato nel perlin noise
    public float getActualTime() {
        return actualTime;
    }

    /**permette di modificare il tempo attuale del noise. USALO SOLO SE VERAMENTE NECESSARIO*/
    public void setActualTime(float atime) {
        actualTime = atime;
    }

    /**restituisce @time aumentato*/
    public float incrementTime(float time) {
        time += timeInc;
        if(time >= getMaxTime()-1)
            time -= (getMaxTime()-1);
        return time;
    }
}
