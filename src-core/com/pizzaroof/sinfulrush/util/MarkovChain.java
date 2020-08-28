package com.pizzaroof.sinfulrush.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.RandomXS128;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

/**catena di markov. Gli stati vanno da 0 a n-1, se si vuole dare un significato agli stati
 * bisogna definire la mappa di traduzione*/
public class MarkovChain<T> {

    /**
     * matrice di transizione
     */
    private int transitionMatrix[][];

    /**
     * stato attuale
     */
    private int actualState;

    /**
     * associa ad uno stato (tra 0 e n-1) un oggetto
     */
    private HashMap<Integer, T> translationMap;

    /**
     * generatore di numeri casuali
     */
    private RandomXS128 rand;

    /**
     * creiamo la catena di markov partendo da un file,
     * il file deve avere n righe e ogni riga deve avere n colonne. Ogni riga deve sommare ad 1: deve in
     * sostanza contenere la matrice di transizione
     */
    public MarkovChain(String path) {
        createFromFile(path);
        translationMap = new HashMap<>();
        setInitialState(0); //di default mettiamo 0 come stato iniziale
        rand = new RandomXS128();
    }

    /**
     * ci spostiamo di stato
     */
    public int moveState() {
        int ind = 0;
        int rnd = rand.nextInt(100); //v.a. uniforme in [0, 99]

        int sum = transitionMatrix[actualState][0]; //prendi il più piccolo prefisso che supera il valore preso a caso
        //potrebbe essere fatto con ricerca binaria, per velocizzare le cose
        while (sum <= rnd) {
            ind++;
            sum += transitionMatrix[actualState][ind];
        }

        actualState = ind;
        return actualState;
    }

    /**stato attuale*/
    public int getActualState() {
        return actualState;
    }

    /**ci spostiamo di stato e facciamo subito la traduzione*/
    public T moveTranslatedState() {
        return translationMap.get(moveState());
    }

    public T getActualTraslatedState() {
        return translationMap.get(actualState);
    }

    /** setta stato iniziale: bisogna darlo per forza come intero, perchè non è detto che possiamo usare la traduzione inversa*/
    public void setInitialState(int initState) {
        actualState = initState;
    }

    /**aggiunge una traduzione: allo stato @state è associato l'oggetto @obj*/
    public void addTranslation(int state, T obj) {
        translationMap.put(state, obj);
    }

    public int numberOfStates() {
        return transitionMatrix.length;
    }

    /**legge la matrice di transizione da un file (n righe, n colonne che danno la matrice di transizione)*/
    private void createFromFile(String path) {
        try {
            BufferedReader reader = Utils.getInternalReader(path);
            String strs[] = reader.readLine().split(" "); //usa prima riga per sapere quanti stati ci sono
            int n = strs.length;
            transitionMatrix = new int[n][n];

            int sum = 0;
            for (int j = 0; j < n; j++) { //sistema la prima riga {
                transitionMatrix[0][j] = Integer.parseInt(strs[j]);
                sum += transitionMatrix[0][j];
            }

            if (sum != 100) //una riga che non somma a 1...
                Gdx.app.log("ERROR", path + " non è stocastica: riga 0");

            for(int i = 1; i < n; i++) { //finisci di leggere il file e creare le matrici
                strs = reader.readLine().split(" ");
                sum = 0;
                for(int j = 0; j < n; j++) {
                    transitionMatrix[i][j] = Integer.parseInt(strs[j]);
                    sum += transitionMatrix[i][j];
                }

                if (sum != 100) //una riga che non somma a 1...
                    Gdx.app.log("ERROR", path + " non è stocastica: riga " + i);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**crea una catena di markov di stringhe dal file.
     * La prima parte del file è la matrice di transizione (per poter essere letta), l'ultima riga contiene l'id dello stato iniziale (intero), e poi
     * n stringhe che rappresentano gli n stati associati*/
    public static MarkovChain<String> fromFile(String path) {
        if(path == null) return null;

        MarkovChain<String> mc = new MarkovChain<>(path);
        String str[] = null;
        try {
            BufferedReader reader = Utils.getInternalReader(path);
            for(int i=0; i<mc.numberOfStates(); i++) reader.readLine();
            str = reader.readLine().split(" ");
            reader.close();
        }catch(IOException e)  {
            e.printStackTrace();
        }
        //ultima riga ha stato iniziale e stringhe associate
        mc.setInitialState(Integer.parseInt(str[0]));
        for(int i=1; i<=mc.numberOfStates(); i++)
            mc.addTranslation(i-1, str[i].equals("null") ? null : str[i]); //si può usare la parola "null" nel file per riferirsi all'oggetto nullo
        return mc;
    }
}