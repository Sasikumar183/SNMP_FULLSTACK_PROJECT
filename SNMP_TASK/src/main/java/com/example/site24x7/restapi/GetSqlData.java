package com.example.site24x7.restapi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import com.example.action.DatabaseConfig;

public class GetSqlData {
    
    public static JSONObject getData(StringBuilder query) {
        JSONObject jsonRes = new JSONObject();

        try {
            Connection con = DatabaseConfig.getConnection();
            PreparedStatement ps = con.prepareStatement(query.toString());
            ResultSet mysqlResultSet = ps.executeQuery();

            String statusQuery = """
                SELECT id, oper_status, admin_status
                FROM (
                    SELECT id, oper_status, admin_status, 
                           ROW_NUMBER() OVER (PARTITION BY id ORDER BY collected_time DESC) AS rn
                    FROM inter_details
                ) t
                WHERE rn = 1;
            """;

            PreparedStatement statusStmt = con.prepareStatement(statusQuery);
            ResultSet statusResultSet = statusStmt.executeQuery();
            JSONObject statusMap = new JSONObject();

            while (statusResultSet.next()) {
                JSONObject statusObj = new JSONObject();
                statusObj.put("oper_status", statusResultSet.getString("oper_status"));
                statusObj.put("admin_status", statusResultSet.getString("admin_status"));
                statusMap.put(String.valueOf(statusResultSet.getInt("id")), statusObj);
            }

            JSONArray dataArray = new JSONArray();
            while (mysqlResultSet.next()) {
                JSONObject record = new JSONObject();
                int interfaceId = mysqlResultSet.getInt("id");

                record.put("interface_id", interfaceId);
                record.put("index", mysqlResultSet.getInt("idx"));
                record.put("interface_name", mysqlResultSet.getString("interface_name"));
                record.put("interface_ip", mysqlResultSet.getString("IP"));
                record.put("avg_in_traffic", mysqlResultSet.getDouble("avg_in_traffic"));
                record.put("avg_out_traffic", mysqlResultSet.getDouble("avg_out_traffic"));
                record.put("avg_in_error", mysqlResultSet.getDouble("avg_in_error"));
                record.put("avg_out_error", mysqlResultSet.getDouble("avg_out_error"));
                record.put("avg_in_discard", mysqlResultSet.getDouble("avg_in_discard"));
                record.put("avg_out_discard", mysqlResultSet.getDouble("avg_out_discard"));

                if (statusMap.has(String.valueOf(interfaceId))) {
                    JSONObject statusData = statusMap.getJSONObject(String.valueOf(interfaceId));
                    record.put("oper_status", statusData.getString("oper_status"));
                    record.put("admin_status", statusData.getString("admin_status"));
                } else {
                    record.put("oper_status", JSONObject.NULL);
                    record.put("admin_status", JSONObject.NULL);
                }

                dataArray.put(record);
            }

            ps.close();
            mysqlResultSet.close();
            statusStmt.close();
            statusResultSet.close();
            con.close();

            jsonRes.put("data", dataArray);

        } catch (SQLException e) {
            e.printStackTrace();
            jsonRes.put("error", "DB ERROR");
        }
        return jsonRes;
    }
}
