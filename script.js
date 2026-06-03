"use strict";

const mockUsers = [
  { id: 1, name: "Alex Morgan", email: "alex@plannermate.app", grade: "11" },
  { id: 2, name: "Maya Ivanova", email: "maya@plannermate.app", grade: "10" }
];

const mockFriends = [
  { id: 1, name: "Maya", status: "Available today", initials: "MI", color: "green" },
  { id: 2, name: "Niko", status: "Free after 17:00", initials: "NK", color: "blue" },
  { id: 3, name: "Sara", status: "Studying until 18:30", initials: "SR", color: "pink" }
];

const mockFriendRequests = [
  { id: 4, name: "Daniel", status: "Wants to plan study sessions", initials: "DN" },
  { id: 5, name: "Elena", status: "Shared cinema interest", initials: "EL" }
];

const mockScheduleItems = [
  {
    id: 1,
    day: "Monday",
    title: "Math revision",
    description: "Practice quadratic equations and check weak problems.",
    startTime: "09:00",
    endTime: "10:30",
    location: "Library",
    repeating: true,
    active: true,
    priority: "high"
  },
  {
    id: 2,
    day: "Monday",
    title: "History notes",
    description: "Summarize chapter 6 before class discussion.",
    startTime: "14:00",
    endTime: "15:00",
    location: "Home desk",
    repeating: false,
    active: true,
    priority: "medium"
  },
  {
    id: 3,
    day: "Tuesday",
    title: "Fitness reset",
    description: "Short workout and walk to clear focus.",
    startTime: "18:00",
    endTime: "19:00",
    location: "Gym",
    repeating: true,
    active: true,
    priority: "low"
  },
  {
    id: 4,
    day: "Wednesday",
    title: "Group project",
    description: "Prepare app database schema presentation.",
    startTime: "16:30",
    endTime: "18:00",
    location: "School lab",
    repeating: false,
    active: true,
    priority: "high"
  },
  {
    id: 5,
    day: "Friday",
    title: "Cinema with friends",
    description: "Confirm tickets and travel time.",
    startTime: "20:00",
    endTime: "22:15",
    location: "City cinema",
    repeating: false,
    active: true,
    priority: "medium"
  },
  {
    id: 6,
    day: "Sunday",
    title: "Weekly review",
    description: "Prepare next week and archive completed tasks.",
    startTime: "17:00",
    endTime: "17:45",
    location: "Home",
    repeating: true,
    active: false,
    priority: "low"
  }
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
  { id: 1, title: "Protect your first focus block", detail: "Move messages and friend planning after math revision." },
  { id: 2, title: "Use a lighter evening", detail: "Fitness and cinema make Friday full. Keep study tasks short." },
  { id: 3, title: "Batch school admin", detail: "Notebook, forms and project files can share one 25-minute slot." }
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
  { icon: "CL", title: "Weekly schedule", text: "Calendar items include time, location, repeating state and active status." },
  { icon: "FR", title: "Friends planning", text: "Preview friend availability and suggest hangouts around real routines." },
  { icon: "IN", title: "Interest matching", text: "Selected interests shape reminders, hangout ideas and study breaks." }
];

const mockSteps = [
  { title: "Add your routine", text: "Create school, study, fitness and friend blocks with clear times." },
  { title: "Pick interests", text: "Choose what matters so suggestions feel personal instead of generic." },
  { title: "Review AI nudges", text: "Use recommendations to reduce overload and keep priorities visible." }
];

const mockHangoutIdeas = [
  { title: "Cinema after project work", detail: "Friday 20:00 works with Maya and Sara." },
  { title: "Study and coffee sprint", detail: "Two 35-minute focus rounds before a short break." },
  { title: "Fitness walk", detail: "Low-cost plan that fits after Tuesday classes." }
];

const weekdays = ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"];

const state = {
  currentRoute: "landing",
  interests: mockInterests.map((interest) => ({ ...interest }))
};

// Future Spring Boot integration point:
// Replace these functions with fetch("/api/...") calls and keep render functions unchanged.
function fetchUsers() {
  return Promise.resolve(mockUsers);
}

function fetchFriends() {
  return Promise.resolve(mockFriends);
}

function fetchFriendRequests() {
  return Promise.resolve(mockFriendRequests);
}

function fetchScheduleItems() {
  return Promise.resolve(mockScheduleItems);
}

function fetchInterests() {
  return Promise.resolve(state.interests);
}

function fetchTasks() {
  return Promise.resolve(mockTasks);
}

function fetchStudyReminders() {
  return Promise.resolve(mockStudyReminders);
}

function fetchAiRecommendations() {
  return Promise.resolve(mockAiRecommendations);
}

const $ = (selector, root = document) => root.querySelector(selector);
const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

document.addEventListener("DOMContentLoaded", () => {
  bindNavigation();
  bindMobileMenu();
  bindModal();
  bindForms();
  bindAuthTabs();
  renderStaticLanding();
  renderAllData();
  navigateToRoute(getInitialRoute());
});

function getInitialRoute() {
  const hashRoute = window.location.hash.replace("#", "");
  return hashRoute || "landing";
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
  const fallbackRoute = $(`[data-page="${route}"]`) ? route : "landing";
  state.currentRoute = fallbackRoute;

  $$(".page-section").forEach((section) => {
    section.classList.toggle("active", section.dataset.page === fallbackRoute);
  });

  $$(".nav-link").forEach((link) => {
    link.classList.toggle("active", link.dataset.route === fallbackRoute);
  });

  const links = $("[data-nav-links]");
  const toggle = $(".menu-toggle");
  links.classList.remove("open");
  toggle.setAttribute("aria-expanded", "false");

  if (updateHash && window.location.hash !== `#${fallbackRoute}`) {
    history.pushState(null, "", `#${fallbackRoute}`);
  }

  window.scrollTo({ top: 0, behavior: "smooth" });
}

function renderStaticLanding() {
  $("#featureGrid").innerHTML = mockFeatures
    .map(
      (feature) => `
        <article class="feature-card">
          <span class="card-icon" aria-hidden="true">${feature.icon}</span>
          <h3>${feature.title}</h3>
          <p>${feature.text}</p>
        </article>
      `
    )
    .join("");

  $("#stepsList").innerHTML = mockSteps
    .map(
      (step) => `
        <li>
          <div>
            <h3>${step.title}</h3>
            <p>${step.text}</p>
          </div>
        </li>
      `
    )
    .join("");
}

async function renderAllData() {
  const [friends, requests, scheduleItems, tasks, reminders, recommendations, interests] = await Promise.all([
    fetchFriends(),
    fetchFriendRequests(),
    fetchScheduleItems(),
    fetchTasks(),
    fetchStudyReminders(),
    fetchAiRecommendations(),
    fetchInterests()
  ]);

  renderDailySchedule(scheduleItems);
  renderCompactList("#upcomingTasks", tasks, true);
  renderCompactList("#studyReminders", reminders);
  renderCompactList("#aiRecommendations", recommendations);
  renderFriendPreview(friends);
  renderFriends(friends);
  renderFriendRequests(requests);
  renderHangoutOptions(friends);
  renderHangoutIdeas();
  renderWeeklyCalendar(scheduleItems);
  renderInterests(interests);
}

function renderDailySchedule(items) {
  const activeItems = items.filter((item) => item.active).slice(0, 4);
  $("#dailySchedule").innerHTML = activeItems.map(renderTimelineItem).join("");
}

function renderTimelineItem(item) {
  return `
    <article class="timeline-item">
      <span class="time-range">${item.startTime}<br />${item.endTime}</span>
      <div>
        <h3>${item.title}</h3>
        <p>${item.description}</p>
      </div>
      <span class="priority ${item.priority}">${item.priority}</span>
    </article>
  `;
}

function renderCompactList(selector, items, showPriority = false) {
  $(selector).innerHTML = items
    .map(
      (item) => `
        <article class="compact-item">
          <h3>${item.title}</h3>
          <p>${item.detail}</p>
          ${showPriority ? `<span class="priority ${item.priority}">${item.priority}</span>` : ""}
        </article>
      `
    )
    .join("");
}

function renderFriendPreview(friends) {
  $("#friendsPreview").innerHTML = friends
    .slice(0, 3)
    .map(
      (friend) => `
        <article class="person-row">
          <span class="avatar">${friend.initials}</span>
          <div>
            <h3>${friend.name}</h3>
            <p>${friend.status}</p>
          </div>
          <span class="status-chip">Open</span>
        </article>
      `
    )
    .join("");
}

function renderFriends(friends) {
  $("#friendList").innerHTML = friends.map(renderPersonRow).join("");
}

function renderFriendRequests(requests) {
  $("#friendRequests").innerHTML = requests
    .map(
      (person) => `
        <article class="person-row">
          <span class="avatar">${person.initials}</span>
          <div>
            <h3>${person.name}</h3>
            <p>${person.status}</p>
          </div>
          <div class="mini-actions" aria-label="Friend request actions">
            <button class="mini-button" type="button" aria-label="Accept request">✓</button>
            <button class="mini-button" type="button" aria-label="Decline request">×</button>
          </div>
        </article>
      `
    )
    .join("");
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
  const friendSelect = $('#hangoutForm select[name="friend"]');
  friendSelect.innerHTML = '<option value="">Choose friend</option>';
  friends.forEach((friend) => {
    const option = document.createElement("option");
    option.value = String(friend.id);
    option.textContent = friend.name;
    friendSelect.append(option);
  });
}

function renderHangoutIdeas() {
  $("#hangoutIdeas").innerHTML = mockHangoutIdeas
    .map(
      (idea) => `
        <article class="idea-item">
          <h3>${idea.title}</h3>
          <p>${idea.detail}</p>
        </article>
      `
    )
    .join("");
}

function renderWeeklyCalendar(items) {
  $("#weeklyCalendar").innerHTML = weekdays
    .map((day) => {
      const dayItems = items.filter((item) => item.day === day);
      return `
        <section class="calendar-day" aria-labelledby="${day.toLowerCase()}-title">
          <h2 id="${day.toLowerCase()}-title">${day}</h2>
          <div class="calendar-items">
            ${
              dayItems.length
                ? dayItems.map(renderCalendarItem).join("")
                : '<article class="calendar-item inactive"><h3>Open time</h3><p>No scheduled items yet.</p></article>'
            }
          </div>
        </section>
      `;
    })
    .join("");
}

function renderCalendarItem(item) {
  return `
    <article class="calendar-item ${item.active ? "" : "inactive"}">
      <h3>${item.title}</h3>
      <p>${item.description}</p>
      <div class="calendar-meta">
        <span>${item.startTime} - ${item.endTime}</span>
        <span>${item.location}</span>
        <span>${item.repeating ? "Repeating" : "One-time"}</span>
        <span>${item.active ? "Active" : "Inactive"}</span>
      </div>
    </article>
  `;
}

function renderInterests(interests) {
  $("#interestGrid").innerHTML = interests
    .map(
      (interest) => `
        <button class="interest-card ${interest.selected ? "selected" : ""}" type="button" data-interest-id="${interest.id}" aria-pressed="${interest.selected}">
          <span class="card-icon" aria-hidden="true">${interest.icon}</span>
          <h2>${interest.name}</h2>
          <p>${interest.description}</p>
        </button>
      `
    )
    .join("");

  updateInterestCount();
  bindInterestCards();
}

function bindInterestCards() {
  $$(".interest-card").forEach((card) => {
    card.addEventListener("click", () => {
      const interestId = Number(card.dataset.interestId);
      const interest = state.interests.find((item) => item.id === interestId);
      interest.selected = !interest.selected;
      card.classList.toggle("selected", interest.selected);
      card.setAttribute("aria-pressed", String(interest.selected));
      updateInterestCount();
    });
  });
}

function updateInterestCount() {
  const selectedCount = state.interests.filter((interest) => interest.selected).length;
  $("#selectedInterestCount").textContent = `${selectedCount} selected`;
}

function bindModal() {
  const modal = $("#scheduleModal");

  document.addEventListener("click", (event) => {
    const openButton = event.target.closest("[data-open-modal]");
    const closeButton = event.target.closest("[data-close-modal]");

    if (openButton) {
      modal.hidden = false;
      modal.querySelector("input").focus();
    }

    if (closeButton || event.target === modal) {
      modal.hidden = true;
    }
  });

  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape" && !modal.hidden) {
      modal.hidden = true;
    }
  });
}

function bindForms() {
  $("#hangoutForm").addEventListener("submit", (event) => {
    event.preventDefault();
    const message = event.currentTarget.querySelector("[data-form-message]");
    showValidationMessage(event.currentTarget, message, "Hangout preview is ready. Backend save will connect later.");
  });

  $("#scheduleItemForm").addEventListener("submit", (event) => {
    event.preventDefault();
    const message = event.currentTarget.querySelector("[data-modal-message]");
    showValidationMessage(event.currentTarget, message, "Schedule item preview saved locally.");
  });

  $$("[data-auth-form]").forEach((form) => {
    form.addEventListener("submit", (event) => {
      event.preventDefault();
      const message = form.querySelector("[data-auth-message]");
      const action = form.dataset.authForm;
      const successText =
        action === "forgot"
          ? "Reset link preview generated. Email API will connect later."
          : "Validation passed. Authentication API will connect later.";
      showValidationMessage(form, message, successText);
    });
  });
}

function showValidationMessage(form, messageElement, successText) {
  const isValid = form.checkValidity();
  messageElement.classList.remove("success", "error");

  if (!isValid) {
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
