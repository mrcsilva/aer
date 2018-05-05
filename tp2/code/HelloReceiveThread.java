import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.List;
import java.util.ArrayList;
import java.net.DatagramPacket;
import java.lang.InterruptedException;
import java.lang.Exception;

class HelloReceiveThread extends Thread {

    private InetAddress ip;
    private Map<InetAddress, No> tabela;
    private int numHellos;
    private BlockingQueue<DatagramPacket> queueH = null;

    public HelloReceiveThread(InetAddress ip, Map<InetAddress, No> tabela, int numHellos, BlockingQueue<DatagramPacket> queueH) {
        this.ip = ip;
        this.tabela = tabela;
        this.numHellos = numHellos;
        this.queueH = queueH;
    }

    @Override
    public void run() {

        try {
            List<DatagramPacket> listR = new ArrayList<DatagramPacket>();

            while (true) {
                try {
                    Thread.sleep(700); // dead interval
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

                // Retira todos os dados da queue e adiciona-os a lista
                // A queue contem todos os hellos recebidos até ao momento
                queueH.drainTo(listR);

                if (!listR.isEmpty()) {
                    this.numHellos += listR.size();
                    this.tabela.get(this.ip).setNumHellos(this.numHellos);
                    listR.clear();
                }
                else {
                    // Vizinho saiu do alcance
                    // Remove-lo de seguida
                    this.tabela.remove(this.ip);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        catch (Exception io) {

        }
    }
}
