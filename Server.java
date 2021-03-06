import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Represents a server listening to a port and handles 
 *
 * @author Philip Wingemo
 * @version 1.0
 * @since   2022-06-31 
 */
public class Server implements Runnable {
  private static String CLIENT_CONNECTED_MSG = "CLIENT CONNECTED: ";
  private static String CLIENT_DISSCONNECTED_MSG = "CLIENT DISSCONNECTED: ";
  private ConcurrentHashMap < SocketAddress, Client > clientMap;
  private BlockingQueue < String > broadcastQueue;
  private ServerSocket serverSocket;
  private Socket clientSocket;
  private Executor executor;

  /**
   * Constructs a server with a specified number of threads and port
   * @param port server port number
   * @param threads specified number of threads
   */
  public Server(int port, int threads) throws IOException {
    serverSocket = new ServerSocket(port);
    clientMap = new ConcurrentHashMap < SocketAddress, Client > ();
    executor = Executors.newFixedThreadPool(threads);
    this.executor.execute(new Broadcaster(clientMap, broadcastQueue));
  }

  /**
   * Adds client to ConcurrentHashMap
   * @param socket client socket
   */
  private void addClient(Socket socket) {
    this.broadcast(CLIENT_CONNECTED_MSG + socket.getRemoteSocketAddress());
    this.clientMap.put(socket.getRemoteSocketAddress(), new Client(socket));
  }

  /**
   * Remove client from ConcurrentHashMap
   * @param client client socket
   */
  public void removeClient(Client client) {
    this.clientMap.remove(client.getSocket().getRemoteSocketAddress());
    this.broadcast(CLIENT_DISSCONNECTED_MSG + client.getSocket().getRemoteSocketAddress());
  }

  public void broadcast(String message) {
    try {
      broadcastQueue.put(message);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    try {
      while (true) {
        this.clientSocket = serverSocket.accept();
        addClient(clientSocket);
      }
    } catch (Exception exception) {
      System.out.println(exception);
    }
  }
}
