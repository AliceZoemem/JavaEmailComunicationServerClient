package sample;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import sample.old.Email;

import java.io.*;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

public class ControllerServer extends Thread implements Initializable {
    private final ModelServer model;
    private final Server pageClient;
    private Integer rowGrid = 0;
    ServerSocket serverSocket;
    List<Socket> listClientSocket = new ArrayList<>();
    List<String> listClientAccount = new ArrayList<>();

    public ControllerServer(ModelServer model, Server pageClient) {
        this.model = model;
        this.pageClient = pageClient;
    }

    public void printStringLogGui(String s){
        Text logText = new Text(s);
        rowGrid++;

        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                logsList.add(logText, 0, rowGrid, 10, 1);
            }
        });
    }

    @FXML
    public GridPane logsList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        printStringLogGui("SERVER ATTIVO");
        new Thread(()-> {
            try {
                openServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public synchronized void openServer() throws IOException{
        serverSocket = new ServerSocket(8189);

        while (true) {
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
                listClientSocket.add(clientSocket);
                //printStringLogGui("Connection accepted: " + clientSocket);
            } catch (IOException e) {
                if(serverSocket.isClosed()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException(
                        "Error accepting client connection", e);
            }

            new Thread(
                    new WorkerRunnable(
                            clientSocket, "Multithreaded Server", this.model)
            ).start();

        }

    }

    public class WorkerRunnable implements Runnable{

        protected Socket clientSocket = null;
        protected String serverText   = null;
        ModelServer model;

        public WorkerRunnable(Socket clientSocket, String serverText, ModelServer model) {
            this.clientSocket = clientSocket;
            this.serverText = serverText;
            this.model = model;
        }

        public synchronized void run() {
            try {
                InputStream input  = clientSocket.getInputStream();
                BufferedReader brinp;
                brinp = new BufferedReader(new InputStreamReader(input));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                String line;
                send(clientSocket,out, "HTTP/1.1 200 OK");
                while (true) {
                    try {
                        line = brinp.readLine();
                        System.out.println("line: " + line);
                        if(line.equals("EXIT")){
                            Integer idClient = listClientSocket.indexOf(clientSocket);
                            if(idClient > -1 && idClient < listClientSocket.size()){
                                listClientSocket.remove(listClientSocket.get(idClient));
                            }
                            if(idClient > -1 && idClient < listClientAccount.size()){
                                listClientAccount.remove(listClientAccount.get(idClient));
                            }
                            this.model.removeCliente(idClient);
                            clientSocket.close();
                            printStringLogGui("Connection closed: " + clientSocket);
                            break;
                        }else if(line.indexOf("REMOVE_") > -1){
                            String linetoRemove = line.replace("REMOVE_", "");
                            Integer idClient = listClientSocket.indexOf(clientSocket);
                            this.model.removeFromFile(linetoRemove, listClientAccount.get(idClient));
                            this.model.openFileAndFillEmailList(idClient, listClientAccount.get(idClient));
                            invioListaEmailCliente(clientSocket, out);
                            break;
                        }else if(line.indexOf("CHANGED_") > -1){
                            String accountToChange = line.replace("CHANGED_", "");
                            Integer idClient = listClientSocket.indexOf(clientSocket);
                            System.out.println("accountToChange " + accountToChange );
                            System.out.println("idClient " + idClient );
                            System.out.println("momentsocket " + clientSocket );
                            for (Socket clientSocket : listClientSocket){
                                System.out.println("csock " + clientSocket );
                            }
                           /*
                            listClientSocket.set(idClient, listClientSocket.get(listClientSocket.size() -1));
                            listClientSocket.remove(listClientSocket.size() -1);
                            this.model.openFileAndFillEmailList(idClient, listClientAccount.get(idClient));
                            invioListaEmailCliente(clientSocket, out);
                            printStringLogGui("Connection changed for: " + clientSocket + " account ( "+ accountToChange +" )");*/
                            break;
                        }else{
                            try{
                                String newAccount = "";
                                newAccount = convertFromJson(line, newAccount.getClass());
                                if(listClientAccount.indexOf(newAccount) == -1){
                                    listClientAccount.add(newAccount);
                                    this.model.addCliente(new ModelServer.ClientConnected(newAccount));
                                    invioListaEmailCliente(clientSocket, out);
                                    printStringLogGui("Connection accepted: " + clientSocket + " account ( "+ newAccount +" )");
                                }
                            }catch (Exception e){
                                try{
                                    Email email = new Email(new ArrayList<>(), new String(), new String(), new String());
                                    email = convertFromJson(line, email.getClass());
                                    String sendEmail = addEmail(email);
                                    if(sendEmail == "OK"){
                                        send(clientSocket, out, sendEmail);
                                        printStringLogGui("Client " + clientSocket + " dell' account " + email.getMittente() +" ha inviato una email a " + email.getIdDestinatari().toString());
                                        invioListaEmailCliente(clientSocket, out);
                                    }else{
                                        send(clientSocket, out, sendEmail);
                                    }
                                }catch (Exception ex){
                                    System.out.println("close connection + exception " + ex);
                                }
                            }
                        }

                    }catch (Exception ex){
                        System.out.println("ex: " + ex);
                        Integer idClient = listClientSocket.indexOf(clientSocket);
                        if(idClient > -1 && idClient < listClientSocket.size()){
                            listClientSocket.remove(listClientSocket.get(idClient));
                        }
                        if(idClient > -1 && idClient < listClientAccount.size()){
                            listClientAccount.remove(listClientAccount.get(idClient));
                        }
                        this.model.removeCliente(idClient);
                        clientSocket.close();
                        printStringLogGui("Connection closed: " + clientSocket);
                        break;
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void invioListaEmailCliente(Socket c, PrintWriter o){
        List<String> emails = this.model.listaClienti().get(listClientSocket.indexOf(c)).listEmails;
        send(c, o, "[");
        for (String em:
                emails) {
            send(c, o, em);
        }
        send(c, o, "]");
    }

    public <E> String convertToJson(E obj){
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public <E> E convertFromJson(String json, Class c){
        Gson gson = new Gson();
        return gson.fromJson(json, (Type) c);
    }

    public void send(Socket clientSocket, PrintWriter out, String msg) {
         try{
             System.out.println("send " + msg);
             out.flush();
             out.println(msg);

             Thread.sleep(100);
        }catch ( InterruptedException e){
            e.printStackTrace();
        }
    }

    public String addEmail(Email e){
        Boolean destinatariEsistenti = false;
        String testoErroreInvio = "";
        try{
            for (String account:
                    e.getIdDestinatari()) {
                String filename = "file_" + account +".txt";
                Path p = Paths.get(this.model.directory + filename);
                try (BufferedWriter writerDes = Files.newBufferedWriter(p, StandardOpenOption.APPEND)) {
                    destinatariEsistenti = true;
                } catch (IOException ioe) {
                    testoErroreInvio += "ERRORE invio email a " + account + " casella inesistente; " + System.getProperty("line.separator");
                }
            }

            if(destinatariEsistenti){
                String jsonEmail = convertToJson(e);

                Path p = Paths.get(this.model.directory + "file_" + e.getMittente().toString() +".txt");
                try (BufferedWriter writer = Files.newBufferedWriter(p, StandardOpenOption.APPEND)) {
                    writer.write(jsonEmail + System.getProperty("line.separator"));
                    writer.flush();
                } catch (IOException ioe) {
                    return "ERRORE scrittura email del mittente";
                }
                this.model.openFileAndFillEmailList(this.listClientAccount.indexOf(e.getMittente()), e.getMittente());

                for (String dest:
                        e.getIdDestinatari()) {
                    p = Paths.get(this.model.directory + "file_" + dest +".txt");
                    try (BufferedWriter writer = Files.newBufferedWriter(p, StandardOpenOption.APPEND)) {
                        writer.write(jsonEmail + System.getProperty("line.separator"));
                        writer.flush();
                    } catch (IOException ioe) {
                        return "ERRORE scrittura email dell' destinatario " + dest;
                    }
                    if(this.listClientAccount.indexOf(dest) != -1){
                        this.model.openFileAndFillEmailList(this.listClientAccount.indexOf(dest), dest);
                        Socket s = this.listClientSocket.get(this.listClientAccount.indexOf(dest));
                        PrintWriter oNew = new PrintWriter(s.getOutputStream(), true);
                        oNew.println("EMAIL RICEVUTA " + jsonEmail);

                        invioListaEmailCliente(s, oNew);
                    }
                }

                if(this.listClientAccount.indexOf(e.getMittente()) != -1){
                    Socket s = this.listClientSocket.get(this.listClientAccount.indexOf(e.getMittente()));
                    PrintWriter output = new PrintWriter(s.getOutputStream(), true);
                    send(s, output, "EMAIL INVIATA");
                }

                return "OK";
            }else {
                return testoErroreInvio;
            }
        }catch (Exception ex){
            System.out.println("Exception send email " + ex);
            return "ERRORE INVIO EMAIL";
        }
    }
}
