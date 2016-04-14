/**
 * \file Bonus.c
 * \brief Classe de gestion des bonus
 */
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import java.lang.reflect.Array;
import javafx.util.Pair;
import java.util.Arrays;
import java.lang.Math;
import javafx.scene.media.AudioClip;
import java.io.File;
/**
* \class Bonus extends Movit  
*/
public class Bonus extends Movit {
	/** * \brief Nombre de types d'aliens */
	private static byte nbonus = (byte)5;
	/** * \brief Chargement de la liste d'images de bonus avec la méthode de Movit */
	private static Image[] img_bonus = Movit.imglist("Bonus",nbonus);
	/** * \brief Vitesse de déplacement horizontal */
	private static double vitesseY=1.0;
	/** * \brief Temps maximum durant lequel un bonus est actif */ 
	public static double MAXTIME=10.0;
	/** * \brief Score apporté par le bonus "score" */
	public static int SCOREUP=200;
	/** * \brief Distance de recul des ennemis causée par le bonus "recul"*/
	public static int DISTRECULE=200;
	/** * \brief Son joué lorsque le joueur attrape un bonus */
	private static AudioClip son = new AudioClip(new File("Ressources/Sons/Bonus.wav").toURI().toString());
	/** * \brief Suite d'actions réalisées à l'activation d'un bonus */
	private Effet actif;
	/** * \brief Suite d'actions réalisées à l'inactivation d'un bonus */
	private Effet inactif;
	/** * \brief Chronomètre du temps écoulé depuis l'activation d'un bonus */
	private float time;

	/**
 	* \brief Constructeur d'un bonus
	*
	* Appelle le constructeur de Movit en utilisant une image de bonus choisie au hasard.
	* Diminue la position en X de la moitié de la largeur de l'image du bonus (pour que le bonus apparaisse au milieu de l'alien qui l'a lâché).
	* Récupère l'index de l'image du bonus qui a été choisie au hasard pour pouvoir l'identifier.
	* Récupère la paire d'effet in/actif de l'effet correspondant à l'index et les attribue respectivement à leur variable.
	* Initialise le chronomètre à 0.0 .
	*/
	public Bonus(double x,double y){
		super(img_bonus[randombonus()],0.0,vitesseY,x,y);
		this.setLayoutX(this.getLayoutX()-this.getImage().getWidth()/2);
		int index = Arrays.asList(img_bonus).indexOf(this.getImage());
		Pair<Effet,Effet> act_bonus = get_bonus(index);
		this.actif=act_bonus.getKey();
		this.inactif=act_bonus.getValue();
		this.time=0.0f;
	}
	/**
 	* \brief Méthode détectant la collision entre bonus et joueur
	*
	* Appelle la fonction collide() du moteur pour voir si une collision s'est produite entre l'image du bonus et du joueur.
	* Joue un son si le joueur a attrapé le bonus.
	* Renvoie vrai ou faux en fonction.
	*/
	public Boolean collide(Player p)
	{
		int pX,pY,x,y;
		
		pX=(int)(p.getLayoutX() + p.getTranslateX());
		pY=(int)(p.getLayoutY() + p.getTranslateY());
		x=(int)(this.getLayoutX() + this.getTranslateX());
		y=(int)(this.getLayoutY() + this.getTranslateY());

		if(Motor.collide(x, y, this.getImage(), pX, pY, p.getImage()) == null)
			return false;
		son.play();
		return true;
	}
	/**
 	* \brief Méthode actualisant la position d'un bonus
	*
	* Ajoute à l'ordonné du bonus sa vitesse verticale. Le bonus ne chutant que vers le bas, il n'a pas de déplacement horizontal.
	*/
	public void proceed(){
		this.setTranslateY(this.getTranslateY() + vecY);
	}
	/**
 	* \brief Méthode ajoutant le temps écoulé (timestep) au chronomètre d'activation du bonus 
	*/
	public void addTimeStep(float timestep){
		this.time+=timestep;
	}
	/**
 	* \brief Méthode décidant de façon aléatoire le type du nouveau bonus créé
	*
	* Renvoie un chiffre aléatoire qui correspondra à l'index de l'image du bonus dans la liste img_bonus.
	* Chaque bonus à une probabilité d'apparaître différente :
	* Bonus "vie"        : 1/7
	* Malus "tir alien"  : 3/14
	* Bonus "tir joueur" : 1/7
	* Bonus "recul"      : 1/14
	* Bonus "score"      : 3/7
	*/
	private static int randombonus(){
		int[] proba = {0,0,1,1,1,2,2,3,4,4,4,4,4,4};
		return proba[(int)(Math.random() * proba.length)];
	}
	/**
 	* \brief Méthode récupérant les effets d'un bonus
	*
	* En fonction de l'index du bonus (tiré aléatoirement), renvoie une paire composée de l'effet du bonus à son activation et à sa désactivation.
	* - Bonus "vie"
	*		Activation    : Rajoute une vie au joueur s'il en a moins de cinq (image de coeur en bas de l'écran).
	*		Désactivation : Aucun effet.
	* - Malus "tir alien"
	*		Activation    : Divise par deux le délai entre deux tirs aliens.
	*		Désactivation : Remet les valeurs initiales.
	* - Bonus "tir joueur"
	*		Activation    : Divise par deux le délai minimum entre deux tirs du joueur et multiplie par deux la vitesse de ses tirs.
	*		Désactivation : Remet les valeurs initiales.
	* - Bonus "recul"
	*		Ativation     : Fait remonter tous les aliens d'une distance DISTRECULE (Provoque aussi un retardement dans la création de nouveaux aliens).
	*		Désactivation : Aucun effet.
	* - Bonus "score"
	*		Activation    : Augmente le score du joueur de la valeur SCOREUP.
	*		Désactivation : Aucun effet.
	*/
	private static Pair<Effet,Effet> get_bonus(int index){
		final Player player=Game.getMotor().getPlayer();
		final Integer newscore;

		switch(index){
			/** * \brief Rajoute une vie au joueur s'il en a moins de cinq */
			case 0:
				return new Pair<Effet,Effet>(() -> {
					if(Game.getVie().getChildren().size()<5)
						Game.getVie().getChildren().add(new ImageView(new Image("/Ressources/life.png")));
				},null);
			/** * \brief Accélère la cadence de tir des aliens (malus) */
			case 1:
				return new Pair<Effet,Effet>(() -> Alien.setDelayTir(Alien.getDelayTir()/2),() -> Alien.setDelayTir(Alien.getDelayTir()*2));
			/** * \brief Accélère la cadence de tir du joueur */
			case 2:
				return new Pair<Effet,Effet>(() -> {
					player.setDelayTir(player.getDelayTir()/2);
					player.setVitesseTir(player.getVitesseTir()*2);
				}, () -> {
					player.setDelayTir(player.getDelayTir()*2);
					player.setVitesseTir(player.getVitesseTir()/2);
				});
			/** * \brief Fait reculer tous les aliens */
			case 3:
				return new Pair<Effet,Effet>(() -> {
					for(Alien a : Game.getMotor().getAliens())
						a.setTranslateY(a.getTranslateY()-DISTRECULE);
				},null);
			/** * \brief Augmente le score */
			default:
				return new Pair<Effet,Effet>(() -> Game.getScore().setText(Integer.toString(new Integer(Game.getScore().getText())+SCOREUP)),null);
		}
	}
	/**
 	* \brief Méthode appelée à l'activation du bonus
	*
	* Lance l'Effet "actif" du bonus puis rajoute ce bonus à la liste d'effets en cours d'activation.
	*/
	public void active(){
		act(actif);
		Game.getMotor().getActiveBonus().add(this);
	}
	/**
 	* \brief Méthode appelée à la désactivation du bonus
	*
	* Lance l'Effet "inactif" du bonus.
	*/
	public void inactive(){
		act(inactif);
	}
	/**
 	* \brief Méthode appelant la méthode act() d'un Effet
	*
	* Lance la suite d'action qui appartient à un Effet.
	*/
	private void act(Effet e){
		if(e!=null)
			e.act();
	}
	/**
 	* \brief Méthode retournant le temps écoulé depuis que le bonus s'est activé
	*/
	public float getTime(){
		return this.time;
	}

	/**
 	* \brief Interface permettant de stocker des actions
	*
	* À la création d'un objet Effet, on doit redéfinir sa méthode act(), ce qui permet de pouvoir y stocker une ou plusieurs actions différentes à accomplir pour chaque Effet.
	* Il suffira alors d'appeler la méthode act() de cet Effet pour que ces actions se produisent.
	*/
	private interface Effet{
		public void act();
	}
}