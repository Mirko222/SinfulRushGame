package com.pizzaroof.sinfulrush.util;

import com.pizzaroof.sinfulrush.attacks.Armory;

/**classe che indica i poteri di un personaggio*/
public class PlayerPower {
    private static final int DEF_MAX_HP = 100;

    private int maxHp;
    private float speedMultiplier;

    private float malusMultiplier;

    private float swordDamageMultiplier, punchDamageMultiplier, sceptreDamageMultiplier;

    public PlayerPower() {
        maxHp = DEF_MAX_HP;
        speedMultiplier = swordDamageMultiplier = punchDamageMultiplier = sceptreDamageMultiplier = malusMultiplier = 1.f;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void setSwordDamageMultiplier(float sdm) {
        swordDamageMultiplier = sdm;
    }

    public void setPunchDamageMultiplier(float sdm) {
        punchDamageMultiplier = sdm;
    }

    public void setSceptreDamageMultiplier(float sdm) {
        sceptreDamageMultiplier = sdm;
    }

    public void setMalusMultiplier(float mm) {
        malusMultiplier = mm;
    }

    public float getSceptreDamageMultiplier() {
        return sceptreDamageMultiplier;
    }

    public float getPunchDamageMultiplier() {
        return punchDamageMultiplier;
    }

    public float getSwordDamageMultiplier() {
        return swordDamageMultiplier;
    }

    public float getMalusMultiplier() {
        return malusMultiplier;
    }

    public float getAttackMultiplier(Armory armory) {
        if(armory.isUsingGlove() || armory.isUsingPunch()) return getPunchDamageMultiplier();
        if(armory.isUsingSword()) return getSwordDamageMultiplier();
        if(armory.isUsingSceptre()) return getSceptreDamageMultiplier();
        return 1.f;
    }
}
