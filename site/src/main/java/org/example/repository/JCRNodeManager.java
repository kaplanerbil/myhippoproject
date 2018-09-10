package org.example.repository;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

/**
 * Utility class for Item Processes.
 * 
 * @author Erbil Kaplan
 * 
 */
public class JCRNodeManager {
	
	private static final String HTML_NEW_LINE_TAG = "<br/>";
	private static final String HTML_TAB_TAG = "&emsp;";
	private static String XPATH_QUERY_FOR_GENERIC_TEXT = "//*[jcr:contains(.,'*<keyword>*')]";

    /**
     * Returns all sub nodes for given node.
     * 
     * @param node represents a node in a workspace
     * @return List<Item> if no errors
     * @throws RepositoryException
     */
	public List<Item> getAllSubNodes(final Node node) throws RepositoryException {
		List<Item> nodeList = new ArrayList<>();
		// to prevent infinite traversing
		if (!node.isNodeType("hippofacnav:facetnavigation")) {
			NodeIterator subNodes = node.getNodes();
			while (subNodes.hasNext()) {
				Node nextNode = subNodes.nextNode();
				nodeList.add(nextNode);
				if (nextNode.getNodes() != null) {
					nodeList.addAll(getAllSubNodes(nextNode));
				}
			}
		}
		return nodeList;
	}
	
	/**
	 * Returns all sub properties for given node.
	 *      
	 * @param node represents a node in a workspace
	 * @return List
	 * @throws RepositoryException
	 */
	public List<Item> getAllPropertiesByNode(final Node node) throws RepositoryException {
		PropertyIterator properties = node.getProperties();
		List<Item> propertiesList = new ArrayList<>();
		
		// traverse all nodes and filter by "nodeName" 
		while (properties.hasNext()) {
			propertiesList.add(properties.nextProperty());
		}
		return propertiesList;	
	}
	
	/**
	 * Returns the node specified by the given name and its all subnodes
	 * 
	 * @param session
	 * @param nodeName 
	 * @return List contains a node and its subNodes
	 * @throws RepositoryException
	 */
	public List<Item> getNodeAndSubNodesByNodeName(final Session session, final String nodeName) throws RepositoryException {
		Node node = findNodeByName(session, nodeName);
		if(node==null) return new ArrayList<>();
		
		List<Item> list = new ArrayList<>();
		list.add(node);
		list.addAll(getAllSubNodes(node));
		return list;
	}
	
	/**
	 * Returns the node specified by the given name
	 * 
	 * @param session
	 * @param nodeName
	 * @return Node
	 * @throws RepositoryException
	 */
	public Node findNodeByName(final Session session, final String nodeName) throws RepositoryException {
		Node rootNode = session.getRootNode();
		NodeIterator nodes = rootNode.getNodes();		
		// traverse all nodes and filter by "nodeName" 
		while (nodes.hasNext()) {
			Node node = nodes.nextNode();
			if(node.getName().equals(nodeName)) {
				return node;
			}
		}
		return null;	
	}
	
	/**
	 * Generates html output for given itemList
	 * 
	 * @param itemList can contain Nodes or Properties 
	 * @param putLeftSpace is condition to place paragraph to the left side or not. 
	 * @return 
	 * @throws RepositoryException
	 */
	public String generateServletOutputFromItemList(final List<Item> itemList, final boolean putLeftSpace) throws RepositoryException {
		StringBuilder out = new StringBuilder("");
		for (Item item : itemList) {
			out.append(putLeftSpace ? generateLeftSpace(item) : "");
			out.append(" - ").append(item.getName());
			out.append(item.isNode() ? " (Node)" : " (Property)");
			out.append(" - Path=\"").append(item.getPath()).append("\"");
			out.append(HTML_NEW_LINE_TAG);
		}
		return out.toString();
	}

	/**
	 * Generates tabs for left side to show hierarchy between nodes, depend on item depth
	 * 
	 * @param <code>Item</code> needed due to depth information
	 * @return A <code>String</code> 
	 * @throws RepositoryException
	 */
	public String generateLeftSpace(final Item item) throws RepositoryException {
		StringBuilder s = new StringBuilder();
		for(int i=0; i<item.getDepth(); i++)
			s.append(HTML_TAB_TAG);
		return s.toString();
	}

	/**
	 * Executes given <code>Query</code> and returns records in ArrayList
	 * 
	 * @param query
	 * @return List contains filtered nodes and those all properties
	 * @throws RepositoryException
	 */
	public List<Item> executeQuery(final Query query) throws RepositoryException {
		// iterate over results
		QueryResult result = query.execute();
		NodeIterator nodes = result.getNodes();
		List<Item> itemList = new ArrayList<>();
		while (nodes.hasNext()) {
			Node node = nodes.nextNode();
			itemList.add(node);
			itemList.addAll(getAllPropertiesByNode(node));
		}
		return itemList;
	}
	
	/**
	 * Searching all nodes which contain given keyword 
	 * and returns names and properties of matched nodes, in the ArrayList
	 * 
	 * @param session
	 * @param keyword A text filter to search all nodes 
	 * @return List contains filtered nodes and all those nodes' properties
	 * @throws RepositoryException
	 */
	public List<Item> queryToSearchAllNodesWithKeyword(final Session session, final String keyword) throws RepositoryException {
		String xPathQuery = XPATH_QUERY_FOR_GENERIC_TEXT.replace("<keyword>", keyword);
		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(xPathQuery, Query.XPATH);
		return executeQuery(query);
	}
	
}
