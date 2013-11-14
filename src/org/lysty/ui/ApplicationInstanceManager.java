package org.lysty.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class ApplicationInstanceManager {

	private static final String NO_ARGS_STR = "$$NOARGS$$";

	private static ApplicationInstanceListener subListener;

	/** Randomly chosen, but static, high socket number */
	public static final int SINGLE_INSTANCE_NETWORK_SOCKET = 44431;

	/** Must end with newline */
	public static final String SINGLE_INSTANCE_SHARED_KEY = "$$NewInstance$$"
			+ System.getProperty("line.separator");

	public static Logger log = Logger
			.getLogger(ApplicationInstanceManager.class);

	/**
	 * Registers this instance of the application.
	 * 
	 * @return true if first instance, false if not.
	 */
	public static boolean registerInstance(String[] args) {
		// returnValueOnError should be true if lenient (allows app to run on
		// network error) or false if strict.
		boolean returnValueOnError = true;
		// try to open network socket
		// if success, listen to socket for new instance message, return true
		// if unable to open, connect to existing and send new instance message,
		// return false
		try {
			final ServerSocket socket = new ServerSocket(
					SINGLE_INSTANCE_NETWORK_SOCKET, 10,
					InetAddress.getLocalHost());
			log.debug("Listening for application instances on socket "
					+ SINGLE_INSTANCE_NETWORK_SOCKET);
			Thread instanceListenerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					boolean socketClosed = false;
					while (!socketClosed) {
						if (socket.isClosed()) {
							socketClosed = true;
						} else {
							try {
								Socket client = socket.accept();
								BufferedReader in = new BufferedReader(
										new InputStreamReader(client
												.getInputStream()));
								String message = in.readLine();
								if (message == null)
									message = "";
								if (SINGLE_INSTANCE_SHARED_KEY.trim().equals(
										message.trim())) {
									log.debug("Shared key matched - new application instance found");
									String newArgsStr = in.readLine();
									if (newArgsStr == null)
										newArgsStr = "";
									String[] newArgs = newArgsStr.split("\\|");
									if (NO_ARGS_STR.equals(newArgs[0])) {
										fireNewInstance(new String[0]);
									} else {
										fireNewInstance(newArgs);
									}
								}
								in.close();
								client.close();
							} catch (IOException e) {
								socketClosed = true;
							}
						}
					}
				}
			});
			instanceListenerThread.start();
			// listen
		} catch (UnknownHostException e) {
			log.error(e.getMessage(), e);
			return returnValueOnError;
		} catch (IOException e) {
			log.debug("Port is already taken.  Notifying first instance.");
			try {
				Socket clientSocket = new Socket(InetAddress.getLocalHost(),
						SINGLE_INSTANCE_NETWORK_SOCKET);
				OutputStream out = clientSocket.getOutputStream();
				out.write(SINGLE_INSTANCE_SHARED_KEY.getBytes());
				StringBuilder builder = new StringBuilder();
				if (args == null || args.length == 0) {
					builder.append("|").append(NO_ARGS_STR);
				} else {
					for (int i = 0; i < args.length; i++) {
						builder.append("|").append(args[i]);
					}
				}
				builder.append(System.getProperty("line.separator"));
				String newArgsStr = builder.substring(1).toString();
				out.write(newArgsStr.getBytes());
				out.close();
				clientSocket.close();
				log.debug("Successfully notified first instance.");
				return false;
			} catch (UnknownHostException e1) {
				log.error(e.getMessage(), e);
				return returnValueOnError;
			} catch (IOException e1) {
				log.error("Error connecting to local port for single instance notification");
				log.error(e1.getMessage(), e1);
				return returnValueOnError;
			}

		}
		return true;
	}

	public static void setApplicationInstanceListener(
			ApplicationInstanceListener listener) {
		subListener = listener;
	}

	private static void fireNewInstance(String[] args) {
		if (subListener != null) {
			subListener.newInstanceCreated(args);
		}
	}
}
