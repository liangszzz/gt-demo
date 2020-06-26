package com.ls.demo.gt.a.controller;

import com.ls.demo.gt.a.service.DemoService;
import com.ls.demo.gt.common.response.Code;
import com.ls.demo.gt.common.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

  @Autowired private DemoService demoService;

  @GetMapping(value = {"/", "index"})
  public Response<String> index() {
    demoService.insert();
    return Response.of(Code.SUCCESS);
  }

  @GetMapping(value = {"/rollback"})
  public Response<String> index2() {
    demoService.insertFail();
    return Response.of(Code.SUCCESS);
  }
}
