import java.net.InetAddress;

class Message {

    private InetAddress ipFrom;
    private InetAddress ipTo;
    private String mess;
    private long timestamp;
    // True => GET_NEWS_FROM
    // False => NEWS_FOR
    private Boolean type;

    public Message(InetAddress ipFrom, InetAddress ipTo, String mess, long timestamp, Boolean type) {
        this.ipFrom = ipFrom;
        this.ipTo = ipTo;
        this.mess= mess;
        this.timestamp = timestamp;
        this.type = type;
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

    public String getType() {
        if(type == true) {
            return "GET_NEWS_FROM";
        }
        else {
            return "NEWS_FOR";
        }
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

    public void setType(String type) {
        if(type.equals("GET_NEWS_FROM")) {
            this.type = true;
        }
        else if(type.equals("NEWS_FOR")){
            this.type = false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(type == true) {
            sb.append("GET_NEWS_FROM ");
        }
        else {
            sb.append("NEWS_FOR ");
        }
        sb.append(ipFrom.getHostAddress() + " ");
        sb.append(ipTo.getHostAddress() + " ");
        if(!mess.equals("")) {
            sb.append(mess+" ");
        }
        sb.append(timestamp);
        return sb.toString();
    }

}
