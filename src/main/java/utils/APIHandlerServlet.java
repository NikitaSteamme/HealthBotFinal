package main.java.utils;


import main.java.functions.DiseaseReqest;
import main.java.functions.Login;
import main.java.functions.Registration;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static main.java.utils.JSONResponses.ERROR_INCORRECT_REQUEST;

/**
 * Created by Nikita on 29.08.2016.
 */
@WebServlet(urlPatterns = "/lol")
public class APIHandlerServlet extends HttpServlet {
    public abstract static class APIRequestHandler {

        protected abstract JSONStreamAware processRequest(HttpServletRequest request) throws Exception;

    }

    private static Map<String, APIRequestHandler> apiRequestHandlers = new HashMap<>();

    static {
        Map<String, APIRequestHandler> map = new HashMap<>();

        map.put("registration", Registration.getInstance());
        map.put("login", Login.getInstance());
        map.put("diseaserequest", DiseaseReqest.getInstance());

        apiRequestHandlers = Collections.unmodifiableMap(map);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    public static Map<String, APIRequestHandler> getApiRequestHandlers() {
        return apiRequestHandlers;
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setDateHeader("Expires", 0);

        JSONStreamAware response = JSON.emptyJSON;

        try {

            long startTime = System.currentTimeMillis();

            String requestType = req.getParameter("requestType");
            if (requestType == null) {
                response = ERROR_INCORRECT_REQUEST;
                return;
            }

            APIRequestHandler apiRequestHandler = apiRequestHandlers.get(requestType);
            if (apiRequestHandler == null) {
                response = ERROR_INCORRECT_REQUEST;
                return;
            }
            response = apiRequestHandler.processRequest(req);
            if (response instanceof JSONObject) {
                ((JSONObject) response).put("requestProcessingTime", System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resp.setContentType("text/plain; charset=UTF-8");
            try (Writer writer = resp.getWriter()) {
                response.writeJSONString(writer);
            }
        }

    }

}
