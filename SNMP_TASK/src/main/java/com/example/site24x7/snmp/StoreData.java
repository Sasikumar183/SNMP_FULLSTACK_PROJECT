package com.example.site24x7.snmp;

import java.sql.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class StoreData {
    private static final Map<Integer, Long> previousInTraffic = new HashMap<>();
    private static final Map<Integer, Long> previousOutTraffic = new HashMap<>();
    private static final Map<Integer, Integer> previousInErrors = new HashMap<>();
    private static final Map<Integer, Integer> previousOutErrors = new HashMap<>();
    private static final Map<Integer, Integer> previousInDiscards = new HashMap<>();
    private static final Map<Integer, Integer> previousOutDiscards = new HashMap<>();
    
    static void fetchData() {
        String apiURL = "http://localhost:9090/SNMP_TASK/GetData";
        StringBuilder data = new StringBuilder();

        try {
            @SuppressWarnings("deprecation")
			URL url = new URL(apiURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.err.println("Failed to fetch data: HTTP " + conn.getResponseCode());
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    data.append(line);
                }
            }

            if (data.length() == 0) {
                System.err.println("Error: API response is empty.");
                return;
            }

            JSONObject jsonobj = new JSONObject(data.toString());
            JSONArray dataArray = jsonobj.getJSONArray("data");

            try (Connection con = DatabaseConfig.getConnection()) {
                if (con == null || con.isClosed()) {
                    System.err.println("Error: Database connection is null or closed.");
                    return;
                }

                String interfaceQuery = """
                		INSERT INTO interface (idx, interface_name, IP) 
						VALUES (?, ?, ?) 
						ON DUPLICATE KEY UPDATE 
						interface_name = IF(interface_name = VALUES(interface_name), interface_name, interface_name);

                		""";


                String findQuery = "SELECT id FROM interface WHERE idx = ? AND IP = ?";

                String detailsQuery = "INSERT INTO inter_details (id, in_traffic, out_traffic, in_error, out_error, in_discard, out_discard, admin_status, oper_status, collected_time) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

                try (PreparedStatement interfaceStmt = con.prepareStatement(interfaceQuery);
                     PreparedStatement findStmt = con.prepareStatement(findQuery);
                     PreparedStatement detailsStmt = con.prepareStatement(detailsQuery)) {

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject obj = dataArray.getJSONObject(i);

                        int idx = obj.optInt("Interface ID", -1);
                        if(idx==-1) {
                        	continue;
                        }
                        String interfaceName = obj.optString("Interface Name", "Unknown");
                        String IP = obj.optString("IP", "0.0.0.0");
                        long inBytes = obj.optLong("inBytes", 0);
                        long outBytes = obj.optLong("outBytes", 0);
                        int inErrors = obj.optInt("inErrors", 0);
                        int outErrors = obj.optInt("outErrors", 0);
                        int inDiscards = obj.optInt("inDiscards", 0);
                        int outDiscards = obj.optInt("outDiscards", 0);
                        int adminStatus = obj.optInt("adminStatus", 0);
                        int operationStatus = obj.optInt("operationStatus", 0);

                        // Insert into `interface` table
                        interfaceStmt.setInt(1, idx);
                        interfaceStmt.setString(2, interfaceName);
                        interfaceStmt.setString(3, IP);
                        interfaceStmt.executeUpdate();

                        // Find the `id` for the inserted or updated entry
                        findStmt.setInt(1, idx);
                        findStmt.setString(2, IP);

                        int interfaceId = -1;
                        try (ResultSet rs = findStmt.executeQuery()) {
                            if (rs.next()) {
                                interfaceId = rs.getInt("id");
                            } else {
                                throw new SQLException("Failed to retrieve interface ID.");
                            }
                        }

                        // Check if this is the first time storing data for this id
                        if (!previousInTraffic.containsKey(interfaceId) || !previousOutTraffic.containsKey(interfaceId)
                                || !previousInErrors.containsKey(interfaceId) || !previousOutErrors.containsKey(interfaceId)
                                || !previousInDiscards.containsKey(interfaceId) || !previousOutDiscards.containsKey(interfaceId)) {

                            // Store the first values but don't insert them into the database yet
                            previousInTraffic.put(interfaceId, inBytes);
                            previousOutTraffic.put(interfaceId, outBytes);
                            previousInErrors.put(interfaceId, inErrors);
                            previousOutErrors.put(interfaceId, outErrors);
                            previousInDiscards.put(interfaceId, inDiscards);
                            previousOutDiscards.put(interfaceId, outDiscards);
                        } 
                        else {
                            // Compute delta for traffic
                            long prevInBytes = previousInTraffic.get(interfaceId);
                            long prevOutBytes = previousOutTraffic.get(interfaceId);
                            long deltaIn = (inBytes >= prevInBytes) ? (inBytes - prevInBytes) : inBytes;
                            long deltaOut = (outBytes >= prevOutBytes) ? (outBytes - prevOutBytes) : outBytes;

                            // Compute delta for errors
                            int prevInErrors = previousInErrors.get(interfaceId);
                            int prevOutErrors = previousOutErrors.get(interfaceId);
                            int deltaInErrors = (inErrors >= prevInErrors) ? (inErrors - prevInErrors) : inErrors;
                            int deltaOutErrors = (outErrors >= prevOutErrors) ? (outErrors - prevOutErrors) : outErrors;

                            // Compute delta for discards
                            int prevInDiscards = previousInDiscards.get(interfaceId);
                            int prevOutDiscards = previousOutDiscards.get(interfaceId);
                            int deltaInDiscards = (inDiscards >= prevInDiscards) ? (inDiscards - prevInDiscards) : inDiscards;
                            int deltaOutDiscards = (outDiscards >= prevOutDiscards) ? (outDiscards - prevOutDiscards) : outDiscards;

                            // Insert into `inter_details` table
                            detailsStmt.setInt(1, interfaceId);
                            detailsStmt.setLong(2, deltaIn);
                            detailsStmt.setLong(3, deltaOut);
                            detailsStmt.setInt(4, deltaInErrors);
                            detailsStmt.setInt(5, deltaOutErrors);
                            detailsStmt.setInt(6, deltaInDiscards);
                            detailsStmt.setInt(7, deltaOutDiscards);
                            detailsStmt.setInt(8, adminStatus);
                            detailsStmt.setInt(9, operationStatus);
                            detailsStmt.executeUpdate();

                            // Update previous values
                            previousInTraffic.put(interfaceId, inBytes);
                            previousOutTraffic.put(interfaceId, outBytes);
                            previousInErrors.put(interfaceId, inErrors);
                            previousOutErrors.put(interfaceId, outErrors);
                            previousInDiscards.put(interfaceId, inDiscards);
                            previousOutDiscards.put(interfaceId, outDiscards);
                        }
                    }
                }
                //System.out.println("Traffic Value "+previousInTraffic);

            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.err.println("Error fetching API data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

