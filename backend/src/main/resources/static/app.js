const API = "/api";
const dayNames = ["Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"];
const mealTypes = [
    { key: "LUNCH", label: "Dejeuner" },
    { key: "DINNER", label: "Diner" }
];
const shoppingCategories = [
    "Fruits et legumes",
    "Frais",
    "Viandes et poissons",
    "Epicerie salee",
    "Epicerie sucree",
    "Surgeles",
    "Boissons",
    "Hygiene et entretien"
];

const state = {
    view: "dashboard",
    recipes: [],
    ingredients: [],
    mealPlan: null,
    shoppingList: null,
    weekStart: startOfWeek(new Date()),
    editingRecipe: null,
    selectedRecipeId: null,
    cookStepIndex: 0
};

const views = {
    dashboard: document.querySelector("#dashboard-view"),
    recipes: document.querySelector("#recipes-view"),
    planner: document.querySelector("#planner-view"),
    shopping: document.querySelector("#shopping-view"),
    cook: document.querySelector("#cook-view")
};

document.addEventListener("DOMContentLoaded", async () => {
    bindNavigation();
    bindRecipeDialog();
    await refreshAll();
});

function bindNavigation() {
    document.querySelectorAll(".nav-item").forEach(button => {
        button.addEventListener("click", () => setView(button.dataset.view));
    });
    document.querySelector("#previous-week").addEventListener("click", async () => {
        state.weekStart.setDate(state.weekStart.getDate() - 7);
        await refreshPlanningData();
        render();
    });
    document.querySelector("#next-week").addEventListener("click", async () => {
        state.weekStart.setDate(state.weekStart.getDate() + 7);
        await refreshPlanningData();
        render();
    });
    document.querySelector("#today-week").addEventListener("click", async () => {
        state.weekStart = startOfWeek(new Date());
        await refreshPlanningData();
        render();
    });
    document.querySelector("#new-recipe").addEventListener("click", () => openRecipeDialog());
}

function bindRecipeDialog() {
    document.querySelector("#close-recipe-dialog").addEventListener("click", closeRecipeDialog);
    document.querySelector("#cancel-recipe").addEventListener("click", closeRecipeDialog);
    document.querySelector("#add-ingredient-row").addEventListener("click", () => addIngredientRow());
    document.querySelector("#add-step-row").addEventListener("click", () => addStepRow());
    document.querySelector("#recipe-form").addEventListener("submit", saveRecipe);
}

async function refreshAll() {
    try {
        const [recipes, ingredients] = await Promise.all([
            api("/recipes"),
            api("/ingredients")
        ]);
        state.recipes = recipes;
        state.ingredients = ingredients;
        state.selectedRecipeId = state.selectedRecipeId || recipes[0]?.id || null;
        await refreshPlanningData();
        render();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function refreshPlanningData() {
    const week = isoDate(state.weekStart);
    const [mealPlan, shoppingList] = await Promise.all([
        api(`/meal-plans/${week}`),
        api(`/shopping-lists/${week}`)
    ]);
    state.mealPlan = mealPlan;
    state.shoppingList = shoppingList;
}

async function api(path, options = {}) {
    const response = await fetch(`${API}${path}`, {
        headers: {
            "Content-Type": "application/json",
            ...(options.headers || {})
        },
        ...options
    });
    if (response.status === 204) {
        return null;
    }
    const body = await response.json().catch(() => ({}));
    if (!response.ok) {
        const details = body.details?.length ? ` (${body.details.join(", ")})` : "";
        throw new Error(`${body.message || "Erreur API"}${details}`);
    }
    return body;
}

function render() {
    document.querySelector("#current-week-label").textContent = formatDate(state.weekStart);
    document.querySelector("#view-title").textContent = viewTitle(state.view);
    document.querySelectorAll(".nav-item").forEach(button => button.classList.toggle("active", button.dataset.view === state.view));
    Object.entries(views).forEach(([name, element]) => element.classList.toggle("active", name === state.view));
    renderDashboard();
    renderRecipes();
    renderPlanner();
    renderShopping();
    renderCook();
}

function setView(view) {
    state.view = view;
    render();
}

function viewTitle(view) {
    return {
        dashboard: "Tableau de bord",
        recipes: "Recettes",
        planner: "Planning hebdomadaire",
        shopping: "Liste de courses",
        cook: "Mode cuisine"
    }[view];
}

function renderDashboard() {
    const plannedCount = state.mealPlan?.items?.length || 0;
    const shoppingCount = Object.values(state.shoppingList?.categories || {}).flat().filter(item => !item.excluded).length;
    const nextMeals = [...(state.mealPlan?.items || [])]
        .sort((a, b) => `${a.date}${a.mealType}`.localeCompare(`${b.date}${b.mealType}`))
        .slice(0, 5);

    views.dashboard.innerHTML = `
        <div class="metrics">
            <div class="metric"><strong>${state.recipes.length}</strong><span>recettes</span></div>
            <div class="metric"><strong>${plannedCount}</strong><span>repas planifies</span></div>
            <div class="metric"><strong>${shoppingCount}</strong><span>produits a acheter</span></div>
        </div>
        <div class="dashboard-grid">
            <div class="panel">
                <div class="panel-header">
                    <h2>Planning de la semaine</h2>
                    <button class="soft-button" data-go="planner">Modifier</button>
                </div>
                ${renderMiniPlanning()}
            </div>
            <div class="panel">
                <div class="panel-header">
                    <h2>Prochains repas</h2>
                    <button class="soft-button" data-go="shopping">Courses</button>
                </div>
                ${nextMeals.length ? `<ul class="step-list">${nextMeals.map(item => `<li><strong>${dayLabel(item.date)}</strong> ${mealLabel(item.mealType)} · ${escapeHtml(item.recipeName)}</li>`).join("")}</ul>` : `<p class="empty">Aucun repas planifie.</p>`}
            </div>
        </div>
    `;
    views.dashboard.querySelectorAll("[data-go]").forEach(button => button.addEventListener("click", () => setView(button.dataset.go)));
}

function renderMiniPlanning() {
    return `<div class="planner-grid mini-planner">${weekDates().map(date => {
        const items = (state.mealPlan?.items || []).filter(item => item.date === isoDate(date));
        return `
            <div class="planner-cell">
                <div class="planner-day">${dayLabel(isoDate(date))}</div>
                ${mealTypes.map(type => {
                    const item = items.find(candidate => candidate.mealType === type.key);
                    return `<p class="muted"><strong>${type.label}</strong><br>${item ? escapeHtml(item.recipeName) : "A definir"}</p>`;
                }).join("")}
            </div>
        `;
    }).join("")}</div>`;
}

function renderRecipes() {
    const categories = unique(state.recipes.map(recipe => recipe.category).filter(Boolean));
    const tags = unique(state.recipes.flatMap(recipe => [...(recipe.tags || [])]));
    views.recipes.innerHTML = `
        <div class="toolbar">
            <input id="recipe-search" placeholder="Rechercher une recette">
            <select id="category-filter"><option value="">Toutes categories</option>${categories.map(category => `<option>${escapeHtml(category)}</option>`).join("")}</select>
            <select id="tag-filter"><option value="">Tous tags</option>${tags.map(tag => `<option>${escapeHtml(tag)}</option>`).join("")}</select>
            <button class="primary-button" id="recipe-add-inline">+ Recette</button>
        </div>
        <div id="recipe-results" class="recipe-grid"></div>
    `;
    const search = views.recipes.querySelector("#recipe-search");
    const category = views.recipes.querySelector("#category-filter");
    const tag = views.recipes.querySelector("#tag-filter");
    const update = () => renderRecipeResults(search.value, category.value, tag.value);
    [search, category, tag].forEach(input => input.addEventListener("input", update));
    views.recipes.querySelector("#recipe-add-inline").addEventListener("click", () => openRecipeDialog());
    update();
}

function renderRecipeResults(query = "", category = "", tag = "") {
    const normalizedQuery = query.trim().toLowerCase();
    const recipes = state.recipes.filter(recipe => {
        const matchesQuery = !normalizedQuery || recipe.name.toLowerCase().includes(normalizedQuery);
        const matchesCategory = !category || recipe.category === category;
        const matchesTag = !tag || (recipe.tags || []).includes(tag);
        return matchesQuery && matchesCategory && matchesTag;
    });
    const container = views.recipes.querySelector("#recipe-results");
    container.innerHTML = recipes.length ? recipes.map(recipeCard).join("") : `<p class="empty">Aucune recette ne correspond aux filtres.</p>`;
    container.querySelectorAll("[data-cook]").forEach(button => button.addEventListener("click", () => startCooking(Number(button.dataset.cook))));
    container.querySelectorAll("[data-edit]").forEach(button => button.addEventListener("click", () => openRecipeDialog(state.recipes.find(recipe => recipe.id === Number(button.dataset.edit)))));
    container.querySelectorAll("[data-delete]").forEach(button => button.addEventListener("click", () => deleteRecipe(Number(button.dataset.delete))));
}

function recipeCard(recipe) {
    const style = recipe.imageUrl ? `style="background-image: url('${escapeAttribute(recipe.imageUrl)}')"` : "";
    return `
        <article class="recipe-card">
            <div class="recipe-image" ${style}></div>
            <div class="recipe-body">
                <div>
                    <h3>${escapeHtml(recipe.name)}</h3>
                    <p class="recipe-meta">${recipe.servings} portions · ${recipe.totalTimeMinutes} min · ${escapeHtml(recipe.category || "Sans categorie")}</p>
                </div>
                <p>${escapeHtml(recipe.description || "Aucune description.")}</p>
                <div class="tags">${(recipe.tags || []).map(tag => `<span class="tag">${escapeHtml(tag)}</span>`).join("")}</div>
                <div class="button-row">
                    <button class="primary-button" data-cook="${recipe.id}">Cuisiner</button>
                    <button class="soft-button" data-edit="${recipe.id}">Modifier</button>
                    <button class="danger-button" data-delete="${recipe.id}">Supprimer</button>
                </div>
            </div>
        </article>
    `;
}

function renderPlanner() {
    views.planner.innerHTML = `
        <div class="planner-grid">
            ${weekDates().map(date => `
                <div class="planner-cell">
                    <div class="planner-day">${dayLabel(isoDate(date))}</div>
                    ${mealTypes.map(type => renderPlannerSlot(date, type)).join("")}
                </div>
            `).join("")}
        </div>
    `;
    views.planner.querySelectorAll("select[data-date]").forEach(select => {
        select.addEventListener("change", () => savePlanItem(select.dataset.date, select.dataset.mealType, select.value));
    });
    views.planner.querySelectorAll("[data-remove-plan]").forEach(button => {
        button.addEventListener("click", () => removePlanItem(Number(button.dataset.removePlan)));
    });
}

function renderPlannerSlot(date, type) {
    const dateIso = isoDate(date);
    const item = (state.mealPlan?.items || []).find(candidate => candidate.date === dateIso && candidate.mealType === type.key);
    return `
        <div class="slot">
            <div class="slot-label">
                <span>${type.label}</span>
                ${item ? `<button class="icon-button" data-remove-plan="${item.id}" title="Retirer">×</button>` : ""}
            </div>
            <select data-date="${dateIso}" data-meal-type="${type.key}">
                <option value="">A definir</option>
                ${state.recipes.map(recipe => `<option value="${recipe.id}" ${item?.recipeId === recipe.id ? "selected" : ""}>${escapeHtml(recipe.name)}</option>`).join("")}
            </select>
        </div>
    `;
}

async function savePlanItem(date, mealType, recipeId) {
    if (!recipeId) {
        return;
    }
    try {
        state.mealPlan = await api(`/meal-plans/${isoDate(state.weekStart)}/items`, {
            method: "POST",
            body: JSON.stringify({ date, mealType, recipeId: Number(recipeId) })
        });
        state.shoppingList = await api(`/shopping-lists/${isoDate(state.weekStart)}`);
        showMessage("Planning mis a jour.");
        render();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function removePlanItem(itemId) {
    try {
        state.mealPlan = await api(`/meal-plans/${isoDate(state.weekStart)}/items/${itemId}`, { method: "DELETE" });
        state.shoppingList = await api(`/shopping-lists/${isoDate(state.weekStart)}`);
        showMessage("Repas retire du planning.");
        render();
    } catch (error) {
        showMessage(error.message, true);
    }
}

function renderShopping() {
    const categories = state.shoppingList?.categories || {};
    const categoryNames = Object.keys(categories);
    views.shopping.innerHTML = categoryNames.length ? `
        <div class="shopping-layout">
            ${categoryNames.map(category => `
                <section class="shopping-category">
                    <h2>${escapeHtml(category)}</h2>
                    <div class="shopping-items">
                        ${categories[category].map(shoppingItem).join("")}
                    </div>
                </section>
            `).join("")}
        </div>
    ` : `<p class="empty">Ajoutez des recettes au planning pour generer la liste de courses.</p>`;
    views.shopping.querySelectorAll("input[data-shopping-id]").forEach(input => {
        input.addEventListener("change", () => updateShoppingItem(Number(input.dataset.shoppingId)));
    });
}

function shoppingItem(item) {
    return `
        <div class="shopping-item ${item.checked ? "checked" : ""} ${item.excluded ? "excluded" : ""}" data-item="${item.id}">
            <div>
                <strong>${formatQuantity(item.quantity)} ${escapeHtml(item.unit)} ${escapeHtml(item.ingredientName)}</strong>
                <p class="muted">${escapeHtml(item.shoppingCategory)}</p>
            </div>
            <div class="check-pair">
                <label><input type="checkbox" data-shopping-id="${item.id}" data-field="excluded" ${item.excluded ? "checked" : ""}> Maison</label>
                <label><input type="checkbox" data-shopping-id="${item.id}" data-field="checked" ${item.checked ? "checked" : ""}> Achete</label>
            </div>
        </div>
    `;
}

async function updateShoppingItem(itemId) {
    const row = views.shopping.querySelector(`[data-item="${itemId}"]`);
    const checked = row.querySelector('[data-field="checked"]').checked;
    const excluded = row.querySelector('[data-field="excluded"]').checked;
    try {
        state.shoppingList = await api(`/shopping-lists/${isoDate(state.weekStart)}/items/${itemId}`, {
            method: "PATCH",
            body: JSON.stringify({ checked, excluded })
        });
        renderShopping();
    } catch (error) {
        showMessage(error.message, true);
    }
}

function renderCook() {
    const selected = state.recipes.find(recipe => recipe.id === state.selectedRecipeId) || state.recipes[0];
    if (!selected) {
        views.cook.innerHTML = `<p class="empty">Ajoutez une recette pour utiliser le mode cuisine.</p>`;
        return;
    }
    state.selectedRecipeId = selected.id;
    state.cookStepIndex = Math.min(state.cookStepIndex, selected.steps.length - 1);
    const step = selected.steps[state.cookStepIndex];
    views.cook.innerHTML = `
        <div class="cook-layout">
            <aside class="panel">
                <label>Recette
                    <select id="cook-recipe-select">
                        ${state.recipes.map(recipe => `<option value="${recipe.id}" ${recipe.id === selected.id ? "selected" : ""}>${escapeHtml(recipe.name)}</option>`).join("")}
                    </select>
                </label>
                <p class="recipe-meta">${selected.servings} portions · preparation ${selected.preparationTimeMinutes} min · cuisson ${selected.cookingTimeMinutes} min</p>
                <h3>Ingredients</h3>
                <ul class="ingredient-list">
                    ${selected.ingredients.map(ingredient => `<li>${formatQuantity(ingredient.quantity)} ${escapeHtml(ingredient.unit)} ${escapeHtml(ingredient.ingredientName)}</li>`).join("")}
                </ul>
            </aside>
            <section class="cook-step">
                <p class="cook-step-number">Etape ${state.cookStepIndex + 1} / ${selected.steps.length}</p>
                <p class="cook-step-text">${escapeHtml(step.description)}</p>
                <div class="button-row">
                    <button class="soft-button" id="previous-step" ${state.cookStepIndex === 0 ? "disabled" : ""}>Precedente</button>
                    <button class="primary-button" id="next-step" ${state.cookStepIndex === selected.steps.length - 1 ? "disabled" : ""}>Suivante</button>
                </div>
                <ul class="step-list">
                    ${selected.steps.map(candidate => `<li class="${candidate.stepOrder === step.stepOrder ? "tag" : ""}">${candidate.stepOrder}. ${escapeHtml(candidate.description)}</li>`).join("")}
                </ul>
            </section>
        </div>
    `;
    views.cook.querySelector("#cook-recipe-select").addEventListener("change", event => {
        state.selectedRecipeId = Number(event.target.value);
        state.cookStepIndex = 0;
        renderCook();
    });
    views.cook.querySelector("#previous-step").addEventListener("click", () => {
        state.cookStepIndex = Math.max(0, state.cookStepIndex - 1);
        renderCook();
    });
    views.cook.querySelector("#next-step").addEventListener("click", () => {
        state.cookStepIndex = Math.min(selected.steps.length - 1, state.cookStepIndex + 1);
        renderCook();
    });
}

function startCooking(recipeId) {
    state.selectedRecipeId = recipeId;
    state.cookStepIndex = 0;
    setView("cook");
}

function openRecipeDialog(recipe = null) {
    state.editingRecipe = recipe;
    const dialog = document.querySelector("#recipe-dialog");
    const form = document.querySelector("#recipe-form");
    form.reset();
    document.querySelector("#recipe-form-title").textContent = recipe ? "Modifier la recette" : "Nouvelle recette";
    form.elements.name.value = recipe?.name || "";
    form.elements.category.value = recipe?.category || "";
    form.elements.servings.value = recipe?.servings || 2;
    form.elements.preparationTimeMinutes.value = recipe?.preparationTimeMinutes || 10;
    form.elements.cookingTimeMinutes.value = recipe?.cookingTimeMinutes || 0;
    form.elements.imageUrl.value = recipe?.imageUrl || "";
    form.elements.description.value = recipe?.description || "";
    form.elements.tags.value = (recipe?.tags || []).join(", ");
    document.querySelector("#ingredient-rows").innerHTML = "";
    document.querySelector("#step-rows").innerHTML = "";
    (recipe?.ingredients || [{ quantity: 1, unit: "piece", ingredientName: "", shoppingCategory: "Epicerie salee" }]).forEach(addIngredientRow);
    (recipe?.steps || [{ description: "", durationMinutes: "" }]).forEach(addStepRow);
    dialog.showModal();
}

function closeRecipeDialog() {
    document.querySelector("#recipe-dialog").close();
}

function addIngredientRow(ingredient = {}) {
    const row = document.createElement("div");
    row.className = "ingredient-row";
    row.innerHTML = `
        <label>Ingredient
            <input name="ingredientName" list="ingredient-options" value="${escapeAttribute(ingredient.ingredientName || "")}" required>
        </label>
        <label>Rayon
            <select name="shoppingCategory">${shoppingCategories.map(category => `<option ${ingredient.shoppingCategory === category ? "selected" : ""}>${category}</option>`).join("")}</select>
        </label>
        <label>Quantite
            <input name="quantity" type="number" min="0.01" step="0.01" value="${ingredient.quantity || 1}" required>
        </label>
        <label>Unite
            <input name="unit" value="${escapeAttribute(ingredient.unit || "piece")}" required>
        </label>
        <button type="button" class="icon-button" title="Retirer">×</button>
    `;
    row.querySelector("button").addEventListener("click", () => row.remove());
    document.querySelector("#ingredient-rows").appendChild(row);
    ensureIngredientDatalist();
}

function addStepRow(step = {}) {
    const row = document.createElement("div");
    row.className = "step-row";
    row.innerHTML = `
        <label>Description
            <textarea name="stepDescription" rows="2" required>${escapeHtml(step.description || "")}</textarea>
        </label>
        <label>Duree
            <input name="durationMinutes" type="number" min="0" value="${step.durationMinutes || ""}">
        </label>
        <button type="button" class="icon-button" title="Retirer">×</button>
    `;
    row.querySelector("button").addEventListener("click", () => row.remove());
    document.querySelector("#step-rows").appendChild(row);
}

function ensureIngredientDatalist() {
    let list = document.querySelector("#ingredient-options");
    if (!list) {
        list = document.createElement("datalist");
        list.id = "ingredient-options";
        document.body.appendChild(list);
    }
    list.innerHTML = state.ingredients.map(ingredient => `<option value="${escapeAttribute(ingredient.name)}"></option>`).join("");
}

async function saveRecipe(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const ingredients = [...document.querySelectorAll(".ingredient-row")].map(row => ({
        ingredientName: row.querySelector('[name="ingredientName"]').value,
        shoppingCategory: row.querySelector('[name="shoppingCategory"]').value,
        quantity: Number(row.querySelector('[name="quantity"]').value),
        unit: row.querySelector('[name="unit"]').value,
        optional: false
    }));
    const steps = [...document.querySelectorAll(".step-row")].map(row => ({
        description: row.querySelector('[name="stepDescription"]').value,
        durationMinutes: numberOrNull(row.querySelector('[name="durationMinutes"]').value)
    }));
    const payload = {
        name: form.elements.name.value,
        description: form.elements.description.value,
        servings: Number(form.elements.servings.value),
        preparationTimeMinutes: Number(form.elements.preparationTimeMinutes.value),
        cookingTimeMinutes: Number(form.elements.cookingTimeMinutes.value),
        imageUrl: form.elements.imageUrl.value,
        category: form.elements.category.value,
        tags: form.elements.tags.value.split(",").map(tag => tag.trim()).filter(Boolean),
        ingredients,
        steps
    };
    try {
        const path = state.editingRecipe ? `/recipes/${state.editingRecipe.id}` : "/recipes";
        const method = state.editingRecipe ? "PUT" : "POST";
        await api(path, { method, body: JSON.stringify(payload) });
        closeRecipeDialog();
        showMessage("Recette enregistree.");
        await refreshAll();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function deleteRecipe(recipeId) {
    if (!confirm("Supprimer cette recette ?")) {
        return;
    }
    try {
        await api(`/recipes/${recipeId}`, { method: "DELETE" });
        showMessage("Recette supprimee.");
        await refreshAll();
    } catch (error) {
        showMessage(error.message, true);
    }
}

function showMessage(message, isError = false) {
    const container = document.querySelector("#messages");
    container.innerHTML = `<div class="message ${isError ? "error" : ""}">${escapeHtml(message)}</div>`;
    window.setTimeout(() => {
        container.innerHTML = "";
    }, 4500);
}

function weekDates() {
    return Array.from({ length: 7 }, (_, index) => {
        const date = new Date(state.weekStart);
        date.setDate(date.getDate() + index);
        return date;
    });
}

function startOfWeek(date) {
    const copy = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
    const day = copy.getUTCDay() || 7;
    copy.setUTCDate(copy.getUTCDate() - day + 1);
    return new Date(copy.getUTCFullYear(), copy.getUTCMonth(), copy.getUTCDate());
}

function isoDate(date) {
    const local = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    local.setMinutes(local.getMinutes() - local.getTimezoneOffset());
    return local.toISOString().slice(0, 10);
}

function formatDate(date) {
    return new Intl.DateTimeFormat("fr-FR", { day: "2-digit", month: "long", year: "numeric" }).format(date);
}

function dayLabel(iso) {
    const date = new Date(`${iso}T00:00:00`);
    return `${dayNames[(date.getDay() + 6) % 7]} ${new Intl.DateTimeFormat("fr-FR", { day: "2-digit", month: "2-digit" }).format(date)}`;
}

function mealLabel(mealType) {
    return mealTypes.find(type => type.key === mealType)?.label || mealType;
}

function formatQuantity(value) {
    return Number(value).toLocaleString("fr-FR", { maximumFractionDigits: 2 });
}

function unique(values) {
    return [...new Set(values)].sort((a, b) => a.localeCompare(b, "fr"));
}

function numberOrNull(value) {
    return value === "" || value == null ? null : Number(value);
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function escapeAttribute(value) {
    return escapeHtml(value).replaceAll("`", "&#096;");
}
