document.getElementById('footer-text').textContent += new Date().getFullYear();

var uploadButton = document.getElementById('uploadButton');
uploadButton.disabled = true;

function getSelectedModel() {
    return document.getElementById('modelSelection').value;
}