<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="javax.servlet.http.*, java.util.*, java.sql.*" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <!-- Ennio Gualandi -->
    <meta charset="UTF-8">
    <title>Home</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="relative grid grid-cols-2 px-24 justify-center items-start gap-8 min-h-[100dvh] w-full bg-gray-50 dark:bg-gray-900 pb-8">
<!-- Discover Latest Posts - Left Column - Start -->
<div class="w-full">
    <h2 class="mt-3 mb-6 self-start text-3xl font-semibold text-black dark:text-white">
        Discover the latest posts!
    </h2>
    <div class="w-full flex flex-col gap-3">
        <%
            List<Map<String, Object>> posts = (List<Map<String, Object>>) request.getAttribute("posts");

            System.out.println("Posts in JSP: " + posts); // Debug statement
            
            if (posts != null) {
                for (Map<String, Object> post : posts) {
                    String postUsername = (String) post.get("post_username");
                    String postDescription = (String) post.get("post_description");
                    Timestamp postCreatedAt = (Timestamp) post.get("post_created_at");
                    int likeCount = (int) post.get("like_count");
                    List<Map<String, Object>> comments = (List<Map<String, Object>>) post.get("comments");
        %>
        <div class="flex flex-col gap-3 p-4 rounded-md bg-white shadow dark:border dark:bg-gray-800 dark:border-gray-700">
            <div class="flex flex-row justify-between items-end w-full">
                <div class="flex flex-row items-end">
                    <img
                            src="https://robohash.org/<%= postUsername %>"
                            alt="profile_picture"
                            class="rounded-full h-12 bg-gray-400"
                    />
                    <h3 class="ml-3 text-lg text-black dark:text-white">
                        <%= postUsername %>
                    </h3>
                </div>
                <div>
                    <p class="text-black dark:text-white"><%= new java.text.SimpleDateFormat("HH:mm").format(postCreatedAt) %>
                    </p>
                </div>
            </div>
            <div>
                <p class="text-gray-600 dark:text-gray-100">
                    <%= postDescription %>
                </p>
            </div>
            <div class="flex flex-row justify-between items-center">
                <div class="text-black dark:text-white">❤️<%= likeCount %>
                </div>
                <form action="auth" method="post" style="display:inline;">
                    <input type="hidden" name="action" value="addLike">
                    <input type="hidden" name="post_id" value="<%= post.get("post_id") %>">
                    <button type="submit" class="rounded-md py-1 px-2 text-black dark:text-white">
                        Like this post!
                    </button>
                </form>
            </div>
            <hr class="w-full h-1 bg-gray-400 rounded-sm dark:bg-gray-100"/>
            <div>
                <form action="auth" method="post"  class="flex flex-row gap-3 items-center">
                    <input type="hidden" name="action" value="addComment">
                    <input type="hidden" name="post_id" value="<%= post.get("post_id") %>">
                    <input
                            type="text"
                            name="comment"
                            placeholder="Write a comment..."
                            class="w-full rounded-md py-1 px-3"
                    />
                    <button type="submit" class="py-1 px-3 bg-cyan-500 text-white rounded-md">
                        ➤
                    </button>
                </form>
            </div>
            <div class="flex flex-col pl-16">
                <%
                    for (Map<String, Object> comment : comments) {
                        String commentUsername = (String) comment.get("comment_username");
                        String commentText = (String) comment.get("comment_text");
                %>
                <div class="flex flex-col gap-3 items-start p-2">
                    <div class="flex flex-row justify-start items-end gap-2">
                        <img
                                src="https://robohash.org/<%= commentUsername %>"
                                alt="profile_picture"
                                class="rounded-full h-8 bg-gray-400"
                        />
                        <h4 class="text-black dark:text-white"><%= commentUsername %>
                        </h4>
                    </div>
                    <p class="w-full text-gray-600 bg-gray-200 p-2 rounded-lg">
                        <%= commentText %>
                    </p>
                </div>
                <% } %>
            </div>
        </div>
        <%
                }
            }
        %>
    </div>
</div>
<!-- Discover Latest Posts - Left Column - End -->
<!-- Post something new - Right Column - Start -->
<div class="sticky top-0 w-full">
    <h2 class="mt-3 mb-6 self-start text-3xl font-semibold text-black dark:text-white">
        Post something new!
    </h2>
    <div class="w-full flex flex-col gap-3">
        <div class="flex flex-col gap-3 p-4 rounded-md bg-white shadow dark:border dark:bg-gray-800 dark:border-gray-700">
            <form action="auth" method="post">
                <input type="hidden" name="action" value="createPost">
                <div class="flex flex-row justify-start items-end w-full">
                    <img src='https://robohash.org/<%= session.getAttribute("username") %>' alt="profile_picture"
                         class="rounded-full h-12 bg-gray-400"/>
                    <h3 class="ml-3 text-lg text-black dark:text-white"><%= session.getAttribute("username") %>
                    </h3>
                </div>
                <div class="mt-3">
                    <textarea name="description" id="post_content" cols="30" rows="10"
                              class="w-full rounded-md p-2 text-gray-600"
                              placeholder="Write something..."></textarea>
                    <button type="submit" class="w-full py-1 px-3 bg-cyan-500 text-white rounded-md font-semibold mt-3">
                        Post
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
<!-- Post something new - Right Column - End -->
</body>
</html>
