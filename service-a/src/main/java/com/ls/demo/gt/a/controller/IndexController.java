package com.ls.demo.gt.a.controller;

import com.ls.demo.gt.a.service.DemoService;
import com.ls.demo.gt.common.response.Code;
import com.ls.demo.gt.common.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Autowired
    private DemoService demoService;

    @GetMapping(value = {"/success1"})
    public Response<String> success1() {
        demoService.insert();
        return Response.of(Code.SUCCESS);
    }

    @GetMapping(value = {"/success2"})
    public Response<String> success2() {
        demoService.insert2();
        return Response.of(Code.SUCCESS);
    }

    @GetMapping(value = {"/fail1"})
    public Response<String> fail1() {
        demoService.insert3();
        return Response.of(Code.SUCCESS);
    }

    @GetMapping(value = {"/fail2"})
    public Response<String> fail2() {
        demoService.insert4();
        return Response.of(Code.SUCCESS);
    }

}
