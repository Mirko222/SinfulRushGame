package com.pizzaroof.sinfulrush.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.StringBuilder;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.actors.stage.TimescaleStage;
import com.pizzaroof.sinfulrush.util.Utils;

/**una label da visualizzare velocemente nel gioco. Uso tipico: danni inflitti a un nemico*/
public class FlashText extends Container<Label> implements Pool.Poolable, ResettableY {

    protected Label label;

    /**durata in secondi del flash*/
    protected float duration;

    protected float maxScale, minScale;

    protected Pool<FlashText> myPool;

    protected Runnable onRemoveCallback;

    protected boolean decreasing;

    private boolean rageOn;
    protected Color rageColor;
    protected Color standardColor;

    public FlashText(Skin skin, Pool<FlashText> pool) {
        super();
        label = new Label(null, skin);
        setActor(label);
        setTransform(true);
        reset();
        this.myPool = pool;
        setName("FlashText");
    }

    @Override
    public void act(float delta) {
        if(((TimescaleStage)getStage()).isSkipTolerantAct()) {
            super.act(delta);
            if(getStage() == null) return;

            boolean rage = ((ShaderStage)getStage()).isRageModeOn();
            if(rage && !rageOn)
                label.setColor(rageColor);
            if(rageOn && !rage)
                label.setColor(standardColor);
            rageOn = rage;
        }
    }

    public void startFlash() {
        float degRot = com.pizzaroof.sinfulrush.util.Utils.randFloat(20, 40) * Utils.randChoice(-1, 1);
        Action action = Actions.parallel(
                                    Actions.sequence(Actions.scaleTo(maxScale, maxScale, duration * 0.5f),
                                            Actions.scaleTo( maxScale * 0.75f + minScale * 0.25f, maxScale * 0.75f + minScale * 0.25f, duration * 0.5f * 0.25f),
                                            Actions.run(new Runnable() {
                                                @Override
                                                public void run() {
                                                    decreasing = true;
                                                }
                                            }),
                                            Actions.scaleTo(minScale, minScale, duration * 0.5f * 0.75f)),
                                    Actions.alpha(0.f, duration, Interpolation.bounceIn),
                                    Actions.rotateBy(degRot, duration));
        addAction(Actions.sequence(action, Actions.removeActor()));
    }

    @Override
    public boolean remove() {
        boolean rem = super.remove();
        if(onRemoveCallback != null)
            onRemoveCallback.run();
        if(myPool != null)
            myPool.free(this);
        return rem;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public void setMinScale(float mscale) {
        this.minScale = mscale;
    }

    public void setMaxScale(float maxScale) {
        this.maxScale = maxScale;
    }

    @Override
    public void setColor(Color color) {
        super.setColor(color);
        label.setColor(color);
        standardColor = color;
    }

    public void setText(CharSequence text) {
        label.setText(text);
    }

    public StringBuilder getText() {
        return label.getText();
    }

    public void setOnRemoveCallback(Runnable runnable) {
        onRemoveCallback = runnable;
    }

    @Override
    public void resetY(float maxy) {
        setY(getY() - maxy);
    }

    @Override
    public void reset() {
        clearActions();
        rageOn = false;
        rageColor = Color.WHITE;
        duration = 1.f;
        minScale = 0.25f;
        maxScale = 1.5f;
        setRotation(0);
        setScale(1);
        setColor(1, 1, 1, 1);
        onRemoveCallback = null;
        decreasing = false;
    }

    public boolean isDecreasing() {
        return decreasing;
    }
}
