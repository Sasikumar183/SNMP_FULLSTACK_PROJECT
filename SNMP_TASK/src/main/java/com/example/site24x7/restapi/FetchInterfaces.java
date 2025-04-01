package com.example.site24x7.restapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.json.JSONObject;

import com.example.site24x7.db.DatabaseConfig;
import com.opensymphony.xwork2.ActionSupport;

@SuppressWarnings({ "deprecation", "serial" })

public class FetchInterfaces extends ActionSupport implements ServletRequestAware {
    private InputStream input;
    private HttpServletRequest request;

    public String execute() {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            input = new ByteArrayInputStream("Invalid Request Method".getBytes(StandardCharsets.UTF_8));
            return SUCCESS;
        }
        
        JSONArray inter_details = new JSONArray();
        String ip = request.getParameter("ip");
        String query = "SELECT idx,interface_name from interface where IP = ?;";

        
        try (Connection con = DatabaseConfig.getConnection();
        	     PreparedStatement stmt = con.prepareStatement(query)) {
        	    
        	    // Correctly set the parameter in the query
        	    stmt.setString(1, ip);

        	    try (ResultSet rs = stmt.executeQuery()) {
        	        while (rs.next()) {
        	            JSONObject obj = new JSONObject();
        	            obj.put("interface_name", rs.getString("interface_name"));  // Fix duplicate "index"
        	            obj.put("index", rs.getInt("idx"));  // Unique key name
        	            inter_details.put(obj);  // Store the JSON object correctly
        	        }
        	    }
            JSONObject res = new JSONObject();
            res.put("data", inter_details);
            String jsonString = res.toString(4);
            input = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

        } catch (SQLException e) {
            e.printStackTrace();
            input = new ByteArrayInputStream("Database Error".getBytes(StandardCharsets.UTF_8));
        }

        return SUCCESS;
    }

    public InputStream getInput() {
        return input;
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }
}
