package com.pizzaroof.sinfulrush.spawner.platform;

import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Platform;
import com.pizzaroof.sinfulrush.actors.physics.game_actors.Player;
import com.pizzaroof.sinfulrush.spawner.HoleFiller;

import java.util.ArrayList;

/**classe che individua buchi liberi lasciati dai generatori di piattaforme*/
public class HoleCatcher {

    /**piattaforme aggiunte (solo alcune in realtà)*/
    private ArrayList<Platform> platforms;

    /**giocatore*/
    private Player player;

    /**larghezza viewport*/
    private float viewportWidth;

    /**oggetto per riempire i buchi*/
    private com.pizzaroof.sinfulrush.spawner.HoleFiller filler;

    public HoleCatcher(Player player) {
        platforms = new ArrayList<>();
        this.player = player;
        filler = null;
    }

    /**aggiunge una piattaforma a quelle che il catcher può analizzare*/
    public void addPlatform(Platform pad, World2D world2d) {
        platforms.add(pad);
        if(platforms.size() > 3) //manteniamo solo 3 piattaforme
            platforms.remove(0);
        checkHoles(world2d);
    }

    /**controlla se ci sono buchi dopo aver aggiunto una piattaforma*/
    private void checkHoles(World2D world2d) {
        //l'idea è individuare 3 piattaforme
        //e trovare il buco formato dalla piattaforma centrale:
        //lo mettiamo ai lati della piattaforma centrale e compreso tra la piattaforma superiore e inferiore
        //ASSUMIAMO CHE IL GENERATORE DI PIATTAFORME CREI PIATTAFORME CRESCENTI/DECRESCENTI

        if(platforms.size() < 3) return; //ci servono esattamente 3 piattaforme

        Platform mid = platforms.get(1);
        Platform down = platforms.get(0).getY() < mid.getY() ? platforms.get(0) : platforms.get(2); //piattaforma sotto la mid (a livello di y, non di ordinamento)
        Platform up = platforms.get(0).getY() < mid.getY() ? platforms.get(2) : platforms.get(0); //piattaforma sopra la mid

        float miny = down.getY() + down.getHeight() + 2 * Math.max(player.getHeight(), down.getHeightOfMonsters()); //lasciamo parecchio spazio sopra la piattaforma bassa (perchè sopra ci saranno giocatore e nemici)
        float maxy = up.getY(); //sotto quella alta non serve molto spazio, tanto stiamo fuori da quella centrale
        if(maxy < miny) return; //vogliamo lasciare abbastanza spazio sopra e sotto le piattaforme per non creare problemi

        //sapendo che il player salterà da 0 a 1 a 2, troviamo quali sono le x minime e massime occupate durante il salto
        float minjx = platforms.get(1).getX(), maxjx = platforms.get(1).getX() + platforms.get(1).getWidth();
        for(int i=0; i<platforms.size()-1; i++) {
            //per il salto da i a i+1, è occupato da x_to_jump a x_for_destination
            float jx = player.xFromWhichToJump(platforms.get(i), platforms.get(i+1));
            minjx = Math.min(minjx, jx);
            maxjx = Math.max(maxjx, jx);
            jx = player.getDestinationForTheJump(platforms.get(i), platforms.get(i+1)).x;
            minjx = Math.min(minjx, jx);
            maxjx = Math.max(maxjx, jx);
        }
        minjx -= player.getWidth(); //aggiungiamo un padding pari alla larghezza del giocatore per stare sicuri
        maxjx += player.getWidth();

        /*Shape shapes[] = new Shape[1];
        shapes[0] = Utils.getBoxShape((maxjx - minjx)/world2d.getPixelPerMeter(), (maxy - miny)/world2d.getPixelPerMeter());
        Vector2 pos = new Vector2((minjx + maxjx) / 2 / world2d.getPixelPerMeter(), (miny + maxy) / 2 / world2d.getPixelPerMeter());
        PhysicSpriteActor actor = new PhysicSpriteActor(world2d, BodyDef.BodyType.KinematicBody, 0, 0, 0, pos, true, shapes);
        player.getStage().addActor(actor);*/

        //lo mettiamo in sostanza: ai lati di quello centrale, più basso della piattaforma alta e più alto della piattaforma bassa
        if(minjx > 0) //area a sinistra di mid
            onHoleCaught(0, miny, minjx, maxy);
        if(maxjx < viewportWidth) //area a destra di mid
            onHoleCaught(maxjx, miny, viewportWidth, maxy);
    }

    public void setViewportWidth(float w) {
        viewportWidth = w;
    }

    /**callback per quando catturiamo un "buco" (fornisce un rettangolo che si può riempire come si ritiene opportno)*/
    public void onHoleCaught(float minX, float minY, float maxX, float maxY) {
        //comportamento di default: delegare al filler (sarebbe bene fare tutto coi filler, ma si può modificare)
        if(filler != null)
            filler.fillHole(minX, minY, maxX, maxY);
    }

    /**setta il filler per riempire i buchi*/
    public void setHoleFiller(HoleFiller filler) {
        this.filler = filler;
    }
}
