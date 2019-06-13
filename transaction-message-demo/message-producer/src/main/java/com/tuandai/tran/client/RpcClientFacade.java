package com.tuandai.tran.client;

import java.nio.charset.Charset;
import java.util.Properties;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClientFacade {
	private static final Logger logger = LoggerFactory.getLogger(RpcClientFacade.class);

	private static RpcClient client;
	private static Properties props = new Properties();

	public static void init(String ip) {
		if(!Isipv4(ip)){
			logger.error("{} is invalid ip address!",ip);
			return;
		}
		
		// Setup properties for the failover
		props.put("client.type", "default_failover");
		// List of hosts (space-separated list of user-chosen host aliases)
		props.put("hosts", "h1 h2");

		// host/port pair for each host alias
		String host1 = ip + ":41414";
		String host2 = ip + ":41415";
		props.put("hosts.h1", host1);
		props.put("hosts.h2", host2);

		// create the client with failover properties
		client = RpcClientFactory.getInstance(props);
	}

	public static void sendDataToFlume(String data) {
		long startTime = System.currentTimeMillis();
		logger.error("sendDataToFlume : " + data);
		// Create a Flume Event object that encapsulates the sample data
		Event event = EventBuilder.withBody(data, Charset.forName("UTF-8"));

		// Send the event
		try {
			client.append(event);
			logger.debug("client.append(event) : " + data);
		} catch (EventDeliveryException e) {
			// clean up and recreate the client
			client.close();
			client = null;
			client = RpcClientFactory.getInstance(props);
		}
		long time = System.currentTimeMillis() - startTime;
		logger.error("=============> 发送消息执行的时间：[" + time + "ms]");
	}

	public static void cleanUp() {
		// Close the RPC connection
		client.close();
	}
	
	public static boolean Isipv4(String ipv4){  
	    if(ipv4==null || ipv4.length()==0){  
	        return false;//字符串为空或者空串  
	    }  
	    String[] parts=ipv4.split("\\.");  
	    if(parts.length!=4){  
	        return false;//分割开的数组根本就不是4个数字  
	    }  
	    for(int i=0;i<parts.length;i++){  
	        try{  
	        int n=Integer.parseInt(parts[i]);  
	        if(n<0 || n>255){  
	            return false;//数字不在正确范围内  
	        }  
	        }catch (NumberFormatException e) {  
	            return false;//转换数字不正确  
	        }  
	    }  
	    return true;  
	}  
}