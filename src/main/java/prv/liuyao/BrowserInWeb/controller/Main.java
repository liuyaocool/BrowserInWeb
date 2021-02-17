package prv.liuyao.BrowserInWeb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import prv.liuyao.BrowserInWeb.config.SysConfig;
import prv.liuyao.BrowserInWeb.utils.HttpUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/")
public class Main {

    private final Map<HttpSession, Long> loginMap =
            new ConcurrentHashMap<>();
    private Thread t;

    @Autowired
    private SysConfig config;

    @GetMapping("login")
    public String login(){
        return "login";
    }

    @GetMapping("check")
    public String check(HttpServletRequest request, String password){
        if (config.getPassword().equals(password)){
            addLogin(request.getSession());
            return "main";
        }
        return "error";
    }

    @GetMapping("doBrowser")
    @ResponseBody
    public String doBrowser(HttpServletRequest request, String url){
        if (!checkLogin(request.getSession())) {
            return "<h1>Error</h1>";
        }
        String html = HttpUtils.ajax(url, HttpUtils.Method.GET, null, null);
        return html;
    }



    private synchronized void addLogin(HttpSession session){
        loginMap.put(session, System.currentTimeMillis());
        checkTimeout();
    }
    private synchronized boolean checkLogin(HttpSession session){
        if (loginMap.containsKey(session)) {
            loginMap.put(session, System.currentTimeMillis());
            return true;
        }
        return false;
    }
    public void checkTimeout(){
        if (null == t){
            t = new Thread(()->{
                long min5 = 300000;
                while (true) {
                    synchronized (this){
                        if (!loginMap.isEmpty()){
                            long t = System.currentTimeMillis();
                            for (HttpSession s: loginMap.keySet()){
                                if (loginMap.get(s) - t > min5) {
                                    loginMap.remove(s);
                                }
                            }
                        }
                        if (loginMap.isEmpty()) {
                            break;
                        }
                    }
                    try {
                        Thread.sleep(min5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        if (!t.isAlive()) {
            t.start();
        }
    }
}
