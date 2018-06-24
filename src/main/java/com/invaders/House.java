package com.invaders;
/* *
 * \file House.java
 * \brief Classe de gestion des maisons (abris)
 */
import java.util.ArrayList;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.scene.media.AudioClip;
import java.io.File;
/**
* \class House extends WritableImage 
*/		
class House extends WritableImage
{
	/** * \brief Position de la maison par rapport au jeu (pour dessiner la maison au bon endroit) */
	private int x;
	private int y;
	/** * \brief Image statique pour l'initialisation de la maison, image commune pour toutes les maisons */
	private static Image base = new Image(House.class.getResource("Images/Maison-Space.png").toExternalForm());
	/** * \brief Son qui est joué lorsqu'une maison reçoit un missile */
	private static AudioClip son_impact;

	/**
 	* \brief Constructeur d'une maison
	 */
	House(int x, int y)
	{
		/* * * \brief Appel du constructeur de la classe mère pour que la WritableImage soit directement fabriquée à partir de l'image */
		super(base.getPixelReader(), (int)base.getWidth(), (int)base.getHeight());

		this.x = x;
		this.y = y;
	}

	/**
 	* \brief Methode initialisant la liste de toutes les maisons
	 */
	static ArrayList<House> init(){
        Game.getLogger().debug("Création des maisons");
		ArrayList<House> h = new ArrayList<>(4);
		int i,j,tour;

		/* *
		* \brief Instancie le son joué lorsqu'une maison subit une collision
		*/
		son_impact = new AudioClip(new File(House.class.getResource("Sons/ImpactMaison.wav").getFile()).toURI().toString());

		/* *
		* \brief Appelle 4 fois le constructeur de House pour renvoyer la liste des 4 maisons du jeu à leur bonnes positions
		*/
		for(i=92, j=515, tour=0; tour<4 ; tour++,i+=176)
			h.add(new House(i,j));
		return h;
	}
	/**
 	* \brief Methode affichant une maison à la bonne position
	 */
	void draw(GraphicsContext gc)
	{
		gc.drawImage(this, (double)x, (double)y);
	}
	/** * \brief Méthode permettant de savoir si un pixel de cordonnées (x, y) se situe dans la zone de la maison
	*
	* Utilisé dans le tracé de cercle pour éviter les exceptions.
	*/
	boolean in_image(int x, int y)
	{
		return x >= 0 &&
				x < (int) this.getWidth() &&
                y >= 0 &&
                y < (int) this.getHeight();
	}

	/** * \brief Méthode appelée depuis la classe Tir pour la destruction d'une maison
	*
	* Utilise la méthode des cercles grossissant en appelant la méthode circle.
	* Affecte tous les pixels dans un certain rayon (transparence).
	*/
	void destruct(int x0, int y0)
	{
        Game.getLogger().trace("Impact sur une maison");
		son_impact.play();
		for(int rr = 0; rr <= Tir.tirRadius; rr++)
			circle(this.getPixelWriter(), x0, y0, rr);
	}

	/** * \brief Implémentation du tracé de cercle d'Andre
	*
	* Source : https://fr.wikipedia.org/wiki/Algorithme_de_trac%C3%A9_de_cercle_d'Andre
	* Cet algorithme a été choisit pour sa facilité d'implémentation, sa rapidité et sa capacité à produire des cercles pleins.
	*/
	private void circle(PixelWriter pr, int x0, int y0, int r)
	{
		int x = 0;
    	int y = r;
    	int d = r - 1;
    	
    	/* * * \brief Boucle pour un octant */
    	while(y >= x)
    	{
    		/* * * \brief Tracé obtenu par symétrie
			*
			* La couleur choisie est une couleur sans opacité (transparent).
			* Cela produit un trou dans l'image. 
    		*/
    	    if(in_image(x0 + x, y0 + y)) {pr.setColor(x0 + x, y0 + y, Color.TRANSPARENT);}
    	    if(in_image(x0 + y, y0 + x)) {pr.setColor(x0 + y, y0 + x, Color.TRANSPARENT);}
    	    if(in_image(x0 - x, y0 + y)) {pr.setColor(x0 - x, y0 + y, Color.TRANSPARENT);}
    	    if(in_image(x0 - y, y0 + x)) {pr.setColor(x0 - y, y0 + x, Color.TRANSPARENT);}
    	    if(in_image(x0 + x, y0 - y)) {pr.setColor(x0 + x, y0 - y, Color.TRANSPARENT);}
    	    if(in_image(x0 + y, y0 - x)) {pr.setColor(x0 + y, y0 - x, Color.TRANSPARENT);}
    	    if(in_image(x0 - x, y0 - y)) {pr.setColor(x0 - x, y0 - y, Color.TRANSPARENT);}
    	    if(in_image(x0 - y, y0 - x)) {pr.setColor(x0 - y, y0 - x, Color.TRANSPARENT);}
    	    

			/* * * \brief Ajustement des variables selon la formule d'Andre, voir démonstration mathématique sur Wikipédia */

    	    /* * * \brief Déplacement en x */
    	    if (d >= 2*x)
    	    {
    	        d -= 2*x + 1;
    	        x ++;
    	    }
    	    /* * * \brief Déplacement en y */
    	    else if (d < 2 * (r-y))
    	    {
    	        d += 2*y - 1;
    	        y --;
    	    }
    	    /* * * \brief Déplacement en x et en y */
    	    else
    	    {
    	        d += 2*(y - x - 1);
    	        y --;
    	        x ++;
    	    }
    	}
	}
	/**
 	* \brief Methode renvoyant l'abscisse de la maison
	*/
	int getX(){
		return this.x;
	}
	/**
 	* \brief Methode renvoyant l'ordonnée de la maison
	*/
	int getY(){
		return this.y;
	}
}