package org.shanghai.util;

import org.shanghai.util.FileUtil;

import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;
import java.text.Normalizer;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;

/**
 * @author hatop@bigfoot.com
 */
public class TextUtil {
    private static Logger logger = Logger.getLogger(TextUtil.class.getName());

    private static final String WORD_SEPARATOR = "\\W+";

    private static final Pattern re = Pattern.compile(
            "# Match a sentence ending in punctuation or EOS.\n" +
            "[^.!?\\s]    # First char is non-punct, non-ws\n" +
            "[^.!?]*      # Greedily consume up to punctuation.\n" +
            "(?:          # Group for unrolling the loop.\n" +
            "  [.!?]      # (special) inner punctuation ok if\n" +
            "  (?!['\"]?\\s|$)  # not followed by ws or EOS.\n" +
            "  [^.!?]*    # Greedily consume up to punctuation.\n" +
            ")*           # Zero or more (special normal*)\n" +
            "[.!?]?       # Optional ending punctuation.\n" +
            "['\"]?       # Optional closing quote.\n" +
            "(?=\\s|$)", 
            Pattern.MULTILINE | Pattern.COMMENTS);

    public static String clean(String raw) {
        try {
            CharsetDecoder utf8Decoder = Charset.forName("UTF-8").newDecoder();
            //utf8Decoder.onMalformedInput(CodingErrorAction.IGNORE);
            //utf8Decoder.onUnmappableCharacter(CodingErrorAction.IGNORE);
            utf8Decoder.onMalformedInput(CodingErrorAction.REPLACE);
            utf8Decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            ByteBuffer bytes = ByteBuffer.wrap(raw.getBytes("UTF-8"));
            CharBuffer parsed = utf8Decoder.decode(bytes);
            String result = parsed.toString().replaceAll("\\p{C}", " ");
            result = Normalizer.normalize(result, Normalizer.Form.NFC); 
            return result;
        } catch( UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unknown");
        } catch( CharacterCodingException e) {
            logger.info("Dirty UTF8 " + e.toString());
        }
        return "";
    }

    /** Formats text as one sentence per line. Much too simple. */
    public static String sentence(String text) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = re.matcher(text);
        while(matcher.find()) {
            String block = matcher.group();
            block = block(block);
            block = block.replaceAll("\\s+", " ").trim();
            //sb.append("#");
            sb.append(block);
            sb.append("\n");
        }
        return sb.toString();
    }

    /** provide very basic block formatting */
    private static String block(String text) {
        int count = 0;
        StringBuilder sb = new StringBuilder();
        String words[] = text.split("\\s");
        for (String word : words) {
            sb.append(word);
            sb.append(' ');
            if (word.length()==0) {
                sb.append("\n\t");
            }
        }
        return sb.toString();
    }

    /** create an local identifier */
    public static String createURI(String title) {
        String uri = title.replaceAll("[^a-zA-Z0-9]","").toLowerCase();
        int x = uri.length()>254?254:uri.length();
        return "http://localhost/ref/" + uri.substring(0,x);
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        if (s1==null || s2==null) {
            return 0.0;
        }
        return JaroWinkler.similarity(s1, s2);
    }

}
