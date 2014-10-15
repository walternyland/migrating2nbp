package hirondelle.stocks.util;

/**
* Collected methods which allow easy implementation of <tt>equals</tt>.
*
* Example use case in a <tt>Planet</tt> class:
* <pre>
public boolean equals(Object aThat){
  if ( this == aThat ) return true;
 
  if ( !(aThat instanceof Planet) ) return false;
  //you may prefer this style, but see discussion in Effective Java
  //if ( aThat == null || aThat.getClass() != this.getClass() ) return false;
 
  Planet that = (Planet)aThat;
  return 
    EqualsUtil.areEqual(this.fPossiblyNullObject, that.fPossiblyNullObject) &&
    EqualsUtil.areEqual(this.fCollection, that.fCollection) &&
    EqualsUtil.areEqual(this.fPrimitive, that.fPrimitive) &&
    Arrays.equals(this.fArray, that.fArray); //arrays are different!
}
* </pre>
*
* <em>Arrays are not handled by this class</em>. 
* This is because the <tt>Arrays.equals</tt> methods should be used for array fields.
*/
public final class EqualsUtil { 

  static public boolean areEqual(boolean aThis, boolean aThat){
    return aThis == aThat;
  }
  
  static public boolean areEqual(char aThis, char aThat){
    return aThis == aThat;
  }
  
  static public boolean areEqual(long aThis, long aThat){
    /* 
    * Implementation Note
    * Note that byte, short, and int are handled by this method, through
    * implicit conversion.
    */
    return aThis == aThat;
  }

  static public boolean areEqual(float aThis, float aThat){
    return Float.floatToIntBits(aThis) == Float.floatToIntBits(aThat);
  }
  
  static public boolean areEqual(double aThis, double aThat){
    return Double.doubleToLongBits(aThis) == Double.doubleToLongBits(aThat);
  }
  
  /**
  * Possibly-null object field.
  *
  * <P>Includes type-safe enumerations and collections, but does not include arrays.
  * See class comment.
  */
  static public boolean areEqual(Object aThis, Object aThat){
    return aThis == null ? aThat == null : aThis.equals(aThat);
  }
}
