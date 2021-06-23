"use strict";!function(){var e=!1,i=[];google.charts.load("current",{packages:["corechart"]}),google.charts.setOnLoadCallback(function(){var t=e=!0,n=!1,a=void 0;try{for(var r,o=i[Symbol.iterator]();!(t=(r=o.next()).done);t=!0){(0,r.value)()}}catch(t){n=!0,a=t}finally{try{t||null==o.return||o.return()}finally{if(n)throw a}}}),$(function(){$(window).keydown(function(t){27==(t.which||t.keyCode)&&$(".modal:not(.modal-noescape)").fadeOut(200)}),app.snackbarContainer=$(".snackbar-container"),$(".fileinput > input[type=file]").on("change",function(){var t=$(this);t.parent().find(".filename").text(t.val().split(/\\|\//).pop())}).on("dragenter",function(){$(this).parent().addClass("dragover")}).on("dragleave dragend drop",function(){$(this).parent().removeClass("dragover")})}),window.app={snackbarContainer:null,graphColors:["#2C7998","#FF6C37","#CE0F3D","#F59E1B","#6A2C70"],drawChart:function(t){e?t():i.push(t)},showModal:function(t){for(var n=(n=$(t)).first();!n.is(".modal:hidden")&&0<n.length;)n=n.parent();n.fadeIn(200)},hideModal:function(t){for(var n=(n=$(t)).first();!n.is(".modal:visible")&&0<n.length;)n=n.parent();n.fadeOut(200)},showSnackbar:function(t,n){var a=1<arguments.length&&void 0!==n?n:5e3;"string"==typeof t&&(t={text:t,duration:a});var r=t.text||"",o=t.html||null,e=t.duration||a,i=t.style||"note",l=t.buttons||[],c=t.onClose,s=$("<div class='card snackbar' style='position: relative; left: 100%; opacity: 0'></div>"),d=$("<div class='card-content'></div>"),u=[];"function"==typeof c&&u.push(c);var f=!1,p={close:function(){if(!f){f=!0,void 0!==T&&clearTimeout(T);var t=!0,n=!1,a=void 0;try{for(var r,o=u[Symbol.iterator]();!(t=(r=o.next()).done);t=!0){(0,r.value)(p)}}catch(t){n=!0,a=t}finally{try{t||null==o.return||o.return()}finally{if(n)throw a}}s.animate({marginTop:-(s.height()+parseInt(s.css("margin-bottom"))),opacity:0},200,"swing",function(){s.remove()})}},setText:function(t){d.text(t)},setHtml:function(t){d.html(t)},onClose:function(t){u.push(t)}};if(o?d.html(o):d.text(r),s.append(d),s.addClass("card-"+i),Array.isArray(l)&&0<l.length){var v=$("<div class='card-content'></div>"),h=!0,y=!1,m=void 0;try{for(var g,b,C=l[Symbol.iterator]();!(h=(g=C.next()).done);h=!0){!function(){var t=g.value;(b=$("<button class='btn-compact btn-outline' style='margin: 0'></button>")).text(t.text||""),b.click(function(){"function"==typeof t.handler&&t.handler(p)}),v.append(b)}()}}catch(t){y=!0,m=t}finally{try{h||null==C.return||C.return()}finally{if(y)throw m}}s.append(v)}this.snackbarContainer.append(s);var w=s.css("margin-top"),x=s.css("margin-bottom"),k=s.height();s.css("margin-top",-(k+parseInt(x))),s.animate({marginTop:w,left:0,opacity:1},200);var T=void 0;return 0<e&&(T=setTimeout(function(){if(!f){var t=f=!0,n=!1,a=void 0;try{for(var r,o=u[Symbol.iterator]();!(t=(r=o.next()).done);t=!0){(0,r.value)(p)}}catch(t){n=!0,a=t}finally{try{t||null==o.return||o.return()}finally{if(n)throw a}}s.animate({marginTop:-(s.height()+parseInt(s.css("margin-bottom"))),opacity:0},200,"swing",function(){s.remove()})}},e)),p}}}();