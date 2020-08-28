package com.pizzaroof.sinfulrush.missions;

import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.screens.BossGameScreen;
import com.pizzaroof.sinfulrush.util.Utils;

import java.util.HashMap;
import java.util.Map;

/**classe che raccoglie dati utili a verificare se è stata fatta una mission*/
public class MissionDataCollector {

    private Mission.LevelType levelType;

    /**hashmap dove salviamo tutte le uccisioni fatte, con relative informazioni:
     * (directory, bonus) -> #kills
     * in sostanza sappiamo quale nemico è stato ucciso con quale arma/bonus*/
    private HashMap<String, HashMap<Mission.BonusType, Integer>> numKills;

    private HashMap<Mission.BonusType, Integer> numBonusTaken;

    private int friendsSaved;

    /**salviamo numero nemici ghiacciati uccisi*/
    private int frozedEnemiesKilled;

    private NGame game;

    /**dati per numero piattaforme senza malus presi*/
    private int lastPlatformFriendKilled, maxGapBetweenPlatforms;

    /**a che punteggio stava quando ha preso la prima gemma?*/
    private int scoreFirstGem;

    private int killDuringRage;

    /**quanti danni sono stati fatti in totale?*/
    private int totDamageMade;

    public MissionDataCollector(Mission.LevelType type, NGame game) {
        this.game = game;
        levelType = type;
        scoreFirstGem = -1;
        totDamageMade = 0;
        friendsSaved = 0;
        killDuringRage = 0;
        frozedEnemiesKilled = 0;
        lastPlatformFriendKilled = 0;
        maxGapBetweenPlatforms = 0;
        numKills = new HashMap<>();
        numBonusTaken = new HashMap<>();
    }

    public void updateFriendsSavings() {
        friendsSaved++;
    }

    public void updateBonusTaken(Mission.BonusType type, Player player) {
        int old = 0;
        if(numBonusTaken.containsKey(type)) old = numBonusTaken.get(type);
        numBonusTaken.put(type, old + 1);

        if(inclusionBonus(type, Mission.BonusType.MAG_STONE) && scoreFirstGem == -1)
            scoreFirstGem = Utils.getScore(player);
    }

    /**chiamato quando @enemy viene ucciso con @deathType*/
    public void updateEnemiesKills(Enemy enemy, Mission.BonusType deathType, Player player, ShaderStage stage) {
        setKills(enemy.getDirectory(), deathType, getKills(enemy.getDirectory(), deathType) + 1);

        if(!getEnemyType(enemy.getDirectory()).equals(Mission.EnemyType.FRIEND))
            totDamageMade += enemy.getMaxHp();

        if(enemy.isFrozen() || enemy.isFreezing() || (deathType != null && deathType.equals(Mission.BonusType.ICE)))
            frozedEnemiesKilled++;

        if(stage.isRageModeOn() && deathType != null && deathType.equals(Mission.BonusType.SWORD) && !getEnemyType(enemy.getDirectory()).equals(Mission.EnemyType.FRIEND))
            killDuringRage++;

        if(getEnemyType(enemy.getDirectory()).equals(Mission.EnemyType.FRIEND)) {
            maxGapBetweenPlatforms = Math.max(maxGapBetweenPlatforms, player.getJumpedPlatforms() - lastPlatformFriendKilled);
            lastPlatformFriendKilled = player.getJumpedPlatforms();
        }
    }

    private void setKills(String enemy, Mission.BonusType type, int value) {
        if(!numKills.containsKey(enemy)) numKills.put(enemy, new HashMap<>());
        numKills.get(enemy).put(type, value);
    }

    private int getKills(String enemy, Mission.BonusType type) {
        if(!numKills.containsKey(enemy)) return 0;
        if(!numKills.get(enemy).containsKey(type)) return 0;
        return numKills.get(enemy).get(type);
    }

    /**aggiona lo stato della missione @mission in base ai dati collezionati.
     *passiamo anche player perché possiamo usarlo per avere qualche info*/
    public void updateMissionStatus(Mission mission, Player player, boolean gameFinished) {
        maxGapBetweenPlatforms = Math.max(maxGapBetweenPlatforms, player.getJumpedPlatforms() - lastPlatformFriendKilled);

        if(mission.isCompleted()) //non ha senso fare altri aggiornamenti
            return;
        if(mission.getLevelTypeFilter() != null && !mission.getLevelTypeFilter().equals(levelType)) //non rispetta filtro sul livello
            return;
        if(mission.getLevelTypeFilter() != null && mission.getLevelTypeFilter().equals(Mission.LevelType.TUTORIAL)) //nessuna missione al tutorial
            return;

        switch (mission.getType()) {
            case NR_GAMES:
                if(gameFinished)
                    mission.setNumReached(mission.getStashedProgress()+1); //ha giocato una partita in più
                break;

            case SAVE_FRIENDS:
                if(mission.isInOneGame()) {
                    if(mission.getNumToReach() <= friendsSaved)
                        mission.setNumReached(mission.getNumToReach());
                } else
                    mission.setNumReached(mission.getStashedProgress() + friendsSaved); //ha salvato giocatori in più
                break;

            case KILL_ENEMIES:

                int goodKills = 0;
                if(!mission.isKillFrozenEnemies() && !mission.isKillDuringRage()) {
                    for (Map.Entry<String, HashMap<Mission.BonusType, Integer>> e : numKills.entrySet()) {
                        if (mission.getSpecificEnemyFilter() != null && !mission.getSpecificEnemyFilter().contains(e.getKey())) //filtro specifico non rispettato
                            continue;
                        Mission.EnemyType thisType = getEnemyType(e.getKey());
                        if (mission.getEnemyTypeFilter() != null && !mission.getEnemyTypeFilter().equals(thisType)) //filtro enemy type non rispettato
                            continue;
                        if(mission.getRemoveThisTypeFilter() != null && mission.getRemoveThisTypeFilter().equals(thisType)) //è uguale al tipo di nemico che devo rimuovere
                            continue;

                        for (Map.Entry<Mission.BonusType, Integer> e2 : e.getValue().entrySet()) {
                            if (mission.getBonusTypeFilter() != null && !mission.getBonusTypeFilter().equals(e2.getKey())) //non va bene come kill: filtro bonus usato non rispettato
                                continue;

                            goodKills += e2.getValue();
                        }
                    }
                }
                else
                    if(mission.isKillFrozenEnemies())
                        goodKills = frozedEnemiesKilled;
                    else
                        if(mission.isKillDuringRage())
                            goodKills = killDuringRage;

                if(mission.isInOneGame()) {
                    if(mission.getNumToReach() <= goodKills)
                        mission.setNumReached(mission.getNumToReach());
                } else
                    mission.setNumReached(mission.getStashedProgress() + goodKills);

                break;

            case TAKE_BONUS:

                int goodBonus = 0;
                for(Map.Entry<Mission.BonusType, Integer> e : numBonusTaken.entrySet())
                    if(mission.getBonusTypeFilter() == null || inclusionBonus(e.getKey(), mission.getBonusTypeFilter()))
                        goodBonus += e.getValue();

                if(mission.isInOneGame()) {
                    if(mission.getNumToReach() <= goodBonus)
                        mission.setNumReached(mission.getNumToReach());
                } else
                    mission.setNumReached(mission.getStashedProgress() + goodBonus);

                break;

            case SCORE_POINTS:
                if(Utils.getScore(player) >= mission.getNumToReach() &&
                        (mission.getBonusTypeFilter() == null || (mission.getBonusTypeFilter().equals(Mission.BonusType.MAG_STONE) && (scoreFirstGem == -1 || mission.getNumToReach() < scoreFirstGem))) ) //missioni speciali: vogliamo farle senza prendere gemme
                    mission.setNumReached(mission.getNumToReach());
                break;

            case JUMP_PLATFORMS:
                if(mission.isInOneGame()) {
                    if(mission.getRemoveThisTypeFilter() != null && mission.getRemoveThisTypeFilter().equals(Mission.EnemyType.FRIEND)) { //tipo particolare: salta tot piattaforme senza uccidere malus
                        if(mission.getNumToReach() <= maxGapBetweenPlatforms)
                            mission.setNumReached(mission.getNumToReach());
                    }
                    else
                        if(mission.getNumToReach() <= player.getJumpedPlatforms())
                            mission.setNumReached(mission.getNumToReach());
                } else
                    mission.setNumReached(mission.getStashedProgress()+player.getJumpedPlatforms());
                break;

            case MAKE_DAMAGE:
                if(mission.isInOneGame()) {
                    if(mission.getNumToReach() <= totDamageMade)
                        mission.setNumReached(mission.getNumToReach());
                } else
                    mission.setNumReached(mission.getStashedProgress()+totDamageMade);
                break;

            case KILL_BOSS:
                if(mission.getNumToReach() <= player.getNumBossKilled())
                    mission.setNumReached(mission.getNumToReach());
                break;
        }

        if(mission.isCompleted()) { //prima non era completa, quindi è stata completata ora... dai i soldi
            game.addGold(mission.getReward());
        }
    }

    /**verifica se i bonus sono uguali, considerando Weapon e MagStone.
     * assumendo: devo fare @b e ho preso @a*/
    private boolean inclusionBonus(Mission.BonusType a, Mission.BonusType b) {
        if(a.equals(b)) return true;
        return ( (b.equals(Mission.BonusType.WEAPON) && (a.equals(Mission.BonusType.PUNCH) || a.equals(Mission.BonusType.SCEPTRE) || a.equals(Mission.BonusType.SWORD)))
                || (b.equals(Mission.BonusType.MAG_STONE) && (a.equals(Mission.BonusType.LIGHTNING) || a.equals(Mission.BonusType.ICE) || a.equals(Mission.BonusType.WIND) || a.equals(Mission.BonusType.SLOWTIME) || a.equals(Mission.BonusType.HEAL))));
    }

    private Mission.EnemyType getEnemyType(String enemy) {
        if(Utils.SNIPER_ENEMIES.contains(enemy) || Utils.SNIPER_HEALERS_ENEMIES.contains(enemy))
            return Mission.EnemyType.FLYING;
        if(Utils.FRIEND_ENEMIES.contains(enemy))
            return Mission.EnemyType.FRIEND;
        //if(Utils.MELEE_ENEMIES.contains(enemy.v1) || Utils.SNIPER_PLATFORM_ENEMIES.contains(enemy.v1))
        //    return Mission.EnemyType.PLATFORM;

        //casi speciali
        /*if(enemy.v1.equals(Constants.LAVA_GOLEM_DIRECTORY) ||
            enemy.v1.equals(Constants.LAVA_GOLEM_3_DIRECTORY) ||
            enemy.v1.equals(Constants.RED_DEMON_DIRECTORY) ||
            enemy.v1.equals(Constants.GIANT_SKELETON1) ||
            enemy.v1.equals(Constants.GIANT_SKELETON2) ||
            enemy.v1.equals(Constants.BLUE_NECROMANCER) ||
            enemy.v1.equals(Constants.GREEN_NECROMANCER) ||
            enemy.v1.equals(Constants.RED_NECROMANCER) ||
            enemy.v1.equals(Constants.GIANT_ZOMBIE1) ||
            enemy.v1.equals(Constants.GIANT_ZOMBIE2) ||
            enemy.v1.equals(Constants.GIANT_ZOMBIE3)) {
            return Mission.EnemyType.PLATFORM;
        }*/

        return Mission.EnemyType.PLATFORM; //i casi speciali attualmente sono tutti platform
    }

}
