package rabbitmq.transport;

import java.nio.ByteBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.component.RunningException;
import com.esri.ges.core.component.RunningState;
import com.esri.ges.transport.InboundTransportBase;
import com.esri.ges.transport.TransportDefinition;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;


public class RabbitMqInboundTransport extends InboundTransportBase implements Runnable
{
	private static final Log log = LogFactory.getLog(RabbitMqInboundTransport.class);

	private String hostname;
	private String userName;
	private String password;
	private String exchange;
	private String exchangeType;
	private String queueName;
	private String routingKey;
	
	private ConnectionFactory connFactory;
	private Connection conn;
	private Channel model;
	
	private Thread thread = null;
	private String channelId = "1";

	public RabbitMqInboundTransport(TransportDefinition definition) throws ComponentException
	{
		super(definition);
	}

	public void applyProperties() throws Exception
	{
		if (getProperty("hostname").isValid())
		{
			String value = (String) getProperty("hostname").getValue();
			if(value != hostname )
			{
				hostname = value;
			}
		}
		if (getProperty("userName").isValid())
		{
			String value = (String) getProperty("userName").getValue();
			if(value != userName )
			{
				userName = value;
			}
		}
		if (getProperty("password").isValid())
		{
			String value = (String) getProperty("password").getValue();
			if(value != password )
			{
				password = value;
			}
		}
		if (getProperty("exchange").isValid())
		{
			String value = (String) getProperty("exchange").getValue();
			if(value != exchange )
			{
				exchange = value;
			}
		}
		if (getProperty("exchangeType").isValid())
		{
			String value = (String) getProperty("exchangeType").getValue();
			if(value != exchangeType )
			{
				exchangeType = value;
			}
		}
		if (getProperty("queueName").isValid())
		{
			String value = (String) getProperty("queueName").getValue();
			if(value != queueName )
			{
				queueName = value;
			}
		}
		if (getProperty("routingKey").isValid())
		{
			String value = (String) getProperty("routingKey").getValue();
			if(value != routingKey )
			{
				routingKey = value;
			}
		}
		
	}

	public void run()
	{
		try
		{
			applyProperties();
			setRunningState(RunningState.STARTED);
			
			connFactory = new ConnectionFactory();
			connFactory.setHost(hostname);
			connFactory.setUsername(userName);
			connFactory.setPassword(password);
			
			conn = connFactory.newConnection();
			model = conn.createChannel();
			
			
			exchange = exchange+"."+exchangeType;
			model.exchangeDeclare(exchange, exchangeType);
			
			if(queueName !="")
			{
				model.queueDeclare(queueName, false, false, false, null);
			}
			else
			{
				queueName = model.queueDeclare().getQueue();
			}
			model.queueBind(queueName, exchange, routingKey);
			
			QueueingConsumer consumer = new QueueingConsumer(model);
	        model.basicConsume(queueName, true, consumer); 
						
			while( getRunningState() == RunningState.STARTED )
			{
				try
				{
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		   			byteListener.receive(ByteBuffer.wrap(delivery.getBody()), channelId);
				}
				catch (Exception e)
				{
					log.error("Unexpected error, stopping the Transport.", e);
					cleanup();
					stop();
				}
			}
		}
		catch (Throwable ex)
		{
			log.error(ex);
			setRunningState(RunningState.ERROR);
		}
	}

	@SuppressWarnings("incomplete-switch")
  public void start() throws RunningException
	{
    switch (getRunningState())
		{
		case STARTING:
		case STARTED:
		case STOPPING:
			try
			{
				cleanup();
			} 
			catch (Exception e)
			{
				log.error("Possible Hung RabbitMQ Listening Instance. "+ e );
			}
			return;
		}
		setRunningState(RunningState.STARTING);
		thread = new Thread(this);
		thread.start();
	}
	
	private void cleanup() throws Exception
	{
			if(model.isOpen())
				model.close();
			if(conn.isOpen())
				conn.close();
	}
}