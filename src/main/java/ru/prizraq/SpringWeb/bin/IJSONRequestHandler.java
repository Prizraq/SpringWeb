package ru.prizraq.SpringWeb.bin;

import java.util.Map;

/**
 * Обработчик HTTP запроса
 * @author evga
 *
 */
public interface IJSONRequestHandler
{
    /**
     * @param parameters параметры запроса
     * @return
     */
    Object handle(Map<String, Object> parameters) throws Exception;
}