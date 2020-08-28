package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.Enemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.FriendEnemy;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies.PlatformEnemy;
import com.pizzaroof.sinfulrush.util.PlayerPower;

import java.util.ArrayList;

/**gameplay screen in cui mettiamo pubblicità*/
public class AdGameplayScreen extends PhaseGameplayScreen {

    private boolean reactivateLevel;

    protected int exitsWithoutInterstitial;

    protected boolean adOn;

    public AdGameplayScreen(NGame game, boolean goingUp, String directory, int charmaps, PlayerPower powers, int exitsWithoutInterstitial) {
        super(game, goingUp, directory, charmaps, powers);
        reactivateLevel = false;
        this.exitsWithoutInterstitial = exitsWithoutInterstitial;
        adOn = true;
    }

    @Override
    public void onPlayerDied(boolean firstDeath) {
        super.onPlayerDied(firstDeath);

        //alla morte del giocatore mostiamo un interstitial
        if(!firstDeath)
            getGame().showInterstitial();
    }

    @Override
    public void updateLogic(float delta) {
        super.updateLogic(delta);
        if(reactivateLevel && !isInPause()) {
            keepPlayingLevel();
            getSoundManager().playSoundtrack(isBossActive() ? getBossLoopSoundtrack() : getSoundtrackPath());
            reactivateLevel = false;
        }
    }

    public void setReactivateLevel(boolean rl) {
        reactivateLevel = rl;
    }

    public void onErrorPlayingVideo() {
    }

    /**chiamato per continuare a giocare il livello dopo una morte.
     * in sostanza è il reward per aver guardato un video*/
    public void keepPlayingLevel() {
        if(player.getHp() <= 0) {
            Platform resp = player.getRespawnPlatform();
            player.setHp(player.getMaxHp());
            player.instantMoveOnFirstPlatform();

            ArrayList<Enemy> enemiesToKill = new ArrayList<>();
            for (Actor a : enemiesGroup.getChildren()) //uccidiamo tutti i nemici
                if (a instanceof Enemy && ((Enemy) a).isInCameraView() &&
                    !(a instanceof FriendEnemy))
                    enemiesToKill.add((Enemy) a);

            for (Enemy e : enemiesToKill) {
                //se il nemico sulla piattaforma su cui stiamo per respawnare ha dei figli, non glieli facciamo spawnare
                if(e instanceof PlatformEnemy && ((PlatformEnemy) e).getMyPlatform().equals(resp))
                    ((PlatformEnemy)e).destroyChildren();
                e.takeDamage(Constants.INFTY_HP, null);
            }
        }
    }
}
