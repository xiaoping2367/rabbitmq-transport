package rabbitmq.transport;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.transport.Transport;
import com.esri.ges.transport.TransportServiceBase;
import com.esri.ges.transport.util.XmlTransportDefinition;

public class RabbitMqInboundTransportService extends TransportServiceBase
{
  public RabbitMqInboundTransportService()
  {
    definition = new XmlTransportDefinition(getResourceAsStream("rabbitmq-inbound-transport-definition.xml"));
  }
  
  public Transport createTransport() throws ComponentException
  {
    return new RabbitMqInboundTransport(definition);
  }
}