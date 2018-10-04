package ebookshop.dao;

import java.util.*;
import java.io.*;

import javax.security.auth.message.callback.PrivateKeyCallback.Request;
import javax.servlet.*;
import javax.servlet.http.*;

import ebookshop.pojo.Book;

public class ShoppingServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8923374322006328025L;
	private Vector<Book> shoplist;

	public void init(ServletConfig conf) throws ServletException {
		super.init(conf);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		HttpSession session = req.getSession(true);
		shoplist = (Vector<Book>) session.getAttribute("ebookshop.cart");
		String do_this = req.getParameter("do_this");
		ServletContext sc = getServletContext();
		RequestDispatcher rd = sc.getRequestDispatcher("/");
		if (do_this == null) {
			session.setAttribute("ebookshop.list", doBlist());
		} else {
			switch (do_this) {
			case "checkout":
				float dollars = 0;
				int books = 0;
				addBooksDollars(dollars, books, req);
				rd = sc.getRequestDispatcher("/Checkout.jsp");
				break;
			case "remove":
				String pos = req.getParameter("position");
				if (shoplist.size() > 0) {
					int count = shoplist.get((new Integer(pos)).intValue()).getQuantity();
					if (count > 1) {
						shoplist.get((new Integer(pos)).intValue()).setQuantity(--count);
					} else {
						shoplist.removeElementAt((new Integer(pos)).intValue());
						shoplist = (Vector<Book>) session.getAttribute("ebookshop.list");
						shoplist.removeElementAt((new Integer(pos)).intValue());
					}
				}
				break;
			case "add":
				shoplist = (Vector<Book>) session.getAttribute("ebookshop.cart");
				boolean found = false;
				Book aBook = getBook(req);
				if (aBook != null) {
					if (shoplist == null) {
						shoplist = new Vector<Book>();
						shoplist.addElement(aBook);
						session.setAttribute("ebookshop.cart", shoplist);
					} else {
						for (int i = 0; i < shoplist.size() && !found; i++) {
							Book b = (Book) shoplist.elementAt(i);
							if (b.getTitle().equals(aBook.getTitle())) {
								b.setQuantity(b.getQuantity() + aBook.getQuantity());
								shoplist.setElementAt(b, i);
								found = true;
							}
						}
						if (!found) {
							shoplist.addElement(aBook);
						}
					}
				} else {
					req.setAttribute("errorType", "1");
				}
				rd = sc.getRequestDispatcher("/");
				break;
			}
		}
		rd.forward(req, res);
	}

	private void addBooksDollars(float dollars, int books, HttpServletRequest req) {
		for (int i = 0; i < shoplist.size(); i++) {
			Book aBook = (Book) shoplist.elementAt(i);
			float price = aBook.getPrice();
			int qty = aBook.getQuantity();
			dollars += price * qty;
			books += qty;
		}
		req.setAttribute("dollars", new Float(dollars).toString());
		req.setAttribute("books", new Integer(books).toString());
	}

	private Vector<String> doBlist() {
		Vector<String> blist = new Vector<String>();
		blist.addElement("Beginning JSP, JSF and Tomcat. Zambon/Sekler $39.99");
		blist.addElement("Beginning JBoss Seam. Nusairat $39.99");
		blist.addElement("Founders at Work. Livingston $25.99");
		blist.addElement("Business Software. Sink $24.99");
		blist.addElement("Foundations of Security. Daswani/Kern/Kesavan $39.99");
		return blist;
	}

	private Book getBook(HttpServletRequest req) {
		String myBook = req.getParameter("book");
		int n = myBook.indexOf('$');
		String title = myBook.substring(0, n);
		String price = myBook.substring(n + 1);
		String qty = req.getParameter("qty");
		try {
			return new Book(title, Float.parseFloat(price), Integer.parseInt(qty));
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
