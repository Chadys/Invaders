package com.invaders;

import javafx.application.Application;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class JavaFXTestClass {
    private static final Lock l = new ReentrantLock();

    @BeforeAll
    static void initGame(){
        if (Game.getMotor() == null) {
            new Thread(() -> Application.launch(Game.class)).start();
            while (Game.getMotor() == null) ; //wait for the game to finish initiating
        }
        else{
            Game.newgame();
        }
    }
}
