/**
 * \file Movit.c
 * \brief Classe de gestion des mobiles
 */
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
/**
* \class Movit extends ImageView
*/
public class Movit extends ImageView{
	/** * \brief vecteur de déplacement horizontal */
	protected double vecX;
	/** * \brief  vecteur de déplacement vertical */
	protected double vecY;
	/**
 	* \brief Constructeur d'un mobile
	*
	* Appelle le constructeur d'ImageView avec l'image donnée en paramètre (ImageView est un objet JavaFX contenant une image affichable (et réglable).
	* Initialise les vecteurs de déplacement et positionne l'image avec les valeurs passés en argument.
	*/
	public Movit(Image img, double vecx, double vecy, double x, double y){
		super(img);
		this.vecX=vecx;
		this.vecY=vecy;
		this.relocate(x,y);
	}
	/**
 	* \brief Méthode créant un tableau d'images
	*
	* Cette méthode a été créée pour éviter les répétitions dans notre code car que ce soit pour les aliens ou les bonus, on doit créer une liste d'images de tous les types d'aliens/bonus possibles.
	* Ces images sont dans les deux cas stockées dans un dossier homonyme (on leur accole un identifiant numérique pour les distinguer).
	* Pour récupérer la liste il nous suffit d'appeler cette fonction en lui précisant un nombre d'images et le nom du dossier les contenant, cette fonction remplira la liste avec les bons chemins.
	*/
	public static Image[] imglist(String s, byte n){
		Image[] list = new Image[n];
		for(int i=0; i<n; i++)
			list[i]=new Image("Ressources/"+s+"/"+s+(i+1)+".png");
		return list;
	}
}