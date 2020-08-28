package com.pizzaroof.sinfulrush;

import android.os.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.pizzaroof.sinfulrush.ad.AdDriver;

import java.util.Timer;
import java.util.TimerTask;

import static com.google.android.gms.ads.AdRequest.*;

public class AndroidLauncher extends AndroidApplication implements AdDriver {

	private static final String INTERSTITIAL_ID = ""; //testing id: ca-app-pub-3940256099942544/1033173712

	private static final String VIDEO_ID = ""; //testing id: ca-app-pub-3940256099942544/5224354917

	/**interstitial ad*/
	private InterstitialAd interstitialAd;

	/**rewarded video*/
	private RewardedVideoAd videoAd;

	/**handler pubblicità*/
	private AdHandler handler = new AdHandler(this);

	private NGame game;

	/**flag per ricaricare video o interstitial*/
	private boolean needToReloadVideo, needToReloadInterstitial;

	private ConnectionStateMonitorOld connectionStateMonitorOld;
	private ConnectionStateMonitor connectionStateMonitor;
	//private AndroidAudio audio; //per timescale dell'audio

	private final static long INIT_DELAY = 1000, MAX_DELAY = 60 * 1000; //sono in millisecondi
	private long delayLoadingRewardVideo, delayLoadingInterstitial;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needToReloadInterstitial = needToReloadVideo = false;
		delayLoadingRewardVideo = INIT_DELAY;
		delayLoadingInterstitial = INIT_DELAY;

		createAdObjects();
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useImmersiveMode = true;
		game = new NGame(this);

		initialize(game, config);

		if(Build.VERSION.SDK_INT < 21)
			connectionStateMonitorOld = new ConnectionStateMonitorOld(this);
		else
			connectionStateMonitor = new ConnectionStateMonitor(this);

		//this.audio = new AndroidAudio(this, config);
		//Gdx.audio = this.audio;
	}

	/*@Override
	public Audio getAudio() {
		return this.audio;
	}*/

	/**crea oggetti per la pubblicità*/
	private void createAdObjects() {
		interstitialAd = new InterstitialAd(getApplicationContext());
		interstitialAd.setAdUnitId(INTERSTITIAL_ID);
		interstitialAd.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				//quando l'interstitial ad viene chiuso, iniziamo a caricarne un altro
				loadInterstitial();
				game.onInterstitialClosed();
			}

			@Override
			public void onAdFailedToLoad(int errorCode) {
				// Code to be executed when an ad request fails.
				//0: internal error
				//1: bad request, magari l'id non è valito
				//2: no internet connection
				//3: nessun ad disponibile

				if(errorCode == ERROR_CODE_NETWORK_ERROR)
					needToReloadInterstitial = true;
				else {
					//errori ERROR_CODE_INTERNAL_ERROR, ERROR_CODE_NO_FILL
					//riproviamo dopo un po' se la situazione è migliorata
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							handler.sendEmptyMessage(AdHandler.LOAD_INTERSTITIAL);
							delayLoadingInterstitial = Math.min(2 * delayLoadingInterstitial, MAX_DELAY);
						}
					}, delayLoadingInterstitial);
				}
			}

			@Override
			public void onAdOpened() {
				// Code to be executed when the ad is displayed.
			}

			@Override
			public void onAdClicked() {
				// Code to be executed when the user clicks on an ad.
			}

			@Override
			public void onAdLeftApplication() {
				// Code to be executed when the user has left the app.
			}

			@Override
			public void onAdLoaded() {
				delayLoadingInterstitial = INIT_DELAY;
			}
		});

		videoAd = MobileAds.getRewardedVideoAdInstance(getApplicationContext());
		videoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
			@Override
			public void onRewardedVideoAdLoaded() {
				delayLoadingRewardVideo = INIT_DELAY;
				System.out.println("video loaded");
			}

			@Override
			public void onRewardedVideoAdOpened() {

			}

			@Override
			public void onRewardedVideoStarted() {

			}

			@Override
			public void onRewardedVideoAdClosed() {
				//carichiamo nuovo video quando viene chiuso
				System.out.println("REWARD VIDEO CLOSED: load a new one");
				loadRewardVideo();
				game.onRewardedVideoClosed();
			}

			@Override
			public void onRewarded(RewardItem rewardItem) {
				System.out.println("MUST GIVE REWARD!! "+(Looper.myLooper() == Looper.getMainLooper()));
				//rewarded viene chiamato sull'UI thread, quindi meglio non fare operazioni pesanti qui
				game.onRewardedVideoWatched();
			}

			@Override
			public void onRewardedVideoAdLeftApplication() {

			}

			@Override
			public void onRewardedVideoAdFailedToLoad(int errorCode) {
				//2 == network connectivity
				System.out.println("Failed to load, error: "+errorCode);
				if(errorCode == ERROR_CODE_NETWORK_ERROR) //per errori di rete abbiamo una callback che li gestisce
					needToReloadVideo = true;
				else {
					//errori ERROR_CODE_INTERNAL_ERROR, ERROR_CODE_NO_FILL
					//riproviamo dopo un po' se la situazione è migliorata
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							handler.sendEmptyMessage(AdHandler.LOAD_VIDEO_AD);
							delayLoadingRewardVideo = Math.min(delayLoadingRewardVideo * 2, MAX_DELAY);
						}
					}, delayLoadingRewardVideo);
				}
			}

			@Override
			public void onRewardedVideoCompleted() {

			}
		});

		loadInterstitial();
		loadRewardVideo();
	}

	/**handler per comunicare con il main thread (lo facciamo statico e poi gli passiamo il launcher, altrimenti potrebbe causare memory leaks. VEDI: HandlerLeak)*/
	private static class AdHandler extends Handler {
		public static final int SHOW_INTERSTITIAL = 0, SHOW_VIDEO_AD = 1, LOAD_INTERSTITIAL = 2,
								LOAD_VIDEO_AD = 3;

		AndroidLauncher launcher;

		AdHandler(AndroidLauncher launcher) {
			this.launcher = launcher;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case SHOW_INTERSTITIAL:
					if(launcher.interstitialAd != null && launcher.interstitialAd.isLoaded())
						launcher.interstitialAd.show();
					else
						launcher.game.onInterstitialClosed();
					break;
				case SHOW_VIDEO_AD:
					//System.out.println("VIDEO REQUEST: "+System.currentTimeMillis());
					if(launcher.videoAd != null && launcher.videoAd.isLoaded())
						launcher.videoAd.show();
					else
						launcher.game.onErrorPlayingVideo();
					break;
				case LOAD_INTERSTITIAL:
					launcher.loadInterstitial();
					break;
				case LOAD_VIDEO_AD:
					launcher.loadRewardVideo();
					break;
			}
		}
	}

	@Override
	public void showInterstitial() {
		//mandiamo messaggio all'handler per far visualizzare l'interstitial: non possiamo visualizzarlo subito perché non siamo sul main thread di android
		handler.sendEmptyMessage(AdHandler.SHOW_INTERSTITIAL);
	}

	@Override
	public void startRewardedVideo() {
		handler.sendEmptyMessage(AdHandler.SHOW_VIDEO_AD);
	}

	protected void loadRewardVideo() {
		videoAd.loadAd(VIDEO_ID, new Builder().build());
	}

	protected void loadInterstitial() {
		interstitialAd.loadAd(new Builder().build());
	}

	protected void onConnectedToInternet() {
		if(needToReloadVideo)
			handler.sendEmptyMessage(AdHandler.LOAD_VIDEO_AD);
		if(needToReloadInterstitial)
			handler.sendEmptyMessage(AdHandler.LOAD_INTERSTITIAL);
		needToReloadVideo = needToReloadInterstitial = false;
	}

	@Override
	public void onResume() {
		super.onResume();
		if(Build.VERSION.SDK_INT < 21)
			connectionStateMonitorOld.enable(this);
		else
			connectionStateMonitor.enable(this);
	}

	@Override
	public void onPause() {
		if(Build.VERSION.SDK_INT < 21)
			connectionStateMonitorOld.disable(this);
		else
			connectionStateMonitor.disable(this);
		super.onPause();
	}
}
