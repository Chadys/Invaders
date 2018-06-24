package com.invaders;

import javafx.application.Application;
import org.junit.jupiter.api.BeforeAll;

class JavaFXTestClass {

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
