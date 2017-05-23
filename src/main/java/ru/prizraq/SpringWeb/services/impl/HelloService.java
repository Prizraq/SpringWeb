package ru.prizraq.SpringWeb.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ru.prizraq.SpringWeb.dao.IHelloDao;
import ru.prizraq.SpringWeb.services.IHelloService;

@Service
public class HelloService
    implements IHelloService
{
    @Autowired
    private IHelloDao helloDao;
    
    public String getHello(String text)
    {
        System.out.println("In service");
        return helloDao.getHello(text);
    }

    public List<Map<String, Object>> getMapForAngular(Map<String, Object> parameters)
    {
        List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("key", 1);
        map.put("value", "value");
        result.add(map);
        return result;
    }

    public Object saveFromAngular(Map<String, Object> parameters)
    {
        System.out.println("Saved " + parameters.get("text"));
        return true;
    }
}
