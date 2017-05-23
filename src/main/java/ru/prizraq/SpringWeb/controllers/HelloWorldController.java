package ru.prizraq.SpringWeb.controllers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ru.prizraq.SpringWeb.bin.IJSONRequestHandler;
import ru.prizraq.SpringWeb.bin.JsonParamExtractor;
import ru.prizraq.SpringWeb.bin.Utils;
import ru.prizraq.SpringWeb.services.IHelloService;


@Controller
public class HelloWorldController
{
    String message = "Welcome to Spring MVC!";
    
    @Autowired
    private IHelloService helloService;
    
    @RequestMapping(value="/hello", method = RequestMethod.GET)
    public String showMessage(
            @RequestParam(value = "name", required = false, defaultValue = "World") String name,
            Model model) {
        System.out.println("in controller");
 
        /*ModelAndView mv = new ModelAndView("helloworld");
        mv.addObject("message", helloService.getHello(message));
        mv.addObject("name", name);
        return mv;*/
        model.addAttribute("message", helloService.getHello(message));
        model.addAttribute("name", name);
        return "helloworld";
    }
    
    // http://localhost:8080/SpringWeb/angular
    @RequestMapping(value = "/angular", method = RequestMethod.GET)
    public String angularPage()
    {
        return "angularPage";
    }
    
    @RequestMapping(value = "/angular/get", method = RequestMethod.POST)
    public void getForAngularPage(HttpServletRequest request, HttpServletResponse response)
    {
        Utils.handleJSONRequest(request, response, new IJSONRequestHandler()
        {
            
            public Object handle(Map<String, Object> parameters)
                throws Exception
            {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("list", helloService.getMapForAngular(parameters));
                return Utils.makeResponse(data);
            }
        }, JsonParamExtractor.getInstance(), false);
    }
    
    @RequestMapping(value = "/angular/save", method = RequestMethod.POST)
    public void saveForAngularPage(HttpServletRequest request, HttpServletResponse response)
    {
        Utils.handleJSONRequest(request, response, new IJSONRequestHandler()
        {
            
            public Object handle(Map<String, Object> parameters)
                throws Exception
            {
                return Utils.makeResponse(helloService.saveFromAngular(parameters));
            }
        }, JsonParamExtractor.getInstance(), false);
    }
}
