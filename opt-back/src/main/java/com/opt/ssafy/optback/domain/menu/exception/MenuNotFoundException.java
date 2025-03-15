package com.opt.ssafy.optback.domain.menu.exception;

public class MenuNotFoundException extends RuntimeException {
  public MenuNotFoundException(String message) {
    super(message);
  }
}
