package renderers;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import other.TestCase;
import tree.Node;

public class TestPathModel extends AbstractTableModel {
  private static final long serialVersionUID = -7480665278104499754L;
  List<TestCase> paths;
  private String[] columnNames =
      {"ID", "Start Node", "End Node", "Steps", "Observations", "User Actions"};
  @SuppressWarnings("rawtypes")
  private Class[] columnTypes = new Class[] {Integer.class, Node.class, Node.class, Integer.class,
      Boolean.class, Boolean.class};

  public TestPathModel(List<TestCase> paths) {
    this.paths = paths;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Class getColumnClass(int columnIndex) {
    return columnTypes[columnIndex];
  }

  public int getColumnCount() {
    return columnNames.length;
  }

  public String getColumnName(int columnIndex) {
    return columnNames[columnIndex];
  }

  public int getRowCount() {
    return (paths == null) ? 0 : paths.size();
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    return (paths == null) ? null : paths.get(rowIndex);
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }
}
