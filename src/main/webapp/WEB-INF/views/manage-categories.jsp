<%
    // Prevent caching
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    response.setHeader("Pragma", "no-cache");
    response.setHeader("Expires", "0");
    
    // Session validation
    if (session == null || session.getAttribute("user") == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Categories • ExpenseTracker</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

<jsp:include page="/WEB-INF/views/layout/header.jsp"/>

<div class="main-content">
    <div class="container" style="max-width: 800px;">

        <jsp:include page="/WEB-INF/views/layout/message.jsp"/>

        <div class="card mb-5">
            <div class="card-header">
                <h2>Manage Categories</h2>
            </div>

            <div class="card-body p-5">

                <!-- Add New Category -->
                <div class="mb-5">
                    <h4>Add New Category</h4>
                    <form action="category" method="post" class="row g-4 align-items-end">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}" />
                        <input type="hidden" name="action" value="add">
                        <div class="col-md-8">
                            <input type="text" name="name" class="form-control" 
                                   placeholder="e.g., Subscriptions" required maxlength="50">
                        </div>
                        <div class="col-md-4">
                            <button type="submit" class="btn-primary w-100">Add Category</button>
                        </div>
                    </form>
                </div>

                <!-- Existing Categories -->
                <h4 class="mb-4">Your Categories</h4>
                <c:choose>
                    <c:when test="${empty categories}">
                        <p class="text-muted">No custom categories yet.</p>
                    </c:when>
                    <c:otherwise>
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Type</th>
                                        <th class="text-center">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="cat" items="${categories}">
                                        <tr>
                                           <td>
                                                <!-- Normal name display -->
                                                <span id="name-display-${cat.id}">
                                                    <strong>${cat.name}</strong>
                                                </span>

                                                <!-- Hidden rename input box -->
                                                <div id="rename-box-${cat.id}" style="display:none;">
                                                    <input type="text"
                                                        id="rename-input-${cat.id}"
                                                        class="form-control d-inline-block"
                                                        style="width: 60%;"
                                                        value="${cat.name}" />

                                                    <button class="btn-primary btn-sm"
                                                            onclick="submitRename('${cat.id}')">Save</button>

                                                    <button class="btn-secondary btn-sm"
                                                            onclick="cancelRename('${cat.id}')">Cancel</button>
                                                    <!-- Inline error message -->
                                                    <p id="rename-error-${cat.id}" 
                                                    style="color: red; margin-top: 4px; font-size: 0.85rem; display:none;"></p>
                                                </div>
                                            </td>

                                            <td>
                                                <c:choose>
                                                    <c:when test="${cat.defaultCategory}">Default (protected)</c:when>
                                                    <c:otherwise>Custom</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center">
                                                <c:if test="${!cat.defaultCategory}">
                                                    <!-- Action triggers open the shared action panel.
                                                         We store id/name on data-* attributes and pass the element (safer for escaping) -->
                                                    <a href="#" class="text-primary me-3 action-open"
                                                       data-id="${cat.id}"
                                                       data-name="${cat.name}"
                                                       onclick="openActionPanel(this); return false;">Rename / Delete</a>
                                                </c:if>
                                            </td>

                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                        <!-- Shared Action Panel (Option C) -->
                        <div id="category-action-panel" class="action-panel" style="display:none;">
                            <div style="display:flex; justify-content:space-between; align-items:center; gap:1rem;">
                                <div>
                                    <div class="small-muted">Selected category:</div>
                                    <div id="panel-category-name" style="font-weight:700; color:var(--deep-space-blue); font-size:1.05rem;">&nbsp;</div>
                                    <div id="panel-category-type" class="muted-note">&nbsp;</div>
                                </div>

                                <div style="text-align:right;">
                                    <button class="btn-back" onclick="closeActionPanel()">Close</button>
                                </div>
                            </div>

                            <div style="margin-top:0.9rem;">
                                <div class="action-tabs">
                                    <div id="tab-rename" class="action-tab active" onclick="switchTab('rename')">Rename</div>
                                    <div id="tab-delete" class="action-tab" onclick="switchTab('delete')">Delete</div>
                                </div>

                                <!-- Rename Section -->
                                <div id="section-rename" class="action-section active">
                                    <div class="rename-box" style="padding:0.8rem;">
                                        <input type="text" id="panel-rename-input" class="rename-input" value="" />
                                        <div class="action-footer">
                                            <button class="btn-primary btn-sm" onclick="panelSubmitRename()">Save</button>
                                            <button class="btn-secondary btn-sm" onclick="closeActionPanel()">Cancel</button>
                                            <div style="flex:1"></div>
                                            <div id="panel-rename-success" class="rename-success" style="display:none;"></div>
                                        </div>
                                        <p id="panel-rename-error" class="rename-error" style="display:none; margin-top:6px;"></p>
                                        <p class="muted-note">Change the name and click <strong>Save</strong>. The name must be unique.</p>
                                    </div>
                                </div>

                                <!-- Delete Section -->
                                <div id="section-delete" class="action-section" style="margin-top:0.5rem;">
                                    <div class="delete-box" style="padding:0.9rem;">
                                        <p>Are you sure you want to delete the category <strong id="panel-delete-name"></strong>?</p>
                                        <div class="action-footer">
                                            <button class="btn-danger btn-sm" onclick="panelSubmitDelete()">Yes, Delete</button>
                                            <button class="btn-secondary btn-sm" onclick="switchTab('rename')">Cancel</button>
                                            <div style="flex:1"></div>
                                            <div id="panel-delete-success" class="rename-success" style="display:none;"></div>
                                        </div>
                                        <p id="panel-delete-error" class="delete-error" style="display:none; margin-top:6px;"></p>
                                        <p class="muted-note">You cannot delete default categories or categories used by expenses.</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="text-center mt-5">
            <p class="tagline fs-4">Your personal finance. Your rules. Your masterpiece.</p>
        </div>
    </div>
</div>

<jsp:include page="/WEB-INF/views/layout/footer.jsp"/>

<!-- BULLETPROOF JavaScript – NO QUOTES, NO ERRORS, EVER -->
<script>
function renameCategory(link) {
    const id = link.getAttribute('data-id');
    const currentName = link.getAttribute('data-name');
    
    const newName = prompt("New category name:", currentName);
    if (newName && newName.trim() !== "" && newName.trim() !== currentName) {
        const form = document.createElement("form");
        form.method = "POST";
        form.action = "category";

        // Safe input creation – no string concatenation
        const inputs = [
            { name: "action", value: "rename" },
            { name: "categoryId", value: id },
            { name: "newName", value: newName.trim() },
            //CSRF implementation
            { name: "csrfToken", value: "${sessionScope.csrfToken}" }
        ];

        inputs.forEach(function(item) {
            const input = document.createElement("input");
            input.type = "hidden";
            input.name = item.name;
            input.value = item.value;
            form.appendChild(input);
        });

        document.body.appendChild(form);
        form.submit();
    }
}

(function(){

    // currently selected category id + name (strings)
    let selectedCategoryId = null;
    let selectedCategoryName = null;

    // Open the shared panel and populate fields
    window.openActionPanel = function(el) {
        const id = el.dataset.id;
        const name = el.dataset.name;

        selectedCategoryId = id;
        selectedCategoryName = name;

        document.getElementById('panel-category-name').textContent = name;
        document.getElementById('panel-category-type').textContent = ''; // optionally show extra info if you want
        document.getElementById('panel-rename-input').value = name;
        document.getElementById('panel-delete-name').textContent = '"' + name + '"';

        // reset messages
        resetPanelMessages();

        // show panel
        document.getElementById('category-action-panel').style.display = 'block';
        // ensure rename tab is active by default
        switchTab('rename');

        // scroll into view a bit smoothly
        document.getElementById('category-action-panel').scrollIntoView({ behavior: 'smooth', block: 'center' });
    };

    window.closeActionPanel = function() {
        document.getElementById('category-action-panel').style.display = 'none';
        selectedCategoryId = null;
        selectedCategoryName = null;
    };

    // show either rename or delete section
    window.switchTab = function(tab) {
        const renameTab = document.getElementById('tab-rename');
        const deleteTab = document.getElementById('tab-delete');
        const sectionRename = document.getElementById('section-rename');
        const sectionDelete = document.getElementById('section-delete');

        if (tab === 'rename') {
            renameTab.classList.add('active');
            deleteTab.classList.remove('active');
            sectionRename.classList.add('active');
            sectionDelete.classList.remove('active');
        } else {
            renameTab.classList.remove('active');
            deleteTab.classList.add('active');
            sectionRename.classList.remove('active');
            sectionDelete.classList.add('active');
        }

        // reset messages when switching
        resetPanelMessages();
    };

    function resetPanelMessages() {
        // rename
        const rErr = document.getElementById('panel-rename-error');
        const rSuc = document.getElementById('panel-rename-success');
        rErr.style.display = 'none'; rErr.textContent = '';
        rErr.style.opacity = 0;
        rSuc.style.display = 'none'; rSuc.textContent = '';
        rSuc.style.opacity = 0;

        // delete
        const dErr = document.getElementById('panel-delete-error');
        const dSuc = document.getElementById('panel-delete-success');
        dErr.style.display = 'none'; dErr.textContent = '';
        dErr.style.opacity = 0;
        dSuc.style.display = 'none'; dSuc.textContent = '';
        dSuc.style.opacity = 0;

        // input style
        document.getElementById('panel-rename-input').classList.remove('error');
    }

    function showPanelError(elId, msg) {
        const el = document.getElementById(elId);
        el.textContent = msg;
        el.style.display = 'block';
        setTimeout(function(){ el.style.opacity = 1; }, 10);
    }

    function showPanelSuccess(elId, msg) {
        const el = document.getElementById(elId);
        el.textContent = msg;
        el.style.display = 'block';
        setTimeout(function(){ el.style.opacity = 1; }, 10);
    }

    // Submit rename via POST to /category (action=rename)
    window.panelSubmitRename = function() {
        const input = document.getElementById('panel-rename-input');
        const newName = input.value.trim();

        // reset messages
        resetPanelMessages();

        if (!selectedCategoryId) {
            showPanelError('panel-rename-error', 'No category selected.');
            return;
        }

        if (!newName) {
            input.classList.add('error');
            showPanelError('panel-rename-error', 'Category name cannot be empty.');
            return;
        }

        if (newName === selectedCategoryName) {
            showPanelError('panel-rename-error', 'The new name is the same as the current name.');
            return;
        }

        // friendly UX: show saving
        showPanelSuccess('panel-rename-success', 'Saving changes...');

        // create and submit form
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = 'category';

        const fields = {
            action: 'rename',
            categoryId: selectedCategoryId,
            newName: newName,
            csrfToken: "${sessionScope.csrfToken}"
        };

        for (const k in fields) {
            const i = document.createElement('input');
            i.type = 'hidden';
            i.name = k;
            i.value = fields[k];
            form.appendChild(i);
        }

        document.body.appendChild(form);

        // give the success message a moment to show
        setTimeout(function(){ form.submit(); }, 250);
    };

    // Submit delete via POST to /category (action=delete)
    window.panelSubmitDelete = function() {
        // reset messages
        resetPanelMessages();

        if (!selectedCategoryId) {
            showPanelError('panel-delete-error', 'No category selected.');
            return;
        }

        // friendly UX
        showPanelSuccess('panel-delete-success', 'Deleting...');

        const form = document.createElement('form');
        form.method = 'POST';
        form.action = 'category';

        const fields = {
            action: 'delete',
            categoryId: selectedCategoryId,
            csrfToken: "${sessionScope.csrfToken}"
        };

        for (const k in fields) {
            const i = document.createElement('input');
            i.type = 'hidden';
            i.name = k;
            i.value = fields[k];
            form.appendChild(i);
        }

        document.body.appendChild(form);

        // submit after a short delay for UX
        setTimeout(function(){ form.submit(); }, 250);
    };

    // Close panel if user presses Escape
    document.addEventListener('keydown', function(e){
        if (e.key === 'Escape') {
            const panel = document.getElementById('category-action-panel');
            if (panel && panel.style.display !== 'none') closeActionPanel();
        }
    });

})();
</script>

</body>
</html>