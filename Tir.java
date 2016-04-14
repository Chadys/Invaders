/**
 * \file Tire.c
 * \brief Classe de gestions des tires de missiles
 */
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.PixelReader;
import javafx.scene.SnapshotParameters;
import java.util.ArrayList;
import javafx.util.Pair;
/**
* \class Tire extend Movit
*/
public class Tir extends Movit
{	/** * \brief Initialisation de la liste d'images des missiles (nous n'y avons mis qu'un seul missile car nous n'avons finalement pas mis en place de système de différents types de missiles bien que nous avions préparé les ressources graphiques pour) */
	private static Image[] bases = {new Image("Ressources/Missile-Space.png")};
	/** * \brief Drapeau indiquant si le missile doit être détruit ou non */
	private Boolean destroy;
	/**
 	* \brief Constructeur d'un missile
	*
	* Initialise l'image, les vecteurs et les position de départ des missiles à l'aide du constructeur de Movit.
	* Si le vecteur de déplacement vertical est positif, (le missile se déplace vers le bas car il a été tiré par un alien),
	* le missile est repositionné pour le mettre au centre par rapport à la largeur de son image, ce que la méthode shoot() de Player ou de Alien ne peut pas faire sans connaissance des dimensions du missile.
	* Elle renverse ensuite l'image du missile afin qu'il soit dessiné dans le bon sens sans avoir eu besoin de crée une image par direction.
	* Si le missile tiré par le joueur se dirige vers le haut (tiré par le joueur), il est repositionné pour être au dessus et au centre du vaisseau du joueur (en utilisant les dimention du missile).
	*/
	public Tir(byte index, double vecx, double vecy, double x, double y)
	{
		super(bases[index], vecx,vecy,0.0,0.0);

		destroy = false;
		if(vecy>0){
			this.relocate(x-this.getLayoutBounds().getWidth()/2,y);
			this.setRotate(180);
			SnapshotParameters params = new SnapshotParameters();
			params.setFill(Color.TRANSPARENT);
			this.setImage(this.snapshot(params, null));
		}
		else
			this.relocate(x-this.getLayoutBounds().getWidth()/2,y-this.getLayoutBounds().getHeight());
	}
	/**
 	* Methode utilisée par la methode proceed()
	*
	* Le missiles sont déplacés pixel par pixel selon un tracé de segment afin de déterminer l'endroit exacte des collisions.
	* Lorsque le missile a rencontré un obstacle (deplace() a renvoyé true), la fonction se termine.
	* La gestion de la durée de vie du missile se fait dans deplace().
	* Source du tracé de segment : http://raphaello.univ-fcomte.fr/Ig/Algorithme/Algorithmique.htm
	*/
	void go(double xi, double yi, double xf, double yf) 
	{
		int cumul, i;
		double xinc, yinc, x, y, dx, dy;

		/** * \brief Le point de départ de l'algorithme est la coordonnée transmise */
		x = xi;
		y = yi;
		/** * \brief La différence en x et en y que l'on parcourt dans l'algorithme s'obtient en soustrayant les coordonnées d'arrivée avec les coordonnées de départ */
		dx = xf - xi;
		dy = yf - yi;
		/** * \brief La valeur à soustraire en x et en y est 1 ou -1 en fonction de la pente */
		xinc = ( dx > 0.0 ) ? 1.0 : -1.0;
		yinc = ( dy > 0.0 ) ? 1.0 : -1.0;
		/** * \brief On normalise la différence en x et y car cela n'a plus d'importance puisque le pas est connu */
		dx = Math.abs(dx);
		dy = Math.abs(dy);
		/**
		* On oublie le premier pixel à être "allumé" (qui est la position courante du missile) car il est déjà analysé dans le précédant appel de go.
		* L'algorithme est divisé en deux en fonction de la pente du segment pour minimiser la complexité. */
		if(dx > dy)
		{
			/** * \brief Algorithme pur de Bresenham */
			cumul = (int)dx / 2;
			for(i = 1; i <= dx; i++)
			{
				x += xinc;
				cumul += dy;
				if(cumul >= dx)
				{
				  cumul -= dx;
				  y += yinc;
				}
				if(!deplace(x, y))
					return;
			}
		}
		else
		{
			cumul = (int)dy / 2;
			for(i = 1; i <= dy; i++)
			{
				y += yinc ;
				cumul += dx ;
				if ( cumul >= dy)
				{
				  cumul -= dy;
				  x += xinc;
				}
				if(!deplace(x, y))
					return;
			}
		}
	}
	/**
 	* \brief Methode de déplacement du missile utilisée par la méthode go()
	*
 	* Cette médhode est appellée pour chaque pixel parcouru par le missile.
 	* Le but est tester les collisions sur chaque entité du jeu (y compris les bords).
	* On teste les collisions en utilisant la méthode de collision générale collide() de Motor.
 	* S'il y a une collision, la fonction détruit le missile.
 	* La gestion de collision pour chaque entité est gérée dans les fonctions collide().
 	* Le retour de cette méthode permettra à go() de s'arrêter.
	*/
	public Boolean deplace(double x, double y)
	{
		/** * \brief Mise à jour de la position du missile */
		this.setTranslateX(x - this.getLayoutX());
		this.setTranslateY(y - this.getLayoutY());

		/** * \brief Test de collision avec le joueur */
		if(collide(Game.getMotor().getPlayer()))
			this.destroy = true;

		/** * \brief Test de collision avec les maisons */
		ArrayList<House> h = Game.getMotor().getHouses();
		for(int i = 0; i < h.size(); i++)
		{
			if(collide(h.get(i)))
				this.destroy = true;
		}

		/** * \brief Test de collision avec les aliens */
		ArrayList<Alien> a = Game.getMotor().getAliens();
		for(int i = 0; i < a.size(); i++)
		{
			if(collide(a.get(i)))
				this.destroy = true;
		}

		/** * \brief Test de collision avec les bords du terrain */
		if(this.getLayoutX()+this.getTranslateX()<0-this.getLayoutBounds().getWidth() || this.getLayoutX()+this.getTranslateX()>Game.WIDTH || this.getLayoutY()+this.getTranslateY()<0-this.getLayoutBounds().getHeight() || this.getLayoutY()+this.getTranslateY()>Game.HEIGHT)
			this.destroy = true;

		if(this.destroy)
			return false;
		return true;
	}

	/**
 	* \brief Methode de detection d'une collision entre un missile et le joueur
	*
 	* En cas de contact, on appelle la méthode de destruction de la classe joueur qui s'occupe d'affecter le joueur.
 	* Renvoie un booléen qui détermine la destruction ou non du missile.
	*/
	private Boolean collide(Player p){
		if(Motor.collide((int)(p.getTranslateX() + p.getLayoutX()), (int)(p.getTranslateY() + p.getLayoutY()), p.getImage(), (int)(this.getTranslateX() + this.getLayoutX()), (int)(this.getTranslateY() + this.getLayoutY()), this.getImage()) != null){
			p.destruct();
			return true;
		}
		return false;
	}
	/**
 	* \brief Methode de detection d'une collision entre un missile et une maison
 	*
 	* En cas de contact, on appelle la méthode de destruction de la classe maison avec les coordonnées de la collision, elle s'occupe d'affecter la maison.
 	* Renvoie un booléen qui détermine la destruction ou non du missile.
	*/
	private Boolean collide(House h)
	{
		Pair<Integer,Integer> coll = Motor.collide(h.getX(), h.getY(), (Image)h, (int)(this.getTranslateX() + this.getLayoutX()), (int)(this.getTranslateY() + this.getLayoutY()), this.getImage());
		if(coll == null)
			return false;
		h.destruct((int)coll.getKey() - h.getX(), (int)coll.getValue() - h.getY(), 15);
		return true;
	}
	/**
 	* \brief Methode de detection d'une collision entre un missile et un allien
 	*
 	* En cas de contact, on appelle la méthode de destruction de la classe alien qui s'occupe d'affecter l'alien.
 	* Si la collision s'est produite alors que le missile a une vitesse verticale positive, nous ignorons la collision car le missile a été tiré par un alien, et les aliens ne doivent pas s'entre-tuer.
 	* Renvoie un booléen qui détermine la destruction ou non du missile.
	 */
	private Boolean collide(Alien a){
		if(this.vecY > 0.0)
			return false;
		if(Motor.collide((int)(a.getTranslateX() + a.getLayoutX()), (int)(a.getTranslateY() + a.getLayoutY()), a.getImage(), (int)(this.getTranslateX() + this.getLayoutX()), (int)(this.getTranslateY() + this.getLayoutY()), this.getImage()) != null){
			a.destruct();
			return true;
		}
		return false;
	}

	/**
 	* \brief Methode effectuant le deplacement des objets missiles
 	*
 	* Appelle la méthode go() avec les positions de départ et d'arrivée du missile.
	 */
	public void proceed()
	{
		go(this.getTranslateX() + this.getLayoutX(), this.getTranslateY() + this.getLayoutY(), this.getTranslateX() + this.getLayoutX() + vecX, this.getTranslateY() + this.getLayoutY() + vecY);
	}
	/**
 	* \brief Methode d'affichage d'un missile
	*/
	public void draw(GraphicsContext gc){
		gc.drawImage(this.getImage(),this.getLayoutX()+this.getTranslateX(), this.getLayoutY()+this.getTranslateY());
	}
	/**
 	* \brief Méthode renvoyant le drapeau indiquant si l'alien doit être détruit
	*/
	public Boolean getDestroy(){
		return this.destroy;
	}
}