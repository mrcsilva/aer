import java.util.concurrent.BlockingQueue;
import java.net.InetAddress;
import java.net.DatagramPacket;

class No {

    private InetAddress ip;
    private int saltos;
    private int numHellos;
    private long timestamp;
    private BlockingQueue<DatagramPacket> queueH = null;

    public No() {
    }

    public No(InetAddress ip, int saltos , int numHellos, BlockingQueue<DatagramPacket> queueH, int timestamp) {
        this.ip = ip;
        this.numHellos = numHellos;
        this.saltos = saltos;
        this.queueH = queueH;
        this.timestamp = timestamp;
    }

    //Gets
    public InetAddress getIp() {
        return this.ip;
    }

    public int getSaltos() {
        return this.saltos;
    }

    public int getNumHellos() {
        return this.numHellos;
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

    public void setSaltos(int saltos) {
        this.saltos = saltos;
    }

    public void setNumHellos(int numHellos) {
        this.numHellos = numHellos;
    }

    public void setTime(long timestamp) {
        this.timestamp = timestamp;
    }

}
