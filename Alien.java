/**
 * \file Alien.c
 * \brief Classe de gestion des aliens
 */
import javafx.scene.image.Image;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.lang.Math;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.media.AudioClip;
import java.io.File;
/**
* \class Alien extends Movit  
*/
public class Alien extends Movit {
	/** * \brief Nombre de types d'aliens */
	private static byte naliens = (byte)5;
	/** * \brief Chargement de la liste d'images d'aliens avec la méthode de Movit */
	private static Image[] aliens = Movit.imglist("Monster", naliens);
	/** * \brief Vitesse de déplacement horizontal */
	private static double vitesseX=0.5;
	/** * \brief Vitesse de déplacement vertical */
	private static double vitesseY=15.0;
	/** * \brief Vitesse de déplacement vertical d'un tir */
	private static double vitesseTir=2.0;
	/** * \brief Délai entre deux tirs ennemis */
	private static float delay_tir=1.0f;
	/** * \brief Chronomètre entre deux tirs */
	private static float current_delay=0.0f;
	/** * \brief Son joué lorsqu'un alien tire */
	private static AudioClip son_tir;
	/** * \brief Témoin du nombre d'ennemis */
	private static SimpleIntegerProperty compteur=compteurProperty();
	/** * \brief Drapeau indiquant si l'ennemi doit être détruit ou non */
	private Boolean destroy;
	/** * \brief Score apporté par l'ennemi */
	private int value;

	/**
 	* \brief Constructeur d'un alien
 	*
	* Appelle le constructeur de Movit, l'alien aura pour commencer une vitesse vertical nul et une vitesse horizontale positive (il démarre donc en se déplaçant vers la droite).
	* Met destroy à false et donne à l'alien une valeur (score) correspondant à son index dans la liste d'images.
	*/
	public Alien(byte index, double x, double y){
		super(aliens[index],vitesseX,0.0,x,y);
		this.destroy=false;
		this.value=(index+1)*10;
	}
	/**
	* \brief Liste de tous les aliens
	*
	* Initialise le son joué lors du tir d'une alien et remet les différents délais et vitesses de la classe à leur valeurs initiales.
	* Initialise la liste d'aliens du jeu et appelle le constructeur 5*11 fois en plaçant chaque alien à sa position (55 pixels entre chaque aliens côte à côte, 50 pixels entre chaque lignes d'aliens).
	* Les aliens sont ajoutés de bas en haut pour que le dernier alien de la liste soit toujours dans la ligne la plus haute (sert pour l'ajout de nouvelles lignes).
	* Initialise le compteur du nombre d'aliens et ajoute une méthode dynamique qui augmente la vitesse des ennemis (et donc la difficulté du jeu) à chaque fois que ce compteur change de valeur.
	* Renvoie la liste ainsi crée.
	*/
	
	public static ArrayList<Alien> init(){
		ArrayList<Alien> aliens = new ArrayList<Alien>(5*11);
		int i,j,tour,tour2;

		son_tir = new AudioClip(new File("Ressources/Sons/TirAlien.wav").toURI().toString());

		resetVitesse();

		for(i=0, j=200, tour=0; tour<5 ; tour++, i=0, j-=50)
			for(tour2=0; tour2<11 ; tour2++,i+=55)
				aliens.add(new Alien((byte)tour,i,j));;
		compteur.set(aliens.size());

		compteur.addListener((observable, oldvalue, newvalue) -> {
			vitesseX += (vitesseX/Math.abs(vitesseX))*0.008;
			vitesseTir += vitesseTir*0.008;
			delay_tir -= delay_tir*0.010;
		});
		return aliens;
	}
	/**
 	* \brief Méthode détectant la collision avec un alien
	*
	* Vérifie d'abord si l'alien n'a pas dépassé le bas de la fenêtre (675px), puis appelle la méthode générale de collision du moteur pour voir si un alien est entré en collision avec le joueur.
	* Renvoi vrai si l'un deux tests est vrai.
	*/
	public Boolean collide(Player p)
	{
		int pX,pY,x,y;
		
		pX=(int)(p.getLayoutX() + p.getTranslateX());
		pY=(int)(p.getLayoutY() + p.getTranslateY());
		x=(int)(this.getLayoutX() + this.getTranslateX());
		y=(int)(this.getLayoutY() + this.getTranslateY());

		if(y > 675 || Motor.collide(x, y, this.getImage(), pX, pY, p.getImage()) != null)
			return true;
		return false;
	}
	/**
 	* \brief Méthode actualisant la position d'un alien
	*
	* Ajoute à la position de l'alien ses vitesses horizontale et verticale.
	*/
	public void proceed(){

		this.setTranslateY(this.getTranslateY() + vecY);
		this.setTranslateX(this.getTranslateX() + vecX);
	}
	/**
 	* \brief Méthode actualisant l'état de tous les aliens
	*
	* Pour chaque alien, met à jour ses vitesses horizontale et verticale :
	* Si la vitesse horizontale est à 0.0, alors les aliens viennent de descendre d'une ligne, cette fonction remet donc la vitesse verticale à 0.0 et remet la vitesse horizontale à sa valeur générale vitesseX.
	* Sinon vérifie si au prochain déplacement, un alien va dépasser le cadre du jeu, si c'est le cas, met la vitesse horizontale à 0.0, la vitesse verticale à vitesseY (pour que le prochain déplacement des aliens soit de les faire descendre), et inverse vitesseX pour qu'au tour de boucle suivant, les aliens se déplacent dans l'autre direction.
	* Enfin, met à jour le delai général en fonction du temps qui s'est écoulé depuis le dernier proceed(), et si le délai est suffisant, fait tirer un alien.
	*/
	public static void proceed(float timestep)
	{
		double newpos;
		ArrayList<Alien> aliens = Game.getMotor().getAliens();

		for(Alien a : aliens){
			if(a.vecX==0.0){
				for(Alien abis : aliens){
					abis.vecX=vitesseX;
					abis.vecY=0.0;
				}
				break;
			}
			newpos=a.getLayoutX()+a.getTranslateX()+a.vecX;
			if(newpos<0 || newpos+a.getImage().getWidth()>Game.WIDTH){
				vitesseX*=-1;
				for(Alien abis : aliens){
					abis.vecX=0.0;
					abis.vecY=vitesseY;
				}
				break;
			}
		}

		current_delay -= timestep;
		if(current_delay<=0.0f){
			current_delay=delay_tir;
			shoot(aliens);
		}
	}
	/**
 	* \brief Méthode rajoutant une ligne d'aliens en haut de l'écran lorsqu'il y a de la place
	*
	* Cherche l'alien avec l'abcisse la plus petite pour faire démarrer la ligne d'aliens au même x.
	* Puis fait une boucle qui va placer les aliens un à un avec le bon écart et au y précisé en argument.
	* Si la position horizontale s'apprête à dépasser le cadre du jeu, place les aliens qui manquent à gauche du premier qui a été placé.
	*/
	public static void addnewline(byte index, double y){
		ArrayList<Alien> aliens = Game.getMotor().getAliens();
		int i;
		double x=Game.WIDTH;
		Alien al;

		for (Alien a : aliens)
			if(x>a.getLayoutX()+a.getTranslateX())
				x=a.getLayoutX()+a.getTranslateX();;
		for(i=0; i<11 ; i++,x+=55){
			aliens.add(new Alien((byte)index,x,y));
			al = aliens.get(aliens.size()-1);
			if(al.getLayoutX()+al.getTranslateX()>Game.WIDTH){
				x-=55*(i+1);
				al.setTranslateX(x-al.getLayoutX());
				for(; i<11 ; i++,x-=55)
					aliens.add(new Alien((byte)index,x,y));
				break;
			}
		}
	}
	/**
 	* \brief Méthode créant un tir d'alien avec lancement du son
	*
	* Ajoute un tir à liste des tirs en appelant le constructeur de Tir en fonction de la position de l'alien qui tire (le tire sera positionné juste en dessous et au centre de l'alien) et de la vitesse de tir de cette classe.
	*/
	public void shoot(){
		Game.getMotor().getTirs().add(new Tir((byte)0,0.0,vitesseTir,this.getLayoutX()+(this.getImage().getWidth()/2)+this.getTranslateX(),this.getLayoutY()+this.getTranslateY()+this.getLayoutBounds().getHeight()));
		son_tir.play();
	}
	/**
 	* \brief Méthode affichant un missile alien (venant d'être tiré)
	*
	* Récupère toutes les positions en x des aliens du jeu.
	* Fait un random pour savoir à quelle position se produira le tir.
	* Cherche l'alien le plus bas à cette abscisse et le fait tirer
	*/
	private static void shoot(ArrayList<Alien> aliens){
		ArrayList<Double> allposX = new ArrayList<Double>();
		double posx;
		Alien kishoot=null;

		for(Alien a : aliens){
			if(!allposX.contains(a.getLayoutX()))
				allposX.add(a.getLayoutX());
		}
		posx = allposX.get((int)(Math.random() * allposX.size()));
		for(Alien a : aliens)
			if(a.getLayoutX()==posx)
				if(kishoot==null || a.getLayoutY()>kishoot.getLayoutY())
					kishoot=a;;;
		if(kishoot!=null)
			kishoot.shoot();
	}
	/**
	* \brief Méthode appelée lors de la destruction d'un alien
	*
	* Ajoute la valeur de l'alien au score de la partie.
	* Met le témoin de destruction de l'alien à vrai pour que le moteur du jeu sache qu'il faut le détruire.
	* Décrémente le compteur du nombre d'alien.
	* A une probabilité de 1/8 d'ajouter un bonus au jeu en appelant le constructeur de Bonus.
	*/
	public void destruct(){
		Text score = Game.getScore();
		Integer newscore = new Integer(score.getText());
		newscore+=this.value;
		score.setText(newscore.toString());
		destroy=true;
		compteur.set(compteur.get()-1);
		if((int)(Math.random()*8) == 4)
			Game.getMotor().getBonus().add(new Bonus(this.getLayoutX()+(this.getImage().getWidth()/2)+this.getTranslateX(),this.getLayoutY()+this.getTranslateY()+this.getLayoutBounds().getHeight()));
	}
	/**
	* \brief Méthode initialisant le compteur du nombre d'aliens
	*/
	private static SimpleIntegerProperty compteurProperty(){
		if (compteur == null) compteur = new SimpleIntegerProperty(0);
		return compteur;
	}
	/**
 	* \brief Méthode renvoyant le drapeau indiquant si l'alien doit être détruit
	*/
	public Boolean getDestroy(){
		return this.destroy;
	}
	/**
 	* \brief Méthode renvoyant la position en Y d'un missile
	*/
	public double getVecY(){
		return this.vecY;
	}
	/**
 	* \brief Méthode renvoyant le delai entre deux tirs
	*/
	public static float getDelayTir(){
		return delay_tir;
	}
	/**
 	* \brief Méthode modifiant le delai entre deux tirs
	*/
	public static void setDelayTir(float delay){
		delay_tir=delay;
	}
	/**
 	* \brief Méthode réinitialisant la vitesse des aliens 
	*
	* Remet la vitesse des aliens à leur valeurs initiales, utilisée en cas de nouvelle partie.
	*/
	public static void resetVitesse(){
		vitesseX=0.5;
		vitesseY=15.0;
		vitesseTir=2.0;
		delay_tir=1.0f;
		current_delay=0.0f;
	}
}
