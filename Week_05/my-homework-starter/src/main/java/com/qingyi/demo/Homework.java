package com.qingyi.demo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author : Luyz
 * @date : 2020/11/18 23:11
 */
public class Homework {
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContextHomework.xml");
        Car littleBlue = (Car) context.getBean("myTeslan");
        System.out.println(littleBlue);
        Car oldCar = (Car) context.getBean("oldCar");
        System.out.println(oldCar);
        Garage myGarage = (Garage) context.getBean("myGarage");
        System.out.println(myGarage);

    }
}
