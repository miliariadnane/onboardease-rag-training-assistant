document.getElementById('applyApiKeyButton').addEventListener('click', function() {

    var applyApiKeyButton = document.getElementById('applyApiKeyButton');
    var apiKeyInput = document.getElementById('apiKeyInput');
    var apiKey = apiKeyInput.value;
    var lockIcon = document.getElementById('lockIcon');

    if (applyApiKeyButton.getAttribute("data-locked") === "true") {
        apiKeyInput.disabled = false;
        apiKeyInput.type = 'text';
        lockIcon.className = 'fas fa-lock-open text-white';
        applyApiKeyButton.className = 'btn btn-danger';
        applyApiKeyButton.setAttribute("data-locked", "false");
    } else {
        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/api-key', true);
        xhr.setRequestHeader('Content-Type', 'application/json');

        xhr.onload = function () {
            if (xhr.readyState == 4 && xhr.status == "200") {
                apiKeyInput.disabled = true;
                apiKeyInput.type = 'password';
                lockIcon.className = 'fas fa-lock text-white';
                applyApiKeyButton.className = 'btn btn-success';
                applyApiKeyButton.setAttribute("data-locked", "true");
                Swal.fire(
                    'Success!',
                    'API key has been set successfully.',
                    'success'
                );
            } else {
                console.error(xhr.responseText);
                Swal.fire(
                    'Error!',
                    'Failed to set the API key.',
                    'error'
                );
            }
        }

        xhr.send(JSON.stringify({ apiKey: apiKey }));
    }
});
