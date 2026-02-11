package com.tickon.identity.auth.infrastructure.email;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "email.sendgrid")
public class EmailProperties {
  private String apiKey;
  private String fromEmail;
  private String fromName;
  private String resetUrlBase;

  public String getApiKey() {
    return apiKey;
  }

  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
  }

  public String getFromEmail() {
    return fromEmail;
  }

  public void setFromEmail(String fromEmail) {
    this.fromEmail = fromEmail;
  }

  public String getFromName() {
    return fromName;
  }

  public void setFromName(String fromName) {
    this.fromName = fromName;
  }

  public String getResetUrlBase() {
    return resetUrlBase;
  }

  public void setResetUrlBase(String resetUrlBase) {
    this.resetUrlBase = resetUrlBase;
  }
}
