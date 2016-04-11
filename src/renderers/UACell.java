package renderers;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import other.Constants;
import tree.Node;

public class UACell extends NodeTableCell implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = 8786460640811709303L;

  @Override
  protected void updateData(Node node, boolean isSelected, JTable table) {
    super.updateData(node, isSelected, table);
    if (!node.getAction().equals("")) {
      panel.setBackground(Constants.cellChosenBG);
    } else {
      panel.setBackground(Constants.cellNotChosenBG);
    }
  }
}
