package com.pizzaroof.sinfulrush.input;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.screens.GameplayScreen;
import com.pizzaroof.sinfulrush.screens.HudGameplayScreen;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.bonus.Bonus;

/**gesture detector usato nel gameplay*/
public class GameplayGesture implements InputProcessor {

    private com.pizzaroof.sinfulrush.NGame game;
    private GameplayScreen gameplayScreen;

    /**stage*/
    private Stage stage;
    /**gruppo dei bonus*/
    private Group bonusGroup;

    private Vector3 tapCoord;

    public GameplayGesture(NGame game, GameplayScreen screen, Stage stage, Group bonusGroup) {
        super();
        this.game = game;
        this.gameplayScreen = screen;
        this.stage = stage;
        this.bonusGroup = bonusGroup;

        tapCoord = new Vector3();
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.BACK //tasto back su android
                || keycode == Input.Keys.ESCAPE) { //esc su desktop
            if(((HudGameplayScreen)gameplayScreen).resumeButton.hasActions() ||
                ((HudGameplayScreen)gameplayScreen).pauseExitButton.hasActions())
                return false;

            if(gameplayScreen.isInPause()) {
                ((HudGameplayScreen)gameplayScreen).showExitDialog();
            } else if (gameplayScreen.player.getHp() > 0 && gameplayScreen.canPause) {
                gameplayScreen.setInPause(true);
                ((HudGameplayScreen) gameplayScreen).showPauseMenu();
                ((HudGameplayScreen) gameplayScreen).initPauseMenuPosition();
                game.getSoundManager().pauseSoundtrack();
            }
        }

        //-------debug da desktop--------
        /*if(keycode == Input.Keys.F) {
            for(Actor e : gameplayScreen.enemiesGroup.getChildren())
                if(((Enemy)e).getHp() > 0)
                    ((Enemy)e).freeze();
        }
        if(keycode == Input.Keys.S)
            ((TimescaleStage)stage).setTemporalyTimeMultiplier(0.45f, 3);

        if(keycode == Input.Keys.W)
            for(Actor a : gameplayScreen.enemiesGroup.getChildren())
                if(a instanceof FlyingSniperEnemy)
                    ((FlyingSniperEnemy) a).flyAway(0);
        if(keycode == Input.Keys.L)
            gameplayScreen.player.changeSpeed(gameplayScreen.player.getSpeed() + 0.5f);
        if(keycode == Input.Keys.O)
            ((HudGameplayScreen)gameplayScreen).showErrorVideoDialog();
        */

        /*if(keycode == Input.Keys.R) {
            gameplayScreen.player.setHp(0);
            ((AdGameplayScreen)gameplayScreen).keepPlayingLevel();
        }

        if(keycode == Input.Keys.B) //crea bug durante il salto del personaggio
            gameplayScreen.player.setState(Player.PlayerState.RUNNING);
        if(keycode == Input.Keys.T)
            gameplayScreen.camController.setIncresingTrauma(1);
        if(keycode == Input.Keys.Y)
            gameplayScreen.camController.setIncresingTrauma(0.8f);
        if(keycode == Input.Keys.U)
            gameplayScreen.camController.setIncresingTrauma(0.65f);
        if(keycode == Input.Keys.I)
            gameplayScreen.camController.setIncresingTrauma(0.5f);
        if(keycode == Input.Keys.K) {
            for(Actor a : gameplayScreen.enemiesGroup.getChildren())
                if(a instanceof PlatformEnemy)
                    ((PlatformEnemy) a).takeDamage(Constants.INFTY_HP);
        }*/
        /*
        if(keycode == Input.Keys.H)
            gameplayScreen.player.heal(100);
        if(keycode == Input.Keys.K) {
            for(Actor a : gameplayScreen.enemiesGroup.getChildren())
                if(a instanceof PlatformEnemy)
                    ((PlatformEnemy) a).takeDamage(Constants.INFTY_HP);
        }

        if(keycode == Input.Keys.D)
            gameplayScreen.player.takeDamage(100, null);
        */

        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        if(gameplayScreen.isInPause() || gameplayScreen.player.getHp() <= 0) return false;

        //gameplayScreen.camController.setIncresingTrauma(1.f);

        tapCoord.set(x, y, 0);
        tapCoord = stage.getCamera().unproject(tapCoord);
        Actor hitted = bonusGroup.hit(tapCoord.x, tapCoord.y, false); //vediamo se ha colpito qualche bonus
        if(hitted instanceof Bonus) //se ha colpito un bonus, lo prendiamo
            ((Bonus)hitted).take();
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
