package com.pizzaroof.sinfulrush.actors.physics.game_actors.enemies;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.pizzaroof.sinfulrush.actors.stage.OptimizedStage;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.util.Utils;

import java.io.IOException;
import java.util.HashSet;

/**è uno sniper enemy, ma invece di sparare al player spara ad altri nemici e li cura.
 * l'ultima riga del file info.txt ha un significato diverso:
 * hp_curati | tempo di ricarica per sfera cura | path effetto | velocità sfera che cura | raggio sfera che cura | (minima) massima distanza per curare | (massima) max dist per curare | path esplosione (null per nessuna) | nome colore effetto*/
public class HealerSniperEnemy extends FlyingSniperEnemy {

    /**gruppo di tutti i nemici vivi*/
    protected Group enemiesGroup;

    /**
     * @param backgroundGroup gruppo dove mettere le cose di background
     * @param effectGroup     gruppo dove mettere gli effetti che si creano (tipo sfere di energia)
     * @param enemiesGroup    gruppo di tutti i nemici vivi
     */
    public HealerSniperEnemy(String directory, SoundManager soundManager, AssetManager asset, Stage stage, com.pizzaroof.sinfulrush.actors.physics.World2D world, Vector2 initPosition, Group backgroundGroup, Group effectGroup, Group enemiesGroup, Shape... shapes) {
        super(directory, soundManager, asset, stage, world, initPosition, backgroundGroup, effectGroup, shapes);
        setTarget(null);
        this.enemiesGroup = enemiesGroup;
    }

    @Override
    protected void computeTarget() {
        if(getBody() == null) return;

        //comportamento: prendiamo il tizio più vicino
        target = null;
        float dst2 = 0;

        Enemy backup = null; //nemico di backup: cioè un curatore, se non trovi altri nemici da curare, cura un curatore
        float dst2backup = 0; //distanza per il curatore di backup

        HashSet<Enemy> enemies = ((OptimizedStage)getStage()).getDamagedEnemies();

        for(Enemy e : enemies) //scegli un nemico a cui mancano degli hp
            if(e.getHp() < e.getMaxHp() && e.getHp() > 0 && e.getBody() != null) { //controllo non necessario... se stanno in damaged enemies sicuro gli mancano hp
                if(e.hashCode() == this.hashCode() || e instanceof FriendEnemy) continue; //non posso curare me stesso, ne un amico del player

                float d2 = e.getBody().getPosition().dst2(getBody().getPosition()); //calcola distanza al quadrato tra nemico da curare e questo healer

                if(e instanceof HealerSniperEnemy) { //healer...
                    if(backup == null || d2 < dst2backup) { //prendo comunque il più vicino
                        backup = e;
                        dst2backup = d2;
                    }
                }
                else if(target == null || d2 < dst2) { //scegli quello più vicino tra quelli da curare
                    setTarget(e);
                    dst2 = d2;
                }
            }

        if(target == null) //se il target è null.. prova col backup (NB: potrebbe essere null pure quello: amen)
            setTarget(backup);
    }

    /**factory method (è uguale a Flying... ma deve restituire un healer)*/
    public static HealerSniperEnemy createEnemy(String directory, SoundManager soundManager, AssetManager assetManager, World2D world, Stage stage, Vector2 initPosition, Group backgroundGroup, Group effectGroup, Group enemiesGroup) {
        try {
            Vector2 dim = com.pizzaroof.sinfulrush.util.Utils.enemyDrawingDimensions(directory);
            Shape shapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(com.pizzaroof.sinfulrush.util.Utils.enemyShapePath(directory), dim.x, dim.y, world.getPixelPerMeter()); //shape
            Shape deadShapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(com.pizzaroof.sinfulrush.util.Utils.enemyDeadShapePath(directory), dim.x, dim.y, world.getPixelPerMeter()); //dead shape
            Shape flipDeadShapes[] = com.pizzaroof.sinfulrush.util.Utils.getShapesFromFile(Utils.enemyDeadShapePath(directory), dim.x, dim.y, world.getPixelPerMeter(), true); //dead flipped shape
            HealerSniperEnemy e = new HealerSniperEnemy(directory, soundManager, assetManager, stage, world, initPosition, backgroundGroup, effectGroup, enemiesGroup, shapes);
            e.setDeadShapes(deadShapes);
            e.setDeadFlippedShapes(flipDeadShapes);
            return e;
        }catch(IOException e) { //non dovrebbe succedere
            e.printStackTrace();
        }
        return null;
    }
}
