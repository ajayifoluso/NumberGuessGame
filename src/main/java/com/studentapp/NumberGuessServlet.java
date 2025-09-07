package com.studentapp;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

public class NumberGuessServletTest {
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private HttpSession session;
    
    @Mock
    private PrintWriter printWriter;
    
    private StringWriter stringWriter;
    private NumberGuessServlet servlet;
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servlet = new NumberGuessServlet();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        
        when(response.getWriter()).thenReturn(printWriter);
        when(request.getSession()).thenReturn(session);
    }
    
    @Test
    public void testBasicServletFunctionality() throws Exception {
        System.out.println("✅ Testing basic servlet functionality...");
        
        when(session.getAttribute("targetNumber")).thenReturn(null);
        when(session.getAttribute("attempts")).thenReturn(0);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        verify(session).setAttribute(eq("targetNumber"), any(Integer.class));
        verify(session).setAttribute("attempts", 0);
        
        String output = stringWriter.toString();
        assertTrue("Should contain game form", output.contains("<form"));
        assertTrue("Should contain input field", output.contains("input"));
    }
    
    @Test
    public void testInvalidInput() throws Exception {
        System.out.println("✅ Testing invalid input handling...");
        
        when(request.getParameter("guess")).thenReturn("");
        when(session.getAttribute("targetNumber")).thenReturn(50);
        when(session.getAttribute("attempts")).thenReturn(1);
        
        servlet.doPost(request, response);
        
        String output = stringWriter.toString();
        assertTrue("Should handle invalid input", 
                  output.contains("Please enter a valid number") || 
                  output.contains("valid") || 
                  output.contains("error"));
    }
    
    @Test
    public void testValidInput() throws Exception {
        System.out.println("✅ Testing valid input handling...");
        
        when(request.getParameter("guess")).thenReturn("25");
        when(session.getAttribute("targetNumber")).thenReturn(50);
        when(session.getAttribute("attempts")).thenReturn(1);
        
        servlet.doPost(request, response);
        
        verify(session).setAttribute("attempts", 2);
        
        String output = stringWriter.toString();
        assertTrue("Should provide game feedback", 
                  output.contains("Too low") || 
                  output.contains("Too high") || 
                  output.contains("Congratulations") ||
                  output.contains("higher") ||
                  output.contains("lower"));
    }
}
