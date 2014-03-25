package rabbitmq.transport;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.transport.Transport;
import com.esri.ges.transport.TransportServiceBase;
import com.esri.ges.transport.util.XmlTransportDefinition;

public class RabbitMqOutboundTransportService extends TransportServiceBase
{
  public RabbitMqOutboundTransportService()
  {
    definition = new XmlTransportDefinition(getResourceAsStream("rabbitmq-outbound-transport-definition.xml"));
  }

  public Transport createTransport() throws ComponentException
  {
    return new RabbitMqOutboundTransport(definition);
  }
}