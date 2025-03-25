package com.example.action;

import com.opensymphony.xwork2.ActionSupport;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;

public class ItemRestAction extends ActionSupport {
    private static final long serialVersionUID = 1L;
    private InputStream inputStream;

    public String execute() {
        List<Map<String, Object>> items = new ArrayList<>();
        
        Map<String, Object> obj = new HashMap<>();
        obj.put("name", "Sasi");
        obj.put("age", 18);
        items.add(obj);

        JSONArray jsonArray = new JSONArray(items);
        String jsonString = jsonArray.toString();

        inputStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
        return SUCCESS;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
