package be.uantwerpen.group1.systemy.gui;

import java.util.ArrayList;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.node.Node;
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
	
	private static String logName = Node.class.getName() + " >> ";
	
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
    	SystemyLogger.log(Level.INFO, logName + "Added " + fileName + " to GUI");
    }
    
    /**
     * Method to delete an item from the display list
     * 
     * @param fileName: String representation of file
     */
    public static void remove(String fileName) {
    	Boolean found = false;
    	for (int i=0; i<files.size(); i++) {
    		if (files.get(i).getFileName() == fileName) {
    			files.remove(i);
    			i--;
    			found = true;
    		}
    	}
    	if (found)
    		SystemyLogger.log(Level.INFO, logName + "Removed " + fileName + " from GUI");
    	else
    		SystemyLogger.log(Level.WARNING, logName + fileName + " not found in GUI file list");
    }

}
