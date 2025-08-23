package crudjava.crudjava.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(UrlUtils.class);

    public static String encode(String value) {
        if (value == null) {
            return null;
        }
        try {
            String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
            logger.debug("URL encoded '{}' to '{}'", value, encoded);
            return encoded;
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to encode URL parameter: {}", value, e);
            return value;
        }
    }


    public static String decode(String value) {
        if (value == null) {
            return null;
        }
        try {
            String decoded = URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
            logger.debug("URL decoded '{}' to '{}'", value, decoded);
            return decoded;
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to decode URL parameter: {}", value, e);
            return value;
        }
    }

    public static boolean needsDecoding(String value) {
        return value != null && value.contains("%");
    }

    public static String autoDecodeIfNeeded(String value) {
        if (needsDecoding(value)) {
            return decode(value);
        }
        return value;
    }
}
