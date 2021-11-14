package com.example.eightballgamerevived;

/*
 * June 2018.
 */

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 *
 * @author vince belanger
 */
public class Hero extends Circle{

    private List<MobListener> listeners = new ArrayList<MobListener>();
    private int score = 0;
    private String name;
    private int movementSpeed = 4;
    private int jumpMultiplier = 3;
    private int health = 3;
    private int directionCode = 0;

    //Flags
    private boolean speedShard = false;
    private boolean levelHeartPiece = false;
    private boolean damageCooldown = false;
    private boolean movementActive = false;

    public Hero(int i, int i0, int i1, Color c, String name) {
        super(i, i0, i1, c);
        setName(name);
    }

    public int getScore(){ return score; }
    public void setScore(int score){ this.score = score; }

    public String getName(){ return name; }
    public void setName(String name){ this.name = name; }

    public int getMovementSpeed(){ return movementSpeed; }
    public void setMovementSpeed(int movementSpeed){ this.movementSpeed = movementSpeed; }

    public int getJumpMultiplier(){ return jumpMultiplier; };
    public void setJumpMultiplier(int jumpMultiplier){ this.jumpMultiplier = jumpMultiplier; }

    public boolean getSpeedShard() { return speedShard; }
    public void setSpeedShard(boolean speedShard) { this.speedShard = speedShard; }

    public boolean getLevelHeartPiece() { return levelHeartPiece; }
    public void setLevelHeartPiece(boolean levelHeartPiece) { this.levelHeartPiece = levelHeartPiece; }

    public void addListener(MobListener toAdd){
        listeners.add(toAdd);
    }
    public void removeListener(MobListener toRemove){
        listeners.remove(toRemove);
    }
    public void notifyMobs() {
        for (MobListener l : listeners)
            l.attack(this);
    }

    public boolean getDamageCooldown(){ return damageCooldown; }
    public void setDamageCooldown(boolean damageCooldown){ this.damageCooldown = damageCooldown; }

    public int getHealth(){ return health; }
    public void setHealth(int health){ this.health = health; }

    public boolean isMovementActive(){ return movementActive; }
    public void setMovementActiveOn(){ this.movementActive = true; }
    public void setMovementActiveOff(){ this.movementActive = false; }

    public int getDirectionCode(){ return directionCode; }
    public void setDirectionCode(int directionCode){ this.directionCode = directionCode; }
}
