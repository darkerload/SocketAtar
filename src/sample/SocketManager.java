package sample;

import javafx.application.Platform;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketManager  implements ISocketManager{

    private String socketMode;
    private String address;
    private int port;
    private Socket socket;
    private ServerSocket serverSocket;
    private ISocketManager messageManager;
    private Thread socketServerThread;
    private Thread socketReaderThread;
    private boolean ready = false;



    public SocketManager(String socketMode, String address, int port){
        this.socketMode = socketMode;
        this.address = address;
        this.port = port;
    }



    public void Connect(Controller.MessageManager messageManager) {
            try {
                this.messageManager = messageManager;

                socketServerThread = new SocketServerThread();
                socketServerThread.start();

                socketReaderThread = new SocketReaderThread();
                socketReaderThread.start();


            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    public void Emit(String data) {
           PrintWriter writer;
            try {
                writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                writer.println(data);
            } catch (Exception ex) {
                ex.printStackTrace();
        }
    }

    public boolean IsSocketClosed(){
        if (socket == null)
            return true;
        return socket.isClosed();
    }

    private synchronized void SocketReady() {
        ready = true;
        notifyAll();
    }

    private synchronized void WaitSocketReady() {
        while (!ready) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
            }
        }
    }

    class SocketServerThread extends Thread{
        @Override
        public void run(){
            try{
                if (socketMode.equals("Client")){
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(address, port));
                }else if (socketMode.equals("Server")){
                    serverSocket = new ServerSocket(port);
                    serverSocket.setReuseAddress(true);
                    socket = serverSocket.accept();
                }
            }catch (Exception ex){
              ex.printStackTrace();
              socket = null;
            }
            finally {
                SocketReady();
            }
        }
    }


    class SocketReaderThread extends Thread {
        @Override
        public void run() {
           WaitSocketReady();

            if (socket != null && !socket.isClosed()) {
                onClosed(false);
            }else{
                onClosed(true);
                return;
            }

            BufferedReader reader;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true){
                    String input = reader.readLine();
                    if (input != null) {
                        onRecevie(input);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                onClosed(true) ;
            }
        }
    }

    public void Close() {
        try {
            if (socket != null && !socket.isClosed()){
                socket.close();
            }
            onClosed(true);
        } catch (IOException ex) {
           ex.printStackTrace();
        }
    }


    @Override
    public void onRecevie(String message) {
        Platform.runLater(() -> {
            messageManager.onRecevie(message);
        });
    }

    @Override
    public void onClosed(boolean status) {
        Platform.runLater(() -> {
            messageManager.onClosed(status);
        });
    }
}


