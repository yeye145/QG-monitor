package com.qg;


import com.pmpsdk.annotation.MethodInvocation;
import com.pmpsdk.annotation.Module;
import com.pmpsdk.annotation.Monitor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Module(type = "模块名")
@Monitor
@MethodInvocation
@RestController
public class TestController {

    @GetMapping("/sdk")
    public String sdk() throws IOException {
        throw new IOException("sdk");
    }


}
