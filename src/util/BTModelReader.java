package util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import tree.Block;
import tree.Node;

public class BTModelReader {

  private TreeMap<Integer, Block> indexToNodes = new TreeMap<Integer, Block>();

  public BTModelReader(String result) {
    org.jdom2.input.SAXBuilder saxBuilder = new SAXBuilder();

    // XML parser has problem reading ||&
    result = result.replace("||&", "");

    org.jdom2.Document doc = null;
    try {
      doc = saxBuilder.build(new StringReader(result));
    } catch (JDOMException | IOException e) {
      e.printStackTrace();
    }
    Element rootNode = doc.getRootElement();
    List<Element> blocks = rootNode.getChildren("block");

    for (int i = 0; i < blocks.size(); i++) {
      indexToNodes.put(Integer.parseInt(blocks.get(i).getChildText("block-index")),
          readBTNode(blocks.get(i), Integer.parseInt(blocks.get(i).getChildText("block-index"))));
    }
  }

  public TreeMap<Integer, Block> getIndexToNodeMap() {
    return indexToNodes;
  }

  private ArrayList<Node> extractNodes(Element block, int index) {
    List<Element> nodes = block.getChildren("node");
    ArrayList<Node> nodesArray = new ArrayList<Node>();

    for (Element nodeInfo : nodes) {
      nodesArray.add(new Node(Integer.parseInt(nodeInfo.getChildText("tag")),
          nodeInfo.getChildText("component"), nodeInfo.getChildText("behaviour-type"),
          nodeInfo.getChildText("behaviour"), nodeInfo.getChildText("flag"), index));
    }
    return nodesArray;
  }

  private static ArrayList<Integer> extractChildrenIndices(Element block) {
    ArrayList<Integer> childrenIndices = new ArrayList<Integer>();
    List<Element> children = block.getChild("children").getChildren();
    for (Element child : children) {
      childrenIndices.add(Integer.parseInt(child.getText()));
    }
    return childrenIndices;
  }

  private Block readBTNode(Element block, int index) {
    ArrayList<Node> nodes = extractNodes(block, index);

    String parentIndex = block.getChild("parent").getChildText("block-index");
    Integer parent = null;
    if (parentIndex != null) {
      parent = Integer.parseInt(parentIndex);
    }

    ArrayList<Integer> children = extractChildrenIndices(block);
    String branchType = block.getChildText("branch-type");
    return new Block(parent, children, index, branchType, nodes);
  }
}
