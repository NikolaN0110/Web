package org.example.web4;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebServlet("/orders")
public class OrdersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><body>");
        out.println("<h1>Pregled narudžbi po danima</h1>");


        out.println("<form method='POST' action='/orders'>");
        out.println("<input type='submit' value='Obriši sve narudžbe' />");
        out.println("</form><br>");


        String[] days = {"ponedeljak", "utorak", "sreda", "cetvrtak", "petak"};

        for (String day : days) {
            out.println("<h3>" + capitalize(day) + "</h3>");
            ConcurrentHashMap<String, Integer> dayMeals = MealServlet.meals.get(day);

            if (dayMeals != null) {
                out.println("<table border='1'><tr><th>Obrok</th><th>Broj narudžbi</th></tr>");
                synchronized (dayMeals) {
                    for (Map.Entry<String, Integer> mealEntry : dayMeals.entrySet()) {
                        out.println("<tr><td>" + mealEntry.getKey() + "</td><td>" + mealEntry.getValue() + "</td></tr>");
                    }
                }
                out.println("</table>");
            } else {
                out.println("<p>Ne postoje podaci za ovaj dan.</p>");
            }
        }

        out.println("</body></html>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        MealServlet.resetMealCounters();
        request.getSession().invalidate();
        MealServlet.loadPassword();
        response.sendRedirect("/orders");
    }



    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

}
