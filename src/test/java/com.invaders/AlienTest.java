package com.invaders;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class AlienTest extends JavaFXTestClass {

    @Test
    void collisionTest() {
        Player player = Game.getMotor().getPlayer();
        Alien alien = new Alien((byte)0,player.getLayoutX(),player.getLayoutY());
        assertTrue(alien.collide(player), "La collision joueur-alien n'est pas détectée");
        alien.setLayoutX(-player.getLayoutX());
        alien.setLayoutY(800);
        assertTrue(alien.collide(player), "La sortie de l'écran de l'alien n'est pas détectée");
    }

    @Test
    void newLineTest(){
        int oldNaliens = Game.getMotor().getAliens().size();
        Alien.addnewline((byte)0, 0);
        int newNaliens = Game.getMotor().getAliens().size();
        assertEquals(newNaliens - oldNaliens, Alien.N_LINE_ALIENS, "L'ajout d'une ligne d'alien a échoué");
    }

    @Test
    void deplacementTest(){
        Alien alien = new Alien((byte)0,0,0);
        alien.vecX = 2;
        alien.vecY = 3;
        double oldTranslateX = alien.getTranslateX();
        double oldTranslateY = alien.getTranslateY();
        alien.proceed();
        assertAll(
            () -> assertEquals(alien.getTranslateX(), oldTranslateX+2, "La mise à jour de la position horizontale de l'alien a échoué"),
            () -> assertEquals(alien.getTranslateY(), oldTranslateY+3, "La mise à jour de la position verticale de l'alien a échoué")
        );
    }
    @Test
    void lineDeplacementTest(){
        Alien alien = new Alien((byte)0,0,0);
        alien.setLayoutX(-1000);
        Game.getMotor().getAliens().clear();
        Game.getMotor().getAliens().add(alien);
        double oldSpeedX = alien.vecX;
        Alien.proceed(-10);
        assertAll(
                () -> assertTrue(alien.vecY > 0, "le déplacement vertical des aliens ne s'est pas activé alors qu'un alien dépasse du bord gauche de l'écran"),
                () -> assertEquals(alien.vecX, 0.0, "le déplacement horizontal des aliens ne s'est pas arrêter alors qu'un alien dépasse du bord gauche de l'écran")
        );
        Alien.proceed(0);
        assertAll(
                () -> assertEquals(alien.vecX, -oldSpeedX, "le déplacement horizontal des alien ne s'est pas inversé après un déplacement vertical"),
                () -> assertEquals(alien.vecY, 0.0, "le déplacement vertical des alien ne s'est pas désactivé après une utilisation")
        );
        alien.setLayoutX(1000);
        Alien.proceed(0);
        assertAll(
                () -> assertTrue(alien.vecY > 0, "le déplacement vertical des aliens ne s'est pas activé alors qu'un alien dépasse du bord droit de l'écran"),
                () -> assertEquals(alien.vecX, 0.0, "le déplacement horizontal des aliens ne s'est pas arrêter alors qu'un alien dépasse du bord droit de l'écran")
        );
        Alien.proceed(0);
        assertAll(
                () -> assertEquals(alien.vecX, oldSpeedX, "le déplacement horizontal des alien ne s'est pas inversé après un déplacement vertical"),
                () -> assertEquals(alien.vecY, 0.0, "le déplacement vertical des alien ne s'est pas désactivé après une utilisation")
        );
    }

    @Test
    void shootTest() {
        Alien alien = mock(Alien.class);
        Game.getMotor().getAliens().clear();
        Game.getMotor().getAliens().add(alien);
        Alien.proceed(Alien.getDelayTir());
        Alien.shoot(new ArrayList<>(Collections.singletonList(alien)));
        verify(alien, times(2)).shoot();
    }

    @Test
    void destructionTest(){
        Alien alien = new Alien((byte)0,0,0);
        int oldCompteur = Alien.getCompteur();
        int oldScore = new Integer(Game.getScore().getText());
        alien.destruct();
        assertTrue(alien.getDestroy(), "Le marqueur de destruction de l'alien ne s'est pas activé");
        assertTrue(oldCompteur > Alien.getCompteur(), "le compteur global d'aliens ne s'est pas décrémenté");
        int newScore = new Integer(Game.getScore().getText());
        assertTrue(newScore > oldScore, "le score du jeu ne s'est pas incrémenté");
        int oldNbonus = Game.getMotor().getBonus().size();
        for (int i = 0; i < 1000; i++) {
            alien.destruct();
        }
        //probability of 1/8 to create a new bonus, in 1000 loop should happen at least once, but test can still theoretically fail
        // need better method, perhaps wait for PowerMock update to junit5 to mock math.random() ?
        int newNbonus = Game.getMotor().getBonus().size();
        assertTrue(oldNbonus < newNbonus, "aucun bonus ne s'est créé");
    }
}
