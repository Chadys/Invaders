package com.invaders;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class TirTest extends JavaFXTestClass {
    private static ResultCaptor<Boolean> resultCaptor = new ResultCaptor<>();

    @BeforeAll
    static void removePotentialCollision(){
        Game.getMotor().getAliens().clear();
        Game.getMotor().getHouses().clear();
        Game.getMotor().getPlayer().setTranslateX(-300);
    }

    @Test
    void collideUpGameBorderTest(){
        Tir tir = spy(new Tir((byte) 0, 0, -Game.HEIGHT, Game.WIDTH/2, Game.HEIGHT/2));

        doAnswer(resultCaptor).when(tir).collideGameBorder();
        tir.proceed();
        assertTrue(tir.getDestroy(), "le marqueur de destruction du missile n'a pas été déclenché");
        assertTrue(resultCaptor.getResult(), "la collision du missile avec le bord haut n'a pas été détectée");
    }

    @Test
    void collideDownGameBorderTest(){
        Tir tir = spy(new Tir((byte) 0, 0, -1, Game.WIDTH/2, Game.HEIGHT*2));

        doAnswer(resultCaptor).when(tir).collideGameBorder();
        tir.proceed();
        assertTrue(tir.getDestroy(), "le marqueur de destruction du missile n'a pas été déclenché");
        assertTrue(resultCaptor.getResult(), "la collision du missile avec le bord bas n'a pas été détectée");
    }

    @Test
    void collideRightGameBorderTest(){
        Tir tir = spy(new Tir((byte) 0, Game.WIDTH, 0, Game.WIDTH/2, Game.HEIGHT/2));

        doAnswer(resultCaptor).when(tir).collideGameBorder();
        tir.proceed();
        assertTrue(tir.getDestroy(), "le marqueur de destruction du missile n'a pas été déclenché");
        assertTrue(resultCaptor.getResult(), "la collision du missile avec le bord droit n'a pas été détectée");
    }

    @Test
    void collideLeftGameBorderTest(){
        Tir tir = spy(new Tir((byte) 0, -Game.WIDTH, 0, Game.WIDTH/2, Game.HEIGHT/2));

        doAnswer(resultCaptor).when(tir).collideGameBorder();
        tir.proceed();
        assertTrue(tir.getDestroy(), "le marqueur de destruction du missile n'a pas été déclenché");
        assertTrue(resultCaptor.getResult(), "la collision du missile avec le bord gauche n'a pas été détectée");
    }

    @Test
    void collidePlayerTest(){
        Player player = spy(new Player());
        Tir tir = new Tir((byte) 0, 0, 0, 0, 0);
        tir.relocate(player.getLayoutX(), player.getLayoutY());

        doNothing().when(player).destruct();
        assertTrue(tir.collide(player), "la collision du missile avec le joueur n'a pas été détectée");
        verify(player).destruct();
        tir.relocate(0,0);
        assertFalse(tir.collide(player), "une collision missile-joueur a été détecté alors qu'elle n'aurait pas dû l'être");
    }

    @Test
    void collideHouseTest(){
        House house = spy(new House(50, 50));
        Tir tir = new Tir((byte) 0, 0, 0, 0, 0);
        tir.relocate(house.getX()+(house.getWidth()/2), house.getY());

        assertTrue(tir.collide(house), "la collision du missile avec une maison n'a pas été détectée");
        verify(house).destruct(anyInt(), anyInt());
        tir.relocate(0,0);
        assertFalse(tir.collide(house), "une collision missile-maison a été détecté alors qu'elle n'aurait pas dû l'être");
    }

    @Test
    void collideAlienTest(){
        Alien alien = spy(new Alien((byte)0, 50, 50));
        Tir tir = new Tir((byte) 0, 0, 0, 0, 0);
        tir.relocate(alien.getLayoutX(), alien.getLayoutY());

        assertTrue(tir.collide(alien), "la collision du missile avec un alien n'a pas été détectée");
        verify(alien).destruct();
        tir.relocate(0,0);
        assertFalse(tir.collide(alien), "une collision missile-alien a été détecté alors qu'elle n'aurait pas dû l'être");
    }
}
