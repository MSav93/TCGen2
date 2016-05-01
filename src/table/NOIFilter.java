package table;

import javax.swing.RowFilter;

import other.Constants;
import tree.Node;

public class NOIFilter extends RowFilter<NodeTableModel, Integer> {

  public boolean include(RowFilter.Entry<? extends NodeTableModel, ? extends Integer> entry) {
    NodeTableModel personModel = entry.getModel();
    Node value = personModel.getValueAt(entry.getIdentifier(), 0);
    if (!value.isCp() && Constants.acceptedNOIFlags.contains(value.getFlag())) {
      return true;
    }
    return false;
  }
}
