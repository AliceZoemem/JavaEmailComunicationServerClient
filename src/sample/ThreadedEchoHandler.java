package sample;

import java.net.Socket;

public class ThreadedEchoHandler implements Runnable {
    Socket s = new Socket();

    ThreadedEchoHandler(Socket socket){
        s = socket;
    }

    @Override
    public void run() {

    }
}
