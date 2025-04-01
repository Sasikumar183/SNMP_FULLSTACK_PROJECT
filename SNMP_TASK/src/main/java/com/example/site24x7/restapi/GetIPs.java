package com.example.site24x7.restapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONArray;
import org.json.JSONObject;

import com.example.site24x7.db.DatabaseConfig;
import com.opensymphony.xwork2.ActionSupport;

@SuppressWarnings({ "deprecation", "serial" })

public class GetIPs extends ActionSupport implements ServletRequestAware {
    private InputStream input;
    private HttpServletRequest request;

    public String execute() {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            input = new ByteArrayInputStream("Invalid Request Method".getBytes(StandardCharsets.UTF_8));
            return SUCCESS;
        }

        JSONArray ipList = new JSONArray();
        String query = "SELECT DISTINCT IP FROM interface";

        try (Connection con = DatabaseConfig.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                ipList.put(rs.getString(1));  // Fixed index from 0 to 1
            }

            JSONObject res = new JSONObject();
            res.put("data", ipList);
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
