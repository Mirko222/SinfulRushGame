package com.pizzaroof.sinfulrush.actors;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Pool;
import com.pizzaroof.sinfulrush.Constants;

/**flash text fatto apposta per i danni*/
public class DamageFlashText extends FlashText {

    private static short GLOBAL_ID = 0;

    /**id sequenziale assegnato al testo*/
    private short id;

    /**danno cumulativo da stampare*/
    private int damage;
    /**primo danno/flash?*/
    private boolean firstFlash;

    /**eventuale prefisso prima del danno*/
    private String prefix;

    public DamageFlashText(Skin skin, Pool<FlashText> pool) {
        super(skin, pool);
        prefix = null;
    }

    public void increaseDamage(int dmg) {
        if(dmg < com.pizzaroof.sinfulrush.Constants.INFTY_HP && dmg + damage < com.pizzaroof.sinfulrush.Constants.INFTY_HP) {
            damage += dmg;
            setText(Integer.toString(damage));
        } else {
            damage = Constants.INFTY_HP;
            maxScale = 2.5f;
            setText("∞");
            //setText(Integer.toString(Constants.INFTY_HP));
        }
        if(prefix != null)
            setText(getText().insert(0, prefix));

        if(firstFlash)
            startFlash();
    }

    @Override
    public void startFlash() {
        super.startFlash();
        firstFlash = false;
    }

    public short getId() {
        return id;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void reset() {
        super.reset();
        prefix = null;
        damage = 0;
        firstFlash = true;
        setText("0");
        if(GLOBAL_ID > Short.MAX_VALUE / 2)
            GLOBAL_ID = 0;
        id = GLOBAL_ID++;
    }
}
