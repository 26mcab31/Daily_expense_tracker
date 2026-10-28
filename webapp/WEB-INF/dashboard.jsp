<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.math.BigDecimal,java.text.NumberFormat,java.util.List,java.util.Locale,model.Expense" %>
<%!
    private String escapeHtml(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String formatRupees(Object value) {
        BigDecimal amount = value instanceof BigDecimal
                ? (BigDecimal) value : BigDecimal.ZERO;
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN"))
                .format(amount);
    }
%>
<%
    String contextPath = request.getContextPath();
    String username = (String) session.getAttribute("username");
    if (username == null) {
        username = "User";
    }
    String initial = username.isEmpty()
            ? "U" : username.substring(0, 1).toUpperCase(Locale.ROOT);
    BigDecimal expenseTotal = (BigDecimal) request.getAttribute("expenseTotal");
    BigDecimal todayTotal = (BigDecimal) request.getAttribute("todayTotal");
    Integer transactionCount = (Integer) request.getAttribute("transactionCount");
    Boolean dashboardLoadError =
            (Boolean) request.getAttribute("dashboardLoadError");
    List<Expense> recentExpenses =
            (List<Expense>) request.getAttribute("recentExpenses");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard | Daily Expense Tracker</title>
    <style>
        *{box-sizing:border-box;margin:0;padding:0}
        body{font-family:Arial,sans-serif;background:#f5f7fb;color:#1f2937}
        .sidebar{position:fixed;left:0;top:0;width:240px;height:100vh;background:linear-gradient(180deg,#4f46e5,#7c3aed);color:white;padding:30px 20px}
        .logo{text-align:center;margin-bottom:45px}
        .logo-icon{font-size:38px;margin-bottom:8px}
        .logo h2{font-size:20px}
        .logo p{font-size:12px;opacity:.8;margin-top:5px}
        .menu{display:flex;flex-direction:column;gap:12px}
        .menu a,.logout button{display:block;width:100%;padding:14px;border:0;border-radius:10px;background:transparent;color:white;text-align:left;text-decoration:none;font-size:15px;cursor:pointer}
        .menu a:hover,.menu .active{background:rgba(255,255,255,.2)}
        .logout{position:absolute;bottom:30px;left:20px;right:20px}
        .logout button{border:1px solid rgba(255,255,255,.5)}
        .logout button:hover{background:white;color:#4f46e5}
        .main{margin-left:240px;padding:35px}
        .topbar{display:flex;justify-content:space-between;align-items:center;margin-bottom:35px;gap:20px}
        .welcome h1{font-size:30px;margin-bottom:8px}
        .welcome p,.card-title,.action-card p{color:#6b7280}
        .profile{display:flex;align-items:center;gap:12px;background:white;padding:10px 16px;border-radius:30px;box-shadow:0 3px 12px #0001}
        .profile-icon{width:38px;height:38px;border-radius:50%;background:#4f46e5;color:white;display:flex;align-items:center;justify-content:center;font-weight:bold}
        .cards{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:22px;margin-bottom:30px}
        .card,.action-card,.recent{background:white;padding:25px;border-radius:16px;box-shadow:0 4px 15px #0000000f}
        .card-icon{float:right;font-size:28px}
        .card-title{font-size:14px;margin-bottom:12px}
        .card-value{font-size:28px;font-weight:bold;margin-bottom:8px;overflow-wrap:anywhere}
        .section-title{margin-bottom:18px;font-size:20px}
        .actions{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:22px}
        .action-card{transition:.2s}
        .action-card:hover{transform:translateY(-3px)}
        .action-icon{font-size:35px;margin-bottom:15px}
        .action-card h3{margin-bottom:8px}
        .action-card p{font-size:14px;margin-bottom:18px}
        .action-card a{display:inline-block;padding:10px 18px;border-radius:8px;background:#4f46e5;color:white;text-decoration:none}
        .action-card a:hover{background:#4338ca}
        .recent{margin-top:30px}
        .recent h2{margin-bottom:18px;font-size:20px}
        .activity{display:flex;justify-content:space-between;align-items:center;gap:12px;padding:15px 0;border-bottom:1px solid #eee}
        .activity:last-child{border-bottom:none}
        .activity-left{display:flex;gap:12px;align-items:center;min-width:0}
        .activity-icon{width:40px;height:40px;flex-shrink:0;border-radius:10px;background:#eef2ff;display:flex;align-items:center;justify-content:center}
        .activity p{font-size:14px;overflow-wrap:anywhere}
        .activity small{color:#9ca3af}
        .amount{font-weight:bold;white-space:nowrap}
        .status{margin:0 0 20px;padding:12px 15px;background:#eef2ff;border-radius:8px;color:#4338ca;font-size:14px}
        .error{background:#fef2f2;color:#b91c1c}
        .empty{padding:15px 0;color:#6b7280}
        @media(max-width:900px){.sidebar{width:200px}.main{margin-left:200px;padding:25px}.cards{grid-template-columns:1fr}}
        @media(max-width:650px){.sidebar{position:relative;width:100%;height:auto}.logo{margin-bottom:20px}.logout{position:relative;left:0;right:0;bottom:auto;margin-top:20px}.main{margin-left:0;padding:20px}.topbar{flex-direction:column;align-items:flex-start}.actions{grid-template-columns:1fr}}
    </style>
</head>
<body>
<aside class="sidebar">
    <div class="logo">
        <div class="logo-icon">💰</div>
        <h2>Daily Expense</h2>
        <p>TRACKER</p>
    </div>
    <nav class="menu" aria-label="Main navigation">
        <a class="active" href="<%= contextPath %>/dashboard">🏠 &nbsp; Dashboard</a>
        <a href="<%= contextPath %>/add-expense.html">➕ &nbsp; Add Expense</a>
        <a href="<%= contextPath %>/view-expenses.html">📋 &nbsp; View Expenses</a>
    </nav>
    <form class="logout" action="<%= contextPath %>/logout" method="post">
        <button type="submit">🚪 &nbsp; Logout</button>
    </form>
</aside>

<main class="main">
    <div class="topbar">
        <div class="welcome">
            <h1>Welcome back, <%= escapeHtml(username) %> 👋</h1>
            <p>Here's an overview of your daily expenses.</p>
        </div>
        <div class="profile">
            <div class="profile-icon"><%= escapeHtml(initial) %></div>
            <span><%= escapeHtml(username) %></span>
        </div>
    </div>

    <% if (Boolean.TRUE.equals(dashboardLoadError)) { %>
        <div class="status error" role="alert">
            Dashboard data could not be loaded. Please try again later.
        </div>
    <% } %>

    <div class="cards">
        <div class="card">
            <div class="card-icon">💰</div>
            <div class="card-title">Total Expenses</div>
            <div class="card-value"><%= Boolean.TRUE.equals(dashboardLoadError)
                    ? "Unavailable" : formatRupees(expenseTotal) %></div>
            <div class="card-small">Your recorded expenses</div>
        </div>
        <div class="card">
            <div class="card-icon">🧾</div>
            <div class="card-title">Total Transactions</div>
            <div class="card-value"><%= Boolean.TRUE.equals(dashboardLoadError)
                    ? "Unavailable"
                    : (transactionCount == null ? 0 : transactionCount) %></div>
            <div class="card-small">Expense records</div>
        </div>
        <div class="card">
            <div class="card-icon">📅</div>
            <div class="card-title">Today's Spending</div>
            <div class="card-value"><%= Boolean.TRUE.equals(dashboardLoadError)
                    ? "Unavailable" : formatRupees(todayTotal) %></div>
            <div class="card-small">Expenses dated today</div>
        </div>
    </div>

    <h2 class="section-title">Quick Actions</h2>
    <div class="actions">
        <div class="action-card">
            <div class="action-icon">➕</div>
            <h3>Add New Expense</h3>
            <p>Record your food, travel, shopping and other daily expenses.</p>
            <a href="<%= contextPath %>/add-expense.html">Add Expense</a>
        </div>
        <div class="action-card">
            <div class="action-icon">📊</div>
            <h3>View Expenses</h3>
            <p>View your recorded expenses in one place.</p>
            <a href="<%= contextPath %>/view-expenses.html">View Expenses</a>
        </div>
    </div>

    <section class="recent">
        <h2>Recent Expenses</h2>
        <% if (Boolean.TRUE.equals(dashboardLoadError)) { %>
            <p class="empty">Recent expenses could not be loaded.</p>
        <% } else if (recentExpenses == null || recentExpenses.isEmpty()) { %>
            <p class="empty">No expenses recorded for this account yet.</p>
        <% } else { %>
            <% for (Expense expense : recentExpenses) { %>
                <div class="activity">
                    <div class="activity-left">
                        <div class="activity-icon">💰</div>
                        <div>
                            <p><strong><%= escapeHtml(expense.getDescription()) %></strong></p>
                            <small><%= escapeHtml(expense.getCategory()) %> ·
                                <%= escapeHtml(expense.getExpenseDate()) %></small>
                        </div>
                    </div>
                    <div class="amount"><%= formatRupees(
                            BigDecimal.valueOf(expense.getAmount())) %></div>
                </div>
            <% } %>
        <% } %>
    </section>
</main>
</body>
</html>
