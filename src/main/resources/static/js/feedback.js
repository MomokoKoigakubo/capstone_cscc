const statusSuccess = "alert alert-success";
const statusSuccessMessage = "Successfully submitted feedback";
const FailedStatus = "alert alert-danger"
const FailedStatusMessage = "Something went wrong, Please try again";


const feedbackForm = document.getElementById("feedbackForm");
feedbackForm.addEventListener("submit", submission);

async function submission(event) {
event.preventDefault();
let customer = { firstName: "", lastName: "", email: ""};
customer = getValue(customer);
if(customer != null) {

const response = await fetch("/api/customers", {
method: "POST",
headers: { "Content-Type": "application/json" },
body: JSON.stringify(customer)
});

if(!response.ok) {
console.log(response.status);
formState(FailedStatusMessage, FailedStatus);
return;
}
let customerID = await response.json();
console.log(customerID);

let feedbackContent = document.getElementById("textArea").value.trim();

const feedbackResponse = await fetch("/api/feedback", {
method: "POST",
headers: { "Content-Type": "application/json" },
body: JSON.stringify({feedbackContent, customerID})
});

if(!feedbackResponse.ok) {
console.log(feedbackResponse.status);
formState(FailedStatusMessage, FailedStatus);
return;
}
let answer = await feedbackResponse.json();
console.log(answer);
feedbackForm.reset();

formState(statusSuccessMessage, statusSuccess);
}}

function getValue(customer) {
let input = document.getElementById("inputfname");
let userValue = input.value.trim();
customer.firstName = userValue;

input = document.getElementById("inputlname");
userValue = input.value.trim();
customer.lastName = userValue;

if(userValue === "") return null;

input = document.getElementById("inputEmail");
userValue = input.value.trim();
customer.email = userValue;

if(userValue === "") return null;
return customer;
}

function formState(message, type) {
let status = document.getElementById("formMessage");
status.className = type;
status.textContent = message;
}