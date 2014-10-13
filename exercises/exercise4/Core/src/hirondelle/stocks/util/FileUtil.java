package hirondelle.stocks.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/** 
 Collected methods to ease manipulation of text files.
 
 <P>Assumes UTF-8 encoding of the files.
*/
public final class FileUtil {

  /**
    Return a whole text file as a single String. 
   @param aName name of a file, relative to the location of the given class. 
   Usually, the file is in the same directory as the class that is using it; in that case, 
   the file name is just the simple file name.
   */
  public static String asString(String aName, Class<?> aClass){
    StringBuilder result = new StringBuilder("");
    List<String> lines = asLines(aName, aClass);
    for (String line : lines){
      result.append(line + Consts.NEW_LINE);
    }
    return result.toString();
  }
  
  /**
   Return a whole text file as an unmodifiable list of lines. 
  @param aName name of a file, relative to the location of the given class. 
   Usually, the file is in the same directory as the class that is using it; in that case, 
   the file name is just the simple file name.
  */
  public static List<String> asLines(String aName, Class<?> aClass){
    List<String> result = new ArrayList<>();
    //can't use Path with URI here, since it doesn't work in Web Start
    try (
      //uses the class loader search mechanism:
      InputStream input = aClass.getResourceAsStream(aName);
      InputStreamReader isr = new InputStreamReader(input, ENCODING);
      BufferedReader reader = new BufferedReader(isr);
    ){
      String line = null;
      while ((line = reader.readLine()) != null) {
        result.add(line);
      }      
    }
    catch (IOException ex){
      ex.printStackTrace();
    }
    return Collections.unmodifiableList(result);
  }
  
  // PRIVATE
  private final static Charset ENCODING = StandardCharsets.UTF_8;

}
