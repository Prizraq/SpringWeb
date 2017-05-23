package ru.prizraq.SpringWeb.services;

import java.util.List;
import java.util.Map;

public interface IHelloService
{
    String getHello(String text);

    List<Map<String, Object>> getMapForAngular(Map<String, Object> parameters);

    Object saveFromAngular(Map<String, Object> parameters);
}
