package sample;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Email implements Serializable {
    private String idMittente;
    private List<String> idDestinatari;
    private String argomento;
    private String testo;
    private String dataSpedizione;

    Email(List<String> destinatari, String mittente, String object, String text){
        idMittente = mittente;
        idDestinatari = destinatari;
        argomento = object;
        testo = text;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        dataSpedizione = dtf.format(now);
    }

    public List<String> getIdDestinatari(){
        return idDestinatari;
    }
    public String getMittente(){
        return idMittente;
    }
    public String getTesto(){
        return testo;
    }
    public String getArgomento(){
        return argomento;
    }
    public String getDataSpedizione(){
        return dataSpedizione;
    }
}
