document.getElementById('applyApiKeyButton').addEventListener('click', function() {
    var apiKey = document.getElementById('apiKeyInput').value;
    var lockIcon = document.getElementById('lockIcon');

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/api-key', true);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.onload = function () {
        if (xhr.readyState == 4 && xhr.status == "200") {
            // Change the lock icon and color when the API key is successfully set
            lockIcon.className = 'fas fa-lock text-white';
            document.getElementById('applyApiKeyButton').className = 'btn btn-success';

            // Display a success message
            Swal.fire(
                'Success!',
                'API key has been set successfully.',
                'success'
            );
        } else {
            // Handle the error
            console.error(xhr.responseText);

            // Display an error message
            Swal.fire(
                'Error!',
                'Failed to set the API key.',
                'error'
            );
        }
    }
    xhr.send(JSON.stringify({ apiKey: apiKey }));
});