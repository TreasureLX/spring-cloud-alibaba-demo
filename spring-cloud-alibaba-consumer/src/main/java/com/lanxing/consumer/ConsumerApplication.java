/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lanxing.consumer;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import feign.hystrix.FallbackFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }

    @FeignClient(name = "provider", fallbackFactory = MyFallbackFactory.class)
    interface EchoService {
        @GetMapping("/divide")
        Integer divide(@RequestParam("a") Integer a, @RequestParam("b") Integer b);

    }

    @Component
    class MyFallbackFactory implements FallbackFactory {

        private EchoService sentinelEchoService = new SentinelEchoServiceFallback();

        private EchoService defaultEchoService = new DefaultEchoServiceFallback();

        @Override
        public Object create(Throwable cause) {
            if (cause instanceof BlockException) {
                return sentinelEchoService;
            }
            return defaultEchoService;
        }
    }

    class SentinelEchoServiceFallback implements EchoService {

        @Override
        public Integer divide(Integer a, Integer b) {
            return -1000;
        }

    }

    class DefaultEchoServiceFallback implements EchoService {

        @Override
        public Integer divide(Integer a, Integer b) {
            return -2000;
        }

    }

    @RestController
    class ConsumerController {
        @Autowired
        private EchoService echoService;

        @GetMapping("/divide")
        public Integer divide(Integer a, Integer b) {
            return echoService.divide(a, b);
        }
    }

}
