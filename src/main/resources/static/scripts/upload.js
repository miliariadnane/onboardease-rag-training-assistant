document.getElementById('linkInput').addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        event.preventDefault();

        links.push(this.value);

        var badge = document.createElement('span');
        badge.className = 'badge bg-primary m-1';
        badge.textContent = 'Link ' + links.length;

        var removeButton = document.createElement('button');
        removeButton.className = 'btn-close btn-close-white';
        removeButton.type = 'button';
        removeButton.setAttribute('aria-label', 'Close');
        removeButton.addEventListener('click', function () {
            var index = links.indexOf(badge.textContent);
            if (index > -1) {
                links.splice(index, 1);
            }

            badge.remove();
        });

        badge.appendChild(removeButton);

        document.getElementById('linkBadges').appendChild(badge);

        this.value = '';
    }
});

document.getElementById('uploadButton').addEventListener('click', function(event) {
    event.preventDefault();

    var formData = new FormData();
    var files = document.querySelector('input[type=file]').files;
    for (var i = 0; i < files.length; i++) {
        formData.append('files', files[i]);
    }
    for (var i = 0; i < links.length; i++) {
        formData.append('url', links[i]);
    }

    var xhr = new XMLHttpRequest();
    xhr.open('POST', '/upload', true);

    document.getElementById('uploadSpinner').style.display = 'inline-block';

    xhr.onload = function() {
        document.getElementById('uploadSpinner').style.display = 'none';

        if (xhr.status === 200) {
            swal("Success", "Files uploaded successfully", "success");
            document.getElementById('ingestButton').disabled = false;
        } else {
            swal("Error", "File upload failed", "error");
        }
    };

    xhr.send(formData);
});
