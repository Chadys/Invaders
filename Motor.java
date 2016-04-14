/**
 * \file Motor.c
 * \brief Classe de gestions du moteur graphique
 */
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.effect.Glow ;
import javafx.scene.text.Text;
import javafx.animation.AnimationTimer;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.util.Pair;
import javafx.scene.image.PixelReader;
import java.lang.Math;
/**
* \class Motor
*/
public class Motor
{	
	/** * \brief Image qui sert de fond à notre jeu */
	private static Image fond;
	/** * \brief Objet dont la méthode //handle() est appelée en boucle à chaque fois qu'on lui demande d'être activée et jusqu'à ce qu'on l'arrête */
	private AnimationTimer gameLoop;
	/** * \brief Utilisé dans la gameloop. Définit la fréquence de rafraîchissement à obtenir */
	private float timeStep;
	/** * \brief Utilisé dans la gameloop. Il s'agit du moment de la frame précedente */
	private float previousNanoTime;
	/** * \brief Utilisé dans la gameloop. Sert à gérer le temps qu'il reste à la fin d'une frame */
	private float accumulatedTime;
	/** * \brief Chronomètre permettant de savoir lorsqu'on doit augmenter le niveau auquel est arrivé le joueur */
	private float level_up_time;
	/** * \brief Indicateur de l'index de l'image d'alien (dans la liste "aliens" de la classe Alien) à utiliser lors de l'affichage d'une nouvelle ligne d'aliens */
	private int index_alien;
	/** * \brief  Liste des objets Tirs actuellement affichés dans le jeu, c'est à dire les missiles qui ont été tirés et ne sont pas encore sortis du cadre ou entrés en collision */
	private ArrayList<Tir> tirs;
	/** * \brief  Liste des objets House actuellement affichés dans le jeu, les quatre maisons qui servent de bouclier au vaisseau du joueur */
	private ArrayList<House> houses;
	/** * \brief  Liste des objets Alien actuellement affichés dans le jeu, regroupe tous les ennemis qui n'ont pas encore été détruits */
	private ArrayList<Alien> aliens;
	/** * \brief  Liste des objets Bonus actuellement affichés dans le jeu, soit les bonus apparus qui ne sont pas encore sortis du cadre ou que le joueur n'a pas encore attrapé */
	private ArrayList<Bonus> bonus;
	/** * \brief  Liste des Bonus Tirs actuellement actifs dans le jeu, c'est-à-dire les bonus qui ont été attrapés par le vaisseau du joueur et qui n'ont pas encore dépassé leur temps limite d'activation */
	private ArrayList<Bonus> active_bonus;
	/** * \brief  L'objet Player qui représente le vaisseau contrôlé par le joueur */
	private Player player;
	/**
 	* \brief Methode de lancement de la GameLoop
	 */
	public void start()
	{
		gameLoop.start();
	}
	/**
 	* \brief Methode d'arrêt de la GameLoop
	 */
	public void stop()
	{
		gameLoop.stop();
	}
	/**
 	* \brief Constructeur d'un moteur
 	*
 	* Le moteur d'un jeu est la partie qui s'occupe de simuler en temps réel les caractéristiques que l'on veut attribuer aux élements du jeu ainsi que les lois physiques qui le compose.
	 */
	public Motor(GraphicsContext gc, ArrayList<KeyCode> keys)
	{
	/**
 	* \brief Initialise les chronomètre à 0.0, initialise le temps qui se passera entre deux proceed() et donne l'index de l'image de la prochaine ligne d'alien qui sera dessiné.
	 */
		level_up_time = 0.0f;
		previousNanoTime  = 0.0f;
		accumulatedTime = 0.0f;
		timeStep = 0.007f;
		index_alien=0;
	
		fond=new Image("/Ressources/Background-Space.png");

		player = new Player();

		/**
		* \brief Initialise tous les objets dynamiques du jeu avec leur propre méthode de classe (ou une liste vide dans le cas des objets qui ne sont pas encore présents soit les missiles et les bonus).
		 */

		tirs=new ArrayList<Tir>();

		houses=House.init();

		aliens=Alien.init();

		bonus=new ArrayList<Bonus>();
		active_bonus=new ArrayList<Bonus>();
		
		/**
		* \brief Initialise la boucle principale du jeu
		*
		* Une boucle de jeu sert à gérer la fréquence d'affichage et de calcul du jeu.
		* Ici la game loop est à temps "fixe", ce qui veut dire que si le jeu ralentis, le moteur du jeu ne dois pas prendre en compte ce ralentissements.
		* Par exemple, si un objet doit se déplacer à vitesse constante, il ralentira si la fréquence d'affichage diminue.
		* A l'inverse, il existe des boucles de jeu qui permettent au moteur de s'adapter à la fréquence d'affichage.
		* Source : http://svanimpe.be/blog/game-loops-fx.html
		 */
		
		gameLoop = new AnimationTimer()
		{
			/**
			* \brief Méthode appellée le plus souvent possible, au maximum 60 fois par secondes
			*/
		
			public void handle(long currentNanoTime)
			{
				/**
				* \brief S'il s'agit de la première frame, on règle le temps précédent pour ne pas créer un grand écart entre deux frames
				*/
				if(previousNanoTime == 0.0f){
					previousNanoTime = currentNanoTime;
					return;
				}
				/**
				* \brief L'espace entre deux frame en secondes
				*/
				float gap = (currentNanoTime - previousNanoTime) / 1e9f;
				/**
				* \brief Actualisation du temps accumulé
				*/
				accumulatedTime += gap;
				/**
				* \brief Actualisation du moment de la frame précédante
				*/
				previousNanoTime = currentNanoTime;
				/**
				* \brief Joue le rôle de régulateur, permet de ne pas afficher le jeu si la fréquence d'affichage est trop élevée. Ainsi, le temps restant servira au calculs du moteur (proceed())
		 		*/
				while (accumulatedTime >= timeStep)
				{
				  	proceed(timeStep, keys);
				  	accumulatedTime -= timeStep;
				};
				render(gc);
			}
			/**
			* \brief Override de la méthode stop pour ne pas dérégler la gameloop
			*/
			@Override
			public void stop(){
				super.stop();
				previousNanoTime  = 0.0f;
				accumulatedTime = 0.0f;
			}
		};
	}
	/**
 	* \brief Methode s'occupant des déplacements et changement d'état à partir des données du jeu
 	*
 	* Méthode la plus importante du jeu : c'est elle qui s'occupe du déroulement général du jeu. Elle est appellée par la gameloop.
 	* Elle parcourt tous les éléments du jeu et s'occupe de les faire intéragir.
 	* Une méthode proceed() d'une classe peut se traduire par "c'est à moi de jouer, comment dois-je jouer ?".
 	* La méthode proceed du moteur de jeu ne s'occupe pas de bouger directement les entitées, elle appelle la méthode proceed de chacune des ses entitées.
	*/
	public void proceed(float timestep, ArrayList<KeyCode> keys)
	{
		Alien a;
		Tir t;
		Bonus b;
		/**
		* \brief Itérateus permettants de supprimer un élément d'une liste que l'on est en train de parcourir
		*/
		Iterator<Tir> it;
		Iterator<Alien> it2;
		Iterator<Bonus> it3;
		
		/** * \brief Ordonnée du dernier alien ajouté au jeu, qui sera forcément dans la ligne la plus haute */
		double y_last_alien;
		/**
		* \brief Incrémente le temps écoulé au chronomètre "level_up_time".
		* Si le chronomètre a dépassé les trente secondes, il se remet à zéro et le niveau affiché augmente de un.
		*/
		level_up_time+=timestep;
		if (level_up_time>30.0){
			level_up_time=0.0f;
			Text level = Game.getLevel();
			Integer newlvl = new Integer(level.getText());
			newlvl++;
			level.setText(newlvl.toString());
		}

		/** * \brief Met à zéro le vecteur déplacement du joueur */
		player.setDeplace(0.0);

		/** * \brief Si la flèche de gauche du clavier a été pressé depuis le dernier proceed(), décrémente de deux le vecteur de déplacement du joueur */
		if(keys.contains(KeyCode.LEFT))
			player.setDeplace(player.getDeplace() - 2.0);

		/** * \brief Si la flèche de droite du clavier a été pressé depuis le dernier proceed(), incrémente de deux le vecteur de déplacement du joueur */
		if(keys.contains(KeyCode.RIGHT))
			player.setDeplace(player.getDeplace() + 2.0);

		/** * \brief Si la touche espace du clavier a été pressé depuis le dernier proceed(), appelle la méthode shoot() du joueur */
		if(keys.contains(KeyCode.SPACE))
			player.shoot();

		/** * \brief Si la touche "P" du clavier a été pressé depuis le dernier proceed(), change l'écran de jeu par celui de pause */
		if(keys.contains(KeyCode.P))
			Game.setCurrentLevel(3);

		/**
		* \brief Appelle le procced() du joueur avec le temps écoulé depuis le dernier
		*/
		player.proceed(timestep);
		
		/**
		* \brief Pour chaque missile, appelle son proceed() et, si son indicateur de destruction s'est mis à 1, l'enlève de la liste
		*/
		it = tirs.iterator();
		while(it.hasNext()){
			t=it.next();
			t.proceed();
			if(t.getDestroy())
				it.remove();
		}
		
		/**
		* \brief Appelle le proceed() de toute la classe Alien avec le temps écoulé depuis le dernier
		*
		* Pour chaque alien, appelle son propre proceed()
		* Appelle la fonction collide() de l'alien avec le joueur, si elle renvoie vrai, retire toutes les vies du joueur et déclanche l'écran gameover.
		* Si l'indicateur de destruction de l'alien est à 1, le retire de la liste.
		*/
		Alien.proceed(timestep);
		it2 = aliens.iterator();
		while(it2.hasNext()){
			a=it2.next();
			a.proceed();
			if(a.collide(player)){
				Game.getVie().getChildren().remove(0,Game.getVie().getChildren().size());
				Game.setCurrentLevel(4);
				return;
			}
			if(a.getDestroy())
				it2.remove();
		}

		/** * \brief Met à jour l'ordonnée de l'alien le plus haut avec les nouvelles positions des aliens qui viennent de se déplacer (à nouveau, le dernier alien ajouté dans la liste sera forcément dans la ligne la plus haute).
		*
		* À chaque fois que cet ordonné dépasse 0.0, ajoute une nouvelle ligne d'alien 50 pixels plus haut. Les 50 représente l'écart qu'il y a entre chaque ligne d'alien.
		* Le fait qu'une nouvelle ligne d'alien soit ajoutée dès que l'ancienne dépasse 0.0 (la nouvelle ligne d'alien est dessinée hors-cadre) permet de faire en sorte que les aliens sorte graduellement du haut de l'écran plutôt que d'apparaître d'un coup.
		* La valeur d'index_alien est incrémentée à chaque nouvelle ligne pour faire en sorte que la prochain soit du type d'alien qui suit (la valeur revient à 0 au lieu de passer à 5 pour ne pas sortir de la liste d'images).
		* On vérifie également avant tout que la liste d'aliens n'est pas vide pour parer à toutes éventualité.
		* Si cette liste est vide, "y_last_alien" prend la valeur 14.0 car la hauteur des images d'alien est de 36px donc, puisque la nouvelle ligne sera dessiné à l'ordonnée "y_last_alien"-50 (-36px dans ce cas), cette nouvelle ligne apparait au joueur immédiatement après la disparition du dernier alien.
		*/
		if(aliens.size()>0){
			a=aliens.get(aliens.size()-1);
			y_last_alien= a.getLayoutY()+a.getTranslateY();
		}
		else 
			y_last_alien=14.0;
		if(y_last_alien>0.0){
			Alien.addnewline((byte)index_alien, y_last_alien-50);
			index_alien = index_alien==4 ? 0 : index_alien+1;
		}
		
		/**
		* \brief Pour chaque bonus affiché, appelle son proceed(), vérifie si une collision a eu lieu avec le joueur (si c'est le cas appelle la méthode active() du bonus et l'enlève de la liste) ou si le bonus n'a pas dépassé le cadre du jeu (l'enlève simplement de la liste dans ce cas).
		*/
		it3 = bonus.iterator();
		while(it3.hasNext()){
			b=it3.next();
			b.proceed();
			if(b.collide(player)){
				b.active();
				it3.remove();
			}
			else if(b.getLayoutX() + b.getTranslateX()>700)
				it3.remove();
		}
		/**
		* \brief Pour chaque bonus en activation, ajoute le temps écoulé depuis le dernier appel à cette fonction, et si ce temps dépasse le temps d'activation maximum des bonus, appelle la méthode inactive() de ce bonus et le retire de la liste
		*/
		it3 = active_bonus.iterator();
		while(it3.hasNext()){
			b=it3.next();
			b.addTimeStep(timestep);
			if(b.getTime()>Bonus.MAXTIME){
				b.inactive();
				it3.remove();
			}
		}
	}

	/**
 	* \brief Methode générale de detection de collision entre deux objets
	*
	* Le but est de considérer une collision comme une zone commune à deux entités.
	* La zone est parcourue une fois et les pixels des deux entités contenus dans cette zone sont comparés.
	* Si les deux pixels des deux entités ne sont pas transparant, alors il y une collision entre les deux entités
	*/
	public static Pair<Integer,Integer> collide(int x1, int y1, Image img1, int x2, int y2, Image img2)
	{
		/** * \brief Test de collision simple : on regarde si les zones des deux entités se chevauchent */
		if ((x1 < x2 + (int)img2.getWidth()) &&
   			(x1 + (int)img1.getWidth() > x2) &&
   			(y1 < y2 + (int)img2.getHeight()) &&
   			(y1 + (int)img1.getHeight() > y2))
		{
			/** * \brief Calcul des coordonnées absolues de la zone d'intersection */
			int c_x1  = Math.max(x1, x2);
			int c_x2  = Math.min(x1 + (int)img1.getWidth(), x2 + (int)img2.getWidth());
			int c_y1  = Math.max(y1, y2);
			int c_y2  = Math.min(y1 + (int)img1.getHeight(), y2 + (int)img2.getHeight());

			/** * \brief Chargement des pixels des deux images comparées */
			PixelReader pr1 = img1.getPixelReader();
			PixelReader pr2 = img2.getPixelReader();

			/** * \brief Lecture en y de la zone d'intersection*/
			for(int readY = c_y1; readY < c_y2 ; readY++)
			{
				/** * \brief Lecture en x de la zone d'intersection*/
				for(int readX = c_x1; readX < c_x2; readX++)
				{
					/** * \brief Récupération du pixel relatif à la première image */
					Color col1 = pr1.getColor((int)readX - x1, (int)readY - y1);
					if(col1.getOpacity() > 0.0)
					{
						/** * \brief Récupération du pixel relatif à la deuxième image */
						Color col2 = pr2.getColor(readX - x2, readY - y2);
						if(col2.getOpacity() > 0.0)
						{
							/** * \brief Collision détectée : on renvoit la position absolue de la collision */
							return new Pair<Integer,Integer>(readX, readY);
						}
					}
				}
			}
		}
		return null;
	}
	/**
 	* \brief Methode d'affichage de tout les objets du jeu.
 	*
 	* Un seul appel par frame
	*/
	public void render(GraphicsContext gc)
	{
		/** * \brief Dessine le fond dans le canvas en recouvrant tout ce qui a été dessiné avant */
		gc.drawImage(fond,0,0);
		/** * \brief Dessine le vaisseau du joueur à sa position actuelle */
		gc.drawImage(player.getImage(),player.getLayoutX()+player.getTranslateX(), player.getLayoutY()+player.getTranslateY());

		/** * \brief Dessine chaque bonus à sa positions actuelle */
		for(Bonus b : bonus)
			gc.drawImage(b.getImage(), b.getLayoutX()+b.getTranslateX(), b.getLayoutY()+b.getTranslateY());

		/** * \brief Ajoute une brillance qui sera appliqué aux prochains éléments dessinés pour des raison de visibilité */
    	gc.setEffect(new Glow());

		/** * \brief Appelle la fonction draw() de chaque missile, qui les dessineront à leurs positions actuelles */
		for(Tir t : tirs)
			t.draw(gc);

		/** * \brief Appelle la fonction draw() de chaque maison, qui la dessineront dans son état de destruction actuels */
		for(House h : houses)
			h.draw(gc);

		/** * \brief Change la brillance utilisé par une plus lumineuse (celui par défaut a une puissance de 0.3, on le passe à 0.5 pour les aliens uniquement) */
    	gc.setEffect(new Glow(0.5));
		/** * \brief Dessine chaque alien à sa positions actuelle */
		for(Alien a : aliens)
			gc.drawImage(a.getImage(), a.getLayoutX()+a.getTranslateX(), a.getLayoutY()+a.getTranslateY());
		/** * \brief Retire l'effet de brillance du GraphicContext pour qu'il ne soit pas appliqué au prochain draw() sur le image qui ne doivent pas en recevoir */
    	gc.setEffect(null);
	}
	/**
 	* \brief Méthode renvoyant la liste des missiles affichés
	*/
	public ArrayList<Tir> getTirs(){
		return this.tirs;
	}
	/**
 	* \brief Méthode renvoyant la liste des maisons
	*/
	public ArrayList<House> getHouses(){
		return this.houses;
	}
	/**
 	* \brief Méthode renvoyant la liste des aliens affichés.
	*/
	public ArrayList<Alien> getAliens(){
		return this.aliens;
	}
	/**
 	* \brief Méthode renvoyant la liste des bonus affichés.
	*/
	public ArrayList<Bonus> getBonus(){
		return this.bonus;
	}
	/**
 	* \brief Méthode renvoyant la liste des bonus actifs actuellement.
	*/
	public ArrayList<Bonus> getActiveBonus(){
		return this.active_bonus;
	}
	/**
 	* \brief Methode renvoyant l'objet joueur.
	*/
	public Player getPlayer(){
		return this.player;
	}
}