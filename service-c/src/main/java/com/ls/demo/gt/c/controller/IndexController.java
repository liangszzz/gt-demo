package com.ls.demo.gt.c.controller;

import com.ls.demo.gt.c.service.DemoService;
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

  @GetMapping(value = {"/fail2"})
  public Response<String> fail2() {
    demoService.insert4();
    return Response.of(Code.SUCCESS);
  }
}
