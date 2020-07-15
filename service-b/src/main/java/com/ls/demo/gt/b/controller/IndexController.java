package com.ls.demo.gt.b.controller;

import com.ls.demo.gt.b.service.DemoService;
import com.ls.demo.gt.common.response.Code;
import com.ls.demo.gt.common.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    @Autowired
    private DemoService demoService;

    @GetMapping(value = {"/", "index"})
    public Response<String> index() {
        demoService.insert();
        return Response.of(Code.SUCCESS);
    }

    @GetMapping(value = "/success2")
    public Response<String> success2() {
        demoService.insert2();
        return Response.of(Code.SUCCESS);
    }
}
