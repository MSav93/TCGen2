package util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import tree.BTNode;
import tree.NodeData;

public class BTModelReader {

  private static TreeMap<Integer, BTNode> indexToNodes = new TreeMap<Integer, BTNode>();
  private static HashMap<String, Integer> tagToIndex = new HashMap<>(); // map tag to block

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
          readBTNode(blocks, Integer.parseInt(blocks.get(i).getChildText("block-index"))));
    }
    for (Integer key : indexToNodes.keySet()) {
      System.out.println(indexToNodes.get(key));
    }
  }

  public TreeMap<Integer, BTNode> getIndexToNodeMap() {
    return indexToNodes;
  }

  public HashMap<String, Integer> getTagToIndexMap() {
    return tagToIndex;
  }

  private static ArrayList<NodeData> extractNodes(Element block) {
    Integer blockIndex = Integer.parseInt(block.getChildText("block-index"));
    List<Element> nodes = block.getChildren("node");
    ArrayList<NodeData> nodesArray = new ArrayList<NodeData>();

    for (Element nodeInfo : nodes) {
      Element tag = nodeInfo.getChild("tag"); // a tag
      nodesArray.add(new NodeData(nodeInfo.getChildText("tag"), nodeInfo.getChildText("component"),
          nodeInfo.getChildText("behaviour-type"), nodeInfo.getChildText("behaviour"), nodeInfo.getChildText("flag")));
      tagToIndex.put(tag.getText(), blockIndex); // map tag to block index
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

  private static BTNode readBTNode(List<Element> blocks, int index) {
    Element block = blocks.get(index);
    ArrayList<NodeData> nodes = extractNodes(block);

    String parentIndex = block.getChild("parent").getChildText("block-index");
    Integer parent = null;
    if (parentIndex != null) {
      parent = Integer.parseInt(parentIndex);
    }

    ArrayList<Integer> children = extractChildrenIndices(block);
    String branchType = block.getChildText("branch-type");
    return new BTNode(parent, children, index, branchType, nodes);
  }
}
