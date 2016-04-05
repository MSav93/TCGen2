package renderers;

import java.util.Set;

import javax.swing.table.AbstractTableModel;

import tree.Node;

public class CPModel extends AbstractTableModel {
  private static final long serialVersionUID = -7480665278104499754L;
  Set<Node> cps;

  public CPModel(Set<Node> cps) {
    this.cps = cps;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public Class getColumnClass(int columnIndex) {
    return Node.class;
  }

  public int getColumnCount() {
    return 1;
  }

  public String getColumnName(int columnIndex) {
    return "Nodes";
  }

  public int getRowCount() {
    return (cps == null) ? 0 : cps.size();
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    return (cps == null) ? null : cps.toArray()[rowIndex];
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }
}
