// var radius = 18;
// var xgap = 120;
// var ygap = 50;

// function drawPlayer(ctx, id, x, y) {
// 	// drawing circle
// 	ctx.beginPath();
// 	ctx.arc(x, y, radius, 0, 2*Math.PI);
// 	ctx.stroke();
// 	// console.log(id + " " + String(id).length);
// 	// displaying id
// 	if (String(id).length == 1)
// 		ctx.fillText(id, x - 5, y + 7);
// 	else if (String(id).length == 2)
// 		ctx.fillText(id, x - 10, y + 7);
// 	else
// 		ctx.fillText(id, x - 15, y + 7);
// }

function process(data) {
	var terms = data.split(",");
	var refresh = Number(terms[0]);
	var turn = Number(terms[1]);
	var n = Number(terms[2]);
	var handles = new Array(n);
	var names = new Array(n);
	var playerMove = new Array(n);
	var i, j = 3, k, len;
	var infoContent = "";

	for (i = 0; i < n; ++ i) {
		names[i] = terms[j];
		++ j;
	}
	for (i = 0; i < n; ++ i) {
		len = Number(terms[j]);
		++ j;
		handles[i] = new Array(len);
		for (k = 0; k < len; ++ k) {
			handles[i][k] = Number(terms[j]);
			playerMove[Number(terms[j])] = i;
			++ j;
		}
	}

	for (i = 0; i < n; ++ i) {
		infoContent += "<p><span class='circled'>p" + (i + 1) + "</span> (" + names[i] + ") grabs handle <span class='rect'>H" + (playerMove[i] + 1) + "</span></p>";
	}
	document.getElementById("info").innerHTML = infoContent;

	var binContent = "";
	for (i = 0; i < n; ++ i) {
		binContent += "<p>Handle <span class='rect'>H" + (i + 1) + "</span>:";
		for (j = 0; j < handles[i].length; ++ j)
			binContent += " <span class='circled'>p" + (handles[i][j] + 1) + "</span>";
		binContent += "</p>";
	}
	document.getElementById("bin").innerHTML = binContent;
	document.getElementById("turn").innerHTML = "Turn: " + turn;
	return refresh;
}

var latest_version = -1;

function ajax(version, retries, timeout) {
	console.log("Version " + version);
	var xhttp = new XMLHttpRequest();
	xhttp.onload = (function() {
			var refresh = -1;
			try {
				if (xhttp.readyState != 4)
					throw "Incomplete HTTP request: " + xhttp.readyState;
				if (xhttp.status != 200)
					throw "Invalid HTTP status: " + xhttp.status;
				//console.log(xhttp.responseText);
				refresh = process(xhttp.responseText);
				//console.log(refresh);
				if (latest_version < version)
					latest_version = version;
				else refresh = -1;
			} catch (message) {
				alert(message);
			}
			if (refresh >= 0)
				setTimeout(function() { ajax(version + 1, 10, 100); }, refresh);
		});
	xhttp.onabort = (function() { location.reload(true); });
	xhttp.onerror = (function() { location.reload(true); });
	xhttp.ontimeout = (function() {
			if (version <= latest_version)
				console.log("AJAX timeout (version " + version + " <= " + latest_version + ")");
			else if (retries == 0)
				location.reload(true);
			else {
				console.log("AJAX timeout (version " + version + ", retries: " + retries + ")");
				ajax(version, retries - 1, timeout * 2);
			}
		});
	xhttp.open("GET", "data.txt", true);
	xhttp.responseType = "text";
	xhttp.timeout = timeout;
	xhttp.send();
}

// ajax(1, 10, 100)

//var data = "12,1,0,2,1,11,0,3,2,3,4,0,0,0,0,0,4,9,10,8,7,1,5,1,6,0";

//process(data);
