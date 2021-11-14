package com.example.eightballgamerevived;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author vincebel7
 */
public class The_game extends Application {
    //Movement codes: 1-UP, 2-RIGHT, 3-DOWN, 4-LEFT
    private static final int        SCREEN_WIDTH = 1600;
    private static final int        SCREEN_HEIGHT = 900;
    private static final int        LAST_LEVEL_INDEX_WITH_BG = 14;

    public static int getScreenHeight(){ return SCREEN_HEIGHT; }
    public static int getScreenWidth(){ return SCREEN_WIDTH; }

    private boolean robotInLevel;
    private boolean launching = true;
    private boolean heartPieceInLevel = false;
    private int bgcounter;
    private int lasercounter = 0;
    private int gameovercounter;

    private Map<Integer,Circle> coinMap;
    private Map<Integer, Demon> demonMap;
    private Map<Integer, Worm> wormMap;
    private Map<Integer, Laser> laserMap = new HashMap<>();
    private Stack heartStack = new Stack();

    private double coinCount;
    private int demonCount;
    private int wormCount;
    private int robotCount;
    private int levelCount = 0;

    private Hero hero;
    private Polygon speedShard;
    private Circle heartPiece;
    private Robot testRobot;

    private Text levelText;
    private Text scoreText;
    private Text shardText;
    private Text introMsg;
    private Text goText;

    private Image levelImg;
    private GridPane gridpane;
    private GridPane healthpane;
    private Group group;
    private Stage stage;
    private Scene scene;

    private Timeline introTL;

    private final Timeline moveTL = new Timeline(new KeyFrame(Duration.millis(10), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if(!hero.isMovementActive()) moveTL.stop();

            switch(hero.getDirectionCode()){
                case(1): hero.setCenterY(hero.getCenterY() - hero.getMovementSpeed()); break;
                case(2): hero.setCenterX(hero.getCenterX() + hero.getMovementSpeed()); break;
                case(3): hero.setCenterY(hero.getCenterY() + hero.getMovementSpeed()); break;
                case(4): hero.setCenterX(hero.getCenterX() - hero.getMovementSpeed()); break;
                default: break;
            }
            collectClosestCoin();
            checkWrap();
            if(!hero.getDamageCooldown()){
                hero.notifyMobs();

                for(int i = 0; i < demonCount; i++){
                    Demon d = demonMap.get(i);
                    rectangleMonsterDamage(d);
                }
                for(int i = 0; i < wormCount; i++){
                    Worm w = wormMap.get(i);
                    rectangleMonsterDamage(w);
                }

                for(int i = 0; i < laserMap.size(); i++){
                    Laser l = laserMap.get(i);
                    rectangleMonsterDamage(l);
                }

                if(robotInLevel){
                    rectangleMonsterDamage(testRobot);

                    if(testRobot.isAggro() && !testRobot.getShootCooldown()){
                        Image robotImg = new Image("robotAggro.png");
                        testRobot.setFill(new ImagePattern(robotImg));
                        laserShotTL.setCycleCount(3);
                        laserShotTL.play();
                        testRobot.setShootCooldown(true);
                        laserTL.setCycleCount(1);
                        laserTL.play();
                    }
                    if(testRobot.isAggro()) {
                        Image robotImg = new Image("robotAggro.png");
                        testRobot.setFill(new ImagePattern(robotImg));
                    }
                    else{
                        Image robotImg = new Image("robot.png");
                        testRobot.setFill(new ImagePattern(robotImg));
                    }
                }
            }
        }
    }));

    private final Timeline laserTL = new Timeline(new KeyFrame(Duration.millis(2000), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            testRobot.setShootCooldown(false);
        }
    }));

    private final Timeline laserShotTL = new Timeline(new KeyFrame(Duration.millis(400), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) { shootLaser(testRobot); }}));

    private final Timeline colorChange = new Timeline(new KeyFrame(Duration.millis(75), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            if(hero.getHealth() == 0){
                levelImg = new Image("deathbg.png");
                scene.setFill(new ImagePattern(levelImg));
                colorChange.stop();
            }
            else if(bgcounter == 0){
                scene.setFill(Color.RED);
                bgcounter = 1;
            }
            else{
                scene.setFill(new ImagePattern(levelImg));
                bgcounter = 0;
            }
        }
    }));

    private final Timeline damageCooldown = new Timeline(new KeyFrame(Duration.millis(500), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            hero.setDamageCooldown(false);
        }
    }));

    private final Timeline mobMover = new Timeline(new KeyFrame(Duration.millis(25), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            for(int i = 0; i < demonCount; i++){
                Demon d = demonMap.get(i);
                if(!d.isAggro()) d.move();
            }

            for(int i = 0; i < laserMap.size(); i++){
                laserMap.get(i).move();
            }
        }
    }));

    private final Timeline heroShrink = new Timeline(new KeyFrame(Duration.millis(35), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            hero.setRadius(hero.getRadius() - 0.5);
            if((hero.getHealth() > 0) || (hero.getRadius() == 0)) heroShrink.stop();
        }
    }));

    private final Timeline gameOverText = new Timeline(new KeyFrame(Duration.millis(500), new EventHandler<ActionEvent>(){
        @Override
        public void handle(ActionEvent event){

            gridpane.getChildren().remove(goText);
            goText = new Text("GAME OVER");
            goText.setFont(Font.font("Monospaced", FontWeight.NORMAL, 40));

            if(gameovercounter == 1){
                goText.setFill(Color.WHITE);
                gameovercounter = 2;
            }
            else{
                goText.setFill(Color.BLACK);
                gameovercounter = 1;
            }
            gridpane.add(goText, 0, 4);
        }
    }));

    private void moveHero() { //1 is UP, 2 is RIGHT, 3 is DOWN, 4 is LEFT
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override public void handle(KeyEvent event) {
                if((null != event.getCode()) && (hero.getHealth() > 0)){
                    switch (event.getCode()) {
                        case W:
                            hero.setDirectionCode(1);

                            moveTL.setCycleCount(Timeline.INDEFINITE);
                            moveTL.play();
                            break;
                        case S:
                            hero.setDirectionCode(3);

                            moveTL.setCycleCount(Timeline.INDEFINITE);
                            moveTL.play();
                            break;
                        case A:
                            hero.setDirectionCode(4);

                            moveTL.setCycleCount(Timeline.INDEFINITE);
                            moveTL.play();
                            break;
                        case D:
                            hero.setDirectionCode(2);

                            moveTL.setCycleCount(Timeline.INDEFINITE);
                            moveTL.play();
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void checkWrap() {
        double edgeDistX = SCREEN_WIDTH - hero.getCenterX(); //if <0 or > scren width, off bounds
        double edgeDistY = SCREEN_HEIGHT - hero.getCenterY(); //if <0 or > screen height, off bounds

        if(edgeDistX < -20) hero.setCenterX(-20);
        else if(edgeDistX > (SCREEN_WIDTH + 20)) hero.setCenterX(SCREEN_WIDTH + 20);

        if(edgeDistY < -20) hero.setCenterY(-20);
        else if(edgeDistY > (SCREEN_HEIGHT + 20)) hero.setCenterY(SCREEN_HEIGHT + 20);
    }

    private void collectClosestCoin(){
        double firstCenterX = hero.getCenterX(); double firstCenterY = hero.getCenterY();
        double secondCenterX; double secondCenterY;
        double distance;

        for(int i = 0; i < coinCount; i++){
            if(coinMap.containsKey(i)){
                secondCenterX = coinMap.get(i).getCenterX();
                secondCenterY = coinMap.get(i).getCenterY();
                distance = Math.sqrt(Math.pow((secondCenterX - firstCenterX),2) + Math.pow((secondCenterY - firstCenterY),2));

                if(distance < hero.getRadius()){
                    group.getChildren().remove(coinMap.get(i));
                    coinMap.remove(i);

                    hero.setScore(hero.getScore() + 1);
                    group.getChildren().remove(gridpane);
                    gridpane.getChildren().remove(scoreText);
                    scoreText = new Text("Score: " + hero.getScore());
                    scoreText.setFont(Font.font("Monospaced", FontWeight.NORMAL, 20));
                    scoreText.setFill(Color.WHITE);
                    gridpane.add(scoreText, 0, 1);
                    group.getChildren().add(gridpane);

                    if(coinMap.isEmpty()) newScreen();
                    return;
                }
            }
        }
        speedShard();
        if(heartPieceInLevel) heartPiece();
    }

    private void speedShard(){
        if(!hero.getSpeedShard()){
            ObservableList<Double> shardPoints = speedShard.getPoints();
            double xPos, yPos, shardDistance;

            for(int i = 0; i < 5; i++){
                xPos = shardPoints.get(i);
                yPos = shardPoints.get(++i);
                shardDistance = Math.sqrt(Math.pow((xPos - hero.getCenterX()),2) + Math.pow((yPos - hero.getCenterY()),2));

                if(shardDistance < hero.getRadius()){
                    hero.setSpeedShard(true);
                    group.getChildren().remove(speedShard);
                    hero.setMovementSpeed(hero.getMovementSpeed() + 2);

                    group.getChildren().remove(gridpane);
                    shardText = new Text("SPEED SHARD ACTIVE");
                    shardText.setFont(Font.font("Monospaced", FontWeight.NORMAL, 20));
                    shardText.setFill(Color.FORESTGREEN);
                    gridpane.add(shardText, 0, 3);
                    group.getChildren().add(gridpane);
                    break;
                }
            }
        }
    }

    private void heartPiece(){
        double distance = Math.sqrt(Math.pow((heartPiece.getCenterX() - hero.getCenterX()),2) + Math.pow((heartPiece.getCenterY() - hero.getCenterY()),2));
        if(!hero.getLevelHeartPiece()){
            if(distance < hero.getRadius()){
                hero.setHealth(hero.getHealth() + 1);
                group.getChildren().remove(heartPiece);
                hero.setLevelHeartPiece(true);

                Rectangle heart = new Rectangle(25, 25);
                Image heartImg = new Image("heart_icon.png");
                heart.setFill(new ImagePattern(heartImg));

                heartStack.push(heart);
                healthpane.add(heart, hero.getHealth() + 1, 0);
            }
        }
    }

    private void rectangleMonsterDamage(Rectangle d){
        double firstCenterX = hero.getCenterX(), firstCenterY = hero.getCenterY();
        double pointX = d.getX(), pointY = d.getY();
        double radius = hero.getRadius(), newDistance;
        double[] points = {pointX, pointY,
                (pointX + d.getWidth()), pointY,
                pointX, (pointY + d.getHeight()),
                (pointX + d.getWidth()), (pointY + d.getHeight())
        };

        for(int i = 0; i < 8; i++){
            newDistance = Math.sqrt(Math.pow((points[i++] - firstCenterX),2) + Math.pow((points[i] - firstCenterY),2)) - radius;

            if(newDistance < 0){
                Object h = heartStack.pop();
                healthpane.getChildren().remove(h);

                hero.setHealth(hero.getHealth() - 1);

                bgcounter = 0;

                colorChange.setCycleCount(2);
                colorChange.play();
                if(hero.getHealth() == 0){
                    hero.setMovementActiveOff();

                    introTL.stop();
                    gridpane.getChildren().remove(introMsg);
                    gameOverText.setCycleCount(Timeline.INDEFINITE);
                    gameOverText.play();

                    heroShrink.setCycleCount((int) (hero.getRadius() * 2) + 1);
                    heroShrink.play();

                }
                hero.setDamageCooldown(true);

                damageCooldown.setCycleCount(1);
                damageCooldown.play();
                break;
            }
        }
    }

    public void shootLaser(Robot r) {
        Laser laser;
        Image laserImg = new Image("laser.png");
        laser = new Laser(7, 7, hero.getCenterX(), hero.getCenterY(), r);
        laser.setFill(new ImagePattern(laserImg));

        laser.setX(r.getX() + (r.getWidth()/2));
        laser.setY(r.getY() + 3);

        laserMap.put(lasercounter, laser);
        group.getChildren().add(laser);

        lasercounter++;
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        GridPane grid = new GridPane();

        Text title = new Text("8 Ball Afterlife - v0.2");
        title.setTextAlignment(TextAlignment.CENTER);
        grid.add(title, 0, 0, 2, 1);

        Label usernameLabel = new Label("Your name: ");
        grid.add(usernameLabel, 0, 1);

        TextField userTextField = new TextField();
        grid.add(userTextField, 1, 1);

        Button btn = new Button("Play");

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);

        final Text actiontarget = new Text();
        grid.add(actiontarget, 1, 6);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String name = userTextField.getText();
                gameSetup(name);
            }
        });

        Scene introScene = new Scene(grid, 1700, 900, Color.CADETBLUE);
        stage.setScene(introScene);
        stage.show();
    }

    private void gameSetup(String name){
        if(levelCount > 0) mobMover.stop();
        hero = new Hero(300,300,30,Color.RED, name);

        Image heroImg = new Image("hero.png");
        hero.setFill(new ImagePattern(heroImg));

        group = new Group(hero);
        scene = new Scene(group, SCREEN_WIDTH, SCREEN_HEIGHT);

        hero.setMovementActiveOff();
        coinMap = new HashMap<>();
        coinCount = 0;
        levelCount = 0;
        gridpane = new GridPane();
        gridpane.setVgap(5);
        healthpane = new GridPane();
        healthpane.setHgap(3);
        healthpane.setAlignment(Pos.TOP_RIGHT);
        hero.setHealth(3);

        for(int i = 0; i < hero.getHealth(); i++){
            Rectangle heart = new Rectangle(25, 25);
            Image heartImg = new Image("heart_icon.png");
            heart.setFill(new ImagePattern(heartImg));
            heartStack.push(heart);
            healthpane.add(heart, i + 1, 0);
        }
        group.getChildren().add(healthpane);

        System.out.println("Hero name: " + hero.getName());
        newScreen();
    }

    public void newScreen(){
        //Remove old assets from group and gridpane
        if(!launching) moveTL.stop();
        hero.setMovementActiveOff();
        gameOverText.stop();

        hero.setLevelHeartPiece(false);
        heartPieceInLevel = false;

        for(int i = 0; i < demonCount; i++){
            Demon d = demonMap.get(i);
            group.getChildren().remove(d);
            hero.removeListener(d);
            demonMap.remove(i);
        }
        for(int i = 0; i < wormCount; i++){
            Worm w = wormMap.get(i);
            group.getChildren().remove(w);
            hero.removeListener(w);
            wormMap.remove(i);
        }
        if(laserMap.size() > 0){
            for(int i = 0; i < laserMap.size(); i++){
                group.getChildren().remove(laserMap.get(i));
            }
            laserMap.clear();
            lasercounter = 0;
        }

        group.getChildren().remove(testRobot);
        group.getChildren().remove(speedShard);
        group.getChildren().remove(heartPiece);
        group.getChildren().remove(gridpane);
        gridpane.getChildren().remove(levelText);
        gridpane.getChildren().remove(scoreText);
        gridpane.getChildren().remove(goText);
        gridpane.getChildren().remove(introMsg);

        //Change level+score Texts, recreate gridpane, add gridpane back to group
        levelCount++;
        levelText = new Text("Level " + levelCount);
        levelText.setFont(Font.font("Monospaced", FontWeight.NORMAL, 20));
        levelText.setFill(Color.WHITE);
        gridpane.add(levelText, 0, 0);
        scoreText = new Text("Score: " + hero.getScore());
        scoreText.setFont(Font.font("Monospaced", FontWeight.NORMAL, 20));
        scoreText.setFill(Color.WHITE);
        gridpane.add(scoreText, 0, 1);
        Button btn = new Button("Restart");

        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBtn.getChildren().add(btn);
        gridpane.add(hbBtn, 0, 2);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                gameSetup(hero.getName());
            }
        });

        group.getChildren().add(gridpane);
        System.out.println("--New screen--");

        //Mob counts
        //Demon
        if(levelCount == 1) demonCount = 0;
        else if((levelCount > 1) && (levelCount < 10)) demonCount = 1;
        else demonCount = 2;

        //Worm
        if(levelCount == 1) wormCount = 1;
        else if((levelCount > 1) && (levelCount < 8)) wormCount = 2;
        else wormCount = 3;

        //Robot
        if((Math.random()*10) < 3){
            robotInLevel = true;
            robotCount = 1;
        }
        else {
            robotInLevel = false;
            robotCount = 0;
        }

        //Hero positioning
        hero.setCenterX((Math.random() * 700)+100);
        hero.setCenterY((Math.random() * 700)+100);

        //Coin generation
        coinCount = Math.random()*(10-5)+5;
        Circle coin;
        int xPos, yPos;
        for(int i = 0; i < coinCount; i++){
            xPos = (int) (Math.random()*(SCREEN_WIDTH - 100) + 100);
            yPos = (int) (Math.random()*SCREEN_HEIGHT - 60) + 60;

            coin = new Circle(xPos, yPos, 10, Color.GREY);
            Image coinImg = new Image("coin.png");
            coin.setFill(new ImagePattern(coinImg));
            coinMap.put(i, coin);
            group.getChildren().add(coin);
        }
        System.out.println((int) Math.ceil(coinCount) + " coins spawned"); //DEBUG

        //Speed Shard generation
        //if(Math.random() < 0.3){
        if(true && (!hero.getSpeedShard())){
            speedShard = new Polygon();
            int sideLength = 30;
            double randomPointX = Math.random()*SCREEN_WIDTH;
            double randomPointY = Math.random()*SCREEN_HEIGHT;
            while((Math.abs(hero.getCenterX() - randomPointX) < 100) && (Math.abs(hero.getCenterY() - randomPointY) < 100)){
                randomPointX = Math.random()*SCREEN_WIDTH;
                randomPointY = Math.random()*SCREEN_HEIGHT;
            }

            double secondPointX = randomPointX + (sideLength/2);
            double secondPointY = randomPointY - Math.sqrt(Math.pow(sideLength, 2) - Math.pow(sideLength/2, 2));
            double thirdPointX = randomPointX + sideLength;
            double thirdPointY = randomPointY;

            speedShard.getPoints().addAll(new Double[]{
                    randomPointX, randomPointY,
                    secondPointX, secondPointY,
                    thirdPointX, thirdPointY,
            });

            Image shardImg = new Image("shard.png");

            speedShard.setFill(new ImagePattern(shardImg));
            group.getChildren().add(speedShard);


            shardText = new Text(" ");
            shardText.setFont(Font.font("Monospaced", FontWeight.NORMAL, 20));
            shardText.setFill(Color.FORESTGREEN);
            gridpane.add(shardText, 0, 3);
        }

        //Heart piece generation
        if((Math.random()*100) < 20){
            heartPieceInLevel = true;
            //if(true){
            double randomPointX = Math.random()*SCREEN_WIDTH;
            double randomPointY = Math.random()*SCREEN_HEIGHT;
            while((Math.abs(hero.getCenterX() - randomPointX) < 100) && (Math.abs(hero.getCenterY() - randomPointY) < 100)){
                randomPointX = Math.random()*SCREEN_WIDTH;
                randomPointY = Math.random()*SCREEN_HEIGHT;
            }

            heartPiece = new Circle(randomPointX, randomPointY, 15, Color.RED);

            Image heartImg = new Image("heart.png");
            heartPiece.setFill(new ImagePattern(heartImg));

            group.getChildren().add(heartPiece);
        }

        //Background color or image
        levelImg = new Image("level" + (levelCount - ((levelCount / 15 ) * LAST_LEVEL_INDEX_WITH_BG)) + ".png");
        scene.setFill(new ImagePattern(levelImg));

        //Generate demons
        demonMap = new HashMap<>();

        Demon d;
        Image demonImg = new Image("demon1.png");
        for(int i = 0; i < demonCount; i++){
            d = new Demon();
            double xPosD = Math.random()*(SCREEN_WIDTH - 200) + 100;
            double yPosD = Math.random()*(SCREEN_HEIGHT - 200) + 100;
            while((Math.abs(hero.getCenterX() - xPosD) < 100) && (Math.abs(hero.getCenterY() - yPosD) < 100)){
                xPosD = Math.random()*SCREEN_WIDTH;
                yPosD = Math.random()*SCREEN_HEIGHT;
            }
            d.setX(xPosD);
            d.setY(yPosD);
            d.setAnchorX(d.getX()); //Set movement anchor
            d.setAnchorY(d.getY()); //Set movement anchor

            d.setFill(new ImagePattern(demonImg));

            group.getChildren().add(d);
            hero.addListener(d);
            demonMap.put(i, d);
        }

        //Generate robot
        Image robotImg = new Image("robot.png");
        for(int i = 0; i < robotCount; i++){
            testRobot = new Robot();
            double xPosD = Math.random()*(SCREEN_WIDTH - 200) + 100;
            double yPosD = Math.random()*(SCREEN_HEIGHT - 200) + 100;
            while((Math.abs(hero.getCenterX() - xPosD) < 100) && (Math.abs(hero.getCenterY() - yPosD) < 100)){
                xPosD = Math.random()*SCREEN_WIDTH;
                yPosD = Math.random()*SCREEN_HEIGHT;
            }
            testRobot.setX(xPosD);
            testRobot.setY(yPosD);

            testRobot.setFill(new ImagePattern(robotImg));

            group.getChildren().add(testRobot);
            hero.addListener(testRobot);

            laserMap = new HashMap<>();
        }


        //Generate worms
        wormMap = new HashMap<>();

        Worm w;
        Image wormImg = new Image("worm.png");
        double xPosW, yPosW;
        for(int i = 0; i < wormCount; i++){
            w = new Worm();

            xPosW = Math.random()*(SCREEN_WIDTH - 200) + 100;
            yPosW = Math.random()*(SCREEN_HEIGHT - 200) + 100;
            while((Math.abs(hero.getCenterX() - xPosW) < 100) && (Math.abs(hero.getCenterY() - yPosW) < 100)){
                xPosW = Math.random()*SCREEN_WIDTH;
                yPosW = Math.random()*SCREEN_HEIGHT;
            }
            w.setX(xPosW);
            w.setY(yPosW);

            w.setFill(new ImagePattern(wormImg));

            group.getChildren().add(w);
            hero.addListener(w);
            wormMap.put(i, w);
        }

        mobMover.setCycleCount(Timeline.INDEFINITE);
        mobMover.play();

        //Intro text
        if(levelCount == 1){
            String str = "You are an old Magic 8 Ball. You have \nbeen thrown away and you must now escape \nthe depths of toy hell.";
            introMsg = new Text();
            introMsg.setFill(Color.WHITE);
            introMsg.setFont(Font.font("Monospaced", FontWeight.NORMAL, 30));
            final IntegerProperty i = new SimpleIntegerProperty(0);

            KeyFrame keyFrame = new KeyFrame(
                    Duration.millis(30),
                    event -> {
                        if (i.get() <= str.length()){
                            group.getChildren().remove(gridpane);
                            gridpane.getChildren().remove(introMsg);
                            introMsg = new Text(str.substring(0, i.get()));
                            introMsg.setFill(Color.WHITE);
                            introMsg.setFont(Font.font("Monospaced", FontWeight.NORMAL, 40));
                            gridpane.add(introMsg, (i.get() + 1), 5);
                            group.getChildren().add(gridpane);

                            i.set(i.get() + 1);
                        }
                    }
            );
            introTL = new Timeline(keyFrame);
            group.getChildren().add(introMsg);
            introTL.setCycleCount(str.length() + 1);
            introTL.play();
        }

        //Begin
        stage.setScene(scene);
        stage.show();

        hero.setMovementActiveOn();
        moveHero();
        if(launching) launching = false;
    }

    public static void main(String[] args) { launch(args); }
}