package com.tickon.identity.shared.kernel.ports;

public interface PasswordHasher {

  String hash(String rawPassword);

  boolean verify(String rawPassword, String hash);
}
