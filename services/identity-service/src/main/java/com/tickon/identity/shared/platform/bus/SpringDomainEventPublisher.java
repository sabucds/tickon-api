package com.tickon.identity.shared.platform.bus;

import com.tickon.common.domain.DomainEvent;
import com.tickon.identity.shared.kernel.ports.out.DomainEventPublisher;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

  private final ApplicationEventPublisher publisher;

  public SpringDomainEventPublisher(ApplicationEventPublisher publisher) {
    this.publisher = publisher;
  }

  @Override
  public void publish(DomainEvent event) {
    publisher.publishEvent(event);
  }

  @Override
  public void publishAll(List<DomainEvent> events) {
    events.forEach(this::publish);
  }
}
