package com.internet.shop.controllers.orders;

import com.internet.shop.lib.Injector;
import com.internet.shop.model.ShoppingCart;
import com.internet.shop.service.OrderService;
import com.internet.shop.service.ShoppingCartService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CreateOrderController extends HttpServlet {
    private static final String USER_ID = "user_Id";
    private static final Injector injector = Injector.getInstance("com.internet.shop");
    private final ShoppingCartService shoppingCartService =
            (ShoppingCartService) injector.getInstance(ShoppingCartService.class);
    private final OrderService orderService =
            (OrderService) injector.getInstance(OrderService.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Long userId = (Long) session.getAttribute(USER_ID);

        ShoppingCart shoppingCart = shoppingCartService.getByUserId(userId);
        if (shoppingCart.getProducts().isEmpty()) {
            req.setAttribute("message", "Your cart is empty");
            req.getRequestDispatcher("/WEB-INF/views/carts/shoppingCart.jsp")
                    .forward(req,resp);
        }

        orderService.completeOrder(shoppingCart);
        req.getRequestDispatcher("/WEB-INF/views/orders/confirmOrder.jsp").forward(req, resp);
    }
}
