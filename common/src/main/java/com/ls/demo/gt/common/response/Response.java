package com.ls.demo.gt.common.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Response<T> {

  private String code;

  private String msg;

  private T data;

  public static Response<String> of(Code code) {
    return Response.<String>builder().code(code).build();
  }

  public static Response<Object> SUCCESS(Object data) {
    return Response.builder().code(Code.SUCCESS).data(data).build();
  }

  public static Response<Object> ERROR(Object data) {
    return Response.builder().code(Code.ERROR).data(data).build();
  }

  @Builder
  public Response(Code code, T data) {
    this.code = code.getCode();
    this.msg = code.getMsg();
    this.data = data;
  }
}
