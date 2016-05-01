package table;

import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import other.PreAmble;

public class TestCasePreAmbleModel extends AbstractTableModel {
  private static final long serialVersionUID = -7480665278104499754L;
  Collection<PreAmble> paths;

  public TestCasePreAmbleModel(Collection<PreAmble> paths) {
    this.paths = paths;
  }

  public void addData(Collection<PreAmble> paths) {
    this.paths = paths;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Class getColumnClass(int columnIndex) {
    return PreAmble.class;
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
    if (paths == null) {
      return null;
    } else {
      return paths.toArray()[rowIndex];
    }
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }
}
