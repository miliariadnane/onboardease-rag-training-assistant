document.getElementById('applyApiKeyButton').addEventListener('click', function() {
    var apiKey = document.getElementById('apiKeyInput').value;
    var lockIcon = document.getElementById('lockIcon');

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/api-key', true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.onload = function () {
        if (xhr.readyState == 4 && xhr.status == "200") {
            lockIcon.className = 'fas fa-lock text-white';
            document.getElementById('applyApiKeyButton').className = 'btn btn-success';

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
});
