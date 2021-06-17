package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import javax.swing.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class ControllerLogin implements Initializable {
    private final ModelClient model;
    private final Client pageClient;
    private String view;

    public ControllerLogin(ModelClient model, Client pageClient, String view) {
        this.model = model;
        this.pageClient = pageClient;
        this.view = view;
    }

    @FXML
    public TextField email;
    public Text errorText;

    public void setView(String page){
        this.view = page;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final Socket[] socket = new Socket[1];
        if(!this.view.equals("wait")){
            email.textProperty().bindBidirectional(model.email());
            errorText.textProperty().bindBidirectional(model.errorText());
        }else{
            Timer timer = new Timer();
            ControllerLogin that = this;

            final TimerTask task = new TimerTask() {
                @Override
                public void run() {
                try{
                    socket[0] = new Socket("127.0.0.1", 8189);
                    System.out.println("Socket aperto");
                    that.pageClient.mainPage(socket[0], that.model);
                }catch (Exception ex){
                    System.out.println("riprova a connettere");
                    try {
                        that.pageClient.waiting();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                }
            };

            timer.schedule(task, 2000);
        }
    }

    public void login(ActionEvent event) throws Exception {
        try{
            if (!email.getText().equals("") && errorText.getText().equals("")) {
                Socket socket = new Socket("127.0.0.1", 8189);
                this.pageClient.mainPage(socket, model);
            }else {
                System.out.println("Try again");
            }
        }catch (Exception ex){
            this.pageClient.waiting();
        }
    }
}