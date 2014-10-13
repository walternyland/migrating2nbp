package hirondelle.stocks.portfolio;

import java.util.Observable;
import java.util.*;
import hirondelle.stocks.util.Args;
import hirondelle.stocks.quotes.Stock;

/**
 * The central abstraction of this package, representing the current selection of 
 * stocks of interest to the end user 
 * (a {@link hirondelle.stocks.portfolio.Portfolio}).
 * 
 * <P><tt>CurrentPortfolio</tt> may be used as an example implementation for any
 * application which edits one item at a time.
 * 
 * <p>The {@link #isUntitled} and {@link #getNeedsSave} properties are particularly
 * significant. They influence the file menu actions. For example, a
 * <tt>CurrentPortfolio</tt> for which {@link #isUntitled} is true cannot be deleted.
 * 
 * <p><tt>CurrentPortfolio</tt> is an {@link java.util.Observable}. To minimize spurious
 * updates, related {@link java.util.Observer} objects need to call
 * {@link java.util.Observable#notifyObservers()} explicitly. This is important in this
 * application, since quotes are fetched from the web each time the current portfolio is
 * updated, and this is a relatively expensive operation.
 */
public final class CurrentPortfolio extends Observable {

  /**
   * Constructor.
   *  
   * @param aPortfolio is the set of stocks of current interest to the user; no 
   * defensive copy is made of <tt>aPortfolio</tt>.
   * @param aNeedsSave is true only if this <tt>CurrentPortfolio</tt> 
   * has been edited by the end user, and these edits have not yet been saved.
   */
  public CurrentPortfolio(Portfolio aPortfolio, NeedsSave aNeedsSave) {
    Args.checkForNull(aPortfolio);
    fPortfolio = aPortfolio;
    fNeedsSave = aNeedsSave.getValue();
    // upon construction of the main window, an update is desired in order to
    // synch the gui with the current portfolio. This update is called explicitly.
    // Thus, setChanged needs to be set here, since it's default value is false.
    setChanged();
  }

  /**
  * Enumeration for the two states of <tt>aNeedsSave</tt> passed to the constructor.
  * Use of an enumeration forces the caller to create a constructor call which has
  * high clarity.
  */
  public enum NeedsSave {
    TRUE(true),
    FALSE(false);
    boolean getValue() {
      return fToggle;
    }
    private final boolean fToggle;
    private NeedsSave(boolean aToggle) {
      fToggle = aToggle;
    }
  }

  /**
   * Revert to an untitled <tt>Portfolio</tt> which does not need a save.
   */
  public void clear() {
    setPortfolio(Portfolio.getUntitledPortfolio());
    setNeedsSave(false);
  }

  /**
   * Return <tt>true</tt> only if the current <tt>Portfolio</tt> has never been
   * saved under a user-specified name, neither in this session, nor in any other. Such a
   * <tt>Portfolio</tt> appears as untitled in the display.
   */
  public boolean isUntitled() {
    return fPortfolio.isUntitled();
  }

  /**
   * Return the {@link Portfolio} of current interest to the user.
   */
  public Portfolio getPortfolio() {
    return fPortfolio;
  }

  /**
   * Change the {@link Portfolio} of current interest to the user.
   */
  public void setPortfolio(Portfolio aPortfolio) {
    Args.checkForNull(aPortfolio);
    fPortfolio = aPortfolio;
    setChanged();
  }

  /**
   * Return the name of this <tt>CurrentPortfolio</tt>.
   */
  public String getName() {
    return fPortfolio.getName();
  }

  /**
   * Change the name of this <tt>CurrentPortfolio</tt>.
   * @param aName has the same conditions as 
   * {@link hirondelle.stocks.portfolio.Portfolio#setName(String)}.
   */
  public void setName(String aName) {
    fPortfolio.setName(aName);
    setChanged();
  }

  /**
   * Return the {@link hirondelle.stocks.quotes.Stock} objects in this
   * <tt>CurrentPortfolio</tt>.
   */
  public Set<Stock> getStocks() {
    return fPortfolio.getStocks();
  }

  /**
   * Change the stocks in this <tt>CurrentPortfolio</tt>.
   * @param aStocks has the same conditions as
   * {@link hirondelle.stocks.portfolio.Portfolio#setStocks(Set)}
   */
  public void setStocks(Set<Stock> aStocks) {
    fPortfolio.setStocks(aStocks);
    setChanged();
  }

  /**
   * Return <tt>true</tt> only if this <tt>CurrentPortfolio</tt> has unsaved
   * edits.
   */
  public boolean getNeedsSave() {
    return fNeedsSave;
  }

  /**
   * Indicate that this <tt>CurrentPortfolio</tt> either does or does not have any
   * unsaved edits.
   */
  public void setNeedsSave(boolean aNeedsSave) {
    fNeedsSave = aNeedsSave;
    setChanged();
  }

  // PRIVATE 
  private Portfolio fPortfolio;
  private boolean fNeedsSave;
}
