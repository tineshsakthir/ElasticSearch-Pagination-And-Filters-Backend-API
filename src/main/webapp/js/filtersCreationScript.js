function showFilterOptions() {
    document.getElementById("filter-buttons").style.display = 'block';
}

// function addFilterField(filterType) {
//     const filterContainer = document.getElementById("filter-container");
//
//     let filterFieldHTML = '';
//
//     if (["SenderEmail", "ReceiverEmail", "MessageTrace", "Subject", "FromIp", "ToIp"].includes(filterType)) {
//         filterFieldHTML = `<div class="filter-field">
//                     <label>${filterType.replace(/([A-Z])/g, ' $1').trim()}</label>
//                     <input type="text" name="${filterType}" class="form-control" placeholder="Enter ${filterType}">
//                     <button type="button" onclick="removeFilter(this)">Remove</button>
//                 </div>`;
//     } else if (filterType === "MessageSize") {
//         filterFieldHTML = `<div class="filter-field">
//                     <label>Message Size</label>
//                     <input type="number" name="MessageSizeMin" class="form-control" placeholder="Min Size">
//                     <input type="number" name="MessageSizeMax" class="form-control" placeholder="Max Size">
//                     <button type="button" onclick="removeFilter(this)">Remove</button>
//                 </div>`;
//     } else if (filterType === "DateRange") {
//         filterFieldHTML = `<div class="filter-field">
//                     <label>Date Range</label>
//                     <input type="datetime-local" name="DateFrom" class="form-control" placeholder="From Date">
//                     <input type="datetime-local" name="DateTo" class="form-control" placeholder="To Date">
//                     <button type="button" onclick="removeFilter(this)">Remove</button>
//                 </div>`;
//     }
//
//     filterContainer.insertAdjacentHTML('beforeend', filterFieldHTML);
//     document.getElementById("filter-buttons").style.display = 'none';  // Hide the button after adding
// }


function addSenderField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>Sender Email</label>
                <input type="email" name="SENDER[]" class="form-control" placeholder="Enter Sender Email" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('SENDERS').insertAdjacentHTML('beforeend', fieldHTML);
}

function addRecipientField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>Recipient Email</label>
                <input type="email" name="RECIPIENT[]" class="form-control" placeholder="Enter Recipient Email" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('RECIPIENTS').insertAdjacentHTML('beforeend', fieldHTML);
}

function addMessageTraceIdField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>Message Trace ID</label>
                <input type="text" name="MESSAGE_TRACE_ID[]" class="form-control" placeholder="Enter Message Trace ID" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('MESSAGE_TRACE_IDS').insertAdjacentHTML('beforeend', fieldHTML);
}

function addSubjectField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>Subject</label>
                <input type="text" name="SUBJECT[]" class="form-control" placeholder="Enter Subject" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('SUBJECTS').insertAdjacentHTML('beforeend', fieldHTML);
}

function addFromIpField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>From IP</label>
                <input type="text" name="FROM_IP[]" class="form-control" placeholder="Enter From IP" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('FROM_IPS').insertAdjacentHTML('beforeend', fieldHTML);
}

function addToIpField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>To IP</label>
                <input type="text" name="TO_IP[]" class="form-control" placeholder="Enter To IP" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('TO_IPS').insertAdjacentHTML('beforeend', fieldHTML);
}

function addSizeField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>Message Size</label>
                <input type="number" name="SIZE[]" class="form-control" placeholder="Enter Size" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('SIZES').insertAdjacentHTML('beforeend', fieldHTML);
}

function addSizeRangeField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>Message Size Range</label>
                <input type="number" name="SIZE_FROM[]" class="form-control" placeholder="Min Size" required>
                <input type="number" name="SIZE_TO[]" class="form-control" placeholder="Max Size" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('SIZE_RANGES').insertAdjacentHTML('beforeend', fieldHTML);
}

function addReceivedField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>Received Date</label>
                <input type="datetime-local" name="RECEIVED[]" class="form-control" placeholder="Enter Date" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('RECEIVEDS').insertAdjacentHTML('beforeend', fieldHTML);
}

function addReceivedRangeField() {
    const fieldHTML = `
            <div class="filter-field">
                <label>Received Date Range</label>
                <input type="datetime-local" name="RECEIVED_FROM[]" class="form-control" placeholder="From Date" required>
                <input type="datetime-local" name="RECEIVED_TO[]" class="form-control" placeholder="To Date" required>
                <button type="button" onclick="removeFilter(this)">Remove</button>
            </div>`;
    document.getElementById('RECEIVED_RANGES').insertAdjacentHTML('beforeend', fieldHTML);
}
function removeFilter(button) {
    button.parentElement.remove();  // Remove the filter field
}