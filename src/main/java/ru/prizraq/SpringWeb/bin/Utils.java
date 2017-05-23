package ru.prizraq.SpringWeb.bin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Вспомогательные методы для серверной части
 * @author evga
 */
public class Utils
{
    public static final String CALLBACK = "callback";
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);
    public static final String UTF_8 = "UTF-8";
    public static final String CONTENT_TEXT_HTML = "text/html";
    public static final String CONTENT_TEXT_CSV = "text/csv";
    public static final String CONTENT_APPLICATION_JSON = "application/json";
    public static final String CONTENT_APPLICATION_JAVASCRIPT = "application/javascript";
    public static final String CONTENT_ZIP = "application/zip";
    
    public static final String SUCCESS = "SUCCESS";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private interface IContentTypeResolver
    {
        String resolveContentType(Map<String, Object> params);
    }

    private static class ConstContentTypeResolver
        implements IContentTypeResolver
    {
        private final String contentType;

        public ConstContentTypeResolver(String t)
        {
            this.contentType = t;
        }

        public String resolveContentType(Map<String, Object> params)
        {
            return contentType;
        }
    }

    private static class ContentTypeResolverByCallbackParam
        implements IContentTypeResolver
    {
        public static final ContentTypeResolverByCallbackParam INSTANCE = new ContentTypeResolverByCallbackParam();
        private ContentTypeResolverByCallbackParam(){};
        public String resolveContentType(Map<String, Object> params)
        {
            String cb = params.get(CALLBACK) != null ?  params.get(CALLBACK).toString() : null;
            return cb == null || cb.equals("") ? CONTENT_APPLICATION_JSON : CONTENT_APPLICATION_JAVASCRIPT;
        }
    }

    /**
     * Возвращает значени куки по ее имени из реквеста 
     * @param request
     * @param cookieName
     * @return
     */
    public static String getParametersCookieValue(HttpServletRequest request, String cookieName)
    {
        Cookie [] cookies = request.getCookies();
        if (cookies != null)
        {
            for (Cookie c : cookies)
            {
                String name = c.getName();
                if (name.equals(cookieName))
                {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    public static Map<String, String> getParams(String requestURI)
        throws UnsupportedEncodingException
    {
        Map<String, String> params = new HashMap<String, String>();
        int ind = requestURI.indexOf("?");
        if (ind > 0)
        {
            String rawQuery = requestURI.substring(ind + 1);
            params = parseRawParam(rawQuery);
        }
        return params;
    }

    public static Map<String, String> parseRawParam(String rawQuery)
        throws UnsupportedEncodingException
    {
        Map<String, String> params = new HashMap<String, String>();
        if (rawQuery != null && !rawQuery.equals(""))
        {
            String [] parts = rawQuery.split("&");
            for (String part : parts)
            {
                part = URLDecoder.decode(part, UTF_8);
                int ind = part.indexOf('=');
                params.put(ind > 0 ? part.substring(0, ind) : part, ind > 0 ? part.substring(ind + 1) : null);
            }
        }
        return params;
    }

    public static String decode(String str)
    {
        if (str == null || str.equals(""))
        {
            return "";
        }
        try
        {
            String tmp = URLDecoder.decode(str, UTF_8);
            if (!tmp.equals(str))
            {
                return decode(tmp);
            }
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return str;
    }

    /**
     * Вспомогательный метод для обработки HTTP запроса
     * @param request запрос
     * @param response ответ
     * @param handler обработчик запроса
     * @param extractor объект для извлечения параметров из запроса
     * @param exceptionHandler обработчик исключения
     */
    public static void handleJSONRequest(HttpServletRequest request, HttpServletResponse response, final IJSONRequestHandler handler, IParamExtractor extractor, final boolean gzip)
    {
        handleRequest(request, response, new IStreamRequestHandler()
        {
            public void handle(final OutputStream out, Map<String, Object> parameters)
                throws Exception
            {
                Object result = handler.handle(parameters);
                if (result != null)
                {
                    String jsonString = JSON.toString(result);
                    if (gzip)
                    {
                        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out);
                        IOUtils.write(jsonString, gzipOutputStream, Utils.UTF_8);
                        gzipOutputStream.flush();
                        gzipOutputStream.finish();
                    }
                    else
                    {
                        IOUtils.write(jsonString, out, Utils.UTF_8);
                    }
                }
            }
        }, extractor, CONTENT_APPLICATION_JSON);
    }

    public static void handleJSONRequestJS(HttpServletRequest request, HttpServletResponse response, final IJSONRequestHandler handler, IParamExtractor extractor)
    {
        handleRequest(request, response, new IStreamRequestHandler()
        {
            public void handle(OutputStream out, Map<String, Object> parameters)
                throws Exception
            {
                Object result = handler.handle(parameters);
                writeJsonResult(result, out, parameters);
            }
        }, extractor, ContentTypeResolverByCallbackParam.INSTANCE);
    }

    public static void writeJsonResult(Object result, OutputStream out, Map<String, Object> requestParams)
        throws IOException
    {
        String callback = requestParams == null ? null : requestParams.get(CALLBACK).toString();
        if (callback != null && !callback.equals(""))
        {
            String jsonData = MAPPER.writeValueAsString(result);
            String wrapped = wrapToCallback(jsonData, callback);
            IOUtils.write(wrapped, out, Utils.UTF_8);
        }
        else
        {
            MAPPER.writeValue(out, result);
        }
    }
    
    public static String wrapToCallback(String jsonData, String callback)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(callback).append("(");
        sb.append(jsonData);
        sb.append(");");
        return sb.toString();
    }

    public static void handleRequest(HttpServletRequest request, HttpServletResponse response, IStreamRequestHandler handler, IParamExtractor extractor, String contentType)
    {
        handleRequest(request, response, handler, extractor, new ConstContentTypeResolver(contentType));
    }

    public static void handleRequest(HttpServletRequest request, HttpServletResponse response, IStreamRequestHandler handler, IParamExtractor extractor, IContentTypeResolver contentTyperesolver)
    {
        Map<String, Object> params = Collections.emptyMap();
        try
        {
            if (request.getCharacterEncoding() == null || request.getCharacterEncoding().equals(""))
            {
                request.setCharacterEncoding(UTF_8);
            }
            params = extractor == null ? null : extractor.extractParam(request);
            response.setCharacterEncoding(UTF_8);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            handler.handle(byteArrayOutputStream, params);
            byte [] responseBytes = byteArrayOutputStream.toByteArray();
            response.setContentType(contentTyperesolver.resolveContentType(params));
            response.setContentLength(responseBytes.length);

            ServletOutputStream outputStream = response.getOutputStream();
            IOUtils.write(responseBytes, outputStream);
        }
        catch (Exception e)
        {
             LOGGER.error("", e);
            
        }
    }

    /**
     * Генерирует ответ для Ajax запроса  
     * @param data
     * @return
     */
    static public Object makeResponse(Object data)
    {
        return makeResponse(data, null);
    }

    static public Object makeResponse(Object data, Object options)
    {
        Map<Object, Object> response = new HashMap<Object, Object>();
        response.put("data", data);
        if (options != null)
        {
            response.put("options", options);
        }
        return response;
    }

    /**
     * Генерирует ответ ошибку для Ajax запроса  
     * @return
     */
    static public Object makeError(Exception e)
    {
        return makeError(e == null ? "null" : e.getMessage());
    }

    /**
     * Генерирует ответ ошибку для Ajax запроса  
     * @return
     */
    static public Object makeError(Map<?, ?> errorsMap)
    {
        Map<Object, Object> responce = new HashMap<Object, Object>();
        responce.put("error", errorsMap);
        return responce;
    }

    static public Object makeError(String errorText)
    {
        return makeError(errorText, null);
    }
    static public Object makeError(String errorText, Object data)
    {
        Map<Object, Object> result = new HashMap<Object, Object>();
        result.put("error", errorText);
        if (data != null)
        {
            result.put("data", data);
        }
        return result;
    }

    static public void setAttachmentName(HttpServletResponse response, String name)
    {
        response.addHeader("Content-Disposition", String.format("attachment; filename=%s", name));
    }

    public static String getCookie(HttpServletRequest request, String cookieName)
    {
        Cookie [] cookies = request.getCookies();
        if (cookies != null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookieName.equals(cookie.getName()))
                {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static String getIP(HttpServletRequest request)
    {
        String addrString = request.getHeader("X-Real-IP");
        if (addrString == null || addrString.equals(""))
        {
            addrString = request.getRemoteAddr();
        }
        return addrString;
    }
}
