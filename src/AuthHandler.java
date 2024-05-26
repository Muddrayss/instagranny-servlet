import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthHandler extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");

        System.out.println("Action: " + action); // Debug statement

        if ("login".equals(action)) {
            handleLogin(request, response);
        } else if ("createPost".equals(action)) {
            handleCreatePost(request, response);
        } else if ("addComment".equals(action)) {
            handleAddComment(request, response);
        } else if ("addLike".equals(action)) {
            handleAddLike(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleFetchPosts(request, response);
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        System.out.println("Logging in: " + username); // Debug statement

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid username or password");
            return;
        }

        try (DatabaseManager db = new DatabaseManager()) {
            String query = "SELECT password FROM GrannyUser WHERE username = ?";
            try (PreparedStatement pstmt = db.getConnection().prepareStatement(query)) {
                pstmt.setString(1, username);
                try (ResultSet resultSet = pstmt.executeQuery()) {
                    if (resultSet.next()) {
                        String storedHashedPassword = resultSet.getString("password");
                        if (HashUtil.hashPassword(password).equals(storedHashedPassword)) {
                            HttpSession session = request.getSession();
                            session.setAttribute("username", username);

                            System.out.println("Login successful"); // Debug statement

                            response.sendRedirect("auth"); // Redirect to servlet to fetch posts
                        } else {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Incorrect password");
                        }
                    } else {
                        registerUser(request, response, db);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void handleCreatePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to create a post");
            return;
        }

        String description = request.getParameter("description");
        if (description == null || description.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Post content cannot be empty");
            return;
        }

        try (DatabaseManager db = new DatabaseManager()) {
            String query = "INSERT INTO Post (username, description) VALUES (?, ?)";
            try (PreparedStatement pstmt = db.getConnection().prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, description);
                pstmt.executeUpdate();
                System.out.println("Post created successfully"); // Debug statement
                response.sendRedirect("auth"); // Redirect to servlet to fetch posts
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void handleAddComment(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to add a comment");
            return;
        }

        String postIdStr = request.getParameter("post_id");
        String commentText = request.getParameter("comment");
        if (postIdStr == null || commentText == null || commentText.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Comment content cannot be empty");
            return;
        }

        int postId = Integer.parseInt(postIdStr);

        try (DatabaseManager db = new DatabaseManager()) {
            String query = "INSERT INTO Comment (post_id, username, comment) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = db.getConnection().prepareStatement(query)) {
                pstmt.setInt(1, postId);
                pstmt.setString(2, username);
                pstmt.setString(3, commentText);
                pstmt.executeUpdate();

                System.out.println("Comment added successfully"); // Debug statement

                response.sendRedirect("auth"); // Redirect to servlet to fetch posts
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void handleAddLike(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        if (username == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You must be logged in to like a post");
            return;
        }

        String postIdStr = request.getParameter("post_id");
        if (postIdStr == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Post ID is required to like a post");
            return;
        }

        int postId = Integer.parseInt(postIdStr);

        try (DatabaseManager db = new DatabaseManager()) {
            // Check if the like already exists
            String checkQuery = "SELECT COUNT(*) FROM PostLike WHERE post_id = ? AND username = ?";
            try (PreparedStatement checkStmt = db.getConnection().prepareStatement(checkQuery)) {
                checkStmt.setInt(1, postId);
                checkStmt.setString(2, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {

                        System.out.println("Post already liked by user"); // Debug statement

                        response.sendRedirect("auth"); // Redirect to servlet to fetch posts
                        return;
                    }
                }
            }

            // Like does not exist, insert new like
            String query = "INSERT INTO PostLike (post_id, username) VALUES (?, ?)";
            try (PreparedStatement pstmt = db.getConnection().prepareStatement(query)) {
                pstmt.setInt(1, postId);
                pstmt.setString(2, username);
                pstmt.executeUpdate();

                System.out.println("Post liked successfully"); // Debug statement

                response.sendRedirect("auth"); // Redirect to servlet to fetch posts
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }

    private void handleFetchPosts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Map<String, Object>> posts = new ArrayList<>();

        try (DatabaseManager db = new DatabaseManager()) {
            String query = "SELECT p.id AS post_id, p.username AS post_username, p.description AS post_description, " +
                    "p.created_at AS post_created_at, COALESCE(pl.like_count, 0) AS like_count, " +
                    "c.id AS comment_id, c.username AS comment_username, c.comment AS comment_text, " +
                    "c.created_at AS comment_created_at " +
                    "FROM Post p " +
                    "LEFT JOIN (SELECT post_id, COUNT(*) AS like_count FROM PostLike GROUP BY post_id) pl " +
                    "ON p.id = pl.post_id " +
                    "LEFT JOIN Comment c ON p.id = c.post_id " +
                    "ORDER BY p.created_at DESC LIMIT 5";

            try (Statement stmt = db.getConnection().createStatement();
                 ResultSet resultSet = stmt.executeQuery(query)) {
                Map<Integer, Map<String, Object>> postMap = new HashMap<>();

                while (resultSet.next()) {
                    int postId = resultSet.getInt("post_id");
                    Map<String, Object> post = postMap.get(postId);

                    if (post == null) {
                        post = new HashMap<>();
                        post.put("post_id", postId);
                        post.put("post_username", resultSet.getString("post_username"));
                        post.put("post_description", resultSet.getString("post_description"));
                        post.put("post_created_at", resultSet.getTimestamp("post_created_at"));
                        post.put("like_count", resultSet.getInt("like_count"));
                        post.put("comments", new ArrayList<Map<String, Object>>());
                        postMap.put(postId, post);
                        posts.add(post);
                    }

                    if (resultSet.getObject("comment_id") != null) {
                        Map<String, Object> comment = new HashMap<>();
                        comment.put("comment_id", resultSet.getInt("comment_id"));
                        comment.put("comment_username", resultSet.getString("comment_username"));
                        comment.put("comment_text", resultSet.getString("comment_text"));
                        comment.put("comment_created_at", resultSet.getTimestamp("comment_created_at"));
                        ((List<Map<String, Object>>) post.get("comments")).add(comment);
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }

        request.setAttribute("posts", posts);

        System.out.println("Posts fetched: " + posts.size()); // Debug statement

        RequestDispatcher dispatcher = request.getRequestDispatcher("home.jsp");
        dispatcher.forward(request, response);
    }

    private void registerUser(HttpServletRequest request, HttpServletResponse response, DatabaseManager db)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String hashedPassword = HashUtil.hashPassword(password);

        String query = "INSERT INTO GrannyUser (username, password) VALUES (?, ?)";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
            HttpSession session = request.getSession();
            session.setAttribute("username", username);

            System.out.println("User registered: " + username); // Debug statement

            response.sendRedirect("auth"); // Redirect to servlet to fetch posts
        } catch (SQLException e) {
            throw new ServletException("Database error", e);
        }
    }
}
