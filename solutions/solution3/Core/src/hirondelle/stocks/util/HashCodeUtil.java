package hirondelle.stocks.util;

/**
* Collected methods which allow easy implementation of <tt>hashCode</tt>.
*
* Example use case:
* <pre>
*  public int hashCode(){
*    final int result = HashCodeUtil.SEED;
*    //collect the contributions of various fields
*    result = HashCodeUtil.hash(result, fPrimitive);
*    result = HashCodeUtil.hash(result, fObject);
*    result = HashCodeUtil.hash(result, fArray);
*    return result;
*  }
* </pre>
*/
public final class HashCodeUtil { 

  /**
  * An initial value for a <tt>hashCode</tt>, to which is added contributions
  * from fields. Using a non-zero value decreases collisons of <tt>hashCode</tt>
  * values.
  */
  public static final int SEED = 23;
  
  /** booleans.  */
  public static int hash(int aSeed, boolean aBoolean) {
    return firstTerm( aSeed ) + ( aBoolean ? 1 : 0 );
  }
  
  /** chars.  */
  public static int hash(int aSeed, char aChar) {
    return firstTerm( aSeed ) + (int)aChar;
  }
  
  /**
  * ints.
  *
  * Note that byte and short are handled by this method, through
  * implicit conversion.
  */
  public static int hash(int aSeed , int aInt) {
    return firstTerm( aSeed ) + aInt;
  }
  
  /** longs.  */
  public static int hash(int aSeed , long aLong) {
    return firstTerm(aSeed)  + (int)( aLong ^ (aLong >>> 32) );
  }
  
  /** floats.  */
  public static int hash(int aSeed , float aFloat) {
    return hash( aSeed, Float.floatToIntBits(aFloat) );
  }
  
  /** doubles.  */
  public static int hash(int aSeed , double aDouble) {
    return hash( aSeed, Double.doubleToLongBits(aDouble) );
  }
  
  /** Possibly-null Object fields.  */
  public static int hash(int aSeed , Object aObject) {
    return hash( aSeed, (aObject == null ? 0 : aObject.hashCode()) );
  }
  
  /** Arrays of Objects.  */
  public static int hash(int aSeed , Object[] aArray) {
    int result = aSeed;
    for (int idx = 0; idx < aArray.length; ++idx) {
      result = hash( result, aArray[idx] );
    }
    return result ;
  }
  
  /** Arrays of booleans.  */
  public static int hash(int aSeed, boolean[] aArray){
    int result = aSeed;
    for (int idx = 0; idx < aArray.length; ++idx){
      result = hash( result, aArray[idx] );
    }
    return result;
  }
  
  
  /** Arrays of chars.  */
  public static int hash(int aSeed, char[] aArray){
    int result = aSeed;
    for ( int idx = 0; idx < aArray.length; ++idx ){
      result = hash( result, aArray[idx] );
    }
    return result;
  }
  
  /** Arrays of bytes.  */
  public static int hash(int aSeed, byte[] aArray){
    int result = aSeed;
    for ( int idx = 0; idx < aArray.length; ++idx ){
      result = hash( result, aArray[idx] );
    }
    return result;
  }
  
  /** Arrays of shorts.  */
  public static int hash(int aSeed, short[] aArray){
    int result = aSeed;
    for ( int idx = 0; idx < aArray.length; ++idx ){
      result = hash( result, aArray[idx] );
    }
    return result;
  }
  
  /** Arrays of integers. */
  public static int hash(int aSeed, int[] aArray){
    int result = aSeed;
    for ( int idx = 0; idx < aArray.length; ++idx ){
      result = hash( result, aArray[idx] );
    }
    return result;
  }
  
  /** Arrays of longs  */
  public static int hash(int aSeed, long[] aArray){
    int result = aSeed;
    for ( int idx = 0; idx < aArray.length; ++idx ){
      result = hash( result, aArray[idx] );
    }
    return result;
  }
  
  /** Arrays of floats.  */
  public static int hash(int aSeed, float[] aArray){
    int result = aSeed;
    for ( int idx = 0; idx < aArray.length; ++idx ){
      result = hash( result, aArray[idx] );
    }
    return result;
  }
  
  /** Arrays of doubles.  */
  public static int hash(int aSeed, double[] aArray){
    int result = aSeed;
    for ( int idx = 0; idx < aArray.length; ++idx ){
      result = hash( result, aArray[idx] );
    }
    return result;
  }
  
  // PRIVATE
  private static final int fODD_PRIME_NUMBER = 37;
  
  private static int firstTerm(int aSeed){
    return fODD_PRIME_NUMBER * aSeed;
  }
}
