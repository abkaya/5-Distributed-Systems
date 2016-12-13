/**
 * This is the logger for SystemY and is implemented via the Java API logging
 * 
 * Documentation is added later
 *
 */
package be.uantwerpen.group1.systemy.log_debug;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class DebugLogger {

	static Logger SystemyLogger;
	FileHandler fileHandler;

	private DebugLogger() throws IOException {

		SystemyLogger = Logger.getLogger("DebugLogger");
		SystemyLogger.setLevel(Level.ALL);
		SystemyLogger.setUseParentHandlers(false);
		fileHandler = new FileHandler("DebugLogger.log");
		SystemyLogger.addHandler(fileHandler);
		fileHandler.setFormatter(new SimpleFormatter());
	}

	/**
	 * This checks if the logger is created, if not it try's to create the logger
	 * @return the logger
	 */
	private static Logger getLogger() {
		if (SystemyLogger == null) {
			try {
				new DebugLogger();
			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
			}
		}
		return SystemyLogger;
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
