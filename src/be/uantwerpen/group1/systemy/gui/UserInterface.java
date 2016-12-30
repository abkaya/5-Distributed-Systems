package be.uantwerpen.group1.systemy.gui;

//import java.util.ArrayList;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.node.Node;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Class to represent the user interface
 * @author Robin
 */
public class UserInterface extends Application {
	
	private static String logName = Node.class.getName() + " >> ";
	
	private static ObservableList<Item> files = FXCollections.observableArrayList();
	private static StackPane root = new StackPane();
	
//    private static ArrayList<Item> files = new ArrayList<Item>();
    
    @Override
    public void start(Stage primaryStage) {
    	
    	/*
    	 * List listener
    	 */
    	files.addListener(new ListChangeListener<Item>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Item> arg0) {
				update();
			}
    	});
    	
    	/*
    	 * Close button listener
    	 */
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                Node.UIShutdown();
            }
        });
        
    	/*
    	 * Display
    	 */
        update();
        primaryStage.setTitle("SystemY");
        primaryStage.setScene(new Scene(UserInterface.root, 512, 600));
        primaryStage.show();
    }
    
    private static void update() {
    	Platform.runLater(() -> {
	        UserInterface.root.getChildren().clear();
	        VBox vbox = new VBox();
	        HBox administrativeHBox = Administrative.getRow();
	        administrativeHBox.setAlignment(Pos.TOP_RIGHT);
			vbox.getChildren().add(administrativeHBox);
	        for (Item file : files) {
				HBox hbox = file.getRow();
				vbox.getChildren().add(hbox);
			}
	        UserInterface.root.getChildren().add(vbox);
		});
    }
    
    /**
     * Method to add an item to the display list
     * 
     * @param fileName: String representation of file
     * @param local: Boolean true if local file
     */
    public static void add(String fileName, Boolean local) {
    	Boolean found = false;
    	for (int i=0; i<files.size(); i++)
    		if (files.get(i).getFileName() == fileName)
    			found = true;
    	if (found) {
    		SystemyLogger.log(Level.WARNING, logName + "file '" + fileName + "' already in GUI");
    	} else {
    		files.add(new Item(fileName, local));
    		SystemyLogger.log(Level.WARNING, logName + "Added '" + fileName + "' to GUI");
    	}
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
