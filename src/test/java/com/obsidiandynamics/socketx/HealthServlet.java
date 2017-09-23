package com.obsidiandynamics.socketx;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

/**
 *  A simple health check that always returns 200 OK.
 */
public final class HealthServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    response.getWriter().write("Cruizin'");
  }
}