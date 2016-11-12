package be.uantwerpen.group1.systemy.networking;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import be.uantwerpen.group1.systemy.logging.SystemyLogger;
import be.uantwerpen.group1.systemy.nameserver.NameServer;

/**
 * TCP class. A distinction between client and server is initially made 
 * by the constructor used.
 * It should hopefully function as our TCP API and be as reusable as possible
 * and serve our needs.
 * 
 * Please make a differentiation between service/port pairs.
 * Suggested:
 * 
 * TCP_xfer : 2001
 * 
 * @author Abdil Kaya
 *
 */
public class TCP {

	private static String logName = TCP.class.getName() + " >> ";

	/* sockets */
	private ServerSocket serverSocket = null;

	/**
	 * clientSocket is only used for client methods. Server methods use their local clientSocket
	 * due to multi-threaded applications on server side
	 */
	private Socket clientSocket = null;

	/*
	 * I presume it's best to declare variables in their respective methods if these methods are to be used in multiple threads at once.
	 */

	/* used for text stream transfer */

	/**
	 * The TCP constructor used by a SERVER, accepting client sockets
	 * 
	 * @param port : port on server side
	 * @param serverSocket : server socket passed by function caller
	 */
	public TCP(String host, int port) {
		try {
			this.serverSocket = new ServerSocket(port, 0, InetAddress.getByName(host));
			SystemyLogger.log(Level.INFO, logName + "- Opened server socket on IP: " + serverSocket.getInetAddress()
					+ ", port (local) :" + port);
			//System.out.println(
			//		"- Opened server socket on IP: " + serverSocket.getInetAddress() + ", port (local) :" + port);
		} catch (IOException e1) {
			SystemyLogger.log(Level.SEVERE, logName + "Could not start the Server Socket on " + host + ":" + port);
			//System.err.println("Could not start the Server Socket on " + host + ":" + port);
		}
	}

	/**
	 * The TCP constructor used by the CLIENT seeking to establish
	 * a connection with the host server
	 * 
	 * @param host : the host running the server socket to set TCP connection with
	 * @param port : the port on which to establish the connection
	 */
	public TCP(int port, String host) {
		try {
			this.clientSocket = new Socket(host, port);
			SystemyLogger.log(Level.INFO, logName + "- Opened client socket on IP : " + clientSocket.getInetAddress()
					+ ", port :" + clientSocket.getLocalPort());
			SystemyLogger.log(Level.INFO,
					logName + "- Socket connection established with server : " + host + ", port : " + port);
			//System.out.println("- Opened client socket on IP : " + clientSocket.getInetAddress() + ", port :"
			//		+ clientSocket.getLocalPort());
			//System.out.println("- Socket connection established with server : " + host + ", port : " + port);
		} catch (IOException e) {
			SystemyLogger.log(Level.SEVERE,
					logName + "clientsocket exception : could not connect to " + host + ":" + port);
			//System.err.println("clientsocket exception : could not connect to " + host + ":" + port);
		}
	}

	/**
	 * This method listens for clients to send them files
	 * and handles each connection on a separate thread.
	 * 
	 * The flow of this connection is as follows:
	 * 1. receive file name from client
	 * 2. send file size back to client {so the client can keep track of completion %s}
	 * 3. send file if 1 and 2 are met.
	 * 
	 * It would be wise to distinguish services by ports.
	 * A proposition of service/port pairs: 
	 * DNS : 2000
	 * TCP_file_xfer : 2001
	 * ..
	 * .
	 * 
	 */
	public void listenToSendFile() {
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				// Running methods in a new thread in java 8
				new Thread(() -> {
					sendFile(clientSocket);
					close(clientSocket);
				}).start();

			} catch (IOException e) {
				SystemyLogger.log(Level.SEVERE, logName + "Could not accept a client socket connection");
				//System.err.println("Could not accept a client socket connection");
			}

		}
	}

	/**
	 * Server method : Receive the filename which the client requests to be sent back
	 * 
	 * @param clientSocket
	 * @return
	 */
	private File receiveFileName(Socket clientSocket, File fileToSend) {
		try {
			InputStream is = clientSocket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String fileName = br.readLine();
			if (new File("tosend\\" + fileName).isFile())
				fileToSend = new File("tosend\\" + fileName);
			else
				return null;
			SystemyLogger.log(Level.INFO, logName + "- Client requests file with name: " + fileName);
			//System.out.println("- Client requests file with name: " + fileName);
			return fileToSend;

		} catch (Exception e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
			//e.printStackTrace();
			return null;
		}
	}

	/**
	 * plain receive tcp text stream method. 
	 * 
	 * You do not have to know who you're receiving data from, but your sender
	 * will have to know. We're using this method on a particular port for
	 * nodes or servers to respond with their unknown IP address.
	 *
	 * If you're trying to receive the DNS its IP address on the node client,
	 * you'll have to use this method with the SERVER constructor, otherwise it will NOT
	 * work.
	 * 
	 * @return
	 */
	public String receiveText() {

		String text = null;
		try {

			Socket clientSocket = this.serverSocket.accept();
			while (true) {
				try {
					InputStream is = clientSocket.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					text = br.readLine();
					SystemyLogger.log(Level.INFO, logName + "DEBUG 1  ");
					//System.out.println("DEBUG 1  ");
				} catch (NumberFormatException | IOException nfe) {
					SystemyLogger.log(Level.SEVERE, logName + "TCP/receiveText exception - Could not receive txt");
				}
				SystemyLogger.log(Level.INFO, logName + "- Client responded with Text : " + text);
				//System.out.println("- Client responded with Text : " + text);
				close(clientSocket);
				return text;
			}
		} catch (Exception e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
			//e.printStackTrace();
			close(clientSocket);
			return text;
		}

	}

	/**
	 * Plain txp send text stream method. This public method will have to be
	 * used with the CLIENT constructor. It is necessary to know the recipient.
	 * 
	 * @param textToSend
	 */
	public void sendText(String textToSend) {
		try {
			OutputStream os = this.clientSocket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(textToSend);
			SystemyLogger.log(Level.INFO, logName + "Sending text : " + textToSend);
			//System.out.println("Sending text : " + textToSend);
			bw.flush();
			close(clientSocket);
		} catch (Exception e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
			//e.printStackTrace();
		}
	}

	/**
	 * Server method : This send the file size back to the client.
	 * 
	 * @param clientSocket
	 * @return
	 */
	private boolean sendFileSize(Socket clientSocket, File fileToSend) {
		String sizeToSend;
		try {
			sizeToSend = Long.toString(fileToSend.length()) + "\n";
			// sizeToSend = "0\n";
			OutputStream os = clientSocket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);
			bw.write(sizeToSend);
			System.out
					.println("- The size of the requested file sent to client: " + Long.toString(fileToSend.length()));
			bw.flush();
			return true;
		} catch (Exception e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
			//e.printStackTrace();
			return false;
		}
	}

	/**
	 * Server method : Once the preliminary checks such as filename and -size are completed, send file.
	 * @param clientSocket
	 */
	private void sendFile(Socket clientSocket) {
		File fileToSend = null;
		fileToSend = receiveFileName(clientSocket, fileToSend);
		if (fileToSend != null && sendFileSize(clientSocket, fileToSend)) {
			try {

				byte[] mybytearray = new byte[(int) fileToSend.length()];
				FileInputStream fis = new FileInputStream(fileToSend);
				BufferedInputStream bis = new BufferedInputStream(fis);
				bis.read(mybytearray, 0, mybytearray.length);
				OutputStream os = clientSocket.getOutputStream();
				System.out.println("- Sending : " + fileToSend + "(" + mybytearray.length + " bytes)");
				os.write(mybytearray, 0, mybytearray.length);
				os.flush();
				os.close();
				bis.close();
				System.out.println("*- File uploaded. Job well done.");
			} catch (Exception e) {
				SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
				//e.printStackTrace();
			}
		}

	}

	/**
	 * Client method : sends the fileName it wished to request from the server node
	 * offering this file.
	 * 
	 * @param fileName
	 * @return boolean : true if succeeded, false if failed
	 */
	private boolean sendFileName(String fileName) {
		try {
			OutputStream os = this.clientSocket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			BufferedWriter bw = new BufferedWriter(osw);
			String sendMessage = fileName + "\n";
			bw.write(sendMessage);
			bw.flush();
			System.out.println("- Requesting following file from the server : " + fileName);
			return true;
		} catch (Exception e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
			//e.printStackTrace();
			return false;
		}
	}

	/**
	 * Client method : expects the fileSize from the server
	 * 
	 * @return int : file size
	 */
	private int receiveFileSize() {
		int fileSize = 0;
		try {
			InputStream is = this.clientSocket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			try {
				fileSize = Integer.parseInt(br.readLine());
			} catch (NumberFormatException nfe) {
				System.err.println("Could not receive the file size");
			}
			System.out.println("- Server responded with filesize : " + fileSize);
			return fileSize;
		} catch (Exception e) {
			SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
			//e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Client method : Once the connection is established (constructor), set the process to receive
	 * the requested file in motion.
	 * 
	 * Process flow : Send fileName > receive fileSize > receive file
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	public void receiveFile(String fileName) {
		int fileSize;
		int bytesRead = 0;
		int current = 0;
		int progress = 0;
		int prevProgress = 0;
		InputStream inputStream = null;

		if (sendFileName(fileName)) {
			fileSize = receiveFileSize();
			try {

				// A buffer, used to splice and receive data in chunks
				byte[] byteArray = new byte[1024];
				System.out.println("- Downloading file: ");

				// reading file from socket
				try {
					inputStream = this.clientSocket.getInputStream();
				} catch (SocketException se) {
					SystemyLogger.log(Level.SEVERE, logName + se.getMessage());
					//se.printStackTrace();
				}
				FileOutputStream fileOutputStream = new FileOutputStream(fileName);
				BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

				// once EOF is reached, the read method will return -1, so we'll use
				// that as the condition in order to read and write data in chunks
				// of 1kB
				do {
					current += bytesRead;

					// print progress every 10%. // using print and \r is nice in a system console, but fills the // eclipse console
					progress = ((int) Math.floor((100 * current) / fileSize));
					if (progress % 10 == 0 && prevProgress != progress) {
						prevProgress = progress;
						System.out.println("- Progress: " + progress + "%");
					}

					try {
						bufferedOutputStream.write(byteArray, 0, bytesRead);
					} catch (SocketException se) {
						SystemyLogger.log(Level.SEVERE, logName + se.getMessage());
						//se.printStackTrace();
					}

				} while ((bytesRead = inputStream.read(byteArray)) >= 0);

				bufferedOutputStream.flush();
				bufferedOutputStream.close();
				inputStream.close();
				this.clientSocket.close();
				
				System.out.println("*-Requested file is downloaded");

			} catch (IOException e) {
				SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
				//e.printStackTrace();
			}
		}

	}

	/**
	 * 
	 * Close the clientSocket after performing whichever task performed.
	 * It is a private method, so the user of this "API" should not have to worry about it.
	 * 
	 * @param clientSocket
	 */
	private void close(Socket clientSocket) {
		{
			System.out.println("*- Closing client socket on port: " + clientSocket.getPort());
			try {
				clientSocket.close();
			} catch (IOException e) {
				SystemyLogger.log(Level.SEVERE, logName + e.getMessage());
				//e.printStackTrace();
			}
		}
	}

}
