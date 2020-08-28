package com.pizzaroof.sinfulrush.actors.stage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.Constants;

/**radice usata da uno shader stage*/
public class ShaderRoot extends Group {

    protected float rageTimePassed;

    /**tempo rallentato?*/
    protected float slowtimeRem;

    /**il background è dark?*/
    protected boolean darkbackground;

    protected ShaderProgram screenShader;

    /**gruppo davanti a TUTTO!! NON è SOGGETTO AGLI SHADERS!!!*/
    protected Group topGroup;

    public ShaderRoot(ShaderProgram screenShader) {
        super();
        this.screenShader = screenShader;
    }

    public void setRageTimePassed(float rtp) {
        rageTimePassed = rtp;
    }

    public void setSlowtime(float slowtime) {
        this.slowtimeRem = slowtime;
    }

    public void setDarkbackground(boolean darkbackground) {
        this.darkbackground = darkbackground;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        //sistemiamo lo shader prima di far partire la draw dalla root
        screenShader.setUniformf(com.pizzaroof.sinfulrush.Constants.RAGE_TIME_NAME, rageTimePassed);
        screenShader.setUniformf(com.pizzaroof.sinfulrush.Constants.SHADER_SCREEN_RESOLUTION, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screenShader.setUniformf(com.pizzaroof.sinfulrush.Constants.SHADER_SLOWTIME, slowtimeRem);
        screenShader.setUniformf(Constants.SHADER_DARKBACKGROUND, darkbackground ? 1.f : 0.f);

        super.draw(batch, alpha);
    }

    public Group getTopGroup() {
        return topGroup;
    }

    @Override
    public void setStage(Stage stage) { //override perché l'altro metodo è protected, e non accessibile allo shader stage
        super.setStage(stage);
    }
}
