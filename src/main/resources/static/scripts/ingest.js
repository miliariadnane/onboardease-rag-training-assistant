document.getElementById('ingestButton').addEventListener('click', function(event) {
    event.preventDefault();

    let model = getSelectedModel();
    let ingestUrl = '/' + model + '/ingest';

    var xhr = new XMLHttpRequest();
    xhr.open('POST', ingestUrl, true);

    document.getElementById('ingestSpinner').style.display = 'inline-block';

    xhr.onload = function() {
        if (xhr.status === 200) {
            swal({
                title: "Ingestion started",
                text: "Please wait... Ingestion is in progress",
                icon: "info",
                button: false,
                closeOnClickOutside: false,
                closeOnEsc: false
            });

            var progressUrl = '/' + model + '/progress';

            // Start polling for progress
            var progressInterval = setInterval(function() {
                $.get(progressUrl, function(data) {
                    document.getElementById('progressBar').value = data;
                    document.getElementById('progressText').textContent = data + '%';

                    if (data >= 100) {
                        clearInterval(progressInterval);
                        document.getElementById('ingestSpinner').style.display = 'none';
                        swal("Success", "Ingestion completed successfully. You can start asking the model.", "success");
                        document.getElementById('submitBtn').disabled = false;
                        document.getElementById('clearBtn').disabled = false;
                    }
                });
            }, 500); // Poll every 1 second
        } else {
            document.getElementById('ingestSpinner').style.display = 'none';
            swal("Error", "Ingestion failed", "error");
        }
    };

    xhr.send();
});
