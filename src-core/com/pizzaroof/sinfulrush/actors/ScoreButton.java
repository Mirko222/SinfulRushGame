package com.pizzaroof.sinfulrush.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.attacks.ButtonPowerball;
import com.pizzaroof.sinfulrush.attacks.Powerball;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

/**bottone dove possiamo salvare lo score di una partita... in sostanza è come un text button, ma gli aggiungiamo una scritta dei danni,
 * per esempio per quando si prende un malus che toglie punti*/
public class ScoreButton extends TextButton {

    private DamageFlashText damageFlashText = null;

    public ScoreButton(String text, Skin skin) {
        super(text, skin);
    }

    public ScoreButton(String text, Skin skin, String styleName) {
        super(text, skin, styleName);
    }

    public ScoreButton(String text, TextButtonStyle style) {
        super(text, style);
    }

    @Override
    public void act(float delta) {
        if(((TimescaleStage)getStage()).isSkipTolerantAct()) {
            super.act(delta);
            if(damageFlashText != null)
                damageFlashText.setPosition(getX() + getWidth() * 0.5f,getY() + getHeight() * 0.5f);
        }
    }

    public void onCollisionWith(ButtonPowerball powerball) {
        Vector2 hitPoint = new Vector2(getX() + getWidth() *0.5f, getY() + getHeight()*0.5f);
        if(powerball.isEvil())
            printDamage(powerball.getPower(), hitPoint, Color.RED, "-", powerball);
        else
            printDamage(powerball.getPower(), hitPoint, Color.GREEN, "+", powerball);
    }

    protected void printDamage(int dmg, Vector2 hitPoint, Color color, String prefix, Powerball ball) {
        if(damageFlashText == null || damageFlashText.isDecreasing() || !com.pizzaroof.sinfulrush.util.Utils.sameColorRGB(damageFlashText.getColor(), color)) {
            damageFlashText = null;

            damageFlashText = Pools.obtainFlashText(getSkin());
            damageFlashText.setPosition(hitPoint.x, hitPoint.y);
            damageFlashText.setDuration(Utils.randFloat(0.6f, 1.f));
            damageFlashText.setColor(color);
            damageFlashText.setPrefix(prefix);
            final short id = damageFlashText.getId();
            damageFlashText.setOnRemoveCallback(new Runnable() {
                @Override
                public void run() {
                    if(damageFlashText != null && damageFlashText.getId() == id)
                        damageFlashText = null;
                }
            });
            getParent().addActorAfter(ball, damageFlashText);
        }
        damageFlashText.increaseDamage(dmg);
    }
}
