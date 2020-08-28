package com.pizzaroof.sinfulrush.actors.stage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Disposable;

/**gruppo dove possiamo mettere effetti particellari additive e verranno stampati come se fossero su background nero
 * (circa)*/
public class AdditiveGroup extends Group implements Disposable {

    private FrameBuffer frameBuffer;

    @Override
    public void act(float delta) {
        super.act(delta);

        if(frameBuffer == null || frameBuffer.getWidth() != (int)getStage().getCamera().viewportWidth || frameBuffer.getHeight() != (int)getStage().getCamera().viewportHeight) {
            if(frameBuffer != null)
                frameBuffer.dispose();
            frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, (int)getStage().getCamera().viewportWidth, (int)getStage().getCamera().viewportHeight, false, false);
        }
    }

    @Override
    public void draw(Batch batch, float alpha) {
        if(frameBuffer == null)
            super.draw(batch, alpha);
        else {
            batch.end();

            getStage().getViewport().apply(); //usa viewport di stage
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            frameBuffer.begin();
            batch.begin();
            Gdx.gl.glClearColor(0, 0, 0, 0); //pulisci schermo
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            Gdx.gl20.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);
            super.draw(batch, alpha); //ristampa stage
            batch.end();
            frameBuffer.end();

            //TextureRegion tmp = new TextureRegion(frameBuffer.getColorBufferTexture());
            //tmp.flip(false, true);

            batch.setProjectionMatrix(getStage().getCamera().combined);
            getStage().getViewport().apply();
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.begin();

            float x = getStage().getCamera().position.x - frameBuffer.getWidth() * 0.5f;
            float y = getStage().getCamera().position.y - frameBuffer.getHeight() * 0.5f;
            batch.draw(frameBuffer.getColorBufferTexture(), x, y + frameBuffer.getHeight(), frameBuffer.getWidth(), -frameBuffer.getHeight());
        }
    }

    @Override
    public void dispose() {
        if(frameBuffer != null)
            frameBuffer.dispose();
    }
}
