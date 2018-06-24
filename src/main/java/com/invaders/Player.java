package com.invaders;
/* *
 * \file Player.java
 * \brief Classe de gestion du joueur
 */
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.media.AudioClip;

import java.io.File;
/**
* \class Player extends ImageView
*/
class Player extends ImageView
{
	/** * \brief Délai minimum entre deux tirs du joueur */
	private float delay_tir;
	/** * \brief Chronomètre du temps restant avant que le joueur puisse tirer à nouveau */
	private float current_delay;
	/** * \brief Vecteur de déplacement horizontal du joueur */
	private double deplace;
	/** * \brief Vecteur de déplacement vertical d'un missile tiré par le joueur */
	private double vitesseTir;
	/** * \brief Son joué lorsque le joueur entre en collision avec un missile */
	private AudioClip son_impact;
	/** * \brief Son joué lorsque le joueur tire */
	private AudioClip son_tir;

	/**
 	* \brief Constructeur du joueur
	*
	* Appelle le constructeur d'ImageView avec l'image du vaisseau du joueur.
	* Initialise les deux sons associés au joueur.
	* Place le joueur en bas et au milieu horizontalement de l'écran.
	* Initialise les vecteurs, délai et chronomètre à leur valeur de départ.
	* Player n'hérite pas de Movit car il ne possède pas de vecteur de déplacement vertical et qu'on a besoin d'utiliser relocate() après avoir défini son image.
	*/
	Player()
	{
		super(new Image(Player.class.getResource("Images/Vaisseau-Space.png").toExternalForm()));
        Game.getLogger().debug("Création du vaisseau joueur");
		son_tir = new AudioClip(new File(getClass().getResource("Sons/TirShip.wav").getFile()).toURI().toString());
		son_impact = new AudioClip(new File(getClass().getResource("Sons/ImpactVaisseau.wav").getFile()).toURI().toString());
		this.relocate(Game.WIDTH/2-this.getImage().getWidth()/2, 640);
		this.delay_tir = 0.6f;
		this.current_delay = 0.0f;
		this.deplace = 0.0;
		this.vitesseTir=3.5;
	}
	/**
 	* \brief Methode actualisant la position de Player
	*
	* Enlève le temps écoulé depuis le dernier proceed() à "current_delay" (borné à 0).
	* Actualise la position du joueur en l'empéchant de disparaître du cadre du jeu.
	* Si le joueur sors du cadre en ajoutant le vecteur de déplacement à la position actuelle, alors on le positionne à la limite.
	*/
	void proceed(float timestep)
	{
		double newpos = this.getTranslateX()+deplace;
		
		current_delay -= timestep;
		if(current_delay < 0.0f)
			current_delay = 0.0f;
		if(this.getLayoutX() + newpos < -(this.getImage().getWidth()/2))
			this.setTranslateX(-((this.getImage().getWidth() / 2) + this.getLayoutX()));
			
		else if(this.getLayoutX() + newpos > Game.WIDTH -(this.getImage().getWidth() / 2))
			this.setTranslateX(this.getLayoutX() + (this.getImage().getWidth() / 2));
			
		else
			this.setTranslateX(newpos);
	}
	/**
 	* \brief Methode de tir du joueur
	*
	* Si le délai minimum entre deux tirs n'est pas encore écoulé, ne fais rien.
	* Sinon, joue le son du tir, réinitialise le délai minimum, et ajoute un nouveau tir à la liste des tirs en appelant le constructeur de Tire en fonction de la position du joueur et de sa vitesse de tir.
	*/
	void shoot()
	{
		if(current_delay > 0.0f)
			return;
        Game.getLogger().trace("Tir vaisseau");
		son_tir.play();
		current_delay = this.delay_tir;
		Game.getMotor().getTirs().add(new Tir((byte)0,0.0,-vitesseTir,this.getLayoutX()+(this.getImage().getWidth()/2)+this.getTranslateX(),this.getLayoutY()));
	}
	/**
 	* \brief Methode appelée en cas de collision entre le joueur et un missile
	*
	* Joue le son de collision entre le joueur et un missile.
	* Retire une vie au joueur et, s'il n'en a plus, met le jeu sur l'écran de gameover.
	*/
	void destruct(){
        Game.getLogger().debug("Impact sur le vaisseau joueur");
		son_impact.play();
		FlowPane vie=Game.getVie();
		vie.getChildren().remove(0);
		if(vie.getChildren().size()==0) {
            Game.getLogger().trace("Le vaisseau joueur a été détuit");
            Game.setCurrentLevel(4);
        }
	}
	/**
 	* \brief Methode renvoyant le vecteur de déplacement
	*/
	double getDeplace(){
		return this.deplace;
	}
	/**
 	* \brief Methode paramètrant le vecteur de deplacement
	*/
	void setDeplace(double pas){
		this.deplace=pas;
	}
	/**
 	* \brief Methode renvoyant le delai minimum entre deux tirs
	*/
	float getDelayTir(){
		return this.delay_tir;
	}
	/**
 	* \brief Methode paramétrant le delai minimum entre deux tirs
	*/
	void setDelayTir(float delay){
		this.delay_tir=delay;
	}
	/**
 	* \brief Methode renvoyant la vitesse d'un missile tiré
	*/
	double getVitesseTir(){
		return this.vitesseTir;
	}
	/**
 	* \brief Methode paramétrant la vitesse d'un missile tiré
	*/
	void setVitesseTir(double vitesse){
		this.vitesseTir=vitesse;
	}
}