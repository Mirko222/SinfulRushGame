package com.pizzaroof.sinfulrush;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.brashmonkey.spriter.gdx.SpriterData;
import com.brashmonkey.spriter.gdx.SpriterDataLoader;
import com.pizzaroof.sinfulrush.actors.physics.World2D;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitter;
import com.pizzaroof.sinfulrush.actors.physics.particles.PhysicParticleEmitterLoader;
import com.pizzaroof.sinfulrush.ad.AdDriver;
import com.pizzaroof.sinfulrush.language.LanguageManager;
import com.pizzaroof.sinfulrush.missions.Mission;
import com.pizzaroof.sinfulrush.missions.MissionManager;
import com.pizzaroof.sinfulrush.screens.AdGameplayScreen;
import com.pizzaroof.sinfulrush.screens.LoadingScreen;
import com.pizzaroof.sinfulrush.audio.SoundManager;
import com.pizzaroof.sinfulrush.screens.MainMenuScreen;
import com.pizzaroof.sinfulrush.util.Utils;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkin;
import com.pizzaroof.sinfulrush.util.assets.FreeTypeSkinLoader;
import com.pizzaroof.sinfulrush.util.pools.Pools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


/**classe principale del gioco*/
public class NGame extends Game {

	/**asset manager di tutto il gioco: questo contiene tutti i media di cui abbiamo bisogno*/
	protected AssetManager assetManager;

	/**mondo 2d: lo mettiamo in game perchè non vogliamo ricrearlo ogni volta che si va nel gameplay*/
	protected World2D world2d;

	/**manager dei suoni*/
	protected SoundManager soundManager;

	/**driver che consente di accedere a funzionalità di pubblicità*/
	protected AdDriver adDriver;

	/**file di preferenze di gioco (contiene salvataggi di gioco e delle impostazioni)*/
	protected Preferences preferences;

	/**manager usato per prendere testi tradotti*/
	protected LanguageManager languageManager;

	/**in questo momento c'è un ad mostrato?*/
	protected boolean showingAd;

	/**quanto oro per video?*/
	public static final int GOLD_PER_VIDEO = 300; //300

	/**oro del giocatore*/
	protected int gold;

	private MissionManager missionManager;

	public NGame(AdDriver adDriver) {
		super();
		this.adDriver = adDriver;
		showingAd = false;
	}


	@Override
	public void create () { //chiamata alla creazione del gioco
		ShaderProgram.pedantic = false; //possiamo fare shaders senza attributi e/o uniforms

		Pools.cleanAllPools(); //android preserva variabili statiche... puliamo tutto all'inizio

		preferences = Gdx.app.getPreferences(Constants.PREFERENCES_NAME);

		gold = preferences.getInteger(Constants.GOLD_PREF, 0);

		languageManager = new LanguageManager(preferences);

		assetManager = new AssetManager();
		//setta loader per caricare animazioni spriter
		assetManager.setLoader(SpriterData.class, new SpriterDataLoader(new InternalFileHandleResolver()));
		//loader per physics particle emitter
		assetManager.setLoader(PhysicParticleEmitter.class, new PhysicParticleEmitterLoader(new InternalFileHandleResolver()));
		assetManager.setLoader(FreeTypeSkin.class, new FreeTypeSkinLoader(new InternalFileHandleResolver()));

		world2d = new World2D(Constants.PIXELS_PER_METER, Constants.GRAVITY_VECTOR);
		soundManager = new SoundManager(assetManager, preferences.getFloat(Constants.SFX_VOLUME_PREFS, Constants.SFX_VOLUME_DEF),
							preferences.getFloat(Constants.MUSIC_VOLUME_PREFS, Constants.MUSIC_VOLUME_DEF));

		missionManager = new MissionManager(this, getPreferences());

		setScreen(new LoadingScreen(this));
		//setScreen(new TestingScreen(this));
	}

	@Override
	public void dispose () { //chiamata alla fine del gioco
		super.dispose();

		preferences.flush();

		assetManager.finishLoading();//se si chiude mentre sta ancora caricando, crasha... quindi facciamogli caricare tutto prima di chiudere

		assetManager.dispose(); //rilasciamo risorse dell'asset manager alla fine del gioco: NON FARLO IN ALTRI PUNTI DEL GIOCO
		world2d.dispose();
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}

	public World2D getWorld2d() {
		return world2d;
	}

	private AdDriver getAdDriver() {
		return adDriver;
	}

	public SoundManager getSoundManager() {
		return soundManager;
	}

	public LanguageManager getLanguageManager() {
		return languageManager;
	}

	public ArrayList<Mission> getActiveMissions() {
		return missionManager.getActiveMissions();
	}

	public ArrayList<Mission> getCompletedMissions() {
		return missionManager.getCompletedMissions();
	}

	public MissionManager getMissionManager() {
		return missionManager;
	}

	/**chiamato quando l'utente guarda il rewarded video*/
	public void onRewardedVideoWatched() {
		if(getScreen() instanceof AdGameplayScreen) //limitiamoci a riattivare il livello: poi nel thread di libgdx facciamo la roba pesante
			((AdGameplayScreen) getScreen()).setReactivateLevel(true);
		else if(getScreen() instanceof MainMenuScreen)
			((MainMenuScreen)getScreen()).increaseGold(GOLD_PER_VIDEO);
	}

	@Override
	public void setScreen(Screen screen) {
		super.setScreen(screen);
	}

	public void onErrorPlayingVideo() {
        hidingAd(); //NB: confidiamo nel fatto che non mettiamo mai un video e un interstitial insieme/a brevissima distanza
		if(getScreen() instanceof AdGameplayScreen)
			((AdGameplayScreen)getScreen()).onErrorPlayingVideo();
		else if(getScreen() instanceof MainMenuScreen)
			((MainMenuScreen)getScreen()).onErrorPlayingVideo();
	}

	public void showInterstitial() {
		showingAd();
		getAdDriver().showInterstitial();
	}

	public void startRewardedVideo() {
		showingAd();
		getAdDriver().startRewardedVideo();
	}

	public void onInterstitialClosed() {
		hidingAd(); //NB: confidiamo nel fatto che non mettiamo mai un video e un interstitial insieme/a brevissima distanza
	}

	public void onRewardedVideoClosed() {
		hidingAd(); //NB: confidiamo nel fatto che non mettiamo mai un video e un interstitial insieme/a brevissima distanza
	}

	/**restituisce quanto oro ha il giocatore*/
	public int getGold() {
		return gold;
	}

	/**aumenta/decrementa oro totale*/
	public void addGold(int amount) {
		gold += amount;
	}

	private void showingAd() {
		showingAd = true;
	}

	private void hidingAd() {
		showingAd = false;
	}

	public boolean isShowingAd() {
		return showingAd;
	}

	public Preferences getPreferences() {
		return preferences;
	}
}
