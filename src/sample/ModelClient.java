package sample;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.apache.commons.validator.routines.EmailValidator;
import sample.old.Email;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ModelClient {

    private Socket socket;
    private String fileName;
    final String directory = "src/sample/files/";
    private StringProperty email;
    private StringProperty errorText;
    private StringProperty errorEmails;
    private StringProperty receivers;
    private Scanner fileScanner;

    public ModelClient() {
        email = new SimpleStringProperty("");
        errorText = new SimpleStringProperty("");
        errorEmails = new SimpleStringProperty("");
        receivers = new SimpleStringProperty("");
        email.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                if(email.get() != "" && !EmailValidator.getInstance().isValid(email.get())){
                    errorText.setValue("Email non valida");
                }else {
                    errorText.setValue("");
                }
            }
        });
        receivers.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                List<String> accountListener = Arrays.asList(s2.split(";"));
                boolean error = false;
                for (String val : accountListener){
                    if(val != "" && !EmailValidator.getInstance().isValid(val)){
                        error = true;
                        errorEmails.setValue((accountListener.size() == 1) ? "Email non valida" : "Email non valide");
                    }
                }
                if(error == false){
                    errorEmails.setValue("");
                }
            }
        });
    }

    public StringProperty email() {
        return email;
    }

    public StringProperty errorText() {
        return errorText;
    }

    public StringProperty receivers() {
        return receivers;
    }

    public StringProperty errorEmails() {
        return errorEmails;
    }

    public void setSocket(Socket s){
        this.socket = s;
    }

    public Socket socket(){
        return socket;
    }
}