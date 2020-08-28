package com.pizzaroof.sinfulrush.spawner.platform.pattern;

import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector2;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.util.Pair;

/**pattern2: 3 piattaforme disposte su x crescenti (o decrescenti) e y decrescenti (ma poco)*/
public class Pattern2 extends Pattern1 {
    public Pattern2(boolean flip) {
        super(flip);
    }

    @Override
    public boolean isPossible(Platform last, float viewportWidth, Player player) {
        //deve esserci spazio all'estremo sinistro o all'estremo detro (dipende dal flip)
        return (!flip && last.getX() > player.getWidth()*1.5f) || (flip && last.getX() + last.getWidth() < viewportWidth - player.getWidth()*1.5f);
    }

    @Override
    public com.pizzaroof.sinfulrush.spawner.platform.pattern.PlatformInfo generate(Platform last, Pair<String, Vector2> type, float viewportWidth, RandomXS128 rand, float spaceUp, Player player) {
        com.pizzaroof.sinfulrush.spawner.platform.pattern.PlatformInfo info = new PlatformInfo();
        info.name = type.v1; //alcune informazioni già le conosciamo
        info.width = (int)type.v2.x;
        info.height = (int)type.v2.y;
        info.position = new Vector2();

        switch(remaining) {
            case 3: //primo creato
                if(!flip) //molto a sinistra (o molto a destra se è flippato)
                    info.position.x = rand.nextInt(1 + (int)Math.min(info.width*0.5f, last.getX() - player.getWidth()*1.5f));
                else {
                    float minX = Math.max(last.getX() + last.getWidth() + player.getWidth()*1.5f, 2*viewportWidth/3 + info.width) - info.width;
                    info.position.x = minX + rand.nextInt(Math.max(1, (int)(viewportWidth - info.width - minX)));
                }
                info.position.y = last.getY() - info.height - spaceUp;
            break;

            case 2: //secondo creato
                if(!flip)
                    info.position.x = last.getX() + last.getWidth() + (1 + rand.nextFloat()) * player.getWidth(); //poco più a destra
                else
                    info.position.x = last.getX() - info.width - (1 + rand.nextFloat()) * player.getWidth();
                info.position.y = last.getY() - (Player.MAGIC_PADDING_MUL + rand.nextFloat()) * player.getHeight(); //poco più basso
            break;

            case 1: //terzo creato
                if(!flip)
                    info.position.x = Math.min(viewportWidth - info.width, last.getX() + last.getWidth() + (1 + rand.nextFloat()) * player.getWidth()); //poco più a destra
                else
                    info.position.x = Math.max(0, last.getX() - info.width - (1 + rand.nextFloat()) * player.getWidth());
                info.position.y = last.getY() - info.height - spaceUp;
            break;
        }

        remaining--;
        return info;
    }
}
