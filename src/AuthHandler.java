import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
// import java.io.PrintWriter;

// Ennio Gualandi
public class AuthHandler extends HttpServlet {
    // Versione con JSP
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if ("Ennio".equals(username) && "123456".equals(password)) {
            request.getSession().setAttribute("username", username);
            response.sendRedirect("home.jsp");
        } else {
            response.sendRedirect("index.html");
        }
    }

    // Versione senza JSP
    /*@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (!"Ennio".equals(username) || !"123456".equals(password)) {
            response.sendRedirect("index.html");
        }

        response.setContentType("text/html");
        PrintWriter output = response.getWriter();

        output.println("<html lang=\"en\">");
        output.println("<head>");
        output.println("<meta charset=\"UTF-8\">");
        output.println("<title>Home</title>");
        output.println("</head>");
        output.println("<body>");
        output.println("<h1>Welcome, " + username + "!</h1>");
        output.println("</body>");
        output.println("</html>");
    }*/
}
