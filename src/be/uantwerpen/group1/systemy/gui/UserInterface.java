package be.uantwerpen.group1.systemy.gui;

import java.util.ArrayList;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Class to represent the user interface
 * @author Robin
 */
public class UserInterface extends Application {
	
    private static ArrayList<Item> files = new ArrayList<Item>();
    
    @Override
    public void start(Stage primaryStage) {
        
        VBox vbox = new VBox();
        HBox administrativeHBox = Administrative.getRow();
        administrativeHBox.setAlignment(Pos.TOP_RIGHT);
		vbox.getChildren().add(administrativeHBox);
        for (Item file : files) {
			HBox hbox = file.getRow();
			vbox.getChildren().add(hbox);
		}
		
		StackPane root = new StackPane();
        root.getChildren().add(vbox);
        
        primaryStage.setTitle("SystemY");
        primaryStage.setScene(new Scene(root, 512, 600));
        primaryStage.show();
    }
    
    /**
     * Method to add an item to the display list
     * 
     * @param fileName: String representation of file
     * @param local: Boolean true if local file
     */
    public static void add(String fileName, Boolean local) {
    	files.add(new Item(fileName, local));
    }

}
