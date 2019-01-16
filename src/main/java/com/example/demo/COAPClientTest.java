package com.example.demo;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;


/**
 * @desc 赛佰特 COAP 客户端
 * @author fuhongbing
 * @date 2019-1-5
 */
public class COAPClientTest {
    JFrame ClientFrame;
    JButton ConnectServer;
    JButton SendMessageButton;
    JTextField deviceIdText;
    JTextField ServerIPAddressText;
    JTextField ServerPortText;
    JTextField InputContentText;
    JTextField CheakPassText;
    JLabel ChatContentLabel;
    JLabel explainLabel;
    String cur_info="";

    public COAPClientTest() {
        CreateFrame();
        GetInfo();
    }

    public void CreateFrame() {

        ClientFrame = new JFrame("赛佰特COAP客户端");
        ClientFrame.setSize(800,600);
        ClientFrame.setLocationRelativeTo(null);
        ClientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel ClientIdPanel = new JPanel();
        ClientIdPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        ClientIdPanel.setSize(800, 100);

        JLabel deviceIdLabel = new JLabel("设备ID");
        deviceIdText = new JTextField(10);
        deviceIdText.setText("deviceId");
        ClientIdPanel.add(deviceIdLabel);
        ClientIdPanel.add(deviceIdText);

        JLabel ServerIPAddressLabel = new JLabel("IP地址");
        ServerIPAddressText = new JTextField(10);
        ServerIPAddressText.setText("127.0.0.1");
        ClientIdPanel.add(ServerIPAddressLabel);
        ClientIdPanel.add(ServerIPAddressText);

        JLabel ServerPortLabel = new JLabel("端口");
        ServerPortText = new JTextField(10);
        ServerPortText.setText("6000");
        ClientIdPanel.add(ServerPortLabel);
        ClientIdPanel.add(ServerPortText);

        JLabel CheakPassLabel = new JLabel("鉴权");
        CheakPassText = new JTextField(10);
        CheakPassText.setText("*PID#Uid#AuthCode*");
        ClientIdPanel.add(CheakPassLabel);
        ClientIdPanel.add(CheakPassText);

        ConnectServer = new JButton("COAP Ping测试");
        ClientIdPanel.add(ConnectServer);
        ClientIdPanel.setBorder(new TitledBorder("信息栏"));

        JPanel explainPanel = new JPanel();
        explainPanel.setPreferredSize(new Dimension(200,400));
        explainPanel.setBorder(new TitledBorder("说明"));
        explainLabel = new JLabel("<html>");
        explainLabel.setPreferredSize(new Dimension(200,400));
        explainPanel.add(explainLabel);
        explainLabel.setText("<html><span color='blue'>1: 设备ID应与鉴权PID相同;<br><br>2: 发送消息封装的消息格式为：TOPIC@设备ID@消息内容<br>如TOPIC@DEVICEID@123<br><br>3: 客户端接收来自服务端的COAP消息默认端口为1000，即接收地址为coap://ip:1000/设备id <br><br>4: COAP ping仅用于测试客户端与服务端的信息发送端口是否正常连接<span></html>");

        JPanel ChatContentPanel = new JPanel();
        ChatContentPanel.setPreferredSize(new Dimension(490,400));
        ChatContentPanel.setBorder(new TitledBorder("消息内容"));
        ChatContentLabel = new JLabel("<html>");
        ChatContentLabel.setPreferredSize(new Dimension(490,400));
        ChatContentPanel.add(ChatContentLabel);

        JPanel InputContentPanel = new JPanel();
        InputContentPanel.setPreferredSize(new Dimension(600,100));
        InputContentText = new JTextField();
        InputContentText.setPreferredSize(new Dimension(600,60));
        SendMessageButton = new JButton("发送");
        InputContentPanel.add(InputContentText);
        InputContentPanel.add(SendMessageButton);
        InputContentPanel.setBorder(new TitledBorder("输入内容"));

        ClientFrame.add(ClientIdPanel, BorderLayout.NORTH);
        ClientFrame.add(explainPanel, BorderLayout.CENTER);
        ClientFrame.add(ChatContentPanel,BorderLayout.WEST);
        ClientFrame.add(InputContentPanel,BorderLayout.SOUTH);

        ClientFrame.setVisible(true);

        AddActionListener();
    }
    private void AddActionListener() {
        //发送
        SendMessageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String checkPass = CheakPassText.getText();
                int strStartIndex = checkPass.indexOf("*");
                int strEndIndex = checkPass.indexOf("#");
                String pid = checkPass.substring(strStartIndex, strEndIndex).substring("*".length());
                if(!pid.equals(deviceIdText.getText().trim())) {
                    Error("设备ID应与鉴权信息pid相同");
                }else {
                    String message = InputContentText.getText().trim();
                    SendMessage("TOPIC@"+deviceIdText.getText()+"@"+message+"@"+checkPass);
                }
            }
        });
        ConnectServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (ClientPingInstance()){
                    Message("网络畅通");
                }else{
                    Message("网络异常");
                }
            }
        });
    }
    //错误提示
    private void Error(String message){
        ChatContentLabel.setText(ChatContentLabel.getText()+"<span color='red'>"+message+"</span>"+"<br />");
    }
    //消息提示
    private void Message(String message){
        ChatContentLabel.setText(ChatContentLabel.getText()+"<span color='black'>"+message+"</span>"+"<br />");
    }

    public boolean ClientPingInstance() {
        String ip=ServerIPAddressText.getText().trim();
        String port=ServerPortText.getText().trim();
        String deviceid=deviceIdText.getText().trim();
        try {
            URI uri = new URI("coap://"+ip+":"+port+"/"+deviceid);
            CoapClient client = new CoapClient(uri);
            return client.ping(2000);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }


    public void SendMessage(String message) {
        String ip=ServerIPAddressText.getText().trim();
        String port=ServerPortText.getText().trim();
        String deviceid=deviceIdText.getText().trim();
        try {
            URI uri = new URI("coap://"+ip+":"+port+"/"+deviceid);
            CoapClient client = new CoapClient(uri);
            CoapResponse response = client.post(message,0);
            if (response != null) {
                if ("NOT_FOUND".equals(response.getCode().name())){
                    Error("找不到该设备的COAP服务,请确认设备ID是否正确！");
                }else{
                    Message(response.getResponseText());
                }
            }
        } catch (Exception e) {
            Message(e.getMessage());
        }
    }

    public  void GetInfo(){
        while (true){
            String ip=ServerIPAddressText.getText().trim();
            String deviceid=deviceIdText.getText().trim();
            try {
                URI uri = new URI("coap://"+ip+":"+1000+"/"+deviceid);
                CoapClient client = new CoapClient(uri);
                CoapResponse response = client.get();
                if (response != null) {
                    if (!response.getResponseText().equals(cur_info)){
                        cur_info=response.getResponseText();
                        Message("接收到服务器发来的消息:"+cur_info);
                    }
                }
                Thread.sleep(1000);
            }catch (Exception e){
                Message(e.getMessage());
            }
        }
    }
    public static void main(String srgs[]) {
        new COAPClientTest();
    }
}