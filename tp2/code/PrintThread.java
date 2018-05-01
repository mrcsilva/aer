import java.util.Map;
import java.net.InetAddress;
import java.lang.Exception;
import java.util.HashMap;

class PrintThread extends Thread {

    private Map<InetAddress, No> tabela;

    public PrintThread(Map<InetAddress, No> tabela) {
        this.tabela = tabela;
    }

    @Override
    public void run() {
        try {
            System.out.format("%30s %20s %15s", "No", "|", "Saltos");
            System.out.println();
            for (No no : this.tabela.values()) {
                System.out.format("%45s %5s %13s", no.getIp().getHostAddress().trim(), "|", no.getSaltos());
                System.out.println();
            }
            try {
                Thread.sleep(5000);
                //System.out.println("Slept Print");
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        catch (Exception io) {

        }
    }
}
