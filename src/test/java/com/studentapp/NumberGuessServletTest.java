package com.studentapp;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

public class NumberGuessServletTest {
    private NumberGuessServlet servlet;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private StringWriter responseWriter;

    @Before
    public void setUp() throws Exception {
        servlet = new NumberGuessServlet();
        servlet.init();
        request = Mockito.mock(HttpServletRequest.class);
        response = Mockito.mock(HttpServletResponse.class);
        responseWriter = new StringWriter();
        Mockito.when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    public void testInvalidInput() throws Exception {
        System.out.println("✅ Testing invalid input handling...");
        Mockito.when(request.getParameter("guess")).thenReturn("abc");
        servlet.doPost(request, response);
        String output = responseWriter.toString();
        assertTrue("Should handle invalid input", output.contains("Invalid input"));
    }

    @Test
    public void testValidInput() throws Exception {
        System.out.println("✅ Testing valid input handling...");
        Mockito.when(request.getParameter("guess")).thenReturn("50");
        servlet.doPost(request, response);
        String output = responseWriter.toString();
        assertTrue("Should provide game feedback", 
                   output.contains("too low") || 
                   output.contains("too high") || 
                   output.contains("Congratulations"));
    }

    @Test
    public void testBasicFunctionality() throws Exception {
        System.out.println("✅ Testing basic servlet functionality...");
        // This test just ensures the servlet initializes properly
        assertNotNull("Servlet should be initialized", servlet);
    }
}
