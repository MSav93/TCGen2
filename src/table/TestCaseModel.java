package table;

import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import other.TestCase;

public class TestCaseModel extends AbstractTableModel {
  private static final long serialVersionUID = -7480665278104499754L;
  Collection<TestCase> paths;

  public TestCaseModel(Collection<TestCase> paths) {
    this.paths = paths;
  }

  public void addData(Collection<TestCase> paths) {
    this.paths = paths;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Class getColumnClass(int columnIndex) {
    return TestCase.class;
  }

  public int getColumnCount() {
    return 1;
  }

  public String getColumnName(int columnIndex) {
    return "Feed";
  }

  public int getRowCount() {
    return (paths == null) ? 0 : paths.size();
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    return (paths == null) ? null : paths.toArray(new TestCase[] {})[rowIndex];
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }
}
