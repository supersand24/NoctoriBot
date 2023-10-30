package Game.ClashOfClans;

public class ClientError {

    String reason;
    String message;
    String type;

    public String getReason() {
        return reason;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    void setReason(String reason) {
        this.reason = reason;
    }

    void setMessage(String message) {
        this.message = message;
    }

    void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ClientError{" +
                "reason='" + reason + '\'' +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
