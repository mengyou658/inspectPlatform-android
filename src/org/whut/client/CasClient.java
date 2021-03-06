package org.whut.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.whut.platform.MainActivity;
import org.whut.platform.UploadActivity;
import org.whut.utils.CustomMultiPartEntity;
import org.whut.utils.CustomMultiPartEntity.ProgressListener;
import org.whut.utils.FileUtils;

import android.annotation.SuppressLint;
import android.os.Message;
import android.util.Log;

@SuppressWarnings("deprecation")
public class CasClient
{
	private static final String CAS_LOGIN_URL_PART = "tickets";
	private static final String CAS_LOGOUT_URL_PART = "tickets";

	private static final int REQUEST_TIMEOUT = 5*1000;
	private static final int SO_TIMEOUT = 10*1000;  

	private static DefaultHttpClient httpClient;

	private String casBaseURL;

	private static CasClient instance;
	
	long Filelength;

	private DefaultHttpClient getHttpClient()
	{
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);
		DefaultHttpClient client = new DefaultHttpClient(httpParams);
		client.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,"UTF-8");
		return client;
	}

	private CasClient (String casBaseUrl)
	{
		CasClient.httpClient = getHttpClient();
		this.casBaseURL = casBaseUrl;
	}


	public static CasClient getInstance(){
		if(instance==null){ 
			synchronized(CasClient.class){
				if(instance==null){
					instance = new CasClient("http://123.57.236.123/cas/v1/");
				}
			}
		}
		return instance;
	}

	public boolean login(String username,String password,String sessionGenerateService){
		String tgt = getTGT(username, password);
		if(tgt==null){
			return false;
		}

		String st = getST( sessionGenerateService, tgt);
		if(st==null){
			return false;
		}

		String sessionId = generateSession(sessionGenerateService,st);
		if(sessionId==null){

			return false;
		}
		return true;
	}


	private String getResponseBody(HttpResponse response){
		StringBuilder sb = new StringBuilder();
		try {
			InputStream instream = response.getEntity().getContent();

			BufferedReader reader =
					new BufferedReader(new InputStreamReader(instream), 65728);
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			instream.close();
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (Exception e) { e.printStackTrace(); }
		return sb.toString();

	}

	public String getTGT(String username, String password)
	{
		String tgt = null;
		HttpPost httpPost = new HttpPost (casBaseURL + CAS_LOGIN_URL_PART);
		try
		{
			List <NameValuePair> nvps = new ArrayList <NameValuePair> ();
			nvps.add(new BasicNameValuePair ("username", username));
			nvps.add(new BasicNameValuePair ("password", password));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			synchronized (httpClient) {
				HttpResponse response = httpClient.execute(httpPost);
				String responseBody = getResponseBody(response);
				switch (response.getStatusLine().getStatusCode())
				{
				case 201:
				{
					Matcher matcher = Pattern.compile(".*action='.*/tickets/(TGT-.*\\.whut\\.org).*")
							.matcher(responseBody.replaceAll("\"", "'"));
					if (matcher.matches()){
						tgt = matcher.group(1);
					}
					break;
				}
				default:
					break;
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return tgt;
	}

	public String getST(String service,String tgt){
		if (tgt == null) return null;
		String st = null;
		HttpPost httpPost = new HttpPost (casBaseURL + CAS_LOGIN_URL_PART+"/" + tgt);
		try
		{
			List <NameValuePair> nvps = new ArrayList <NameValuePair> ();
			nvps.add(new BasicNameValuePair ("service", service));
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));

			synchronized (httpClient) {
				HttpResponse response = httpClient.execute(httpPost);
				String responseBody = getResponseBody(response);

				switch (response.getStatusLine().getStatusCode())
				{
				case 200:
				{
					st = responseBody;
					break;
				}
				default:
					break;
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return st;
	}


	public String generateSession(String service,String st){
		String sessionId = "";
		HttpGet httpGet = new HttpGet (service+"?ticket="+st);
		try
		{
			synchronized (httpClient) {
				HttpResponse response = httpClient.execute(httpGet);
				EntityUtils.toString(response.getEntity() ,"utf-8");
				switch (response.getStatusLine().getStatusCode())
				{
				case 200:
				{
					List<Cookie> cookies = httpClient.getCookieStore().getCookies();
					if (! cookies.isEmpty()){
						for (Cookie cookie : cookies){
							if(cookie.getName().equals("JSESSIONID")){
								sessionId = cookie.getValue();
								break;
							}
						}
					}
					break;
				}
				default:
					break;
				}
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return sessionId;
	}


	synchronized public String doSendFile2(String ServicePath,String FilePath) throws ClientProtocolException, IOException {
		httpClient.getParams().setParameter(  
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);  
		HttpPost httppost = new HttpPost(ServicePath);  
		File file = new File(FilePath);
		CustomMultiPartEntity entity = new CustomMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,null,Charset.forName("UTF-8"),new ProgressListener() {
			
			@Override
			public void transferred(long num) {
				Message msg=Message.obtain();
				msg.what=2;
				msg.arg1=(int)(num/Filelength)*100;
				UploadActivity.handler.sendMessage(msg);
			}
		});  
		
		
		FileBody fileBody = new FileBody(file);  
		entity.addPart("filename", fileBody);  
		Filelength=entity.getContentLength();
		httppost.setEntity(entity);  
		HttpResponse response = httpClient.execute(httppost);
		HttpEntity resEntity = response.getEntity();  
		return EntityUtils.toString(resEntity);
	}
	
synchronized public String doSendFile(String ServicePath,String FilePath) throws ClientProtocolException, IOException {
		
		httpClient.getParams().setParameter(  
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);  
		HttpPost httppost = new HttpPost(ServicePath);  
		File file = new File(FilePath);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,
	               null, Charset.forName("UTF-8")); 
		
		FileBody fileBody = new FileBody(file);  
		entity.addPart("filename", fileBody);  
		httppost.setEntity(entity);  
		HttpResponse response = httpClient.execute(httppost);
		HttpEntity resEntity = response.getEntity();  
		return EntityUtils.toString(resEntity);
	}

	/***
	 * 
	 * @author Yone 
	 * 
	 * 
	 */

	@SuppressLint("SdCardPath")
	synchronized public void doGetUpdateFile(String ServicePath){
		HttpGet httpGet = new HttpGet(ServicePath);
		InputStream inStream=null;
		long downloadedLength=0;
		try
		{
			
		FileUtils.createUpdateDirectory();
		OutputStream outStream = new FileOutputStream(new File("/sdcard/inspect/update/InspectPlatform.apk"));
		synchronized (httpClient) {
				HttpResponse response = httpClient.execute(httpGet);
				switch (response.getStatusLine().getStatusCode())
				{
				case 200:
					long length = response.getEntity().getContentLength();
					inStream =  response.getEntity().getContent();
					byte[] bytes = new byte[1024];  
		            int len = -1;  
		            while((len = inStream.read(bytes))!=-1){  
		                    outStream.write(bytes, 0, len);  
		                    downloadedLength+=len;
							Message msg = Message.obtain();
							msg.arg1=(int)(downloadedLength*100/length);
							msg.what = 9;
							MainActivity.handler.sendMessage(msg); 
		                }					
					break;
				default:
					break;
				}
			}
			
		inStream.close();
		outStream.close();

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}



/***
 * 
 * @param service
 * @param filePath
 * @param params
 * @return
 * @throws ClientProtocolException
 * @throws IOException
 */

	synchronized public String uploadImage(String service,String filePath,HashMap<String,String> params)throws ClientProtocolException, IOException{
		httpClient.getParams().setParameter(  
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1); 
		HttpPost httppost = new HttpPost(service);
		File file = new File(filePath);
		MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE,
	               null, Charset.forName("UTF-8"));
		FileBody imageBody = new FileBody(file);
		entity.addPart("filename",imageBody);
		
		StringBody itemId = new StringBody(params.get("itemId"));
		StringBody tableRecordId = new StringBody(params.get("tableRecordId"));
		StringBody itemRecordId = new StringBody(params.get("itemRecordId"));
		
		entity.addPart("itemId",itemId);
		entity.addPart("tableRecordId",tableRecordId);
		entity.addPart("itemRecordId",itemRecordId);
	
		httppost.setEntity(entity);  
		HttpResponse response = httpClient.execute(httppost);
		HttpEntity resEntity = response.getEntity();  
		return EntityUtils.toString(resEntity);
	}




	public String doGet(String service){
		Log.i("cas client doGet url:", service);
		HttpGet httpGet = new HttpGet (service);
		try{
			synchronized (httpClient) {
				HttpResponse response = httpClient.execute(httpGet);
				String responseBody = getResponseBody(response);
				switch (response.getStatusLine().getStatusCode())
				{
				case 200:
				{
					Log.i("cas client doGet response:", responseBody);

					return responseBody;
				}
				default:
					break;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	synchronized public InputStream DoGetFile(String service)
	{
		HttpGet httpGet = new HttpGet (service);
		try
		{
			synchronized (httpClient) {
				HttpResponse response = httpClient.execute(httpGet);
				switch (response.getStatusLine().getStatusCode())
				{
				case 200:
				{
					Log.i("cas client doGetfile", "success");

					return  response.getEntity().getContent();
				}
				default:
					break;
				}
			}


		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;

	}

	synchronized public String doPost(String service,HashMap<String,Object> params){
		Log.i("cas client doPost url:", service);
		HttpPost httpPost = new HttpPost (service);
		try
		{
			List <NameValuePair> nvps = new ArrayList <NameValuePair> ();
			for(String key:params.keySet()){
				nvps.add(new BasicNameValuePair (key, (String) params.get(key)));
			}
			httpPost.setEntity(new UrlEncodedFormEntity(nvps,HTTP.UTF_8));

			synchronized (httpClient) {
				HttpResponse response = httpClient.execute(httpPost);
				String responseBody = getResponseBody(response);
				switch (response.getStatusLine().getStatusCode())
				{
				case 200:
				{
					Log.i("cas client doPost response:", responseBody);
					return responseBody;
				}
				default:
					break;
				}
			}


		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	synchronized public String doPostNoParams(String service){
		Log.i("cas client doPost url:", service);
		HttpPost httpPost = new HttpPost (service);
		try
		{
			synchronized (httpClient) {
				HttpResponse response = httpClient.execute(httpPost);
				String responseBody = getResponseBody(response);
				switch (response.getStatusLine().getStatusCode())
				{
				case 200:
				{
					Log.i("cas client doPost response:", responseBody);
					return responseBody;
				}
				default:
					break;
				}
			}


		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}





	public boolean logout ()
	{
		boolean logoutSuccess = false;
		HttpDelete httpDelete = new HttpDelete(casBaseURL + CAS_LOGOUT_URL_PART);
		try
		{
			HttpResponse response = httpClient.execute(httpDelete);
			logoutSuccess = (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK);
		}
		catch (Exception e)
		{
			logoutSuccess = false;
		}
		return logoutSuccess;
	}


	public boolean reset(){
		instance = null;
		boolean result = false;
		result = logout();
		return result;
	}

}