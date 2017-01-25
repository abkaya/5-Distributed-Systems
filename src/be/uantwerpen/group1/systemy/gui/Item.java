package be.uantwerpen.group1.systemy.gui;

import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.node.Node;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Class to represent one row of GUI buttons for 1 item
 * @author Robin
 */
public class Item {
	
	private static String logName = Node.class.getName() + " >> ";
	
	String fileName;
	Label label;
	Button btnOpen;
	Button btnDelete;
	Button btnDeleteLocal = null;
	
	/**
	 * Constructor
	 * @param fileName: String of filename
	 * @param local: true for local file, else false
	 */
	Item(String fileName, Boolean local) {
		
		this.fileName = fileName;
		label = new Label(fileName);
		
		btnOpen = new Button();
        btnOpen.setText("Open");
        btnOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	SystemyLogger.log(Level.INFO, logName + "Button Open '" + fileName + "' pressed");
            	Node.UIOpen(fileName);
            }
        });
        
        btnDelete = new Button();
        btnDelete.setText("Delete");
        btnDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	SystemyLogger.log(Level.INFO, logName + "Button Delete '" + fileName + "' pressed");
            	Node.UIDelete(fileName);
            }
        });
        
        if (local) {
	        btnDeleteLocal = new Button();
	        btnDeleteLocal.setText("Delete Local");
	        btnDeleteLocal.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	            	SystemyLogger.log(Level.INFO, logName + "Button Delete Local '" + fileName + "' pressed");
	                Node.UIDeleteLocal(fileName);
	            }
	        });
        }
        
	}
	
	/**
	 * Returns row of JavaFX buttons
	 * @return HBox: Styled output in JavaFX
	 */
	public HBox getRow() {
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(5, 5, 0, 5));
		hbox.setSpacing(10);
		
		HBox.setHgrow(label, Priority.ALWAYS);
	    label.setMaxWidth(Double.MAX_VALUE);
	    hbox.getChildren().add(label);
	    
	    HBox.setHgrow(btnOpen, Priority.ALWAYS);
	    btnOpen.setMinWidth(80);
	    btnOpen.setMaxWidth(80);
	    hbox.getChildren().add(btnOpen);
	    
	    HBox.setHgrow(btnDelete, Priority.ALWAYS);
	    btnDelete.setMinWidth(100);
	    btnDelete.setMaxWidth(100);
	    hbox.getChildren().add(btnDelete);
	    
	    if (btnDeleteLocal != null) {
		    HBox.setHgrow(btnDeleteLocal, Priority.ALWAYS);
		    btnDeleteLocal.setMinWidth(120);
		    btnDeleteLocal.setMaxWidth(120);
		    hbox.getChildren().add(btnDeleteLocal);
	    } else {
	    	Label emptyLabel = new Label("");
	    	HBox.setHgrow(emptyLabel, Priority.ALWAYS);
	    	emptyLabel.setMinWidth(120);
	    	emptyLabel.setMaxWidth(120);
	    	hbox.getChildren().add(emptyLabel);
	    }
	    
	    return hbox;
	}

	/**
	 * Get method for fileName
	 * @return String: fileName
	 */
	public String getFileName() {
		return fileName;
	}
	
}
