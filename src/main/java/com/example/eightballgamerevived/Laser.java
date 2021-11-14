package com.example.eightballgamerevived;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import javafx.scene.shape.Rectangle;

/**
 *
 * @author VinceB
 */
class Laser extends Rectangle {

    private double destinationX;
    private double destinationY;
    private double sourceX;
    private double sourceY;
    private double speed = 7;
    private int updown = 0; //1 is up, 2 is down
    private int leftright = 0; //1 is left, 2 is right

    public Laser(int l, int w, double heroX, double heroY, Robot r){
        super(l, w);
        destinationX = heroX;
        destinationY = heroY;
        sourceX = r.getX();
        sourceY = r.getY();

        if(destinationY < sourceY)
            updown = 1;
        else if(destinationY > sourceY)
            updown = 2;
        if(destinationX < sourceX)
            leftright = 1;
        else if(destinationX > sourceX)
            leftright = 2;

        if(((destinationX - sourceX) < r.getWidth()) && ((destinationX - sourceX) > 0))
            leftright = 0;

        if((destinationY - sourceY) < r.getHeight() && (destinationY - sourceY) > 0)
            updown = 0;
    }

    public void move(){
        if(updown == 1)
            setY(getY() - speed);
        else if(updown == 2)
            setY(getY() + speed);
        if(leftright == 1)
            setX(getX() - speed);
        else if(leftright == 2)
            setX(getX() + speed);
    }
}