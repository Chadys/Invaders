package com.invaders;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PlayerTest extends JavaFXTestClass {

    @Test
    void deplacementTest(){
        Player player = spy(new Player());
        player.setDeplace(-Game.WIDTH);
        player.proceed(0);
        assertEquals(player.getLayoutX()+player.getTranslateX(),
                -player.getImage().getWidth() / 2,
                "Le déplacement du joueur n'a pas été correctement borné à gauche");
        player.setDeplace(Game.WIDTH*2);
        player.proceed(0);
        assertEquals(player.getLayoutX()+player.getTranslateX(),
                Game.WIDTH-player.getImage().getWidth() / 2,
                "Le déplacement du joueur n'a pas été correctement borné à droite");
        player.setDeplace(-2);
        double oldTranslateX = player.getTranslateX();
        player.proceed(0);
        assertEquals(player.getTranslateX(),oldTranslateX - 2,
                "Le déplacement du joueur n'a pas été fait aux bonnes coordonnées");
        verify(player, times(3)).setTranslateX(anyDouble());
    }

    @Test
    void shootTest(){
        Player player = new Player();
        int oldNTirs = Game.getMotor().getTirs().size();
        player.shoot();
        int newNTirs = Game.getMotor().getTirs().size();
        assertEquals(newNTirs, oldNTirs+1, "La production d'un tir par le joueur a échoué");

        oldNTirs = newNTirs;
        player.shoot();
        newNTirs = Game.getMotor().getTirs().size();
        assertEquals(newNTirs, oldNTirs, "le temps minimum entre deux tir n'est pas respecté");
    }

    @Test
    void destructTest(){
        Player player = new Player();

        int oldVies = Game.getVie().getChildren().size();
        while (oldVies > 0){
            player.destruct();
            int newVies = Game.getVie().getChildren().size();
            assertEquals(newVies, oldVies-1, "La vie du joueur n'a pas décrémenté après un impact");
            oldVies = newVies;
        }
        assertEquals(Game.getCurrentlevel(), 4, "L'écran de Game Over ne s'est pas déclenché après que le joueur ait perdu toutes ses vies");
    }
}
