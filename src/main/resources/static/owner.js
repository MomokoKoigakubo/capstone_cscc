const FailedStatus = "alert alert-danger";
const FailedStatusMessage = "Could not load the panel data, Please refresh the page";
const EmptyDash = "—";

const feedbackState = {rows: [], filter: ""};
const customerState = {rows: [], filter: ""};

document.addEventListener("DOMContentLoaded", loadPanel);
document.getElementById("feedbackSearch").addEventListener("input", function (event) {
    feedbackState.filter = event.target.value.trim().toLowerCase();
    renderFeedback();
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
        renderFeedback();
        renderCustomers();
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
    const customerByID = new Map(customers.map(customer => [customer.customerID, customer]));

    const submissionCounts = new Map();
    feedback.forEach(row => {
        const current = submissionCounts.get(row.customerID) || 0;
        submissionCounts.set(row.customerID, current + 1);
    });

    //newest first, the iso strings sort the same as the dates they stand for
    feedbackState.rows = feedback
        .map(row => ({...row, customer: customerByID.get(row.customerID) || null}))
        .sort((left, right) => (right.submitted || "").localeCompare(left.submitted || ""));

    customerState.rows = customers.map(customer => ({
        ...customer,
        submissions: submissionCounts.get(customer.customerID) || 0
    }));
}

function renderStats(customers, feedback) {
    document.getElementById("statFeedback").textContent = feedback.length.toLocaleString();
    document.getElementById("statCustomers").textContent = customers.length.toLocaleString();

    const latest = feedbackState.rows.length > 0 ? feedbackState.rows[0].submitted : null;
    document.getElementById("statLatest").textContent = latest ? formatDate(latest) : "No feedback yet";
}

function renderFeedback() {
    const rows = feedbackState.rows.filter(row => matchesFeedback(row, feedbackState.filter));
    const body = document.getElementById("feedbackBody");
    body.replaceChildren();

    if (rows.length === 0) {
        body.appendChild(emptyRow(feedbackState.filter ? "No feedback matches that search" : "No feedback yet"));
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

function matchesFeedback(row, term) {
    if (term === "") return true;
    return [row.feedbackContent, customerName(row.customer), row.customer ? row.customer.email : ""]
        .some(field => (field || "").toLowerCase().includes(term));
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

function panelState(message, type) {
    const status = document.getElementById("panelMessage");
    status.className = type;
    status.textContent = message;
}
