/*
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

package com.qingyi;

import com.qingyi.demo.Car;
import com.qingyi.demo.Garage;
import com.qingyi.demo.Model;
import com.qingyi.prop.SpringBootPropertiesConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

/**
 * Spring boot starter configuration.
 * @author Luyz
 */
@Configuration
@EnableConfigurationProperties(SpringBootPropertiesConfiguration.class)
@ConditionalOnProperty(prefix = "com.qingyi", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class SpringBootConfiguration {

    private final SpringBootPropertiesConfiguration props;
    @Bean("model3")
    public Model getModel3() {
        Model model = new Model();
        model.setBrand("Tesla");
        model.setModelName("model 3");
        model.setPowerType(1);
        return model;
    }

    @Bean("c30")
    public Model getC30() {
        Model model = new Model();
        model.setBrand("长城");
        model.setModelName("C30");
        model.setPowerType(0);
        return model;
    }

    @Bean("littleBlue")
    @Autowired()
    public Car getLittleBlue(Model model3){
        Car car = new Car();
        car.setModel(model3);
        car.setNumber("鲁AD1***2");
        return car;
    }

    @Bean("oldCar")
    @Autowired()
    public Car getOldCar(Model c30){
        Car car = new Car();
        car.setModel(c30);
        car.setNumber("鲁A6***U");
        return car;
    }


    @Bean("garage")
    @Autowired
    public Garage getMyGarage(Car littleBlue, Car oldCar) {
        Garage garage = new Garage();
        System.out.println(props.getProps().keySet());
        garage.setMyCars(new ArrayList<>());
        garage.getMyCars().add(littleBlue);
        garage.getMyCars().add(oldCar);
        return garage;
    }
}
