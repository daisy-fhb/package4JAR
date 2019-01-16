package com.example.demo;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author: wxm
 * @create: 2019-01-01 10:57
 */
public class TcpClient {

        JFrame ClientFrame;
        JButton ConnectServer;
        JButton DisconnectServer;
        JButton SendMessageButton;
        JTextField deviceIdText;
        JTextField ServerIPAddressText;
        JTextField ServerPortText;
        JTextField InputContentText;
        JTextField CheakPassText;
        JLabel ChatContentLabel;
        JLabel explainLabel;

        Socket socket;
        BufferedReader input;
        PrintStream output;

        DefaultListModel<String> OnlineClientNickName;
        String ToTargetName = "deviceId";

        ClientThread cliendThread;

        public TcpClient() {
            CreateFrame();
        }
        public void ConnectServer() {
            String ServerIPAddress = ServerIPAddressText.getText().trim();
            int ServerPort = Integer.parseInt(ServerPortText.getText().trim());
            String checkPass = CheakPassText.getText();
            try {
                socket = new Socket(ServerIPAddress, ServerPort);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintStream(socket.getOutputStream());

                SendMessage(checkPass);

                cliendThread = new ClientThread();

            } catch (UnknownHostException e) {
                Error("主机地址异常"+e.getMessage());
                return;
            } catch (IOException e) {
                Error("连接服务器异常"+e.getMessage());
                return;
            }
        }

        public void CreateFrame() {

            ClientFrame = new JFrame("赛佰特Tcp客户端");
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
            ServerPortText.setText("6001");
            ClientIdPanel.add(ServerPortLabel);
            ClientIdPanel.add(ServerPortText);

            JLabel CheakPassLabel = new JLabel("鉴权");
            CheakPassText = new JTextField(10);
            CheakPassText.setText("*PID#Uid#AuthCode*");
            ClientIdPanel.add(CheakPassLabel);
            ClientIdPanel.add(CheakPassText);

            ConnectServer = new JButton("连接");
            DisconnectServer = new JButton("断开");
            ClientIdPanel.add(ConnectServer);
            ClientIdPanel.add(DisconnectServer);
            ClientIdPanel.setBorder(new TitledBorder("信息栏"));

            JPanel explainPanel = new JPanel();
            explainPanel.setPreferredSize(new Dimension(200,400));
            explainPanel.setBorder(new TitledBorder("说明"));
            explainLabel = new JLabel("<html>");
            explainLabel.setPreferredSize(new Dimension(200,400));
            explainPanel.add(explainLabel);
            explainLabel.setText("<html><span color='blue'>设备ID应与鉴权PID相同,鉴权为<br>第一次点击连接服务器进行使用;<br>发送消息封装了消息格式为：<br>TOPIC@设备ID@{[通道]=[值],[通道]=[值],……}<br>如TOPIC@DEVICEID@{C0=0,C1=1,……}<span></html>");

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
            //连接
            ConnectServer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String checkPass = CheakPassText.getText();
                    int strStartIndex = checkPass.indexOf("*");
                    int strEndIndex = checkPass.indexOf("#");
                    String pid = checkPass.substring(strStartIndex, strEndIndex).substring("*".length());

                    if(!pid.equals(deviceIdText.getText().trim())) {
                        Error("设备ID应与鉴权信息pid相同");
                    }else {
                        ConnectServer();
                    }
                }
            });
            //断开
            DisconnectServer.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ClientFrame.dispatchEvent(new WindowEvent(ClientFrame, WindowEvent.WINDOW_CLOSING) );
                }
            });
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
                        SendMessage("TOPIC@"+deviceIdText.getText()+"@"+message);

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


        public class ClientThread implements Runnable{
            boolean isRuning = true;
            public ClientThread () {
                new Thread(this).start();
            }
            @Override
            public void run() {
                while(isRuning) {
                    String message;
                    try {
                        message = input.readLine();
                        Message("服务器："+message);
                        System.out.println("客户端接收到"+message);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        Error("客户端接收消息失败"+ e.getMessage());
                        break;
                    }
                }
            }
        }


        public void SendMessage(String message) {
            output.println(message);
            output.flush();
        }

        public static void main(String srgs[]) {
            @SuppressWarnings("unused")
            TcpClient client = new TcpClient();
        }
}
