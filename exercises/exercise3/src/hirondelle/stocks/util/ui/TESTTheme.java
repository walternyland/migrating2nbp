package hirondelle.stocks.util.ui;

import javax.swing.plaf.metal.MetalTheme;

import junit.framework.TestCase;

/** JUnit test cases for {@link Theme}. */
public final class TESTTheme extends TestCase {

  /** Run the test cases.  */
   public static void main(String... aArgs) {
     String[] testCaseName = {TESTTheme.class.getName()};
     junit.textui.TestRunner.main(testCaseName);
  }

  public TESTTheme(String aName) {
   super( aName );
  }

  // TEST CASES //

  public void testValuesSize (){
    assertTrue(Theme.VALUES.size() > 2);
  }
  
  public void testAquaName(){
    assertTrue( Theme.AQUA.getName().equals("Aqua") );
  }

  public void testDefaultName(){
    assertTrue( Theme.DEFAULT.getName().equals("Default") );
  }
  
  public void testParseValidAqua(){
    String name = "Aqua";
    MetalTheme theme = Theme.valueOf(name);
    assertTrue( theme == Theme.AQUA );
  }
  
  public void testParseInvalidAqua() {
    String name = "AQUA"; //upper-case
    try {
      MetalTheme theme = Theme.valueOf( name );
    }
    catch ( IllegalArgumentException ex ) {
      return;
    }
    fail("Parse of invalid theme name did not throw exception.");
  }
}
