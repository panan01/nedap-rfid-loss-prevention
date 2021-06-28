// Map marker icons
const s = 2;
const blueMarkerIcon = L.icon({
    iconUrl: 'icons/blue_map_marker.png',
    iconSize: [24*s, 24*s],
    iconAnchor: [12*s, 22*s],
    popupAnchor: [0, -20*s]
});
const redMarkerIcon = L.icon({
    iconUrl: 'icons/red_map_marker.png',
    iconSize: [24*s, 24*s],
    iconAnchor: [12*s, 22*s],
    popupAnchor: [0, -20*s]
});
const greenMarkerIcon = L.icon({
    iconUrl: 'icons/green_map_marker.png',
    iconSize: [24*s, 24*s],
    iconAnchor: [12*s, 22*s],
    popupAnchor: [0, -20*s]
});
const yellowMarkerIcon = L.icon({
    iconUrl: 'icons/yellow_map_marker.png',
    iconSize: [24*s, 24*s],
    iconAnchor: [12*s, 22*s],
    popupAnchor: [0, -20*s]
});
const multiMarkerIcon = L.icon({
    iconUrl: 'icons/multi_map_marker.png',
    iconSize: [24*s, 24*s],
    iconAnchor: [12*s, 22*s],
    popupAnchor: [0, -20*s]
});
const markerIcons = [blueMarkerIcon, redMarkerIcon, greenMarkerIcon, yellowMarkerIcon];
google.charts.load('current', {'packages':['bar']});

$(function() {
    makeCommandBar({
        element: document.getElementById("searchbar")
    });

    app.inject("user.username", "Four-Zero-Four");
});
function navbarMenu() {
    let links = document.getElementById("navbarLinks");
    if (links.style.display === "table") {
        links.style.display = "none";
        links.style.minWidth = "0px";
    } else {
        links.style.display = "table";
        links.style.minWidth = "100px";
    }
}
function logOut() {
    document.cookie = "token=; Secure; expires=Thu, 01 Jan 1970 00:00:00 GMT";
    document.cookie = "email=; Secure; expires=Thu, 01 Jan 1970 00:00:00 GMT";
    window.location.href = "login.html";
}

function checkLogin() {
    // check logged in
    try {
        const token = document.cookie.split('; ').find(row => row.startsWith('token=')).split('=', 2)[1];
        const email = document.cookie.split('; ').find(row => row.startsWith('email=')).split('=', 2)[1];
        $.ajax({
            url: "rest/account/" + email,
            method: 'GET',
            headers: {'Authorization': token},
            dataType: "json",
            success: function(response) {
                console.log(response);
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.log("unsuccessful request");
                window.location.href = "login.html";
            }
        })
        return null;
    } catch (err) {
        if (err instanceof TypeError) {
            window.location.href = "login.html";
        } else {
            throw err;
        }
    }
}
window.onload = checkLogin();
