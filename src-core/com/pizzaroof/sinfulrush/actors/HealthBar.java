package com.pizzaroof.sinfulrush.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.pizzaroof.sinfulrush.actors.stage.DoubleActActor;
import com.pizzaroof.sinfulrush.actors.stage.ShaderStage;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.util.Pair;

/**classe per barra degli hp.
 * NB: usare setWidth e setHeight per le dimensioni con cui stampare*/
public class HealthBar extends DoubleActActor {

    /**tempo in secondi per apparire/scomparire*/
    public static final float TIME_TO_APPEAR = 0.3f;

    /**percentuale di hp presenti nella barra (da 0 a 1)*/
    private float hp;

    /**destinazione hp*/
    private float destHp;

    /**fattore di interpolazione*/
    protected float lerpWeight = 0.1f * 50.f;

    /**texture per i vari segmenti della barra: parte interna (sia sul bordo che centrale), parte esterna (sia sul bordo che centrale)*/
    private TextureRegion inCenter, inBorder, outCenter, outBorder;
    /**lartghezza del bordo*/
    private float borderWidth;

    /**gradiente di colori ordinati decrescentemente*/
    private com.pizzaroof.sinfulrush.util.Pair<Float, Color> gradient[];

    /**informazioni su dimensioni minime/massime*/
    private int minWidth, maxWidth, minHeight, maxHeight;

    /**coordinate centro*/
    private float centerX, centerY;

    /**quanto si sposta la y al massimo quando ci si muove*/
    private float yOffset;

    /**colore reale della barra getColor potrebbe dare colori diversi, per questioni legate agli shader*/
    protected Color realColor;

    /**se vero, appena arriva a 0 smette con linear interpolation*/
    private boolean fast0;

    //FrameBuffer buffer;

    /**i primi parametri sono le regioni per la grafica... NB: inCenter e outCenter devono avere stessa dimensione e devono combaciare (anche inBorder e outBorder)*/
    public HealthBar(TextureRegion inCreg, TextureRegion inBreg, TextureRegion outCreg, TextureRegion outBreg) {
        this.inCenter = inCreg;
        this.inBorder = inBreg;
        this.outCenter = outCreg;
        this.outBorder = outBreg;
        fast0 = false;

        //buffer = new FrameBuffer(Pixmap.Format.RGBA8888, Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, true);
        setHp(1.f);
        setColor(1,1, 1, 0);
        realColor = new Color(1, 1, 1, 0);
        setName("HealthBar");
    }

    /**setta gli hp in maniera smooth: cioè non vengono modificati subito gli hp ma convergono gradualmente al target*/
    public void setSmoothHp(float hp) {
        this.destHp = Math.max(0.f, Math.min(1.f, hp));
    }

    /**setta la percentuale di hp*/
    public void setHp(float hp) {
        this.hp = hp;
        destHp = hp;
    }

    /**setta posizione dal centro*/
    public void setCenterPosition(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        setPosition(centerX - getWidth()/2.f, centerY - getHeight()/2.f);
    }

    @Override
    public void setHeight(float h) {
        super.setHeight(h);
        //quando settiamo l'altezza, setto anche la larghezza del bordo (che deve essere mantenuta in proporzione, e poi si aggiungerà la parte centrale per il resto)
        recomputeBorderWidth();
    }

    private void recomputeBorderWidth() {
        borderWidth = getHeight() * (float)inBorder.getRegionWidth() / (float)inBorder.getRegionHeight();
    }

    @Override
    public void actSkipTolerant(float delta) {
        setColor(realColor);

        super.actSkipTolerant(delta);
        /*if(buffer == null) {
            buffer = new FrameBuffer(Pixmap.Format.RGBA8888, (int)getStage().getCamera().viewportWidth, (int)getStage().getCamera().viewportHeight, false);
            System.out.println((int)getStage().getCamera().viewportWidth+" "+(int)getStage().getCamera().viewportHeight);
        }*/

        recomputeBorderWidth();
        setX(centerX - getWidth()/2.f);

        //modifica hp con linear interpolation
        float mul = Math.min(1.f, lerpWeight * delta);
        hp = hp + (destHp - hp) * mul;

        if(gradient != null) { //troviamo con che colore stampare la barra
            float alpha = getColor().a;

            int actual = 0;
            while (actual < gradient.length - 1 && hp <= gradient[actual + 1].v1) //trova colore attuale: assumendo che il gradient è ordinato (NB: potremmo usare ricerca binaria...)
                actual++;

            if (actual == gradient.length - 1) { //ultimo elemento del gradient: mettiamo solo lui
                setColor(gradient[actual].v2);
            } else { //facciamo interpolazione tra colore attuale e prossimo colore
                float cInter = (gradient[actual].v1 - hp) / (gradient[actual].v1 - gradient[actual + 1].v1); //fattore di interpolazione per il colore
                setColor(new Color(gradient[actual].v2));
                getColor().lerp(gradient[actual + 1].v2, cInter);
            }

            getColor().a = alpha;
        }

        realColor = getColor();
        if(getStage() instanceof ShaderStage && ((ShaderStage) getStage()).isRageModeOn()) {
            setColor(1, 0, 0, realColor.a);
        }
    }

    public float getHp() {
        return hp;
    }

    public void setFast0(boolean f0) {
        fast0 = f0;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        super.draw(batch, alpha);

        if(getColor() != null) {
            Color tmp = batch.getColor();

            batch.setColor(getColor()); //cambiamo colore batch

            //buffer.begin();

            //stampa prima parte esterna
            batch.draw(outCenter, getX() + borderWidth - 1, getY(), getWidth() - borderWidth * 2f + 1, getHeight()); //aggiungiamo un po' di overlapping tra la parte centrale e i bordi
            batch.draw(outBorder, getX(), getY(), borderWidth, getHeight());
            batch.draw(outBorder, getX() + getWidth(), getY(), -borderWidth, getHeight()); //flippata

            //poi stampiamo parte interna
            if(getHp() > com.pizzaroof.sinfulrush.Constants.EPS && (!fast0 || destHp > Constants.EPS)) {
                float healthW = hp * (getWidth() - 2 * borderWidth);
                batch.draw(inCenter, getX() + borderWidth - 1, getY(), Math.max(0, healthW) + 1, getHeight());
                batch.draw(inBorder, getX(), getY(), borderWidth, getHeight());
                if(!fast0 || getHp() >= 0.5f)
                    batch.draw(inBorder, getX() + healthW + 2 * borderWidth, getY(), -borderWidth, getHeight());
            }

            /*batch.end();
            buffer.end();
            batch.begin();
            batch.draw(buffer.getColorBufferTexture(), 0, 0);*/

            batch.setColor(tmp); //ripristiniamo colore batch
        }
    }

    /**setta il gradiente di colori. NB: i colori devono essere ordinati decrescentemente rispetto al primo valore.
     * ES: (1.0, GREEN), (0.5, YELLOW), (0.25, RED) in questo modo la barra inizia verde, diventa gialla a metà, diventa rossa a un quarto, e finisce rossa fino a 0
     * NB: 1.0 e 0.0 devono sempre esserci*/
    public void setGradient(Pair<Float, Color>... colors) {
        this.gradient = colors;
    }

    /**fa apparire la barra degli hp (cioè dalla dimensione minima, va alla massima)*/
    public void appear() {
       this.addAction(Actions.parallel(Actions.sizeTo(maxWidth, maxHeight, TIME_TO_APPEAR), //andiamo a massima dimensione
               Actions.moveBy(0, centerY + yOffset - getY(), TIME_TO_APPEAR), //ci spostiamo anche un po' verso l'alto
               Actions.fadeIn(TIME_TO_APPEAR))); //e appariamo

    }

    /**fa scomparire la barra degli hp*/
    public void disappear() {
        addAction(Actions.parallel(Actions.sizeTo(minWidth, minHeight, TIME_TO_APPEAR), //contrario di appear...
                Actions.moveBy(0, centerY - getY(), TIME_TO_APPEAR), Actions.fadeOut(TIME_TO_APPEAR)));
    }

    /**setta min/max width/height*/
    public void setRangeDimensions(int minW, int minH, int maxW, int maxH) {
        this.minWidth = minW;
        this.maxWidth = maxW;
        this.minHeight = minH;
        this.maxHeight = maxH;
        setWidth(minWidth);
        setHeight(minHeight);
    }

    public void setYoffset(float yoff) {
        this.yOffset = yoff;
    }

    public void setRealColor(Color color) {
        realColor = color;
    }

    public void setLerpWeight(float w) {
        lerpWeight = w;
    }

    @Override
    public boolean remove() {
        boolean r = super.remove();
        setName("HealthBar - removed");
        return r;
    }
}
