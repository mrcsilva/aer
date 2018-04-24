import java.net.InetAddress;

class Message {

    private InetAddress ipFrom;
    private InetAddress ipTo;
    private String mess;
    private long timestamp;

    public Message() {
    }

    public Message(InetAddress ipFrom, InetAddress ipTo, String mess, long timestamp) {
        this.ipFrom = ipFrom;
        this.ipTo = ipTo;
        this.mess= mess;
        this.timestamp = timestamp;
    }

    //Gets

    public InetAddress getIpFrom() {
        return this.ipFrom;
    }

    public InetAddress getIpTo() {
        return this.ipTo;
    }

    public String getMess() {
        return this.mess;
    }

    public long getTime() {
        return this.timestamp;
    }


    //Sets

    public void setIpFrom(InetAddress ipFrom) {
        this.ipFrom = ipFrom;
    }

    public void setIpTo(InetAddress ipTo) {
        this.ipTo = ipTo;
    }

    public void setMess(String  mess) {
        this.mess = mess;
    }

    public void setTime(long timestamp) {
        this.timestamp = timestamp;
    }

}
