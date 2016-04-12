package table;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import other.Constants;
import tree.Node;

public class CPCell extends NodeTableCell implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = 7053396305268198391L;

  @Override
  protected void updateData(Node node, boolean isSelected, JTable table) {
    super.updateData(node, isSelected, table);
    if (node.isCp()) {
      panel.setBackground(Constants.selectedColour);
    } else {
      panel.setBackground(Constants.notSelectedColour);
    }
  }
}
