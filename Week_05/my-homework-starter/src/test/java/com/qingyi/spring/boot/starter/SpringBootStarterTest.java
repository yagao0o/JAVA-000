package com.qingyi.spring.boot.starter;/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.qingyi.demo.Car;
import com.qingyi.demo.Garage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = com.qingyi.spring.boot.starter.SpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("common")
public class SpringBootStarterTest {

    @Resource
    private Garage garage;
    @Resource
    private Car littleBlue;
    @Resource
    private Car oldCar;
    

    @Test
    public void assertProperties() {
        assertNotNull(littleBlue);
        assertEquals(littleBlue.getNumber(), "鲁AD1***2");
        assertEquals(littleBlue.getModel().getBrand(), "Tesla");
        assertNotNull(oldCar);
        assertEquals(oldCar.getNumber(), "鲁A6***U");
        assertEquals(oldCar.getModel().getModelName(), "C30");
        assertNull(garage.getLocation());
        System.out.println(garage.getMyCars().get(0));
        assertEquals(garage.getMyCars().size(), 2);
    }
}
