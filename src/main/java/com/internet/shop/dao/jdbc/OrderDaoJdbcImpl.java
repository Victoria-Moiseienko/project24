package com.internet.shop.dao.jdbc;

import com.internet.shop.dao.OrderDao;
import com.internet.shop.exeptions.DataProcessingException;
import com.internet.shop.lib.Dao;
import com.internet.shop.model.Order;
import com.internet.shop.model.Product;
import com.internet.shop.util.DbConnectionUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Dao
public class OrderDaoJdbcImpl implements OrderDao {
    @Override
    public List<Order> getUserOrders(Long userId) {
        String query = "SELECT * FROM orders AS o"
                       + " WHERE o.user_id = ? AND o.deleted = FALSE ";
        List<Order> orderList = new ArrayList<>();
        try (Connection connection = DbConnectionUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, userId);
            ResultSet sqlResult = preparedStatement.executeQuery();
            while (sqlResult.next()) {
                Long id = sqlResult.getLong("order_id");
                Order order = new Order(id, userId);
                orderList.add(order);
            }
            preparedStatement.close();
            for (Order order : orderList) {
                order.setProducts(extractProductForOrder(order.getId(), connection));
            }
        } catch (SQLException e) {
            throw new DataProcessingException(
                    "Can't get all orders for user with id = " + userId, e);
        }
        return orderList;
    }

    @Override
    public Order create(Order order) {
        String query = "INSERT INTO orders (user_id) VALUES (?)";
        try (Connection connection = DbConnectionUtil.getConnection()) {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, order.getUserId());
            preparedStatement.executeUpdate();
            ResultSet resultKey = preparedStatement.getGeneratedKeys();
            if (resultKey.next()) {
                order.setId(resultKey.getLong(1));
            }
            insertProducts(order, connection);
        } catch (SQLException e) {
            throw new DataProcessingException("Order for user with id " + order.getUserId()
                    + " has not been added", e);
        }
        return order;
    }

    private void insertProducts(Order order, Connection connection) throws SQLException {
        String query = "INSERT INTO orders_products (orders_id, products_id) VALUES (?, ?)";
        for (Product product : order.getProducts()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, order.getId());
            preparedStatement.setLong(2, product.getId());
            preparedStatement.executeUpdate();
        }
    }

    @Override
    public Optional<Order> get(Long id) {
        String query = "SELECT * FROM orders"
                        + " JOIN orders_products AS op ON orders.order_id = op.orders_id"
                        + " JOIN products ON products.product_id = op.products_id"
                        + " WHERE orders.order_id = ? ";
        try (Connection connection = DbConnectionUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setLong(1, id);
            ResultSet sqlResult = preparedStatement.executeQuery();
            if (sqlResult.next()) {
                Order order = parseOrder(sqlResult);
                return Optional.of(order);
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get order by id = " + id, e);
        }
        return Optional.empty();
    }

    private Order parseOrder(ResultSet sqlResult) throws SQLException {
        Long id = sqlResult.getLong("order_id");
        Long userId = sqlResult.getLong("user_id");
        List<Product> productList = new ArrayList<>();
        do {
            productList.add(parseProduct(sqlResult));
        } while (sqlResult.next());
        return new Order(id, userId, productList);
    }

    private Product parseProduct(ResultSet sqlResult) throws SQLException {
        Long productId = sqlResult.getLong("product_id");
        String name = sqlResult.getString("name");
        Double price = sqlResult.getDouble("price");
        return new Product(productId, name, price);
    }

    @Override
    public Order update(Order order) {
        try (Connection connection = DbConnectionUtil.getConnection()) {
            deleteProductsFromOrder(order.getId(), connection);
            insertProducts(order, connection);
        } catch (SQLException e) {
            throw new DataProcessingException(
                    "Can't update order for user = " + order.getUserId(), e);
        }
        return order;
    }

    @Override
    public boolean delete(Long id) {
        String query = "UPDATE orders"
                        + " SET deleted = TRUE"
                        + " WHERE order_id = ?";
        try (Connection connection = DbConnectionUtil.getConnection()) {
            PreparedStatement prepareStatement = connection.prepareStatement(query);
            prepareStatement.setLong(1, id);
            deleteProductsFromOrder(id, connection);
            return prepareStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataProcessingException("Order with id =" + id
                    + " has not been deleted", e);
        }
    }

    private void deleteProductsFromOrder(Long id, Connection connection) throws SQLException {
        String query = "DELETE FROM orders_products WHERE orders_id = ?";
        PreparedStatement prepareStatement = connection.prepareStatement(query);
        prepareStatement = connection.prepareStatement(query);
        prepareStatement.setLong(1, id);
        prepareStatement.executeUpdate();
    }

    @Override
    public List<Order> getAll() {
        String query = "SELECT * FROM orders AS o"
                        + " JOIN orders_products AS op ON o.order_id = op.orders_id"
                        + " JOIN products AS p ON p.product_id = op.products_id"
                        + " WHERE o.deleted = FALSE ";
        List<Order> orderList = new ArrayList<>();
        try (Connection connection = DbConnectionUtil.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet sqlResult = preparedStatement.executeQuery();
            while (sqlResult.next()) {
                Long id = sqlResult.getLong("order_id");
                Long userId = sqlResult.getLong("user_id");
                orderList.add(new Order(id, userId));
            }
            for (Order order : orderList) {
                order.setProducts(extractProductForOrder(order.getId(), connection));
            }
        } catch (SQLException e) {
            throw new DataProcessingException("Can't get all orders", e);
        }
        return orderList;
    }

    private List<Product> extractProductForOrder(Long id, Connection connection)
            throws SQLException {
        String query = "SELECT * FROM products AS p"
                        + " JOIN orders_products AS op ON p.product_id = op.products_id"
                        + " WHERE op.orders_id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, id);
        ResultSet resultSet = statement.executeQuery();
        List<Product> products = new ArrayList<>();
        while (resultSet.next()) {
            products.add(parseProduct(resultSet));
        }
        return products;
    }
}
