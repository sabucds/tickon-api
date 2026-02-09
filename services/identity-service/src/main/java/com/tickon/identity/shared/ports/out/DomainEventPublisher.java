package com.tickon.identity.shared.ports.out;

import com.tickon.common.domain.DomainEvent;
import java.util.List;

public interface DomainEventPublisher {

  void publish(DomainEvent event);

  void publishAll(List<DomainEvent> events);
}
