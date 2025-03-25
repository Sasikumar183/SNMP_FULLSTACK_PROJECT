package com.example.site24x7.restapi;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import javax.servlet.http.HttpServletRequest;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.json.JSONObject;


@SuppressWarnings("deprecation")
public class GetData extends ActionSupport implements ServletRequestAware{
	private static final long serialVersionUID = 1L;
    private InputStream input;
    private HttpServletRequest request;
    
    public String execute() throws NumberFormatException, SQLException {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            input = new ByteArrayInputStream("Invalid Request Method".getBytes(StandardCharsets.UTF_8));
            return SUCCESS;
        }

        String time = request.getParameter("time");
        
        String ip =  request.getParameter("ip");
//        System.out.println(ip);

        String interval = time.equals("1h") ? "1 HOUR" :
                          time.equals("6h") ? "6 HOUR" :
                          time.equals("12h") ? "12 HOUR" :
                          time.equals("1d") ? "1 DAY" :
                          time.equals("1w") ? "7 DAY" :
                          time.equals("30d") ? "30 DAY" : "NA";
        
        

 
        //For Specific interface
        if (interval.equals("NA")) {
            input = new ByteArrayInputStream("Invalid time interval".getBytes(StandardCharsets.UTF_8));
            return SUCCESS;
        }
        
        String id = request.getParameter("id");
        if (id != null && !id.isEmpty()) {
            //System.out.println("Parameter contains the search string!");
            JSONObject generalDetails = new JSONObject();
            JSONObject general = GetSpecificInterface.getGeneralDetails(Integer.parseInt(id));
            JSONObject interfaceInsight = GetSpecificInterface.getInsights(Integer.parseInt(id),time,ip);
            JSONObject status = GetSpecificInterface.getCurrentStatus(Integer.parseInt(id));
            
            generalDetails.append("general", general);
            generalDetails.append("status",status);
            generalDetails.append("data", interfaceInsight);
            input = new ByteArrayInputStream(generalDetails.toString(4).getBytes(StandardCharsets.UTF_8));

        }
        
        
        
        //For dashboard
        else {
        	StringBuilder query = new StringBuilder(
        		    "SELECT " +
        		    "    inter_details.id, " +
        		    "    interface.idx, " +
        		    "    interface.interface_name, " +
        		    "    interface.IP, " +
        		    "    AVG(in_traffic) AS avg_in_traffic, " +
        		    "    AVG(out_traffic) AS avg_out_traffic, " +
        		    "    AVG(in_error) AS avg_in_error, " +
        		    "    AVG(out_error) AS avg_out_error, " +
        		    "    AVG(in_discard) AS avg_in_discard, " +
        		    "    AVG(out_discard) AS avg_out_discard " +
        		    "FROM inter_details " +
        		    "JOIN interface ON inter_details.id = interface.id " +
        		    "WHERE collected_time >= NOW() - INTERVAL " + interval + " AND interface.IP = '"+ip+
        		    "' GROUP BY inter_details.id, interface.idx, interface.interface_name, interface.IP;"
        		);
            JSONObject jsonRes,jsonRes2,finalRes;
			try {
				jsonRes = GetSqlData.getData(query);
				
				if (! (time.equals("1h") || time.equals("6h"))){
					jsonRes2 = GetCassandraData.getData(time,ip);
//					System.out.println("MYSQL");
//					System.out.println(jsonRes.toString(4));
//					System.out.println("Cassandra");
//					System.out.println(jsonRes2.toString(4));
					finalRes = AggreageTwoDB.getAggregate(jsonRes.getJSONArray("data"), jsonRes2.getJSONArray("data"));
					String jsonString = finalRes.toString(4); 
		            input = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
				}
				
				else {
					String jsonString = jsonRes.toString(4);
		            input = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
				}
				//System.out.println("RES 2 is"+ jsonRes2.toString(4));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
	            input = new ByteArrayInputStream("Database error".getBytes(StandardCharsets.UTF_8));
			}
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
