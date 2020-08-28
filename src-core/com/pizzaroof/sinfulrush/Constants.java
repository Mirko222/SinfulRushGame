package com.pizzaroof.sinfulrush;

import com.badlogic.gdx.math.Vector2;

/**classe che contiene solo costanti importanti in tutto il gioco*/
public class Constants {
    /**dimensioni virtuali dello schermo*/
    public static final int VIRTUAL_WIDTH = 1080, VIRTUAL_HEIGHT = 1920;

    /**quanti pixel ci sono in un metro?*/
    public static final float PIXELS_PER_METER = 250.f;

    /**vettore gravità nel mondo*/
    public static final Vector2 GRAVITY_VECTOR = new Vector2(0, -10f);

    /**epsilon (un valore mooolto vicino a 0: usalo per confronti con float)*/
    public static final float EPS = 0.0001f;

    public static final int INFTY_HP = 999999999;

    /**score minimo per sbloccare il cimitero*/
    public static final int SCORE_TO_UNLOCK_CEMETERY = 250; //250

    /**attrito di default per le piattaforme*/
    public static final float DEFAULT_PLATFORM_FRICTION = 0.8f;
    /**attrito piccolo ma che ha un effetto visibile ad occhio (utile se vogliamo dare poco poco attrito alle cose)*/
    public static final float SMALL_BUT_EFFECTIVE_FRICTION = 0.08f;

    /**skin di default per la ui*/
    public static final String DEFAULT_SKIN_PATH = "ui_skin/tasti.json";

    public static final String DEFAULT_SKIN_ATLAS = "ui_skin/tasti.atlas";

    /**suffisso che identifica la texture atlas all'interno della cartella di una piattaforma*/
    public static final String PAD_ATLAS_SUFFIX = "pad.pack";
    /**suffisso che identifica il file shape relativo al nome di una piattaforma*/
    public static final String PAD_SHAPE_SUFFIX = "shape";
    /**suffisso che identifica il file dimensione relativo al nome di una piattaforma*/
    public static final String PAD_DIM_SUFFIX = "dim";

    /**suffisso per la texture atlas con le animazioni in una cartella del player*/
    public static final String PLAYER_SCML_SUFFIX = "animations.scml";
    /**suffisso per informazioni aggiuntive sul player*/
    public static final String PLAYER_INFO_FILE_SUFFIX = "info.txt";
    /**suffisso per shape del player*/
    public static final String PLAYER_SHAPE_FILE_SUFFIX = "shape.txt";

    /**suffisso per la texture atlas con le animazioni in una cartella di un enemy*/
    public static final String ENEMY_SCML_SUFFIX = "animations.scml";
    /**suffisso per informazioni aggiuntive sul nemico*/
    public static final String ENEMY_INFO_FILE_SUFFIX = "info.txt";
    /**suffisso per shape del nemico*/
    public static final String ENEMY_SHAPE_FILE_SUFFIX = "shape.txt";
    /**atlas del nemico (da usare solo per prendere qualche oggetto particolare)*/
    public static final String ENEMY_ATLAS_SUFFIX = "atlas.pack";

    /**suffisso per file scml dentro cartella bonus*/
    public static final String BONUS_SCML_SUFFIX = "animations.scml";
    /**suffisso per informazioni aggiuntive sul bonus*/
    public static final String BONUS_INFO_SUFFIX = "info.txt";

    /**suffisso per shape di morte del nemico (solo alcuni ce l'hanno, altri lasciano lo stesso)*/
    public static final String DEAD_ENEMY_SHAPE_FILE_SUFFIX = "deadshape.txt";

    /**suffisso per individuare la grafica spriter per un certo effetto*/
    public static final String SPRITER_GRAPHICS_EFFECT_SUFFIX = "animations.scml";
    /**suffisso per individuare le info su un effetto spriter*/
    public static final String SPRITER_EFFECT_INFO_SUFFIX = "info.txt";

    /**larghezza/altezza per considerare "grande" un nemico*/
    public static final int BIG_ENEMY_THRESHOLD = 180;
    /**larghezza/altezza per considerare "medio" un nemico*/
    public static final int MEDIUM_ENEMY_THRESHOLD = 120;

    /**percorso della catena di markov usata dai nemici quando sono soli sulla piattaforma*/
    public static final String ENEMY_EMPTY_PLATFORM_MARKOV = "markov_chains/enemy_empty_movement.mkc";

    /**catena di markov per i bonus quando c'è il boss (facciamo uscire solo alcuni bonus, che potrebbero essere utili)*/
    public static final String BOSS_BONUS_MARKOV_CHAIN = "markov_chains/boss_bonus_spawns.mkc";

    /**category bits per il player: indica che tipo di oggetto è, in modo che gli altri possono decidere se entrarci in collisione o no*/
    public static final short PLAYER_CATEGORY_BITS = 0x1;

    /**category bits per i nemici*/
    public static final short ENEMIES_CATEGORY_BITS = 0x2;

    /**category bits per le particelle fisiche*/
    public static final short PARTICLES_CATEGORY_BITS = 0x4;

    /**category bits per le powerball*/
    public static final short POWERBALL_CATEGORY_BITS = 0x8;

    /**----------tutte le cartelle dei personaggi/piattaforme/nemici------------*/

    public static final String THIEF_DIRECTORY = "players/thief";
    public static final String WEREWOLF_DIRECTORY = "players/werewolf";
    public static final String ELF_DIRECTORY = "players/elf";
    public static final String KNIGHT_DIRECTORY = "players/knight";

    public static final String DEMON_DARKNESS_1_DIRECTORY = "enemies/hell/demon_darkness_1";
    public static final String DEMON_DARKNESS_2_DIRECTORY = "enemies/hell/demon_darkness_2";
    public static final String DEMON_DARKNESS_3_DIRECTORY = "enemies/hell/demon_darkness_3";
    public static final String DEVIL_CHIBI_DIRECTORY = "enemies/hell/devil_chibi";
    public static final String HELL_KNIGHT_CHIBI_DIRECTORY = "enemies/hell/hell_knight_chibi";
    public static final String SUCCUBUS_CHIBI_DIRECTORY = "enemies/hell/succubus_chibi";
    public static final String FALLEN_ANGEL_1_DIRECTORY = "enemies/hell/fallen_angel_1";
    public static final String FALLEN_ANGEL_2_DIRECTORY = "enemies/hell/fallen_angel_2";
    public static final String CERBERUS_DIRECTORY = "enemies/hell/cerberus";
    public static final String LAVA_GOLEM_DIRECTORY = "enemies/hell/lava_golem";
    public static final String LAVA_GOLEM_2_DIRECTORY = "enemies/hell/lava_golem2";
    public static final String LAVA_GOLEM_3_DIRECTORY = "enemies/hell/lava_golem3";
    public static final String GARGOYLE_1_DIRECTORY = "enemies/hell/gargoyle_1";
    public static final String GARGOYLE_2_DIRECTORY = "enemies/hell/gargoyle_2";
    public static final String GARGOYLE_1_FLY_DIRECTORY = "enemies/hell/gargoyle_1_fly";
    public static final String GARGOYLE_2_FLY_DIRECTORY = "enemies/hell/gargoyle_2_fly";
    public static final String RED_DEMON_DIRECTORY = "enemies/hell/red_demon";
    public static final String BLUE_DEMON_DIRECTORY = "enemies/hell/blue_demon";
    public static final String PURPLE_DEMON_DIRECTORY = "enemies/hell/purple_demon";
    public static final String ELEMENTAL_DIRECTORY = "enemies/hell/elemental";
    public static final String GHOUL_DIRECTORY = "enemies/cemetery/ghoul";
    public static final String MUMMY_DIRECTORY = "enemies/cemetery/mummy";
    public static final String ARMORED_SKULL_DIRECTORY = "enemies/cemetery/armored_skull_chibi";
    public static final String LICH_DIRECTORY = "enemies/cemetery/lich_chibi";
    public static final String SKELETON_CHIBI_DIRECTORY = "enemies/cemetery/skeleton_chibi";
    public static final String ZOMBIE_CHIBI_DIRECTORY = "enemies/cemetery/zombie_chibi";
    public static final String DEATH_REAPER = "enemies/cemetery/death_reaper";
    public static final String BLUE_NECROMANCER = "enemies/cemetery/blue_necromancer";
    public static final String RED_NECROMANCER = "enemies/cemetery/red_necromancer";
    public static final String GREEN_NECROMANCER = "enemies/cemetery/green_necromancer";
    public static final String BLUE_SKELETON = "enemies/cemetery/blue_skeleton";
    public static final String GREEN_SKELETON = "enemies/cemetery/green_skeleton";
    public static final String RED_SKELETON = "enemies/cemetery/red_skeleton";
    public static final String VAMPIRE_DIRECTORY = "enemies/cemetery/vampire";
    public static final String GHOST_HEALER_DIRECTORY = "enemies/cemetery/ghost_healer";
    public static final String GIANT_SKELETON1 = "enemies/cemetery/giant_skeleton1";
    public static final String GIANT_SKELETON2 = "enemies/cemetery/giant_skeleton2";
    public static final String GIANT_ZOMBIE1 = "enemies/cemetery/giant_zombie1";
    public static final String GIANT_ZOMBIE2 = "enemies/cemetery/giant_zombie2";
    public static final String GIANT_ZOMBIE3 = "enemies/cemetery/giant_zombie3";
    public static final String GHOST1_DIRECTORY = "enemies/cemetery/ghost1";
    public static final String GHOST2_DIRECTORY = "enemies/cemetery/ghost2";
    public static final String FLYING_GHOST1_DIRECTORY = "enemies/cemetery/flying_ghost1";
    public static final String FLYING_GHOST2_DIRECTORY = "enemies/cemetery/flying_ghost2";
    public static final String MINI_SKELETON1_DIRECTORY = "enemies/cemetery/mini_skeleton_1";
    public static final String MINI_SKELETON2_DIRECTORY = "enemies/cemetery/mini_skeleton_2";
    public static final String MINI_SKELETON3_DIRECTORY = "enemies/cemetery/mini_skeleton_3";
    public static final String YELLOW_FRIEND_DIRECTORY = "enemies/shared/yellow_friend";
    public static final String RED_FRIEND_DIRECTORY = "enemies/shared/red_friend";
    public static final String ORANGE_FRIEND_DIRECTORY = "enemies/shared/orange_friend";
    public static final String OGRE_DIRECTORY = "enemies/tutorial/ogre";

    public static final String HELL_PAD_1 = "platforms/hell/p1";
    public static final String HELL_PAD_2 = "platforms/hell/p2";
    public static final String HELL_PAD_3 = "platforms/hell/p3";
    public static final String HELL_PAD_4 = "platforms/hell/p4";
    public static final String CEMETERY_PAD_1 = "platforms/cemetery/cmpad1";
    public static final String CEMETERY_PAD_2 = "platforms/cemetery/cmpad2";
    public static final String CEMETERY_PAD_3 = "platforms/cemetery/cmpad3";
    public static final String CEMETERY_PAD_4 = "platforms/cemetery/cmpad4";
    public static final String CEMETERY_PAD_1_COVER = "cmpad1_cover";
    public static final String CEMETERY_PAD_2_COVER = "cmpad2_cover";
    public static final String CEMETERY_PAD_3_COVER = "cmpad3_cover";
    public static final String CEMETERY_PAD_4_COVER = "cmpad4_cover";
    public static final String TUTORIAL_PAD_1 = "platforms/tutorial/1";
    public static final String TUTORIAL_PAD_2 = "platforms/tutorial/2";

    public static final String SWORD_BONUS_DIRECTORY = "bonus/sword";
    public static final String BONUS_ICON_TYPE_1_DIR = "bonus/bonusIcon1";
    public static final String LIGHTNING_BONUS_NAME = "bonus/lightning"; //nome dei bonus, che però graficamente sono bonusType1
    public static final String WIND_BONUS_NAME = "bonus/wind";
    public static final String ICE_BONUS_NAME = "bonus/ice";
    public static final String ELISIR_S_BONUS_NAME = "bonus/elisir_s";
    public static final String ELISIR_M_BONUS_NAME = "bonus/elisir_m";
    public static final String ELISIR_L_BONUS_NAME = "bonus/elisir_l";
    public static final String SLOWTIME_BONUS_NAME = "bonus/slowtime";
    public static final int WIND_CHAR_MAPS = 0; //id della char maps per wind
    public static final int ICE_CHAR_MAPS = 1;
    public static final int ELISIR_S_CHAR_MAPS = 2;
    public static final int ELISIR_M_CHAR_MAPS = 3;
    public static final int ELISIR_L_CHAR_MAPS = 4;
    public static final int SLOWTIME_CHAR_MAPS = 5;

    public static final String SWORD_BONUS_NAME = "bonus/sword", DOUBLE_SWORD_BONUS_NAME = "bonus/Dsword";
    public static final String RAGE_SWORD_BONUS_NAME = "bonus/ragesword", DOUBLE_RAGE_SWORD_BONUS_NAME = "bonus/Dragesword";
    public static final String SCEPTRE_BONUS_NAME = "bonus/sceptre";
    public static final String SCEPTRE_SPLIT_BONUS_NAME = "bonus/sceptresplit";
    public static final String GLOVE_BONUS_NAME = "bonus/glove";
    public static final int RAGE_SWORD_CHAR_MAPS = 0;
    public static final int SCEPTRE_CHAR_MAPS = 2;
    public static final int SCEPTRE_SPLIT_CHAR_MAPS = 1;
    public static final int GLOVE_CHAR_MAPS = 3;

    /**------------costanti per proprietà dagli attacchi--------------*/

    public static final String PUNCH_ATLAS = "punch/atlas.pack";

    /**---------effetti vari-------*/
    public static final String FIREBALL_EFFECT = "effects/particles/fireball.pe";
    public static final String HEAL_FIREBALL_EFFECT = "effects/particles/greenfireball.pe";
    public static final String HEAL_EFFECT = "effects/particles/heal1.pe";
    public static final String MEDIUM_HEAL_EFFECT = "effects/particles/healmedium1.pe";
    public static final String LARGE_HEAL_EFFECT = "effects/particles/healbig1.pe";
    public static final String FIRE_EXPLOSION_EFFECT = "effects/particles/fire_explosion.pe";
    public static final String FRIEND_BALL_EFFECT = "effects/particles/friend_fireball.pe";
    public static final String BOSS_FIREBALL = "effects/particles/fireball2.pe";
    public static final String BOSS_FIREBALL_EXPLOSION = "effects/particles/fire_explosion2.pe";
    public static final String TREASURE_BALL = "effects/particles/treasure_ball.pe";
    public static final String TREASURE_EXPLOSION = "effects/particles/treasure_explosion.pe";

    public static final String SCEPTRE_BALL_EFFECT = "effects/sheet/sceptreball";
    public static final int SCEPTRE_BALL_ORIGINAL_WIDTH = 466;
    public static final int SCEPTRE_B_CHARACTER_MAPS = 0;

    public static final String DISAPPEARING_SMOKE = "effects/sheet/smokeball";
    public static final String EXPLOSION_EFFECT = "effects/sheet/explosion";

    public static final String LIGHTNING_EFFECT = "effects/sheet/lightning";
    public static final float LIGHTNING_ORIGINAL_WIDTH = 198, LIGHTNING_ORIGINAL_HEIGHT = 618;

    public static final String WIND_EFFECT = "effects/sheet/wind";
    public static final float ORIGINAL_WIND_WIDTH = 512, ORIGINAL_WIND_HEIGHT = 294;

    public static final String ICE_EFFECT = "effects/sheet/ice";
    public static final float ORIGINAL_ICE_WIDTH = 410, ORIGINAL_ICE_HEIGHT = 563;
    public static final float ORIGINAL_ICE_FLYING_WIDTH = 480, ORIGINAL_ICE_FLYING_HEIGHT = 480;

    public static final String PHYSIC_PARTICLE_ATLAS = "effects/physic_particles/particles.atlas";

    public static final String PHYSIC_PARTICLE_BLOOD = "effects/physic_particles/blood.ppe";
    public static final int DEF_MIN_BLOOD_RADIUS = 6, DEF_MAX_BLOOD_RADIUS = 13;

    public static final String PHYSIC_PARTICLE_BONES = "effects/physic_particles/bones.ppe";
    public static final int DEF_MIN_BONES_RADIUS = 15, DEF_MAX_BONES_RADIUS = 30;

    public static final String PHYSIC_PARTICLE_MEDIUM_BONES = "effects/physic_particles/medium_bones.ppe";
    public static final int DEF_MIN_MEDBONES_RADIUS = 23, DEF_MAX_MEDBONES_RADIUS = 38;

    public static final String PHYSIC_PARTICLE_BIG_BONES = "effects/physic_particles/big_bones.ppe";
    public static final int DEF_MIN_BIGBONES_RADIUS = 35, DEF_MAX_BIGBONES_RADIUS = 45;

    public static final String PHYSIC_PARTICLE_FEATHER = "effects/physic_particles/piume.ppe";
    public static final int DEF_MIN_FEATHER_RADIUS = 18, DEF_MAX_FEATHER_RADIUS = 33;

    public static final String PHYSIC_PARTICLE_ZOMBIEP = "effects/physic_particles/zombie_piece.ppe", PHYSIC_PARTICLE_ZOMBIEP2 = "effects/physic_particles/zombie_piece2.ppe", PHYSIC_PARTICLE_ZOMBIEP3 = "effects/physic_particles/zombie_piece3.ppe";
    public static final int DEF_MIN_ZOMBIEP_RADIUS = 18, DEF_MAX_ZOMBIEP_RADIUS = 33;

    public static final String PHYSIC_PARTICLE_LAVA_ROCKS = "effects/physic_particles/lavarocks.ppe";
    public static final int DEF_MIN_LAVAROCKS_RADIUS = 15, DEF_MAX_LAVAROCKS_RADIUS = 30;

    public static final String PHYSIC_PARTICLE_LAVA_ROCKS_3 = "effects/physic_particles/lavarocks3.ppe";
    public static final int DEF_MIN_LAVAROCKS3_RADIUS = 15, DEF_MAX_LAVAROCKS3_RADIUS = 30;

    public static final String PHYSIC_PARTICLE_LAVA_ROCKS_2 = "effects/physic_particles/lavarocks2.ppe";
    public static final int DEF_MIN_LAVAROCKS2_RADIUS = 13, DEF_MAX_LAVAROCKS2_RADIUS = 20;

    public static final String PHYSIC_PARTICLE_ELEM_ROCKS = "effects/physic_particles/lavarocks_elemental.ppe";
    public static final int DEF_MIN_ELEM_ROCKS_RADIUS = 25, DEF_MAX_ELEM_ROCKS_RADIUS = 40;

    public static final String SPARKLE_ATLAS = "effects/sheet/sparkle/sparkles.atlas";
    public static final String SPARKLE_REG_NAME = "sparkle";

    public static final String SWORD_SHADER = "shaders/sword/shader.vert";
    public static final String MAIN_SCREEN_SHADER = "shaders/mainscreen/shader.vert";

    /**----------GUI---------*/
    public static final String LOGO_TEXTURE = "logo.png";
    public static final String NAME_TEXTURE = "name.png";

    public static final String CUSTOM_BUTTONS_DECORATIONS = "ui/main_menu/custom_buttons/atlas.pack";
    public static final String COIN_ATLAS = CUSTOM_BUTTONS_DECORATIONS; //per motivi di efficienza
    public static final String COIN_REG_NAME = "coin-gold";

    public static final String HEALTH_BAR_ATLAS = "ui/health_bar/atlas.pack";
    public static final String HEALTH_BAR_IN_CENTER_NAME = "center_in";
    public static final String HEALTH_BAR_IN_BORDER_NAME = "border_in";
    public static final String HEALTH_BAR_OUT_CENTER_NAME = "center_out";
    public static final String HEALTH_BAR_OUT_BORDER_NAME = "border_out";

    public static final String HUD_ATLAS = "ui/hud/atlas.pack";

    public static final String SHOP_PLAYERS_ATLAS = "shop_players/atlas.pack";

    /**------- backgrounds ------*/

    public static final String HELL_GRADIENT_BG = "backgrounds/hell/gradient.png";
    public static final String HELL_DECORATIONS = "backgrounds/hell/decorations/atlas.pack";
    public static final String HELL_DECORATIONS_BG = "backgrounds/hell/decorations2/atlas.pack";
    public static final String HELL_METEORS = "backgrounds/hell/meteors/atlas.pack";

    public static final String CEMETERY_GRADIENT_BG = "backgrounds/cemetery/gradient.png";
    public static final String CEMETERY_FIRST_GRADIENT_BG = "backgrounds/cemetery/first_gradient.png";
    public static final String CEMETERY_DECORATIONS = "backgrounds/cemetery/decorations/atlas.pack";

    public static final String MENU_BACKGROUND = "ui/main_menu/bg.png";

    /**--------- shaders names-----*/

    public static final String RAGE_TIME_NAME = "u_rage_time";
    public static final String SHADER_SCREEN_RESOLUTION = "u_resolution";
    public static final String SHADER_SLOWTIME = "u_slowtime";
    public static final String SHADER_DARKBACKGROUND = "u_darkbackground";

    /**---------- music and sounds --------*/

    public static final String CEMETERY_SOUNDTRACK_INTRO = "sounds/soundtracks/soundtrack_cemetery_intro.ogg";
    public static final String CEMETERY_SOUNDTRACK_LOOP = "sounds/soundtracks/soundtrack_cemetery_loop.ogg";
    public static final String HELL_SOUNDTRACK = "sounds/soundtracks/soundtrack_hell.ogg";
    public static final String MENU_SOUNDTRACK = "sounds/soundtracks/soundtrack_menu.ogg";
    public static final String BOSS_INTRO_SOUNDTRACK = "sounds/soundtracks/soundtrack_boss_intro.ogg";
    public static final String BOSS_LOOP_SOUNDTRACK = "sounds/soundtracks/soundtrack_boss_loop.ogg";
    public static final String HELL_SOUNDTRACK2 = "sounds/soundtracks/soundtrack_hell2.ogg";
    public static final String CEMETERY_SOUNDTRACK_INTRO2 = "sounds/soundtracks/soundtrack_cemetery2_intro.ogg";
    public static final String CEMETERY_SOUNDTRACK_LOOP2 = "sounds/soundtracks/soundtrack_cemetery2_loop.ogg";
    public static final String BOSS_INTRO_SOUNDTRACK2 = "sounds/soundtracks/soundtrack_boss2_intro.ogg";
    public static final String BOSS_LOOP_SOUNDTRACK2 = "sounds/soundtracks/soundtrack_boss2_loop.ogg";

    public static final String BUTTON_CLICK_SFX = "sounds/sfx/button_click.ogg";
    public static final String SCEPTRE_SPAWN_SFX = "sounds/sfx/sceptre_spawn.ogg";
    public static final String ICE_ACTIVATION_SFX = "sounds/sfx/ice.ogg";
    public static final String PUNCH_HIT_SFX = "sounds/sfx/punch_hit.ogg";
    public static final String PUNCH_DAMAGE_SFX = "sounds/sfx/punch_damage.ogg";
    public static final String BONUS_TAKEN_SFX = "sounds/sfx/bonus_taken.ogg";
    public static final String ENEMY_DEATH_SFX = "sounds/sfx/enemy_death.ogg";
    public static final String ENEMY_DEATH2_SFX = "sounds/sfx/enemy_death2.ogg";
    public static final String PLAYER_HURT_SFX = "sounds/sfx/player_hurt.ogg";
    public static final String HEALTH_POTION_SFX = "sounds/sfx/health_potion.ogg";
    public static final String SWORD_SWING_SFX = "sounds/sfx/sword.ogg";
    public static final String SWORD_DAMAGE_SFX = "sounds/sfx/sword_damage.ogg";
    public static final String SLOWTIME_SFX = "sounds/sfx/slowtime.ogg";
    public static final String THUNDER_SFX = "sounds/sfx/thunder.ogg";
    public static final String WIND_SFX = "sounds/sfx/wind.ogg";
    public static final String PLAYER_DEATH_SFX = "sounds/sfx/player_death.ogg";
    public static final String RAGE_SFX = "sounds/sfx/rage.ogg";
    public static final String SCEPTRE_EXPLOSION_SFX = "sounds/sfx/sceptre_explosion.ogg";
    public static final String PORTAL_SFX = "sounds/sfx/portal.ogg";
    public static final String CLOCK_TICK_SFX = "sounds/sfx/clock.ogg";
    public static final String BIG_EXPLOSION_SFX = "sounds/sfx/big_explosion1.ogg";
    public static final String BIG_EXPLOSION2_SFX = "sounds/sfx/big_explosion2.ogg";
    public static final String BOSS_DEATH_SFX = "sounds/sfx/boss_death.ogg";
    public static final String BOSS_HURT_SFX = "sounds/sfx/boss_hurt.ogg";
    public static final String BOSS_ROAR_SFX = "sounds/sfx/boss_roar.ogg";
    public static final String BOSS_ATTACK_SFX = "sounds/sfx/boss_attack.ogg";
    public static final String BOSS_BALL_EXPLOSION_SFX = "sounds/sfx/boss_fire_explosion.ogg";
    public static final String DEATH_HEAD_CUT_SFX = "sounds/sfx/headboss.ogg";
    public static final String FRIEND_DEATH_SFX = "sounds/sfx/friend_death.ogg";
    public static final String EXPLOSION_ENEMY_SFX = "sounds/sfx/explosion_enemies.ogg";
    //public static final String JUMP_SFX = "sounds/sfx/jump.ogg";

    /**----------- preferences ------*/
    public static final String PREFERENCES_NAME = "pandipaneprefs";
    public static final String HELL_HIGHSCORE_PREFS = "hellhighscore";
    public static final String CEMETERY_HIGHSCORE_PREFS = "cemeteryhighscore";
    public static final String MUSIC_VOLUME_PREFS = "musicvolume";
    public static final String SFX_VOLUME_PREFS = "sfxvolume";
    public static final String VIBRATIONS_PREFS = "vibrations";
    public static final String SCREENSHAKE_PREFS = "screenshake";
    public static final String LANGUAGE_PREFS = "language";
    public static final String ENEMIES_KILLED_PREFS = "enemies_killed";
    public static final String FRIENDS_KILLED_PREFS = "friends_killed";
    public static final String FRIENDS_SAVED_PREFS = "friends_saved";
    public static final String PLATFORMS_JUMPED_PREFS = "platforms_jumped";
    public static final String TIME_PLAYED_PREFS = "time_played";
    public static final String BOSS_KILLED_PREFS = "boss_killed";
    public static final String HELL_LOCAL_PREF = "hell_";
    public static final String CEMETERY_LOCAL_PREF = "cemetery_";
    public static final String TUTORIAL_DIALOG_PREF = "tutorial_dialog";
    public static final String CEMETERY_UNLOCKED_DIALOG_PREF = "cemetery_unlocked_dialog";
    public static float MUSIC_VOLUME_DEF = 0.5f, SFX_VOLUME_DEF = 1.f;
    public static final String PLAYERS_SKIN_PREF = "players_skin";
    public static final String GOLD_PREF = "gold";
    public static final String LAST_GOLD_VIDEO_PREF = "last_gold_video";
    public static final String MISSIONS_PREFS = "missions_completed";
    public static final String ACTIVE_MISSION_PREFS = "active_mission";

    /**-------nomi livelli------*/
    public static final String HELL_NAME = "Down to Hell";
    public static final String CEMETERY_NAME = "Run from Death";
}
