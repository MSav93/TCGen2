package renderers;

import java.util.Collection;

import javax.swing.table.AbstractTableModel;

import tree.Node;

public class NodeTableModel extends AbstractTableModel {
  private static final long serialVersionUID = -7480665278104499754L;
  Collection<Node> nodes;

  public NodeTableModel(Collection<Node> cps) {
    this.nodes = cps;
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
    return (nodes == null) ? 0 : nodes.size();
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    return (nodes == null) ? null : nodes.toArray()[rowIndex];
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }
}
