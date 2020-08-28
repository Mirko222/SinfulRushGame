package com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.attacks.ButtonPowerball;
import com.pizzaroof.sinfulrush.attacks.FollowingPowerball;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.actors.physics.PhysicSpriteActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.CameraController;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.JuicyPlayer;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.io.BufferedReader;
import java.io.IOException;

/**nemico "neutro", non bisogna ucciderlo o si subisce un malus; appena il personaggio raggiunge la sua piattaforma,
 * viene liberato.
 * il file info è organizzato come per un platform enemy (invece di attack c'è free)
 * sull'ultima riga:
 * malus_weight malus_type*/
public class FriendEnemy extends PlatformEnemy {

    private static final float BALL_SPEED = 6, BALL_RADIUS = 0.1f;

    /**modifichiamo leggermente la durata della free animation, per farle un po' più random*/
    private static final float FREE_ANIM_DELTA = 0.1f;

    protected int FREE_ANIM; //animazione di quando viene liberato

    protected com.pizzaroof.sinfulrush.actors.physics.game_actors.Player player;

    /**indica quanto è grave il malus*/
    protected int malusWeight;

    protected MalusType malusType;

    /**bottone dello score, necessario solo con nemici che attaccano lo score*/
    protected Button scoreButton;

    protected Group hudGroup;

    public enum MalusType {
        SCORE_DAMAGE, //malus che danneggia lo score
        PLAYER_DAMAGE //malus che danneggia il player
    }

    protected FriendEnemy(World2D world, SoundManager soundManager, Stage stage, Player player, float density, float friction, float restitution, String directory, AssetManager asset, Vector2 initPosition, Group backgroundGroup, Group effectGroup, Shape... shapes) {
        super(world, soundManager, stage, density, friction, restitution, directory, asset, initPosition, backgroundGroup, effectGroup, shapes);
        this.player = player;
        FREE_ANIM = ATTACK_ANIM; //l'animazione di free la mettiamo nel posto di quella d'attacco
        setSpriterAnimationDuration(FREE_ANIM, getSpriterAnimationDuration(FREE_ANIM) + com.pizzaroof.sinfulrush.util.Utils.randFloat(-FREE_ANIM_DELTA, FREE_ANIM_DELTA));

        malusWeight = (int)Math.floor(malusWeight * player.getPlayerPower().getMalusMultiplier());
    }

    @Override
    protected void updateWhenPlayerIsOnPlatform(float delta) {
        super.updateWhenPlayerIsOnPlatform(delta);
        getBody().setLinearVelocity(0, getBody().getLinearVelocity().y);
        //viene liberato se è ancora vivo
        if (getHp() > 0 && getCurrentSpriterAnimation() != FREE_ANIM) {
            myPlatform.removeEnemy(this);
            backgroundGroup.addActor(this);
            setSpriterAnimation(FREE_ANIM);
            player.increaseFriendsSaved();
            if(missionDataCollector != null)
                missionDataCollector.updateFriendsSavings();
            soundManager.portal();
        }
    }

    @Override
    protected void resetAnimationOnPlayerDeath() {
        if(getCurrentSpriterAnimation() != FREE_ANIM)
            super.resetAnimationOnPlayerDeath();
    }


    @Override
    public void onSpriterAnimationEnded(int id) {
        super.onSpriterAnimationEnded(id);
        if(id == FREE_ANIM) {
            player.onSavedFriend();
            remove();
        }
    }

    @Override
    public void takeDamage(int dmg, PhysicSpriteActor attacker, Mission.BonusType damageType) {
        if(getCurrentSpriterAnimation() == FREE_ANIM) return; //se stiamo in free, ormai non possiamo morire
        super.takeDamage(dmg, attacker, damageType);
    }

    @Override
    public void takeDamage(int dmg, Vector2 hitPoint, Color color, Mission.BonusType damageType) {
        if(getCurrentSpriterAnimation() == FREE_ANIM) return;
        takeDamage(dmg, damageType);
        printDamage(dmg, hitPoint, color);
    }

    @Override
    public void dying(Mission.BonusType deathType) {
        super.dying(deathType);
        applyMalus();
    }

    @Override
    protected void printDamage(int dmg, Vector2 hitPoint, Color color) {
        super.printDamage(dmg, hitPoint, color);
        damageFlashText.setText("MALUS!");
    }

    @Override
    protected void playEnemyDeath() {
        soundManager.friendDeath();
    }

    protected void applyMalus() {

        //possono esserci di diversi tipi... di default facciamo perdere un po' di punteggio o di vita
        if(malusType.equals(MalusType.SCORE_DAMAGE)) {

            ButtonPowerball powerball = new ButtonPowerball(world, com.pizzaroof.sinfulrush.Constants.FRIEND_BALL_EFFECT, getBody().getPosition(), BALL_SPEED, malusWeight, BALL_RADIUS, Vector2.Zero, com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor.MALUS_VIOLET) {
                @Override
                public void onCollisionWithButton() {
                    super.onCollisionWithButton();
                    player.increasePlatformMalus(malusWeight);
                }
            };
            powerball.setExplosionEffect(com.pizzaroof.sinfulrush.Constants.FIRE_EXPLOSION_EFFECT);
            powerball.setExplosionEffectColor(com.pizzaroof.sinfulrush.util.pools.Pools.PEffectColor.VIOLET);
            powerball.setRemoveEffectBeforeExplosion(true);
            powerball.setTargetBtn(scoreButton);
            powerball.setSoundManager(soundManager);
            hudGroup.addActorAfter(scoreButton, powerball);

        } else {
            Vector2 backup = player.getBody().getPosition().cpy().sub(getBody().getPosition());
            FollowingPowerball powerball = new FollowingPowerball(world, com.pizzaroof.sinfulrush.Constants.FRIEND_BALL_EFFECT, getBody().getPosition(), BALL_SPEED, malusWeight, BALL_RADIUS, player,
                    backup, Pools.PEffectColor.MALUS_VIOLET);
            powerball.setExplosionEffect(Constants.FIRE_EXPLOSION_EFFECT);
            powerball.setExplosionEffectColor(Pools.PEffectColor.VIOLET);
            powerball.setSoundManager(soundManager);
            powerball.setRemoveEffectBeforeExplosion(true);
            effectGroup.addActor(powerball);
        }

        cameraController.incrementTrauma(CameraController.MAX_TRAUMA);
        if(canVibrate)
            Gdx.input.vibrate(JuicyPlayer.DEF_VIBRATE_MILLIS);
    }

    @Override
    protected void initFromDirectory(String dir, AssetManager asset, Stage stage) {
        super.initFromDirectory(dir, asset, stage);
        try {
            BufferedReader reader = com.pizzaroof.sinfulrush.util.Utils.getInternalReader(com.pizzaroof.sinfulrush.util.Utils.enemyInfoPath(dir));
            for(int i=0; i<4; i++) reader.readLine();
            String strs[] = reader.readLine().split(" ");
            malusWeight = Integer.parseInt(strs[0]);
            malusType = MalusType.valueOf(strs[1]);
            reader.close();
        }catch(IOException e) { //non dovrebbe accadere
            e.printStackTrace();
        }
    }

    @Override
    public void recomputeColor() {
    }

    public void setScoreButton(Button b) {
        scoreButton = b;
    }

    public void setHudGroup(Group group) {
        hudGroup = group;
    }

    @Override
    protected void updatePlayerStatsOnDeath(Mission.BonusType deathType) {
        player.increaseFriendsKilled();
        if(missionDataCollector != null)  //se è null, non registriamo missioni
            missionDataCollector.updateEnemiesKills(this, deathType, player, (ShaderStage)getStage());
    }

    public MalusType getMalusType() {
        return malusType;
    }

    /**factory method per creare un nemico dalla directory (e altre cose ovvie)*/
    public static FriendEnemy createEnemy(String directory, SoundManager soundManager, Player player, AssetManager assetManager, World2D world, Stage stage, Vector2 initPosition, Group behindGroup, Group effectGroup) {
        try {
            Vector2 dim = com.pizzaroof.sinfulrush.util.Utils.enemyDrawingDimensions(directory);
            Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(Utils.enemyShapePath(directory), dim.x, dim.y, world.getPixelPerMeter());
            return new FriendEnemy(world, soundManager, stage, player,0, 0, 0, directory, assetManager, initPosition, behindGroup, effectGroup, shapes);
        }catch(IOException e) { //non dovrebbe succedere
            e.printStackTrace();
        }
        return null;
    }
}
