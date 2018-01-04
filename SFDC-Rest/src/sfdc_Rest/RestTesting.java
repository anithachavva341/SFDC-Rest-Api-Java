package sfdc_Rest;
/*
 * author:anwesh,
 * Description:Salesforce rest api integration with Java,
 * Operation:Crud Functionality with Custom object Like Candidate
 */
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.ClientProtocolException;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.JSONException;
 
public class RestTesting {
 
private static final String LOGINURL = "https://login.salesforce.com";
private static final String GRANTTYPE = "/services/oauth2/token?grant_type=password";
private static final String CLIENTID = "xxxxxxxx";
private static final String CLIENTSECRET = "xxxxxxxxx";
private static final String USERID = "xxxxxx";//salesforce account Id
private static final String PASSWORD = "xxxxxxx";//PassWord&security token
private static final String ACCESSTOKEN = "access_token";
private static final String INSTANCEURL = "instance_url";
private static String instanceUrl;
private static Header oAuthHeader;
private static Header printHeader = new BasicHeader("X-PrettyPrint", "1");
private static String candidateId;
private static String FirstName;
private static String LastName;
private static String Phone;
private static String leadId ;
private static String leadFirstName;
private static String leadLastName;
private static String leadCompany;
public static void main(String[] args) {
	 
HttpClient httpclient = HttpClientBuilder.create().build();
 
String loginURL = LOGINURL + GRANTTYPE + 
"&client_id=" + CLIENTID + 
"&client_secret=" + CLIENTSECRET + 
"&username=" + USERID + 
"&password=" + PASSWORD;
 
HttpPost httpPost = new HttpPost(loginURL);
HttpResponse httpResponse = null;
 
try {
httpResponse = httpclient.execute(httpPost);
} catch (ClientProtocolException clientProtocolException) {
clientProtocolException.printStackTrace();
} catch (IOException ioException) {
ioException.printStackTrace();
} catch (Exception exception) {
exception.printStackTrace();
}
 
final int statusCode = httpResponse.getStatusLine().getStatusCode();
if (statusCode != HttpStatus.SC_OK) {
System.out.println("Error authenticating to Salesforce.com platform: " + statusCode);
return;
}
 
String httpMessage = null;
try {
httpMessage = EntityUtils.toString(httpResponse.getEntity());
} catch (IOException ioException) {
ioException.printStackTrace();
}
 
JSONObject jsonObject = null;
String accessToken = null;
try {
jsonObject = (JSONObject) new JSONTokener(httpMessage).nextValue();
accessToken = jsonObject.getString(ACCESSTOKEN);
instanceUrl = jsonObject.getString(INSTANCEURL);
} catch (JSONException jsonException) {
jsonException.printStackTrace();
}
 
oAuthHeader = new BasicHeader("Authorization", "OAuth " + accessToken) ;
 					//Crud Operation
					getCandidates();
					deleteCandidate();
					createLeads();
					
 
httpPost.releaseConnection();
}

public static void getCandidates() {
 
System.out.println("Query Candiadte Records");
 
try {
 
HttpClient httpClient = HttpClientBuilder.create().build();
 
String finalURI = instanceUrl + "/services/data/v41.0/query?q=Select+Id+,+First_Name__c+,+Last_Name__c+,+Phone__c+From+Candidates__c+Limit+5";
System.out.println("Query URL: " + finalURI);
HttpGet httpGet = new HttpGet(finalURI);
httpGet.addHeader(oAuthHeader);
httpGet.addHeader(printHeader);
 
HttpResponse httpResponse = httpClient.execute(httpGet);
 
int statusCode = httpResponse.getStatusLine().getStatusCode();
if (statusCode == 200) {
String responseString = EntityUtils.toString(httpResponse.getEntity());
try {
JSONObject jsonObject = new JSONObject(responseString);
System.out.println("JSON result of Query:\n" + jsonObject.toString(1));
JSONArray jsonArray = jsonObject.getJSONArray("records");
for (int i = 0; i < jsonArray.length(); i++){
candidateId = jsonObject.getJSONArray("records").getJSONObject(i).getString("Id");
FirstName = jsonObject.getJSONArray("records").getJSONObject(i).getString("First_Name__c");
LastName = jsonObject.getJSONArray("records").getJSONObject(i).getString("Last_Name__c");
Phone = jsonObject.getJSONArray("records").getJSONObject(i).getString("Phone__c");

//Since the values are available, can be used later to create objects.
}
} catch (JSONException jsonException) {
jsonException.printStackTrace();
}
} else {
System.out.print("Query was unsuccessful. Status code returned is " + statusCode);
System.out.println(httpResponse.getEntity().getContent());
System.exit(-1);
}
} catch (IOException ioException) {
ioException.printStackTrace();
} catch (Exception exception) {
exception.printStackTrace();
}
}
public static void deleteCandidate() {
    System.out.println("\n_______________Candidate DELETE _______________");

    //Notice, the id for the record to update is part of the URI, not part of the JSON
    String uri = instanceUrl +"/services/data/v41.0/sobjects/Candidates__c/a0H7F000001R41j";
    try {
        //Set up the objects necessary to make the request.
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpDelete httpDelete = new HttpDelete(uri);
        httpDelete.addHeader(oAuthHeader);
        httpDelete.addHeader(printHeader);

        //Make the request
        HttpResponse response = httpClient.execute(httpDelete);

        //Process the response
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 204) {
            System.out.println("Deleted the Candidate successfully.");
        } else {
            System.out.println("Candidate  delete NOT successful. Status code is " + statusCode);
        }
    } catch (JSONException e) {
        System.out.println("Issue creating JSON or processing results");
        e.printStackTrace();
    } catch (IOException ioe) {
        ioe.printStackTrace();
    } catch (NullPointerException npe) {
        npe.printStackTrace();
    }
}

public static void createLeads() {
    System.out.println("\n_______________ Lead INSERT _______________");

    String uri = instanceUrl + "/services/data/v41.0/sobjects/Lead";
    try {

        //create the JSON object containing the new lead details.
        JSONObject lead = new JSONObject();
        lead.put("FirstName", "REST API");
        lead.put("LastName", "Lead");
        lead.put("Company", "SalesforceWorld.com");
     

        System.out.println("JSON for lead record to be inserted:\n" + lead.toString(1));

        //Construct the objects needed for the request
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader(oAuthHeader);
        httpPost.addHeader(printHeader);
        // The message we are going to post
        StringEntity body = new StringEntity(lead.toString(1));
        body.setContentType("application/json");
        httpPost.setEntity(body);

        //Make the request
        HttpResponse response = httpClient.execute(httpPost);

        //Process the results
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 201) {
            String response_string = EntityUtils.toString(response.getEntity());
            JSONObject json = new JSONObject(response_string);
            // Store the retrieved lead id to use when we update the lead.
            leadId = json.getString("id");
            System.out.println("New Lead id from response: " + leadId);
        } else {
            System.out.println("Insertion unsuccessful. Status code returned is " + statusCode);
        }
    } catch (JSONException e) {
        System.out.println("Issue creating JSON or processing results");
        e.printStackTrace();
    } catch (IOException ioe) {
        ioe.printStackTrace();
    } catch (NullPointerException npe) {
        npe.printStackTrace();
    }
}

	}
