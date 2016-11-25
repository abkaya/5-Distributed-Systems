/**
 * This is the logger for SystemY and is implemented via the Java API logging
 * 
 * Documentation is added later
 *
 */
package be.uantwerpen.group1.systemy.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DebugLogger {

	static Logger DebugLogger;
	FileHandler fileHandler;

	private DebugLogger() throws IOException {

		DebugLogger = Logger.getLogger("DebugLogger");
		DebugLogger.setLevel(Level.ALL);
		DebugLogger.setUseParentHandlers(false);
		fileHandler = new FileHandler("DebugLogger.log");
		DebugLogger.addHandler(fileHandler);
		fileHandler.setFormatter(new SimpleFormatter());
	}

	/**
	 * This checks if the logger is created, if not it try's to create the logger
	 * @return the logger
	 */
	private static Logger getLogger() {
		if (DebugLogger == null) {
			try {
				new DebugLogger();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return DebugLogger;
	}

	/**
	 * This is the log method that can be used in the whole project 
	 * (check documentation in the top of this class for the usage of the logger)
	 * @param level: this parameter defines the level of the logging
	 * @param msg: this parameter is the message
	 */
	public static void log(Level level, String msg) {
		getLogger().log(level, msg);
		System.out.println(level + " > " + msg);
	}

}
