package com.qingyi.demo;

import lombok.Data;

import java.util.List;

/**
 * @author : Luyz
 * @date : 2020/11/18 23:33
 */
@Data
public class Garage {
    List<Car> myCars;
    String location;
}
