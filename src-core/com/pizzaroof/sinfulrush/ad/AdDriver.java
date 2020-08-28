package com.pizzaroof.sinfulrush.ad;

/**interfaccia che consente di richiamare da libgdx alcune funzionalità offerta da admob*/
public interface AdDriver {

    /**permette di visualizzare un interstitial, se presente*/
    void showInterstitial();

    /**fa partire un rewarded video*/
    void startRewardedVideo();
}
