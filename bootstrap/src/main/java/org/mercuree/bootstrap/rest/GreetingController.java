/*
 * Copyright (c) 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mercuree.bootstrap.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.spring.annotation.Selector;

/**
 * TODO: javadoc
 * <p/>
 *
 * @author Alexander Valyugin
 */
@Controller
public class GreetingController {

    @Autowired
    private Reactor reactor;

    @RequestMapping("/greeting")
    @ResponseBody
    public String greeting(@RequestParam(value = "name", required = false, defaultValue = "World") String name) {
        String response =  Math.random() < 0.5 ? "ok" : "error";
        reactor.notify("test.topic", Event.wrap("/greeting/" + response));
        return response;
    }

    @Selector(value = "test.topic", reactor = "@rootReactor")
    public void handleTestTopic(Event<String> event) {
        System.out.println(event);
    }

}
