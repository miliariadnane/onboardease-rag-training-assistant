let links = [];
const badges = document.getElementById('linkBadges');
const linkInput = document.getElementById('linkInput');
const table = document.getElementById('linkTable');
const tableDiv = document.getElementById('linkTableDiv');

function refreshBadgeAndLinks() {
    Array.from(badges.children).forEach((badge, index) => {
        // Update the text content of each row in the table (skip the header row)
        badge.firstChild.textContent = `Link ${index + 1}`;
    });
    Array.from(table.rows).slice(1).forEach((row, index) => { // slice(1) to skip header row
        row.firstChild.textContent = index + 1;
    });
}

linkInput.addEventListener('keydown', function (event) {
    if (event.key === 'Enter') {
        event.preventDefault();
        links.push(this.value);

        const badge = document.createElement('span');
        badge.className = 'badge bg-primary m-1';

        const badgeNumber = document.createElement('span');
        badgeNumber.textContent = 'Link ' + links.length;
        badge.appendChild(badgeNumber);

        const removeButton = document.createElement('button');
        removeButton.className = 'btn-close btn-close-white ml-1';
        removeButton.type = 'button';
        removeButton.setAttribute('aria-label', 'Close');
        removeButton.addEventListener('click', () => {
            // Get the index of the badge
            const index = Array.from(badges.children).indexOf(badge);

            // Remove the link from the links array and the row from the table
            links.splice(index, 1);
            table.deleteRow(index+1); // add 1 to skip header row
            badge.remove();

            if (!links.length) {
                tableDiv.style.display = 'none';
                linkInput.value = ''; // clear the input
            } else {
                refreshBadgeAndLinks();
            }
        });

        // Add the remove button to the badge and the badge to the badges div
        badge.appendChild(removeButton);
        badges.appendChild(badge);

        // Add a new row to the table
        const row = table.insertRow(-1);
        const cell1 = row.insertCell(0);
        cell1.textContent = links.length;
        const cell2 = row.insertCell(1);
        cell2.innerHTML = '<a href="' + this.value + '" target="_blank">' + this.value + '</a>';

        tableDiv.style.display = 'block'; // show the table when a new link is added

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
