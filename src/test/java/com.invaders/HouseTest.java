package com.invaders;

import javafx.scene.image.PixelReader;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HouseTest extends JavaFXTestClass {

    @Test
    void limitTest(){
        House house = new House(0, 0);
        int inX = (int)house.getWidth()/2;
        int inY = (int)house.getHeight()/2;
        int outX1 = (int)house.getWidth()+1;
        int outX2 = -1;
        int outY1 = (int)house.getHeight()+1;
        int outY2 = -1;
        String outMessage = "Un pixel situé hors de la maison est détecté à l'intérieur";

        assertAll(
            () -> assertTrue(house.in_image(inX, inY), "Un pixel situé dans la maison est détecté à l'extérieur"),
            () -> assertFalse(house.in_image(inX, outY1), outMessage),
            () -> assertFalse(house.in_image(outX1, inY), outMessage),
            () -> assertFalse(house.in_image(inX, outY2), outMessage),
            () -> assertFalse(house.in_image(outX2, inY), outMessage),
            () -> assertFalse(house.in_image(outX1, outY1), outMessage),
            () -> assertFalse(house.in_image(outX2, outY2), outMessage)
        );
    }

    @Test
    void destructTest(){
        House house = new House(0, 0);
        int x = (int)house.getWidth()/2;
        int y = 0;
        PixelReader pr = house.getPixelReader();
        final String msgDestroyed = "transparent pixel before house has been destroyed there";
        assertAll(
            () -> assertNotEquals(pr.getColor(x, y).getOpacity(), 0.0, msgDestroyed),
            () -> assertNotEquals(pr.getColor(x, y + Tir.tirRadius).getOpacity(), 0.0, msgDestroyed)
        );
        house.destruct(x, y);
        final String msgNotDestroyed = "pixel in destruction radius didn't become transparent";
        assertAll(
                () -> assertEquals(pr.getColor(x, y).getOpacity(), 0.0, msgNotDestroyed),
                () -> assertEquals(pr.getColor(x, y + Tir.tirRadius).getOpacity(), 0.0, msgNotDestroyed)
        );
    }
}
