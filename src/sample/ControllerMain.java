package sample;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import sample.old.Email;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.URL;
import java.util.*;

public class ControllerMain implements Initializable {
    private final ModelClient model;
    private final Client pageClient;
    private String currentPage;
    private String oldCurrentPage;
    private boolean startListEmail;

    private Socket socket;
    BufferedReader input;
    PrintWriter out;
    boolean socketBegin;

    List<String> emailsClient = new ArrayList<>();

    public ControllerMain(ModelClient model, Client pageClient, String currentPage, Socket s) {
        this.model = model;
        this.pageClient = pageClient;
        this.currentPage = currentPage;
        this.socket = s;
    }

    public void setSocket() {
        //ogni 10minuti
        /*new Timer().schedule(
                new TimerTask() {
                    @Override
                    public synchronized void run() {
                        new Thread(()-> {
                            if(socketBegin == false && !currentPage.equals("start") && !currentPage.equals("dash")){
                                synchronized(this)
                                {
                                    System.out.println("ping");
                                    try {
                                        socket = new Socket("127.0.0.1", 8189);
                                        System.out.println(socket);
                                        socketFunctionalities(true);
                                    } catch (IOException e) {
                                        System.out.println("e " + e);
                                        e.printStackTrace();
                                    }
                                }
                            }else {
                                socketBegin = false;
                                socketFunctionalities(false);
                            }
                        }).start();

                    }
                }, 0, 600000);*/


        socketFunctionalities(false);
    }

    public void socketFunctionalities(Boolean changed){
        ControllerMain that = this;
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println(socket.toString());
            if (currentPage.equals("start")) {
                send( socket, out, that.model.email().get());
            }
            if(changed){
                System.out.println("CAMBIO");
                send( socket, out, "CHANGED_" + that.model.email().get());
            }
            String msg = "";
            msg = input.readLine();
            while (msg != null){
                if(msg != null){
                    System.out.println("msgg " + msg);
                    if(msg.indexOf("[") == 0){
                        startListEmail = true;
                        that.emailsClient = new ArrayList<>();
                    }else if(msg.indexOf("]") == 0){
                        startListEmail = false;
                        if(currentPage.equals("receivedEmails") || currentPage.equals("sentEmails")){
                            initializeEmails(true);
                        }
                    }else if(msg.indexOf("ERRORE ") > -1 && that.errorText != null){
                        that.errorText.setText(msg);
                    }else if(msg.indexOf("EMAIL INVIATA") > -1 && that.errorText != null){
                        that.errorText.setText(msg);
                    }else if(msg.indexOf("EMAIL RICEVUTA") > -1){
                        Email email = new Email(new ArrayList<>(), new String(), new String(), new String());
                        email = convertFromJson(msg.replace("EMAIL RICEVUTA ", ""), email.getClass());
                        openAlert(email);
                    }else{
                        if(startListEmail){
                            Email email = new Email(new ArrayList<>(), new String(), new String(), new String());
                            email = convertFromJson(msg, email.getClass());
                            if(email != null && that.emailsClient.indexOf(msg) == -1){
                                that.emailsClient.add(msg);
                            }else{
                                System.out.println("no way: " + msg);
                                System.out.println("no way: " + that.errorText);
                            }
                        }
                    }
                }
                msg = input.readLine();
            }
        }catch (Exception ex){
            System.out.println("server closed " + ex);
            try {
                pageClient.waiting();
            }catch (Exception exe){

                System.out.println("exe " + exe);
            }

        }
    }

    public void send(Socket clientSocket, PrintWriter out, String msg) {
        try{
            System.out.println("send " + msg);

            out.println(msg);
            out.flush();

            Thread.sleep(100);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public void openAlert(Email e){
        System.out.println("openalert");
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Informazione");
                if(e != null){
                    alert.setHeaderText("Nuova Email Ricevuta da " + e.getMittente());
                }
                alert.showAndWait();
            }
        });

    }

    public void setCurrentPage(String page){
        this.oldCurrentPage = this.currentPage;
        this.currentPage = page;
    }

    public void createRowEmail(Email finalEmail, Integer[] gridRow, String jsonEmail){
        var that = this;
        Platform.runLater(new Runnable(){
            @Override
            public synchronized void run() {
                Integer row = 0;
                GridPane gridEmail = new GridPane();
                gridEmail.setPadding(new Insets(10, 0, 10, 0));
                gridEmail.setStyle("-fx-border-color: black ;-fx-border-width: 2;");
                if(currentPage.equals("receivedEmails")){
                    gridEmail.add(new Text("Mittente : " + finalEmail.getMittente().toString()), 0, row);
                }
                gridEmail.add(new Text("Destinatari : " + finalEmail.getIdDestinatari().toString()), 0, ++row);
                gridEmail.add(new Text("Data : " + finalEmail.getDataSpedizione().toString()), 2, row);
                gridEmail.add(new Text("Argomento : " +finalEmail.getArgomento().toString()), 0, ++row);
                gridEmail.add(new Text("Testo : " +finalEmail.getTesto().toString()), 0, ++row);
                gridEmail.add(new Text(""), 0, ++row);

                Button bRispondi = new Button("Rispondi");
                Button bRispondiAll = new Button("Rispondi a Tutti");
                Button bInoltra = new Button("Inoltra");
                Button bElimina = new Button("Elimina");
                bElimina.setStyle("-fx-background-color: #d13c0f;");

                bRispondi.setOnAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent e) {
                        try {
                            that.pageClient.writeEmail( finalEmail.getMittente(), finalEmail.getArgomento(), finalEmail.getTesto() + System.getProperty("line.separator"));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                });
                bRispondiAll.setOnAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent e) {
                        try {
                            List<String> all = finalEmail.getIdDestinatari();
                            all.add(finalEmail.getMittente());
                            all.remove(that.model.email().get());
                            that.pageClient.writeEmail( String.join("; ", all), finalEmail.getArgomento(), finalEmail.getTesto() + System.getProperty("line.separator"));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                });
                bInoltra.setOnAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent e) {
                        try {
                            that.pageClient.writeEmail("", finalEmail.getArgomento(), "(" + finalEmail.getMittente() +  ") "+ finalEmail.getTesto() + System.getProperty("line.separator"));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    }
                });
                bElimina.setOnAction(new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent e) {
                        send(socket, out, "REMOVE_" + jsonEmail);
                    }
                });

                gridEmail.add(bInoltra, 0, ++row);
                gridEmail.add(bRispondi, 1, row);
                gridEmail.add(bRispondiAll, 2, row);
                gridEmail.add(bElimina, 3, row);

                gridPaneEmail.addRow(gridRow[0], gridEmail);
                gridRow[0]++;
            }
        });
    }

    @FXML
    public TextField receivers;
    public TextField argument;
    public TextField text;
    public Text errorText;
    public Text errorEmails;
    public Text resSendEmail;
    public Text account;
    public GridPane gridPaneEmail;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(account != null){
            account.setText("Benvenuto " + model.email().get());
            account.setFont(new Font("Arial", 15));
        }
        if(receivers != null && errorEmails != null){
            receivers.textProperty().bindBidirectional(model.receivers());
            errorEmails.textProperty().bindBidirectional(model.errorEmails());
        }
        if(currentPage.equals("start") && (oldCurrentPage == null || !oldCurrentPage.equals(currentPage))){
            var that = this;
            new Thread(()-> {
                that.setSocket();
            }).start();
            this.model.setSocket(this.socket);
        }
        if(currentPage.equals("receivedEmails") || currentPage.equals("sentEmails")){
            if(emailsClient != null){
                initializeEmails(false);
            }
        }
    }

    public void initializeEmails(Boolean clear){
        if(clear){
            Platform.runLater(new Runnable(){
                @Override
                public synchronized void run() {
                    gridPaneEmail.getChildren().clear();
                }
            });
        }
        final Integer[] gridRow = {1};
        Integer total = 0;
        for (String emailLine:
                emailsClient) {
            Email email = new Email(new ArrayList<>(), new String(), new String(), new String());
            try {
                email = convertFromJson(emailLine, email.getClass());
            } catch (Exception e) {
                System.out.println("eee " + e);
                e.printStackTrace();
            }
            if(email != null){
                if(currentPage.equals("receivedEmails") && !email.getMittente().equals(this.model.email().get())){
                    total++;
                    createRowEmail(email, gridRow, emailLine);
                }
                if(currentPage.equals("sentEmails") && email.getMittente().equals(this.model.email().get())){
                    total++;
                    createRowEmail(email, gridRow, emailLine);
                }
            }
        }

        if(total == 0){
            gridPaneEmail.add(new Text("Casella vuota"), 3, 0);
        }
    }

    public void logout() throws Exception {
        out.println("EXIT");
        socketBegin = true;
        emailsClient = new ArrayList<>();
        this.pageClient.login();
        this.socket.close();
    }

    public void stop() throws Exception {
        emailsClient = new ArrayList<>();
        socketBegin = true;
        this.socket.close();
        out.println("EXIT");
    }

    public void received(ActionEvent event) throws Exception {
        if(!currentPage.equals("receivedEmails")) {
            this.pageClient.receivedEmails();
        }
    }

    public void sent(ActionEvent event) throws Exception {
        if(!currentPage.equals("sentEmails")) {
            this.pageClient.sentEmails();
        }
    }

    public void writeEmail(ActionEvent event) throws Exception {
        this.pageClient.writeEmail("", "", "");
    }

    public void sendEmail(ActionEvent event) throws Exception {
        if(text.getText().equals("") || !errorEmails.getText().equals("") || receivers.getText().equals("")){
            errorText.setText("Destinatari e Testo dell' email non possono essere vuoti");
        }else{
            errorText.setText("");
            List<String> selectedItems = Arrays.asList(receivers.getText().replace(" ", "").toLowerCase(Locale.ROOT).split(";"));
            Email e = new Email(selectedItems, this.model.email().get(), argument.getText(), text.getText());
            String jsonEmail = convertToJson(e);
            out.println(jsonEmail);
        }
    }

    public <E> String convertToJson(E obj){
        Gson gson = new Gson();
        return gson.toJson(obj);
    }

    public <E> E convertFromJson(String json, Class c) throws Exception {
        try{
            Gson gson = new Gson();
            return gson.fromJson(json, (Type) c);
        }catch (Exception e){
            System.out.println("Convertion ex " + e);
            return null;
        }
    }

}