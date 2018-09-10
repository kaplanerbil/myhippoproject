package org.example.servlet;

import java.io.IOException;
import java.util.List;

import javax.jcr.Item;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.example.listener.JCREventListener;
import org.example.repository.JCRNodeManager;
import org.hippoecm.hst.site.HstServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Erbil Kaplan
 * 
 */
public class AssessmentServlet extends HttpServlet {
	private static Logger log = LoggerFactory.getLogger(AssessmentServlet.class);
	private static JCREventListener listener = new JCREventListener();
	private JCRNodeManager nodeManager;
	/*
	 * 	assessment 2.6
	 *  Register a listener that prints out details about events that occur on nodes
	 *  under /content/ in the tree
	 *  
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			listener.startListening();
			nodeManager = new JCRNodeManager();
		} catch (RepositoryException e) {
			log.error("Error occured when starting the JCR event listener", e);
		}
	}

	@Override
	public void destroy() {
		super.destroy();
		try {
			listener.stopListening();
		} catch (RepositoryException e) {
			log.error("Error occured when stopping the JCR event listener", e);
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		Repository repository = HstServices.getComponentManager().getComponent(Repository.class.getName());
		Session session =null;
		StringBuilder output = new StringBuilder();
		try {
			session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
			output.append(processContentNodeIteration(session));
			output.append(processKeywordSearch(session, req.getParameter("keyword")));
		} catch (RepositoryException e) {
			log.error(e.getMessage(), e);
			res.sendError(500);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			res.sendError(500);
		} finally {
			if (session != null) {
				session.logout();
				session = null;
			}
		}
		
		res.setContentType("text/html");
		res.getWriter().write(output.toString());
	}

	/**
	 * assessment 2.4 
	 * 
	 * Recursively finds all the nodes under /content/ and return html for the names of descendant nodes
	 *
	 * @param session
	 * @return
	 * @throws RepositoryException
	 */
	private String processContentNodeIteration(Session session) throws RepositoryException {
		List<Item> nodes = nodeManager.getNodeAndSubNodesByNodeName(session, "content");
		StringBuilder output=new StringBuilder();
		output.append("<b>Content Node and SubNodes:</b><br/>");
		output.append(nodeManager.generateServletOutputFromItemList(nodes, true));
		return output.toString();
	}

	/**
	 * 	assessment 2.5  
	 * 
	 *  Execute Queries from the Repository for Some Text and Display the
	 *	Names and Properties of All the Nodes That Contain That Text
	 *  getting text by passing "keyword" as request parameter.  
	 *  http://localhost:8080/site/assessment?keyword=myhippo
	 *  
	 * @param session
	 * @param keyword A text filter to search all nodes 
	 * @return
	 * @throws RepositoryException
	 */
	private String processKeywordSearch(Session session, String keyword)
			throws RepositoryException {  
		StringBuilder output=new StringBuilder();
		if(keyword!=null) {
			List<Item> keywordNodes = nodeManager.queryToSearchAllNodesWithKeyword(session, keyword);
			output.append("<br/><b>");
			output.append(keywordNodes.size()).append(" Items Found for keyword=\"").append(keyword);
			output.append("\":</b><br/>");
			output.append(nodeManager.generateServletOutputFromItemList(keywordNodes, false));
		}
		return output.toString();
	}



}