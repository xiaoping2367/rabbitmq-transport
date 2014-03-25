package rabbitmq.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.component.RunningException;
import com.esri.ges.core.component.RunningState;
import com.esri.ges.transport.OutboundTransportBase;
import com.esri.ges.transport.TransportDefinition;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqOutboundTransport extends OutboundTransportBase
{
  static final private Log log = LogFactory.getLog(RabbitMqOutboundTransport.class);

  private String hostname;
  private String userName;
  private String password;
  private String queueName;

private String exchange;
  private String exchangeType;
  private String routingKey;
  
  private ConnectionFactory connFactory;
  private Connection conn;
  private Channel model;
    
  public RabbitMqOutboundTransport(TransportDefinition definition) throws ComponentException
  {
    super(definition);
  }

  @SuppressWarnings("unused")
  private void applyProperties() throws IOException
  {
    hostname = getProperty("hostname").getValueAsString();
    userName = getProperty("userName").getValueAsString();
    password = getProperty("password").getValueAsString();
    exchange = getProperty("exchange").getValueAsString();
    exchangeType = getProperty("exchangeType").getValueAsString();
    queueName = getProperty("queueName").getValueAsString();
    routingKey = getProperty("routingKey").getValueAsString();
  }

  @SuppressWarnings("unused")
  public void receive(ByteBuffer buffer, String channelId)
  {
	byte[] b = new byte[buffer.remaining()];
    try
    {
		model.basicPublish(exchange, routingKey, null, b);
	} catch (IOException e)
	{
		log.error("Cannot Publish to RabbitMQ: " + e);
	}
  }
  
 
  public void start() throws RunningException
  {
    try
    {
      setRunningState(RunningState.STARTING);
      applyProperties();
      connFactory = new ConnectionFactory();
      connFactory.setHost(hostname);
      connFactory.setUsername(userName);
      connFactory.setPassword(password);
      conn = connFactory.newConnection();
      model = conn.createChannel();
      
      //QueueDeclare and ExchangeDeclare are Idempotent
      
      exchange = exchange+"."+exchangeType;
      model.exchangeDeclare(exchange, exchangeType);
      
      if(queueName!="")
      {
    	  model.queueDeclare(queueName, false, false, false,null);
      }
      else
      {
    	queueName = model.queueDeclare().getQueue();  
      }
      
      model.queueBind(queueName, exchange, routingKey);
            
      setRunningState(RunningState.STARTED);
    }
    catch (IOException e)
    {
      log.error("Unable to initialize the " + this.getClass().getName() + " transport", e);
      setRunningState(RunningState.ERROR);
      
      try
      {
    	    if(model.isOpen())
    	    	  model.close();
    	      if(conn.isOpen())
    	    	  conn.close();
      }
      catch(Exception rabbErr)
      {
    	 log.error("Possible Hung RabbitMQ Connection: "+rabbErr);
      }
    }
  }
}