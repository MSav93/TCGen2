package table;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import other.Constants;
import tree.Node;

public class ORCell extends NodeTableCell implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = 8786460640811709303L;

  @Override
  protected void updateData(Node node, boolean isSelected, JTable table) {
    super.updateData(node, isSelected, table);
    if (!node.getObservable().equals("")) {
      panel.setBackground(Constants.nodeSelectedColour);
    } else {
      panel.setBackground(Constants.notSelectedColour);
    }
  }
}
