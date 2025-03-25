package com.example.site24x7.restapi;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;

import com.example.action.DatabaseConfig;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;

@SuppressWarnings("deprecation")
public class RenameInterfaceName extends ActionSupport implements ServletRequestAware {

    private static final long serialVersionUID = 1L;
    private InputStream input;
    private HttpServletRequest request;

    public String execute() {
    	
    	if (!"PATCH".equalsIgnoreCase(request.getMethod())) {
            input = new ByteArrayInputStream("Invalid Request Method".getBytes(StandardCharsets.UTF_8));
            return SUCCESS;
        }
    	
        String name = request.getParameter("name");
        int id = Integer.parseInt(request.getParameter("id"));
        String query = "UPDATE interface SET interface_name = ? WHERE id = ?";

        // DB Connection
        try {
            Connection con = DatabaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();
            String message = (rows == 1) ? "Updated" : "No interface with that id";
            JSONObject res = new JSONObject();
            res.put("message", message);
            String jsonString = res.toString(4);
            input = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

            ps.close();
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
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
