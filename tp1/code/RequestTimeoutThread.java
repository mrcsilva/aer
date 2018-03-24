import java.util.Map;
import java.net.InetAddress;
import java.lang.Exception;

class RequestTimeoutThread extends Thread {

    private int tempo;
    private Map<InetAddress, No> tabela;
    private InetAddress ip;

    public RequestTimeoutThread(int tempo, Map<InetAddress, No> tabela, InetAddress ip) {
        this.tempo = tempo;
        this.tabela = tabela;
        this.ip = ip;
    }

    @Override
    public void run() {
        try {
            //Está a imprimir null nos ecras
            Thread.sleep(tempo);
            if(tabela.get(ip).getSaltos()==-1) {
                tabela.remove(ip);
            }
        }
        catch(Exception e) {
            System.out.println("Timeout: "+e.getMessage());
        }
    }

}
