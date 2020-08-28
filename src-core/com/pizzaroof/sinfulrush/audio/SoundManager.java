package com.pizzaroof.sinfulrush.audio;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.util.Utils;

/**classe che gestisce suoni*/
public class SoundManager {

    private AssetManager assetManager;
    private float sfxVolume;
    private float musicVolume;

    /**suono per click di bottoni*/
    private Sound buttonClick; //, jump;
    private Sound sceptreSpawn, iceSpawn, punchHit, punchDamage, bonusTaken, enemyDeath, enemyDeath2, clockTick, bossBallExplosion, friendDeath;
    private Sound swordSwing, swordDamage, slowtime, thunder, wind, playerDeath, rage, sceptreExplosion, bossHurt, bossDeath, bossRoar, bossAttack;
    private DelaySound playerHurt, healthPotion, portal, enemyExplosion;
    private SequenceSound bossExplosion, bossExplosion2;

    private Music currentSoundtrack;
    private String currentSoundtrackName;
    private String nextSoundtrack;
    private float switchDur, switchPassed;

    private Runnable onSoundtrackChanged;

    private Stage stage;

    private static final float VOLUME_MUL_SLOWTIME = 0.25f;

    public SoundManager(AssetManager assetManager, float sfxVolume, float musicVolume) {
        this.assetManager = assetManager;
        this.sfxVolume = sfxVolume;
        this.musicVolume = musicVolume;

        playerHurt = new DelaySound(assetManager, com.pizzaroof.sinfulrush.Constants.PLAYER_HURT_SFX, 0.8f);
        healthPotion = new DelaySound(assetManager, com.pizzaroof.sinfulrush.Constants.HEALTH_POTION_SFX, 1.f);
        portal = new DelaySound(assetManager, com.pizzaroof.sinfulrush.Constants.PORTAL_SFX, 0.6f);
        enemyExplosion = new DelaySound(assetManager, com.pizzaroof.sinfulrush.Constants.EXPLOSION_ENEMY_SFX, 0.25f);
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = com.pizzaroof.sinfulrush.util.Utils.clamp(sfxVolume, 0, 1);
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = com.pizzaroof.sinfulrush.util.Utils.clamp(musicVolume, 0, 1);
        replaySoundtrack();
    }

    public boolean useSfx() {
        return sfxVolume > com.pizzaroof.sinfulrush.Constants.EPS;
    }

    public boolean useMusic() {
        return musicVolume > com.pizzaroof.sinfulrush.Constants.EPS;
    }

    /**fa partire la soundtrack @name istantaneamente*/
    public void playSoundtrack(String name) {
        if((name.equals(currentSoundtrackName) && currentSoundtrack.isPlaying()) || !assetManager.isLoaded(name)) return;
        if(currentSoundtrack != null && currentSoundtrack.isPlaying())
            stopSoundtrack();
        currentSoundtrackName = name;
        currentSoundtrack = assetManager.get(name, Music.class);
        if(!useMusic()) return;
        currentSoundtrack.setLooping(true);
        currentSoundtrack.setVolume(musicVolume);
        currentSoundtrack.play();
    }

    /**richiama play con soundtrack attuale*/
    public void replaySoundtrack() {
        if(currentSoundtrack != null) {
            if(!useMusic()) {
                currentSoundtrack.pause();
                return;
            }
            currentSoundtrack.setLooping(true);
            currentSoundtrack.setVolume(musicVolume);
            currentSoundtrack.play();
        }
    }

    public void setStage(TimescaleStage stage) {
        this.stage = stage;
    }

    private float changeVolumeInterpolation(float p) {
        if(p <= 0.4f) return (0.4f - p) / 0.4f; //decresce linearmente (0, 1), (0.4, 0)
        if(p >= 0.4f && p <= 0.6f) return 0.f; //sta fermo un po'
        return (p - 0.6f) / 0.4f; //cresce linearmente (0.6, 0) (1, 1)
    }

    /**aggiorna soundmanager: necessario solo se si usa change soundtrack*/
    public void update(float delta) {
        if(nextSoundtrack != null) {
            switchPassed += delta;
            float p = switchPassed / switchDur;
            if(p > 0.4f && (!currentSoundtrackName.equals(nextSoundtrack) || !currentSoundtrack.isPlaying()))
                playSoundtrack(nextSoundtrack);
            p = changeVolumeInterpolation(p);
            currentSoundtrack.setVolume(musicVolume * p);
            if(switchPassed > switchDur) {
                nextSoundtrack = null;
                if(onSoundtrackChanged != null)
                    onSoundtrackChanged.run();
                onSoundtrackChanged = null;
            }
        }
        else if(stage instanceof TimescaleStage && currentSoundtrack != null) { //abbassiamo il volume con tempo rallentato (ma non durante gli switch)
            float vol = currentSoundtrack.getVolume();
            boolean tm = ((TimescaleStage) stage).timeMultiplierApplied();
            if(Math.abs(vol - musicVolume * VOLUME_MUL_SLOWTIME) > com.pizzaroof.sinfulrush.Constants.EPS && tm)
                currentSoundtrack.setVolume(musicVolume * VOLUME_MUL_SLOWTIME);
            if(Math.abs(vol - musicVolume) > com.pizzaroof.sinfulrush.Constants.EPS && !tm)
                currentSoundtrack.setVolume(musicVolume);
        }

        //update dei delay sounds
        playerHurt.update(delta);
        healthPotion.update(delta);
        portal.update(delta);
        enemyExplosion.update(delta);
        if(bossExplosion != null && stage instanceof TimescaleStage)
            bossExplosion.update(delta * ((TimescaleStage)stage).getTimeMultiplier());
        if(bossExplosion2 != null && stage instanceof TimescaleStage)
            bossExplosion2.update(delta * ((TimescaleStage)stage).getTimeMultiplier());
    }

    /**stoppa la soundtrack attuale*/
    public void stopSoundtrack() {
        currentSoundtrack.stop();
    }

    public void pauseSoundtrack() {
        if(currentSoundtrack != null && useMusic())
            currentSoundtrack.pause();
    }

    public void resumeSoundtrack() {
        if(currentSoundtrack != null && useMusic())
            currentSoundtrack.play();
    }

    /**passa da una soundtrack all'altra gradualmente*/
    public void changeSoundtrack(String newSoundtrack, float time) {
        if(!useMusic()) return;
        this.nextSoundtrack = newSoundtrack;
        this.switchPassed = 0;
        this.switchDur = time;
    }

    public void changeSoundtrackIfNew(String newSoundtrack, float time) {
        if(currentSoundtrackName == null || !currentSoundtrackName.equals(newSoundtrack))
            changeSoundtrack(newSoundtrack, time);
    }

    /**esegue suono click, probabilmente di un bottone*/
    public void click() {
        //il pitch casuale aiuta a creare un po' di variazione
        buttonClick = playSound(buttonClick, com.pizzaroof.sinfulrush.Constants.BUTTON_CLICK_SFX, randomPitch(), 0);
    }

    public void sceptreSpawn() {
        sceptreSpawn = playSound(sceptreSpawn, com.pizzaroof.sinfulrush.Constants.SCEPTRE_SPAWN_SFX, randomPitch(), 0);
    }

    public void bossBallExplosion() {
        bossBallExplosion = playSound(bossBallExplosion, com.pizzaroof.sinfulrush.Constants.BOSS_BALL_EXPLOSION_SFX, smallPitch(), 0);
    }

    public void iceSpawn() {
        iceSpawn = playSound(iceSpawn, com.pizzaroof.sinfulrush.Constants.ICE_ACTIVATION_SFX, randomPitch(), 0);
    }

    public void punchHit() {
        punchHit = playSound(punchHit, com.pizzaroof.sinfulrush.Constants.PUNCH_HIT_SFX, randomPitch(), 0);
    }

    public void punchDamage() {
        punchDamage = playSound(punchDamage, com.pizzaroof.sinfulrush.Constants.PUNCH_DAMAGE_SFX, randomPitch(), 0);
    }

    public void bossAttack() {
        bossAttack = playSound(bossAttack, com.pizzaroof.sinfulrush.Constants.BOSS_ATTACK_SFX, reallySmallPitch(), 0);
    }

    public void bonusTaken() {
        bonusTaken = playSound(bonusTaken, com.pizzaroof.sinfulrush.Constants.BONUS_TAKEN_SFX, randomPitch(), 0);
    }

    public void enemyDeath() {
        if(com.pizzaroof.sinfulrush.util.Utils.randBool())
            enemyDeath1();
        else
            enemyDeath2();
    }

    public void enemyDeath1() {
        enemyDeath = playSound(enemyDeath, com.pizzaroof.sinfulrush.Constants.ENEMY_DEATH_SFX, mediumPitch(), 0);
    }

    public void enemyDeath2() {
        enemyDeath2 = playSound(enemyDeath2, com.pizzaroof.sinfulrush.Constants.ENEMY_DEATH2_SFX, mediumPitch(), 0);
    }

    public void playerHurt() {
        playerHurt.play(smallPitch(), 0, sfxVolume);
    }

    public void enemyExplosion() {
        enemyExplosion.play(randomPitch(), 0, sfxVolume);
    }

    public void friendDeath() {
        friendDeath = playSound(friendDeath, com.pizzaroof.sinfulrush.Constants.FRIEND_DEATH_SFX, smallPitch(), 0);
    }

    /*public void jump() {
        jump = playSound(jump, Constants.JUMP_SFX, 1, 0);
    }*/

    public void sceptreExplosion() {
        sceptreExplosion = playSound(sceptreExplosion, com.pizzaroof.sinfulrush.Constants.SCEPTRE_EXPLOSION_SFX, smallPitch(), 0);
    }

    public void portal() {
        portal.play(smallPitch(), 0, sfxVolume);
    }

    public void rage() {
        rage = playSound(rage, com.pizzaroof.sinfulrush.Constants.RAGE_SFX, 1, 0);
    }

    public void healthPotion() {
        healthPotion.play(1, 0, sfxVolume);
    }

    public void swordSwing() {
        swordSwing = playSound(swordSwing, com.pizzaroof.sinfulrush.Constants.SWORD_SWING_SFX, smallPitch(), 0);
    }

    public void swordDamage() {
        swordDamage = playSound(swordDamage, com.pizzaroof.sinfulrush.Constants.SWORD_DAMAGE_SFX, randomPitch(), 0);
    }

    public void slowtime() {
        slowtime = playSound(slowtime, com.pizzaroof.sinfulrush.Constants.SLOWTIME_SFX, reallySmallPitch(), 0);
    }

    public void thunder() {
        thunder = playSound(thunder, com.pizzaroof.sinfulrush.Constants.THUNDER_SFX, smallPitch(), 0);
    }

    public void wind() {
        wind = playSound(wind, com.pizzaroof.sinfulrush.Constants.WIND_SFX, smallPitch(), 0);
    }

    public void playerDeath() {
        playerDeath = playSound(playerDeath, com.pizzaroof.sinfulrush.Constants.PLAYER_DEATH_SFX, reallySmallPitch(), 0);
    }

    public void clockTick(float pitch) {
        clockTick = playSound(clockTick, com.pizzaroof.sinfulrush.Constants.CLOCK_TICK_SFX, pitch, 0);
    }

    public void bossExplosion() {
        if(!useSfx() || !assetManager.isLoaded(com.pizzaroof.sinfulrush.Constants.BIG_EXPLOSION_SFX) || !assetManager.isLoaded(com.pizzaroof.sinfulrush.Constants.BIG_EXPLOSION2_SFX)) return;

        if(bossExplosion == null) {
            //dobbiamo ancora crearlo
            bossExplosion = new SequenceSound();
            Sound exp = assetManager.get(com.pizzaroof.sinfulrush.Constants.BIG_EXPLOSION_SFX, Sound.class);
            Sound exp2 = assetManager.get(com.pizzaroof.sinfulrush.Constants.BIG_EXPLOSION2_SFX, Sound.class);
            bossExplosion.addSound(exp, 0, 1);
            bossExplosion.addSound(exp, 0.3f, 1);
            bossExplosion.addSound(exp, 0.3f, 1);
            bossExplosion.addSound(exp, 0.3f, 1);
            bossExplosion.addSound(exp2, 0.3f, 1);
            bossExplosion.addSound(exp, 0.32f, 1);
            bossExplosion.addSound(exp2, 0.32f, 1);
        }
        for(int i=0; i<bossExplosion.getSize(); i++)
            bossExplosion.setPitch(i, smallPitch());

        bossExplosion.play(sfxVolume);
    }

    public void bossExplosion2() {
        if(!useSfx() || !assetManager.isLoaded(com.pizzaroof.sinfulrush.Constants.BIG_EXPLOSION_SFX) || !assetManager.isLoaded(com.pizzaroof.sinfulrush.Constants.BIG_EXPLOSION2_SFX)) return;

        if(bossExplosion2 == null) {
            //dobbiamo ancora crearlo
            bossExplosion2 = new SequenceSound();
            Sound exp = assetManager.get(com.pizzaroof.sinfulrush.Constants.BIG_EXPLOSION_SFX, Sound.class);
            Sound exp2 = assetManager.get(com.pizzaroof.sinfulrush.Constants.BIG_EXPLOSION2_SFX, Sound.class);
            Sound exp3 = assetManager.get(com.pizzaroof.sinfulrush.Constants.DEATH_HEAD_CUT_SFX, Sound.class);
            bossExplosion2.addSound(exp, 0.05f, 1);
            bossExplosion2.addSound(exp, 0.45f, 1);
            bossExplosion2.addSound(exp3, 0.9f, 1);//1.f -> taglio testa
            bossExplosion2.addSound(exp2, 0.3f, 1);
        }
        for(int i=0; i<bossExplosion2.getSize(); i++)
            bossExplosion2.setPitch(i, smallPitch());

        bossExplosion2.play(sfxVolume);
    }

    public void bossRoar() {
        bossRoar = playSound(bossRoar, com.pizzaroof.sinfulrush.Constants.BOSS_ROAR_SFX, 1, 0);
    }

    public void bossDeath() {
        bossDeath = playSound(bossDeath, com.pizzaroof.sinfulrush.Constants.BOSS_DEATH_SFX, 1, 0);
    }

    public void bossHurt() {
        bossHurt = playSound(bossHurt, Constants.BOSS_HURT_SFX, 1, 0);
    }

    private Sound playSound(Sound sound, String name, float pitch, float pan) {
        if(!useSfx()) return sound;
        if(sound == null)
            if(assetManager.isLoaded(name))
                sound = assetManager.get(name, Sound.class);
            else
                return null;
        sound.play(sfxVolume, pitch, pan);
        return sound;
    }

    /**restituisce un float casuale da usare come pitch (deve essere tra 0.5 e 2)*/
    public float randomPitch() {
        return com.pizzaroof.sinfulrush.util.Utils.randFloat(0.5f, 2.f);
    }

    public float mediumPitch() {
        return com.pizzaroof.sinfulrush.util.Utils.randFloat(0.7f, 1.5f);
    }

    /**pitch random ma piccolo*/
    public float smallPitch() {
        return com.pizzaroof.sinfulrush.util.Utils.randFloat(0.8f, 1.2f);
    }

    public float reallySmallPitch() {
        return Utils.randFloat(0.95f, 1.05f);
    }

    public Music getCurrentSoundtrack() {
        return currentSoundtrack;
    }

    /**setta la callback per quando finisce il cambio da una soundtrack all'altra.
     * NB: va reimpostata per ogni cambio!!! alla fine del cambio la callback viene messa a null*/
    public void setOnSoundtrackChanged(Runnable runnable) {
        this.onSoundtrackChanged = runnable;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
