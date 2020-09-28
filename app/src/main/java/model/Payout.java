package model;

public class Payout {

    private int id;
    private String user;
    private String description;
    private String agent;
    private String amount;
    private String status;
    private String date_requested;
    private String date_approved;
    private String channel;
    private int investment_id;
    private String investment;

    public String getInvestment() {
        return investment;
    }

    public void setInvestment(String investment) {
        this.investment = investment;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate_requested() {
        return date_requested;
    }

    public void setDate_requested(String date_requested) {
        this.date_requested = date_requested;
    }

    public String getDate_approved() {
        return date_approved;
    }

    public void setDate_approved(String date_approved) {
        this.date_approved = date_approved;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getInvestment_id() {
        return investment_id;
    }

    public void setInvestment_id(int investment_id) {
        this.investment_id = investment_id;
    }

}
