package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.Socket;

public class Client extends Application {
    private Stage stage;
    private Parent root;
    private ModelClient model;
    private ControllerLogin controllerLogin;
    private ControllerMain controller;
    private Client client = this;
    FXMLLoader loader;
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.stage = primaryStage;
        final ModelClient model = new ModelClient();
        final Client test = this;
        this.loader = new FXMLLoader();
        this.loader.setLocation(getClass().getResource("view/login.fxml"));
        controllerLogin = new ControllerLogin(model, test, "login");
        this.loader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> aClass) {
                return controllerLogin;
            }
        });
        root = this.loader.load();

        primaryStage.setTitle("Login");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    public void login() throws Exception{
        try {
            model = new ModelClient();
            client = this;

            FXMLLoader l = new FXMLLoader();
            l.setLocation(getClass().getResource("view/login.fxml"));
            this.controllerLogin = new ControllerLogin(model, client, "login");
            l.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> aClass) {
                    return controllerLogin;
                }
            });
            root = l.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void waiting() throws Exception{
        try {
            model = new ModelClient();
            client = this;
            FXMLLoader l = new FXMLLoader();
            l.setLocation(getClass().getResource("view/waiting.fxml"));
            controllerLogin.setView("wait");
            //this.controllerLogin = new ControllerLogin(model, client, "wait");
            l.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> aClass) {
                    return controllerLogin;
                }
            });
            root = l.load();
            if(stage == null){
                stage = new Stage();
            }
            System.out.println("root " + root);
            System.out.println("stage " + stage);
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mainPage(Socket s, ModelClient model) throws Exception{
        try {
            this.model = model;

            final Client test = this;

            this.controller = new ControllerMain(model, test, "start", s);
            //loader.setLocation(getClass().getResource("view/dashboard.fxml"));
            this.loader = new FXMLLoader(getClass().getResource("view/dashboard.fxml"));
            var that = this;
            this.loader.setControllerFactory(new Callback<Class<?>, Object>() {
                @Override
                public Object call(Class<?> aClass) {
                    return that.controller;
                }
            });

            final BorderPane root = this.loader.load();
            AnchorPane center= new AnchorPane();
            root.setCenter(center);
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("e dash " + e);
            e.printStackTrace();
        }
    }

    public void receivedEmails() throws Exception{
        try {
            var that = this;
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/dashboard.fxml"));
            this.controller.setCurrentPage("dash");
            loader.setControllerFactory(c -> controller);
            BorderPane root = loader.load();
            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/receivedEmails.fxml"));
            this.controller.setCurrentPage("receivedEmails");
            loader.setControllerFactory(c -> controller);
            AnchorPane center=loader.load();
            root.setCenter(center);
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sentEmails() throws Exception{
        try {
            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/dashboard.fxml"));
            this.controller.setCurrentPage("dash");
            loader.setControllerFactory(c -> controller);
            BorderPane root = loader.load();
            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/sentEmails.fxml"));
            this.controller.setCurrentPage("sentEmails");
            loader.setControllerFactory(c -> controller);
            AnchorPane center=loader.load();
            root.setCenter(center);
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeEmail(String listIdDestinatari, String argomento, String testo) throws Exception{
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/dashboard.fxml"));
            this.controller.setCurrentPage("dash");
            loader.setControllerFactory(c -> controller);
            BorderPane root = loader.load();
            loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/writeEmail.fxml"));
            loader.setControllerFactory(c -> controller);
            AnchorPane center= loader.load();
            root.setCenter(center);
            stage.getScene().setRoot(root);
            this.controller.argument.setText(argomento);
            this.controller.text.setText(testo);
            this.controller.receivers.setText(listIdDestinatari);
            this.controller.errorEmails.setText("");
            this.controller.errorText.setText("");
            this.controller.setCurrentPage("writeEmail");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("STOP");
        try{
            this.controller.stop();
        }catch (Exception ex){

        }
    }
}