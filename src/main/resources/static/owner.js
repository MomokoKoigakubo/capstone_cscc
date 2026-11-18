const FailedStatus = "alert alert-danger";
const FailedStatusMessage = "Could not load the panel data, Please refresh the page";
const EmptyDash = "—";

const FilterWarning = "alert alert-warning";
const SearchDelayMs = 300;

const feedbackState = {rows: [], filtered: false};
const customerState = {rows: [], filter: ""};
let customerByID = new Map();
let searchTimer = null;
let searchRequest = 0;

document.addEventListener("DOMContentLoaded", loadPanel);
//typing waits for a pause so every key press isn't its own database query
document.getElementById("feedbackSearch").addEventListener("input", function () {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(searchFeedback, SearchDelayMs);
});
["feedbackFrom", "feedbackTo", "feedbackSort"].forEach(id =>
    document.getElementById(id).addEventListener("change", searchFeedback));
document.getElementById("feedbackFilters").addEventListener("submit", function (event) {
    event.preventDefault();
    searchFeedback();
});
//reset clears the inputs after this handler runs, so search on the next tick
document.getElementById("feedbackFilters").addEventListener("reset", function () {
    setTimeout(searchFeedback, 0);
});
document.getElementById("customerSearch").addEventListener("input", function (event) {
    customerState.filter = event.target.value.trim().toLowerCase();
    renderCustomers();
});

async function loadPanel() {
    try {
        const [customers, feedback] = await Promise.all([
            getJson("/api/customers"),
            getJson("/api/feedback")
        ]);
        buildState(customers, feedback);
        renderStats(customers, feedback);
        renderCustomers();
        await searchFeedback();
    } catch (error) {
        //the session expired or was never there, the chain answers json calls with a 401
        if (error.status === 401 || error.status === 403) {
            window.location.assign("/login");
            return;
        }
        console.log(error);
        panelState(FailedStatusMessage, FailedStatus);
        clearBody("feedbackBody", "Feedback unavailable");
        clearBody("customerBody", "Customers unavailable");
    }
}

async function getJson(url) {
    const response = await fetch(url, {headers: {"Accept": "application/json"}});
    if (!response.ok) {
        const error = new Error("request to " + url + " failed");
        error.status = response.status;
        throw error;
    }
    return response.json();
}

function buildState(customers, feedback) {
    customerByID = new Map(customers.map(customer => [customer.customerID, customer]));

    const submissionCounts = new Map();
    feedback.forEach(row => {
        const current = submissionCounts.get(row.customerID) || 0;
        submissionCounts.set(row.customerID, current + 1);
    });

    customerState.rows = customers.map(customer => ({
        ...customer,
        submissions: submissionCounts.get(customer.customerID) || 0
    }));
}

function renderStats(customers, feedback) {
    document.getElementById("statFeedback").textContent = feedback.length.toLocaleString();
    document.getElementById("statCustomers").textContent = customers.length.toLocaleString();

    //the iso strings sort the same as the dates they stand for
    const latest = feedback.map(row => row.submitted || "").sort().pop();
    document.getElementById("statLatest").textContent = latest ? formatDate(latest) : "No feedback yet";
}

/*
* Filtering, sorting and the word counts happen on the server, this only reads the
* inputs, checks the date range and sends them as query parameters.
* */
async function searchFeedback() {
    const keyword = document.getElementById("feedbackSearch").value.trim();
    const from = document.getElementById("feedbackFrom").value;
    const to = document.getElementById("feedbackTo").value;
    const sort = document.getElementById("feedbackSort").value;

    if (from && to && from > to) {
        filterState("The From date has to be on or before the To date", FilterWarning);
        return;
    }
    filterState("", "");

    const params = new URLSearchParams({sort: sort});
    if (keyword) params.set("keyword", keyword);
    if (from) params.set("from", from);
    if (to) params.set("to", to);

    //an older, slower response must not overwrite a newer search
    const requestNumber = ++searchRequest;
    try {
        const result = await getJson("/api/feedback/search?" + params.toString());
        if (requestNumber !== searchRequest) return;

        feedbackState.filtered = Boolean(keyword || from || to);
        feedbackState.rows = result.feedback.map(row => ({
            ...row,
            customer: customerByID.get(row.customerID) || null
        }));
        renderFeedback();
        renderTopWords(result.topWords);
    } catch (error) {
        if (requestNumber !== searchRequest) return;
        if (error.status === 401 || error.status === 403) {
            window.location.assign("/login");
            return;
        }
        console.log(error);
        filterState("Could not run that search, check the filters and try again", FilterWarning);
    }
}

function renderTopWords(words) {
    const container = document.getElementById("topWords");
    container.replaceChildren();

    if (words.length === 0) {
        const none = document.createElement("span");
        none.className = "stat-label small";
        none.textContent = "No words to count";
        container.appendChild(none);
        return;
    }

    words.forEach(entry => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "btn btn-sm btn-outline-light word-chip";
        button.title = "Filter by \"" + entry.word + "\"";
        button.textContent = entry.word + " ";

        const badge = document.createElement("span");
        badge.className = "badge text-bg-secondary";
        badge.textContent = entry.count.toLocaleString();
        button.appendChild(badge);

        button.addEventListener("click", function () {
            document.getElementById("feedbackSearch").value = entry.word;
            searchFeedback();
        });
        container.appendChild(button);
    });
}

function renderFeedback() {
    const rows = feedbackState.rows;
    const body = document.getElementById("feedbackBody");
    body.replaceChildren();

    if (rows.length === 0) {
        body.appendChild(emptyRow(feedbackState.filtered ? "No feedback matches those filters" : "No feedback yet"));
    } else {
        rows.forEach(row => {
            const tr = document.createElement("tr");
            tr.appendChild(cell(row.feedbackID, "num"));
            tr.appendChild(cell(formatDate(row.submitted), "nowrap-cell"));
            tr.appendChild(cell(customerName(row.customer), "nowrap-cell"));
            tr.appendChild(cell(row.customer ? row.customer.email : EmptyDash));
            tr.appendChild(cell(row.feedbackContent, "feedback-text"));
            body.appendChild(tr);
        });
    }
    document.getElementById("feedbackCount").textContent = rows.length.toLocaleString();
}

function renderCustomers() {
    const rows = customerState.rows.filter(row => matchesCustomer(row, customerState.filter));
    const body = document.getElementById("customerBody");
    body.replaceChildren();

    if (rows.length === 0) {
        body.appendChild(emptyRow(customerState.filter ? "No customers match that search" : "No customers yet"));
    } else {
        rows.forEach(row => {
            const tr = document.createElement("tr");
            tr.appendChild(cell(row.customerID, "num"));
            tr.appendChild(cell(row.firstName));
            tr.appendChild(cell(row.lastName));
            tr.appendChild(cell(row.email));
            tr.appendChild(cell(row.submissions, "num"));
            body.appendChild(tr);
        });
    }
    document.getElementById("customerCount").textContent = rows.length.toLocaleString();
}

function matchesCustomer(row, term) {
    if (term === "") return true;
    return [row.firstName, row.lastName, row.email]
        .some(field => (field || "").toLowerCase().includes(term));
}

function customerName(customer) {
    if (customer === null) return EmptyDash;
    const name = [customer.firstName, customer.lastName].filter(part => part).join(" ");
    return name === "" ? EmptyDash : name;
}

/*
* textContent and not innerHTML, feedbackContent is whatever a customer typed into
* the public form so putting it in as markup would run their script in here.
* */
function cell(value, className) {
    const td = document.createElement("td");
    td.textContent = (value === null || value === undefined || value === "") ? EmptyDash : String(value);
    if (className) td.className = className;
    return td;
}

function emptyRow(message) {
    const tr = document.createElement("tr");
    const td = document.createElement("td");
    td.colSpan = 5;
    td.className = "text-center stat-label py-4";
    td.textContent = message;
    tr.appendChild(td);
    return tr;
}

function clearBody(id, message) {
    const body = document.getElementById(id);
    body.replaceChildren();
    body.appendChild(emptyRow(message));
}

function formatDate(value) {
    if (!value) return EmptyDash;
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString(undefined, {dateStyle: "medium", timeStyle: "short"});
}

function filterState(message, type) {
    const status = document.getElementById("filterMessage");
    status.className = type;
    status.textContent = message;
}

function panelState(message, type) {
    const status = document.getElementById("panelMessage");
    status.className = type;
    status.textContent = message;
}
