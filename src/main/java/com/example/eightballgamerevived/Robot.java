package com.example.eightballgamerevived;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 *
 * @author VinceB
 */
public class Robot extends Rectangle implements MobListener {

    private int direction = 1;
    private int aggroDistance = 500;

    private boolean shootCooldown = false;

    //Flags
    private boolean aggro = false;

    public Robot() {
        super(45, 80, Color.BLACK);
    }

    @Override
    public void attack(Hero h) {
        double distance = Math.sqrt(Math.pow((h.getCenterX() - getX()),2) + Math.pow((h.getCenterY() - getY()),2));
        if((distance < aggroDistance) && !h.getDamageCooldown()){
            aggro = true;
        }
        else aggro = false;
    }

    //Getters and setters
    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }

    public boolean isAggro() { return aggro; }
    public void setAggroOff() { aggro = false; }
    public void setAggroOn() { aggro = true; }

    public boolean getShootCooldown(){ return shootCooldown; }
    public void setShootCooldown(boolean shootCooldown){ this.shootCooldown = shootCooldown; }
}