package sample;

public interface ISocketManager {
    void onRecevie(String line);
    void onClosed(boolean status);
}
