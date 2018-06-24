package com.invaders;
/*
 * \file Game.java
 * \brief Classe Main
 */
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollPane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.beans.property.SimpleIntegerProperty;
import java.util.ArrayList;
import javafx.scene.media.AudioClip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
* \class Game extend Application
*/
public final class Game extends Application
{
	/** * \brief Identifiant qui indique quel est l'écran actuellement affiché */
	private static SimpleIntegerProperty currentlevel=currentlevelProperty();
	/** * \brief Liste qui stocke les différents écrans du jeu */
	private static ArrayList<Region> levels = new ArrayList<>();
	/** * \brief Son déclenché lorsqu'on appuie sur un bouton */
	private static AudioClip son_boutton;
	/** * \brief Son qui est joué lorsque le joueur est sur l'écran du menu */
	private static AudioClip son_menu;
	/** * \brief Conteneur qui contient les ImageView des cœurs représentants la vie du joueur */
	private static FlowPane vie;
	/** * \brief Objet Text qui contient le score du joueur */
	private static Text score;
	/** * \brief Objet Text qui contient le level auquel le joueur est arrivé */
	private static Text level;
	/** * \brief Objet dans lequel sont dessinés les éléments dynamiques du jeu : joueur, ennemis, bonus, maisons, missiles */
	private static GraphicsContext gc;
	/** * \brief Liste des touches que le joueur a pressées */
	private static ArrayList<KeyCode> keys;
	/** * \brief Moteur du jeu */
	private static Motor motor;
	/** * \brief Largeur de la fenêtre */
	static final double WIDTH=800;
	/** * \brief Hauteur de la fenêtre */
	static final double HEIGHT=800;
	/** * \brief General logger for the whole app */
	private static final Logger logger = LogManager.getLogger("Detailed");
	/** * \brief Levels name for use with logger */
	private static final String[] levelsName = new String[] {"Menu", "Règles", "Jeu", "Pause", "Game Over"};
	/**
 	* \brief Méthode principale d'initialisation du jeu
	*/
	@Override
	public void start(Stage jeu)
	{
		logger.debug("Initialisation du jeu");
        /* * \brief Empêche le redimensionnement de la fenêtre */
		jeu.setResizable(false);
		jeu.setTitle("Infinite Invaders");
		son_boutton = new AudioClip(new File(getClass().getResource("Sons/Bouton.wav").getFile()).toURI().toString());
		son_menu = new AudioClip(new File(getClass().getResource("Sons/MainMenu.wav").getFile()).toURI().toString());
        /* * \brief Fait en sorte que la musique du menu se joue en boucle */
		son_menu.setCycleCount(AudioClip.INDEFINITE);
		son_menu.play();
		levels=new ArrayList<>();
		/* * \brief Ajoute le menu principal à la liste des écrans du jeu */
		levels.add(menu());
		/* * \brief Y ajoute la page de règle */
		levels.add(regles());
		/* * \brief Y ajoute l'écran de jeu */
		levels.add(maingame());
		/* * \brief Y ajoute l'écran de pause */
		levels.add(pause());
		/* * \brief Y ajoute l'écran de game over */
		levels.add(gameover());
		keys = new ArrayList<>();
		motor = new Motor(gc, keys);
		/* * \brief Construit la fenêtre de WIDTH*HEIGHT contenant le menu */
		Scene scenejeu = new Scene(levels.get(0),WIDTH, HEIGHT);
		/* * \brief Charge la feuille de style qui sera utilisé pour l'apparence des boutons */
		scenejeu.getStylesheets().add(getClass().getResource("Images/Buttons.css").toExternalForm());
		AudioClip son_gameOver = new AudioClip(new File(getClass().getResource("Sons/GameOver.wav").getFile()).toURI().toString());
		
		/*
		* Ajoute à l'objet "currentlevel" des événements dynamiques qui se produisent lorsque la valeur de "currentlevel" change. Ainsi les réglages nécessaires en fonction de l'écran qu'on veut afficher se feront automatiquement.
		* Si on vient de quitter l'écran de pause ou de gameover, il faut retirer du conteneur de celui-ci de l'écran du jeu pour que ce dernier puisse à nouveau être affiché directement par la fenêtre (seul un conteneur qui n'est contenu par aucun autre peut être utilisé comme conteneur "racine").
		* Change le conteneur "racine" pour lui donner l'écran correspondant à la nouvelle valeur.
		* Si le nouvel écran est celui du menu ou des règles, lance la musique d'ambiance du menu si ce n'est pas déjà fait, sinon arrête la musique.
		* Si le nouvel écran est celui du jeu, lance le moteur du jeu.
		* Si le nouvel écran est celui de pause ou de gameover, arrête le moteur du jeu puis ajoute l'écran de jeu au conteneur de cet écran (pour que la pause ou le gameover viennent s'afficher par-dessus le jeu).
		* Enfin, si le nouvel écran est celui du game over, lance le son du gameover et lance la fonction newgame().
		*/
		currentlevel.addListener((observable, oldvalue, newvalue) -> 
		{
			if (newvalue.intValue() < levelsName.length)
				logger.debug("Passage à l'écran de {}", levelsName[newvalue.intValue()]);
			else
				logger.error("Demande de changement d'écran pour l'écran inexistant {}", newvalue);
			if(oldvalue.intValue() > 2)
				((Pane)levels.get(oldvalue.intValue())).getChildren().remove(levels.get(2));
			scenejeu.setRoot(levels.get(newvalue.intValue()));

			if(newvalue.intValue() < 2){
				if(!son_menu.isPlaying())
					son_menu.play();
			}
			else{
				if(son_menu.isPlaying())
					son_menu.stop();
				if(newvalue.intValue() == 2)
					motor.start();
				else{
					motor.stop();
					((Pane)levels.get(newvalue.intValue())).getChildren().add(0,levels.get(2));
					if(newvalue.intValue() > 3){
						son_gameOver.play();
						newgame();
					}
				}
			}
		});
		jeu.setScene(scenejeu);

		/*
		* Fait en sorte que lorsqu'une touche du clavier est appuyée, celle-ci soit ajoutée dans la liste des touches qui sont passées au moteur.
		* Si une touche du clavier est relâchée, elle est au contraire enlevée de cette même liste.
		* Une exception est faite pour que la touche "Échap", lorsqu'elle est pressée, fasse revenir au menu principal en jouant le son qui correspond à l'appuie d'un bouton, sauf si le jeu affiche actuellement l'écran de jeu (dans ce cas il faudra passer par la touche "P" qui met en pause).
		*/
		scenejeu.setOnKeyPressed(event -> {
            KeyCode key = event.getCode();
            if(key==KeyCode.ESCAPE && currentlevel.get()!=2){
                logger.trace("Touche {} pressée", key.getName());
                son_boutton.play();
                currentlevel.set(0);
            }
            else if(!keys.contains(key)) {
                logger.trace("Touche {} pressée", key.getName());
                keys.add(key);
            }
        });
		scenejeu.setOnKeyReleased(event -> {
            KeyCode key = event.getCode();
            logger.trace("Touche {} relachée", key.getName());
            keys.remove(key);
        });
		jeu.show();
	}

	/**
 	* \brief Methode de création de l'écran de menu.
	*
	* Crée un conteneur pour le menu et lui ajoute une image de fond et un petit texte servant de copyright.
	* Puis lui ajoute un autre conteneur qui possèdera quatres boutons :
	* Le boutons "Continuer" qui, lorsqu'il est pressé, change l'écran du menu actuellement affiché par l'écran du jeu qui avait été désactivé.
	* Le bouton "Nouvelle Partie" qui, s'il est pressé, lance la musique correspondante au démarrage d'une nouvelle partie, redonne au joueur le nombre de vies maximum s'il lui en manquait, met le score à 0 et le niveau à 1, appelle la fonction newgame() seulement si une nouvelle partie avait déjà été lancée précédemment, remplace l'écran de menu affiché par celui du jeu et enfin active le bouton "continuer".
	* Le bouton "Règles" qui, lorsqu'il est pressé, change l'écran de menu actuellement affiché par l'écran des règles.
	* Enfin, le bouton "Quit" quitte proprement le jeu si l'on clique dessus.
	*/
	private StackPane menu(){
	    logger.debug("Création de l'écran de menu");
        StackPane root = new StackPane();
		ImageView fond = new ImageView(new Image(getClass().getResource("Images/MainMenu.png").toExternalForm()));
		fond.setFitHeight(HEIGHT);
        fond.setPreserveRatio(true);
        fond.setSmooth(false);

		HBox menu = new HBox();
		/* * \brief Marge inter-elements */
		menu.setSpacing(80);
		menu.setAlignment(Pos.CENTER);
		menu.setMaxHeight(64);

        FocusedButton buttonContinue = new FocusedButton("Continuer");
		buttonContinue.setFont(Font.font("Futura", FontWeight.BOLD, 24));
		buttonContinue.setPrefHeight(35);
		buttonContinue.setOnAction(boutonact(()->currentlevel.set(2)));
        buttonContinue.setDisable(true);

		AudioClip son_newGame = new AudioClip(new File(getClass().getResource("Sons/StartPartie.wav").getFile()).toURI().toString());
		FocusedButton buttonNew = new FocusedButton("Nouvelle Partie");
		buttonNew.setPrefHeight(50);
		buttonNew.setFont(Font.font("Futura", FontWeight.BOLD, 24));
		buttonNew.setOnAction(event -> {
		    logger.trace("Clic sur le bouton {}", ((Button)event.getSource()).getText());
            son_newGame.play();
            Image life = new Image(getClass().getResource("Images/life.png").toExternalForm());
            for(int i=vie.getChildren().size();i<5;i++)
                vie.getChildren().add(new ImageView(life));
            score.setText("0");
            level.setText("1");
            if(!buttonContinue.isDisable())
                newgame();
            currentlevel.set(2);
            buttonContinue.setDisable(false);
        });

		FocusedButton buttonRules = new FocusedButton("Règles");
		buttonRules.setFont(Font.font("Futura", FontWeight.BOLD, 20));
		buttonRules.setPrefHeight(20);
		buttonRules.setOnAction(boutonact(()->currentlevel.set(1)));

		FocusedButton buttonQuit = new FocusedButton("Quitter");
		buttonQuit.setFont(Font.font("Futura", FontWeight.BOLD, 20));
		buttonQuit.setPrefHeight(20);
		buttonQuit.setOnAction(boutonact(Platform::exit));

		menu.getChildren().addAll(buttonNew, buttonContinue, buttonRules, buttonQuit);
		Text disclaimer = new Text("© Malidoca 2016\nVersion 0.3");
		disclaimer.setTextAlignment(TextAlignment.RIGHT);
		disclaimer.setFill(Color.WHITE);

		root.getChildren().addAll(fond, menu,disclaimer);
		root.setAlignment(Pos.BOTTOM_CENTER);
		StackPane.setAlignment(disclaimer,Pos.TOP_RIGHT);
		StackPane.setMargin(disclaimer,new Insets(5,5,0,0));
		return root;
	}
	/**
 	* \brief Methode de création du objet Text avec un style particulier.
	*
	* Fonction faisant des économies de lignes en parametrant le style des textes de l'écran de règles qui nécessitent tous les même réglages :
	* une largeur maximum de 400px, une taille de 20px, une couleur blanche et enfin un alignement central.
	*/
	private Text text(String s)
	{
		Text ret = new Text(s);
		ret.setWrappingWidth(400);
		ret.setFont(new Font(20));
		ret.setFill(Color.WHITE);
		ret.setTextAlignment(TextAlignment.CENTER);
		return ret;
	}
	/**
 	* \brief Methode de création de l'écran de règles.
	*
	* Crée un conteneur de couleur noire et de type "ScrollPane", c'est à dire que s'il est plus grand que la fenêtre dans lequel il se trouve, il créera des barres de défilement qui permettront de le voir intégralement.
	* Dans ce conteneur sont ajoutées successivement les images et textes qui permettent d'illustrer les règles du jeu.
	*/
	private ScrollPane regles()
	{
        logger.debug("Création de l'écran de règles");
		ScrollPane root = new ScrollPane();
		root.setStyle("-fx-background-color:#000000");
		VBox vBox = new VBox();
		vBox.setStyle("-fx-background-color:#000000");
		vBox.setAlignment(Pos.TOP_CENTER);
     	vBox.setPrefWidth(780);
		root.setPrefSize(800, 800);

		vBox.getChildren().add(text("\nBienvenue sur Infinite Invaders !\n\nVous embarquez dans un vaisseau destiné à stopper une invasion alienne progressant en direction de la planète Terre.\n\nLorsque le jeu commence, vous êtes donc dans le vaisseau ci-contre.\n"));
		vBox.getChildren().add(new ImageView(getClass().getResource("Images/Vaisseau-Space.png").toExternalForm()));
		vBox.getChildren().add(text("\nVotre but est d'abattre les ennemis en leurs tirant dessus.\n\nChaque ennemi abattu vous rapporte des points. Mais attention, à chaque enemi détruit, leur progression en direction de la Terre accelère.\n"));
		vBox.getChildren().add(new ImageView(getClass().getResource("Images/Monster/MonsterList.png").toExternalForm()));
		vBox.getChildren().add(text("\nPour sauver la Terre, vous avez pour mission d'abattre les ennemis avant qu'ils n'arrivent tout en bas, dans le cas contraire, vous perdez et l'avenir de la Terre est compromis.\n\nPour vous aider durant votre quête, vous pouvez vous protéger derrière les maisons qui sont juste au-dessus de vous. Mais attention, elles se détruisent un peu plus à chaque collision avec un missile.\n"));
		vBox.getChildren().add(new ImageView(getClass().getResource("Images/Maison-Space.png").toExternalForm()));
		vBox.getChildren().add(text("\nSuite à une mauvaise programmation de l'itinéraire des aliens, il est possible qu'à leur mort, certains laissent tomber des objets pouvant vous être très utile afin de vous aider à lutter contre eux, ne les ratez pas !\n\nMais faites attention, l'ennemi est malin, il peut aussi vous tendre des pièges destinés à vous ralentir...\n"));
		HBox hBox = new HBox(10);
		hBox.setAlignment(Pos.TOP_CENTER);
		hBox.getChildren().add(new ImageView(getClass().getResource("Images/Bonus/Bonus1.png").toExternalForm()));
		hBox.getChildren().add(new ImageView(getClass().getResource("Images/Bonus/Bonus2.png").toExternalForm()));
		hBox.getChildren().add(new ImageView(getClass().getResource("Images/Bonus/Bonus3.png").toExternalForm()));
		hBox.getChildren().add(new ImageView(getClass().getResource("Images/Bonus/Bonus4.png").toExternalForm()));
		hBox.getChildren().add(new ImageView(getClass().getResource("Images/Bonus/Bonus5.png").toExternalForm()));
		vBox.getChildren().add(hBox);

		vBox.getChildren().add(text("\nLes aliens étant très déterminés à prendre le contrôle de la Terre et asservir les Humains, leur quête risque d'être très longue voir... infinie...\n\nRéussirez vous à sauver notre planète ? Notre avenir est entre vos mains !\n\nTOUCHES :\n\n[<-] [->] : se déplacer\n[ESPACE] : tirer\n[P] : pause\n[ESC] : retour au menu\n"));

		root.setContent(vBox);

		return root;
	}
	/**
 	* \brief Methode de création de l'écran de jeu.
	*
	* Crée un conteneur qui contiendra les trois conteneurs qui composent une partie en cours.
	* La barre du haut "top" est composé d'une image de fond, et de deux textes avec leur réglages de position et de couleur. Le premier texte indique le niveau actuel du jeu (qui commence à 1) et le deuxième texte indique le score (qui commence à 0).
	* Ces deux textes sont ensuite attribués aux deux attributs "score" et "level" de cette classe, pour pouvoir y accéder et les modifier facilement ailleurs dans le code.
	* La barre du bas "bottom" est composée d'une image de fond et d'un autre conteneur qui sera rempli par cinq images de cœur représentant la vie du joueur.
	* Ce dernier conteneur sera lié à l'attribut "vie" de cette classe afin de pouvoir y accédant facilement pour ajouter/retirer des coeur.
	* Enfin, au centre se trouve un Canvas "main" qui sera utilisé pour accueillir un GraphicsContext dans lequel on va pouvoir dessiner tous les objets dynamiques du jeu (joueur, ennemis, bonus, maisons, missiles).
	* Ce GraphicsContext sera également stocké en tant qu'attribut "gc" pour pouvoir dessiner facilement dedans.
	*/
	private BorderPane maingame(){
        logger.debug("Création de l'écran de jeu principal");
		BorderPane root = new BorderPane();

		ImageView top_fond = new ImageView(new Image(Game.class.getResource("Images/HUD-Haut.png").toExternalForm()));
		score=new Text("0");
		level=new Text("1");
		score.setStyle("-fx-background-color:#FFFFFF");
		level.setFont(Font.font("Futura",37));
		score.setFont(Font.font("Futura",37));
		level.setFill(Color.WHITE);
		score.setFill(Color.INDIGO);
		AnchorPane top = new AnchorPane(top_fond, level, score);
		AnchorPane.setLeftAnchor(level,123.0);
        AnchorPane.setRightAnchor(score,25.0);
        AnchorPane.setTopAnchor(level,0.0);
        AnchorPane.setTopAnchor(score,0.0);
		root.setTop(top);

		ImageView bottom_fond = new ImageView(new Image(Game.class.getResource("Images/HUD-Bas.png").toExternalForm()));
		FlowPane lives = new FlowPane();
		AnchorPane bottom = new AnchorPane(bottom_fond, lives);
		AnchorPane.setRightAnchor(lives,28.0);
		AnchorPane.setBottomAnchor(lives,8.0);
		root.setBottom(bottom);

		lives.setHgap(6);
		lives.setAlignment(Pos.TOP_RIGHT);
		Image life = new Image(Game.class.getResource("Images/life.png").toExternalForm());
		for(int i=0;i<5;i++)
			lives.getChildren().add(new ImageView(life));
		vie=lives;

		Canvas main = new Canvas(WIDTH,700);
		root.setCenter(main);
		gc = main.getGraphicsContext2D();
		return root;
	}
	/**
 	* \brief Methode créent de l'écran de pause
	*
	* L'écran de pause est constitué d'un cadre blanc translucide à bordure noire, de taille 350x500px.
	* À l'intérieur, on y met un texte "PAUSE" en haut et deux boutons : un bouton "Reprendre" qui fera revenir à l'écran du jeu  et un bouton "Menu qui fait lui revenir au manu principal.
	* Pour des raisons esthétiques et pour que le joueur puisse voir où en est sa partie avant de la reprendre, l'écran de jeu devra être affiché derrière tout ce cadre.
	* C'est pourquoi l'écran de jeu est ajouté en tant "enfant" de l'écran de pause à chaque fois qu'on veut afficher ce dernier et qu'il en est retiré sinon.
	*/
	private StackPane pause(){
        logger.debug("Création de l'écran de pause");
		StackPane root = new StackPane();

		VBox menu = new VBox();
		menu.setPadding(new Insets(50,0,100,0)); /* * \brief Marge vbox/elements */
		menu.setSpacing(20); /* * \brief Marge inter-elements */
		menu.setStyle("-fx-background-color: rgb(255,255,255,0.4)");
		menu.setAlignment(Pos.CENTER);
		menu.setMaxSize(350,500);
		Text p = new Text("PAUSE");
		p.setFont(Font.font("Futura", FontWeight.BOLD, 40));
		menu.setBorder(new Border(new BorderStroke(Color.BLACK,BorderStrokeStyle.SOLID,CornerRadii.EMPTY,BorderStroke.DEFAULT_WIDTHS)));
		
		FocusedButton buttonResume = new FocusedButton("Reprendre");
		buttonResume.setPrefHeight(50);
		buttonResume.setFont(Font.font("Futura", FontWeight.BOLD, 24));
		buttonResume.setOnAction(boutonact(()->currentlevel.set(2)));
        
        FocusedButton buttonMenu = new FocusedButton("Menu");
		buttonMenu.setFont(Font.font("Futura", FontWeight.BOLD, 24));
		buttonMenu.setPrefHeight(35);
		buttonMenu.setOnAction(boutonact(()->currentlevel.set(0)));
		
		menu.getChildren().addAll(p, buttonResume, buttonMenu);
		root.getChildren().add(menu);
		return root;
	}
	/**
 	* \brief Methode créent l'écran de gameover
	*
	* L'écran de gameover est constitué d'un conteneur contenant un grand texte blanc indiquant "GAMEOVER".
	* De la même manière que pour l'écran de pause, l'écran de jeu est situé en dessous de ce texte si l'écran de gameover est affiché.
	*/
	private StackPane gameover(){
        logger.debug("Création de l'écran de game over");
		StackPane root = new StackPane();
		Text over = new Text("GAMEOVER");
		over.setFont(Font.font("Futura", FontWeight.BOLD, 60));
		over.setFill(Color.WHITE);
		root.getChildren().add(over);
		return root;
	}
	/**
 	* \brief Methode de réinitialisation d'une partie
	*
	* Un tout nouveau moteur est attribué à notre classe "Game" et le bouton "Continuer" du menu principal et désactivé.
	*/
	static void newgame(){
        logger.debug("Réinitialisation du jeu");
		motor = new Motor(gc, keys);
		((HBox)((Pane)levels.get(0)).getChildren().get(1)).getChildren().get(1).setDisable(true);
	}
	/**
 	* \brief Methode initialisant l'indicateur de l'écran sur lequel on se trouve
	*/
	private static SimpleIntegerProperty currentlevelProperty(){
		if (currentlevel == null) currentlevel = new SimpleIntegerProperty(0);
		return currentlevel;
	}

	/**
 	* \brief Methode changeant la valeur de l'écran sur lequel on se trouve
	 */
	static void setCurrentLevel(int n){
		currentlevel.set(n);
	}
		
	/**
 	* \brief Methode renvoyant l'objet "motor" qui représente le moteur du jeu
	 */
	static Motor getMotor(){
		return motor;
	}
	/**
 	* \brief Methode renvoyant le conteneur des vies du joueur
	 */
	static FlowPane getVie(){
		return vie;
	}
	/**
 	* \brief Methode retournant l'objet Text qui contient le score du joueur
	 */
	static Text getScore(){
		return score;
	}
	/**
 	* \brief Methode retournant l'objet Text qui contient le niveau auquel est arrivé le joueur
	 */
	static Text getLevel(){
		return level;
	}
	/**
 	* \brief Classe qui permet de créer des boutons avec des réglages personnalisés
	*
	* Cette classe hérite de la classe "Button" de JavaFx. Elle s'en différencie par le fait qu'on peut actionner ces boutons par la touche "Entrée" et non uniquement par la touche "Espace".
	* Tous les boutons de notre jeu utilisent cette classe.
	*/
	private class FocusedButton extends Button {
    	FocusedButton(String text){
    	    super(text);
    	    bindFocusToDefault();
    	}
    	private void bindFocusToDefault(){
    	    defaultButtonProperty().bind(focusedProperty());
    	}
	}
	/**
 	* \brief interface permettant de stocker des actions
	*
	* À la création d'un objet Effet, on doit redéfinir sa méthode act(), ce qui permet de pouvoir y stocker une ou plusieurs actions différentes à accomplir pour chaque Effet.
	* Il suffira alors d'appeler la méthode act() de cet Effet pour que ces actions se produisent.
	*/
 	private interface Effet{
		void act();
	}
	/**
 	* \brief Méthode de création d'un manager d'événement
	*
	* Cette méthode nous sert à créer un manager d'évènement qui est un objet dont la méthode handle() est appelée à chaque fois que l'évènement auquel il est lié se produit.
	* On lui entre un Effet (une suite d'action) en argument et il se crée en redéfinissant sa méthode handle() jouant le son associé à un bouton. Il accomplit ensuite tout ce qui est stocké dans le nouvel Effet.
	* Ceci nous permet d'éviter un certain nombre de répétitions dans notre code en ayant pas à repréciser à chaque bouton créé que l'on veut qu'il joue le son qui lui est associé lorsqu'il s'active (excepté pour le bouton "Nouvelle Partie" qui n'utilise pas cette méthode boutonact() car il doit jouer un autre son).
	*/
	private static EventHandler<ActionEvent> boutonact(Effet e){
		return event -> {
            logger.trace("Clic sur le bouton {}", ((Button)event.getSource()).getText());
            son_boutton.play();
            e.act();
        };
	}
	/**
	 * \brief Methode renvoyant le logger global
	 */
	static Logger getLogger() { return logger; }
	/**
	 * \brief Methode renvoyant le niveau de l'écran courant
	 */
	static int getCurrentlevel() {
		return currentlevel.get();
	}

	/**
 	* \brief Methode de lancement general du jeu.
	*/
	public static void main(String[] args){
		launch(args);
	}
}
