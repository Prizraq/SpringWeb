package ru.prizraq.SpringWeb.bin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;


/**
 * Класс для извлечения параметров из реквест патока, в виде JSON
 * @author evga
 */
public class JsonParamExtractor 
    implements IParamExtractor
{
    
    private static final JsonParamExtractor INSTANCE = new JsonParamExtractor(false);
    private static final JsonParamExtractor INSTANCE_GZIP = new JsonParamExtractor(true);
    private boolean gzip;
    public static JsonParamExtractor getInstance()
    {
        return INSTANCE;
    }
    public static JsonParamExtractor getGzipInstance()
    {
        return INSTANCE_GZIP;
    }
    
    private JsonParamExtractor(boolean gzip)
    {
        this.gzip = gzip;
    }
    
    public Map<String, Object> extractParam(HttpServletRequest request)
    {
        try
        {   
            InputStream is = gzip ? new GZIPInputStream(request.getInputStream()) : request.getInputStream();
            String strData = streamToString(is);
            return (Map<String, Object>)JSON.parse(strData);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        
    }
    
    static public String streamToString(InputStream is) throws IOException
    {
        final char [] buffer = new char[1024 * 256];
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(is, "UTF-8");
        try
        {
            int read;
            do
            {
                read = in.read(buffer, 0, buffer.length);
                if (read > 0)
                {
                    out.append(buffer, 0, read);
                }
            }
            while (read >= 0);
        }
        finally
        {
            in.close();
        }
        return out.toString();

    }

    public Map<String, Object> extractParamFromJsonString(String jsonString)
        throws Exception
    {
        if (isBlank(jsonString))
        {
            return Collections.emptyMap();
        }
        else
        {
            return (Map<String, Object>)JSON.parse(jsonString);
        }
    }
    
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

}
