package com.example.demo;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;

import java.io.*;

public class LinuxSysUtil {
   public  static void ShowInfo() {
        try {
            String cmd = "free -m |grep Mem";
            String memre=CmdToolkit.readConsole(cmd,false);
            System.out.println(NumberUtil.div(memre.split("        ")[4].trim(),memre.split("        ")[3].trim()));
            System.out.println(StrUtil.subBetween(writeTopMsg(),"%Cpu(s): ","us,"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public static String writeTopMsg() {
        InputStreamReader isr = null;
        BufferedReader br = null;

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;

        String result = "";
        try {
            String cmd = "top -b -n 1";// 直接写top输出为空，动态命令，只能加参数输出一次
            Process ps = Runtime.getRuntime().exec(cmd);
            isr = new InputStreamReader(ps.getInputStream());
            br = new BufferedReader(isr);
            File file = new File("/usr/topmsg.txt");
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            fos = new FileOutputStream(file, true);
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);

            StringBuffer sb = new StringBuffer();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
                bw.write(line + "\n");
            }
            result = sb.toString();
        } catch (Exception e) {
            System.out.println("writeTopMsg error:" + e);
        } finally {
            try {
                if (bw != null)
                    bw.close();
            } catch (IOException e) {
                System.out.println("close bw error:" + e);
            }
            try {
                if (osw != null)
                    osw.close();
            } catch (IOException e) {
                System.out.println("close osw error:" + e);
            }
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                System.out.println("close fos error:" + e);
            }
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                System.out.println("close br error:" + e);
            }
            try {
                if (isr != null)
                    isr.close();
            } catch (IOException e) {
                System.out.println("close isr error:" + e);
            }
        }
        return result;
    }
    public static void main(String[] args) {
        ShowInfo();
    }
}
