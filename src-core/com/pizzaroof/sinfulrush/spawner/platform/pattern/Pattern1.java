package com.pizzaroof.sinfulrush.spawner.platform.pattern;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.util.Pair;

/**pattern1: dividiamo le x in 3 parti, la prima piattaforma starà nel primo terzo, la seconda piattaforma starà almeno ad un terzo di distanza dalla prima
 * (NB: quindi le piattaforme coinvolte devono avere lunghezza minore di 1/3 dello schermo), infine si crea una terza piattaforma sotto la seconda, spostata leggermente a sinistra.*/
public class Pattern1 extends Pattern {

    public Pattern1(boolean flip) {
        super(flip);
    }

    @Override
    public boolean isPossible(Platform last, float viewportWidth, Player player) {
        //non facciamo controlli troppo precisi:
        //per applicare il pattern 1 vogliamo il primo terzo di schermo libero (o l'ultimo terzo se è flippato)
        return (!flip && last.getX() > viewportWidth/3) || (flip && last.getX() + last.getWidth() < 2*viewportWidth/3);
    }

    @Override
    public void start() {
        remaining = 3; //3 componenti
    }

    @Override
    public com.pizzaroof.sinfulrush.spawner.platform.pattern.PlatformInfo generate(Platform last, Pair<String, Vector2> type, float viewportWidth, RandomXS128 rand, float spaceUp, Player player) {

        com.pizzaroof.sinfulrush.spawner.platform.pattern.PlatformInfo info = new PlatformInfo();
        info.name = type.v1; //alcune informazioni già le conosciamo
        info.width = (int)type.v2.x;
        info.height = (int)type.v2.y;
        info.position = new Vector2();

        float lastx = flip ? (viewportWidth - last.getX() - last.getWidth()) : last.getX();

        switch(remaining) {
            case 3: //primo del pattern
                info.position.x = rand.nextInt(Math.max(1, (int)(viewportWidth/3 - info.width))); //primo terzo di schermo
                info.position.y = last.getY() - info.height - spaceUp; //abbastanza in basso
            break;

            case 2: //secondo elemento pattern
                info.position.x = lastx + last.getWidth() + viewportWidth/3; //a destra della prima
                info.position.y = last.getY() - (rand.nextFloat()+Player.MAGIC_PADDING_MUL) * player.getHeight();
            break;

            case 1: //ultimo elemento pattern
                info.position.x = lastx - info.width + 1 + rand.nextInt(info.width/2); //a sinistra della seconda
                info.position.y = last.getY() - info.height - spaceUp;
            break;
        }

        if(flip) //invertiamo x
            info.position.x = viewportWidth - info.width - info.position.x;

        remaining--;
        return info;
    }
}
