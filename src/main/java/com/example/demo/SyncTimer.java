package com.example.demo;

import cn.hutool.http.HttpUtil;
import cn.hutool.setting.dialect.Props;
import com.alibaba.fastjson.JSONObject;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SyncTimer {


    JFrame ClientFrame;
    JButton ConnectServer;
    JTextField ServerIPAddressText;
    JTextField ServerPortText;
    JLabel ChatContentLabel;
    JLabel explainLabel;

    public SyncTimer() {
        CreateFrame();
    }

    public void CreateFrame() {

        ClientFrame = new JFrame("中央监控系统时钟同步客户端软件");
        ClientFrame.setSize(800,600);
        ClientFrame.setLocationRelativeTo(null);
        ClientFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel ClientIdPanel = new JPanel();
        ClientIdPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        ClientIdPanel.setSize(800, 100);


        JLabel ServerIPAddressLabel = new JLabel("服务器IP地址");
        ServerIPAddressText = new JTextField(10);
        ServerIPAddressText.setText("127.0.0.1");
        ClientIdPanel.add(ServerIPAddressLabel);
        ClientIdPanel.add(ServerIPAddressText);

        JLabel ServerPortLabel = new JLabel("端口");
        ServerPortText = new JTextField(10);
        ServerPortText.setText("8081");
        ClientIdPanel.add(ServerPortLabel);
        ClientIdPanel.add(ServerPortText);


        ConnectServer = new JButton("开始同步");
        ClientIdPanel.add(ConnectServer);
        ClientIdPanel.setBorder(new TitledBorder("信息栏"));

        JPanel explainPanel = new JPanel();
        explainPanel.setPreferredSize(new Dimension(200,400));
        explainPanel.setBorder(new TitledBorder("说明"));
        explainLabel = new JLabel("<html>");
        explainLabel.setPreferredSize(new Dimension(200,400));
        explainPanel.add(explainLabel);
        explainLabel.setText("<html><span color='blue'>1:请在上面输入需要同步的服务器的IP和端口；<br><br>2:同步完成以后，本机时间将于该服务器保持一致；<br><br>3:请使用windows/linux/mac os等系统的管理员账号权限运行本软件。<span></html>");

        JPanel ChatContentPanel = new JPanel();
        ChatContentPanel.setPreferredSize(new Dimension(490,400));
        ChatContentPanel.setBorder(new TitledBorder("消息内容"));
        ChatContentLabel = new JLabel("<html>");
        ChatContentLabel.setPreferredSize(new Dimension(490,400));
        ChatContentPanel.add(ChatContentLabel);


        ClientFrame.add(ClientIdPanel, BorderLayout.NORTH);
        ClientFrame.add(explainPanel, BorderLayout.CENTER);
        ClientFrame.add(ChatContentPanel,BorderLayout.WEST);

        ClientFrame.setVisible(true);

        AddActionListener();
    }
    private void AddActionListener() {
        //发送
        ConnectServer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String ip=ServerIPAddressText.getText().trim();
                String port=ServerPortText.getText().trim();
                JSONObject re=startSyncClock(ip,port);
                if (re.getBoolean("flag")){
                    Message(re.toJSONString());
                }else{
                    Error(re.toJSONString());
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







    /**
     * 修改系统时间
     * yyyy-MM-dd HH:mm:ss
     * @param dataStr_   2017-11-11   yyyy-MM-dd
     * @param timeStr_   11:11:11     HH:mm:ss
     */
    public JSONObject updateSysDateTime(String dataStr_, String timeStr_){
        JSONObject re=new JSONObject();
        try {
            String osName = System.getProperty("os.name");
            // Window 系统
            if (osName.matches("^(?i)Windows.*$")) {
                String cmd;
                // 格式：yyyy-MM-dd
                cmd = " cmd /c date " + dataStr_;
                Runtime.getRuntime().exec(cmd);
                // 格式 HH:mm:ss
                cmd = " cmd /c time " + timeStr_;
                Runtime.getRuntime().exec(cmd);
                re.put("msg","windows 时间同步成功");
                re.put("flag",true);
            } else if (osName.matches("^(?i)Linux.*$")||osName.contains("Mac OS")) {
                // Linux 系统 格式：yyyy-MM-dd HH:mm:ss   date -s "2017-11-11 11:11:11"
                FileWriter excutefw = new FileWriter("/usr/updateSysTime.sh");
                BufferedWriter excutebw=new BufferedWriter(excutefw);
                excutebw.write("date -s \"" + dataStr_ +" "+ timeStr_ +"\"\r\n");
                excutebw.close();
                excutefw.close();
                String cmd_date ="sh /usr/updateSysTime.sh";
                Runtime.getRuntime().exec(cmd_date);
                System.out.println("cmd :" + cmd_date + " date :" + dataStr_ +" time :" + timeStr_);
                re.put("msg","linux/mac 时间同步成功");
                re.put("flag",true);
            } else {
                re.put("msg","操作系统无法识别");
                re.put("flag",false);
            }
        } catch (IOException e) {
            Error(e.getMessage());
        }
        return re;
    }


    /**
     * 定时任务方法
     */
    public JSONObject startSyncClock(String ip,String port){
        JSONObject re=new JSONObject();
        try {
            String serverTime= HttpUtil.get(ip+":"+port+"/windMachine/clocksync",3000);
            re=updateSysDateTime(serverTime.split(" ")[0],serverTime.split(" ")[1]);
            re.put("time",serverTime);
        }catch (Exception e){
            re.put("error","连接超时，请检查ip/地址是否正确");
            re.put("flag",false);
        }
        return re;
    }


    public static void main(String[] args) {
        new SyncTimer();
    }

}
