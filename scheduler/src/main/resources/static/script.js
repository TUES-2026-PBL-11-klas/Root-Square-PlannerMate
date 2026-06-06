"use strict";

const API_BASE_URL = "https://planmate-scheduler.onrender.com/api/schedule-items";
const IAM_BASE_URL = "https://planmate-iam.onrender.com";
//aaaaaaaaaaaaaaa

const mockFriends = [
  { id: 1, name: "Maya", status: "Available today", initials: "MI" },
  { id: 2, name: "Niko", status: "Free after 17:00", initials: "NK" },
  { id: 3, name: "Sara", status: "Studying until 18:30", initials: "SR" }
];

const mockFriendRequests = [
  { id: 4, name: "Daniel", status: "Wants to plan study sessions", initials: "DN" },
  { id: 5, name: "Elena", status: "Shared cinema interest", initials: "EL" }
];

const mockTasks = [
  { id: 1, title: "Submit literature essay", detail: "Due Wednesday at 23:59", priority: "high" },
  { id: 2, title: "Buy lab notebook", detail: "Needed for Thursday biology", priority: "medium" },
  { id: 3, title: "Review English vocabulary", detail: "20 minutes before commute", priority: "low" }
];

const mockStudyReminders = [
  { id: 1, title: "Physics formula check", detail: "Repeat once today and once tomorrow." },
  { id: 2, title: "Exam prep block", detail: "Reserve two 45-minute sessions this weekend." },
  { id: 3, title: "Flashcards", detail: "12 cards are ready for quick review." }
];

const mockAiRecommendations = [
  { id: 1, title: "Protect your first focus block", detail: "Move messages and friend planning after revision." },
  { id: 2, title: "Use a lighter evening", detail: "Keep late study tasks short after a packed day." },
  { id: 3, title: "Batch school admin", detail: "Notebook, forms and project files can share one slot." }
];

const mockInterests = [
  { id: 1, name: "Sports", icon: "SP", selected: true, description: "Training, team events and active breaks." },
  { id: 2, name: "Music", icon: "MU", selected: false, description: "Practice time, playlists and concerts." },
  { id: 3, name: "Gaming", icon: "GM", selected: true, description: "Sessions with friends without losing study time." },
  { id: 4, name: "Studying", icon: "ST", selected: true, description: "Revision plans, reminders and exam prep." },
  { id: 5, name: "Cinema", icon: "CI", selected: false, description: "Movie nights and weekend plans." },
  { id: 6, name: "Fitness", icon: "FT", selected: true, description: "Workouts, walks and recovery windows." },
  { id: 7, name: "Technology", icon: "TC", selected: false, description: "Projects, coding and product ideas." },
  { id: 8, name: "Art", icon: "AR", selected: false, description: "Creative breaks and portfolio time." }
];

const mockFeatures = [
  { icon: "AI", title: "Smart day planning", text: "AI-style recommendations help balance school, study time and social plans." },
  { icon: "CL", title: "Connected schedule", text: "Calendar items are loaded from the Spring Boot REST API." },
  { icon: "FR", title: "Friends planning", text: "Preview friend availability and suggest hangouts around real routines." },
  { icon: "IN", title: "Interest matching", text: "Selected interests shape reminders, hangout ideas and study breaks." }
];

const mockSteps = [
  { title: "Add your routine", text: "Create school, study, fitness and friend blocks with clear times." },
  { title: "Review your week", text: "Use the connected schedule view to see real backend data." },
  { title: "Export events", text: "Send schedule items to Google Calendar or download Apple Calendar files." }
];

const mockHangoutIdeas = [
  { title: "Cinema after project work", detail: "Friday evening works with Maya and Sara." },
  { title: "Study and coffee sprint", detail: "Two 35-minute focus rounds before a short break." },
  { title: "Fitness walk", detail: "Low-cost plan that fits after classes." }
];

const weekdays = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];

const state = {
  currentRoute: "landing",
  scheduleItems: [],
  interests: mockInterests.map((interest) => ({ ...interest })),
  editingScheduleId: null
};

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

document.addEventListener("DOMContentLoaded", async () => {
  bindNavigation();
  bindMobileMenu();
  bindModal();
  bindForms();
  bindAuthTabs();
  renderStaticLanding();
  renderMockSections();
  await refreshScheduleItems();
  navigateToRoute(getInitialRoute());
});

function getInitialRoute() {
  return window.location.hash.replace("#", "") || "landing";
}

function bindNavigation() {
  document.addEventListener("click", (event) => {
    const routeTarget = event.target.closest("[data-route]");
    if (!routeTarget) {
      return;
    }

    event.preventDefault();
    navigateToRoute(routeTarget.dataset.route);
  });

  window.addEventListener("hashchange", () => navigateToRoute(getInitialRoute(), false));
}

function bindMobileMenu() {
  const toggle = $(".menu-toggle");
  const links = $("[data-nav-links]");

  toggle.addEventListener("click", () => {
    const isOpen = links.classList.toggle("open");
    toggle.setAttribute("aria-expanded", String(isOpen));
  });
}

function navigateToRoute(route, updateHash = true) {
  const nextRoute = $(`[data-page="${route}"]`) ? route : "landing";
  state.currentRoute = nextRoute;

  $$(".page-section").forEach((section) => {
    section.classList.toggle("active", section.dataset.page === nextRoute);
  });

  $$(".nav-link").forEach((link) => {
    link.classList.toggle("active", link.dataset.route === nextRoute);
  });

  $("[data-nav-links]").classList.remove("open");
  $(".menu-toggle").setAttribute("aria-expanded", "false");

  if (updateHash && window.location.hash !== `#${nextRoute}`) {
    history.pushState(null, "", `#${nextRoute}`);
  }

  window.scrollTo({ top: 0, behavior: "smooth" });
}

function renderStaticLanding() {
  $("#featureGrid").innerHTML = mockFeatures.map((feature) => `
    <article class="feature-card">
      <span class="card-icon" aria-hidden="true">${feature.icon}</span>
      <h3>${feature.title}</h3>
      <p>${feature.text}</p>
    </article>
  `).join("");

  $("#stepsList").innerHTML = mockSteps.map((step) => `
    <li>
      <div>
        <h3>${step.title}</h3>
        <p>${step.text}</p>
      </div>
    </li>
  `).join("");
}

function renderMockSections() {
  renderCompactList("#upcomingTasks", mockTasks, true);
  renderCompactList("#studyReminders", mockStudyReminders);
  renderCompactList("#aiRecommendations", mockAiRecommendations);
  renderFriendPreview(mockFriends);
  renderFriends(mockFriends);
  renderFriendRequests(mockFriendRequests);
  renderHangoutOptions(mockFriends);
  renderHangoutIdeas();
  renderInterests();
}

async function getScheduleItems() {
  return requestJson(API_BASE_URL);
}

async function createScheduleItem(item) {
  return requestJson(API_BASE_URL, {
    method: "POST",
    body: JSON.stringify(item)
  });
}

async function updateScheduleItem(id, item) {
  return requestJson(`${API_BASE_URL}/${id}`, {
    method: "PUT",
    body: JSON.stringify(item)
  });
}

async function deleteScheduleItem(id) {
  return requestJson(`${API_BASE_URL}/${id}`, {
    method: "DELETE"
  });
}

async function requestJson(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    ...options
  });

  if (!response.ok) {
    throw new Error(await readErrorMessage(response));
  }

  if (response.status === 204) {
    return null;
  }

  return response.json();
}

async function readErrorMessage(response) {
  try {
    const body = await response.json();
    if (body.fields) {
      return Object.values(body.fields).join(" ");
    }
    return body.message || response.statusText;
  } catch {
    return response.statusText;
  }
}

async function refreshScheduleItems() {
  try {
    state.scheduleItems = await getScheduleItems();
    $("#apiStatus").textContent = "Backend connected";
    $("#apiStatus").classList.remove("error");
  } catch (error) {
    state.scheduleItems = [];
    $("#apiStatus").textContent = "Backend offline";
    $("#apiStatus").classList.add("error");
  }

  renderDailySchedule(state.scheduleItems);
  renderWeeklyCalendar(state.scheduleItems);
}

function renderDailySchedule(items) {
  const today = toDateInput(new Date());
  const visibleItems = items
    .filter((item) => item.active && item.date >= today)
    .sort(compareScheduleItems)
    .slice(0, 5);

  $("#dailySchedule").innerHTML = visibleItems.length
    ? visibleItems.map(renderTimelineItem).join("")
    : renderEmptyState("No active schedule items from the backend yet.");
}

function renderTimelineItem(item) {
  return `
    <article class="timeline-item">
      <span class="time-range">${item.startTime}<br />${item.endTime}</span>
      <div>
        <h3>${escapeHtml(item.title)}</h3>
        <p>${escapeHtml(item.description || item.location || "No description added.")}</p>
      </div>
      <span class="priority ${item.active ? "medium" : ""}">${item.active ? "active" : "inactive"}</span>
    </article>
  `;
}

function renderCompactList(selector, items, showPriority = false) {
  $(selector).innerHTML = items.map((item) => `
    <article class="compact-item">
      <h3>${item.title}</h3>
      <p>${item.detail}</p>
      ${showPriority ? `<span class="priority ${item.priority}">${item.priority}</span>` : ""}
    </article>
  `).join("");
}

function renderFriendPreview(friends) {
  $("#friendsPreview").innerHTML = friends.slice(0, 3).map(renderPersonRow).join("");
}

function renderFriends(friends) {
  $("#friendList").innerHTML = friends.map(renderPersonRow).join("");
}

function renderFriendRequests(requests) {
  $("#friendRequests").innerHTML = requests.map((person) => `
    <article class="person-row">
      <span class="avatar">${person.initials}</span>
      <div>
        <h3>${person.name}</h3>
        <p>${person.status}</p>
      </div>
      <div class="mini-actions">
        <button class="mini-button" type="button" aria-label="Accept request">✓</button>
        <button class="mini-button" type="button" aria-label="Decline request">×</button>
      </div>
    </article>
  `).join("");
}

function renderPersonRow(person) {
  return `
    <article class="person-row">
      <span class="avatar">${person.initials}</span>
      <div>
        <h3>${person.name}</h3>
        <p>${person.status}</p>
      </div>
      <button class="mini-button" type="button" aria-label="Plan with ${person.name}">+</button>
    </article>
  `;
}

function renderHangoutOptions(friends) {
  const select = $('#hangoutForm select[name="friend"]');
  select.innerHTML = '<option value="">Choose friend</option>';
  friends.forEach((friend) => {
    const option = document.createElement("option");
    option.value = friend.id;
    option.textContent = friend.name;
    select.append(option);
  });
}

function renderHangoutIdeas() {
  $("#hangoutIdeas").innerHTML = mockHangoutIdeas.map((idea) => `
    <article class="idea-item">
      <h3>${idea.title}</h3>
      <p>${idea.detail}</p>
    </article>
  `).join("");
}

function renderWeeklyCalendar(items) {
  const today = toDateInput(new Date());
  const weekDates = getWeekDates(new Date()).map(toDateInput);
  const weekItems = items.filter((item) => weekDates.includes(item.date));

  $("#weeklyCalendar").innerHTML = weekDates.map((dateValue) => {
    const date = parseDateInput(dateValue);
    const dayItems = weekItems.filter((item) => item.date === dateValue).sort(compareScheduleItems);

    return `
      <section class="calendar-day ${dateValue === today ? "today" : ""}" aria-label="${formatLongDate(date)}">
        <h2>${date.toLocaleDateString(undefined, { weekday: "short", month: "short", day: "numeric" })}</h2>
        <div class="calendar-items">
          ${dayItems.length ? dayItems.map(renderCalendarItem).join("") : renderEmptyState("Open time")}
        </div>
      </section>
    `;
  }).join("");
}

function renderCalendarItem(item) {
  return `
    <article class="calendar-item ${item.active ? "" : "inactive"}">
      <h3>${escapeHtml(item.title)}</h3>
      <p>${escapeHtml(item.description || "No description added.")}</p>
      <div class="calendar-meta">
        <span>${item.startTime} - ${item.endTime}</span>
        <span>${escapeHtml(item.location || "No location")}</span>
        <span>${item.repeating ? "Repeating" : "One-time"}</span>
        <span>${item.active ? "Active" : "Inactive"}</span>
      </div>
      <div class="calendar-actions">
        <button class="small-button" type="button" data-schedule-action="edit" data-id="${item.id}">Edit</button>
        <button class="danger-button" type="button" data-schedule-action="delete" data-id="${item.id}">Delete</button>
        <button class="small-button" type="button" data-schedule-action="google" data-id="${item.id}">Add to Google Calendar</button>
        <button class="small-button" type="button" data-schedule-action="apple" data-id="${item.id}">Add to Apple Calendar</button>
      </div>
    </article>
  `;
}

function renderInterests() {
  $("#interestGrid").innerHTML = state.interests.map((interest) => `
    <button class="interest-card ${interest.selected ? "selected" : ""}" type="button" data-interest-id="${interest.id}" aria-pressed="${interest.selected}">
      <span class="card-icon" aria-hidden="true">${interest.icon}</span>
      <h2>${interest.name}</h2>
      <p>${interest.description}</p>
    </button>
  `).join("");

  updateInterestCount();
  bindInterestCards();
}

function bindInterestCards() {
  $$(".interest-card").forEach((card) => {
    card.addEventListener("click", () => {
      const interest = state.interests.find((item) => item.id === Number(card.dataset.interestId));
      interest.selected = !interest.selected;
      card.classList.toggle("selected", interest.selected);
      card.setAttribute("aria-pressed", String(interest.selected));
      updateInterestCount();
    });
  });
}

function updateInterestCount() {
  const count = state.interests.filter((interest) => interest.selected).length;
  $("#selectedInterestCount").textContent = `${count} selected`;
}

function bindModal() {
  const modal = $("#scheduleModal");

  document.addEventListener("click", async (event) => {
    const openButton = event.target.closest("[data-open-modal]");
    const closeButton = event.target.closest("[data-close-modal]");
    const actionButton = event.target.closest("[data-schedule-action]");

    if (openButton) {
      openScheduleModal();
    }

    if (closeButton || event.target === modal) {
      closeScheduleModal();
    }

    if (actionButton) {
      await handleScheduleAction(actionButton);
    }
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !modal.hidden) {
      closeScheduleModal();
    }
  });
}

async function handleScheduleAction(button) {
  const item = state.scheduleItems.find((scheduleItem) => String(scheduleItem.id) === button.dataset.id);
  if (!item) {
    return;
  }

  if (button.dataset.scheduleAction === "edit") {
    openScheduleModal(item);
  }

  if (button.dataset.scheduleAction === "delete") {
    await deleteScheduleItem(item.id);
    await refreshScheduleItems();
  }

  if (button.dataset.scheduleAction === "google") {
    const { url } = await requestJson(`${API_BASE_URL}/${item.id}/google-calendar-url`);
    window.open(url, "_blank", "noopener");
  }

  if (button.dataset.scheduleAction === "apple") {
    const link = document.createElement("a");
    link.href = `${API_BASE_URL}/${item.id}/apple-calendar`;
    link.download = "";
    document.body.append(link);
    link.click();
    link.remove();
  }
}

function openScheduleModal(item = null) {
  const form = $("#scheduleItemForm");
  state.editingScheduleId = item ? item.id : null;
  $("#modalTitle").textContent = item ? "Edit schedule item" : "Add schedule item";
  form.reset();

  form.elements.id.value = item?.id || "";
  form.elements.title.value = item?.title || "";
  form.elements.description.value = item?.description || "";
  form.elements.date.value = item?.date || toDateInput(new Date());
  form.elements.startTime.value = item?.startTime || "09:00";
  form.elements.endTime.value = item?.endTime || "10:00";
  form.elements.location.value = item?.location || "";
  form.elements.repeating.checked = Boolean(item?.repeating);
  form.elements.active.checked = item ? Boolean(item.active) : true;
  form.querySelector("[data-modal-message]").textContent = "";

  $("#scheduleModal").hidden = false;
  form.elements.title.focus();
}

function closeScheduleModal() {
  $("#scheduleModal").hidden = true;
  state.editingScheduleId = null;
}

function bindForms() {
  $$("[data-auth-form]").forEach((form) => {
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const message = form.querySelector("[data-auth-message]");
    message.classList.remove("success", "error");
    message.textContent = "";

    if (form.dataset.authForm === "login") {
      try {
        const response = await fetch(`${IAM_BASE_URL}/api/auth/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            email: form.elements.email.value,
            password: form.elements.password.value
          })
        });
        if (!response.ok) throw new Error("Invalid credentials");
        const data = await response.json();
        localStorage.setItem("token", data.token);
        message.textContent = "Login successful!";
        message.classList.add("success");
        navigateToRoute("dashboard");
      } catch (error) {
        message.textContent = error.message;
        message.classList.add("error");
      }

    } else if (form.dataset.authForm === "register") {
      try {
        const response = await fetch(`${IAM_BASE_URL}/api/auth/register`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            name: form.elements.name.value,
            email: form.elements.email.value,
            password: form.elements.password.value
          })
        });
        if (!response.ok) throw new Error("Registration failed");
        message.textContent = "Account created! Please login.";
        message.classList.add("success");
      } catch (error) {
        message.textContent = error.message;
        message.classList.add("error");
      }

    } else if (form.dataset.authForm === "forgot") {
      message.textContent = "Reset link preview generated.";
      message.classList.add("success");
    }
  });
});
$("#scheduleItemForm").addEventListener("submit", saveScheduleItemFromForm);
}

async function saveScheduleItemFromForm(event) {
  event.preventDefault();
  const form = event.currentTarget;
  const message = form.querySelector("[data-modal-message]");
  message.className = "form-message";
  message.textContent = "";

  if (!form.checkValidity()) {
    form.reportValidity();
    message.textContent = "Please complete the required fields.";
    message.classList.add("error");
    return;
  }

  const item = {
    title: form.elements.title.value.trim(),
    description: form.elements.description.value.trim(),
    date: form.elements.date.value,
    startTime: form.elements.startTime.value,
    endTime: form.elements.endTime.value,
    location: form.elements.location.value.trim(),
    repeating: form.elements.repeating.checked,
    active: form.elements.active.checked
  };

  if (item.endTime <= item.startTime) {
    message.textContent = "End time must be after start time.";
    message.classList.add("error");
    return;
  }

  try {
    if (state.editingScheduleId) {
      await updateScheduleItem(state.editingScheduleId, item);
    } else {
      await createScheduleItem(item);
    }
    await refreshScheduleItems();
    closeScheduleModal();
  } catch (error) {
    message.textContent = error.message;
    message.classList.add("error");
  }
}

function showValidationMessage(form, messageElement, successText) {
  messageElement.classList.remove("success", "error");

  if (!form.checkValidity()) {
    form.reportValidity();
    messageElement.textContent = "Please complete the required fields correctly.";
    messageElement.classList.add("error");
    return;
  }

  messageElement.textContent = successText;
  messageElement.classList.add("success");
}

function bindAuthTabs() {
  $$(".auth-tab").forEach((tabButton) => {
    tabButton.addEventListener("click", () => {
      const view = tabButton.dataset.authView;
      $$(".auth-tab").forEach((button) => button.classList.toggle("active", button === tabButton));
      $$("[data-auth-form]").forEach((form) => {
        form.classList.toggle("active", form.dataset.authForm === view);
      });
    });
  });
}

function renderEmptyState(message) {
  return `<div class="compact-item"><p>${escapeHtml(message)}</p></div>`;
}

function compareScheduleItems(first, second) {
  return `${first.date}T${first.startTime}`.localeCompare(`${second.date}T${second.startTime}`);
}

function getWeekDates(date) {
  const start = startOfWeek(date);
  return Array.from({ length: 7 }, (_, index) => addDays(start, index));
}

function startOfWeek(date) {
  const day = date.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  return addDays(date, diff);
}

function addDays(date, days) {
  const nextDate = new Date(date);
  nextDate.setDate(nextDate.getDate() + days);
  return nextDate;
}

function toDateInput(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function parseDateInput(value) {
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function formatLongDate(date) {
  return date.toLocaleDateString(undefined, { weekday: "long", month: "long", day: "numeric" });
}

function escapeHtml(value) {
  const div = document.createElement("div");
  div.textContent = String(value);
  return div.innerHTML;
}

window.PlanMateSchedule = {
  getScheduleItems,
  createScheduleItem,
  updateScheduleItem,
  deleteScheduleItem
};
