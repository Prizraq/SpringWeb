package ru.prizraq.SpringWeb.bin;

import java.io.OutputStream;
import java.util.Map;

/**
 * Обработчик HTTP зароса
 * @author evga
 *
 */
public interface IStreamRequestHandler
{
    /**
     * @param parameters параметры запроса
     * @return
     */
    void handle(OutputStream out, Map<String, Object> parameters) throws Exception;
}