package com.pizzaroof.sinfulrush.missions;

import com.badlogic.gdx.Preferences;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**gestisce missioni attive/completate/mancanti.
 *
 * Ogni missione ha un identificativo che non cambia nel corso delle versioni: ordId.
 * Il file prefs contiene una bitmask che indica se la missione è completa o no (la bitmask è indicizzata con ordId).
 * Ogni missione ha un id univoco, ma che può cambiare nel corso delle versioni.
 * Le missioni vengono ordinate in base all'id mutevole. I blocchi di missioni sono definiti da quest'ordinamento (le prime x missioni danno il primo blocco, e cosi via)
 *
 * E' possibile aggiungere nuovi blocchi in mezzo a blocchi esistenti: basta modificare gli id mutevoli, shiftando le cose
 * E' possibile spostare una missione da un blocco all'altro: come prima, basta modificare id mutevole
 * Può essere implementata la rimozione di una missione (basta omettere qualche ordId dalle missioni totali)
 *
 * ---> il blocco attivo è il primo blocco in cui c'è una missione non completata (questo blocco potrebbe cambiare alla modifica di una versione!!!)
 *
 * Il file pref che mantiene i progressi sulle varia missioni: contiene coppie (ordId, progresso) per le missioni in cui il progresso è >0 (ma non quelle completate, quelle completate sono solo nella bitmask)
 *
 * */
public class MissionManager {

    /**quante missioni per gruppo?*/
    public static final int MISSIONS_PER_GROUP = 3;

    /**quante missioni in totale?*/
    private static final int NUM_MISSIONS = MISSIONS_PER_GROUP * 12; //6

    /**indice della missione attiva*/
    private int activeIndex;
    private ArrayList<Mission> allMissions;

    /**in realtà non servirebbe salvarle, sono calcolabili da allMission, lo facciamo per efficienza*/
    private ArrayList<Mission> activeMissions, completedMissions;

    private Preferences preferences;

    private NGame game;

    public MissionManager(NGame game, Preferences preferences) {
        this.game = game;
        this.preferences = preferences;
        loadMissions();
    }

    private void loadMissions() {
        long bitmask = preferences.getLong(Constants.MISSIONS_PREFS, 0); //bitmask delle missioni: 1 fatta, 0 non fatta
        //bitmask = Math.max(bitmask, (1L<<21)-1); //per sbloccare fino a cimitero

        allMissions = new ArrayList<>();
        for(int i=0; i<NUM_MISSIONS; i++) { //ordId resta sempre ordine di inserimento (<--- modifica qui se vuoi implementare rimozione missioni)
            Mission msn = new Mission(i, getMissionIdFromImmutableId(i));

            if(Utils.bitmaskSet(bitmask, i)) //completata già
                msn.setNumReached(msn.getNumToReach());
            allMissions.add(msn);
        }
        Collections.sort(allMissions, new Comparator<Mission>() { //ordiniamo per id: otteniamo il vero ordinamento delle missioni
            @Override
            public int compare(Mission o1, Mission o2) {
                return o1.getId() - o2.getId();
            }
        });

        activeIndex = getActiveIndex(allMissions);

        //non è detto che le missioni siano: completate | attive | rimanenti perché possiamo aggiungere missioni in mezzo in altre versioni
        //quindi la situazione è completate | attive | rimanenti/completate
        if(activeIndex == -1) {
            activeMissions = new ArrayList<>();
            completedMissions = allMissions;
        } else {
            activeMissions = new ArrayList<>();
            completedMissions = new ArrayList<>();
            for(int i=0; i<activeIndex; i++) //prima delle attive sono tutte completate
                completedMissions.add(allMissions.get(i));
            for(int i=activeIndex+MISSIONS_PER_GROUP; i<allMissions.size(); i+=MISSIONS_PER_GROUP) //dopo le attive, inseriamo solo quelle in cui tutto il gruppo è completato
                if(isGroupCompleted(i))
                    for(int j=i; j<i+MISSIONS_PER_GROUP; j++)
                        completedMissions.add(allMissions.get(j));

            for(int i=activeIndex; i<activeIndex+MISSIONS_PER_GROUP; i++)
                activeMissions.add(allMissions.get(i));
        }

        //leggiamo ora informazioni sulle missioni su cui sono stati fatti progressi
        String activeStr[] = preferences.getString(Constants.ACTIVE_MISSION_PREFS, "").split(" ");
        HashMap<Integer, Integer> progress = new HashMap<>(); //coppie (ordId, progresso) salvate sul file prefs
        if(activeStr.length%2 == 0) //altrimenti c'è qualche problema col file...
            for(int i=0; i<activeStr.length; i+=2) //parsing delle coppie
                progress.put(Integer.parseInt(activeStr[i]), Integer.parseInt(activeStr[i+1]));
        for(Mission ms : allMissions) { //aggiorna valori attuali per missioni
            int r = 0;
            if(progress.containsKey(ms.getOrderId()))
                r = progress.get(ms.getOrderId());
            ms.setNumReached(Math.max(ms.getNumReached(), r));
        }
    }

    /**indice della prima missione attiva (e poi le altre seguono sequenzialmente), assumendo che allMissions sia già stato ordinato
     * @return -1 se non ci sono missioni attive*/
    private int getActiveIndex(ArrayList<Mission> all) {
        for(int i=0; i<all.size(); i++)
            if(!all.get(i).isCompleted())
                return (i / MISSIONS_PER_GROUP) * MISSIONS_PER_GROUP;
        return -1;
    }

    /**il gruppo che comincia ad @index è completo?*/
    private boolean isGroupCompleted(int index) {
        for(int i=index; i<index+MISSIONS_PER_GROUP; i++)
            if(!allMissions.get(i).isCompleted())
                return false;
        return true;
    }

    public void putMissionsOnPrefs() {
        long bitmask = 0;

        for(int i=0; i<allMissions.size(); i++) //metti 1 alle completate
            if(allMissions.get(i).isCompleted())
                bitmask |= (1L<<(allMissions.get(i).getOrderId())); //devo usare l'order id, perché sul file sono memorizzate nell'altro modo
        preferences.putLong(Constants.MISSIONS_PREFS, bitmask);

        //salva progressi sulle missioni
        StringBuilder prog = new StringBuilder();
        boolean first = true;
        for(Mission ms : allMissions)
            if(!ms.isCompleted() && ms.getNumReached() > 0) { //non è completata, ma abbiamo fatto dei progressi: salviamoli
                String str = ms.getOrderId()+" "+ms.getNumReached();
                prog.append(first ? str : " "+str);
                first = false;
            }

        preferences.putString(Constants.ACTIVE_MISSION_PREFS, prog.toString());
        preferences.putInteger(Constants.GOLD_PREF, game.getGold());
    }

    /**da chiamare quando ci sono degli aggiornamenti nelle missioni attive:
     * cambia le eventuali missioni attive (se tutto il blocco è stato completato)*/
    public void updateActiveMissions() {
        if(activeMissions.size() > 0) {
            boolean comp = true;
            for (Mission m : activeMissions)
                if (!m.isCompleted())
                    comp = false;

            if (comp) { //tutto blocco completato
                completedMissions.addAll(activeMissions);
                activeMissions.clear();
                activeIndex += MISSIONS_PER_GROUP;
                for (int i = activeIndex; i < allMissions.size(); i += MISSIONS_PER_GROUP)
                    if (!isGroupCompleted(i)) { //trovato nuovo gruppo
                        activeIndex = i;
                        for (int j = i; j < i + MISSIONS_PER_GROUP; j++)
                            activeMissions.add(allMissions.get(j));
                        break;
                    }
            }
        }
    }

    /**da chiamare all'inizio di una partita per fare il backup dei dati delle missioni*/
    public void makeProgressBackup() {
        for(Mission m : activeMissions)
            m.setStashProgress(m.getNumReached());
    }

    /**rimuoviamo progressi fatti*/
    public void undoProgress() {
        for(Mission m : activeMissions)
            m.setNumReached(m.getStashedProgress());
    }

    public ArrayList<Mission> getActiveMissions() {
        return activeMissions;
    }

    public ArrayList<Mission> getCompletedMissions() {
        return completedMissions;
    }

    public ArrayList<Mission> getAllMissions() {
        return allMissions;
    }

    /**in base all'id non mutevole, restituisce l'id, che verrà poi usato per ordinare*/
    private int getMissionIdFromImmutableId(int ordId) {
        return ordId; //hanno stesso ordinamento
    }

}
