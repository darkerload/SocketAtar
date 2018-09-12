package sample;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.io.IOException;


public class Controller {

    private SocketManager socketManager;

    @FXML
    private TextField txtAddress;

    @FXML
    private TextField txtPort;

    @FXML
    private ChoiceBox cmbSocketMode;

    @FXML
    private ListView lstMessageArea;

    @FXML
    private TextField txtSendMessage;

    @FXML
    private Button btnConnect;

    private ObservableList<String> rcvdMsgsData;


    @FXML
    void btnConnect(ActionEvent event) {

        String address = this.txtAddress.getText();
        int port = Integer.parseInt(this.txtPort.getText());
        String socketMode = this.cmbSocketMode.getSelectionModel().getSelectedItem().toString();


        try{
            if (socketManager != null && socketManager.IsSocketClosed() == false)
            {
                socketManager.Close();
            }else {
                socketManager = new SocketManager(socketMode,address,port);
                socketManager.Connect(new MessageManager());
                rcvdMsgsData = FXCollections.observableArrayList();
                lstMessageArea.setItems(rcvdMsgsData);

                if (socketMode.equals("Server")){
                    btnConnect.setText("Established connection, waiting for clients");
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR,"No Connect", ButtonType.OK);
            alert.showAndWait();
        }
    }


    @FXML
    void sendMessage() throws IOException {
        String message = txtSendMessage.getText().trim();
        if (!message.isEmpty()) {
            socketManager.Emit(message);
            lstMessageArea.getItems().add("Me: "  + message);
            txtSendMessage.clear();
        }
    }


    class MessageManager implements ISocketManager {

        @Override
        public void onRecevie(String message) {
            if (message != null && !message.equals("")) {
                rcvdMsgsData.add(message);
            }
        }

        @Override
        public void onClosed(boolean status) {
        //    isClosed = status;
            if (status){
                Alert alert = new Alert(Alert.AlertType.ERROR,"Socket is closed", ButtonType.OK);
                alert.showAndWait();
                btnConnect.setText("Connect");
            }else   {
                Alert alert = new Alert(Alert.AlertType.INFORMATION,"Connected", ButtonType.OK);
                alert.showAndWait();
                btnConnect.setText("Connected/Close");
            }

        }
    }



}