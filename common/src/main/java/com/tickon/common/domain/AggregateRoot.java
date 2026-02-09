package com.tickon.common.domain;

import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRoot {

  private final List<DomainEvent> domainEvents = new ArrayList<>();

  protected void registerEvent(DomainEvent event) {
    domainEvents.add(event);
  }

  public List<DomainEvent> domainEvents() {
    return new ArrayList<>(domainEvents);
  }

  public void clearEvents() {
    domainEvents.clear();
  }
}
