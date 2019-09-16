package com.example.demo;

import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author fuhongbing
 * @desc 端口转发
 * @date 2019/01/19
 */
@Component
public class MappedUtil implements Callable<Boolean> {

    InputStream is;
    OutputStream os;
    boolean isfinised=false;
    ServerSocket ss;

    public MappedUtil() {}

    public MappedUtil(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public Boolean call() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = this.is;
            out = this.os;
            byte buffer[] = new byte[8192];
            int a;
            while (!isfinised && (a = in.read(buffer)) > 0) {
                out.write(buffer, 0, a);
                out.flush();
            }
        } catch (Exception e) {
            isfinised=true;
            if (e.getMessage().contains("Socket closed")||e.getMessage().contains("Connection reset")){
//                System.out.println("客户端已退出");
            }else {
                e.printStackTrace();
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isfinised;
        }
    }

    /**
     * 转发请求
     *
     * @param port_recept  接收请求的端口
     * @param port_forward 待转发的端口
     */
    public void TransferRequst(int port_recept, String ip_forward, int port_forward) {
        Socket s=null;
        Socket socket;
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            String forwardHost = ip_forward;
            System.out.println("正在转发请求至  "+ip_forward+" "+port_forward);
            //转发端口已占用
            if (ss!=null&&!ss.isClosed()){
                closeServer();
            }
            ss= new ServerSocket(port_recept);
            while (!ss.isClosed()) {
                s = ss.accept();
                String ip = s.getInetAddress().getHostAddress();
                System.out.println("Host:" + ip + " 连接成功!");
                socket = new Socket(forwardHost, port_forward);
                System.out.println("转发到:" + forwardHost + ",端口:" + port_forward);
                Future f=pool.submit(new MappedUtil(socket.getInputStream(), s.getOutputStream()));
                Future f2=pool.submit(new MappedUtil(s.getInputStream(), socket.getOutputStream()));
                if ((Boolean) f.get()){
                    System.out.println( "Host:" +ip+" 退出连接！");
//                    System.exit(0);
                }
            }
        } catch (Exception e) {
            if (!e.getMessage().contains("Socket closed")){
                System.out.println( "本次  "+ ip_forward+":"+port_forward+" 的请求已经处理完毕，即将退出转发服务");
            }else{
                e.printStackTrace();
            }
        }finally {
            try {
                if (s!=null){
                    s.close();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void closeServer(){
        try {
            ss.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String ip= System.getProperty("ip");
        System.out.println("server: "+ip);
        new MappedUtil().TransferRequst(503, ip, 666);
    }
}
