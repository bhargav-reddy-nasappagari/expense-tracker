<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<footer class="main-footer">
    <div class="container">
        <p>&copy; <span id="year"></span> ExpenseTracker. All rights reserved.</p>
        <p class="text-muted">Your personal finance. Your rules. Your masterpiece.</p>
    </div>
</footer>

<script>
    document.getElementById("year").textContent = new Date().getFullYear();
</script>

</body>
</html>