package com.pizzaroof.sinfulrush.language;

import com.badlogic.gdx.Preferences;
import com.pizzaroof.sinfulrush.Constants;
import com.pizzaroof.sinfulrush.NGame;
import com.pizzaroof.sinfulrush.menus.ShopMenu;

import java.util.Locale;

/**manager delle lingue: restituisce testi tradotti (ovviamente testi predefiniti, non a runtime)*/
public class LanguageManager {

    protected Language actualLanguage;

    public LanguageManager(Preferences prefs) {
        try {
            String lang = prefs.getString(com.pizzaroof.sinfulrush.Constants.LANGUAGE_PREFS, Locale.getDefault().getLanguage().toUpperCase());
            actualLanguage = Language.valueOf(lang);
        } catch (IllegalArgumentException e) {
            //passata lingua non supportata...
            actualLanguage = Language.EN; //di default mettiamo inglese
        }
        prefs.putString(com.pizzaroof.sinfulrush.Constants.LANGUAGE_PREFS, actualLanguage.toString());
    }

    public void setLanguage(Language language) {
        this.actualLanguage = language;
    }

    public Language getActualLanguage() {
        return actualLanguage;
    }

    /**lingue supportate*/
    public enum Language {
        EN,
        IT,
        RO,
        MG
    }

    public enum Text {
        YES,
        NO,
        PLAY,
        SHOP,
        OF_COURSE,
        EXIT,
        RESTART,
        RESUME,
        PAUSE,
        STATS,
        WATCH_VIDEO,
        ERROR_LOADING_VIDEO,
        CHOICE_NO_TUTORIAL,
        GENERAL_CONGRATULATIONS,
        SETTINGS_TITLE,
        SETTINGS_LANGUAGE,
        SETTINGS_MUSIC_VOL,
        SETTINGS_SFX_VOL,
        SETTINGS_VIBRATIONS,
        SETTINGS_CANCEL,
        NEW_BESTSCORE_MESSAGE,
        EXIT_CONFIRMATION,
        HOW_UNLOCK_CEMETERY,
        DIALOG_TAKE_TUTORIAL,
        CEMETERY_UNLOCKED,
        TUTORIAL_TAP,
        TUTORIAL_GAME_OBJ,
        TUTORIAL_TAKE_SWORD,
        TUTORIAL_USE_SWORD,
        TUTORIAL_TAKE_SCEPTRE,
        TUTORIAL_USE_SCEPTRE,
        TUTORIAL_TAKE_BONUS,
        TUTORIAL_FRIENDS,
        TUTORIAL_FINISHED,
        TUTORIAL_COMPLETE_TEXT,
        HELL_RUN_AWAY,
        HELL_NOT_THAT_BAD,
        HELL_GOLEMS_CREATED,
        HELL_ANNOYING,
        HELL_REAL_SHOW,
        HELL_BOSS_DEATH,
        STATS_TIME_PLAYED,
        STATS_ENEMIES_KILLED,
        STATS_FRIENDS_SAVED,
        STATS_FRIENDS_KILLED,
        STATS_PLATFORMS_JUMPED,
        STATS_BESTSCORE,
        STATS_BOSS_KILLED,
        TUTORIAL_START,
        SHOP_DIALOG,
        LOADING,
        CEMETERY_MY_REIGN,
        CEMETERY_GO_BACK,
        CEMETERY_LAST_WARNING,
        CEMETERY_ON_SPAWN,
        CEMETERY_ON_DEATH,
        SUGGESTION1,
        SUGGESTION2,
        SUGGESTION3,
        SUGGESTION4,
        SUGGESTION5,
        SUGGESTION6,
        SUGGESTION7,
        SUGGESTION8,
        EXIT_CONFIRMATION_GAME,
        BUY,
        SELECT,
        SELECTED,
        BUY_CONFIRM,
        NEED_MORE_MONEY,
        WATCH_VIDEO_FOR_MONEY,
        WAIT_TIME_VIDEO,
        MISSIONS,
        YOUNG_WARRIOR_PL,
        WEREWOLF_PL,
        KNIGHT_PL,
        THIEF_PL,
        ELF_PL,
        ASSASSIN_PL,
        PLAYER_DESCR_1,
        PLAYER_DESCR_2,
        PLAYER_DESCR_3,
        PLAYER_DESCR_4,
        PLAYER_DESCR_5,
        PLAYER_DESCR_6,
    }

    /**restituisce il testo tradotto con la lingua del manager*/
    public String getText(Text text, String... prms) {
        switch (actualLanguage) {
            case EN:
                switch(text) {
                    case TUTORIAL_GAME_OBJ: return "The goal of the game is to protect the\n[#82b5e1]blue-haired guy[]\nfor as long as possible";
                    case TUTORIAL_TAP: return "Tap to kill the [#F00000]monsters[]\n(you can use multiple fingers\nat the same time)";
                    case TUTORIAL_TAKE_SWORD: return "Tap on the [#ecda18]sword[] to take it";
                    case TUTORIAL_USE_SWORD: return "Drag your finger to use the [#ecda18]sword[]";
                    case TUTORIAL_TAKE_SCEPTRE: return "Tap on the [#87e34b]sceptre[] to take it";
                    case TUTORIAL_USE_SCEPTRE: return "Drag and release your finger\nto use the [#87e34b]sceptre[]";
                    case TUTORIAL_TAKE_BONUS: return "Tap on the [#FF0000]gem[] to get a bonus";
                    case TUTORIAL_FRIENDS: return "[#FF0000]Warning!\nDO NOT[] kill your friends!";
                    case TUTORIAL_FINISHED: return "You completed the tutorial!";
                    case TUTORIAL_COMPLETE_TEXT: return "Completed";
                    case HELL_RUN_AWAY: return "You're still in time\nto run away...";
                    case HELL_NOT_THAT_BAD: return "You're not that bad...\nbut still not good enough!";
                    case HELL_GOLEMS_CREATED: return "It's boring down here...\nThat's why I created Golems!";
                    case HELL_ANNOYING: return "You're really annoying...";
                    case HELL_REAL_SHOW: return "Get ready for the real show!";
                    case HELL_BOSS_DEATH: return "You defeated me...\nBut can you run from Death?";
                    case GENERAL_CONGRATULATIONS: return "Congratulations!";
                    case NEW_BESTSCORE_MESSAGE: return "New best score!";
                    case STATS_TIME_PLAYED: return "Time played:";
                    case STATS_ENEMIES_KILLED: return "Enemies killed:";
                    case STATS_FRIENDS_SAVED: return "Friends saved:";
                    case STATS_FRIENDS_KILLED: return "Friends killed:";
                    case STATS_PLATFORMS_JUMPED: return "Jumped platforms:";
                    case STATS_BESTSCORE: return "Best score:";
                    case STATS_BOSS_KILLED: return "Bosses killed:";
                    case ERROR_LOADING_VIDEO: return "Error loading video!\nCheck your Internet connection!";
                    case WATCH_VIDEO: return "Watch video to revive  ";
                    case PAUSE: return "Pause";
                    case RESTART: return "Restart      ";
                    case EXIT: return "Exit";
                    case RESUME: return "Resume      ";
                    case SETTINGS_TITLE: return "Settings";
                    case SETTINGS_CANCEL: return "Cancel";
                    case SETTINGS_LANGUAGE: return "Language: ";
                    case SETTINGS_SFX_VOL: return "Sfx volume: ";
                    case SETTINGS_MUSIC_VOL: return "Music volume: ";
                    case SETTINGS_VIBRATIONS: return "Vibrations: ";
                    case SHOP: return "Shop";
                    case PLAY: return "Play            ";
                    case EXIT_CONFIRMATION: return "Are you sure you want to exit?";
                    case YES: return "Yes";
                    case NO: return "No";
                    case STATS: return "Stats";
                    case OF_COURSE: return "Of course";
                    case CHOICE_NO_TUTORIAL: return "No, I like losing";
                    case DIALOG_TAKE_TUTORIAL: return "Would you like to take a\nQUICK tutorial?";
                    case HOW_UNLOCK_CEMETERY: return "Score at least "+ Constants.SCORE_TO_UNLOCK_CEMETERY+" in\n\""+ com.pizzaroof.sinfulrush.Constants.HELL_NAME+"\"\nto unlock \""+ com.pizzaroof.sinfulrush.Constants.CEMETERY_NAME+"\"";
                    case CEMETERY_UNLOCKED: return "You unlocked \""+ Constants.CEMETERY_NAME+"\"";
                    case SHOP_DIALOG: return "The shop will be added soon!";
                    case LOADING: return "Loading...";
                    case TUTORIAL_START: return "Let's start with a\nquick tutorial!";
                    case CEMETERY_MY_REIGN: return "How dare you\ncome to my kingdom!?";
                    case CEMETERY_GO_BACK: return "You better go back...";
                    case CEMETERY_LAST_WARNING: return "This is gonna be my last warning!";
                    case CEMETERY_ON_SPAWN: return "I had warned you...";
                    case CEMETERY_ON_DEATH: return "You defeated me...\nbut Death never dies";
                    case SUGGESTION1: return "Tip: [#b0b0b0]hit the enemies\nas soon as they appear on screen[]";
                    case SUGGESTION2: return "Tip: [#b0b0b0]the player jumps on its own,\nyou should focus on the enemies[]";
                    case SUGGESTION3: return "Tip: [#b0b0b0]if you kill one of your friends,\nyou'll suffer a malus[]";
                    case SUGGESTION4: return "Tip: [#b0b0b0]be careful with\nthe flying enemies[]";
                    case SUGGESTION5: return "Tip: [#b0b0b0]taking a bonus\nis never a bad idea[]";
                    case SUGGESTION6: return "Tip: [#b0b0b0]when you're using a double\nsword, you can use two fingers[]";
                    case SUGGESTION7: return "Tip: [#b0b0b0]when you're using a sceptre,\nthe farthest you attack\nthe more damage you make[]";
                    case SUGGESTION8: return "Tip: [#b0b0b0]look at the top left icon\nto know the weapon you're using[]";
                    case EXIT_CONFIRMATION_GAME: return "Are you sure you want to exit?\n(you'll lose missions' progresses)";
                    case BUY: return "Buy";
                    case SELECT: return "Select";
                    case SELECTED: return "Selected";
                    case BUY_CONFIRM: return "Are you sure\nyou want to buy?";
                    case NEED_MORE_MONEY: return "Earn more money\ncompleting missions!";
                    case WATCH_VIDEO_FOR_MONEY: return NGame.GOLD_PER_VIDEO+" coins for 1 video   ";
                    case WAIT_TIME_VIDEO: return "You need to wait: "+prms[0];
                    case MISSIONS: return "Missions";
                    case YOUNG_WARRIOR_PL: return "PETER";
                    case WEREWOLF_PL: return "WEREWOLF";
                    case ELF_PL: return "ELF";
                    case THIEF_PL: return "THIEF";
                    case KNIGHT_PL: return "KNIGHT";
                    case ASSASSIN_PL: return "ASSASSIN";
                    case PLAYER_DESCR_1: return "Just a common human";
                    case PLAYER_DESCR_2: return "[#3FB837]+20%[] damage\n(swords/punches)";
                    case PLAYER_DESCR_3: return "[#3FB837]+25%[] HP";
                    case PLAYER_DESCR_4: return "[#3FB837]-10%[] speed movement";
                    case PLAYER_DESCR_5: return "[#3FB837]-45%[] damage from malus\n[#3FB837]+20%[] damage (sceptre)";
                    case PLAYER_DESCR_6: return "[#D52929]-35%[] HP\n[#3FB837]+70%[] damage (all weapons)";
                    default: return "";
                }

            case IT:
                switch (text) {
                    case TUTORIAL_GAME_OBJ: return "L'obiettivo del gioco è proteggere il\n[#82b5e1]ragazzo dai capelli blu[]\nper più tempo possibile";
                    case TUTORIAL_TAP: return "Tappa sui [#F00000]mostri[] per ucciderli\n(puoi usare più dita\ncontemporaneamente)";
                    case TUTORIAL_TAKE_SWORD: return "Tocca la [#ecda18]spada[] per prenderla";
                    case TUTORIAL_USE_SWORD: return "Trascina il dito per usare la [#ecda18]spada[]";
                    case TUTORIAL_TAKE_SCEPTRE: return "Tocca lo [#87e34b]scettro[] per prenderlo";
                    case TUTORIAL_USE_SCEPTRE: return "Trascina e rilascia per\nusare lo [#87e34b]scettro[]";
                    case TUTORIAL_TAKE_BONUS: return "Tocca la [#FF0000]gemma[] per\nricevere un bonus";
                    case TUTORIAL_FRIENDS: return "[#FF0000]Attenzione!\nNON[] uccidere i tuoi amici!";
                    case TUTORIAL_FINISHED: return "Hai completato il tutorial!";
                    case TUTORIAL_COMPLETE_TEXT: return "Completato";
                    case HELL_RUN_AWAY: return "Sei ancora in tempo per\nscappare...";
                    case HELL_NOT_THAT_BAD: return "Non male...\nma non è ancora abbastanza!";
                    case HELL_GOLEMS_CREATED: return "È noioso qui giù...\nPer questo ho creato i Golem!";
                    case HELL_ANNOYING: return "Sei veramente fastidioso...";
                    case HELL_REAL_SHOW: return "Inizia il vero spettacolo...";
                    case HELL_BOSS_DEATH: return "Mi hai sconfitto...\nMa riuscirai a scappare dalla Morte?";
                    case GENERAL_CONGRATULATIONS: return "Congratulazioni!";
                    case NEW_BESTSCORE_MESSAGE: return "Nuovo best score!";
                    case STATS_TIME_PLAYED: return "Tempo di gioco:";
                    case STATS_ENEMIES_KILLED: return "Nemici uccisi:";
                    case STATS_FRIENDS_SAVED: return "Amici salvati:";
                    case STATS_FRIENDS_KILLED: return "Amici uccisi:";
                    case STATS_PLATFORMS_JUMPED: return "Piattaforme saltate:";
                    case STATS_BESTSCORE: return "Best score:";
                    case STATS_BOSS_KILLED: return "Boss uccisi:";
                    case ERROR_LOADING_VIDEO: return "Errore nel caricamento del video!\nControlla la connessione\na Internet!";
                    case WATCH_VIDEO: return "Guarda un video    \nper rinascere    ";
                    case PAUSE: return "Pausa";
                    case RESTART: return "Ricomincia     ";
                    case EXIT: return "Esci";
                    case RESUME: return "Riprendi    ";
                    case SETTINGS_TITLE: return "Impostazioni";
                    case SETTINGS_CANCEL: return "Annulla";
                    case SETTINGS_LANGUAGE: return "Lingua: ";
                    case SETTINGS_SFX_VOL: return "Volume effetti: ";
                    case SETTINGS_MUSIC_VOL: return "Volume musica: ";
                    case SETTINGS_VIBRATIONS: return "Vibrazioni: ";
                    case SHOP: return "Negozio";
                    case PLAY: return "Gioca           ";
                    case EXIT_CONFIRMATION: return "Sei sicuro di voler uscire?";
                    case YES: return "Si";
                    case NO: return "No";
                    case STATS: return "Statistiche";
                    case OF_COURSE: return "Certamente";
                    case CHOICE_NO_TUTORIAL: return "No, mi piace perdere";
                    case DIALOG_TAKE_TUTORIAL: return "Vuoi fare un BREVE tutorial?";
                    case HOW_UNLOCK_CEMETERY: return "Fai almeno "+ com.pizzaroof.sinfulrush.Constants.SCORE_TO_UNLOCK_CEMETERY+" punti in\n\""+ com.pizzaroof.sinfulrush.Constants.HELL_NAME+"\"\nper sbloccare \""+ com.pizzaroof.sinfulrush.Constants.CEMETERY_NAME+"\"";
                    case CEMETERY_UNLOCKED: return "Hai sbloccato \""+ com.pizzaroof.sinfulrush.Constants.CEMETERY_NAME+"\"";
                    case SHOP_DIALOG: return "Il negozio sarà aggiunto presto!";
                    case LOADING: return "Caricamento...";
                    case TUTORIAL_START: return "Cominciamo con un\nbreve tutorial!";
                    case CEMETERY_MY_REIGN: return "Hai un bel coraggio\na venire nel mio regno!";
                    case CEMETERY_GO_BACK: return "Faresti meglio a tornare indietro...";
                    case CEMETERY_LAST_WARNING: return "Questo sarà il mio ultimo avvertimento!";
                    case CEMETERY_ON_SPAWN: return "Ti avevo avvertito...";
                    case CEMETERY_ON_DEATH: return "Mi hai sconfitto..\nma la Morte non muore mai";
                    case SUGGESTION1: return "Consiglio: [#b0b0b0]colpisci i nemici\nappena appaiono sullo schermo[]";
                    case SUGGESTION2: return "Consiglio: [#b0b0b0]il giocatore salta\nda solo, concentrati sui nemici[]";
                    case SUGGESTION3: return "Consiglio: [#b0b0b0]se uccidi un tuo amico\nsubirai un malus[]";
                    case SUGGESTION4: return "Consiglio: [#b0b0b0]fai attenzione\nai nemici volanti[]";
                    case SUGGESTION5: return "Consiglio: [#b0b0b0]prendere un bonus\nnon è mai una cattiva scelta[]";
                    case SUGGESTION6: return "Consiglio: [#b0b0b0]con la doppia spada\npuoi usare due dita[]";
                    case SUGGESTION7: return "Consiglio: [#b0b0b0]con lo scettro,\npiù attacchi da lontano,\npiù danni farai[]";
                    case SUGGESTION8: return "Consiglio: [#b0b0b0]guarda l'icona\nin alto a sinistra\nper sapere che arma stai usando[]";
                    case EXIT_CONFIRMATION_GAME: return "Sei sicuro di voler uscire?\n(perderai i progressi\nnelle missioni)";
                    case BUY: return "Compra";
                    case SELECT: return "Seleziona";
                    case SELECTED: return "Selezionato";
                    case BUY_CONFIRM: return "Sei sicuro di\nvolerlo comprare?";
                    case NEED_MORE_MONEY: return "Guadagna più soldi\ncompletando missioni!";
                    case WATCH_VIDEO_FOR_MONEY: return NGame.GOLD_PER_VIDEO+" monete per 1 video   ";
                    case WAIT_TIME_VIDEO: return "Devi aspettare: "+prms[0];
                    case MISSIONS: return "Missioni";
                    case YOUNG_WARRIOR_PL: return "PETER";
                    case WEREWOLF_PL: return "LUPO MANNARO";
                    case ELF_PL: return "ELFO";
                    case THIEF_PL: return "LADRO";
                    case KNIGHT_PL: return "CAVALIERE";
                    case ASSASSIN_PL: return "ASSASSINO";
                    case PLAYER_DESCR_1: return "un comune umano";
                    case PLAYER_DESCR_2: return "[#3FB837]+20%[] danni (spade/pugni)";
                    case PLAYER_DESCR_3: return "[#3FB837]+25%[] HP";
                    case PLAYER_DESCR_4: return "[#3FB837]-10%[] velocità movimento";
                    case PLAYER_DESCR_5: return "[#3FB837]-45%[] danni da malus\n[#3FB837]+20%[] danni (scettro)";
                    case PLAYER_DESCR_6: return "[#D52929]-35%[] HP\n[#3FB837]+70%[] danni (tutte le armi)";
                    default: return "";
                }

            case RO:
                switch (text) {
                    case TUTORIAL_GAME_OBJ: return "Obiectivul jocului este de a proteja\n[#82b5e1]băiatul cu părul albastru[]\n pentru cât mai mult timp";
                    case TUTORIAL_TAP: return "Apasă pe [#F00000]monștri[] pentru a-i ucide\n(poți folosi mai multe\ndegete in același timp)";
                    case TUTORIAL_TAKE_SWORD: return "Atinge [#ecda18]sabia[] pentru a lua-o";
                    case TUTORIAL_USE_SWORD: return "Mișcă degetul pentru a folosi [#ecda18]sabia[]";
                    case TUTORIAL_TAKE_SCEPTRE: return "Atinge [#87e34b]sceptrul[] pentru a-l lua";
                    case TUTORIAL_USE_SCEPTRE: return "Mișcă și ridica degetul\npentru a folosi [#87e34b]sceptrul[]";
                    case TUTORIAL_TAKE_BONUS: return "Atinge [#FF0000]piatra prețioasa[]\npentru a primi un bonus";
                    case TUTORIAL_FRIENDS: return "[#FF0000]Atențiune!\nNU[] ucide prieteni tăi!";
                    case TUTORIAL_FINISHED: return "Ai completat tutorialul!";
                    case TUTORIAL_COMPLETE_TEXT: return "Completat";
                    case HELL_RUN_AWAY: return "Ești încă in timp sa fugi...";
                    case HELL_NOT_THAT_BAD: return "Nu-i rău...\ndar nu-i încă destul!";
                    case HELL_GOLEMS_CREATED: return "E plictisitor aici...\nde aia am creat Golemii!";
                    case HELL_ANNOYING: return "Ești chiar enervant...";
                    case HELL_REAL_SHOW: return "Începe adevăratul spectacol...";
                    case HELL_BOSS_DEATH: return "M-ai învins...\ndar o sa reușești sa fugi de Moarte?";
                    case GENERAL_CONGRATULATIONS: return "Felicitări!";
                    case NEW_BESTSCORE_MESSAGE: return "Nou best score!";
                    case STATS_TIME_PLAYED: return "Timp de joacă:";
                    case STATS_ENEMIES_KILLED: return "Inamici uciși:";
                    case STATS_FRIENDS_SAVED: return "Prieteni salvați:";
                    case STATS_FRIENDS_KILLED: return "Prieteni uciși:";
                    case STATS_PLATFORMS_JUMPED: return "Platforme sărite:";
                    case STATS_BESTSCORE: return "Best score:";
                    case STATS_BOSS_KILLED: return "Boși uciși:";
                    case ERROR_LOADING_VIDEO: return "S-a verificat o eroare!\ncontrolează conexiunea internet!";
                    case WATCH_VIDEO: return "Urmărește un video    \npentru a renaște    ";
                    case PAUSE: return "Pauză";
                    case RESTART: return "Restart     ";
                    case EXIT: return "Ieșire";
                    case RESUME: return "Reluare    ";
                    case SETTINGS_TITLE: return "Setări";
                    case SETTINGS_CANCEL: return "Anulează";
                    case SETTINGS_LANGUAGE: return "Limbă: ";
                    case SETTINGS_SFX_VOL: return "Volum efecte: ";
                    case SETTINGS_MUSIC_VOL: return "Volum muzică: ";
                    case SETTINGS_VIBRATIONS: return "Vibrații: ";
                    case SHOP: return "Magazin";
                    case PLAY: return "Joacă           ";
                    case EXIT_CONFIRMATION: return "Ești sigur că vrei să ieși?";
                    case YES: return "Da";
                    case NO: return "Nu";
                    case STATS: return "Statistici";
                    case OF_COURSE: return "Sigur";
                    case CHOICE_NO_TUTORIAL: return "Nu, îmi place sa pierd";
                    case DIALOG_TAKE_TUTORIAL: return "Vrei sa faci un scurt turorial?";
                    case HOW_UNLOCK_CEMETERY: return "Fa măcar "+ com.pizzaroof.sinfulrush.Constants.SCORE_TO_UNLOCK_CEMETERY+" puncte în\n\""+ com.pizzaroof.sinfulrush.Constants.HELL_NAME+"\"\npentru a debloca\n\""+ com.pizzaroof.sinfulrush.Constants.CEMETERY_NAME+"\"";
                    case CEMETERY_UNLOCKED: return "Ai deblocat \""+ com.pizzaroof.sinfulrush.Constants.CEMETERY_NAME+"\"";
                    case SHOP_DIALOG: return "Magazinul va fi adăugat curând!";
                    case LOADING: return "Încărcare...";
                    case TUTORIAL_START: return "Să începem cu un scurt tutorial!";
                    case CEMETERY_MY_REIGN: return "Cum îndrăznești\nsă vii in regatul meu!";
                    case CEMETERY_GO_BACK: return "Ai face mai bine sa te întorci înapoi...";
                    case CEMETERY_LAST_WARNING: return "Acesta este ultimul advertisment!";
                    case CEMETERY_ON_SPAWN: return "Te-am avertizat...";
                    case CEMETERY_ON_DEATH: return "M-ai învins...\ndar Moartea nu moare nici o dată";
                    case SUGGESTION1: return "Sugestie: [#b0b0b0]lovește inamicii\ncât mai curând posibil[]";
                    case SUGGESTION2: return "Sugestie: [#b0b0b0]Jucătorul sare\nsingur, concentrează-te pe inamici[]";
                    case SUGGESTION3: return "Sugestie: [#b0b0b0]dacă ucizi un prieten\nvei suferi un malus[]";
                    case SUGGESTION4: return "Sugestie: [#b0b0b0]fii atent\nla inamicii zburători[]";
                    case SUGGESTION5: return "Sugestie: [#b0b0b0]a lua un bonus\nnu-i nici o dată o alegere rea[]";
                    case SUGGESTION6: return "Sugestie: [#b0b0b0]cu sabia dublă\ndublă poți folosi douo degete[]";
                    case SUGGESTION7: return "Sugestie: [#b0b0b0]cu sceptrul,\nmai departe ataci,\nmai multe daune faci[]";
                    case SUGGESTION8: return "Sugestie: [#b0b0b0]uită-te în colțul\nde sus in stânga\npentru a vedea ce arma folosești[]";
                    case EXIT_CONFIRMATION_GAME: return "Ești sigur că vrei să ieși?\n(o sa pierzi progresele misiunilor)";
                    case BUY: return "Cumpără";
                    case SELECT: return "Selectează";
                    case SELECTED: return "Selectat";
                    case BUY_CONFIRM: return "Ești sigur că\nvrei să cumperi?";
                    case NEED_MORE_MONEY: return "Câștiga mai mulți bani\ncompletănd misiunile!";
                    case WATCH_VIDEO_FOR_MONEY: return NGame.GOLD_PER_VIDEO+" monede pentru 1 video   ";
                    case WAIT_TIME_VIDEO: return "Trebuie să aștepți: "+prms[0];
                    case MISSIONS: return "Misiuni";
                    case YOUNG_WARRIOR_PL: return "PETER";
                    case WEREWOLF_PL: return "VÂRCOLAC";
                    case ELF_PL: return "ELF";
                    case THIEF_PL: return "HOȚ";
                    case KNIGHT_PL: return "CAVALER";
                    case ASSASSIN_PL: return "ASASIN";
                    case PLAYER_DESCR_1: return "un om comun";
                    case PLAYER_DESCR_2: return "[#3FB837]+20%[] daune (săbii/pumni)";
                    case PLAYER_DESCR_3: return "[#3FB837]+25%[] HP";
                    case PLAYER_DESCR_4: return "[#3FB837]-10%[] viteza de mișcare";
                    case PLAYER_DESCR_5: return "[#3FB837]-45%[] daune de malus\n[#3FB837]+20%[] daune (sceptru)";
                    case PLAYER_DESCR_6: return "[#D52929]-35%[] HP\n[#3FB837]+70%[] daune (toate armele)";
                    default: return "";
                }

            case MG:
                switch (text) {
                    case TUTORIAL_GAME_OBJ: return "ny tanjona amny lalao dia miaro ny\n[#82b5e1]ankizy any manana volo manga[]\nany fotoana maharitra indrindra";
                    case TUTORIAL_TAP: return "Tsindrio le [#F00000]biby goavam-be[] hamono\nazy ireo (afaka mampiasa fanondro\nbebe kokoa)";
                    case TUTORIAL_TAKE_SWORD: return "Tsindrio le [#ecda18]sabatra[] mba\nhandraisana azy";
                    case TUTORIAL_USE_SWORD: return "Sariho ny fanondronao mba\nhampiasana ny [#ecda18]sabatra[]";
                    case TUTORIAL_TAKE_SCEPTRE: return "Tsindrio le [#87e34b]tehim-panjaka[]\nmba handraisana azy";
                    case TUTORIAL_USE_SCEPTRE: return "Sariho sy esory ny fanondrondao mba\nampiasana [#87e34b]tehim-panjaka[]";
                    case TUTORIAL_TAKE_BONUS: return "Kasiho ny [#FF0000]vato[] mba\n ahazahoana bonus";
                    case TUTORIAL_FRIENDS: return "[#FF0000]Tandremo!\nAZA[] mamono ireo nanmanao!";
                    case TUTORIAL_FINISHED: return "Voafenao ny fanazavana!";
                    case TUTORIAL_COMPLETE_TEXT: return "Tapitra!";
                    case HELL_RUN_AWAY: return "Mbola manana fotoana\nhandosirana ianao...";
                    case HELL_NOT_THAT_BAD: return "Tsy ratsy...\nfa mbola tsy lafatra!";
                    case HELL_GOLEMS_CREATED: return "Mahakamo ato ambany...\nireo no antony namoroako ny Golem!";
                    case HELL_ANNOYING: return "Tena manelingelina ianao...";
                    case HELL_REAL_SHOW: return "Manomboka any tena spectacle...";
                    case HELL_BOSS_DEATH: return "Resinao aho...\nfa vitanao ve ny manafaka\nahy amin'ny fahafatesana?";
                    case GENERAL_CONGRATULATIONS: return "Arahabaina!";
                    case NEW_BESTSCORE_MESSAGE: return "Record Vaovao!";
                    case STATS_TIME_PLAYED: return "Faharetan'ny lalao:";
                    case STATS_ENEMIES_KILLED: return "Fahavalo maty:";
                    case STATS_FRIENDS_SAVED: return "Namana avotra:";
                    case STATS_FRIENDS_KILLED: return "Namana maty:";
                    case STATS_PLATFORMS_JUMPED: return "Ny isan'ny sehatra nifindranao:";
                    case STATS_BESTSCORE: return "Vokatra tsara indrindra:";
                    case STATS_BOSS_KILLED: return "Lehibehiny maty:";
                    case ERROR_LOADING_VIDEO: return "Fahadisoana ny tamin'ny\nfampidirana ny video!\nzereho ny lein!";
                    case WATCH_VIDEO: return "Zereo ny video     \nmba hanombohana indray     ";
                    case PAUSE: return "Pause";
                    case RESTART: return "Manomboka indray     ";
                    case EXIT: return "Mivoaha";
                    case RESUME: return "Manomboha indray    ";
                    case SETTINGS_TITLE: return "Paramètres:";
                    case SETTINGS_CANCEL: return "Annuler";
                    case SETTINGS_LANGUAGE: return "Langue: ";
                    case SETTINGS_SFX_VOL: return "Effet Volume: ";
                    case SETTINGS_MUSIC_VOL: return "Volume musique: ";
                    case SETTINGS_VIBRATIONS: return "Vibration: ";
                    case SHOP: return "Boutique";
                    case PLAY: return "Mlalao           ";
                    case EXIT_CONFIRMATION: return "Resy lahatra ve ianao\nfa tsy ilalao intsony?";
                    case YES: return "Oui";
                    case NO: return "No";
                    case STATS: return "Statistiques";
                    case OF_COURSE: return "Mazava ho azy";
                    case CHOICE_NO_TUTORIAL: return "Te ho resy aho";
                    case DIALOG_TAKE_TUTORIAL: return "Te hanao fanazavana KELY ianao?";
                    case HOW_UNLOCK_CEMETERY: return "Manaova "+ com.pizzaroof.sinfulrush.Constants.SCORE_TO_UNLOCK_CEMETERY+" points\n\""+ com.pizzaroof.sinfulrush.Constants.HELL_NAME+"\"\nmba amohana ny\n\""+ com.pizzaroof.sinfulrush.Constants.CEMETERY_NAME+"\"";
                    case CEMETERY_UNLOCKED: return "Voavohanao ny \""+ Constants.CEMETERY_NAME+"\"";
                    case SHOP_DIALOG: return "Afaka fotoana fohy dia ho\ntonga ny Boutique!";
                    case LOADING: return "Chargement...";
                    case TUTORIAL_START: return "Hanomboka amin'ny\nfahanazavana fohy isika!";
                    case CEMETERY_MY_REIGN: return "Tena manana courage be\nhiditra amin'ny fanjakako!";
                    case CEMETERY_GO_BACK: return "Tsara ho anao ny\nmihemotra na miverina...";
                    case CEMETERY_LAST_WARNING: return "Ity no fampitandremana\nfarany hataoko!";
                    case CEMETERY_ON_SPAWN: return "Nampitandrina anao aho...";
                    case CEMETERY_ON_DEATH: return "Resinao aho..\nfa ny maty tsy maty";
                    case SUGGESTION1: return "Torohevitra: [#b0b0b0]raha vao mipoitra eo\namn'ny ecrant ny fahavalo, dia tafiho[]";
                    case SUGGESTION2: return "Torohevitra: [#b0b0b0]Ny mpilalao\ndia mitsambikina irery, mifantoha\ntsara ianao amn'ny fahavalo[]";
                    case SUGGESTION3: return "Torohevitra: [#b0b0b0]Raha mamono ny\nnamanao ianao dia mahazo sazy[]";
                    case SUGGESTION4: return "Torohevitra: [#b0b0b0]Tandremo\nny fahavalo manidina[]";
                    case SUGGESTION5: return "Torohevitra: [#b0b0b0]Mahazo bonus\ndia safidy diso[]";
                    case SUGGESTION6: return "Torohevitra: [#b0b0b0]Afaka mampiasa\nfanondro roa ianao amn'ny\nfampiasana ny sabatra roa[]";
                    case SUGGESTION7: return "Torohevitra: [#b0b0b0]Amn'ny tehimpanjaka,\ndia tsaratsara kokoa,\n ny manafika avy alavitra[]";
                    case SUGGESTION8: return "Torohevitra: [#b0b0b0]Jereo ny button\n ery ambony ankavia mba\nahafantaranao ny\nfiadiana ampiasainao[]";
                    case EXIT_CONFIRMATION_GAME: return "Azo antoka ve fa\nte hivoaka ianao?\n(ho very ny fandrosoanao)";
                    case BUY: return "Hividy";
                    case SELECT: return "Mifidy";
                    case SELECTED: return "Voafantina";
                    case BUY_CONFIRM: return "Mahatoky ve ianao fa\nte hividy azy ianao?";
                    case NEED_MORE_MONEY: return "Afaka azonao vola bebe\nkokoa amin'ny famitana iraka";
                    case WATCH_VIDEO_FOR_MONEY: return NGame.GOLD_PER_VIDEO+" vola madinika ho an'ny video   ";
                    case WAIT_TIME_VIDEO: return "Mila miandry ianao: "+prms[0];
                    case MISSIONS: return "asa\nfitoriana";
                    case YOUNG_WARRIOR_PL: return "PETER";
                    case WEREWOLF_PL: return "AMBOADIA"; //"AMBOADIA\nOLON'VELONA";
                    case ELF_PL: return "ELF";
                    case THIEF_PL: return "MPANGALATRA";
                    case KNIGHT_PL: return "MIARAMILA";
                    case ASSASSIN_PL: return "PAMONO OLONA";
                    case PLAYER_DESCR_1: return "olona tsotra";
                    case PLAYER_DESCR_2: return "[#3FB837]+20%[] fahavoazana\n(sabatra/tontondry)";
                    case PLAYER_DESCR_3: return "[#3FB837]+25%[] HP";
                    case PLAYER_DESCR_4: return "[#3FB837]-10%[] hetsika\nhafainganam-pandeha";
                    case PLAYER_DESCR_5: return "[#3FB837]-45%[] fahavoazana\navy malus\n[#3FB837]+20%[] fahavoazana\n(tehim-panjakana)";
                    case PLAYER_DESCR_6: return "[#D52929]-35%[] HP\n[#3FB837]+70%[] fahavoazana\n(fitaovam-piadiana rehetra)";
                    default: return "";
                }
        }

        return null;
    }
}
