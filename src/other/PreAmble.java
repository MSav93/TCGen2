package other;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class PreAmble implements Iterable<TestCase> {

  private ArrayList<TestCase> preAmble = new ArrayList<TestCase>();

  @Override
  public Iterator<TestCase> iterator() {
    return preAmble.iterator();
  }

  public int size() {
    return preAmble.size();
  }

  public TestCase get(int i) {
    return preAmble.get(i);
  }

  public void add(TestCase tc) {
    preAmble.add(tc);
  }

  public Collection<TestCase> getUnderlyingStructure() {
    return preAmble;
  }
}
