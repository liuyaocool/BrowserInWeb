package prv.liuyao.BrowserInWeb.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpUtils {

    public enum Method{

        OPTIONS("OPTIONS"), POST("POST"), GET("GET"), HEAD("HEAD"),
        PUT("PUT"), DELETE("DELETE"), CONNECT("CONNECT"), TRACE("TRACE");
        private String method;
        Method(String method) { this.method = method; }
        public String getMethod() { return method; }
    }

    public enum Charset{
        UTF8("utf-8");
        private String code;
        Charset(String code) { this.code = code; }
        public String getCode() { return code; }
    }


    /**
     * 在JAVA代码开发过程中需要用的HTTP请求发送和接收消息。
     * 碰到了个很奇怪的问题，是通过HttpURLConnection的setRequestMethod方法已经设置了请求方式为GET，可最终传递到服务端的却是POST。
     * 通过调试发现关键问题出在getOutputStream方法上。
     * POST请求是将发送的内容放在请求正文中，而GET是放在url之后。两种不同的请求最好用两个不同的方法，或判断。
     *
     * //如果请求方式为POST
     * if("POST".equals(requestMethod)){
     *     PrintWriter printout = new PrintWriter(new OutputStreamWriter(httpurlcon.getOutputStream()));
     *     printout.write(contents);
     *     printout.flush();
     *     printout.close();
     *      ...
     * }
     * GET不需要用 OutputStream将内容写入。
     *
     * @param url
     * @param method
     * @param headers
     * @param data
     * @return
     */
    public static String ajax(String url, Method method,
                              Map<String, String> headers,
                              Map<String, String> data){
        Charset encoding = Charset.UTF8;
        String result = null;
        OutputStreamWriter osw = null;
        try{
            System.out.println("----------------------- request -----------------------");
            HttpURLConnection conn = getConn(url, method, headers);
//            System.out.println("param：" + data);
            //添加其他设置
            conn.setDoOutput(true);

//            conn.setConnectTimeout(5000);
//            conn.setReadTimeout(5000);
            //添加数据包
            switch (method){
                case POST:
                    osw = new OutputStreamWriter(conn.getOutputStream(), encoding.getCode());
                    if (null != data && data.size() > 0){
                        JSONObject json = new JSONObject();
                        for (String key : data.keySet()) {
                            json.put(key, data.get(key));
                        }
                        osw.write(json.toString());
                    }
                    osw.flush();
                    break;
                default: break;
            }
            //请求
            conn.connect();
            System.out.println("----------------------- response ----------------------");
            //并获得响应数据
            if (conn.getResponseCode() == 200) {
                result = getResult(conn, encoding);
            } else {
                result = String.valueOf(conn.getResponseCode());
            }
            System.out.println("result：" + result);
        }catch (Exception e){
//            e.printStackTrace();
            System.out.println("ERROR：" + e.getMessage());
        }finally {
            close(osw);
        }
        System.out.println("======================================== over =============================================");
        return result;
    }

    public static void fileRequest(String url, Method method, Map<String, File> files) {
        System.out.println("=========================请求=================================");
        System.out.println(url);
        String BOUNDARY = UUID.randomUUID().toString(),
                PREFIX = "--",
                LINEND = "\r\n",
                MULTIPART_FROM_DATA = "multipart/form-data";
        Charset encoding = Charset.UTF8;
        DataOutputStream dos = null;
        InputStream is = null;

        Map<String, String> headers = new HashMap<>();
        headers.put("connection", "keep-alive");
        headers.put("Charsert", "UTF-8");
        headers.put("Content-Type", MULTIPART_FROM_DATA + ";boundary=" + BOUNDARY);

        try {
            HttpURLConnection conn = getConn(url, method, headers);

//        conn.setReadTimeout(5 * 1000);
            conn.setDoInput(true);// 允许输入
            conn.setDoOutput(true);// 允许输出
            conn.setUseCaches(false);

            dos = new DataOutputStream(conn.getOutputStream());
            System.out.println("---------------------------------------------------");

            // 发送文件数据
            if (ObjectUtil.isEmpty(files)){
                return;
            }
            for (Map.Entry<String, File> file : files.entrySet()) {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1.append("Content-Disposition: form-data; name=\"files\"; filename=\"" + file.getKey() + "\"" + LINEND);
                sb1.append("Content-Type: application/octet-stream; charset=" + encoding + LINEND);
                sb1.append(LINEND);
                System.out.println(sb1);

                dos.write(sb1.toString().getBytes());
                is = new FileInputStream(file.getValue());
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    dos.write(buffer, 0, len);
                }
                close(is);
                dos.write(LINEND.getBytes());
            }
            System.out.println("-------------------------------");

            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            dos.write(end_data);
            dos.flush();

            System.out.println("=========================响应=================================");
            // 得到响应码
            System.out.println("responseCode："+conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                String result = getResult(conn, encoding);

                com.alibaba.fastjson.JSONObject datas = com.alibaba.fastjson.JSONObject.parseObject(result);
                System.out.println(datas.getInteger("code"));
                System.out.println(datas.getString("message"));
                JSONArray res = datas.getJSONArray("data");
                for (int i = 0; i < res.size(); i++) {
                    System.out.println(res.get(i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(dos);
            close(is);
        }

    }

    private static HttpURLConnection getConn(String url, Method method,
                                             Map<String, String> headers) throws IOException {
        System.out.println(url);
        HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
        conn.setRequestMethod(method.getMethod());
        System.out.println("method：" + conn.getRequestMethod());
        //添加请求头
        if (!ObjectUtil.isEmpty(headers)){
            for(String key: headers.keySet()){
                conn.setRequestProperty(key, headers.get(key));
                System.out.println(key + "：" + headers.get(key));
            }
        }
        return conn;
    }

    public static String getResult(HttpURLConnection conn, Charset encoding){
        InputStreamReader isr = null;
        BufferedReader br = null;
        String result = null;
        try {
            isr = new InputStreamReader(conn.getInputStream(), encoding.getCode());
            br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null){
                sb.append(line);
            }
            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            close(isr);
            close(br);
            conn.disconnect();
        }
        return result;
    }


    public static void close(AutoCloseable clo){
        try {
            if (null != clo){ clo.close(); }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T extends com.alibaba.fastjson.JSON> T toJson(String jsonStr) {
        return (T) com.alibaba.fastjson.JSON.parse(jsonStr);
    }

    public static void main(String[] args) throws IOException {
        System.out.println(UUID.randomUUID().toString());
        Map<String, String> headers = new HashMap<>();
        Map<String, String> data = new HashMap<>();

        String addr = "http://127.0.0.1:8083/all/getPage";
        addr = "http://1770.ztshangwu.com/?channelNo=1770#/video?video_id=153";
        addr = "https://www.baidu.com";
//        headers.put("Content-Type", "text/html");
//        headers.put("User-Agent", "iPhone");
        String res = ajax(addr, Method.GET, headers, data);

        Document document = Jsoup.parse(res);
        Elements script = document.getElementsByTag("script");
        for (Element e: script){

            System.out.println();
        }

//        headers.clear();
//        doAjax(addr + "getString", Method.GET, headers, data);//ok
//        doAjax(addr + "getJsonString", Method.GET, headers, data);//ok
//        doAjax(addr + "getJsonMap", Method.GET, headers, data);//ok
//        doAjax(addr + "postString", Method.POST, headers, data);
//        doAjax(addr + "postJsonString", Method.POST, headers, data);//ok
//        doAjax(addr + "postJson", Method.POST, headers, data);//ok
//        doAjax(addr + "postJsonMap", Method.POST, headers, data);//ok

    }

}
