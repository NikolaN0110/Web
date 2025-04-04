package org.example.web4;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/meal-selection")
public class MealServlet extends HttpServlet {

    public static ConcurrentHashMap <String,  ConcurrentHashMap<String,Integer>> meals;
    private static String password;

    public void init() {
        meals=new ConcurrentHashMap<String,ConcurrentHashMap<String,Integer>>();
        loadMealsFromFiles();
        loadPassword();

    }

    private void loadMealsFromFiles(){
        String[] days = {"ponedeljak", "utorak", "sreda", "cetvrtak", "petak"};
        String basePath = "C:\\Users\\nikol\\Desktop\\web domaci 4\\web4\\src\\main\\resources\\";

        for (String day : days) {
            ConcurrentHashMap<String, Integer> mealList = new ConcurrentHashMap<>();
            String fullPath = basePath + day + ".txt";

            try (BufferedReader reader = new BufferedReader(new FileReader(fullPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    mealList.put(line, 0);
                }
            } catch (IOException e) {
                System.err.println("Greška pri učitavanju za dan: " + day + " -> " + e.getMessage());
            }

            meals.put(day, mealList);
        }
    }

    public static void loadPassword(){
        String path = "C:\\Users\\nikol\\Desktop\\web domaci 4\\web4\\src\\main\\resources\\password.txt";

        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            password = reader.readLine();
            reader.close();
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void resetMealCounters(){
        for (Map.Entry<String, ConcurrentHashMap<String, Integer>> dayEntry : meals.entrySet()) {
            ConcurrentHashMap<String, Integer> dayMeals = dayEntry.getValue();
            synchronized (dayMeals) {  // synchronize on the inner map
                for (Map.Entry<String, Integer> mealEntry : dayMeals.entrySet()) {
                    dayMeals.put(mealEntry.getKey(), 0);
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();


        String userSession = (String) request.getSession().getAttribute("userSession");

        boolean userLoggedIn = (userSession != null);

        out.println("<html><body>");

        if (userLoggedIn) {
            out.println("<h2>Već ste odabrali svoje obroke za ovu nedelju.</h2>");
            out.println("<h3>Vaši odabrani obroci:</h3>");
            String[] days = {"ponedeljak", "utorak", "sreda", "cetvrtak", "petak"};
            for (String day : days) {
                String selectedMeal = (String) request.getSession().getAttribute(day);
                if (selectedMeal != null) {
                    out.println("<p>" + capitalize(day) + ": " + selectedMeal + "</p>");
                }
            }
            out.println("<br><a href='/meal-selection'>Povratak na izbor obroka</a>");
        } else {
            out.println("<h1>Odaberite obroke</h1>");
            out.println("<form method='POST' action='/meal-selection'>");

            String[] days = {"ponedeljak", "utorak", "sreda", "cetvrtak", "petak"};

            for (String day : days) {
                out.println("<h3>" + capitalize(day) + "</h3>");
                out.println("<select name='" + day + "'>");
                ConcurrentHashMap<String, Integer> dayMeals = meals.get(day);
                if (dayMeals != null) {
                    synchronized (dayMeals) {
                        for (String meal : dayMeals.keySet()) {
                            out.println("<option value='" + meal + "'>" + meal + "</option>");
                        }
                    }
                }
                out.println("</select><br>");
            }

            out.println("<br><input type='password' name='password' placeholder='Enter password' required><br>");
            out.println("<input type='submit' value='Potvrdite unos'>");
            out.println("</form>");
        }

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        // Validate password
        String passwordInput = request.getParameter("password");

        if (passwordInput != null && passwordInput.equals(password)) {

            request.getSession().setAttribute("userSession", "active");


            String[] days = {"ponedeljak", "utorak", "sreda", "cetvrtak", "petak"};
            for (String day : days) {
                String selectedMeal = request.getParameter(day);
                if (selectedMeal != null) {
                    // Store the selected meal in session
                    request.getSession().setAttribute(day, selectedMeal);

                    // Update the counter for the selected meal
                    ConcurrentHashMap<String, Integer> dayMeals = meals.get(day);
                    if (dayMeals != null) {
                        synchronized (dayMeals) {
                            Integer count = dayMeals.get(selectedMeal);
                                dayMeals.put(selectedMeal, count + 1);
                        }
                    }
                }
            }

            out.println("<html><body>");
            out.println("<h2>Obroci su uspešno odabrani!</h2>");
            out.println("<br><a href='/meal-selection'>Povratak na izbor obroka</a>");
            out.println("</body></html>");
        } else {
            out.println("<html><body>");
            out.println("<h2>Neispravan password!</h2>");
            out.println("<br><a href='/meal-selection'>Povratak na izbor obroka</a>");
            out.println("</body></html>");
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}





