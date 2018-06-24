package com.invaders;

import javafx.application.Platform;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MotorTest extends JavaFXTestClass {
    @BeforeEach
    void resetAlienToPreventNewTirException(){
        Game.getMotor().getAliens().clear();
    }

    @Test
    void bonusTest(){
        Bonus bonus = spy(new Bonus(800, 0));
        Game.getMotor().getBonus().add(bonus);
        Game.getMotor().proceed(0, new ArrayList<>());
        assertFalse(Game.getMotor().getBonus().contains(bonus), "Le bonus ne s'est pas enlevé de la liste des bonus affichés après être sorti de l'écran");
        Mockito.doReturn(true).when(bonus).collide(any(Player.class));
        Game.getMotor().getBonus().add(bonus);
        Game.getMotor().proceed(0, new ArrayList<>());
        assertFalse(Game.getMotor().getBonus().contains(bonus), "Le bonus ne s'est pas enlevé de la liste des bonus affichés après une collision avec le joueur");
        Mockito.verify(bonus).active();
    }

    @Test
    void activeBonusTest(){
        Bonus bonus = spy(new Bonus(0, 0));
        bonus.active();
        Mockito.doReturn(Bonus.MAXTIME+1).when(bonus).getTime();
        Game.getMotor().proceed(0, new ArrayList<>());
        assertFalse(Game.getMotor().getActiveBonus().contains(bonus), "Le bonus ne s'est pas enlevé de la liste des bonus affichés après une collision avec le joueur");
        Mockito.verify(bonus).inactive();
    }

    @Test
    void collideTest(){
        WritableImage transparentImg = new WritableImage(10, 10);
        WritableImage opaqueImg = new WritableImage(10, 10);
        PixelWriter pw = opaqueImg.getPixelWriter();
        for (int i = 0; i < opaqueImg.getWidth(); i++) {
            for (int j = 0; j < opaqueImg.getHeight(); j++) {
                pw.setColor(i, j, Color.BLACK);
            }
        }
        assertNull(Motor.collide(0, 0, transparentImg, 0, 0, transparentImg), "Deux images transparentes ne devraient pas provoquer de collision");
        assertNull(Motor.collide(0, 0, transparentImg, 0, 0, opaqueImg), "Une image transparente ne peut pas provoqué de collision avec une image opaque");
        assertNull(Motor.collide(0, 0, opaqueImg, 0, 0, transparentImg), "Une image opaque ne peut pas provoqué de collision avec une image transparente");
        assertNotNull(Motor.collide(0, 0, opaqueImg, 0, 0, opaqueImg), "Deux images opaques devraient provoquer une collision");
    }

    @Test
    void levelUpTest() {
        int oldLvl = new Integer(Game.getLevel().getText());
        Game.getMotor().proceed(800, new ArrayList<>());
        int newLvl = new Integer(Game.getLevel().getText());
        assertEquals(oldLvl, newLvl-1, "le level up n'a pas eu lieu au bout du temps imparti");
    }

    @Test
    void playerActionTest() {
        int oldNTirs = Game.getMotor().getTirs().size();
        Game.getMotor().proceed(0, new ArrayList<>(Collections.singletonList(KeyCode.SPACE)));
        int newNTirs = Game.getMotor().getTirs().size();
        Game.getMotor().getTirs().clear();
        assertEquals(newNTirs, oldNTirs+1, "La gestion du tir du joueur a échoué");

        String msg = "La gestion du déplacement du joueur a échoué";
        double oldPlayerDeplace = 0.0;
        Game.getMotor().proceed(0, new ArrayList<>(Collections.singletonList(KeyCode.RIGHT)));
        double newPlayerDeplace = Game.getMotor().getPlayer().getDeplace();
        assertEquals(newPlayerDeplace, oldPlayerDeplace + 2, msg);
        Game.getMotor().proceed(0, new ArrayList<>(Collections.singletonList(KeyCode.LEFT)));
        newPlayerDeplace = Game.getMotor().getPlayer().getDeplace();
        assertEquals(newPlayerDeplace, oldPlayerDeplace - 2, msg);
        Game.getMotor().proceed(0, new ArrayList<>(Arrays.asList(KeyCode.LEFT, KeyCode.RIGHT)));
        newPlayerDeplace = Game.getMotor().getPlayer().getDeplace();
        assertEquals(newPlayerDeplace, oldPlayerDeplace, msg);
    }

    @Test
    void pauseTest() throws InterruptedException {
        Platform.runLater(() -> Game.getMotor().proceed(0, new ArrayList<>(Collections.singletonList(KeyCode.P))));
        TimeUnit.SECONDS.sleep(1);
        assertEquals(Game.getCurrentlevel(), 3, "La mise en pause du jeu n'a pas fonctionnée");
    }

    @Test
    void aliensProceedingTest(){
        Alien alien = mock(Alien.class);
        Game.getMotor().getAliens().add(alien);
        Game.getMotor().proceed(0, new ArrayList<>());
        verify(alien).proceed();
        verify(alien).collide(Game.getMotor().getPlayer());
        verify(alien).getDestroy();
        // alien choosen as bottom alien for addNewLine
        verify(alien).getLayoutY();
        verify(alien).getTranslateY();
    }

    @Test
    void aliensDestroyTest(){
        Alien alien = mock(Alien.class);
        Game.getMotor().getAliens().add(alien);
        when(alien.getDestroy()).thenReturn(true);
        Game.getMotor().proceed(0, new ArrayList<>());
        assertFalse(Game.getMotor().getAliens().contains(alien), "un alien avec un marqueur à détruire ne l'a pas été");
    }

    @Test
    void alienGameOverTest() throws InterruptedException {
        Alien alien = mock(Alien.class);
        Game.getMotor().getAliens().add(alien);
        when(alien.collide(Game.getMotor().getPlayer())).thenReturn(true);
        Platform.runLater(() -> Game.getMotor().proceed(0, new ArrayList<>()));
        TimeUnit.SECONDS.sleep(1);
        assertAll(
            () -> assertTrue(Game.getVie().getChildren().isEmpty(), "La vie du joueur n'a pas été mise à zéro après une collision joueur-alien"),
            () -> assertEquals(Game.getCurrentlevel(), 4, "L'écran de Game Over ne s'est pas déclenché")
        );
    }
}
