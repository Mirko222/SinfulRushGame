package com.pizzaroof.sinfulrush.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.brashmonkey.spriter.gdx.SpriterDataLoader;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.actors.physics.ParticleActor;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.pools.Pools;

//schermo per fare test
public class TestingScreen extends AbstractScreen {

    AssetManager assetManager;

    FrameBuffer buffer;

    ParticleActor particleActor;

    World2D world2D;

    Texture background;

    public TestingScreen(NGame game) {
        super(game);

        assetManager = new AssetManager();
        SpriterDataLoader sloader = new SpriterDataLoader(new InternalFileHandleResolver());
        assetManager.setLoader(SpriterData.class, sloader);

        SpriterDataLoader.SpriterDataParameter parameter = new SpriterDataLoader.SpriterDataParameter();
        assetManager.load("players/thief/animations.scml", SpriterData.class, parameter);
        //assetManager.load("players/thief/animations.scml", SpriterData.class, parameter);
        assetManager.load("bonus/bonusIcon1/animations.scml", SpriterData.class, new SpriterDataLoader.SpriterDataParameter());
        assetManager.load(Constants.HEALTH_BAR_ATLAS, TextureAtlas.class);

        assetManager.load(Constants.FIREBALL_EFFECT, ParticleEffect.class);

        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(new InternalFileHandleResolver()));
        //assetManager.load(Constants.DEFAULT_FONT_PATH, FreeTypeFontGenerator.class); //questo ci serve perché altrimenti non possiamo caricare le skin per l'ui
        assetManager.finishLoading(); //dobbiamo finire di caricare, o non riusciamo a caricare la skin
        Pools.addEffectPool(Constants.FIREBALL_EFFECT, assetManager.get(Constants.FIREBALL_EFFECT));

        world2D = new World2D(Constants.PIXELS_PER_METER, Constants.GRAVITY_VECTOR);
        stage.addActor(world2D);

        particleActor = new ParticleActor(world2D, BodyDef.BodyType.KinematicBody, 0, 0, 0, new Vector2(1.f, 1.f), false,
                Utils.getCircleShape(0.3f));
        particleActor.addEffect(Constants.FIREBALL_EFFECT);
        stage.addActor(particleActor);

        background = new Texture(Constants.HELL_GRADIENT_BG);
    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        if(buffer != null) buffer.dispose();
        buffer = new FrameBuffer(Pixmap.Format.RGBA8888, (int)stage.getCamera().viewportWidth, (int)stage.getCamera().viewportHeight, false, false);
    }


    @Override
    public void redraw() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        stage.getViewport().apply(); //usa viewport di stage
        buffer.begin();
        stage.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glClearColor(0, 0, 0, 0); //pulisci schermo
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        //Gdx.gl20.glBlendFuncSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE);
        stage.draw(); //ristampa stage
        buffer.end();

        TextureRegion tmp = new TextureRegion(buffer.getColorBufferTexture());
        tmp.flip(false, true);

        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
        stage.getViewport().apply();
        stage.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        stage.getBatch().begin();
        stage.getBatch().draw(background, 0, 0);
        //particleActor.draw(stage.getBatch(), 1);
        stage.getBatch().draw(tmp, 0, 0);
        stage.getBatch().end();


        stage.getBatch().setProjectionMatrix(stage.getCamera().combined);
        stage.getViewport().apply();
        stage.getBatch().setBlendFunction(GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA);
        stage.getBatch().begin();
        //stage.getBatch().draw(tmp, 0,0);
        //particleActor.draw(stage.getBatch(), 1);
        stage.getBatch().end();
    }

    @Override
    public void updateLogic(float delta) {
        super.updateLogic(delta);

        float vy = particleActor.getBody().getLinearVelocity().y;
        if(Gdx.input.isKeyPressed(Input.Keys.D)) particleActor.getBody().setLinearVelocity(4, vy);
        else if(Gdx.input.isKeyPressed(Input.Keys.A)) particleActor.getBody().setLinearVelocity(-4, vy);
        else particleActor.getBody().setLinearVelocity(0f, vy);

        float vx = particleActor.getBody().getLinearVelocity().x;
        if(Gdx.input.isKeyPressed(Input.Keys.W)) particleActor.getBody().setLinearVelocity(vx, 4);
        else if(Gdx.input.isKeyPressed(Input.Keys.S)) particleActor.getBody().setLinearVelocity(vx, -4);
        else particleActor.getBody().setLinearVelocity(vx, 0f);
    }


}
