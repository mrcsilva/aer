import java.util.concurrent.BlockingQueue;
import java.net.InetAddress;
import java.net.DatagramPacket;

class No {

    private InetAddress ip;
    private InetAddress ipVizinho;
    private int saltos;
    private long timestamp;
    private BlockingQueue<DatagramPacket> queueH = null;

    public No() {
    }

    public No(InetAddress ip, InetAddress ipVizinho, int saltos , BlockingQueue<DatagramPacket> queueH, int timestamp) {
        this.ip = ip;
        this.ipVizinho = ipVizinho;
        this.saltos = saltos;
        this.queueH = queueH;
        this.timestamp = timestamp;
    }

    //Gets
    public InetAddress getIp() {
        return this.ip;
    }

    public InetAddress getIpVizinho() {
        return this.ipVizinho;
    }

    public int getSaltos() {
        return this.saltos;
    }

    public BlockingQueue<DatagramPacket> getQueue() {
        return this.queueH;
    }

    public long getTime() {
        return this.timestamp;
    }


    //Sets

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public void setIpVizinho(InetAddress ipVizinho) {
        this.ipVizinho = ipVizinho;
    }

    public void setSaltos(int saltos) {
        this.saltos = saltos;
    }

    public void setTime(long timestamp) {
        this.timestamp = timestamp;
    }

}
