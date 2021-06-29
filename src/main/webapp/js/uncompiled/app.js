(function() {
    var chartsDrawn = false;
    var chartFuncs = [];

    google.charts.load('current', {'packages':['corechart']});
    google.charts.setOnLoadCallback(drawCharts);

    function drawCharts() {
        chartsDrawn = true;
        for (let func of chartFuncs) {
            func();
        }
    }

    $(function() {
        $(window).keydown(function(e) {
            var key = e.which || e.keyCode;

            if (key == 27) {
                $(".modal:not(.modal-noescape)").fadeOut(200);
            }
        });

        app.snackbarContainer = $(".snackbar-container");

        $(".fileinput > input[type=file]").on("change", function() {
            let $this = $(this);
            $this.parent().find(".filename").text($this.val().split(/\\|\//).pop());
        }).on("dragenter", function() {
            let $this = $(this);
            $this.parent().addClass("dragover");
        }).on("dragleave dragend drop", function() {
            let $this = $(this);
            $this.parent().removeClass("dragover");
        });
    });

    window.app = {
        snackbarContainer: null,

        graphColors: [
            "#2C7998",
            "#FF6C37",
            "#CE0F3D",
            "#F59E1B",
            "#6A2C70"
        ],

        drawChart: function(fn) {
            if (chartsDrawn) {
                fn();
            } else {
                chartFuncs.push(fn);
            }
        },
        
        showModal: function(obj) {
            var $el = $(obj);
            $el = $el.first();
            while (!$el.is(".modal:hidden") && $el.length > 0) {
                $el = $el.parent();
            }
            $el.fadeIn(200);
        },
        hideModal: function(obj) {
            var $el = $(obj);
            $el = $el.first();
            while (!$el.is(".modal:visible") && $el.length > 0) {
                $el = $el.parent();
            }
            $el.fadeOut(200);
        },
        showSnackbar: function(props, dur = 5000) {
            if (typeof props === "string") {
                props = {text: props, duration: dur};
            }

            var text = props.text || "";
            var html = props.html || null;
            var duration = props.duration || dur;
            var style = props.style || "note";
            var buttons = props.buttons || [];
            var onClose = props.onClose;

            var $sb = $("<div class='card snackbar' style='position: relative; left: 100%; opacity: 0'></div>");
            var $sbInner = $("<div class='card-content'></div>");

            var closeListeners = [];
            if (typeof onClose === "function") {
                closeListeners.push(onClose);
            }
            var closed = false;

            var snackbarObject = {
                close: function() {
                    if (closed) {
                        return;
                    }

                    closed = true;

                    if (timeout !== undefined)
                        clearTimeout(timeout);

                    for (let listener of closeListeners) {
                        listener(snackbarObject);
                    }
                    
                    $sb.animate({
                        marginTop: -($sb.height() + parseInt($sb.css("margin-bottom"))),
                        opacity: 0
                    }, 200, "swing", function() {
                        $sb.remove();
                    });
                },
                setText: function(text) {
                    $sbInner.text(text);
                },
                setHtml: function(html) {
                    $sbInner.html(html);
                },
                onClose: function(listener) {
                    closeListeners.push(listener);
                }
            };

            if (html) {
                $sbInner.html(html);
            } else {
                $sbInner.text(text);
            }
            $sb.append($sbInner);
            $sb.addClass("card-" + style);

            if (Array.isArray(buttons) && buttons.length > 0) {
                var $sbButtons = $("<div class='card-content'></div>");

                for (let button of buttons) {
                    var $button = $("<button class='btn-compact btn-outline' style='margin: 0'></button>");
                    $button.text(button.text || "");
                    $button.click(function() {
                        if (typeof button.handler === "function") {
                            button.handler(snackbarObject);
                        }
                    });
                    $sbButtons.append($button);
                }
                $sb.append($sbButtons);
            }

            var $el = this.snackbarContainer;
            $el.append($sb);
            
            var marginTop = $sb.css("margin-top");
            var marginBottom = $sb.css("margin-bottom");
            var height = $sb.height();

            $sb.css("margin-top", -(height + parseInt(marginBottom)));
            $sb.animate({
                marginTop: marginTop,
                left: 0,
                opacity: 1
            }, 200);

            var timeout = undefined;
            if (duration > 0) {
                timeout = setTimeout(function() {
                    if (closed) {
                        return;
                    }

                    closed = true;

                    for (let listener of closeListeners) {
                        listener(snackbarObject);
                    }
                    $sb.animate({
                        marginTop: -($sb.height() + parseInt($sb.css("margin-bottom"))),
                        opacity: 0
                    }, 200, "swing", function() {
                        $sb.remove();
                    });
                }, duration);
            }

            return snackbarObject;
        }
    };
})();