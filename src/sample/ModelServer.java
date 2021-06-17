package sample;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ModelServer {

    final String directory = "src/sample/files/";
    public static class ClientConnected{
        public String email;
        public List<String> listEmails;
        public ClientConnected(String email){
            this.email = email;
            this.listEmails = new ArrayList<>();
        }
    }

    private List<ClientConnected> listaClienti;

    public ModelServer() {
        listaClienti = new ArrayList<>();
    }

    public List<ClientConnected> listaClienti() {
        return listaClienti;
    }

    public synchronized void addCliente(ClientConnected c) throws IOException {
        try {
            Scanner fileScanner = new Scanner(new File(this.directory, "file_" + c.email +".txt"));
        } catch (FileNotFoundException e) {
            File dir = new File(this.directory);
            dir.mkdirs();
            File yourFile = new File(dir,"file_" + c.email +".txt");
            yourFile.createNewFile();
        }
        listaClienti.add(c);
        openFileAndFillEmailList(listaClienti.indexOf(c), c.email);
    }

    public void removeCliente(Integer i){
        for (ClientConnected cs:
                listaClienti) {
            System.out.println(cs.email);
        }
        if(i < listaClienti.size())
            listaClienti.remove(listaClienti.get(i));
        for (ClientConnected cs:
                listaClienti) {
            System.out.println(cs.email);
        }
    }

    public synchronized void removeFromFile(String linetoRemove, String email){
        try{
            File inputFile = new File(directory, "file_" + email +".txt");
            File tempFile = new File(directory, "myTempFile.txt");

            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String currentLine;
            while((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                if(!trimmedLine.equals(linetoRemove)){
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
            }
            writer.close();
            reader.close();

            reader = new BufferedReader(new FileReader(tempFile));
            writer = new BufferedWriter(new FileWriter(inputFile));

            while((currentLine = reader.readLine()) != null) {
                String trimmedLine = currentLine.trim();
                if(!trimmedLine.equals(linetoRemove)){
                    writer.write(currentLine + System.getProperty("line.separator"));
                }
            }
            writer.close();
            reader.close();
            tempFile.delete();
        }catch (Exception ex){
            System.out.println("Ex "+ ex);
        }
    }

    public synchronized List<String> openFileAndFillEmailList(int indexClient, String email){
        this.listaClienti.get(indexClient).listEmails = new ArrayList<>();
        String entireEmail = "";
        try{
            Scanner fileScanner = new Scanner(new File(directory, "file_" + email +".txt"));
            while (fileScanner.hasNext()){
                String line = fileScanner.next();
                if(line.indexOf("{") >= 0){
                    entireEmail += line;
                }else if(line.indexOf("}") >= 0){
                    entireEmail += " " +line.substring(0, line.indexOf("}") + 1);
                    this.listaClienti.get(indexClient).listEmails.add(entireEmail);
                    if(line.indexOf("{") >= 0){
                        entireEmail = line.substring(line.indexOf("{"), line.length() - line.indexOf("{"));
                    }else{
                        entireEmail = "";
                    }

                }else{
                    entireEmail += " " +line;
                }
            }
        }catch (Exception ex){
            System.out.println("Problemi  " + ex);
        }

        return this.listaClienti.get(indexClient).listEmails;
    }


}