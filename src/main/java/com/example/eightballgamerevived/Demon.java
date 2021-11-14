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
public class Demon extends Rectangle implements MobListener {

    private double movementAnchorX = getX();
    private double movementAnchorY = getY();
    private Timeline attackTL;
    private int direction = 1;
    private int moveDistance = 150;
    private double directionDistanceRemaining = moveDistance;
    private double speed = 2.5;
    private int aggroDistance = 500;

    //Flags
    private boolean aggro = false;

    public Demon() {
        super(30, 50, Color.BLACK);
    }

    @Override
    public void attack(Hero h) {
        double distance = Math.sqrt(Math.pow((h.getCenterX() - getX()),2) + Math.pow((h.getCenterY() - getY()),2));
        if((distance < aggroDistance) && !h.getDamageCooldown()){
            setAggroOn();
            attackTL = new Timeline(new KeyFrame(Duration.millis(500), new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    aggroMove(h);

                }
            }));
            attackTL.setCycleCount(1);
            attackTL.play();
        }
        else this.setAggroOff();
    }

    public void aggroMove(Hero h){
        if(!h.getDamageCooldown()){
            if(h.getCenterY() < getY())
                setY(getY() - speed);
            else if(h.getCenterY() > getY())
                setY(getY() + speed);
            if(h.getCenterX() < getX())
                setX(getX() - speed);
            else if(h.getCenterX() > getX())
                setX(getX() + speed);
            checkWrap();
        }
    }

    private void checkWrap() {
        double edgeDistX = The_game.getScreenWidth() - getX(); //if <0 or > scren width, off bounds
        double edgeDistY = The_game.getScreenHeight() - getY(); //if <0 or > screen height, off bounds

        if(edgeDistX < -20) setX(-20);
        else if(edgeDistX > (The_game.getScreenWidth() + 20)) setX(The_game.getScreenWidth() + 20);

        if(edgeDistY < -20) setY(-20);
        else if(edgeDistY > (The_game.getScreenHeight() + 20)) setY(The_game.getScreenHeight() + 20);
    }

    public void move() {
        //Movement path
        if((directionDistanceRemaining == 0) && (direction == 2)){
            direction = 3;
            directionDistanceRemaining =  moveDistance;
        }
        else if((directionDistanceRemaining == 0) && (direction == 4)){
            direction = 1;
            directionDistanceRemaining = moveDistance;
        }
        else if((directionDistanceRemaining == 0)  && (direction == 3)){
            direction = 4;
            directionDistanceRemaining = moveDistance;
        }
        else if((directionDistanceRemaining == 0) && (direction == 1)){
            direction = 2;
            directionDistanceRemaining = moveDistance;
        }

        switch(getDirection()){
            case(1): setY(getY() - speed); break;
            case(2): setX(getX() + speed); break;
            case(3): setY(getY() + speed); break;
            case(4): setX(getX() - speed); break;
        }
        directionDistanceRemaining = directionDistanceRemaining - speed;
        //hero.notifyMobs();

        double edgeDistX = The_game.getScreenWidth() - getX(); //if <0 or > scren width, off bounds
        double edgeDistY = The_game.getScreenHeight() - getY(); //if <0 or > screen height, off bounds

        if(edgeDistX < -20) setX(-20);
        else if(edgeDistX > (The_game.getScreenWidth() + 20)) setX(The_game.getScreenWidth() + 20);

        if(edgeDistY < -20) setY(-20);
        else if(edgeDistY > (The_game.getScreenHeight() + 20)) setY(The_game.getScreenHeight() + 20);
    }

    //Getters and setters
    public double getAnchorX() { return movementAnchorX; }
    public void setAnchorX(double movementAnchorX) { this.movementAnchorX = movementAnchorX; }

    public double getAnchorY() { return movementAnchorY; }
    public void setAnchorY(double movementAnchorY) { this.movementAnchorY = movementAnchorY; }

    public int getDirection() { return direction; }
    public void setDirection(int direction) { this.direction = direction; }

    public double getDirectionDistanceRemaining() { return directionDistanceRemaining; }
    public void setDirectionDistanceRemaining(double directionDistanceRemaining) { this.directionDistanceRemaining = directionDistanceRemaining; }

    public int getMoveDistance() { return moveDistance; }
    public void setMoveDistance(int moveDistance) { this.moveDistance = moveDistance; }

    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }

    public boolean isAggro() { return aggro; }
    public void setAggroOff() { this.aggro = false; }
    public void setAggroOn() { this.aggro = true; }
}