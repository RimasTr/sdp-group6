package balle.io.reader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * SocketVisionReader: Captures input from vision system.
 * 
 * Usage: - Create an instance. - Add the listener.
 * 
 * Contains an inner class SocketThread that will update listeners with world
 * information.
 * 
 * NOTE: Reader implements AbstractVisionReader
 */
public class SocketVisionReader extends Reader {

    public static final int    PORT           = 28546;
	// these values are not set in stone
	// pick them as you wish
	private static final double TRESHOLD = 1.2;
	private static final double DTRESHOLD = 5;
    public static final String ENTITY_BIT     = "E";
    public static final String PITCH_SIZE_BIT = "P";
    public static final String GOAL_POS_BIT   = "G";
	private double x1 = 0, x2 = 0, x3 = 0, y1 = 0, y2 = 0, y3 = 0, d1 = 0,
			d2 = 0;


    public SocketVisionReader() {
        new SocketThread().start();
    }

    class SocketThread extends Thread {

        @Override
        public void run() {

            try {
                ServerSocket server = new ServerSocket(PORT);

                while (true) {
                    Socket socket = server.accept();

                    System.out.println("Client connected");

                    Scanner scanner = new Scanner(new BufferedInputStream(
                            socket.getInputStream()));

                    while (scanner.hasNextLine()) {
                        try {
                            parse(scanner.nextLine());
                        } catch (java.util.NoSuchElementException e) {
                            System.out.println("No input from camera!");
                        }
                    }

                    System.out.println("Client disconnected");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void parse(String line) {

        // Ignore Comments
        if (line.charAt(0) != '#') {

            String[] tokens = line.split(" ");

            if (tokens[0].equals(ENTITY_BIT)) {
				/*
				 * if(coordinateCount<MAXCOORDINATECOUNT) {
				 * 
				 * } coordinateCount++;
				 */
				
				double x1p=Double.parseDouble(tokens[1]);
				double y1p=Double.parseDouble(tokens[2]);
				double d1p=Double.parseDouble(tokens[3]);

				double x2p=Double.parseDouble(tokens[4]);
				double y2p=Double.parseDouble(tokens[5]);
				double d2p=Double.parseDouble(tokens[6]);

				double x3p=Double.parseDouble(tokens[7]);
				double y3p=Double.parseDouble(tokens[8]);

				if (Math.abs(x1p - x1) > TRESHOLD && x1p > 0)
					x1 = x1p;
				if (Math.abs(y1p - y1) > TRESHOLD && y1p > 0)
					y1 = y1p;
				if (Math.abs(d1p - d1) > DTRESHOLD && d1p > 0)
					d1 = d1p;

				if (Math.abs(x2p - x2) > TRESHOLD && x2p > 0)
					x2 = x2p;
				if (Math.abs(y2p - y2) > TRESHOLD && y2p > 0)
					y2 = y2p;
				if (Math.abs(d2p - d2) > DTRESHOLD && d2p > 0)
					d2 = d2p;

				if (Math.abs(x3p - x3) > TRESHOLD && x3p > 0)
					x3 = x3p;
				if (Math.abs(y3p - y3) > TRESHOLD && y3p > 0)
					y3 = y3p;
				//System.out.println("Updating (entity): " + line);
				System.out.println("1st team (" + x1 + "," + y1 + "," + d1
						+ ")");
				System.out.println("2nd team (" + x2 + "," + y2 + "," + d2
						+ ")");
				System.out.println("Ball     (" + x3 + "," + y3 + ")");

				propagate(x1, y1, d1, x2, y2, d2, x3, y3,
						Long.parseLong(tokens[9]));

            } else if (tokens[0].equals(PITCH_SIZE_BIT)) {
                // System.out.println("Updating (pitch size): " + line);
                propagatePitchSize(Double.parseDouble(tokens[1]),
                        Double.parseDouble(tokens[2]));
            } else if (tokens[0].equals(GOAL_POS_BIT)) {
            	// form: G XMIN XMAX YMIN YMAX
            	propagateGoals(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]),
            					Double.parseDouble(tokens[3]), Double.parseDouble(tokens[4]) );

            } else {
                // System.err.println("Could not decode: " + line);
            }

        }

    }

	public static void main(String[] args) {
		SocketVisionReader svr = new SocketVisionReader();
	}

}
