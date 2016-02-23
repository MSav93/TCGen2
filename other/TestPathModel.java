package other;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class TestPathModel extends AbstractTableModel {
  private static final long serialVersionUID = -7480665278104499754L;
  List<TestPath> paths;

  public TestPathModel(List<TestPath> paths) {
    this.paths = paths;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Class getColumnClass(int columnIndex) {
    return TestPath.class;
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
    return (paths == null) ? null : paths.get(rowIndex);
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return true;
  }
}
