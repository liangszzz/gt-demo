package com.ls.demo.gt.common.response;

import lombok.Getter;

public enum Code {
  SUCCESS("SUCCESS", "成功"),

  ERROR("ERROR", "系统异常");

  @Getter private final String code;

  @Getter private final String msg;

  Code(String code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  @Override
  public String toString() {
    return code + "";
  }
}
