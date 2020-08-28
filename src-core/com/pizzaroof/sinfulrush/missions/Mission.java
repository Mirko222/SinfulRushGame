package com.pizzaroof.sinfulrush.missions;

import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.util.Pair;

import java.util.HashSet;


/**definisce una missione*/
public class Mission {

    /**ha un id usato per memorizzarlo su file
     * è un id sequenziale (appena viene aggiunta una missione l'id aumenta)*/
    private int saveId;

    /**è l'id che indica l'ordine effettivo delle missioni, quindi possono essere salvate in un ordine
     * e poi usate in un ordine diverso.
     * Questo permette di aggiungere nuovi missioni (aumentando l'id sequenziale) e poi riordinarle modificando gli id
     * NB: gli id cambiano da versione a versione, i saveId restano uguali*/
    private int id;

    /**ogni missione prevederà di fare qualcosa un certo numero di volte (eventualemente 1)*/
    private int numToReach;

    /**quante volte è stato raggiunto l'obiettivo?*/
    private int numReached;

    /**mantiene una copia di numReached, in modo da poter fare l'undo se si esce in partita*/
    private int stashProgress;

    /**ricompensa in monete della missione*/
    private int reward;

    /**la missione deve essere effettuata in una sola partita?*/
    private boolean inOneGame;

    /**tipo di missione*/
    private MissionType type;

    /**filtro sul tipo di nemici da considerare per la missione
     * es: uccidi 5 nemici volanti*/
    private EnemyType enemyTypeFilter;

    /**filtro sul tipo di bonus*/
    private BonusType bonusTypeFilter;

    /**filtro sul livello*/
    private LevelType levelTypeFilter;

    /**filtro su un nemico specifico, identificato da directory*/
    private HashSet<String> specificEnemyFilter;

    /**flag che dice se si devono uccidere nemici ghiacciati nella missione,
     * ha senso solo in certi tipi di missione ovviamente (nelle kill_enemies)*/
    private boolean killFrozenEnemies;

    /**dobbiamo fare kill durante rage?*/
    private boolean killDuringRage;

    /**non consideriamo questo tipo di nemici nelle missioni*/
    private EnemyType removeThisTypeFilter;

    /**vari tipi di missione possibili*/
    public enum MissionType {
        NR_GAMES, //gioca un certo numero di partite
        KILL_ENEMIES, //uccidi un certo numero di nemici
        SAVE_FRIENDS, //salva un certo numero di amici
        TAKE_BONUS, //prendi un certo numero di volte un bonus
        SCORE_POINTS, //raggiungi un certo punteggio
        JUMP_PLATFORMS, //salta un certo numero di piattaforme
        BUY_PLAYER, //compra un personaggio al negozio
        MAKE_DAMAGE, //fai un certo numero di danni
        KILL_BOSS //uccidi tot boss
    }

    public enum EnemyType {
        FLYING,
        PLATFORM,
        FRIEND
    }

    public enum BonusType {
        WEAPON, //arma generica
        MAG_STONE, //gemma magica generica
        SCEPTRE,
        SWORD,
        PUNCH,
        LIGHTNING,
        WIND,
        ICE,
        SLOWTIME,
        HEAL
    }

    public enum LevelType {
        TUTORIAL,
        HELL,
        CEMETERY
    }

    /**@param id identificativo per i gruppi (mutevole)
     * @param oid id sequenziale (immutevole)*/
    public Mission(int oid, int id) {
        this.saveId = oid;
        this.id = id;
        init();
    }

    /**obiettivo della missione?*/
    public void setScope(int toReach, int reached) {
        numToReach = toReach;
        numReached = reached;
    }

    public void setRemoveThisTypeFilter(EnemyType type) {
        this.removeThisTypeFilter = type;
    }

    public EnemyType getRemoveThisTypeFilter() {
        return removeThisTypeFilter;
    }

    public void setNumToReach(int ntr) {
        numToReach = ntr;
    }

    public int getOrderId() {
        return saveId;
    }

    public int getId() {
        return id;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }

    public int getReward() {
        return reward;
    }

    public int getNumToReach() {
        return numToReach;
    }

    public int getNumReached() {
        return numReached;
    }

    public void setNumReached(int nr) {
        numReached = Math.min(nr, getNumToReach());
    }

    public void setType(MissionType type) {
        this.type = type;
    }

    public void setInOneGameFlag(boolean oneGame) {
        inOneGame = oneGame;
    }

    public void setEnemyTypeFilter(EnemyType filter) {
        enemyTypeFilter = filter;
    }

    public void setBonusTypeFilter(BonusType filter) {
        bonusTypeFilter = filter;
    }

    public void setLevelTypeFilter(LevelType filter) {
        levelTypeFilter = filter;
    }

    public boolean isInOneGame() {
        return inOneGame;
    }

    public BonusType getBonusTypeFilter() {
        return bonusTypeFilter;
    }

    public EnemyType getEnemyTypeFilter() {
        return enemyTypeFilter;
    }

    public HashSet<String> getSpecificEnemyFilter() {
        return specificEnemyFilter;
    }

    public LevelType getLevelTypeFilter() {
        return levelTypeFilter;
    }

    public void setKillFrozenEnemies(boolean kfe) {
        killFrozenEnemies = kfe;
    }

    public boolean isKillFrozenEnemies() {
        return killFrozenEnemies;
    }

    public void setStashProgress(int v) {
        stashProgress = v;
    }

    public int getStashedProgress() {
        return stashProgress;
    }

    public void setSpecificEnemyFilter(String directory) {
        this.specificEnemyFilter = new HashSet<>();
        specificEnemyFilter.add(directory);
    }

    public void setKillDuringRage(boolean killDuringRage) {
        this.killDuringRage = killDuringRage;
    }

    public boolean isKillDuringRage() {
        return killDuringRage;
    }

    public void setSpecificEnemyFilter(HashSet<String> directories) {
        this.specificEnemyFilter = directories;
    }

    /**completata quando abbiamo effettuato il numero corretto di volte quello che dovevamo fare*/
    public boolean isCompleted() {
        return numReached == numToReach;
    }

    public MissionType getType() {
        return type;
    }

    /**inizializza la missione, in base all'orderId*/
    private void init() {
        numReached = 0;
        switch(saveId) {
            case 0: //gioca 3 partite
                setNumToReach(3);
                setType(MissionType.NR_GAMES);
                setReward(100);
                break;

            case 1: //uccidi 25 nemici
                setNumToReach(25);
                setType(MissionType.KILL_ENEMIES);
                setRemoveThisTypeFilter(EnemyType.FRIEND); //gli amici non contano come nemici uccisi
                setReward(150);
                break;

            case 2: //salva 5 amici in una partita
                setNumToReach(5);
                setType(MissionType.SAVE_FRIENDS);
                setInOneGameFlag(true);
                setReward(200);
                break;

            case 3: //uccidi 8 amici
                setNumToReach(8);
                setType(MissionType.KILL_ENEMIES);
                setEnemyTypeFilter(EnemyType.FRIEND);
                setReward(200);
                break;

            case 4: //prendi 3 armi
                setNumToReach(3);
                setType(MissionType.TAKE_BONUS);
                setBonusTypeFilter(BonusType.WEAPON);
                setReward(150);
                break;

            case 5: //compra un altro giocatore
                setNumToReach(1);
                setType(MissionType.BUY_PLAYER);
                setInOneGameFlag(true);
                setReward(150);
                break;

            case 6: //uccidi 5 cerberi in una partita
                setNumToReach(5);
                setType(MissionType.KILL_ENEMIES);
                setSpecificEnemyFilter(Constants.CERBERUS_DIRECTORY);
                setInOneGameFlag(true);
                setReward(200);
                break;

            case 7: //70 punti inferno
                setNumToReach(70);
                setType(MissionType.SCORE_POINTS);
                setLevelTypeFilter(LevelType.HELL);
                setInOneGameFlag(true);
                setReward(200);
                break;

            case 8: //uccidi 25 nemici con scettro
                setNumToReach(25);
                setType(MissionType.KILL_ENEMIES);
                setBonusTypeFilter(BonusType.SCEPTRE);
                setRemoveThisTypeFilter(EnemyType.FRIEND);
                setReward(200);
                break;

            case 9: //usa 3 fulmini in una partita
                setNumToReach(3);
                setType(MissionType.TAKE_BONUS);
                setBonusTypeFilter(BonusType.LIGHTNING);
                setInOneGameFlag(true);
                setReward(300);
                break;

            case 10: //110 score inferno
                setNumToReach(110);
                setType(MissionType.SCORE_POINTS);
                setLevelTypeFilter(LevelType.HELL);
                setInOneGameFlag(true);
                setReward(300);
                break;

            case 11: //raccogli 25 gemme magiche
                setNumToReach(25);
                setType(MissionType.TAKE_BONUS);
                setBonusTypeFilter(BonusType.MAG_STONE);
                setReward(250);
                break;

            case 12: //uccidi 20 gargoyle volanti
                setNumToReach(20);
                setType(MissionType.KILL_ENEMIES);
                HashSet<String> gargoyles = new HashSet<>();
                gargoyles.add(Constants.GARGOYLE_1_FLY_DIRECTORY);
                gargoyles.add(Constants.GARGOYLE_2_FLY_DIRECTORY);
                setSpecificEnemyFilter(gargoyles);
                setReward(250);
                break;

            case 13: //uccidi 20 mini-golem in una partita
                setNumToReach(20);
                setType(MissionType.KILL_ENEMIES);
                setSpecificEnemyFilter(Constants.LAVA_GOLEM_2_DIRECTORY);
                setInOneGameFlag(true);
                setReward(350);
                break;

            case 14: //170 score inferno
                setNumToReach(170);
                setType(MissionType.SCORE_POINTS);
                setLevelTypeFilter(LevelType.HELL);
                setInOneGameFlag(true);
                setReward(350);
                break;

            case 15: //uccidi 10 amici in una partita
                setNumToReach(10);
                setType(MissionType.KILL_ENEMIES);
                setEnemyTypeFilter(EnemyType.FRIEND);
                setInOneGameFlag(true);
                setReward(300);
                break;

            case 16: //rallenta il tempo 20 volte
                setNumToReach(20);
                setType(MissionType.TAKE_BONUS);
                setBonusTypeFilter(BonusType.SLOWTIME);
                setReward(300);
                break;

            case 17: //curati 20 volte
                setNumToReach(20);
                setType(MissionType.TAKE_BONUS);
                setBonusTypeFilter(BonusType.HEAL);
                setReward(300);
                break;

            case 18: //uccidi 15 nemici ghiacciati in una partita
                setNumToReach(15);
                setType(MissionType.KILL_ENEMIES);
                setKillFrozenEnemies(true);
                setInOneGameFlag(true);
                //amici non possono essere ghiacciati, non serve filtro
                setReward(300);
                break;

            case 19: //70 piattaforme senza uccidere malus
                setNumToReach(70);
                setType(MissionType.JUMP_PLATFORMS);
                setInOneGameFlag(true);
                setRemoveThisTypeFilter(EnemyType.FRIEND);
                setReward(300);
                break;

            case 20: //affronta boss
                setNumToReach(233);
                setType(MissionType.JUMP_PLATFORMS);
                setLevelTypeFilter(LevelType.HELL);
                setInOneGameFlag(true);
                setReward(400);
                break;

            case 21: //3 partite cimitero
                setNumToReach(3);
                setType(MissionType.NR_GAMES);
                setLevelTypeFilter(LevelType.CEMETERY);
                setReward(150);
                break;

            case 22: //50 score al cimitero
                setNumToReach(50);
                setType(MissionType.SCORE_POINTS);
                setLevelTypeFilter(LevelType.CEMETERY);
                setInOneGameFlag(true);
                setReward(250);
                break;

            case 23: //20 vampiri
                setNumToReach(20);
                //setInOneGameFlag(true);
                setType(MissionType.KILL_ENEMIES);
                setSpecificEnemyFilter(Constants.VAMPIRE_DIRECTORY);
                setReward(300);
                break;

            case 24: //uccidi 20 nemici con vento
                setNumToReach(20);
                setType(MissionType.KILL_ENEMIES);
                setBonusTypeFilter(BonusType.WIND);
                setReward(200);
                break;

            case 25: //60 score senza nessuna gemma
                setNumToReach(60);
                setType(MissionType.SCORE_POINTS);
                setBonusTypeFilter(BonusType.MAG_STONE);
                setInOneGameFlag(true);
                setLevelTypeFilter(LevelType.CEMETERY);
                setReward(400);
                break;

            case 26: //100 score al cimitero
                setNumToReach(100);
                setType(MissionType.SCORE_POINTS);
                setLevelTypeFilter(LevelType.CEMETERY);
                setInOneGameFlag(true);
                setReward(400);
                break;

            case 27: //uccidi 30 fantasmi grandi
                setNumToReach(30);
                setType(MissionType.KILL_ENEMIES);
                HashSet<String> ghosts = new HashSet<>();
                ghosts.add(Constants.GHOST1_DIRECTORY);
                ghosts.add(Constants.GHOST2_DIRECTORY);
                ghosts.add(Constants.FLYING_GHOST1_DIRECTORY);
                ghosts.add(Constants.FLYING_GHOST2_DIRECTORY);
                setSpecificEnemyFilter(ghosts);
                setReward(200);
                break;

            case 28: //150 score al cimitero
                setNumToReach(150);
                setType(MissionType.SCORE_POINTS);
                setLevelTypeFilter(LevelType.CEMETERY);
                setInOneGameFlag(true);
                setReward(350);
                break;

            case 29: //uccidi 30 nemici durante rage
                setNumToReach(30);
                setType(MissionType.KILL_ENEMIES);
                setKillDuringRage(true);
                setRemoveThisTypeFilter(EnemyType.FRIEND);
                setReward(300);
                break;

            case 30: //uccidi 15 nemici con il ghiaccio
                setNumToReach(15);
                setType(MissionType.KILL_ENEMIES);
                setBonusTypeFilter(BonusType.ICE);
                setReward(400);
                break;

            case 31: //60 nemici con scettro in una partita
                setNumToReach(60);
                setType(MissionType.KILL_ENEMIES);
                setBonusTypeFilter(BonusType.SCEPTRE);
                setInOneGameFlag(true);
                setRemoveThisTypeFilter(EnemyType.FRIEND);
                setReward(300);
                break;

            case 32: //uccidi 300 scheletri
                setNumToReach(300);
                setType(MissionType.KILL_ENEMIES);
                HashSet<String> skulls = new HashSet<>();
                skulls.add(Constants.SKELETON_CHIBI_DIRECTORY);
                skulls.add(Constants.BLUE_SKELETON);
                skulls.add(Constants.GREEN_SKELETON);
                skulls.add(Constants.RED_SKELETON);
                skulls.add(Constants.GIANT_SKELETON1);
                skulls.add(Constants.GIANT_SKELETON2);
                skulls.add(Constants.MINI_SKELETON1_DIRECTORY);
                skulls.add(Constants.MINI_SKELETON2_DIRECTORY);
                skulls.add(Constants.MINI_SKELETON3_DIRECTORY);
                skulls.add(Constants.ARMORED_SKULL_DIRECTORY);
                setSpecificEnemyFilter(skulls);
                setReward(450);
                break;

            case 33: //uccidi 100 zombie grandi
                setNumToReach(100);
                setType(MissionType.KILL_ENEMIES);
                HashSet<String> zombies = new HashSet<>();
                zombies.add(Constants.GIANT_ZOMBIE1);
                zombies.add(Constants.GIANT_ZOMBIE2);
                zombies.add(Constants.GIANT_ZOMBIE3);
                setSpecificEnemyFilter(zombies);
                setReward(400);
                break;

            case 34: //fai 20000 danni
                setNumToReach(20000);
                setType(MissionType.MAKE_DAMAGE);
                setReward(400);
                break;

            case 35: //uccidi boss cimitero
                setNumToReach(1);
                setType(MissionType.KILL_BOSS);
                setLevelTypeFilter(LevelType.CEMETERY);
                setReward(500);
                break;

            default:
                numToReach = 10;
                reward = 50;
        }
    }

    public String getDisplayDescription(LanguageManager languageManager) {
        String text = "";
        switch (languageManager.getActualLanguage()) {
            case EN:
                switch (getOrderId()) {
                    case 0: text = "Play "+getNumToReach()+" games"; break;

                    case 1: text = "Kill "+getNumToReach()+" enemies"; break;

                    case 2: text = "Save "+getNumToReach()+" friends in one game"; break;
                    case 3: text = "Kill "+getNumToReach()+" friends"; break;
                    case 4: text = "Take "+getNumToReach()+" weapons"; break;
                    case 5: text = "Buy one character in the shop"; break;
                    //case : text = "Kill "+getNumToReach()+" flying-enemies in one game"; break;
                    case 6: text = "Kill "+getNumToReach()+" cerberus in one game"; break;

                    case 14:
                    case 10:
                    case 7: text = "Score "+getNumToReach()+" points in "+Constants.HELL_NAME; break;

                    case 8: text = "Kill "+getNumToReach()+" enemies using a sceptre"; break;
                    case 9: text = "Use "+getNumToReach()+" thunder gems in one game"; break;
                    case 11: text = "Take "+getNumToReach()+" magical gems"; break;
                    case 12: text = "Kill "+getNumToReach()+" flying gargoyles"; break;
                    case 13: text = "Kill "+getNumToReach()+" mini-golems in one game"; break;

                    case 15: text = "Kill "+getNumToReach()+" friends in one game"; break;
                    case 16: text = "Slow down time "+getNumToReach()+" times"; break;
                    case 17: text = "Heal yourself "+getNumToReach()+" times"; break;

                    case 18: text = "Kill "+getNumToReach()+" frozen enemies in one game"; break;

                    case 19: text = "Jump "+getNumToReach()+" platforms saving all the friends"; break;

                    case 20: text = "Fight the boss of "+Constants.HELL_NAME; break;

                    //cimitero
                    case 21: text = "Play "+getNumToReach()+" games in "+Constants.CEMETERY_NAME; break;

                    case 28:
                    case 26:
                    case 22: text = "Score "+getNumToReach()+" points in "+Constants.CEMETERY_NAME; break;

                    case 23: text = "Kill "+getNumToReach()+" vampires"; break;

                    case 24: text = "Kill "+getNumToReach()+" enemies using the\nwind gem"; break;

                    case 25: text = "Score "+getNumToReach()+" points in "+Constants.CEMETERY_NAME+"\nwithout magical gems"; break;

                    case 27: text = "Kill "+getNumToReach()+" giant ghosts"; break;

                    case 29: text = "Kill "+getNumToReach()+" enemies during the Rage"; break;

                    case 30: text = "Freeze "+getNumToReach()+" flying-enemies"; break;

                    case 31: text = "Kill "+getNumToReach()+" enemies using a sceptre\nin one game"; break;

                    case 32: text = "Kill "+getNumToReach()+" skeletons"; break;

                    case 33: text = "Kill "+getNumToReach()+" giant zombies"; break;

                    case 34: text = "Deal "+getNumToReach()+" damage\nto the enemies"; break;

                    case 35: text = "Kill the boss of "+Constants.CEMETERY_NAME; break;
                }
                break;

            case IT:
                switch (getOrderId()) {
                    case 0: text = "Gioca "+getNumToReach()+" partite"; break;

                    case 1: text = "Uccidi "+getNumToReach()+" nemici"; break;

                    case 2: text = "Salva "+getNumToReach()+" amici in una partita"; break;
                    case 3: text = "Uccidi "+getNumToReach()+" amici"; break;
                    case 4: text = "Prendi "+getNumToReach()+" armi"; break;
                    case 5: text = "Compra un personaggio al negozio"; break;

                    case 6: text = "Uccidi "+getNumToReach()+" cerberi in una partita"; break;

                    case 14:
                    case 10:
                    case 7: text = "Fai "+getNumToReach()+" punti in "+Constants.HELL_NAME; break;

                    case 8: text = "Uccidi "+getNumToReach()+" nemici con lo scettro"; break;
                    case 9: text = "Usa "+getNumToReach()+" gemme fulmine in una partita"; break;
                    case 11: text = "Prendi "+getNumToReach()+" gemme magiche"; break;
                    case 12: text = "Uccidi "+getNumToReach()+" gargoyle volanti"; break;
                    case 13: text = "Uccidi "+getNumToReach()+" mini-golem in una partita"; break;

                    case 15: text = "Uccidi "+getNumToReach()+" amici in una partita"; break;
                    case 16: text = "Rallenta il tempo "+getNumToReach()+" volte"; break;
                    case 17: text = "Curati "+getNumToReach()+" volte"; break;

                    case 18: text = "Uccidi "+getNumToReach()+" nemici ghiacciati in una partita"; break;

                    case 19: text = "Salta "+getNumToReach()+" piattaforme\nsalvando tutti gli amici"; break;

                    case 20: text = "Affronta il boss di "+Constants.HELL_NAME; break;

                    //cimitero
                    case 21: text = "Gioca "+getNumToReach()+" partite in "+Constants.CEMETERY_NAME; break;

                    case 28:
                    case 26:
                    case 22: text = "Fai "+getNumToReach()+" punti in "+Constants.CEMETERY_NAME; break;

                    case 23: text = "Uccidi "+getNumToReach()+" vampiri"; break;

                    case 24: text = "Uccidi "+getNumToReach()+" nemici con la gemma\ndel vento"; break;

                    case 25: text = "Fai "+getNumToReach()+" punti in "+Constants.CEMETERY_NAME+"\nsenza gemme magiche"; break;

                    case 27: text = "Uccidi "+getNumToReach()+" fantasmi giganti"; break;

                    case 29: text = "Uccidi "+getNumToReach()+" nemici durante il Rage"; break;

                    case 30: text = "Ghiaccia "+getNumToReach()+" nemici volanti"; break;

                    case 31: text = "Uccidi "+getNumToReach()+" nemici con lo scettro\nin una partita"; break;

                    case 32: text = "Uccidi "+getNumToReach()+" scheletri"; break;

                    case 33: text = "Uccidi "+getNumToReach()+" zombie giganti"; break;

                    case 34: text = "Fai "+getNumToReach()+" danni\nai nemici"; break;

                    case 35: text = "Uccidi il boss di "+Constants.CEMETERY_NAME; break;
                }
                break;

            case RO:
                switch(getOrderId()) {
                    case 0: text = "Joacă de "+getNumToReach()+" ori"; break;

                    case 1: text = "Ucide "+getNumToReach()+" inamici"; break;

                    case 2: text = "Salvează "+getNumToReach()+" prieteni într-un joc"; break;
                    case 3: text = "Ucide "+getNumToReach()+" prieteni"; break;
                    case 4: text = "Folosește "+getNumToReach()+" arme"; break;
                    case 5: text = "Cumpără un personaj în magazin"; break;

                    case 6: text = "Ucide "+getNumToReach()+" cerberi într-un joc"; break;

                    case 14:
                    case 10:
                    case 7: text = "Fă "+getNumToReach()+" de puncte în "+Constants.HELL_NAME; break;

                    case 8: text = "Ucide "+getNumToReach()+" inamici folosind sceptrul"; break;
                    case 9: text = "Folosește "+getNumToReach()+" tunete într-un joc"; break;
                    case 11: text = "Folosește "+getNumToReach()+" pietre magice"; break;
                    case 12: text = "Ucide "+getNumToReach()+" gargoyle zburători"; break;
                    case 13: text = "Ucide "+getNumToReach()+" mini-golem într-un joc"; break;

                    case 15: text = "Ucide "+getNumToReach()+" prieteni într-un joc"; break;
                    case 16: text = "Incetinează timpul de "+getNumToReach()+" ori"; break;
                    case 17: text = "Folosește pietre de viața de "+getNumToReach()+" ori"; break;

                    case 18: text = "Ucide "+getNumToReach()+" inamici înghețați într-un joc"; break;

                    case 19: text = "Sari "+getNumToReach()+" platforme salvănd toți prieteni"; break;

                    case 20: text = "Luptă contra bosului din "+Constants.HELL_NAME; break;

                    //cimitero
                    case 21: text = "Joacă de "+getNumToReach()+" ori în "+ Constants.CEMETERY_NAME; break;

                    case 28:
                    case 26:
                    case 22: text = "Fă "+getNumToReach()+" de puncte în "+Constants.CEMETERY_NAME; break;

                    case 23: text = "Ucide "+getNumToReach()+" vampiri"; break;

                    case 24: text = "Ucide "+getNumToReach()+" inamici cu piatra\nvăntului"; break;

                    case 25: text = "Fă "+getNumToReach()+" de puncte în "+Constants.CEMETERY_NAME+"\nfără pietre magice"; break;

                    case 27: text = "Ucide "+getNumToReach()+" fantome mari"; break;

                    case 29: text = "Ucide "+getNumToReach()+" inamici cu Rage"; break;

                    case 30: text = "Îngheață "+getNumToReach()+" inamici zburători"; break;

                    case 31: text = "Ucide "+getNumToReach()+" inamici cu sceptrul\nîntr-un joc"; break;

                    case 32: text = "Ucide "+getNumToReach()+" schelete"; break;

                    case 33: text = "Ucide "+getNumToReach()+" zombie giganți"; break;

                    case 34: text = "Fă "+getNumToReach()+" de daune\nla inamici"; break;

                    case 35: text = "Ucide bosul din "+Constants.CEMETERY_NAME; break;
                }
                break;

            case MG:
                switch (getOrderId()) {
                    case 0: text = "Mila fanamby ianao"+getNumToReach(); break;

                    case 1: text = "Mila hamonoanao "+getNumToReach()+" fahavalo"; break;

                    case 2: text = "Mamonjy ianao "+getNumToReach()+" namana ao\nanatin'ny fanamby iray"; break;
                    case 3: text = "Hamonoanao "+getNumToReach()+" namana"; break;
                    case 4: text = "Mandray ianao "+getNumToReach()+" fitaovam-piadiana"; break;
                    case 5: text = "Mividiana "+getNumToReach()+" amina fivarotana"; break;
                    case 6: text = "Hamonoanao "+getNumToReach()+" alika telo loha\nanatin'ny fanamby iray"; break;

                    case 14:
                    case 10:
                    case 7: text = "Manaova "+getNumToReach()+" vokatra anatin' "+Constants.HELL_NAME; break;

                    case 8: text = "Mila hamonoanao "+getNumToReach()+" fahavalo miaraka\namin'ny tehim-panjaka"; break;
                    case 9: text = "Fampiasa ianao "+getNumToReach()+" vatosoa tselatra\nanatin'ny fanamby iray"; break;
                    case 11: text = "Mandray ianao "+getNumToReach()+" vatosoa"; break;
                    case 12: text = "Hamonoanao "+getNumToReach()+" gargoyle\nmanidina"; break;
                    case 13: text = "Hamonoanao "+getNumToReach()+" mini-golem\nanatin'ny fanamby iray"; break;

                    case 15: text = "Hamonoanao "+getNumToReach()+" namana anatin'ny\nfanamby iray"; break;
                    case 16: text = "Ataovy miadana ny fotona miaraka ny\nvatosoa in "+getNumToReach(); break;
                    case 17: text = "Mikarakara ianao in "+getNumToReach(); break;

                    case 18: text = "Mila hamoanao "+getNumToReach()+" fahavalo nivaingana\nanatin'ny fanamby iray"; break;

                    case 19: text = "Mitsambikinao "+getNumToReach()+" sehatra\nmamonjy namana rehetra"; break;

                    case 20: text = "Hiatrehana ny panjaka "+Constants.HELL_NAME; break;

                    //case 20: text = "Fanamby ny boss amin'ny "+Constants.HELL_NAME; break;

                    //cimitero
                    case 21: text = "Mlalao "+getNumToReach()+" match amin'ny\n"+Constants.CEMETERY_NAME; break;

                    case 28:
                    case 26:
                    case 22: text = "Manaova "+getNumToReach()+" points amin'ny\n"+Constants.CEMETERY_NAME; break;

                    case 23: text = "Hamonoanao "+getNumToReach()+" vampira"; break;

                    case 24: text = "Hamonoanao "+getNumToReach()+" fahavalon'ny miaraka\nny vatosoa fitaratra"; break;

                    case 25: text = "Manaova "+getNumToReach()+" points amin'ny\n"+Constants.CEMETERY_NAME+" tsy mampiasa vatosoa"; break;

                    case 27: text = "Hamonoanao matoatoa\ngoavambe "+getNumToReach(); break;

                    case 29: text = "Hamonoanao fahavalo "+getNumToReach()+" miaraka\nny Rage"; break;

                    case 30: text = "Mivaingana ny fahavalo\nmanidina "+getNumToReach(); break;

                    case 31: text = "Hamonoanao "+getNumToReach()+" fahavalo miaraka ny\ntehim-panjaka ao anaty ny fanamby iray"; break;

                    case 32: text = "Hamonoanao squelette "+getNumToReach(); break;

                    case 33: text = "Hamonoanao "+getNumToReach()+" zombie\ngoavambe"; break;

                    case 34: text = "Manaova "+getNumToReach()+" fahasimbana amin'ny\nfahavalo"; break;

                    case 35: text = "Hamonoanao ny boss "+Constants.CEMETERY_NAME; break;
                }
                break;
        }

        if(getNumToReach() > 1 && !isInOneGame()) text += " ("+getNumReached()+"/"+getNumToReach()+")";
        if(isCompleted()) return "[#FFCE21]"+text+"[]";
        return text;
    }
}
