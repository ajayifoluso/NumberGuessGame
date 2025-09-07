package com.studentapp;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.io.StringWriter;

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
        when(session.getAttribute("attempts")).thenReturn(null);
        
        servlet.doGet(request, response);
        
        verify(response).setContentType("text/html");
        verify(session).setAttribute(eq("targetNumber"), any(Integer.class));
        verify(session).setAttribute("attempts", 0);
        
        String output = stringWriter.toString();
        assertTrue("Should contain game form", output.contains("<form"));
        assertTrue("Should contain input field", output.contains("input"));
        assertTrue("Should contain game title", output.contains("Number Guessing Game"));
    }
    
    @Test
    public void testInvalidInput() throws Exception {
        System.out.println("✅ Testing invalid input handling...");
        
        when(request.getParameter("guess")).thenReturn("abc");
        when(session.getAttribute("targetNumber")).thenReturn(50);
        when(session.getAttribute("attempts")).thenReturn(1);
        when(session.getAttribute("gameWon")).thenReturn(false);
        
        servlet.doPost(request, response);
        
        String output = stringWriter.toString();
        assertTrue("Should handle invalid input", 
                  output.contains("Invalid input") || 
                  output.contains("valid number"));
    }
    
    @Test
    public void testValidInputTooLow() throws Exception {
        System.out.println("✅ Testing valid input handling (too low)...");
        
        when(request.getParameter("guess")).thenReturn("25");
        when(session.getAttribute("targetNumber")).thenReturn(50);
        when(session.getAttribute("attempts")).thenReturn(1);
        when(session.getAttribute("gameWon")).thenReturn(false);
        
        servlet.doPost(request, response);
        
        verify(session).setAttribute("attempts", 2);
        
        String output = stringWriter.toString();
        assertTrue("Should provide feedback for too low", 
                  output.contains("Too low") || 
                  output.contains("higher"));
    }
    
    @Test
    public void testValidInputTooHigh() throws Exception {
        System.out.println("✅ Testing valid input handling (too high)...");
        
        when(request.getParameter("guess")).thenReturn("75");
        when(session.getAttribute("targetNumber")).thenReturn(50);
        when(session.getAttribute("attempts")).thenReturn(1);
        when(session.getAttribute("gameWon")).thenReturn(false);
        
        servlet.doPost(request, response);
        
        verify(session).setAttribute("attempts", 2);
        
        String output = stringWriter.toString();
        assertTrue("Should provide feedback for too high", 
                  output.contains("Too high") || 
                  output.contains("lower"));
    }
    
    @Test
    public void testCorrectGuess() throws Exception {
        System.out.println("✅ Testing correct guess...");
        
        when(request.getParameter("guess")).thenReturn("50");
        when(session.getAttribute("targetNumber")).thenReturn(50);
        when(session.getAttribute("attempts")).thenReturn(1);
        when(session.getAttribute("gameWon")).thenReturn(false);
        
        servlet.doPost(request, response);
        
        verify(session).setAttribute("attempts", 2);
        verify(session).setAttribute("gameWon", true);
        
        String output = stringWriter.toString();
        assertTrue("Should congratulate on correct guess", 
                  output.contains("Congratulations") ||
                  output.contains("guessed it right"));
    }
    
    @Test
    public void testNewGameAction() throws Exception {
        System.out.println("✅ Testing new game action...");
        
        when(request.getParameter("action")).thenReturn("newGame");
        when(session.getAttribute("targetNumber")).thenReturn(50);
        when(session.getAttribute("attempts")).thenReturn(5);
        when(session.getAttribute("gameWon")).thenReturn(true);
        
        servlet.doPost(request, response);
        
        verify(session).removeAttribute("targetNumber");
        verify(session).removeAttribute("attempts");
        verify(session).removeAttribute("gameWon");
    }
    
    @Test
    public void testOutOfRangeInput() throws Exception {
        System.out.println("✅ Testing out of range input...");
        
        when(request.getParameter("guess")).thenReturn("150");
        when(session.getAttribute("targetNumber")).thenReturn(50);
        when(session.getAttribute("attempts")).thenReturn(1);
        when(session.getAttribute("gameWon")).thenReturn(false);
        
        servlet.doPost(request, response);
        
        String output = stringWriter.toString();
        assertTrue("Should handle out of range input", 
                  output.contains("between 1 and 100"));
    }
}