package com.szcinda.sf;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping({"/callback"})
public class SFController {
    @Value("${file.path}")
    private String filePath;
    @Value("${redis.pattern}")
    private String pattern;
    @Value("${redis.file.pattern}")
    private String filePattern;
    private static final ConcurrentLinkedDeque<CallbackData> queue = new ConcurrentLinkedDeque<>();
    private static final ConcurrentLinkedDeque<String> queue2 = new ConcurrentLinkedDeque<>();
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public SFController() {
    }

    @GetMapping({"/test"})
    public String hello() {
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    @PostMapping({"data2"})
    public void callback2(@RequestBody CallbackData callbackData) {
        queue.add(callbackData);
    }

    @PostMapping({"data"})
    public void callback(HttpServletRequest request) throws Exception {
        String callbackData = this.getBodyString(request);
        System.out.println(this.pattern + ":callbackData:" + callbackData);
        queue2.add(callbackData);
    }

    @Scheduled(
            cron = "0 0/1 * * * ? "
    )
    private void addToRedis() {
        List<String> dataList = new ArrayList<>();

        while (!queue2.isEmpty()) {
            String callbackDataString = queue2.poll();
            dataList.add(callbackDataString);
        }

        System.out.println(this.pattern + ":dataList size:" + dataList.size());
        if (dataList.size() > 0) {
            dataList.forEach((data) -> {
                this.redisTemplate.opsForValue().set(this.pattern + UUID.randomUUID().toString(), data);
                System.out.println(this.pattern + "********************added*********************");
            });
        }

    }

    @GetMapping({"testRedis/{number}"})
    public void testRedis(@PathVariable String number) {
        int num = Integer.parseInt(number);
        int count = num * 10000;
        StopWatch stopWatch = new StopWatch("REDIS " + count + " KEY 性能测试");
        stopWatch.start("写入REDIS缓存");

        for (int i = 0; i < count; ++i) {
            this.redisTemplate.opsForValue().set(this.pattern + ":test" + i, "value" + i, 1800L, TimeUnit.SECONDS);
        }

        stopWatch.stop();
        stopWatch.start("读取REDIS缓存");
        Set<String> keys = this.redisTemplate.keys("H*");
        stopWatch.stop();
        stopWatch.start("删除REDIS缓存");
        if (keys != null && keys.size() > 0) {
            Iterator var6 = keys.iterator();

            while (var6.hasNext()) {
                String phone = (String) var6.next();
                this.redisTemplate.delete(phone);
            }
        }
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
    }

    @GetMapping({"deleteTodayLog"})
    public String deleteTodayLog() throws Exception {
        String date = LocalDateTime.now().toLocalDate().toString();
        File file = new File(this.filePath + File.separator + this.filePattern + date + ".csv");
        if (file.exists()) {
            file.delete();
        }
        if (file.exists()) {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
            bw.write(new String(new byte[]{-17, -69, -65}));
            bw.write("手机号,外呼记录,外呼时间,结束时间");
            bw.newLine();
            bw.close();
        }

        return "SUCCESS";
    }

    @GetMapping({"/phoneStatus"})
    public void phoneStatus(HttpServletResponse response) throws Exception {
        String date = LocalDateTime.now().toLocalDate().toString();
        File file = new File(this.filePath + File.separator + this.filePattern + date + ".csv");
        if (!file.exists()) {
            file.createNewFile();
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(new String(new byte[]{-17, -69, -65}));
            bw.write("手机号,外呼记录,外呼时间,结束时间");
            bw.newLine();
            bw.close();
        }

        response.setContentType("application/force-download");
        response.addHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(date + ".csv", "UTF-8"));
        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            OutputStream os = response.getOutputStream();

            for (int i = bis.read(buffer); i != -1; i = bis.read(buffer)) {
                os.write(buffer, 0, i);
            }
        } catch (Exception var21) {
            var21.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (Exception var20) {
                    var20.printStackTrace();
                }
            }

            if (fis != null) {
                try {
                    fis.close();
                } catch (Exception var19) {
                    var19.printStackTrace();
                }
            }

        }

    }

    public String getBodyString(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            inputStream = request.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            char[] bodyCharBuffer = new char[1024];
            boolean var6 = false;

            int len;
            while ((len = reader.read(bodyCharBuffer)) != -1) {
                sb.append(new String(bodyCharBuffer, 0, len));
            }
        } catch (IOException var19) {
            var19.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException var18) {
                    var18.printStackTrace();
                }
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException var17) {
                    var17.printStackTrace();
                }
            }

        }

        return sb.toString();
    }

    @Scheduled(
            cron = "0 0/2 * * * ? "
    )
    private void saveLog() throws Exception {
        String date = LocalDateTime.now().toLocalDate().toString();
        File file = new File(this.filePath + File.separator + this.filePattern + date + ".csv");
        boolean isNew = false;
        if (!file.exists()) {
            isNew = file.createNewFile();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
        if (isNew) {
            bw.write(new String(new byte[]{-17, -69, -65}));
            bw.write("手机号,外呼记录,外呼时间,结束时间");
            bw.newLine();
        }

        List<CallbackData> dataList = new ArrayList<>();
        Set<String> keys = this.redisTemplate.keys(this.pattern + "*");
        Iterator var7;
        if (keys != null && keys.size() > 0) {
            System.out.println(this.pattern + ":Set<String> keys:" + keys.size());
            var7 = keys.iterator();

            while (var7.hasNext()) {
                String time = (String) var7.next();
                String data = this.redisTemplate.opsForValue().get(time);
                if (data != null) {
                    try {
                        CallbackData callbackData = this.objectMapper.readValue(data, CallbackData.class);
                        dataList.add(callbackData);
                        this.redisTemplate.delete(time);
                    } catch (Exception var11) {
                        var11.printStackTrace();
                    }
                }
            }
        }

        System.out.println(this.pattern + "写入文本的数量:" + dataList.size());
        if (dataList.size() > 0) {
            var7 = dataList.iterator();

            while (var7.hasNext()) {
                CallbackData callbackData = (CallbackData) var7.next();
                bw.write(callbackData.getSubject().getCalled() + "," + callbackData.getSubject().getDuration() + "," + callbackData.getSubject().getCreateTime() + "," + callbackData.getSubject().getReleaseTime());
                bw.newLine();
            }
        }

        bw.close();
    }
}