package be.uantwerpen.group1.systemy.gui;

import java.util.logging.Level;

import be.uantwerpen.group1.systemy.log_debug.SystemyLogger;
import be.uantwerpen.group1.systemy.node.Node;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * Class to represent one row of GUI buttons holding administrative buttons
 * @author Robin
 */
public class Administrative {
	
	private static String logName = Node.class.getName() + " >> ";
	
	/**
	 * Returns row of JavaFX administrative buttons
	 * @return HBox: Styled output in JavaFX
	 */
	public static HBox getRow() {

		HBox hbox = new HBox();
		hbox.setPadding(new Insets(5, 5, 0, 5));
		hbox.setSpacing(10);
		
		Button btnShutDown = new Button();
        btnShutDown.setText("Shutdown Node");
        btnShutDown.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            	SystemyLogger.log(Level.INFO, logName + "Button Shutdown pressed");
            	Node.UIShutdown();
            }
        });
	    HBox.setHgrow(btnShutDown, Priority.ALWAYS);
	    btnShutDown.setMinWidth(140);
	    btnShutDown.setMaxWidth(140);
	    hbox.getChildren().add(btnShutDown);
	    
	    return hbox;
	    		
	}
	
}
