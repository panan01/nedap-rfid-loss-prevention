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

let dispatcher = new CommandDispatcher([
    {
        aliases: ["dashboard", "main", "home", "dash", "homepage"],
        trigger: function() {
            window.location.href = "dashboard.html";
        }
    }, {
        aliases: ["log out", "log-out", "logout"],
        trigger: logOut
    }, {
        aliases: ["account"],
        trigger: function() {
            window.location.href = "account.html";
        }
    }, {
        aliases: ["data", "report", "data report", "upload", "download", "excel"],
        trigger: function() {
            window.location.href = "data_reports.html";
        }
    }, {
        aliases: ["admin", "admin dashboard", "admin overview"],
        trigger: function() {
            window.location.href = "admin_overview.html";
        }
    }
]);
function makeCommandBar(opts) {
    var element = opts.element;
    var $element = $(element);

    $element.on("input", (event) => {
        if ($element.val()[0] === '/') {
            $element.addClass("command-mode");

            let parseResult = dispatcher.parse($element.val().substring(1));
            if (parseResult.errors.length > 0) {
                $element.addClass("command-error");
            } else {
                $element.removeClass("command-error");
            }
        } else {
            $element.removeClass("command-mode");
            $element.removeClass("command-error");
        }
    });

    $element.keypress((event) => {
        var key = event.which || event.keyCode;

        if (key === 13) { // enter
            event.stopPropagation();
            event.preventDefault();

            if ($element.val()[0] === '/') {
                $element.addClass("command-mode");

                let parseResult = dispatcher.parse($element.val().substring(1));
                if (parseResult.errors.length > 0) {
                    $element.addClass("command-error");
                    console.log(parseResult.errors);
                } else {
                    $element.val("");
                    $element.removeClass("command-mode");
                    $element.removeClass("command-error");
                    parseResult.execute();
                }
            } else {
                $element.removeClass("command-mode");
                $element.removeClass("command-error");
            }
        }
    });
}
$(function() {
    makeCommandBar({
        element: document.getElementById("searchbar")
    });
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
    // document.cookie = "token=; Secure; expires=Thu, 01 Jan 1970 00:00:00 GMT";
    // document.cookie = "email=; Secure; expires=Thu, 01 Jan 1970 00:00:00 GMT";
    sessionStorage.removeItem("token");
    sessionStorage.removeItem("email");
    window.location.href = "login.html";
}

function checkLogin() {
    // check logged in
    try {
        // const token = document.cookie.split('; ').find(row => row.startsWith('token=')).split('=', 2)[1];
        // const email = document.cookie.split('; ').find(row => row.startsWith('email=')).split('=', 2)[1];
        const token = sessionStorage.getItem("token");
        const email = sessionStorage.getItem("email");
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
