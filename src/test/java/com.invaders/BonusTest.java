package com.invaders;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;

class BonusTest extends JavaFXTestClass {

    @Test
    void collisionTest() {
        Player player = Game.getMotor().getPlayer();
        Bonus bonus = new Bonus(0,0);
        bonus.relocate(player.getLayoutX(),player.getLayoutY());
        assertTrue(bonus.collide(player), "La collision joueur-bonus n'est pas détectée");
    }

    @Test
    void activationTest(){
        Bonus bonus = spy(new Bonus(0,0));
        bonus.active();
        assertTrue(Game.getMotor().getActiveBonus().contains(bonus), "Le bonus ne s'est pas ajouté à laliste des bonus actifs");
    }

    @Test
    void randomBonusTest(){
        for (int i = 0; i < 1000; i++) {
            assertTrue(Bonus.randombonus() >= 0, "Le type de bonus renvoyé est trop petit");
            assertTrue(Bonus.randombonus() <= 4, "Le type de bonus renvoyé est trop grand");
        }
    }
}
