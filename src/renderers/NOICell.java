package renderers;

import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import other.Constants;
import tree.Node;

public class NOICell extends NodeTableCell implements TableCellEditor, TableCellRenderer {
  private static final long serialVersionUID = 343467290283539890L;

  @Override
  protected void updateData(Node node, boolean isSelected, JTable table) {
    super.updateData(node, isSelected, table);
    if (node.isNoi()) {
      panel.setBackground(Constants.cellChosenBG);
    } else {
      panel.setBackground(Constants.cellNotChosenBG);
    }
  }
}
