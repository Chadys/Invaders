package com.invaders;

import javafx.scene.image.Image;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovitTest extends JavaFXTestClass {

    @Test
    void ImagesLisTest(){
        Image[] imglist = Movit.imglist("Monster", (byte)3);
        assertEquals(imglist.length, 3, "la taille de la liste d'images créée n'est pas correcte");
        assertAll(
            () -> assertTrue(imglist[0].impl_getUrl().endsWith("Images/Monster/Monster1.png"), "le path de la première image n'est pas correct"),
            () -> assertTrue(imglist[1].impl_getUrl().endsWith("Images/Monster/Monster2.png"), "le path de la deuxième image n'est pas correct"),
            () -> assertTrue(imglist[2].impl_getUrl().endsWith("Images/Monster/Monster3.png"), "le path de la troisième image n'est pas correct")
        );
    }
}
