import java.util.Map;
import java.net.InetAddress;


class NewsThread extends Thread {

    // Formato do data
    // GET_NEWS_FROM IP_ORIGEM IP_DESTINO
    private String[] data;
    private Map<InetAddress, No> tabela;

    public NewsThread(String[] data, Map<InetAddress, No> tabela) {
        this.data = data;
        this.tabela = tabela;
    }

    @Override
    public void run() {

    }

}
