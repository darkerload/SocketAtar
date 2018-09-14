package sample;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
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
    private TextArea txtSendMessage;

    @FXML
    private TextArea txtSelectedItem;

    @FXML
    private Button btnConnect;

    private ObservableList<String> rcvdMsgsData;


    @FXML
    void btnCopyMessage(ActionEvent event){
        StringSelection stringSelection = new StringSelection(txtSelectedItem.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    @FXML
        void lstMessageArea(MouseEvent mouse) throws IOException{
        String item = lstMessageArea.getSelectionModel().getSelectedItem().toString();
        if (item.startsWith("Me: ")){
            txtSelectedItem.setText(item.substring(4,item.length()));
        }else{
            txtSelectedItem.setText(item);
        }
    }


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
    void btnSendMessage() throws IOException {
        sendMessage();
    }

    void sendMessage(){
        String message = txtSendMessage.getText();
        socketManager.Emit(message);
        lstMessageArea.getItems().add("Me: "  + message);
        txtSendMessage.clear();
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