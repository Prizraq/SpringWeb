package ru.prizraq.SpringWeb.bin;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * Интерфейс для класса который может извлеч параметры из HTTP запроса, и предоставить их в виде мапки
 * @author evga
 *
 */
public interface IParamExtractor
{
    /**
     * Извлекает параметры из реквеста
     * @param request
     * @return
     * @throws Exception
     */
    Map<String, Object> extractParam(HttpServletRequest request);
}
