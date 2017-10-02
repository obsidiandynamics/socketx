package sample.servlet;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

public final class HealthCheckServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;
  
  @Override
  public void init(ServletConfig config) throws ServletException {
    System.out.format("Initialised with %s\n", config);
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.getWriter().write("Cruizin'");
  }
}