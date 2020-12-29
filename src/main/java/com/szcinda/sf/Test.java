//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.szcinda.sf;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CountDownLatch;

public class Test {
    public static final int THREAD_NUM = 500;
    static String url = "http://193.112.1.68:9009/callback/data";
    static String data = "{   \"notify\": \"Cdr\",   \"info\": {     \"appID\": \"aa493f1902752377f911de2d0611bb2c\",     \"callID\": \"6da10244-e699-11ea-9192-005056a18948\",     \"sessionID\": \"20200825140843187-1301-1662-0000-28597\"   },   \"subject\": {     \"caller\": \"70266\",     \"called\": \"%s\",     \"business\": 61,     \"ttsCount\": 0,     \"ttsLength\": 0,     \"ivrCount\": 0,     \"ivrTime\": 0,     \"duration\": 0,     \"cost\": 0.0,     \"recordFilename\": \"\",     \"recordSize\": 0,     \"createTime\": \"1598335723254\",     \"answerTime\": \"0\",     \"releaseTime\": \"1598335783750\",     \"dtmf\": \"\",     \"direction\": 1,     \"callout\": 1,     \"softCause\": 804   },   \"data\": \"abc123\",   \"timestamp\": \"1598335785950\" }";
    private static long startTime = 0L;

    public Test() {
    }

    public static void doHttpPost(String data, String URL, String phone) {
        byte[] xmlData = data.getBytes();
        InputStream instr = null;
        Object out = null;

        try {
            URL url = new URL(URL);
            URLConnection urlCon = url.openConnection();
            HttpURLConnection httpUrlConnection = (HttpURLConnection)urlCon;
            urlCon.setDoOutput(true);
            urlCon.setDoInput(true);
            urlCon.setUseCaches(false);
            urlCon.setRequestProperty("content-Type", "application/json");
            urlCon.setRequestProperty("charset", "utf-8");
            urlCon.setRequestProperty("Content-length", String.valueOf(xmlData.length));
            DataOutputStream printout = new DataOutputStream(urlCon.getOutputStream());
            printout.write(xmlData);
            printout.flush();
            printout.close();
            instr = urlCon.getInputStream();
            String code = Integer.toString(httpUrlConnection.getResponseCode());
            if ("".equals(code.trim())) {
                System.out.println("返回空");
            }

            System.out.println("返回数据为:" + code);
            long endTime = System.currentTimeMillis();
            System.out.println(phone + " ended at: " + endTime + ", cost: " + (endTime - startTime) + " ms.");
        } catch (Exception var21) {
            var21.printStackTrace();
        } finally {
            try {
                ((ByteArrayOutputStream)out).close();
                instr.close();
            } catch (Exception var20) {
            }

        }

    }

    private void init() {
        try {
            long phone = 13112274760L;
            startTime = System.currentTimeMillis();
            System.out.println("CountDownLatch started at: " + startTime);
            CountDownLatch countDownLatch = new CountDownLatch(1);

            for(int i = 0; i < 50; ++i) {
                ++phone;
                (new Thread(new Test.Run(countDownLatch, String.valueOf(phone)))).start();
            }

            countDownLatch.countDown();
        } catch (Exception var5) {
            System.out.println("Exception: " + var5);
        }

    }

    public static void main(String[] args) {
        Test test = new Test();
        test.init();
    }

    private class Run implements Runnable {
        private final CountDownLatch startLatch;
        private final String phone;

        public Run(CountDownLatch startLatch, String phone) {
            this.startLatch = startLatch;
            this.phone = phone;
        }

        public void run() {
            try {
                this.startLatch.await();
                String text = String.format(Test.data, this.phone);
                Test.doHttpPost(text, Test.url, this.phone);
            } catch (Exception var2) {
                var2.printStackTrace();
            }

        }
    }
}
